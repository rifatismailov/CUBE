package com.example.cube.draw;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class Notification_contact_View extends View {

    public Notification_contact_View(Context context) {
        super(context);
        init();
    }

    public Notification_contact_View(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Notification_contact_View(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Встановлюємо круглу форму
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        setBackground(shape);
    }

    /**
     * Метод для зміни кольору в залежності від команди
     * @param command команда для зміни кольору
     */
    public void setColorByCommand(String command) {
        GradientDrawable background = (GradientDrawable) getBackground();
        switch (command) {
            case "A0101":
                background.setColor(Color.RED);
                break;
            case "A0100":
                background.setColor(Color.BLUE);
                break;
            case "A0111":
                background.setColor(Color.BLACK);
                break;
            default:
                background.setColor(Color.GRAY); // Колір за замовчуванням
                break;
        }
    }
}
