package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.HistorySearchedModel;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 3/30/2017.
 */

public class HistorySearchedAdapter extends BaseAdapter {
        private ArrayList<HistorySearchedModel> listData;
        private LayoutInflater layoutInflater;

        public HistorySearchedAdapter(Context aContext, ArrayList<HistorySearchedModel> listData) {
            this.listData = listData;
            layoutInflater = LayoutInflater.from(aContext);
        }


    @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.history_location_searched, null);
                holder = new ViewHolder();
                holder.mainView = (TextView) convertView.findViewById(R.id.history_main_txt);
                holder.secView = (TextView) convertView.findViewById(R.id.history_sec_txt);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mainView.setText(listData.get(position).getMainText());
            holder.secView.setText(listData.get(position).getSecText());
            return convertView;
        }

        static class ViewHolder {
            TextView mainView;
            TextView secView;
        }
}
