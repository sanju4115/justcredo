package com.credolabs.justcredo.enums;

public enum PageTypes {
    DETAIL_PAGE("detail_page"),
    READ_REVIEW_PAGE("read_reviews"),
    OWN_PROFILE_PAGE("own_profile");

    private final String type;

    PageTypes(String type) {
        this.type = type;
    }

    public String getValue() {
        return this.type;
    }
}