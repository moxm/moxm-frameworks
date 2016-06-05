/*******************************************************************************
 * Copyright 2014 Sergey Tarasevich
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
package com.moxm.frameworks.fileloader.cache.disc;


import com.moxm.frameworks.fileloader.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for disk cache
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.9.2
 */
public interface DiskCache {
	/**
	 * Returns root directory of disk cache
	 *
	 * @return Root directory of disk cache
	 */
	File getDirectory();

	/**
	 * Returns file of cached image
	 *
	 * @param fileUri Original image URI
	 * @return File of cached image or <b>null</b> if image wasn't cached
	 */
	File get(String fileUri);

	/**
	 * Saves image stream in disk cache.
	 * Incoming image stream shouldn't be closed in this method.
	 *
	 * @param fileUri    Original image URI
	 * @param imageStream Input stream of image (shouldn't be closed in this method)
	 * @param listener    Listener for saving progress
	 * @return <b>true</b> - if image was saved successfully; <b>false</b> - if image wasn't saved in disk cache.
	 * @throws IOException
	 */
	boolean save(String fileUri, InputStream imageStream, IoUtils.CopyListener listener) throws IOException;


	/**
	 * Removes image file associated with incoming URI
	 *
	 * @param fileUri Image URI
	 * @return <b>true</b> - if image file is deleted successfully; <b>false</b> - if image file doesn't exist for
	 * incoming URI or image file can't be deleted.
	 */
	boolean remove(String fileUri);

	/** Closes disk cache, releases resources. */
	void close();

	/** Clears disk cache. */
	void clear();
}
