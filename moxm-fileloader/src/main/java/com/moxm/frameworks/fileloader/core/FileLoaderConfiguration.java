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

import android.content.Context;

import com.moxm.frameworks.fileloader.cache.disc.DiskCache;
import com.moxm.frameworks.fileloader.cache.disc.naming.FileNameGenerator;
import com.moxm.frameworks.fileloader.cache.disc.naming.SimpleFileNameGenerator;
import com.moxm.frameworks.fileloader.core.assist.FlushedInputStream;
import com.moxm.frameworks.fileloader.core.assist.QueueProcessingType;
import com.moxm.frameworks.fileloader.core.download.BaseFileDownloader;
import com.moxm.frameworks.fileloader.core.download.FileDownloader;
import com.moxm.frameworks.fileloader.utils.L;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

/**
 *
 * @since 1.0.0
 */
public final class FileLoaderConfiguration {


	final Executor taskExecutor;
	final boolean customExecutor;

	final int threadPoolSize;
	final int threadPriority;
	final QueueProcessingType tasksProcessingType;

	final DiskCache diskCache;
	final FileDownloader downloader;

	final FileDownloader networkDeniedDownloader;
	final FileDownloader slowNetworkDownloader;

	private FileLoaderConfiguration(final Builder builder) {
		taskExecutor = builder.taskExecutor;
		threadPoolSize = builder.threadPoolSize;
		threadPriority = builder.threadPriority;
		tasksProcessingType = builder.tasksProcessingType;
		diskCache = builder.diskCache;
		downloader = builder.downloader;

		customExecutor = builder.customExecutor;

		networkDeniedDownloader = new NetworkDeniedFileDownloader(downloader);
		slowNetworkDownloader = new SlowNetworkFileDownloader(downloader);

		L.writeDebugLogs(builder.writeLogs);
	}


	public static FileLoaderConfiguration createDefault(Context context) {
		return new Builder(context).build();
	}


	/**
	 * Builder for {@link FileLoaderConfiguration}
	 *
	 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
	 */
	public static class Builder {

		private static final String WARNING_OVERLAP_DISK_CACHE_PARAMS = "diskCache(), diskCacheSize() and diskCacheFileCount calls overlap each other";
		private static final String WARNING_OVERLAP_DISK_CACHE_NAME_GENERATOR = "diskCache() and diskCacheFileNameGenerator() calls overlap each other";
		private static final String WARNING_OVERLAP_MEMORY_CACHE = "memoryCache() and memoryCacheSize() calls overlap each other";
		private static final String WARNING_OVERLAP_EXECUTOR = "threadPoolSize(), threadPriority() and tasksProcessingOrder() calls "
				+ "can overlap taskExecutor() and taskExecutorForCachedImages() calls.";

		/** {@value} */
		public static final int DEFAULT_THREAD_POOL_SIZE = 3;
		/** {@value} */
		public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 2;
		/** {@value} */
		public static final QueueProcessingType DEFAULT_TASK_PROCESSING_TYPE = QueueProcessingType.FIFO;

		private Context context;

		private Executor taskExecutor = null;
		private boolean customExecutor = false;

		private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
		private int threadPriority = DEFAULT_THREAD_PRIORITY;
//		private boolean denyCacheImageMultipleSizesInMemory = false;
		private QueueProcessingType tasksProcessingType = DEFAULT_TASK_PROCESSING_TYPE;

//		private int memoryCacheSize = 0;
		private long diskCacheSize = 0;
		private int diskCacheFileCount = 0;

		private DiskCache diskCache = null;
		private FileNameGenerator diskCacheFileNameGenerator = null;
		private FileDownloader downloader = null;

		private boolean writeLogs = false;

		public Builder(Context context) {
			this.context = context.getApplicationContext();
		}



		/**
		 * Sets custom {@linkplain Executor executor} for tasks of loading and displaying images.<br />
		 * <br />
		 * <b>NOTE:</b> If you set custom executor then following configuration options will not be considered for this
		 * executor:
		 * <ul>
		 * <li>{@link #threadPoolSize(int)}</li>
		 * <li>{@link #threadPriority(int)}</li>
		 * <li>{@link #tasksProcessingOrder(QueueProcessingType)}</li>
		 * </ul>
		 *
		 */
		public Builder taskExecutor(Executor executor) {
			if (threadPoolSize != DEFAULT_THREAD_POOL_SIZE || threadPriority != DEFAULT_THREAD_PRIORITY || tasksProcessingType != DEFAULT_TASK_PROCESSING_TYPE) {
				L.w(WARNING_OVERLAP_EXECUTOR);
			}

			this.taskExecutor = executor;
			return this;
		}



		/**
		 * Sets thread pool size for image display tasks.<br />
		 * Default value - {@link #DEFAULT_THREAD_POOL_SIZE this}
		 */
		public Builder threadPoolSize(int threadPoolSize) {
			if (taskExecutor != null) {
				L.w(WARNING_OVERLAP_EXECUTOR);
			}

			this.threadPoolSize = threadPoolSize;
			return this;
		}

		/**
		 * Sets the priority for image loading threads. Should be <b>NOT</b> greater than {@link Thread#MAX_PRIORITY} or
		 * less than {@link Thread#MIN_PRIORITY}<br />
		 * Default value - {@link #DEFAULT_THREAD_PRIORITY this}
		 */
		public Builder threadPriority(int threadPriority) {
			if (taskExecutor != null) {
				L.w(WARNING_OVERLAP_EXECUTOR);
			}

			if (threadPriority < Thread.MIN_PRIORITY) {
				this.threadPriority = Thread.MIN_PRIORITY;
			} else {
				if (threadPriority > Thread.MAX_PRIORITY) {
					this.threadPriority = Thread.MAX_PRIORITY;
				} else {
					this.threadPriority = threadPriority;
				}
			}
			return this;
		}

		/**
		 * When you display an image in a small {@link android.widget.ImageView ImageView} and later you try to display
		 * this image (from identical URI) in a larger {@link android.widget.ImageView ImageView} so decoded image of
		 * bigger size will be cached in memory as a previous decoded image of smaller size.<br />
		 * So <b>the default behavior is to allow to cache multiple sizes of one image in memory</b>. You can
		 * <b>deny</b> it by calling <b>this</b> method: so when some image will be cached in memory then previous
		 * cached size of this image (if it exists) will be removed from memory cache before.
		 */
		public Builder denyCacheImageMultipleSizesInMemory() {
//			this.denyCacheImageMultipleSizesInMemory = true;
			return this;
		}

		/**
		 * Sets type of queue processing for tasks for loading and displaying images.<br />
		 * Default value - {@link QueueProcessingType#FIFO}
		 */
		public Builder tasksProcessingOrder(QueueProcessingType tasksProcessingType) {
			if (taskExecutor != null) {
				L.w(WARNING_OVERLAP_EXECUTOR);
			}

			this.tasksProcessingType = tasksProcessingType;
			return this;
		}



		/**
		 * Sets maximum disk cache size for images (in bytes).<br />
		 * By default: disk cache is unlimited.<br />
		 * <b>NOTE:</b> If you use this method then
		 * will be used as disk cache. You can use {@link #diskCache(DiskCache)} method for introduction your own
		 * implementation of {@link DiskCache}
		 */
		public Builder diskCacheSize(int maxCacheSize) {
			if (maxCacheSize <= 0) throw new IllegalArgumentException("maxCacheSize must be a positive number");

			if (diskCache != null) {
				L.w(WARNING_OVERLAP_DISK_CACHE_PARAMS);
			}

			this.diskCacheSize = maxCacheSize;
			return this;
		}

		/** @deprecated Use {@link #diskCacheFileCount(int)} instead */
		@Deprecated
		public Builder discCacheFileCount(int maxFileCount) {
			return diskCacheFileCount(maxFileCount);
		}

		/**
		 * Sets maximum file count in disk cache directory.<br />
		 * By default: disk cache is unlimited.<br />
		 * <b>NOTE:</b> If you use this method then
		 * will be used as disk cache. You can use {@link #diskCache(DiskCache)} method for introduction your own
		 * implementation of {@link DiskCache}
		 */
		public Builder diskCacheFileCount(int maxFileCount) {
			if (maxFileCount <= 0) throw new IllegalArgumentException("maxFileCount must be a positive number");

			if (diskCache != null) {
				L.w(WARNING_OVERLAP_DISK_CACHE_PARAMS);
			}

			this.diskCacheFileCount = maxFileCount;
			return this;
		}

		/** @deprecated Use {@link #diskCacheFileNameGenerator(FileNameGenerator)} */
		@Deprecated
		public Builder discCacheFileNameGenerator(FileNameGenerator fileNameGenerator) {
			return diskCacheFileNameGenerator(fileNameGenerator);
		}

		/**
		 * Sets name generator for files cached in disk cache.<br />
		 * Default value -
		 * {@link DefaultConfigurationFactory#createFileNameGenerator()
		 * DefaultConfigurationFactory.createFileNameGenerator()}
		 */
		public Builder diskCacheFileNameGenerator(FileNameGenerator fileNameGenerator) {
			if (diskCache != null) {
				L.w(WARNING_OVERLAP_DISK_CACHE_NAME_GENERATOR);
			}

			this.diskCacheFileNameGenerator = fileNameGenerator;
			return this;
		}

		/** @deprecated Use {@link #diskCache(DiskCache)} */
		@Deprecated
		public Builder discCache(DiskCache diskCache) {
			return diskCache(diskCache);
		}

		public Builder diskCache(DiskCache diskCache) {
			if (diskCacheSize > 0 || diskCacheFileCount > 0) {
				L.w(WARNING_OVERLAP_DISK_CACHE_PARAMS);
			}
			if (diskCacheFileNameGenerator != null) {
				L.w(WARNING_OVERLAP_DISK_CACHE_NAME_GENERATOR);
			}

			this.diskCache = diskCache;
			return this;
		}

		public Builder fileDownloader(FileDownloader downloader) {
			this.downloader = downloader;
			return this;
		}


		public Builder writeDebugLogs() {
			this.writeLogs = true;
			return this;
		}

		/** Builds configured {@link FileLoaderConfiguration} object */
		public FileLoaderConfiguration build() {
			initEmptyFieldsWithDefaultValues();
			return new FileLoaderConfiguration(this);
		}

		private void initEmptyFieldsWithDefaultValues() {
			if (taskExecutor == null) {
				taskExecutor = DefaultConfigurationFactory
						.createExecutor(threadPoolSize, threadPriority, tasksProcessingType);
			} else {
				customExecutor = true;
			}
			if (diskCache == null) {
				if (diskCacheFileNameGenerator == null) {
					diskCacheFileNameGenerator = new SimpleFileNameGenerator();
				}
				diskCache = DefaultConfigurationFactory
						.createDiskCache(context, diskCacheFileNameGenerator, diskCacheSize, diskCacheFileCount);
			}

			if (downloader == null) {
				downloader = new BaseFileDownloader(context);
			}
		}
	}

	/**
	 * Decorator. Prevents downloads from network (throws {@link IllegalStateException exception}).<br />
	 * In most cases this downloader shouldn't be used directly.
	 *
	 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
	 * @since 1.8.0
	 */
	private static class NetworkDeniedFileDownloader implements FileDownloader {

		private final FileDownloader wrappedDownloader;

		public NetworkDeniedFileDownloader(FileDownloader wrappedDownloader) {
			this.wrappedDownloader = wrappedDownloader;
		}

		@Override
		public InputStream getStream(String fileUri, Object extra) throws IOException {
			switch (Scheme.ofUri(fileUri)) {
				case HTTP:
				case HTTPS:
					throw new IllegalStateException();
				default:
					return wrappedDownloader.getStream(fileUri, extra);
			}
		}
	}


	private static class SlowNetworkFileDownloader implements FileDownloader {

		private final FileDownloader wrappedDownloader;

		public SlowNetworkFileDownloader(FileDownloader wrappedDownloader) {
			this.wrappedDownloader = wrappedDownloader;
		}

		@Override
		public InputStream getStream(String fileUri, Object extra) throws IOException {
			InputStream inputStream = wrappedDownloader.getStream(fileUri, extra);
			switch (Scheme.ofUri(fileUri)) {
				case HTTP:
				case HTTPS:
					return new FlushedInputStream(inputStream);
				default:
					return inputStream;
			}
		}
	}
}
