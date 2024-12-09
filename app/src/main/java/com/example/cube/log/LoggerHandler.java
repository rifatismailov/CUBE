package com.example.cube.log;

import android.annotation.SuppressLint;

public class LoggerHandler {

    @SuppressLint("SetTextI18n")
    public void setLog(LoggerViewHolder loggerViewHolder, Logger logger) {
        loggerViewHolder.binding.clas.setText(logger.getClas());
        loggerViewHolder.binding.logger.setText(logger.getLog());
    }
}
