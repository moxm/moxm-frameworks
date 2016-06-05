package com.moxm.frameworks.http.task;

/**
 * Author: richard.ma
 * Time: 2014/05/23
 */
public interface TaskHandler {

    boolean supportPause();

    boolean supportResume();

    boolean supportCancel();

    void pause();

    void resume();

    void cancel();

    boolean isPaused();

    boolean isCancelled();
}
