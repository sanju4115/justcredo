package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.credolabs.justcredo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Sanjay kumar on 4/6/2017.
 */

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> expandableListTitle;
    private HashMap<String, LinkedHashMap<String,String>> expandableListDetail;

    public CustomExpandableListAdapter(Context context, List<String> expandableListTitle,
                                       HashMap<String, LinkedHashMap<String, String>> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        //final String expandedListText = (String) getChild(listPosition, expandedListPosition);
        LinkedHashMap<String, String> hMap = expandableListDetail.get(this.expandableListTitle.get(listPosition));
        List<String> feeType = new ArrayList<String>(hMap.keySet());
        List<String> fee = new ArrayList<String>(hMap.values());


        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.school_fee_list_item, null);
        }
        TextView expandedListTextViewFeeType = (TextView) convertView
                .findViewById(R.id.expandedListItem);
        expandedListTextViewFeeType.setText(feeType.get(expandedListPosition));

        TextView expandedListTextViewFee = (TextView) convertView
                .findViewById(R.id.fees);
        expandedListTextViewFee.setText(fee.get(expandedListPosition));
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.school_fee_group, null);
        }
        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
