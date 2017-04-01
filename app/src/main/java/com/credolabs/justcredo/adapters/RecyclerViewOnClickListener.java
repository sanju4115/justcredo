package com.credolabs.justcredo.adapters;

import android.view.View;

/**
 * Created by Sanjay kumar on 4/1/2017.
 */

public class RecyclerViewOnClickListener {
    /** Interface for Item Click over Recycler View Items **/
    public interface OnClickListener {
        public void OnItemClick(View view, int position);
    }
}
