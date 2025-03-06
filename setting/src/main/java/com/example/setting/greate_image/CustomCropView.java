package com.example.setting.greate_image;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CustomCropView extends View {
    private Paint paint;
    private Paint circlePaint;
    private float cornerRadius;

    public CustomCropView(Context context) {
        super(context);
        init();
    }

    public CustomCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.parseColor("#ff33b5e5"));
        paint.setAntiAlias(true); // Для згладжування країв

        circlePaint = new Paint();
        circlePaint.setColor(Color.WHITE); // Колір кола в середині
        circlePaint.setAntiAlias(true);

        cornerRadius = getResources().getDisplayMetrics().density * 50; // Розмір заокруглення
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Малюємо заокруглений прямокутник
        RectF rect = new RectF(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

        // Малюємо круг в середині
        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;
        canvas.drawCircle(centerX, centerY, getWidth() / 4, circlePaint);
    }
}
