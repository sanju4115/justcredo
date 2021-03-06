package com.credolabs.justcredo.model;

import java.io.Serializable;

/**
 * Created by ravindrapatidar on 26/03/17.
 */

public class CategoryModel implements Serializable{

    private String name;
    private String description;
    private String image;

    public static final String CATEGORYMODEL = "categoryModel";

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
}
