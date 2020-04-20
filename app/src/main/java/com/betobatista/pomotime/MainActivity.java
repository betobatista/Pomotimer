package com.betobatista.pomotime;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends Activity {

    private CountDownTimer timer;
    private TextView txtTimer;
    private TextView btnStart;
    private ProgressBar progressBar;
    private NotificationCompat.Builder notification;
    private MediaPlayer mediaPlayer;
    private LinearLayout layout;
    private boolean getWork = true;
    private boolean buttonStatus = true;
    private long newTimer;
    private String newTitle;
    private int count = 0;
    private int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTimer = findViewById(R.id.txtTimer);
        layout = findViewById(R.id.layoutBack);
        progressBar = findViewById(R.id.progressBar);
        mediaPlayer = MediaPlayer.create(this, R.raw.sound_buzzer);
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
                    startTimer(newTimer);
                } else {
                    cancelTimer();
                }
            }
        });
    }

    private void setNewTimer() {
        progressBar.setVisibility(View.INVISIBLE);
        if(count < 4) {
            if (getWork) {
                count++;
                newTimer = 15000;
                newTitle = "Working..";
                layout.setBackgroundColor(getResources().getColor(R.color.colorWork));
            } else {
                newTimer = 3000;
                newTitle = "Resting..";
                layout.setBackgroundColor(getResources().getColor(R.color.colorRest));
            }
        } else {
            count = 0;
            newTimer = 900000;
            newTitle = "Long Rest";
            layout.setBackgroundColor(getResources().getColor(R.color.colorRest));
        }
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
            btnStart.setText("Start");
        } else {
            btnStart.setText("Cancel");
        }
    }

    private void startTimer(long newTime) {
        progressBar.setVisibility(View.VISIBLE);
        timer = new CountDownTimer(newTime, 1000) {

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
        SystemClock.sleep(5000);
        mediaPlayer.pause();
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
    }
}
