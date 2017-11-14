package com.credolabs.justcredo.newplace;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 10/21/2017.
 */

public class PlaceUtil {

    public static ArrayList<String> getClassesTypeList(){
        ArrayList<String> list = new ArrayList<>();
        list.add(PlaceTypes.Classses.PlayGroup.getValue());
        list.add(PlaceTypes.Classses.Nursery.getValue());
        list.add(PlaceTypes.Classses.LKG.getValue());
        list.add(PlaceTypes.Classses.UKG.getValue());
        list.add(PlaceTypes.Classses.FIRST.getValue());
        list.add(PlaceTypes.Classses.SECOND.getValue());
        list.add(PlaceTypes.Classses.THIRD.getValue());
        list.add(PlaceTypes.Classses.FOURTH.getValue());
        list.add(PlaceTypes.Classses.FIFTH.getValue());
        list.add(PlaceTypes.Classses.SIXTH.getValue());
        list.add(PlaceTypes.Classses.SEVENTh.getValue());
        list.add(PlaceTypes.Classses.EIGHTH.getValue());
        list.add(PlaceTypes.Classses.NINETH.getValue());
        list.add(PlaceTypes.Classses.TENTH.getValue());
        list.add(PlaceTypes.Classses.ELEVENTH.getValue());
        list.add(PlaceTypes.Classses.TWELTH.getValue());
        return list;
    }

    public static ArrayList<String> getSchoolTypeList(){
        ArrayList<String> list = new ArrayList<>();
        list.add(PlaceTypes.SchoolTypes.Pre.getValue());
        list.add(PlaceTypes.SchoolTypes.Primary.getValue());
        list.add(PlaceTypes.SchoolTypes.Secondary.getValue());
        list.add(PlaceTypes.SchoolTypes.SpecialSchool.getValue());
        list.add(PlaceTypes.SchoolTypes.International.getValue());
        return list;
    }

    public static ArrayList<String> getBoardTypeList(){
        ArrayList<String> list = new ArrayList<>();
        list.add(PlaceTypes.Boards.CBSE.getValue());
        list.add(PlaceTypes.Boards.CISCE.getValue());
        list.add(PlaceTypes.Boards.NIOS.getValue());
        list.add(PlaceTypes.Boards.StateBoards.getValue());
        list.add(PlaceTypes.Boards.CIE.getValue());
        list.add(PlaceTypes.Boards.IBO.getValue());
        return list;
    }

    public static ArrayList<String> getGenderList(){
        ArrayList<String> list = new ArrayList<>();
        list.add(PlaceTypes.Gender.BOYS.getValue());
        list.add(PlaceTypes.Gender.GIRLS.getValue());
        list.add(PlaceTypes.Gender.CO.getValue());
        return list;
    }

    public static ArrayList<String> getExtraFacilities(){
        ArrayList<String> list = new ArrayList<>();
        list.add(PlaceTypes.ExtraFacilities.Boarding.getValue());
        list.add(PlaceTypes.ExtraFacilities.Music.getValue());
        list.add(PlaceTypes.ExtraFacilities.Singing.getValue());
        list.add(PlaceTypes.ExtraFacilities.Painting.getValue());
        list.add(PlaceTypes.ExtraFacilities.Sports.getValue());
        return list;
    }
}
