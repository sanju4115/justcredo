package com.credolabs.justcredo.model;

import java.util.ArrayList;

/**
 * Created by ravindrapatidar on 26/03/17.
 */

public class Categories {

    public ArrayList<CategoryModel> getCategories() {
        return categories;
    }

    public void setPlaces(ArrayList<CategoryModel> categories) {
        this.categories = categories;
    }

    private ArrayList<CategoryModel> categories;
}
