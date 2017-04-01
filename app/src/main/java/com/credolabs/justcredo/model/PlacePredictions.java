package com.credolabs.justcredo.model;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 3/25/2017.
 */

public class PlacePredictions {

    public ArrayList<PlaceAutoComplete> getPlaces() {
        return predictions;
    }

    public void setPlaces(ArrayList<PlaceAutoComplete> places) {
        this.predictions = places;
    }

    private ArrayList<PlaceAutoComplete> predictions;
}
