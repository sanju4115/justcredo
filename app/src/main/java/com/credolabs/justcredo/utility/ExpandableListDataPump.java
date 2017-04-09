package com.credolabs.justcredo.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Sanjay kumar on 4/6/2017.
 */

public class ExpandableListDataPump {

    public static HashMap<String, LinkedHashMap<String,String>> getData(Object classes) {
        LinkedHashMap<String,LinkedHashMap<String,String>> feeStructure = (LinkedHashMap<String, LinkedHashMap<String, String>>) classes;

        HashMap<String, LinkedHashMap<String,String>> expandableListDetail = new HashMap<>();

        for (String key : feeStructure.keySet())
        {
            LinkedHashMap<String,String> fees = feeStructure.get(key);
            expandableListDetail.put(key,fees);
        }

        List<String> cricket = new ArrayList<String>();
        cricket.add("India");
        cricket.add("Pakistan");
        cricket.add("Australia");
        cricket.add("England");
        cricket.add("South Africa");

        List<String> football = new ArrayList<String>();
        football.add("Brazil");
        football.add("Spain");
        football.add("Germany");
        football.add("Netherlands");
        football.add("Italy");

        List<String> basketball = new ArrayList<String>();
        basketball.add("United States");
        basketball.add("Spain");
        basketball.add("Argentina");
        basketball.add("France");
        basketball.add("Russia");

        /*expandableListDetail.put("CRICKET TEAMS", cricket);
        expandableListDetail.put("FOOTBALL TEAMS", football);
        expandableListDetail.put("BASKETBALL TEAMS", basketball);*/
        return expandableListDetail;
    }
}
