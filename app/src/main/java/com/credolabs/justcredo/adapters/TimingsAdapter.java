package com.credolabs.justcredo.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.credolabs.justcredo.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Sanjay kumar on 4/9/2017.
 */

public class TimingsAdapter extends ArrayAdapter<String> {
    private HashMap<String, HashMap<String, String>> list;
    private Context context;
    private Activity mActivity;
    ArrayList<String> days;


    public TimingsAdapter(Context context, HashMap<String, HashMap<String, String>> list, Activity activity) {
        super(context,R.layout.school_hours_item);
        // TODO Auto-generated constructor stub
        this.list=list;
        this.context=activity;
        this.mActivity = activity;
        this.days = new ArrayList<String>(list.keySet());
    }

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     */
    public TimingsAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }


    public class Holder
    {
        TextView schoolDays;
        TextView schoolStatus;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        TimingsAdapter.Holder holder=new TimingsAdapter.Holder();
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.school_hours_item, null);
            holder.schoolDays = (TextView) convertView.findViewById(R.id.school_days);
            holder.schoolStatus = (TextView) convertView.findViewById(R.id.school_status);

            convertView.setTag(holder);
        }
        else {
            holder = (TimingsAdapter.Holder) convertView.getTag();
        }


        HashMap<String,String> timings = list.get(days.get(position));
        if (timings.get("status").equalsIgnoreCase("close")){
            holder.schoolDays.setText(days.get(position).toUpperCase());
            holder.schoolStatus.setText("Closed");
        }else if(timings.get("status").equalsIgnoreCase("open")){
            holder.schoolDays.setText(days.get(position).toUpperCase());
            holder.schoolStatus.setText(timings.get("from")+" To "+timings.get("to"));

        }
        return convertView;


    }
}
