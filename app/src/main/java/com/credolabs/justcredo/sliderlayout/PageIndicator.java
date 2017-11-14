package com.credolabs.justcredo.sliderlayout;

/**
 * Created by Sanjay kumar on 10/17/2017.
 */


import android.support.v4.view.ViewPager;


public interface PageIndicator extends ViewPager.OnPageChangeListener {

    void setViewPager(ViewPager view);

    void setViewPager(ViewPager view, int initialPosition);

    void setCurrentItem(int item);

    void setOnPageChangeListener(ViewPager.OnPageChangeListener listener);

    void notifyDataSetChanged();
}