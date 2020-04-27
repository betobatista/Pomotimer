package com.betobatista.pomotime;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.Random;

public class MainActivity extends Activity {

    private String[] motivationWork = {"Let's get to work!", "You can do it!", "Ok let's go!", "Keep focused!"};
    private String[] motivationRest = {"Ok let's get some rest!", "Relax dude!", "Great job, now relax!", "Good, Let's drink some water!"};

    private CountDownTimer timer;
    private TextView txtTitle;
    private TextView txtTimer;
    private TextView btnStart;
    private TextView txtMotivation;
    private ProgressBar progressBar;
    private NotificationCompat.Builder notification;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private LinearLayout layout;
    private boolean getWork = true;
    private boolean buttonStatus = true;
    private long newTimer;
    private String newTitle;
    private int count = 0;
    private int i;

    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //AdView adView = new AdView(this);
        //adView.setAdSize(AdSize.BANNER);
        //adView.setAdUnitId("ca-app-pub-3807774153243992/9353232646");*/

        /*MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
                        .build());*/
        adView = findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        txtTitle = findViewById(R.id.txtTitle);
        txtTimer = findViewById(R.id.txtTimer);
        txtMotivation = findViewById(R.id.txtMotivation);

        layout = findViewById(R.id.layoutBack);
        progressBar = findViewById(R.id.progressBar);
        mediaPlayer = MediaPlayer.create(this, R.raw.sound_alarm_pomodoro);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        createNotification();
        setNewTimer();

        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonStatus = !buttonStatus;
                setButton();
                i = 0;
                progressBar.setProgress(i);
                if(!buttonStatus) {
                    startTimer();
                } else {
                    cancelTimer();
                }
            }
        });
    }

    private void setNewTimer() {
        String motivationText = null;
        progressBar.setVisibility(View.INVISIBLE);
        Random random = new Random();
        int i = random.nextInt(4);
        if(count < 4) {
            if (getWork) {
                count++;
                newTimer = 15000;
                newTitle = "Working";
                motivationText = motivationWork[i];
                layout.setBackgroundColor(getResources().getColor(R.color.colorWork));
            } else {
                newTimer = 3000;
                newTitle = "Resting";
                motivationText = motivationRest[i];
                layout.setBackgroundColor(getResources().getColor(R.color.colorRest));
            }
        } else {
            count = 0;
            newTimer = 900000;
            newTitle = "Long Rest";
            motivationText = motivationRest[i];
            layout.setBackgroundColor(getResources().getColor(R.color.colorRest));
        }
        txtTitle.setText(newTitle);
        txtMotivation.setText(motivationText);
        setTimer(newTimer);
    }

    private void setTimer(long millis){
        int seconds = (int) (millis/1000);
        int minutes = seconds/60;
        seconds = seconds % 60;
        String time = String.format("%d:%02d", minutes, seconds);

        i++;
        progressBar.setProgress((int) (i * 100 / (newTimer / 1000)));

        if(notification != null) {
            callNotification(newTitle, time);
        }
        txtTimer.setText(time);
    }

    private void setButton(){
        if(buttonStatus){
            btnStart.setText(getString(R.string.start));
        } else {
            btnStart.setText(getString(R.string.cancel));
        }
    }

    private void startTimer() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            vibrator.cancel();
        }
        progressBar.setVisibility(View.VISIBLE);
        timer = new CountDownTimer(newTimer, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                setTimer(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                i++;
                progressBar.setProgress(100);
                getWork = !getWork;
                buttonStatus = !buttonStatus;
                setNewTimer();
                setButton();
                callSound();
            }
        }.start();
    }

    private void callSound() {
        mediaPlayer.start();
        vibrator.vibrate(4000);
    }

    private void cancelTimer() {
        if(timer!= null){
            progressBar.setVisibility(View.INVISIBLE);
            timer.cancel();
            setNewTimer();
        }
    }

    private void createNotification(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        notification = new NotificationCompat.Builder(this, "MYCHANNEL")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
    }

    private void callNotification(String title, String text){
        notification.setContentTitle(title).setContentText(text);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, notification.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(1);

        //if (mediaPlayer != null) mediaPlayer.release();
        if (adView != null) adView.destroy();

        super.onDestroy();
    }

    /**
     * Called when leaving the activity
     */
    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    /**
     * Called when returning to the activity
     */
    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

}
