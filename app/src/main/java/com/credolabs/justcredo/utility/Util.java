package com.credolabs.justcredo.utility;

/**
 * Created by Sanjay kumar on 4/12/2017.
 */

public class Util {
    public static long getMinutesDifference(long timeStart,long timeStop){
        long diff = timeStop - timeStart;
        long diffMinutes = diff / (60 * 1000);

        return  diffMinutes;
    }
}
