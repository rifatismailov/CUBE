package com.example.cube.sound;

import android.content.Context;
import android.media.MediaPlayer;

import com.example.cube.R;

public class SoundPlayer {
    private MediaPlayer mediaPlayer;

    public void playNotificationSoundChat(Context context) {
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Звільняємо попередній плеєр
        }
        mediaPlayer = MediaPlayer.create(context, R.raw.notification_message_143029); // Файл в res/raw/
        mediaPlayer.setOnCompletionListener(mp -> mp.release()); // Автоматичне закриття після завершення
        mediaPlayer.start(); // Відтворення
    }
    public void playNotificationSound(Context context) {
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Звільняємо попередній плеєр
        }
        mediaPlayer = MediaPlayer.create(context, R.raw.notify_8_313753); // Файл в res/raw/
        mediaPlayer.setOnCompletionListener(mp -> mp.release()); // Автоматичне закриття після завершення
        mediaPlayer.start(); // Відтворення
    }
}
