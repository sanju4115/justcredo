package com.credolabs.justcredo.model;

/**
 * Created by Sanjay kumar on 3/25/2017.
 */

public class PlaceAutoComplete {
        private String place_id;
        private String description;

    public Object getStructured_formatting() {
        return structured_formatting;
    }

    public void setStructured_formatting(Object structured_formatting) {
        this.structured_formatting = structured_formatting;
    }

    private Object structured_formatting;

        public String getPlaceDesc() {
            return description;
        }

        public void setPlaceDesc(String placeDesc) {
            description = placeDesc;
        }

        public String getPlaceID() {
            return place_id;
        }

        public void setPlaceID(String placeID) {
            place_id = placeID;
        }
}
