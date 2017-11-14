package com.credolabs.justcredo.notifications;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.SearchAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class NotificationsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public NotificationsFragment() {
    }

    public static NotificationsFragment newInstance(String param1, String param2) {
        NotificationsFragment fragment = new NotificationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
        DatabaseReference userNotification = FirebaseDatabase.getInstance().getReference().child(UserNotification.USER_NOTIFICATION_DATABASE);
        userNotification.keepSynced(true);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        final ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.progress);
        final LinearLayout not_found = (LinearLayout) view.findViewById(R.id.not_found);
        TextView not_found_text1 = (TextView) view.findViewById(R.id.not_found_text1);
        TextView not_found_text2 = (TextView) view.findViewById(R.id.not_found_text2);
        not_found_text1.setText("No notification for you.");
        not_found_text2.setVisibility(View.GONE);
        final ArrayList<UserNotification> list = new ArrayList<>();
        final RecyclerView items = (RecyclerView) view.findViewById(R.id.notification_items);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(items.getContext(),mLayoutManager.getOrientation());
        items.addItemDecoration(mDividerItemDecoration);
        items.setHasFixedSize(true);
        items.setLayoutManager(mLayoutManager);
        final NotificationAdapter adapter = new NotificationAdapter(getActivity(), list);
        items.setAdapter(adapter);
        progressBar.setVisibility(View.VISIBLE);
        items.setVisibility(View.GONE);
        not_found.setVisibility(View.GONE);

        userNotification.orderByChild(UserNotification.TO_UID).equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    UserNotification notification = noteDataSnapshot.getValue(UserNotification.class);
                    list.add(notification);
                }

                Collections.sort(list, new Comparator<UserNotification>(){
                    public int compare(UserNotification o1, UserNotification o2){
                        return o2.getTimestamp().compareTo(o1.getTimestamp());
                    }
                });
                if (list.size() > 0 ) {
                    items.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }else{
                    progressBar.setVisibility(View.GONE);
                    not_found.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                not_found.setVisibility(View.VISIBLE);
            }
        });
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }
}
