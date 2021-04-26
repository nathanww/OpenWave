package com.neurelectrics.openwave;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class VibrationService extends IntentService {

    PowerManager.WakeLock wl;


    public VibrationService() {
        super("VibrationService");
    }
    VibrationEffect effect;

    float currentHr=-1;


    @Override
    protected void onHandleIntent(Intent intent) {



        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wl=powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "OpenWave:vibrate");
        wl.acquire();

        //set up the vibrator

        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
         //start a thread to keep this vibration pattern running
        VibrationData vd=VibrationData.getInstance();

        VibrationEffect ef=vd.effect;
        boolean isInteractive=powerManager.isInteractive();
        int resets=0;
        while(true) {
                        Log.i("vibedata",currentHr+","+vd.power+","+vd.mod+","+vd.main);

                        try {
                            int temp=1;
                            while (temp <=10 ) {
                                Thread.sleep(1000);
                                if (!ef.equals(vd.effect) || powerManager.isInteractive() != isInteractive || vd.forceStart) { //only restart vibration if the vibration effect has been updated or if this is the first run or if we need to reset due to the screen swtiching off/on

                                    vibrator.cancel();
                                    vibrator.vibrate(vd.effect);
                                    ef=vd.effect;
                                    isInteractive=powerManager.isInteractive();
                                    vd.forceStart=false;
                                    temp=1;
                                }
                                else {
                                    temp++;
                                }
                            }
                            vibrator.cancel();
                            vibrator.vibrate(vd.effect);
                            /*
                            if (resets >= 10) {
                                vibrator.cancel();
                                vibrator.vibrate(vd.effect);
                                resets=0;
                            }
                            else {
                                resets++;
                            }


                            if (!ef.equals(vd.effect) || powerManager.isInteractive() != isInteractive || vd.forceStart) { //only restart vibration if the vibration effect has been updated or if this is the first run or if we need to reset due to the screen swtiching off/on

                            vibrator.cancel();
                                vibrator.vibrate(vd.effect);
                                ef=vd.effect;
                                isInteractive=powerManager.isInteractive();
                                vd.forceStart=false;
                                resets=0;
                            }*/


                        } catch (Exception e) {
                            Log.e("timing","interrupted");
                        }
                    }







}


public void onDestroy() {
        super.onDestroy();
        wl.release();

}



}