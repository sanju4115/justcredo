package com.credolabs.justcredo.utility;

import java.util.Random;

/**
 * Created by Sanjay kumar on 4/1/2017.
 */

public class RandomNumberGenerator {
    // Random generator method this will generate int nos.
    public int RandomGenerator() {
        Random random = new Random();
        int randomNum = random.nextInt(9);

        return randomNum;
    }
}
