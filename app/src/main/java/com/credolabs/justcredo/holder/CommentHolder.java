package com.credolabs.justcredo.holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.Comment;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Created by sanjaykumar on 24/03/18.
 */

public class CommentHolder extends RecyclerView.ViewHolder{
    View mView;
    FirebaseAuth mAuth;
    ImageView comment_user_image;
    TextView comment_user_name,comment_user_text;

    private Context context;


    public CommentHolder(View itemView,Context context) {
        super(itemView);
        this.context=context;
        mView = itemView;
        mAuth = FirebaseAuth.getInstance();
        comment_user_image = mView.findViewById(R.id.comment_user_image);
        comment_user_name = mView.findViewById(R.id.comment_user_name);
        comment_user_text = mView.findViewById(R.id.comment_user_text);

    }

    public void setCommentLayout(final Comment model) {
        if (model.getUid()!=null && model.getComment()!=null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(User.DB_REF).document(model.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()){
                    User user = task.getResult().toObject(User.class);
                    if (user.getProfilePic() != null){
                        Util.loadCircularImageWithGlide(context,user.getProfilePic(),comment_user_image);
                    }
                    if (user.getName() != null)
                        comment_user_name.setText(user.getName());
                    if (model.getComment() != null)
                        comment_user_text.setText(model.getComment());
                }
            });
        }
    }
}
