package com.moxm.frameworks.samples;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.moxm.frameworks.samples.event.ContentFragmentEvent;
import com.moxm.frameworks.samples.otto.BusProvider;
import com.moxm.frameworks.samples.view.BehindFragment;
import com.moxm.frameworks.samples.view.design.DesignFragment;
import com.squareup.otto.Subscribe;


/**
 * Created by Richard on 15/5/29.
 */
public class MainActivity extends SlidingFragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.item_main);
        setBehindContentView(R.layout.menu_frame);

        SlidingMenu menu =  getSlidingMenu();
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);


        getSupportFragmentManager().beginTransaction().replace(R.id.container, new DesignFragment()).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, new BehindFragment()).commit();
    }


    @Subscribe
    public void onContentChangeEvent(ContentFragmentEvent event) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, event.content).commit();
        if (getSlidingMenu().isMenuShowing()) {
            toggle();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggle();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }
}
