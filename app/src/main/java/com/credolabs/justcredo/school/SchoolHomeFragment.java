package com.credolabs.justcredo.school;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.credolabs.justcredo.HorizontalListViewFragment;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.ReadReviewActivity;
import com.credolabs.justcredo.ReviewActivity;
import com.credolabs.justcredo.adapters.TextViewAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.model.ZoomObject;
import com.credolabs.justcredo.newplace.BoardsFragment;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.newplace.TypePlaceFragment;
import com.credolabs.justcredo.sliderlayout.ImageSlideAdapter;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.ExpandableHeightGridView;
import com.credolabs.justcredo.utility.NearByPlaces;
import com.credolabs.justcredo.utility.Util;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class SchoolHomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private School model;
    private String mParam2;

    private EditText editTextName, editTextEmail, editTextWebsite, descriptionText, addressLine1, addressLine2, addressCity, addressState, addressCountry, mobileNumber;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private LinearLayout school_edit_home;


    private OnFragmentInteractionListener mListener;
    private LatLng latLng;
    private Button save_school_home;
    private ProgressDialog mProgressDialog;
    private LinearLayout bookmark;
    private ImageView bookmarkImage;
    private FirebaseUser user;
    private LinearLayout fragment_container;
    private Button edit_school_home,cancel_school_home;

    public SchoolHomeFragment() {
    }

    public static SchoolHomeFragment newInstance(School param1, String param2) {
        SchoolHomeFragment fragment = new SchoolHomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            model = (School) getArguments().getSerializable(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        Fragment top_rated = HorizontalListViewFragment.newInstance(PlaceTypes.PageTypes.DETAIL_PAGE.getValue(),model.getType(),model);
        transaction.add(R.id.fragment_container, top_rated );
        transaction.commit();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_school_home, container, false);
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));

        final ViewPager mPager = (ViewPager) view.findViewById(R.id.view_pager);
        if (model.getImages()!=null) {
            final ArrayList<String> list = new ArrayList<String>(model.getImages().values());
            String address = Util.getAddress(model.getAddress());
            ZoomObject zoomObject = new ZoomObject();
            zoomObject.setImages(list);
            zoomObject.setName(model.getName());
            zoomObject.setAddress(address);
            zoomObject.setLogo(list.get(0));
            mPager.setAdapter(new ImageSlideAdapter(getActivity(), list, zoomObject));
            final int[] currentPage = {0};
            // Auto start of viewpager
            final Handler handler = new Handler();
            final Runnable Update = new Runnable() {
                public void run() {
                    if (currentPage[0] == list.size()) {
                        currentPage[0] = 0;
                    }
                    mPager.setCurrentItem(currentPage[0]++, true);
                }
            };
            Timer swipeTimer = new Timer();
            swipeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(Update);
                }
            }, 2500, 2500);
        }else {
            mPager.setVisibility(View.GONE);
        }
        //TextView edit_button = (TextView) view.findViewById(R.id.edit_button);
        fragment_container = (LinearLayout) view.findViewById(R.id.fragment_container);
        school_edit_home = (LinearLayout) view.findViewById(R.id.school_edit_home);
        LinearLayout edit_school_home_section = (LinearLayout) view.findViewById(R.id.edit_school_home_section);
        cancel_school_home = (Button) view.findViewById(R.id.cancel_school_home);
        edit_school_home = (Button) view.findViewById(R.id.edit_school_home);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        editTextName    = (EditText) view.findViewById(R.id.EditTextName);
        editTextEmail   = (EditText) view.findViewById(R.id.EditTextEmail);
        editTextWebsite = (EditText) view.findViewById(R.id.EditTextWebsite);
        descriptionText = (EditText) view.findViewById(R.id.description_text);
        LinearLayout addressLayout = (LinearLayout) view.findViewById(R.id.addressLayout);
        addressLine1    = (EditText) view.findViewById(R.id.addressLine1);
        addressLine2    = (EditText) view.findViewById(R.id.addressLine2);
        addressCity     = (EditText) view.findViewById(R.id.addressCity);
        addressState    = (EditText) view.findViewById(R.id.addressState);
        addressCountry  = (EditText) view.findViewById(R.id.addressCountry);
        mobileNumber    = (EditText) view.findViewById(R.id.mobileNumber);
        save_school_home = (Button) view.findViewById(R.id.save_school_home);
        bookmark = (LinearLayout) view.findViewById(R.id.bookmark);
        bookmarkImage = (ImageView) view.findViewById(R.id.bookmarkImage);

        setBookmark();

        if (Util.checkSchoolAdmin(model)){
            edit_school_home_section.setVisibility(View.VISIBLE);
            edit_school_home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment_container.setVisibility(View.GONE);
                    school_edit_home.setVisibility(View.VISIBLE);
                    edit_school_home.setVisibility(View.GONE);
                    cancel_school_home.setVisibility(View.VISIBLE);
                    buildEditSection(view);


                }
            });

            cancel_school_home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment_container.setVisibility(View.VISIBLE);
                    school_edit_home.setVisibility(View.GONE);
                    edit_school_home.setVisibility(View.VISIBLE);
                    cancel_school_home.setVisibility(View.GONE);
                }
            });
        }
        final LinearLayout layoutReviews = (LinearLayout) view.findViewById(R.id.layout_reviews);
        layoutReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),ReadReviewActivity.class);
                intent.putExtra("key",model.getId());
                intent.putExtra("type","schools");
                startActivity(intent);
            }
        });

        TextView reviewBtn = (TextView) view.findViewById(R.id.btn_review);
        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),ReviewActivity.class);
                intent.putExtra("name",model.getName());
                intent.putExtra("type","schools");
                intent.putExtra("id",model.getId());
                intent.putExtra("addressCity",model.getAddress().get("addressCity"));
                intent.putExtra("addressState",model.getAddress().get("addressState"));
                startActivity(intent);

            }
        });

        // for school header details
        TextView schoolName = (TextView) view.findViewById(R.id.school_name);
        if (model.getStatus()!=null && model.getStatus().equals(School.StatusType.VERIFIED.getValue())){
            schoolName.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_verified,0);
        }
        schoolName.setText(model.getName());
        TextView schoolAddress = (TextView) view.findViewById(R.id.school_address);
        schoolAddress.setText(Util.getAddress(model.getAddress()));
        TextView schoolReviewNo = (TextView) view.findViewById(R.id.school_review_no);
        if (model.getNoOfReview()!=null){
            schoolReviewNo.setText(String.valueOf(model.getNoOfReview()));
        }

        TextView school_bookmark_no = (TextView) view.findViewById(R.id.school_bookmark_no);
        if (model.getNoOfBookmarks()!=null){
            school_bookmark_no.setText(String.valueOf(model.getNoOfBookmarks()));
        }

        TextView distance = (TextView) view.findViewById(R.id.distance);
        if (model.getLatitude()!=0 && model.getLongitude()!=0){
            distance.setText(getResources().getString(R.string.km,NearByPlaces.distance(getActivity(),model.getLatitude(),model.getLongitude())));
        }

        // for school description
        TextView schoolDescription = (TextView) view.findViewById(R.id.description);
        schoolDescription.setText(model.getDescription());
        LinearLayout schoolCategory = (LinearLayout) view.findViewById(R.id.category_layout);
        LinearLayout board_layout = (LinearLayout) view.findViewById(R.id.board_layout);
        LinearLayout schoolGender = (LinearLayout) view.findViewById(R.id.gender);
        if (model.getCategories()!=null){
            for (String value : model.getCategories().values()) {
                TextView htext =new TextView(getActivity());
                htext.setText(value);
                htext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star,0,0,0);
                htext.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT , LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                schoolCategory.addView(htext);
            }
        }else {
            schoolCategory.setVisibility(View.GONE);
        }

        if (model.getBoards()!=null){
            for (String value : model.getBoards().values()) {
                final TextView htext =new TextView(getActivity());
                htext.setText(value);
                htext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked_vector,0,0,0);
                htext.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT , LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                board_layout.addView(htext);
            }
        }else {
            board_layout.setVisibility(View.GONE);
        }


        if (model.getGender()!=null){
            for (String value : model.getGender().values()) {
                TextView htext =new TextView(getActivity());
                htext.setText(value);
                htext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star,0,0,0);
                htext.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT , LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                schoolGender.addView(htext);
            }
        }else {
            schoolGender.setVisibility(View.GONE);
        }


        // rating and review section
        TextView schoolRating = (TextView) view.findViewById(R.id.rating);
        TextView noOfRatings = (TextView) view.findViewById(R.id.noOfRatings);
        LinearLayoutCompat layoutRating = (LinearLayoutCompat) view.findViewById(R.id.layout_rating);
        if (model.getNoOfRating() ==null || model.getNoOfRating()==0){
            layoutRating.setVisibility(View.GONE);
        }else {
            schoolRating.setText(String.valueOf(model.getRating()));
            noOfRatings.setText(String.valueOf(model.getNoOfRating()));
        }
        final ImageView profile1 = (ImageView) view.findViewById(R.id.profile1);
        final ImageView profile2 = (ImageView) view.findViewById(R.id.profile2);
        final ImageView profile3 = (ImageView) view.findViewById(R.id.profile3);
        String nameFirstUser = " ";
        //final LinearLayout layoutReviews = (LinearLayout) view.findViewById(R.id.layout_reviews);
        final LinearLayout layoutReviewsNo = (LinearLayout) view.findViewById(R.id.layout_reviews_no);

        TextView noOfReviewsMinusOne = (TextView) view.findViewById(R.id.noOfReviewsMinusOne);
        if (model.getNoOfReview()!=null && model.getNoOfReview()!=0){
            noOfReviewsMinusOne.setText(String.valueOf(model.getNoOfReview()-1));
            TextView allReview = (TextView) view.findViewById(R.id.all_review);
            allReview.setText("Read All Reviews ("+model.getNoOfReview()+")");
        }

        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("reviews");
        final TextView reviewerName1 = (TextView) view.findViewById(R.id.reviewer_name1);

        final ArrayList<Review> reviews= new ArrayList<>() ;
        mDatabaseReference.orderByChild("schoolID").equalTo(model.getId()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Review review = dataSnapshot.getValue(Review.class);
                reviews.add(review);
                switch (reviews.size()){
                    case 1:
                        layoutReviews.setVisibility(View.VISIBLE);
                        layoutReviewsNo.setVisibility(View.GONE);
                        profile1.setVisibility(View.VISIBLE);
                        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("users");
                        if (review != null) {
                            userReference.orderByKey().equalTo(review.getUserID()).addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    User user = dataSnapshot.getValue(User.class);
                                    //profile1.setImageUrl(user.getProfilePic(),imgLoader);
                                    if (user != null) {
                                        Util.loadCircularImageWithGlide(getActivity(),user.getProfilePic(),profile1);
                                    }
                                    reviewerName1.setText(user.getName());
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        break;
                    case 2:
                        profile2.setVisibility(View.VISIBLE);
                        DatabaseReference userReference2 = FirebaseDatabase.getInstance().getReference().child("users");
                        if (review != null) {
                            userReference2.orderByKey().equalTo(review.getUserID()).addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    User user = dataSnapshot.getValue(User.class);
                                    //profile2.setImageUrl(user.getProfilePic(),imgLoader);
                                    if (user != null) {
                                        Util.loadCircularImageWithGlide(getActivity(),user.getProfilePic(),profile2);
                                    }

                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        break;
                    case 3:
                        profile3.setVisibility(View.VISIBLE);
                        DatabaseReference userReference3 = FirebaseDatabase.getInstance().getReference().child("users");
                        if (review != null) {
                            userReference3.orderByKey().equalTo(review.getUserID()).addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    User user = dataSnapshot.getValue(User.class);
                                    //profile3.setImageUrl(user.getProfilePic(),imgLoader);
                                    if (user != null) {
                                        Util.loadCircularImageWithGlide(getActivity(),user.getProfilePic(),profile3);
                                    }

                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        break;
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        LinearLayout layout_classes = (LinearLayout) view.findViewById(R.id.layout_classes);
        ExpandableHeightGridView expandableHeightGridView = (ExpandableHeightGridView) view.findViewById(R.id.checked);
        if (model.getClasses()!=null){
            ArrayList<String> classList = new ArrayList<>(model.getClasses().values());
            Collections.sort(classList);
            expandableHeightGridView.setAdapter(new TextViewAdapter(getActivity(),classList,""));
        }else{
            layout_classes.setVisibility(View.GONE);
        }


        // Section for editing place by admin
        ImageView btn_edit_classes = (ImageView) view.findViewById(R.id.btn_edit_classes);
        ImageView btn_edit_type = (ImageView) view.findViewById(R.id.btn_edit_type);
        if (Util.checkSchoolAdmin(model)) {
            final BoardsFragment boardsFragment = BoardsFragment.newInstance(PlaceTypes.Action.EDIT_BACKUP.getValue(), model);
            final LinearLayout edit_boards_section = (LinearLayout) view.findViewById(R.id.edit_boards_section);
            final LinearLayout edit_type_section = (LinearLayout) view.findViewById(R.id.edit_type_section);
            final LinearLayout content = (LinearLayout) view.findViewById(R.id.content);
            Button save_boards = (Button) view.findViewById(R.id.save_boards);
            Button cancel_boards = (Button) view.findViewById(R.id.cancel_boards);
            final LinearLayout fragContainer = (LinearLayout) view.findViewById(R.id.container);
            fragContainer.setVisibility(View.GONE);
            final ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress);
            btn_edit_classes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progress.setVisibility(View.VISIBLE);
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, boardsFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    edit_boards_section.setVisibility(View.VISIBLE);
                    edit_type_section.setVisibility(View.GONE);
                    content.setVisibility(View.GONE);
                    fragContainer.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                }
            });

            cancel_boards.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    edit_boards_section.setVisibility(View.GONE);
                    fragContainer.setVisibility(View.GONE);
                    content.setVisibility(View.VISIBLE);
                }
            });

            save_boards.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setMessage("Do you want to update?");
                    alertDialogBuilder.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    progress.setVisibility(View.VISIBLE);
                                    HashMap<String, HashMap<String, String>> map = boardsFragment.getFragmentState();
                                    DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(School.SCHOOL_DATABASE);

                                    mDatabaseReference.child(model.getId()).child(School.BOARDS).setValue(map.get(School.BOARDS));
                                    mDatabaseReference.child(model.getId()).child(School.CLASSES).setValue(map.get(School.CLASSES));
                                    fragContainer.setVisibility(View.GONE);
                                    content.setVisibility(View.VISIBLE);
                                    edit_boards_section.setVisibility(View.GONE);
                                    progress.setVisibility(View.GONE);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(
                                            getActivity());
                                    builder.setCancelable(true);
                                    builder.setMessage("Congrats, place updated successfully ! You can write blog and upload photos so that users" +
                                            " can know about this.");
                                    builder.setPositiveButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            });

                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    alertDialogBuilder.create();
                    alertDialogBuilder.show();
                }
            });

            final TypePlaceFragment typePlaceFragment = TypePlaceFragment.newInstance(PlaceTypes.Action.EDIT_BACKUP.getValue(), model);
            Button save_type = (Button) view.findViewById(R.id.save_type);
            Button cancel_type = (Button) view.findViewById(R.id.cancel_type);
            btn_edit_type.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progress.setVisibility(View.VISIBLE);
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, typePlaceFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    edit_type_section.setVisibility(View.VISIBLE);
                    edit_boards_section.setVisibility(View.GONE);
                    content.setVisibility(View.GONE);
                    fragContainer.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                }
            });

            cancel_type.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    edit_type_section.setVisibility(View.GONE);
                    fragContainer.setVisibility(View.GONE);
                    content.setVisibility(View.VISIBLE);
                }
            });

            save_type.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setMessage("Do you want to update?");
                    alertDialogBuilder.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    progress.setVisibility(View.VISIBLE);
                                    HashMap<String, HashMap<String, String>> map = typePlaceFragment.getFragmentState();
                                    DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(School.SCHOOL_DATABASE);

                                    mDatabaseReference.child(model.getId()).child(School.CATEGORIES).setValue(map.get(School.CATEGORIES));
                                    mDatabaseReference.child(model.getId()).child(School.GENDER).setValue(map.get(School.GENDER));
                                    fragContainer.setVisibility(View.GONE);
                                    content.setVisibility(View.VISIBLE);
                                    edit_type_section.setVisibility(View.GONE);
                                    progress.setVisibility(View.GONE);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(
                                            getActivity());
                                    builder.setCancelable(true);
                                    builder.setMessage("Congrats, place updated successfully ! You can write blog and upload photos so that users" +
                                            " can know about this.");
                                    builder.setPositiveButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            });

                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    alertDialogBuilder.create();
                    alertDialogBuilder.show();
                }
            });
        }else {
            btn_edit_classes.setVisibility(View.GONE);
            btn_edit_type.setVisibility(View.GONE);
        }
        return view;

    }

    private void setBookmark() {

        final DatabaseReference mBookmarkReference = FirebaseDatabase.getInstance().getReference().child("bookmarks");

        mBookmarkReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(model.getId())){
                    bookmarkImage.setImageResource(R.drawable.ic_bookmark_green_24dp);
                }else{
                    bookmarkImage.setImageResource(R.drawable.ic_bookmark_secondary);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                School.onBookmark(model,user,getActivity(),bookmarkImage);

            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (imageReturnedIntent != null) {
            switch (requestCode) {
                case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                    if (resultCode == RESULT_OK) {
                        Place place = PlaceAutocomplete.getPlace(getActivity(), imageReturnedIntent);
                        String address = String.valueOf(place.getAddress());
                        latLng = place.getLatLng();
                        String[] str= address.split(",");
                        int j = 1;

                        addressLine1.setText("");
                        addressLine2.setText("");
                        addressCity.setText("");
                        addressState.setText("");
                        addressCountry.setText("");

                        for(int i=str.length-1; i >=0 ; i--){
                            if(j==1){
                                addressCountry.setText(str[i]);
                            }else if (j==2){
                                addressState.setText(str[i]);
                            }else if (j==3){
                                addressCity.setText(str[i]);
                            }else if (j==4){
                                addressLine2.setText(str[i]);
                            }else if (j==5){
                                addressLine1.setText(str[i]);
                            }else {
                                addressLine1.setText(addressLine1.getText().toString().concat(" "+str[i]));
                            }
                            j++;
                        }

                        //addressLayout.setVisibility(View.VISIBLE);
                    } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                        Status status = PlaceAutocomplete.getStatus(getActivity(), imageReturnedIntent);
                        Log.i("TAG", status.getStatusMessage());
                    } else if (resultCode == RESULT_CANCELED) {
                        // The user canceled the operation.
                    }
                    break;
            }
        }
    }


    private void buildEditSection(View view) {
        editTextName.setText(model.getName());
        editTextEmail.setText(model.getMail());
        editTextWebsite.setText(model.getWebsite());
        descriptionText.setText(model.getDescription());
        addressLine1.setText(model.getAddress().get("addressLine1"));
        addressLine2.setText(model.getAddress().get("addressLine2"));
        addressCity.setText(model.getAddress().get("addressCity"));
        addressState.setText(model.getAddress().get("addressState"));
        addressCountry.setText(model.getAddress().get("addressCountry"));
        mobileNumber.setText(model.getMobileNumber());


        EditText adressText = (EditText) view.findViewById(R.id.adressText);
        final Activity activity = getActivity();
        adressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                            .setCountry("IN")
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_GEOCODE)
                            .build();
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setFilter(typeFilter)
                                    .build(activity);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                } catch (GooglePlayServicesNotAvailableException e) {
                }
            }
        });

        mProgressDialog = new ProgressDialog(getActivity());

        save_school_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validationPassed = validate();
                if (validationPassed) {
                    startPosting();
                }
            }
        });


    }

    private boolean validate() {
        String name = editTextName.getText().toString().trim();
        if (TextUtils.isEmpty(name)){
            editTextName.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    "Please provide a name to this place!");
            return false;
        }

        String description = descriptionText.getText().toString().trim();
        if (TextUtils.isEmpty(description)){
            descriptionText.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    "Please provide description to this place!");
            return false;
        }

        if (description.length()< 100){
            descriptionText.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    "Description should be alteast 100 characters!");
            return false;
        }

        String city = addressCity.getText().toString().trim();
        String state = addressState.getText().toString().trim();
        String country = addressCountry.getText().toString().trim();
        String mobile = mobileNumber.getText().toString().trim();
        if (TextUtils.isEmpty(city)){
            addressCity.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    "Please provide city in the address section!");
            return false;
        }
        if (TextUtils.isEmpty(state)){
            addressState.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    "Please provide state in the address section!");
            return false;
        }
        if (TextUtils.isEmpty(country)){
            addressCountry.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    "Please provide country in the address section!");
            return false;
        }
        if (TextUtils.isEmpty(mobile)){
            mobileNumber.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    "Please provide mobile number in the address section!");
            return false;
        }

        return true;
    }


    private void startPosting() {
        String name = editTextName.getText().toString().trim();
        String mail = editTextEmail.getText().toString().trim();
        String website = editTextWebsite.getText().toString().trim();
        String description = descriptionText.getText().toString().trim();

        String line1 = addressLine1.getText().toString().trim();
        String line2 = addressLine2.getText().toString().trim();
        String city = addressCity.getText().toString().trim();
        String str[] = addressState.getText().toString().trim().split(" ");
        String country = addressCountry.getText().toString().trim();
        String state = str[0].trim();

        String mobile = mobileNumber.getText().toString().trim();


        mProgressDialog.setMessage("Submitting Your Place");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("schools");
        final DatabaseReference newPlace = mDatabaseReference.child(model.getId());
        if (!TextUtils.isEmpty(name)) {
            newPlace.child("name").setValue(name);
        }

        if (!TextUtils.isEmpty(mail)) {
            newPlace.child("mail").setValue(mail);
        }

        if (!TextUtils.isEmpty(website)) {
            newPlace.child("website").setValue(website);
        }

        if (!TextUtils.isEmpty(description)) {
            newPlace.child("description").setValue(description);
        }

        if (!TextUtils.isEmpty(mobile)) {
            newPlace.child("mobileNumber").setValue(mobile);
        }

        DatabaseReference addressRefrence = newPlace.child("address");

        if (!TextUtils.isEmpty(line1)) {
            addressRefrence.child("addressLine1").setValue(line1);
        }
        if (!TextUtils.isEmpty(line2)) {
            addressRefrence.child("addressLine2").setValue(line2);
        }
        if (!TextUtils.isEmpty(city)) {
            addressRefrence.child("addressCity").setValue(city);
        }
        if (!TextUtils.isEmpty(state)) {
            addressRefrence.child("addressState").setValue(state);
        }
        if (!TextUtils.isEmpty(country)) {
            addressRefrence.child("addressCountry").setValue(country);
        }
        if (str.length == 2) {
            String postalCode = str[1].trim();
            addressRefrence.child("postalCode").setValue(postalCode);

        }

        if (latLng !=null){
            double latitude = latLng.latitude;
            newPlace.child("latitude").setValue(latitude);
            double longitude = latLng.longitude;
            newPlace.child("longitude").setValue(longitude);
        }

        mProgressDialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setMessage("Congrats, your place updated successfully !");
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        fragment_container.setVisibility(View.VISIBLE);
                        school_edit_home.setVisibility(View.GONE);
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
    }
    @Override
    public void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
