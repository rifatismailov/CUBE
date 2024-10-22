package com.example.cube.dialog;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;

public class KeyboardUtil {
    public static void setupKeyboardListener(Activity activity, View contentView) {
        final View rootView = contentView.getRootView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getHeight();
            int keypadHeight = screenHeight - r.bottom;

            // Якщо клавіатура відкрита, піднімаємо весь контент
            if (keypadHeight > screenHeight * 0.15) { // Клавіатура займає більше ніж 15% висоти екрана
                contentView.setPadding(0, 0, 0, keypadHeight);
            } else {
                contentView.setPadding(0, 0, 0, 0);
            }
        });
    }
}