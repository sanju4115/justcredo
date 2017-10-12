package com.credolabs.justcredo.model;

/**
 * Created by Sanjay kumar on 3/30/2017.
 */

public class HistorySearchedModel {
    String mainText;
    String secText;
    String latitude;
    String longitude;
    String addressLine1;
    String addressLine2;
    String addressCity;
    String addressState;
    String addressCountry;

    public HistorySearchedModel() {
    }

    public HistorySearchedModel(String mainText, String secText, String latitude, String longitude, String addressLine1, String addressLine2, String addressCity, String addressState, String addressCountry) {
        this.mainText = mainText;
        this.secText = secText;
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressCity = addressCity;
        this.addressState = addressState;
        this.addressCountry = addressCountry;
    }

    public String getMainText() {
        return mainText;
    }

    public void setMainText(String mainText) {
        this.mainText = mainText;
    }

    public String getSecText() {
        return secText;
    }

    public void setSecText(String secText) {
        this.secText = secText;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressState() {
        return addressState;
    }

    public void setAddressState(String addressState) {
        this.addressState = addressState;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
    }
}
