/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.moxm.frameworks.fileloader.core;

import android.os.Handler;

import com.moxm.frameworks.fileloader.core.assist.FailReason;
import com.moxm.frameworks.fileloader.core.assist.FailReason.FailType;
import com.moxm.frameworks.fileloader.core.assist.LoadedFrom;
import com.moxm.frameworks.fileloader.core.download.FileDownloader;
import com.moxm.frameworks.fileloader.core.download.FileDownloader.Scheme;
import com.moxm.frameworks.fileloader.core.listener.FileLoadingListener;
import com.moxm.frameworks.fileloader.core.listener.FileLoadingProgressListener;
import com.moxm.frameworks.fileloader.utils.IoUtils;
import com.moxm.frameworks.fileloader.utils.L;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;


final class LoadFileTask implements Runnable, IoUtils.CopyListener {

	private static final String LOG_WAITING_FOR_RESUME = "ImageLoader is paused. Waiting...  [%s]";
	private static final String LOG_RESUME_AFTER_PAUSE = ".. Resume loading [%s]";
	private static final String LOG_DELAY_BEFORE_LOADING = "Delay %d ms before loading...  [%s]";
	private static final String LOG_START_DISPLAY_IMAGE_TASK = "Start display image task [%s]";
	private static final String LOG_WAITING_FOR_IMAGE_LOADED = "Image already is loading. Waiting... [%s]";
	private static final String LOG_GET_IMAGE_FROM_MEMORY_CACHE_AFTER_WAITING = "...Get cached bitmap from memory after waiting. [%s]";
	private static final String LOG_LOAD_IMAGE_FROM_NETWORK = "Load image from network [%s]";
	private static final String LOG_LOAD_IMAGE_FROM_DISK_CACHE = "Load image from disk cache [%s]";
	private static final String LOG_RESIZE_CACHED_IMAGE_FILE = "Resize image in disk cache [%s]";
	private static final String LOG_PREPROCESS_IMAGE = "PreProcess image before caching in memory [%s]";
	private static final String LOG_POSTPROCESS_IMAGE = "PostProcess image before displaying [%s]";
	private static final String LOG_CACHE_IMAGE_IN_MEMORY = "Cache image in memory [%s]";
	private static final String LOG_CACHE_IMAGE_ON_DISK = "Cache image on disk [%s]";
	private static final String LOG_PROCESS_IMAGE_BEFORE_CACHE_ON_DISK = "Process image before cache on disk [%s]";
	private static final String LOG_TASK_CANCELLED_IMAGEAWARE_REUSED = "ImageAware is reused for another image. Task is cancelled. [%s]";
	private static final String LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED = "ImageAware was collected by GC. Task is cancelled. [%s]";
	private static final String LOG_TASK_INTERRUPTED = "Task was interrupted [%s]";

	private static final String ERROR_NO_IMAGE_STREAM = "No stream for image [%s]";
	private static final String ERROR_PRE_PROCESSOR_NULL = "Pre-processor returned null [%s]";
	private static final String ERROR_POST_PROCESSOR_NULL = "Post-processor returned null [%s]";
	private static final String ERROR_PROCESSOR_FOR_DISK_CACHE_NULL = "Bitmap processor for disk cache returned null [%s]";

	private final FileLoaderEngine engine;
	private final FileLoadingInfo imageLoadingInfo;
	private final Handler handler;

	// Helper references
	private final FileLoaderConfiguration configuration;
	private final FileDownloader downloader;
	private final FileDownloader networkDeniedDownloader;
	private final FileDownloader slowNetworkDownloader;
	final String uri;
//	private final String memoryCacheKey;
	final FileLoadingListener listener;
	final FileLoadingProgressListener progressListener;
	private final boolean syncLoading;

	// State vars
	private LoadedFrom loadedFrom = LoadedFrom.NETWORK;

	public LoadFileTask(FileLoaderEngine engine, FileLoadingInfo fileLoadingInfo, Handler handler) {
		this.engine = engine;
		this.imageLoadingInfo = fileLoadingInfo;
		this.handler = handler;

		configuration = engine.configuration;
		downloader = configuration.downloader;
		networkDeniedDownloader = configuration.networkDeniedDownloader;
		slowNetworkDownloader = configuration.slowNetworkDownloader;
		uri = imageLoadingInfo.uri;
//		memoryCacheKey = imageLoadingInfo.memoryCacheKey;
		listener = imageLoadingInfo.listener;
		progressListener = imageLoadingInfo.progressListener;
		syncLoading = false;
	}

	@Override
	public void run() {
		if (waitIfPaused()) return;
		if (delayIfNeed()) return;

		ReentrantLock loadFromUriLock = imageLoadingInfo.loadFromUriLock;
		if (loadFromUriLock.isLocked()) {
		}

		loadFromUriLock.lock();
		File file;
		try {
			checkTaskNotActual();

			file = tryLoadFile();
			if (file == null) return; // listener callback already was fired

			checkTaskNotActual();
			checkTaskInterrupted();
		} catch (TaskCancelledException e) {
			fireCancelEvent();
			return;
		} finally {
			loadFromUriLock.unlock();
		}
	}

	/** @return <b>true</b> - if task should be interrupted; <b>false</b> - otherwise */
	private boolean waitIfPaused() {
		AtomicBoolean pause = engine.getPause();
		if (pause.get()) {
			synchronized (engine.getPauseLock()) {
				if (pause.get()) {
					try {
						engine.getPauseLock().wait();
					} catch (InterruptedException e) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/** @return <b>true</b> - if task should be interrupted; <b>false</b> - otherwise */
	private boolean delayIfNeed() {

		return false;
	}

	private File tryLoadFile() throws TaskCancelledException {
		File file = null;
		try {
			File loadFile = configuration.diskCache.get(uri);
			if (loadFile != null && loadFile.exists() && loadFile.length() > 0) {
				loadedFrom = LoadedFrom.DISC_CACHE;

				checkTaskNotActual();
				file = loadFile;
				if (listener != null) {
					Runnable r = new Runnable() {
						@Override
						public void run() {
							listener.onLoadingComplete(uri);
						}
					};
					runTask(r, false, handler, engine);
				}
			}
			if (file == null) {
				loadedFrom = LoadedFrom.NETWORK;

				String imageUriForDecoding = uri;
				if (tryCacheImageOnDisk()) {
					file = configuration.diskCache.get(uri);
					if (loadFile != null) {
						imageUriForDecoding = Scheme.FILE.wrap(loadFile.getAbsolutePath());
					}
				}

				checkTaskNotActual();
				file = new File(imageUriForDecoding);

				if (file == null) {
					fireFailEvent(FailType.DECODING_ERROR, null);
				}
			}
		} catch (IllegalStateException e) {
			fireFailEvent(FailType.NETWORK_DENIED, null);
		} catch (TaskCancelledException e) {
			throw e;
		} catch (OutOfMemoryError e) {
			L.e(e);
			fireFailEvent(FailType.OUT_OF_MEMORY, e);
		} catch (Throwable e) {
			L.e(e);
			fireFailEvent(FailType.UNKNOWN, e);
		}
		return file;
	}


	/** @return <b>true</b> - if image was downloaded successfully; <b>false</b> - otherwise */
	private boolean tryCacheImageOnDisk() throws TaskCancelledException {

		boolean loaded;
		try {
			loaded = downloadImage();
			if (listener != null) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						listener.onLoadingComplete(uri);
					}
				};
				runTask(r, false, handler, engine);
			}
		} catch (IOException e) {
			L.e(e);
			loaded = false;
		}
		return loaded;
	}

	private boolean downloadImage() throws IOException {
		InputStream is = getDownloader().getStream(uri, getDownloader());
		if (is == null) {
			return false;
		} else {
			try {
				return configuration.diskCache.save(uri, is, this);
			} finally {
				IoUtils.closeSilently(is);
			}
		}
	}


	@Override
	public boolean onBytesCopied(int current, int total) {
		return syncLoading || fireProgressEvent(current, total);
	}

	/** @return <b>true</b> - if loading should be continued; <b>false</b> - if loading should be interrupted */
	private boolean fireProgressEvent(final int current, final int total) {
		if (isTaskInterrupted()) return false;
		if (progressListener != null) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					progressListener.onProgressUpdate(uri, current, total);
				}
			};
			runTask(r, false, handler, engine);
		}
		return true;
	}

	private void fireFailEvent(final FailType failType, final Throwable failCause) {
		if (syncLoading || isTaskInterrupted()) return;
		Runnable r = new Runnable() {
			@Override
			public void run() {
				listener.onLoadingFailed(uri, new FailReason(failType, failCause));
			}
		};
		runTask(r, false, handler, engine);
	}

	private void fireCancelEvent() {
		if (syncLoading || isTaskInterrupted()) return;
		Runnable r = new Runnable() {
			@Override
			public void run() {
				listener.onLoadingCancelled(uri);
			}
		};
		runTask(r, false, handler, engine);
	}

	private FileDownloader getDownloader() {
		FileDownloader d;
		if (engine.isNetworkDenied()) {
			d = networkDeniedDownloader;
		} else if (engine.isSlowNetwork()) {
			d = slowNetworkDownloader;
		} else {
			d = downloader;
		}
		return d;
	}

	/**
	 * @throws TaskCancelledException if task is not actual (target ImageAware is collected by GC or the image URI of
	 *                                this task doesn't match to image URI which is actual for current ImageAware at
	 *                                this moment)
	 */
	private void checkTaskNotActual() throws TaskCancelledException {
	}




	/** @throws TaskCancelledException if current task was interrupted */
	private void checkTaskInterrupted() throws TaskCancelledException {
		if (isTaskInterrupted()) {
			throw new TaskCancelledException();
		}
	}

	/** @return <b>true</b> - if current task was interrupted; <b>false</b> - otherwise */
	private boolean isTaskInterrupted() {
		if (Thread.interrupted()) {
			return true;
		}
		return false;
	}

	String getLoadingUri() {
		return uri;
	}

	static void runTask(Runnable r, boolean sync, Handler handler, FileLoaderEngine engine) {
		if (sync) {
			r.run();
		} else if (handler == null) {
			engine.fireCallback(r);
		} else {
			handler.post(r);
		}
	}

	/**
	 * Exceptions for case when task is cancelled (thread is interrupted, image view is reused for another task, view is
	 * collected by GC).
	 *
	 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
	 * @since 1.9.1
	 */
	class TaskCancelledException extends Exception {
	}
}
