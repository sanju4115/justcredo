package com.credolabs.justcredo.school;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.credolabs.justcredo.HorizontalListViewFragment;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.ReadReviewActivity;
import com.credolabs.justcredo.ReviewActivity;
import com.credolabs.justcredo.adapters.TextViewAdapter;
import com.credolabs.justcredo.enums.PageTypes;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.DbConstants;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.model.ZoomObject;
import com.credolabs.justcredo.newplace.BoardsFragment;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.newplace.TypePlaceFragment;
import com.credolabs.justcredo.sliderlayout.ImageSlideAdapter;
import com.credolabs.justcredo.utility.Constants;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class SchoolHomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String TAG = "SchoolHomeFragment";
    private School model;
    private EditText editTextName, editTextEmail, editTextWebsite, descriptionText, addressLine1,
            addressLine2, addressCity, addressState, addressCountry, mobileNumber;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private LinearLayout school_edit_home;
    private LatLng latLng;
    private Button save_school_home;
    private ProgressDialog mProgressDialog;
    private LinearLayout bookmark;
    private ImageView bookmarkImage;
    private FirebaseUser user;
    private LinearLayout fragment_container;
    private Button edit_school_home,cancel_school_home;
    private Query query;
    private Review review1,review2,review3;
    private User user1,user2,user3;

    public SchoolHomeFragment() {
    }

    public static SchoolHomeFragment newInstance(School param1) {
        SchoolHomeFragment fragment = new SchoolHomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            model = (School) getArguments().getSerializable(ARG_PARAM1);
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        Fragment top_rated = HorizontalListViewFragment.newInstance(PageTypes.DETAIL_PAGE.getValue(),model.getType(),model);
        transaction.add(R.id.fragment_container, top_rated );
        transaction.commit();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_school_home, container, false);
        if (getActivity()!=null)ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));

        final ViewPager mPager = view.findViewById(R.id.view_pager);
        if (model.getImages()!=null) {
            final ArrayList<String> list = new ArrayList<>(model.getImages().values());
            String address = Util.getAddress(model.getAddress());
            ZoomObject zoomObject = new ZoomObject();
            zoomObject.setImages(list);
            zoomObject.setName(model.getName());
            zoomObject.setAddress(address);
            zoomObject.setLogo(list.get(0));
            mPager.setAdapter(new ImageSlideAdapter(getActivity(), list, zoomObject));
            final int[] currentPage = {0};
            final Handler handler = new Handler();
            final Runnable Update = () -> {
                if (currentPage[0] == list.size()) {
                    currentPage[0] = 0;
                }
                mPager.setCurrentItem(currentPage[0]++, true);
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
        fragment_container = view.findViewById(R.id.fragment_container);
        school_edit_home = view.findViewById(R.id.school_edit_home);
        LinearLayout edit_school_home_section = view.findViewById(R.id.edit_school_home_section);
        cancel_school_home = view.findViewById(R.id.cancel_school_home);
        edit_school_home = view.findViewById(R.id.edit_school_home);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        editTextName    = view.findViewById(R.id.EditTextName);
        editTextEmail   = view.findViewById(R.id.EditTextEmail);
        editTextWebsite = view.findViewById(R.id.EditTextWebsite);
        descriptionText = view.findViewById(R.id.description_text);
        addressLine1    = view.findViewById(R.id.addressLine1);
        addressLine2    = view.findViewById(R.id.addressLine2);
        addressCity     = view.findViewById(R.id.addressCity);
        addressState    = view.findViewById(R.id.addressState);
        addressCountry  = view.findViewById(R.id.addressCountry);
        mobileNumber    = view.findViewById(R.id.mobileNumber);
        save_school_home = view.findViewById(R.id.save_school_home);
        bookmark = view.findViewById(R.id.bookmark);
        bookmarkImage = view.findViewById(R.id.bookmarkImage);

        setBookmark();

        if (Util.checkSchoolAdmin(model)){
            edit_school_home_section.setVisibility(View.VISIBLE);
            edit_school_home.setOnClickListener(v -> {
                fragment_container.setVisibility(View.GONE);
                school_edit_home.setVisibility(View.VISIBLE);
                edit_school_home.setVisibility(View.GONE);
                cancel_school_home.setVisibility(View.VISIBLE);
                buildEditSection(view);


            });

            cancel_school_home.setOnClickListener(v -> {
                fragment_container.setVisibility(View.VISIBLE);
                school_edit_home.setVisibility(View.GONE);
                edit_school_home.setVisibility(View.VISIBLE);
                cancel_school_home.setVisibility(View.GONE);
            });
        }
        final LinearLayout layoutReviews = view.findViewById(R.id.layout_reviews);
        layoutReviews.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(),ReadReviewActivity.class);
            intent.putExtra(School.ID,model.getId());
            intent.putExtra(School.NAME,model.getName());
            startActivity(intent);
        });

        TextView reviewBtn = view.findViewById(R.id.btn_review);
        reviewBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(),ReviewActivity.class);
            intent.putExtra(School.NAME,model.getName());
            intent.putExtra(School.TYPE,School.SCHOOL_DATABASE);
            intent.putExtra(School.ID,model.getId());
            intent.putExtra(School.ADDRESS_CITY,model.getAddress().get(School.ADDRESS_CITY));
            intent.putExtra(School.ADDRESS_STATE,model.getAddress().get(School.ADDRESS_STATE));
            startActivity(intent);

        });

        TextView schoolName = view.findViewById(R.id.school_name); // for school header details
        if (model.getStatus()!=null && model.getStatus().equals(School.StatusType.VERIFIED.getValue())){
            schoolName.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_verified,0);
        }
        schoolName.setText(model.getName());
        TextView schoolAddress = view.findViewById(R.id.school_address);
        schoolAddress.setText(Util.getAddress(model.getAddress()));
        TextView schoolReviewNo = view.findViewById(R.id.school_review_no);
        if (model.getNoOfReview()!=null){
            schoolReviewNo.setText(String.valueOf(model.getNoOfReview()));
        }

        TextView school_bookmark_no = view.findViewById(R.id.school_bookmark_no);
        if (model.getNoOfBookmarks()!=null){
            school_bookmark_no.setText(String.valueOf(model.getNoOfBookmarks()));
        }

        TextView distance = view.findViewById(R.id.distance);
        if (model.getLatitude()!=0 && model.getLongitude()!=0){
            distance.setText(getResources().getString(R.string.km,NearByPlaces.distance(getActivity(),model.getLatitude(),model.getLongitude())));
        }

        TextView schoolDescription = view.findViewById(R.id.description); // for school description
        schoolDescription.setText(model.getDescription());
        LinearLayout schoolCategory = view.findViewById(R.id.category_layout);
        LinearLayout board_layout = view.findViewById(R.id.board_layout);
        LinearLayout schoolGender = view.findViewById(R.id.gender);
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


        TextView schoolRating = view.findViewById(R.id.rating);   // rating and review section
        TextView noOfRatings = view.findViewById(R.id.noOfRatings);
        LinearLayoutCompat layoutRating = view.findViewById(R.id.layout_rating);
        if (model.getNoOfRating() ==null || model.getNoOfRating()==0){
            layoutRating.setVisibility(View.GONE);
        }else {
            schoolRating.setText(String.valueOf(model.getRating()));
            noOfRatings.setText(String.valueOf(model.getNoOfRating()));
        }
        final ImageView profile1 = view.findViewById(R.id.profile1);
        final ImageView profile2 = view.findViewById(R.id.profile2);
        final ImageView profile3 = view.findViewById(R.id.profile3);
        final LinearLayout layoutReviewsNo = view.findViewById(R.id.layout_reviews_no);

        final TextView reviewerName1 = view.findViewById(R.id.reviewer_name1);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Review.DB_REVIEWS_REF)
                .whereEqualTo(Review.SCHOOL_ID,model.getId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult()!=null){
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    if (documents.size()>0){
                        layoutReviews.setVisibility(View.VISIBLE);
                        layoutReviewsNo.setVisibility(View.GONE);
                    }
                    switch (documents.size()){
                        case 1:
                            review1 = documents.get(0).toObject(Review.class);
                            query = db.collection(User.DB_REF).whereEqualTo(User.UID,review1.getUserID());
                            break;
                        case 2:
                            review1 = documents.get(0).toObject(Review.class);
                            review2 = documents.get(1).toObject(Review.class);
                            query = db.collection(User.DB_REF).whereEqualTo(User.UID,review1.getUserID())
                                                              .whereEqualTo(User.UID,review2.getUserID());
                            break;
                        default:
                            review1 = documents.get(0).toObject(Review.class);
                            review2 = documents.get(1).toObject(Review.class);
                            review3 = documents.get(2).toObject(Review.class);
                            query = db.collection(User.DB_REF).whereEqualTo(User.UID,review1.getUserID())
                                                              .whereEqualTo(User.UID,review2.getUserID())
                                                              .whereEqualTo(User.UID,review3.getUserID());
                            break;
                    }
                    query.get().addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful() && userTask.getResult() != null) {
                            List<DocumentSnapshot> userDocument = userTask.getResult().getDocuments();
                            for (int i=0;i<userDocument.size();i++) {
                                switch (i){
                                    case 0:
                                        user1 = userDocument.get(i).toObject(User.class);
                                        if (user1 != null) {
                                            profile1.setVisibility(View.VISIBLE);
                                            Util.loadCircularImageWithGlide(getActivity(),user1.getProfilePic(),profile1);
                                            reviewerName1.setText(String.format(getString(R.string.review_no_msg_for_1), user1.getName()));
                                        }
                                        break;
                                    case 1:
                                        user2 = userDocument.get(i).toObject(User.class);
                                        if (user2 != null) {
                                            if (user1.getUid().equals(user2.getUid())){
                                                reviewerName1.setText(String.format(getString(R.string.review_no_msg_for_1), user1.getName()));
                                            }else {
                                                reviewerName1.setText(String.format(getString(R.string.review_no_msg_for_2), user1.getName()));
                                                profile2.setVisibility(View.VISIBLE);
                                                Util.loadCircularImageWithGlide(getActivity(),user2.getProfilePic(),profile2);
                                            }
                                        }
                                        break;
                                    case 2:
                                        user3 = userDocument.get(i).toObject(User.class);
                                        if (user3 != null) {
                                            if (user3.getUid().equals(user1.getUid()) || user3.getUid().equals(user2.getUid())){
                                                reviewerName1.setText(String.format(getString(R.string.review_no_msg_for_2), user1.getName()));
                                            }else {
                                                profile3.setVisibility(View.VISIBLE);
                                                Util.loadCircularImageWithGlide(getActivity(),user3.getProfilePic(),profile3);
                                                reviewerName1.setText(String.format(getString(R.string.review_no_msg_for_greater), user1.getName(), model.getNoOfReview() -1 ));
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    });
                }
        });

        LinearLayout layout_classes = view.findViewById(R.id.layout_classes);
        ExpandableHeightGridView expandableHeightGridView = view.findViewById(R.id.checked);
        if (model.getClasses()!=null){
            ArrayList<String> classList = new ArrayList<>(model.getClasses().values());
            Collections.sort(classList);
            expandableHeightGridView.setAdapter(new TextViewAdapter(getActivity(),classList,""));
        }else{
            layout_classes.setVisibility(View.GONE);
        }


        ImageView btn_edit_classes = view.findViewById(R.id.btn_edit_classes); // Section for editing place by admin
        ImageView btn_edit_type = view.findViewById(R.id.btn_edit_type);
        if (Util.checkSchoolAdmin(model)) {
            final BoardsFragment boardsFragment = BoardsFragment.newInstance(PlaceTypes.Action.EDIT_BACKUP.getValue(), model);
            final LinearLayout edit_boards_section = view.findViewById(R.id.edit_boards_section);
            final LinearLayout edit_type_section = view.findViewById(R.id.edit_type_section);
            final LinearLayout content = view.findViewById(R.id.content);
            Button save_boards = view.findViewById(R.id.save_boards);
            Button cancel_boards = view.findViewById(R.id.cancel_boards);
            final LinearLayout fragContainer = view.findViewById(R.id.container);
            fragContainer.setVisibility(View.GONE);
            final ProgressBar progress = view.findViewById(R.id.progress);
            btn_edit_classes.setOnClickListener(v -> {
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
            });

            cancel_boards.setOnClickListener(v -> {
                edit_boards_section.setVisibility(View.GONE);
                fragContainer.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);
            });

            save_boards.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setMessage(R.string.update_confirmation_msg);
                alertDialogBuilder.setPositiveButton(R.string.yes,
                        (arg0, arg1) -> {
                            progress.setVisibility(View.VISIBLE);
                            HashMap<String, HashMap<String, String>> map = boardsFragment.getFragmentState();
                            WriteBatch batch = db.batch();
                            DocumentReference documentReference = db.collection(School.SCHOOL_DATABASE).document(model.getId());
                            batch.update(documentReference, School.BOARDS, map.get(School.BOARDS));
                            batch.update(documentReference, School.CLASSES, map.get(School.CLASSES));
                            batch.commit().addOnCompleteListener(task -> {
                                fragContainer.setVisibility(View.GONE);
                                content.setVisibility(View.VISIBLE);
                                edit_boards_section.setVisibility(View.GONE);
                                progress.setVisibility(View.GONE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        getActivity());
                                builder.setCancelable(true);
                                builder.setMessage(R.string.place_updated_success_msg);
                                builder.setPositiveButton(R.string.ok,
                                        (dialog, which) -> dialog.dismiss());
                                AlertDialog alert = builder.create();
                                alert.show();
                            });
                        });
                alertDialogBuilder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
                alertDialogBuilder.create();
                alertDialogBuilder.show();
            });

            final TypePlaceFragment typePlaceFragment = TypePlaceFragment.newInstance(PlaceTypes.Action.EDIT_BACKUP.getValue(), model);
            Button save_type = view.findViewById(R.id.save_type);
            Button cancel_type = view.findViewById(R.id.cancel_type);
            btn_edit_type.setOnClickListener(v -> {
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
            });

            cancel_type.setOnClickListener(v -> {
                edit_type_section.setVisibility(View.GONE);
                fragContainer.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);
            });

            save_type.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setMessage(R.string.update_confirmation_msg);
                alertDialogBuilder.setPositiveButton(R.string.yes,
                        (arg0, arg1) -> {
                            progress.setVisibility(View.VISIBLE);
                            HashMap<String, HashMap<String, String>> map = typePlaceFragment.getFragmentState();
                            WriteBatch batch = db.batch();
                            DocumentReference documentReference = db.collection(School.SCHOOL_DATABASE).document(model.getId());
                            batch.update(documentReference, School.CATEGORIES, map.get(School.CATEGORIES));
                            batch.update(documentReference, School.GENDER, map.get(School.GENDER));
                            batch.commit().addOnCompleteListener(task -> {
                                fragContainer.setVisibility(View.GONE);
                                content.setVisibility(View.VISIBLE);
                                edit_type_section.setVisibility(View.GONE);
                                progress.setVisibility(View.GONE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        getActivity());
                                builder.setCancelable(true);
                                builder.setMessage(R.string.place_updated_success_msg);
                                builder.setPositiveButton(R.string.ok,
                                        (dialog, which) -> dialog.dismiss());
                                AlertDialog alert = builder.create();
                                alert.show();
                            });
                        });
                alertDialogBuilder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
                alertDialogBuilder.create();
                alertDialogBuilder.show();
            });
        }else {
            btn_edit_classes.setVisibility(View.GONE);
            btn_edit_type.setVisibility(View.GONE);
        }
        return view;

    }

    private void setBookmark() {
        FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_BOOKMARK).document(user.getUid())
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()!=null && task.getResult().contains(model.getId())){
                bookmarkImage.setImageResource(R.drawable.ic_bookmark_green_24dp);
            }else {
                bookmarkImage.setImageResource(R.drawable.ic_bookmark_secondary);
            }
        });
        bookmark.setOnClickListener(v -> School.onBookmark(model,user,getActivity(),bookmarkImage));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (imageReturnedIntent != null) {
            switch (requestCode) {
                case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                    if (resultCode == RESULT_OK) {
                        if (getActivity()!=null) {
                            Place place = PlaceAutocomplete.getPlace(getActivity(), imageReturnedIntent);
                            String address = String.valueOf(place.getAddress());
                            latLng = place.getLatLng();
                            String[] str = address.split(",");
                            int j = 1;

                            addressLine1.setText("");
                            addressLine2.setText("");
                            addressCity.setText("");
                            addressState.setText("");
                            addressCountry.setText("");

                            for (int i = str.length - 1; i >= 0; i--) {
                                if (j == 1) {
                                    addressCountry.setText(str[i]);
                                } else if (j == 2) {
                                    addressState.setText(str[i]);
                                } else if (j == 3) {
                                    addressCity.setText(str[i]);
                                } else if (j == 4) {
                                    addressLine2.setText(str[i]);
                                } else if (j == 5) {
                                    addressLine1.setText(str[i]);
                                } else {
                                    addressLine1.setText(addressLine1.getText().toString().concat(" " + str[i]));
                                }
                                j++;
                            }
                        }
                    } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                        if (getActivity()!=null) {
                            Status status = PlaceAutocomplete.getStatus(getActivity(), imageReturnedIntent);
                            Log.i(TAG, status.getStatusMessage());
                        }
                    } else if (resultCode == RESULT_CANCELED) {
                        Log.i(TAG, "user cancelled the operation");
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
        addressLine1.setText(model.getAddress().get(School.ADDRESSLINE1));
        addressLine2.setText(model.getAddress().get(School.ADDRESSLINE2));
        addressCity.setText(model.getAddress().get(School.ADDRESS_CITY));
        addressState.setText(model.getAddress().get(School.ADDRESS_STATE));
        addressCountry.setText(model.getAddress().get(School.ADDRESS_COUNTRY));
        mobileNumber.setText(model.getMobileNumber());


        EditText adressText = view.findViewById(R.id.adressText);
        final Activity activity = getActivity();
        adressText.setOnClickListener(v -> {
            try {
                AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                        .setCountry(Constants.COUNTRY_INDIA)
                        .setTypeFilter(AutocompleteFilter.TYPE_FILTER_GEOCODE)
                        .build();
                if (activity != null) {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .setFilter(typeFilter)
                            .build(activity);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);

                }
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                Crashlytics.log(Log.INFO,TAG, e.getMessage());
            }
        });

        mProgressDialog = new ProgressDialog(getActivity());
        save_school_home.setOnClickListener(v -> {
            boolean validationPassed = validate();
            if (validationPassed) {
                startPosting();
            }
        });
    }

    private boolean validate() {
        String name = editTextName.getText().toString().trim();
        if (TextUtils.isEmpty(name)){
            editTextName.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    getString(R.string.provide_placce_name));
            return false;
        }

        String description = descriptionText.getText().toString().trim();
        if (TextUtils.isEmpty(description)){
            descriptionText.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    getString(R.string.provide_place_desc));
            return false;
        }

        if (description.length()< 100){
            descriptionText.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    getString(R.string.desc_100_msg));
            return false;
        }

        String city = addressCity.getText().toString().trim();
        String state = addressState.getText().toString().trim();
        String country = addressCountry.getText().toString().trim();
        String mobile = mobileNumber.getText().toString().trim();
        if (TextUtils.isEmpty(city)){
            addressCity.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    getString(R.string.provide_city));
            return false;
        }
        if (TextUtils.isEmpty(state)){
            addressState.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    getString(R.string.provide_state));
            return false;
        }
        if (TextUtils.isEmpty(country)){
            addressCountry.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    getString(R.string.provide_country));
            return false;
        }
        if (TextUtils.isEmpty(mobile)){
            mobileNumber.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    getString(R.string.provide_mobile));
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


        mProgressDialog.setMessage(getString(R.string.submitting_your_place));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        HashMap<String,Object> map = new HashMap<>();
        if (!TextUtils.isEmpty(name)) {
            map.put(School.NAME,name);
        }

        if (!TextUtils.isEmpty(mail)) {
            map.put(School.MAIL,mail);
        }

        if (!TextUtils.isEmpty(website)) {
            map.put(School.WEBSITE,website);
        }

        if (!TextUtils.isEmpty(description)) {
            map.put(School.DESCRIPTION,description);
        }

        if (!TextUtils.isEmpty(mobile)) {
            map.put(School.MOBILE_NUMBER,mobile);
        }

        HashMap<String,String> address = new HashMap<>();

        if (!TextUtils.isEmpty(line1)) {
            address.put(School.ADDRESSLINE1,line1);
        }
        if (!TextUtils.isEmpty(line2)) {
            address.put(School.ADDRESSLINE2,line2);
        }
        if (!TextUtils.isEmpty(city)) {
            address.put(School.ADDRESS_CITY,city);
        }
        if (!TextUtils.isEmpty(state)) {
            address.put(School.ADDRESS_STATE,state);
        }
        if (!TextUtils.isEmpty(country)) {
            address.put(School.ADDRESS_COUNTRY,country);
        }
        if (str.length == 2) {
            String postalCode = str[1].trim();
            address.put(School.POSTAL_CODE,postalCode);
        }

        map.put(School.ADDRESS,address);

        if (latLng !=null){
            double latitude = latLng.latitude;
            map.put(School.LATITUDE,latitude);
            double longitude = latLng.longitude;
            map.put(School.LONGITUDE,longitude);
        }

        FirebaseFirestore.getInstance().collection(School.SCHOOL_DATABASE).document(model.getId()).update(map)
                .addOnCompleteListener(task -> {
                    mProgressDialog.dismiss();
                    if (getActivity()!=null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(true);
                        if (task.isSuccessful()) {
                            builder.setMessage(R.string.congrats_place_add_msg);

                        } else {
                            builder.setMessage(R.string.went_wrong_try_later);
                        }
                        builder.setPositiveButton(R.string.ok,
                                (dialog, which) -> {
                                    fragment_container.setVisibility(View.VISIBLE);
                                    school_edit_home.setVisibility(View.GONE);
                                    dialog.dismiss();
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });

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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity()!=null)ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
