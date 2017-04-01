package com.credolabs.justcredo.utility;

/**
 * Created by Sanjay kumar on 3/25/2017.
 */

public class AddressDataModel {

    String address;
    String city;
    String knowName;

    public AddressDataModel() {
    }

    public AddressDataModel(String address, String city, String knowName) {
        this.address = address;
        this.city = city;
        this.knowName = knowName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getKnowName() {
        return knowName;
    }

    public void setKnowName(String knowName) {
        this.knowName = knowName;
    }
}
