package com.credolabs.justcredo.search;

import android.content.Context;

import com.credolabs.justcredo.model.FilterModel;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.utility.Util;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

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

    public static HashMap<String,ArrayList<School>> convert(HashMap<String,HashMap<String,String>> map){
        Gson gson = new Gson();
        JsonElement jsonElement = null;
        School school = null;
        HashMap<String,ArrayList<School>> schoolListMap = new HashMap<>();
        ArrayList<School> allSchools = new ArrayList<>();
        ArrayList<School> schools = new ArrayList<>();
        ArrayList<School> musicClasses = new ArrayList<>();
        ArrayList<School> sportsClasses = new ArrayList<>();
        ArrayList<School> artClasses = new ArrayList<>();
        ArrayList<School> coachingClasses = new ArrayList<>();
        ArrayList<School> tutors = new ArrayList<>();
        schoolListMap.put(PlaceTypes.SCHOOLS.getValue(),schools);
        schoolListMap.put(PlaceTypes.MUSIC.getValue(),musicClasses);
        schoolListMap.put(PlaceTypes.SPORTS.getValue(),sportsClasses);
        schoolListMap.put(PlaceTypes.ART.getValue(),artClasses);
        schoolListMap.put(PlaceTypes.COACHING.getValue(),coachingClasses);
        schoolListMap.put(PlaceTypes.PrivateTutors.getValue(),tutors);
        schoolListMap.put("ALL",allSchools);

        if (map!=null) {
            ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(map.values());
            for (HashMap<String, String> eachMap : list) {
                jsonElement = gson.toJsonTree(eachMap);
                school = gson.fromJson(jsonElement, School.class);
                allSchools.add(school);
                if (school.getType() != null && school.getType().equalsIgnoreCase(PlaceTypes.SCHOOLS.getValue())) {
                    schools.add(school);
                } else if (school.getType() != null && school.getType().equalsIgnoreCase(PlaceTypes.MUSIC.getValue())) {
                    musicClasses.add(school);
                } else if (school.getType() != null && school.getType().equalsIgnoreCase(PlaceTypes.SPORTS.getValue())) {
                    sportsClasses.add(school);
                } else if (school.getType() != null && school.getType().equalsIgnoreCase(PlaceTypes.ART.getValue())) {
                    artClasses.add(school);
                } else if (school.getType() != null && school.getType().equalsIgnoreCase(PlaceTypes.COACHING.getValue())) {
                    coachingClasses.add(school);
                } else if (school.getType() != null && school.getType().equalsIgnoreCase(PlaceTypes.PrivateTutors.getValue())) {
                    tutors.add(school);
                }
            }
        }

        return schoolListMap;

    }

    public static ArrayList<School> filterByTypeAndCity(ArrayList<School> list, Context context, String type){
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
                        if (school.getType()!=null && school.getType().equalsIgnoreCase(type)) {
                            newList.add(school);
                        }
                    }
                }
                return newList;
            }
        }

        return list;
    }


    public static ArrayList<School> filterByType(ArrayList<School> list, String type){

        ArrayList<School> newList = new ArrayList<>();
        for (School school : list){
            if (school.getType()!=null && school.getType().equalsIgnoreCase(type)){
                newList.add(school);
            }
        }
        return newList;
    }

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

    public static ArrayList<School> searchFull(ArrayList<School> schools, String query){
        TreeSet<School> newList = new TreeSet<>();
        for (School school : schools){
            if (school.getName()!=null && school.getName().toLowerCase().contains(query.toLowerCase())){
                newList.add(school);
            }
            if (school.getAddress()!=null){
                HashMap<String,String> map = school.getAddress();
                ArrayList<String> list = new ArrayList<>(map.values());
                for (String str : list){
                    if (str.toLowerCase().contains(query.toLowerCase())){
                        newList.add(school);
                    }
                }
            }
            if (school.getCategories()!=null){
                HashMap<String,String> map = school.getCategories();
                ArrayList<String> list = new ArrayList<>(map.values());
                for (String str : list){
                    if (str.toLowerCase().contains(query.toLowerCase())){
                        newList.add(school);
                    }
                }
            }
            if (school.getExtracurricular()!=null){
                HashMap<String,String> map = school.getExtracurricular();
                ArrayList<String> list = new ArrayList<>(map.values());
                for (String str : list){
                    if (str.toLowerCase().contains(query.toLowerCase())){
                        newList.add(school);
                    }
                }
            }
            if (school.getFacilities()!=null){
                HashMap<String,String> map = school.getFacilities();
                ArrayList<String> list = new ArrayList<>(map.values());
                for (String str : list){
                    if (str.toLowerCase().contains(query.toLowerCase())){
                        newList.add(school);
                    }
                }
            }
            if (school.getSpecialFacilities()!=null){
                HashMap<String,String> map = school.getSpecialFacilities();
                ArrayList<String> list = new ArrayList<>(map.values());
                for (String str : list){
                    if (str.toLowerCase().contains(query.toLowerCase())){
                        newList.add(school);
                    }
                }
            }
            if (school.getMusic()!=null){
                HashMap<String,String> map = school.getMusic();
                ArrayList<String> list = new ArrayList<>(map.values());
                for (String str : list){
                    if (str.toLowerCase().contains(query.toLowerCase())){
                        newList.add(school);
                    }
                }
            }
            if (school.getSports()!=null){
                HashMap<String,String> map = school.getSports();
                ArrayList<String> list = new ArrayList<>(map.values());
                for (String str : list){
                    if (str.toLowerCase().contains(query.toLowerCase())){
                        newList.add(school);
                    }
                }
            }
            if (school.getBoards()!=null){
                HashMap<String,String> map = school.getBoards();
                ArrayList<String> list = new ArrayList<>(map.values());
                for (String str : list){
                    if (str.toLowerCase().contains(query.toLowerCase())){
                        newList.add(school);
                    }
                }
            }
            if (school.getClasses()!=null){
                HashMap<String,String> map = school.getClasses();
                ArrayList<String> list = new ArrayList<>(map.values());
                for (String str : list){
                    if (str.toLowerCase().contains(query.toLowerCase())){
                        newList.add(school);
                    }
                }
            }
            if (school.getSinging()!=null){
                HashMap<String,String> map = school.getSinging();
                ArrayList<String> list = new ArrayList<>(map.values());
                for (String str : list){
                    if (str.toLowerCase().contains(query.toLowerCase())){
                        newList.add(school);
                    }
                }
            }
            if (school.getDancing()!=null){
                HashMap<String,String> map = school.getDancing();
                ArrayList<String> list = new ArrayList<>(map.values());
                for (String str : list){
                    if (str.toLowerCase().contains(query.toLowerCase())){
                        newList.add(school);
                    }
                }
            }
            if (school.getInstruments()!=null){
                HashMap<String,String> map = school.getInstruments();
                ArrayList<String> list = new ArrayList<>(map.values());
                for (String str : list){
                    if (str.toLowerCase().contains(query.toLowerCase())){
                        newList.add(school);
                    }
                }
            }
            if (school.getOther_genres()!=null){
                HashMap<String,String> map = school.getOther_genres();
                ArrayList<String> list = new ArrayList<>(map.values());
                for (String str : list){
                    if (str.toLowerCase().contains(query.toLowerCase())){
                        newList.add(school);
                    }
                }
            }
            if (school.getClassesType()!=null){
                HashMap<String,String> map = school.getClassesType();
                ArrayList<String> list = new ArrayList<>(map.values());
                for (String str : list){
                    if (str.toLowerCase().contains(query.toLowerCase())){
                        newList.add(school);
                    }
                }
            }
            if (school.getType()!=null){
                String map = school.getType();
                if (map.toLowerCase().contains(query.toLowerCase())){
                    newList.add(school);
                }
            }
        }
        return new ArrayList<>(newList);
    }

    public static ArrayList<School> appliyFilter(ArrayList<String> filtersList, HashMap<String, ArrayList<FilterModel>> filterMap, ArrayList<School> schoolsList) {
        for (String filter : filtersList){
            ArrayList<FilterModel> criterionList = filterMap.get(filter);
            for (FilterModel filterModel : criterionList){
                if (filterModel.isSelected()){
                    ArrayList<School> filteredList = new ArrayList<>();
                    for (School school : schoolsList) {
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

                    }
                    schoolsList = filteredList;
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
