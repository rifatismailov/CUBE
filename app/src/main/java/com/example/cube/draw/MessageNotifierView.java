package com.example.cube.draw;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.cube.R;

import java.util.ArrayList;
import java.util.List;

public class MessageNotifierView extends View {
    private List<Integer> colors = new ArrayList<>();
    private int progress = 0;  // Змінна для збереження проценту завантаження
    private int progressRadius = 0; // Змінна для збереження радіуса прогресу
    private boolean alignStart = true; // Змінна для налаштування вирівнювання
    private int spacing = 30; // Відстань між кульками

    public MessageNotifierView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageNotifierView, 0, 0);
        try {
            alignStart = a.getBoolean(R.styleable.MessageNotifierView_alignStart, true);
            spacing = a.getDimensionPixelSize(R.styleable.MessageNotifierView_spacing, 30);
        } finally {
            a.recycle();
        }
    }


    public void setHashes(List<String> hashes) {
        colors.clear();
        for (String hash : hashes) {
            int color = generateColorFromHash(hash);
            colors.add(color);
        }
        invalidate(); // Перемалювати View
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate(); // Перемалювати View
    }

    public void setProgressRadius(int radius) {
        this.progressRadius = radius;
        invalidate(); // Перемалювати View
    }

    private int generateColorFromHash(String hash) {
        // Переводимо перші шість символів хешу в колір
        try {
            return Color.parseColor("#" + hash.substring(0, 6));
        } catch (Exception e) {
            return Color.BLACK; // У разі помилки, повертаємо чорний
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        int circleRadius = 15; // Розмір точки

        if (!colors.isEmpty()) {
            int firstCircleY = getHeight() / 2; // Координата Y для всіх кульок

            // Визначення початкової координати X залежно від alignStart
            int firstCircleX = alignStart
                    ? circleRadius + 20 // Початок зліва
                    : getWidth() - circleRadius - 20; // Початок справа

            // Малюємо першу кулю
            paint.setColor(colors.get(0));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(firstCircleX, firstCircleY, circleRadius, paint);

            // Відображення прогресу навколо першої кулі
            if (progress > 0) {
                paint.setColor(colors.get(0));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(10);

                // Використовуємо progressRadius або за замовчуванням обчислюємо його
                int progressCircleRadius = progressRadius > 0 ? progressRadius : circleRadius + 15;
                float sweepAngle = (float) (360 * progress / 100);

                canvas.drawArc(firstCircleX - progressCircleRadius, firstCircleY - progressCircleRadius,
                        firstCircleX + progressCircleRadius, firstCircleY + progressCircleRadius,
                        -90, sweepAngle, false, paint);
            }

            // Малюємо решту куль у рядок
            int direction = alignStart ? 1 : -1; // Напрямок малювання (праворуч або ліворуч)
            int spacingBetweenCircles = 2 * circleRadius + spacing;

            for (int i = 1; i < colors.size(); i++) {
                int x = firstCircleX + (i * spacingBetweenCircles * direction);
                int y = firstCircleY;

                paint.setColor(colors.get(i));
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(x, y, circleRadius, paint);
            }
        }
    }

}
