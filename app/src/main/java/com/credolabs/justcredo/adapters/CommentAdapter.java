package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.holder.CommentHolder;
import com.credolabs.justcredo.model.Comment;

import java.util.ArrayList;

/**
 * Created by sanjaykumar on 24/03/18.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentHolder> {
    private ArrayList<Comment> arrayList;
    private Context context;

    public CommentAdapter(Context context, ArrayList<Comment> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());

        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(
                R.layout.comment_list_entry, viewGroup, false);
        return new CommentHolder(mainGroup,context);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        holder.setCommentLayout(arrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return (null != arrayList ? arrayList.size() : 0);
    }
}
