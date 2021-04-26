package com.neurelectrics.openwave;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.w3c.dom.Text;

import java.util.Map;

import static android.content.Context.VIBRATOR_SERVICE;


public class MainActivity extends WearableActivity {
int mainFreq=5;
int modFreq=0;
int power=255;
String[] freqDes={"7 Hz","10 Hz","15 Hz","20 Hz","25 Hz","Max"};
int[] mainFreqs={7,10,15,20,25,-1};
PendingIntent pi;
double[] modFreqs={-1, 0.1,0.2,0.3,0.4,0.5,1,2,3,4,5,6,7,8,9,10,15,20,25};
String[] modFreqDes={"None","0.1 Hz","0.2 Hz","0.3 Hz","0.4 Hz","0.5 Hz","1 Hz","2 Hz","3 Hz","4 Hz","5 Hz","6 Hz","7 Hz","8 Hz","9 Hz","10 Hz","15 Hz","20 Hz","25 Hz"};


    public void sendNotification() {
//create the channel
        CharSequence name ="alertChannel";
        String description = "Persistent notification when OpenWave is running";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel("alertChannel", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);


        int notificationId = 001;
// The channel ID of the notification.
        String id = "notification";
// Build intent for notification content
        Intent viewIntent = new Intent(this, MainActivity.class);
        viewIntent.putExtra("test", id);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

// Notification channel ID is ignored for Android 7.1.1
// (API level 25) and lower.
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "alertChannel")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("OpenWave is running")
                        .setContentText("Open to change or stop")
                        .setContentIntent(viewPendingIntent)
                        .setOngoing(false);

// Issue the notification with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());


    }

    int[] modulate(int[] pattern1, int[] pattern2) {
        int[] result=new int[1000];
        int old=1000;
        int COMPRESSION_THRESH=0; // don't change the otuput value until its different from previous value by this
        if (modFreqs[modFreq] < 1) {
            COMPRESSION_THRESH=50; // having fewer changes in the signal can reduce choppiness at low frequencies but makes the signal worse at high frequencies
        }
        for (int i=0; i< 1000; i++) {
                float temp1 = (float) (pattern1[i]) / 255f;
                float temp2 = (float) (pattern2[i]) / 255f;
                int temp=(int) Math.round((temp1 * temp2) * 255f);
                if (Math.abs(temp-old) > COMPRESSION_THRESH) {
                    result[i] = temp;
                    old = temp;
                }
                else {
                    result[i]=old;
                }





        }

        return result;
    }

    int[] createPattern(int maxPower, double freq, boolean squareWave) {
        int[] pattern=new int[1000];
        double scale=5/freq;
        //this completes one cycle every 20, giving us a 50 hz pulse sequence
        for (int i=0; i< 1000; i++) {
            double result;
            if (freq<0) { //full on mode
                result=1;
            }
            else {
                result = (Math.sin((double) (i / scale) / Math.PI) + 1) / 2; //generate a sine wave in 20 steps with amplitude ranging from 0 to 1.0
                if (squareWave) {
                    if (result > 0.5) {
                        result = 1;
                    } else {
                        result = 0;
                    }
                }
            }
            pattern[i]=(int)Math.round(result*maxPower);
           
        }
        return pattern;
    }

    long[] createDurations() {
        long[] pattern = new long[1000];
        for (int i = 0; i < 1000; i++) {
            pattern[i] = 10;


        }
        return pattern;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendNotification();


        //start with an initial pattern
        mainFreq=5;
        modFreq=0;
        long[] mVibratePattern = createDurations();
        int[] primaryFreq = createPattern(power,mainFreqs[mainFreq],true);
        int[] moduFreq=createPattern(power,modFreqs[modFreq],false);
        VibrationData vd= VibrationData.getInstance();
        vd.effect = VibrationEffect.createWaveform(mVibratePattern, modulate(primaryFreq,moduFreq), -1);
        vd.main=mainFreqs[mainFreq];
        vd.mod=modFreqs[modFreq];
        Intent msgIntent = new Intent(MainActivity.this, VibrationService.class);
        startService(msgIntent);
        /*
        long[] vduration={1000,1};
        int[] vpower={254,0};
        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        VibrationEffect ef=VibrationEffect.createWaveform(vduration,vpower, 0);
        vibrator.vibrate(ef); */

        int[] temp=modulate(primaryFreq,moduFreq);


        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
/*
        //start the hr monitoring--turned off for production version
        Intent alarmIntent = new Intent(MainActivity.this, hrScan.class);
        pi= PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 60000*3;
       manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pi);*/



        modFreq=0;
        //set up frequency controls
        TextView mfView=(TextView)findViewById(R.id.mf);
        mfView.setText(freqDes[mainFreq]);
        ImageButton mfDec=(ImageButton)findViewById(R.id.mfDec);
        mfDec.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (mainFreq >= 1) {
                    mainFreq--;
                }
                TextView mfView=(TextView)findViewById(R.id.mf);
                mfView.setText(freqDes[mainFreq]);

                long[] mVibratePattern = createDurations();
                int[] primaryFreq = createPattern(power,mainFreqs[mainFreq],true);
                int[] moduFreq=createPattern(power,modFreqs[modFreq],false);
                VibrationData vd= VibrationData.getInstance();
                vd.effect = VibrationEffect.createWaveform(mVibratePattern, modulate(primaryFreq,moduFreq), -1);
                vd.main=mainFreqs[mainFreq];
                vd.mod=modFreqs[modFreq];

                //start the vibration service
                Intent msgIntent = new Intent(MainActivity.this, VibrationService.class);
                startService(msgIntent);


                /*
                long[] mVibratePattern = createDurations();
                int[] primaryFreq = createPattern(255,mainFreqs[mainFreq],true);
                int[] moduFreq=createPattern(255,modFreqs[modFreq],false);
                VibrationEffect effect = VibrationEffect.createWaveform(mVibratePattern, modulate(primaryFreq,moduFreq), 0);
                vibrator.vibrate(effect);*/

            }
        });

        ImageButton mfInc=(ImageButton)findViewById(R.id.mfInc);
        mfInc.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (mainFreq < freqDes.length-1) {
                    mainFreq++;
                }
                TextView mfView=(TextView)findViewById(R.id.mf);
                mfView.setText(freqDes[mainFreq]);
                long[] mVibratePattern = createDurations();
                int[] primaryFreq = createPattern(power,mainFreqs[mainFreq],true);
                int[] moduFreq=createPattern(power,modFreqs[modFreq],false);
                VibrationData vd= VibrationData.getInstance();
                vd.effect = VibrationEffect.createWaveform(mVibratePattern, modulate(primaryFreq,moduFreq), -1);
                primaryFreq=moduFreq=null;
                System.gc();
                vd.main=mainFreqs[mainFreq];
                vd.mod=modFreqs[modFreq];

                //start the vibration service
                Intent msgIntent = new Intent(MainActivity.this, VibrationService.class);
                startService(msgIntent);

            }
        });

        //set up modulation controls
        TextView modView=(TextView)findViewById(R.id.mod);
        modView.setText(modFreqDes[modFreq]);
        ImageButton modDec=(ImageButton)findViewById(R.id.modDec);
        modDec.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (modFreq >= 1) {
                    modFreq--;
                }
                TextView modView=(TextView)findViewById(R.id.mod);
                modView.setText(modFreqDes[modFreq]);
                long[] mVibratePattern = createDurations();
                int[] primaryFreq = createPattern(power,mainFreqs[mainFreq],true);
                int[] moduFreq=createPattern(power,modFreqs[modFreq],false);
                VibrationData vd= VibrationData.getInstance();
                vd.effect = VibrationEffect.createWaveform(mVibratePattern, modulate(primaryFreq,moduFreq), 1);
                primaryFreq=moduFreq=null;
                System.gc();
                vd.main=mainFreqs[mainFreq];
                vd.mod=modFreqs[modFreq];

                //start the vibration service
                Intent msgIntent = new Intent(MainActivity.this, VibrationService.class);
                startService(msgIntent);

            }
        });

        ImageButton modInc=(ImageButton)findViewById(R.id.modInc);
        modInc.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (modFreq < modFreqDes.length-1) {
                    modFreq++;
                }
                TextView modView=(TextView)findViewById(R.id.mod);
                modView.setText(modFreqDes[modFreq]);
                long[] mVibratePattern = createDurations();
                int[] primaryFreq = createPattern(power,mainFreqs[mainFreq],true);
                int[] moduFreq=createPattern(power,modFreqs[modFreq],false);
                VibrationData vd= VibrationData.getInstance();
                vd.effect = VibrationEffect.createWaveform(mVibratePattern, modulate(primaryFreq,moduFreq), -1);
                primaryFreq=moduFreq=null;
                System.gc();
                vd.main=mainFreqs[mainFreq];
                vd.mod=modFreqs[modFreq];

                //start the vibration service
                Intent msgIntent = new Intent(MainActivity.this, VibrationService.class);
                startService(msgIntent);
            }
        });


        Button stop=(Button)findViewById(R.id.stopButton);

        stop.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {


                NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nMgr.cancel(001);
                System.exit(0);
            }
        });

        //bar for controlling amplitude
        SeekBar amp = (SeekBar) findViewById(R.id.powerControl);

        amp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                power=progress;
                long[] mVibratePattern = createDurations();
                int[] primaryFreq = createPattern(power,mainFreqs[mainFreq],true);
                int[] moduFreq=createPattern(power,modFreqs[modFreq],false);
                VibrationData vd= VibrationData.getInstance();


                vd.effect = VibrationEffect.createWaveform(mVibratePattern, modulate(primaryFreq,moduFreq), -1);
                vd.main=mainFreqs[mainFreq];
                vd.mod=modFreqs[modFreq];
                primaryFreq=moduFreq=null;
                System.gc();
                vd.power=progress;

                //start the vibration service
                Intent msgIntent = new Intent(MainActivity.this, VibrationService.class);
                startService(msgIntent);

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //write custom code to on start progress
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });





//output the saved data
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Map<String,?> keys = prefs.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.d("data values",entry.getKey() + ": " +
                    entry.getValue().toString());
        }
    }
    public void onEnterAmbient() {
        Intent msgIntent = new Intent(MainActivity.this, VibrationService.class);
        msgIntent.putExtra("primaryFreq", mainFreqs[mainFreq]);
        msgIntent.putExtra("modFreq", modFreqs[modFreq]);
        startService(msgIntent);
    }

    public void onUserLeaveHint() {
        Log.e("openwave","leaving");
    }


}