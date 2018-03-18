package com.credolabs.justcredo.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by sanjaykumar on 17/03/18.
 */

public class FilterMap implements Serializable{

    private ArrayList<FilterModel> boards;
    private ArrayList<FilterModel> categories;
    private ArrayList<FilterModel> classes;
    private ArrayList<FilterModel> extracurricular;
    private ArrayList<FilterModel> facilities;
    private ArrayList<FilterModel> gender;
    private ArrayList<FilterModel> specialFacilities;
    private ArrayList<FilterModel> classesType;
    private ArrayList<FilterModel> singing;
    private ArrayList<FilterModel> dancing;
    private ArrayList<FilterModel> instruments;
    private ArrayList<FilterModel> other_genres;


    public static final String SCHOOL_FILTER = "schoolFilters";
    public static final String ART_FILTER = "artFilters";
    public static final String COACHING_FILTER = "coachingFilters";
    public static final String MUSIC_FILTER = "musicFilters";
    public static final String SPORTS_FILTER = "sportsFilters";
    public static final String TUTOR_FILTER = "tutorFilters";
    public static final String FILTER_DB = "filter";


    public FilterMap() {
    }

    public ArrayList<FilterModel> getBoards() {
        return boards;
    }

    public void setBoards(ArrayList<FilterModel> boards) {
        this.boards = boards;
    }

    public ArrayList<FilterModel> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<FilterModel> categories) {
        this.categories = categories;
    }

    public ArrayList<FilterModel> getClasses() {
        return classes;
    }

    public void setClasses(ArrayList<FilterModel> classes) {
        this.classes = classes;
    }

    public ArrayList<FilterModel> getExtracurricular() {
        return extracurricular;
    }

    public void setExtracurricular(ArrayList<FilterModel> extracurricular) {
        this.extracurricular = extracurricular;
    }

    public ArrayList<FilterModel> getFacilities() {
        return facilities;
    }

    public void setFacilities(ArrayList<FilterModel> facilities) {
        this.facilities = facilities;
    }

    public ArrayList<FilterModel> getGender() {
        return gender;
    }

    public void setGender(ArrayList<FilterModel> gender) {
        this.gender = gender;
    }

    public ArrayList<FilterModel> getSpecialFacilities() {
        return specialFacilities;
    }

    public void setSpecialFacilities(ArrayList<FilterModel> specialFacilities) {
        this.specialFacilities = specialFacilities;
    }

    public ArrayList<FilterModel> getClassesType() {
        return classesType;
    }

    public void setClassesType(ArrayList<FilterModel> classesType) {
        this.classesType = classesType;
    }

    public ArrayList<FilterModel> getSinging() {
        return singing;
    }

    public void setSinging(ArrayList<FilterModel> singing) {
        this.singing = singing;
    }

    public ArrayList<FilterModel> getDancing() {
        return dancing;
    }

    public void setDancing(ArrayList<FilterModel> dancing) {
        this.dancing = dancing;
    }

    public ArrayList<FilterModel> getInstruments() {
        return instruments;
    }

    public void setInstruments(ArrayList<FilterModel> instruments) {
        this.instruments = instruments;
    }

    public ArrayList<FilterModel> getOther_genres() {
        return other_genres;
    }

    public void setOther_genres(ArrayList<FilterModel> other_genres) {
        this.other_genres = other_genres;
    }
}
