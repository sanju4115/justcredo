package com.credolabs.justcredo.adapters;

import android.view.View;

/**
 * Created by Sanjay kumar on 4/1/2017.
 */

public class RecyclerViewOnClickListener {

    @FunctionalInterface
    public interface OnClickListener {
        void OnItemClick(View view, int position);
    }
}
