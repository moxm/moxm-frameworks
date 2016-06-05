/*
 * Copyright (C) 2013 Leszek Mzyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moxm.frameworks.support.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * A ViewPager subclass enabling infinte scrolling of the viewPager elements
 * 
 * When used for paginating views (in opposite to fragments), no code changes
 * should be needed only change xml's from <android.support.v4.view.ViewPager>
 * to <com.imbryk.viewPager.LoopViewPager>
 * 
 * If "blinking" can be seen when paginating to first or last view, simply call
 * seBoundaryCaching( true ), or change DEFAULT_BOUNDARY_CASHING to true
 * 
 * When using a FragmentPagerAdapter or FragmentStatePagerAdapter,
 * additional changes in the adapter must be done. 
 * The adapter must be prepared to create 2 extra items e.g.:
 * 
 * The original adapter creates 4 items: [0,1,2,3]
 * The modified adapter will have to create 6 items [0,1,2,3,4,5]
 * with mapping realPosition=(position-1)%count
 * [0->3, 1->0, 2->1, 3->2, 4->3, 5->0]
 */
public class LoopPager extends ViewPager {

    private static final boolean DEFAULT_BOUNDARY_CASHING = false;

    OnPageChangeListener mOuterPageChangeListener;
    private LoopPagerWrapper mAdapter;
    private boolean mBoundaryCaching = DEFAULT_BOUNDARY_CASHING;
    
    private static final boolean START_AUTO_SCROLL = true;
    private static final boolean STOP_AUTO_SCROLL = false;
    private boolean isAutoScroll = STOP_AUTO_SCROLL;
    private static final long DEFAULT_INTERVAL = 1500;
    /** auto scroll time in milliseconds, default is {@link #DEFAULT_INTERVAL} **/
    private long interval = DEFAULT_INTERVAL;
    private long oldTime = 0;
    private int scrollState = -1;
    
    /**
     * helper function which may be used when implementing FragmentPagerAdapter
     *   
     * @param position
     * @param count
     * @return (position-1)%count
     */
    public static int toRealPosition( int position, int count ){
    	
        position = position-1;
        if( position < 0 ){
            position += count;
        }else{
            position = position%count;
        }
        return position;
    }
    
    /**
     * If set to true, the boundary views (i.e. first and last) will never be destroyed
     * This may help to prevent "blinking" of some views 
     * 
     * @param flag
     */
    public void setBoundaryCaching(boolean flag) {
        mBoundaryCaching = flag;
        if (mAdapter != null) {
            mAdapter.setBoundaryCaching(flag);
        }
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        mAdapter = new LoopPagerWrapper(adapter);
        mAdapter.setBoundaryCaching(mBoundaryCaching);
        super.setAdapter(mAdapter);
        setCurrentItem(0, false);
    }

    @Override
    public PagerAdapter getAdapter() {
        return mAdapter != null ? mAdapter.getRealAdapter() : mAdapter;
    }

    @Override
    public int getCurrentItem() {
        return mAdapter != null ? mAdapter.toRealPosition(super.getCurrentItem()) : 0;
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        int realItem = mAdapter.toInnerPosition(item);
        super.setCurrentItem(realItem, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        if (getCurrentItem() != item) {
            setCurrentItem(item, true);
        }
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mOuterPageChangeListener = listener;
    };

    public LoopPager(Context context) {
        super(context);
        init();
    }

    public LoopPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        super.setOnPageChangeListener(onPageChangeListener);
    }

    private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
        private float mPreviousOffset = -1;
        private float mPreviousPosition = -1;

        @Override
        public void onPageSelected(int position) {

            int realPosition = mAdapter.toRealPosition(position);
            if (mPreviousPosition != realPosition) {
                mPreviousPosition = realPosition;
                if (mOuterPageChangeListener != null) {
                    mOuterPageChangeListener.onPageSelected(realPosition);
                }
            }
            //页面翻页完成
            if(scrollState == 2){
            	oldTime = System.currentTimeMillis();
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                int positionOffsetPixels) {
            int realPosition = position;
            if (mAdapter != null) {
                realPosition = mAdapter.toRealPosition(position);

                if (positionOffset == 0
                        && mPreviousOffset == 0
                        && (position == 0 || position == mAdapter.getCount() - 1)) {
                    setCurrentItem(realPosition, true);
                }
            }

            mPreviousOffset = positionOffset;
            if (mOuterPageChangeListener != null) {
                if (realPosition != mAdapter.getRealCount() - 1) {
                    mOuterPageChangeListener.onPageScrolled(realPosition,
                            positionOffset, positionOffsetPixels);
                } else {
                    if (positionOffset > .5) {
                        mOuterPageChangeListener.onPageScrolled(0, 0, 0);
                    } else {
                        mOuterPageChangeListener.onPageScrolled(realPosition,
                                0, 0);
                    }
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        	scrollState = state;//记录翻页状态state_1正在翻页,state_2翻页完成,state_0空闲状态
            if (mAdapter != null) {
                int position = LoopPager.super.getCurrentItem();
                int realPosition = mAdapter.toRealPosition(position);
                if (state == ViewPager.SCROLL_STATE_IDLE
                        && (position == 0 || position == mAdapter.getCount() - 1)) {
                    setCurrentItem(realPosition, false);
                }
            }
            if (mOuterPageChangeListener != null) {
                mOuterPageChangeListener.onPageScrollStateChanged(state);
            }
        }
    };

    
    
   
    
    
    
     
	
	/**
     * show next view
     */
    public void showNext() {
        PagerAdapter adapter = getAdapter();
        if ((null == adapter) || (adapter.getCount() <= 1)) {
			return;
		}
        int currentItem = getCurrentItem();
        //smoothScroll参数设置为true，否则滑到第一个或最后一个页面会直接跳转，不会平滑滑动
        setCurrentItem(currentItem + 1, true);
//        Log.i("MainActivity", "showNext currentItem + 1 = " + (currentItem + 1));
    }
    
   
    /**
     * auto scroll time interval in milliseconds, default is {@link #DEFAULT_INTERVAL}
     * 
     * @param interval 间隔时间，单位是毫秒
     */
	public void setInterval(long interval) {
		this.interval = interval;
	}
	
	Thread mScrollThread;
	private final Handler scrollHandler = new Handler();

	private class ScrollRunnable implements Runnable {

		@Override
		public void run() {
			Looper.prepare();
			Thread.currentThread();
			oldTime = System.currentTimeMillis();
			while (isAutoScroll) {// 三秒钟下一页
				try {
					Thread.sleep(100);
					long curTime = System.currentTimeMillis();
					long intervalTime = curTime - oldTime;
					if (intervalTime >= interval) {
						scrollHandler.post(new Runnable() {
							public void run() {
								showNext();
							}
						});
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	}
	
	/**
     * start auto scroll, first scroll delay time is {@link #getInterval()}
     */
    public void startAutoScroll() {
        isAutoScroll = START_AUTO_SCROLL;
        mScrollThread = new Thread(new ScrollRunnable());
    	mScrollThread.start();
    }
    
    /**
     * stop auto scroll
     */
    public void stopAutoScroll() {
        isAutoScroll = STOP_AUTO_SCROLL;
    }
}
