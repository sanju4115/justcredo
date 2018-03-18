package com.credolabs.justcredo.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by ravindrapatidar on 26/03/17.
 */

public class CategoryModel implements Serializable,Parcelable{

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CategoryModel createFromParcel(Parcel in) {
            return new CategoryModel(in);
        }

        public CategoryModel[] newArray(int size) {
            return new CategoryModel[size];
        }
    };

    private String name;
    private String description;
    private String image;
    private String key;

    public static final String CATEGORYMODEL = "categoryModel";

    public static final String DB_REF = "categories";

    public CategoryModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private CategoryModel(Parcel in){
        this.name = in.readString();
        this.description = in.readString();
        this.image =  in.readString();
        this.key = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.image);
        dest.writeString(this.key);
    }

}
