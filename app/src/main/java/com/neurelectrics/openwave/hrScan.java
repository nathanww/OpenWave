package com.neurelectrics.openwave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import static android.content.Context.POWER_SERVICE;
import static android.content.Context.SENSOR_SERVICE;

public class hrScan extends BroadcastReceiver implements SensorEventListener {
    double currentHr=-1;
    double[] hrVals=new double[30];
    int val=0;
    double maxHR=-1;
    double minHR=1000;
    Handler handler = new Handler();
    SensorManager mSensorManager;
    Sensor mHeartRateSensor;
    long firstEpoch=0;
    SensorEventListener sensorEventListener;
    Context thisContext;
    PowerManager.WakeLock wl;

    SharedPreferences pref;
    private Runnable calculateHRV = new Runnable() {
        @Override
        public void run() {

            if (val < hrVals.length && currentHr >0) { //if the buffer is not full and we have a valid heart rate
                if (currentHr > maxHR) {
                    maxHR=currentHr;
                }
                if (currentHr < minHR) {
                    minHR=currentHr;
                }
                hrVals[val] = currentHr;
                val++;
                handler.postDelayed(this, 1000);
            }
            if (val >= hrVals.length) {

                //get the current stimulation data as well
                VibrationData vd=VibrationData.getInstance();

                SharedPreferences.Editor editor = pref.edit();
                Log.i("hrv",maxHR+","+minHR+","+calculateSD(hrVals)+","+(System.currentTimeMillis()-firstEpoch));
                editor.putString(System.currentTimeMillis()+"",maxHR+","+minHR+","+calculateSD(hrVals)+","+(System.currentTimeMillis()-firstEpoch)+","+vd.power+","+vd.mod+","+vd.main);
                editor.commit();
                val=0;
                hrVals=new double[30];
                currentHr=-1;
                maxHR=-1;
                minHR=1000;
                wl.release();
                mSensorManager.unregisterListener(hrScan.this);
            }

        }
    };


    public static double calculateSD(double numArray[])
    {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.length;

        for(double num : numArray) {
            sum += num;
        }

        double mean = sum/length;

        for(double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation/length);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        wl=powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "OpenWave:hr");
        wl.acquire();


        //start heart rate monitoring
        mSensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        firstEpoch=System.currentTimeMillis();
        mSensorManager.registerListener(this, mHeartRateSensor, mSensorManager.SENSOR_DELAY_FASTEST);
         pref = PreferenceManager.getDefaultSharedPreferences(context);
        handler.post(calculateHRV);







    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            currentHr=event.values[0];
            if (currentHr > 0 || true) {
                Log.i("hr",currentHr+"");

            }

        }
    }



}