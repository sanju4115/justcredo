package com.credolabs.justcredo.utility;

import com.credolabs.justcredo.model.ObjectModel;

import java.util.ArrayList;
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
}
