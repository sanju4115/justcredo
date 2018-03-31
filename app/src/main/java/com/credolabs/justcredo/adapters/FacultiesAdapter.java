package com.credolabs.justcredo.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.credolabs.justcredo.MyApplication;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.utility.CircularNetworkImageView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Sanjay kumar on 4/8/2017.
 */

public class FacultiesAdapter extends ArrayAdapter<String> {
    private ArrayList<LinkedHashMap<String,Object>> list;
    private Context context;
    private Activity mActivity;

    public FacultiesAdapter(Context context, ArrayList<LinkedHashMap<String, Object>> list, Activity activity) {
        super(context, R.layout.school_faculties_item);
        // TODO Auto-generated constructor stub
        this.list=list;
        this.context=activity;
        this.mActivity = activity;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }


    public class Holder
    {
        CircularNetworkImageView facultyImage;
        TextView facultyName;
        TextView facultyQualification;
        TextView facultySubjects;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        FacultiesAdapter.Holder holder=new FacultiesAdapter.Holder();
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageLoader imgLoader = MyApplication.getInstance().getImageLoader();


        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.school_faculties_item, null);
            holder.facultyImage = (CircularNetworkImageView) convertView.findViewById(R.id.faculty_image);
            holder.facultyName = (TextView) convertView.findViewById(R.id.faculty_name);
            holder.facultyQualification = (TextView) convertView.findViewById(R.id.faculty_qualification);
            holder.facultySubjects = (TextView) convertView.findViewById(R.id.faculty_subjects);
            convertView.setTag(holder);
        }
        else {
            holder = (FacultiesAdapter.Holder) convertView.getTag();
        }

        holder.facultyImage.setImageUrl(String.valueOf(list.get(position).get("image")),imgLoader);
        holder.facultyName.setText((CharSequence) list.get(position).get("name"));
        ArrayList<String> qualificationList = (ArrayList<String>) list.get(position).get("qualification");

        String qualification = android.text.TextUtils.join(", ", qualificationList);;
        holder.facultyQualification.setText(qualification);

        ArrayList<String> subjectList = (ArrayList<String>) list.get(position).get("subject");

        String subject = android.text.TextUtils.join(", ", subjectList);;
        holder.facultySubjects.setText(subject);



        return convertView;


    }
}
