package com.example.cube.notification;

import android.annotation.SuppressLint;

public class NotificationHandler {

    @SuppressLint("SetTextI18n")
    public void setLog(NotificationViewHolder notificationViewHolder, NotificationLogger logger) {
        notificationViewHolder.binding.info.setText(logger.getInfo());
    }
}
