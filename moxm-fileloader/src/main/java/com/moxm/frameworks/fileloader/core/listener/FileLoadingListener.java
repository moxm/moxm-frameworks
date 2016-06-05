/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
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
package com.moxm.frameworks.fileloader.core.listener;


import com.moxm.frameworks.fileloader.core.assist.FailReason;

public interface FileLoadingListener {

	/**
	 * Is called when image loading task was started
	 *
	 * @param fileUri Loading file URI
	 */
	void onLoadingStarted(String fileUri);

	/**
	 * Is called when an error was occurred during image loading
	 *
	 * @param fileUri   Loading file URI
	 * @param failReason {@linkplain FailReason The reason} why image
	 *                   loading was failed
	 */
	void onLoadingFailed(String fileUri, FailReason failReason);

	/**
	 * Is called when image is loaded successfully (and displayed in View if one was specified)
	 *
	 * @param fileUri    Loaded file URI
	 */
	void onLoadingComplete(String fileUri);

	/**
	 * Is called when image loading task was cancelled because View for image was reused in newer task
	 *
	 * @param fileUri Loading file URI
	 */
	void onLoadingCancelled(String fileUri);
}
