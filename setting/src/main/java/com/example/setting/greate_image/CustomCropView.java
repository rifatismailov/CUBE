package com.example.setting.greate_image;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Клас CustomCropView створює користувацький вигляд, який малює заокруглений прямокутник та круг в середині.
 * Використовується для відображення специфічного графічного елементу на екрані.
 * Прямокутник має заокруглені кути, а всередині малюється білий круг.
 */
public class CustomCropView extends View {

    // Об'єкти Paint для малювання прямокутника і кола
    private Paint paint;
    private Paint circlePaint;

    // Радіус заокруглення для прямокутника
    private float cornerRadius;

    /**
     * Конструктор класу, який ініціалізує вигляд без атрибутів XML.
     *
     * @param context Контекст, який використовується для ініціалізації.
     */
    public CustomCropView(Context context) {
        super(context);
        init();
    }

    /**
     * Конструктор класу, який ініціалізує вигляд з атрибутами з XML.
     *
     * @param context Контекст, який використовується для ініціалізації.
     * @param attrs Атрибути XML, передані з макету.
     */
    public CustomCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Ініціалізація об'єктів Paint для малювання.
     * Встановлює кольори та інші параметри для малювання.
     */
    private void init() {
        // ініціалізація Paint для прямокутника
        paint = new Paint();
        paint.setColor(Color.parseColor("#000000")); // чорний колір
        paint.setAntiAlias(true); // для згладжування країв

        // ініціалізація Paint для кола
        circlePaint = new Paint();
        circlePaint.setColor(Color.WHITE); // білий колір
        circlePaint.setAntiAlias(true);

        // Встановлення значення заокруглення (залежить від щільності екрану)
        cornerRadius = getResources().getDisplayMetrics().density * 50;
    }

    /**
     * Метод для малювання елементів на канвасі.
     * Малює заокруглений прямокутник, а також круг всередині нього.
     *
     * @param canvas Канвас, на якому буде малюватися.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Малюємо заокруглений прямокутник, що займає всю ширину та висоту View
        RectF rect = new RectF(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

        // Малюємо круг в центрі View
        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;
        canvas.drawCircle(centerX, centerY, getWidth() / 4, circlePaint);
    }
}
