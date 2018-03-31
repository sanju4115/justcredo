package com.credolabs.justcredo.newplace;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 10/21/2017.
 */

public enum PlaceTypes {

    SCHOOLS("Schools"),
    MUSIC("Music Classes"),
    SPORTS("Sports Classes"),
    ART("Art Classes"),
    COACHING("Coaching"),
    PrivateTutors("Private Tutors");

    private final String type;

    PlaceTypes(String type) {
        this.type = type;
    }

    public String getValue() {
        return this.type;
    }

    public ArrayList<String> getList(){
        ArrayList<String> list = new ArrayList<>();
        list.add(SCHOOLS.getValue());
        list.add(MUSIC.getValue());
        list.add(ART.getValue());
        list.add(SPORTS.getValue());
        return list;
    }

    public enum SchoolTypes {
        Pre("Pre School"),
        Primary("Primary School"),
        Secondary("Secondary School"),
        SpecialSchool("Special School"),
        International("International School");

        private final String type;

        SchoolTypes(String type) {
            this.type = type;
        }

        public String getValue() {
            return this.type;
        }


    }

    public enum Boards {
        InternationalBoards("International Boards"),
        NationalBoards("National Boards"),
        CBSE("CBSE"),
        CISCE("CISCE(ICSE,ISC)"),
        NIOS("NIOS"),
        StateBoards("State Government Boards"),
        CIE("CIE"),
        IBO("IBO");

        private final String type;

        Boards(String type) {
            this.type = type;
        }

        public String getValue() {
            return this.type;
        }


    }

    public enum Classses {
        PlayGroup("Play Group(Pre-Nursery)"),
        Nursery("Nursery"),
        LKG("LKG"),
        UKG("UKG"),
        FIRST("1st Class"),
        SECOND("2nd Class"),
        THIRD("3rd Class"),
        FOURTH("4th Class"),
        FIFTH("5th Class"),
        SIXTH("6th Class"),
        SEVENTh("7th Class"),
        EIGHTH("8th Class"),
        NINETH("9th Class"),
        TENTH("10th Class"),
        ELEVENTH("11th Class"),
        TWELTH("12th Class");

        private final String type;

        Classses(String type) {
            this.type = type;
        }

        public String getValue() {
            return this.type;
        }
    }

    public enum Gender {
        BOYS("Boys"),
        GIRLS("Girls"),
        CO("Co-Educational");

        private final String type;

        Gender(String type) {
            this.type = type;
        }

        public String getValue() {
            return this.type;
        }
    }

    public enum ExtraFacilities {
        Boarding("Boarding Facility"),
        Music("Music Classes"),
        Singing("Singing Classes"),
        Painting("Painting Classes"),
        Sports("Sports Classes");

        private final String type;

        ExtraFacilities(String type) {
            this.type = type;
        }

        public String getValue() {
            return this.type;
        }
    }

    public enum Action {
        BUILD("build"),
        EDIT("edit"),
        EDIT_BACKUP("edit_backup");

        private final String type;

        Action(String type) {
            this.type = type;
        }

        public String getValue() {
            return this.type;
        }
    }



}
