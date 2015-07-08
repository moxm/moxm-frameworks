package com.moxm.frameworks.app.samples.event;

import android.support.v4.app.Fragment;

/**
 * Created by Richard on 15/6/2.
 */
public class ContentFragmentEvent {

    public Fragment content;

    public ContentFragmentEvent(Fragment content){
        this.content = content;
    }
}
