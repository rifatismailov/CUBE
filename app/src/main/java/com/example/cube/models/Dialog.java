package com.example.cube.models;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

public class Dialog {
    int x;
    int y;
    View linearlayout;
    AlertDialog alertDialog;

    public Dialog(Context context, int dialog, int dialogStyle, int x, int y) {
        this.x = x;
        this.y = y;
        final AlertDialog.Builder ratingdialog = new AlertDialog.Builder(context);
        final View linearlayout = LayoutInflater.from(context).inflate(dialog, null);
        this.linearlayout = linearlayout;
        ratingdialog.setView(linearlayout);
        final AlertDialog alertDialog = ratingdialog.show();
        if (dialogStyle == 0) {
            alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        } else {
            alertDialog.getWindow().getAttributes().windowAnimations = dialogStyle;
            /**установка прозрачного фона вашего диалога*/
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
            params.x = x;
            params.y = y - 300;
            params.gravity = Gravity.TOP | Gravity.LEFT;
            alertDialog.getWindow().setAttributes(params);
        }
        this.alertDialog = alertDialog;
    }

    public View getLinearlayout() {
        return linearlayout;
    }

    public AlertDialog getAlertDialog() {
        return alertDialog;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}