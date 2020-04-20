package com.betobatista.pomotime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity{

    private CountDownTimer timer;
    private TextView txtTimer;
    private AppCompatButton btnStart;
    private NotificationCompat.Builder notification;
    private MediaPlayer mediaPlayer;
    private boolean getWork = true;
    private boolean buttonStatus = true;
    private long newTimer;
    private String newTitle;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTimer = findViewById(R.id.txtTimer);
        mediaPlayer = MediaPlayer.create(this, R.raw.sound_buzzer);
        createNotification();
        setNewTimer();

        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonStatus = !buttonStatus;
                setButton();
                if(!buttonStatus) {
                    startTimer(newTimer);
                } else {
                    cancelTimer();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setNewTimer() {
        if(count < 4) {
            if (getWork) {
                count++;
                newTimer = 15000;
                newTitle = "Working..";
            } else {
                newTimer = 3000;
                newTitle = "Resting..";
            }
        } else {
            count = 0;
            newTimer = 900000;
            newTitle = "Long Rest";
        }
        setTimer(newTimer);
    }

    private void setTimer(long millis){
        int seconds = (int) (millis/1000);
        int minutes = seconds/60;
        seconds = seconds % 60;
        String time = String.format("%d:%02d", minutes, seconds);

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
        timer = new CountDownTimer(newTime, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                setTimer(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                getWork = !getWork;
                buttonStatus = !buttonStatus;
                setNewTimer();
                callSound();
                setButton();
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
