/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package com.moxm.frameworks.camera;

import android.app.Activity;
import android.content.Intent;

/**
 * 如需使用文件选取功能必须实现该接口，否则无法回调
 */
public interface CameraInterface {

    /**
     * Launch an activity for which you would like a result when it finished. When this activity exits,
     * your onActivityResult() method will be called.
     *
     * @param command     The command object
     * @param intent      The intent to start
     * @param requestCode   The request code that is passed to callback to identify the activity
     */
    abstract public void startActivityForResult(CameraLauncher command, Intent intent, int requestCode);

    /**
     * Set the plugin to be called when a sub-activity exits.
     *
     * @param plugin      The plugin on which onActivityResult is to be called
     */
    abstract public void setActivityResultCallback(CameraLauncher plugin);

    /**
     * Get the Android activity.
     *
     * @return the Activity
     */
    public abstract Activity getSupportActivity();

}
