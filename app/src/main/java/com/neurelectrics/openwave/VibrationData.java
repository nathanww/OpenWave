package com.neurelectrics.openwave;

import android.os.VibrationAttributes;
import android.os.VibrationEffect;

public class VibrationData  {

    private static VibrationData INSTANCE = null;
    public VibrationEffect effect;

    public double mod=0;
    public double main=0;
    public int power=255;
    public boolean forceStart=true;
    // other instance variables can be here

    private VibrationData() {};

    public static VibrationData getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new VibrationData();
        }
        return(INSTANCE);
    }



    // other instance methods can follow
}