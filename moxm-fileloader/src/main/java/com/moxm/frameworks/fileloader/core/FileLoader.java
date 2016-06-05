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
import android.os.Looper;
import android.text.TextUtils;

import com.moxm.frameworks.fileloader.cache.disc.DiskCache;
import com.moxm.frameworks.fileloader.core.listener.FileLoadingListener;
import com.moxm.frameworks.fileloader.core.listener.FileLoadingProgressListener;
import com.moxm.frameworks.fileloader.core.listener.SimpleFileLoadingListener;
import com.moxm.frameworks.fileloader.utils.L;

public class FileLoader {

	public static final String TAG = FileLoader.class.getSimpleName();

	static final String LOG_INIT_CONFIG = "Initialize ImageLoader with configuration";
	static final String LOG_DESTROY = "Destroy ImageLoader";
	static final String LOG_LOAD_IMAGE_FROM_MEMORY_CACHE = "Load image from memory cache [%s]";

	private static final String WARNING_RE_INIT_CONFIG = "Try to initialize ImageLoader which had already been initialized before. " + "To re-init ImageLoader with new configuration call ImageLoader.destroy() at first.";
	private static final String ERROR_WRONG_ARGUMENTS = "Wrong arguments were passed to loadFile() method (ImageView reference must not be null)";
	private static final String ERROR_NOT_INIT = "ImageLoader must be init with configuration before using";
	private static final String ERROR_INIT_CONFIG_WITH_NULL = "ImageLoader configuration can not be initialized with null";

	private FileLoaderConfiguration configuration;
	private FileLoaderEngine engine;

	private FileLoadingListener defaultListener = new SimpleFileLoadingListener();

	private volatile static FileLoader instance;

	/** Returns singleton class instance */
	public static FileLoader getInstance() {
		if (instance == null) {
			synchronized (FileLoader.class) {
				if (instance == null) {
					instance = new FileLoader();
				}
			}
		}
		return instance;
	}

	protected FileLoader() {
	}


	public synchronized void init(FileLoaderConfiguration configuration) {
		if (configuration == null) {
			throw new IllegalArgumentException(ERROR_INIT_CONFIG_WITH_NULL);
		}
		if (this.configuration == null) {
			L.d(LOG_INIT_CONFIG);
			engine = new FileLoaderEngine(configuration);
			this.configuration = configuration;
		} else {
			L.w(WARNING_RE_INIT_CONFIG);
		}
	}


	public boolean isInited() {
		return configuration != null;
	}


	public void loadFile(String uri) {
		loadFile(uri, null, null);
	}


	public void loadFile(String uri, FileLoadingListener listener) {
		loadFile(uri, listener, null);
	}



	public void loadFile(String uri, FileLoadingListener listener, FileLoadingProgressListener progressListener) {
		checkConfiguration();

		if (listener == null) {
			listener = defaultListener;
		}

		if (TextUtils.isEmpty(uri)) {
			listener.onLoadingStarted(uri);
			listener.onLoadingComplete(uri);
			return;
		}

		listener.onLoadingStarted(uri);

		FileLoadingInfo fileLoadingInfo = new FileLoadingInfo(uri, listener, progressListener, engine.getLockForUri(uri));

		LoadFileTask fileTask = new LoadFileTask(engine, fileLoadingInfo, defineHandler());
		engine.submit(fileTask);


		/*if (bmp != null && !bmp.isRecycled()) {
			L.d(LOG_LOAD_IMAGE_FROM_MEMORY_CACHE, memoryCacheKey);

			if (options.shouldPostProcess()) {
				ImageLoadingInfo imageLoadingInfo = new ImageLoadingInfo(uri, imageAware, targetSize, memoryCacheKey,
						options, listener, progressListener, engine.getLockForUri(uri));
				ProcessAndDisplayImageTask displayTask = new ProcessAndDisplayImageTask(engine, bmp, imageLoadingInfo,
						defineHandler(options));
				if (options.isSyncLoading()) {
					displayTask.run();
				} else {
					engine.submit(displayTask);
				}
			} else {
				options.getDisplayer().display(bmp, imageAware, LoadedFrom.MEMORY_CACHE);
				listener.onLoadingComplete(uri, imageAware.getWrappedView(), bmp);
			}
		} else {
			if (options.shouldShowImageOnLoading()) {
				imageAware.setImageDrawable(options.getImageOnLoading(configuration.resources));
			} else if (options.isResetViewBeforeLoading()) {
				imageAware.setImageDrawable(null);
			}

			ImageLoadingInfo imageLoadingInfo = new ImageLoadingInfo(uri, imageAware, targetSize, memoryCacheKey,
					options, listener, progressListener, engine.getLockForUri(uri));
			LoadAndDisplayImageTask displayTask = new LoadAndDisplayImageTask(engine, imageLoadingInfo,
					defineHandler(options));
			if (options.isSyncLoading()) {
				displayTask.run();
			} else {
				engine.submit(displayTask);
			}
		}*/
	}


	/**
	 * Checks if ImageLoader's configuration was initialized
	 *
	 * @throws IllegalStateException if configuration wasn't initialized
	 */
	private void checkConfiguration() {
		if (configuration == null) {
			throw new IllegalStateException(ERROR_NOT_INIT);
		}
	}

	/** Sets a default loading listener for all display and loading tasks. */
	public void setDefaultLoadingListener(FileLoadingListener listener) {
		defaultListener = listener == null ? new SimpleFileLoadingListener() : listener;
	}



	public DiskCache getDiskCache() {
		checkConfiguration();
		return configuration.diskCache;
	}

	public void clearDiskCache() {
		checkConfiguration();
		configuration.diskCache.clear();
	}



	public void denyNetworkDownloads(boolean denyNetworkDownloads) {
		engine.denyNetworkDownloads(denyNetworkDownloads);
	}

	public void handleSlowNetwork(boolean handleSlowNetwork) {
		engine.handleSlowNetwork(handleSlowNetwork);
	}

	/**
	 * Pause ImageLoader. All new "load&display" tasks won't be executed until ImageLoader is {@link #resume() resumed}.
	 * <br />
	 * Already running tasks are not paused.
	 */
	public void pause() {
		engine.pause();
	}

	/** Resumes waiting "load&display" tasks */
	public void resume() {
		engine.resume();
	}

	
	public void stop() {
		engine.stop();
	}

	public void destroy() {
		if (configuration != null) L.d(LOG_DESTROY);
		stop();
		configuration.diskCache.close();
		engine = null;
		configuration = null;
	}

	private static Handler defineHandler() {
		Handler handler = new Handler();
		if (handler == null && Looper.myLooper() == Looper.getMainLooper()) {
			handler = new Handler();
		}
		return handler;
	}

	/**
	 * Listener which is designed for synchronous image loading.
	 *
	 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
	 * @since 1.9.0
	 */
	private static class SyncFileLoadingListener extends SimpleFileLoadingListener {

		@Override
		public void onLoadingComplete(String imageUri) {
		}
	}
}
