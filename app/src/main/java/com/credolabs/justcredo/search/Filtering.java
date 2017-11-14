package com.credolabs.justcredo.search;

import android.content.Context;

import com.credolabs.justcredo.model.FilterModel;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.utility.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Created by Sanjay kumar on 10/6/2017.
 */

public class Filtering {

    public static ArrayList<ObjectModel> filterByName(ArrayList<ObjectModel> list, String query){
        ArrayList<ObjectModel> finalList = new ArrayList<>();
        for (ObjectModel model : list){
            Boolean passed = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE).matcher(model.getName().trim()).find();
            if (passed){
                finalList.add(model);
            }
        }
        return finalList;
    }

    public static void sortByRating(ArrayList<School> list){
        Collections.sort(list, new Comparator<School>(){
            public int compare(School o1, School o2){
                return o2.getRating().compareTo(o1.getRating());
            }
        });
    }

    public static void sortByDistance(ArrayList<School> list){
        Collections.sort(list, new Comparator<School>(){
            public int compare(School o1, School o2){
                return Integer.valueOf(o1.getDistance()).compareTo(o2.getDistance());
            }
        });
    }

    public static ArrayList<School> filterByCity(ArrayList<School> list, Context context){
        String addressCity="";
        final HashMap<String,String> addressHashMap = Util.getCurrentUSerAddress(context);
        if (addressHashMap.get("addressCity")!=null){
            if (addressHashMap.get("addressCity").trim().equalsIgnoreCase("Gurgaon")){
                addressCity = "Gurugram";
            }else{
                addressCity = addressHashMap.get("addressCity").trim();
            }

            ArrayList<School> newList = new ArrayList<>();
            if (!addressCity.equals("")){
                for (School school : list){
                    if (school.getAddress()!=null && addressCity.equalsIgnoreCase(school.getAddress().get(School.ADDRESS_CITY))){
                        newList.add(school);
                    }
                }
                return newList;
            }
        }

        return list;
    }

    public static ArrayList<School> filterByCategory(ArrayList<School> list, String category){
        ArrayList<School> newList = new ArrayList<>();
        ArrayList<String> categories;
        for (School school : list){
            if (school.getCategories()!=null) {
                categories = new ArrayList<>(school.getCategories().values());
                if (categories.contains(category.substring(0, category.length() - 1))) {
                    newList.add(school);
                }
            }
        }
        return newList;
    }

    public static ArrayList<String> getAllFacilities(ArrayList<School> list){
        TreeSet<String> newList = new TreeSet<>();
        ArrayList<String> facilities;
        for (School school : list){
            if (school.getFacilities()!=null) {
                facilities = new ArrayList<>(school.getFacilities().values());
                newList.addAll(facilities);
            }
        }
        return new ArrayList<>(newList);
    }

    static TreeSet<String> getAllClassesType(ArrayList<School> list){
        TreeSet<String> newList = new TreeSet<>();
        ArrayList<String> facilities;
        for (School school : list){
            if (school.getClassesType()!=null) {
                facilities = new ArrayList<>(school.getClassesType().values());
                newList.addAll(facilities);
            }
        }
        return newList;
    }

    static TreeSet<String> getAllExtraCuricular(ArrayList<School> list){
        TreeSet<String> newList = new TreeSet<>();
        ArrayList<String> facilities;
        for (School school : list){
            if (school.getExtracurricular()!=null) {
                facilities = new ArrayList<>(school.getExtracurricular().values());
                newList.addAll(facilities);
            }
        }
        return newList;
    }

    static TreeSet<String> getAllOtherGenres(ArrayList<School> list){
        TreeSet<String> newList = new TreeSet<>();
        ArrayList<String> facilities;
        for (School school : list){
            if (school.getOther_genres()!=null) {
                facilities = new ArrayList<>(school.getOther_genres().values());
                newList.addAll(facilities);
            }
        }
        return newList;
    }

    public static ArrayList<School> appliyFilter(ArrayList<String> filtersList, HashMap<String, ArrayList<FilterModel>> filterMap, ArrayList<School> schoolsList) {
        for (String filter : filtersList){
            ArrayList<FilterModel> criterionList = filterMap.get(filter);
            for (FilterModel filterModel : criterionList){
                if (filterModel.isSelected()){
                    for (School school : schoolsList) {
                        ArrayList<School> filteredList = new ArrayList<>();
                        try {
                            if (filter.equalsIgnoreCase(School.TYPE)){
                                char first = Character.toUpperCase(filter.charAt(0));
                                filter = first + filter.substring(1);
                                String str = (String) school.getClass().getMethod("get" + filter).invoke(school);
                                if (str!=null) {
                                    if (str.equals(filterModel.getName())) {
                                        filteredList.add(school);
                                    }
                                }
                            }else{
                                char first = Character.toUpperCase(filter.charAt(0));
                                filter = first + filter.substring(1);
                                HashMap<String, String> map = (HashMap<String, String>) school.getClass().getMethod("get" + filter).invoke(school);
                                if (map!=null) {
                                    ArrayList<String> list = new ArrayList<>(map.values());
                                    if (list.contains(filterModel.getName())) {
                                        filteredList.add(school);
                                    }
                                }
                            }


                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }

                        schoolsList = filteredList;
                    }
                }
            }
        }

        return schoolsList;

    }

    /*filtersList.add(School.TYPE);
    filtersList.add(School.BOARDS);
    filtersList.add(School.CLASSES);
    filtersList.add(School.GENDER);
    filtersList.add(School.SPECIAL_FACILITIES);
    filtersList.add(School.FACILITIES);
    filtersList.add(School.EXTRACURRICULAR);
    filtersList.add(School.CLASSES_TYPE);
    filtersList.add(School.SINGING);
    filtersList.add(School.DANCING);
    filtersList.add(School.INSTRUMENTS);
    filtersList.add(School.OTHER_GENRES);*/
}
