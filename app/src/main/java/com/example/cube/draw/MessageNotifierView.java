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

/**
 * Клас MessageNotifierView - віджет для відображення кольорових кругів,
 * що представляють повідомлення, з можливістю візуалізації прогресу.
 */
public class MessageNotifierView extends View {

    /**
     * Список кольорів, що будуть використовуватися для відображення кругів.
     */
    private List<Integer> colors = new ArrayList<>();

    /**
     * Поточний рівень прогресу (у відсотках).
     */
    private int progress = 0;

    /**
     * Радіус круга, що відображає прогрес.
     */
    private int progressRadius = 0;

    /**
     * Визначає, чи буде вирівнювання кругів починатися зліва (true) або справа (false).
     */
    private final boolean alignStart;

    /**
     * Відстань між кругами.
     */
    private final int spacing;

    /**
     * Конструктор класу, що ініціалізує параметри з XML.
     *
     * @param context Контекст додатку.
     * @param attrs Атрибути, отримані з XML.
     */
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

    /**
     * Встановлює список кольорів на основі переданих хешів.
     *
     * @param hashes Список хешів, які будуть перетворені в кольори.
     */
    public void setHashes(List<String> hashes) {
        colors.clear();
        for (String hash : hashes) {
            int color = generateColorFromHash(hash);
            colors.add(color);
        }
        invalidate(); // Оновлення вигляду
    }

    /**
     * Встановлює рівень прогресу.
     *
     * @param progress Значення прогресу (у відсотках).
     */
    public void setProgress(int progress) {
        this.progress = progress;
        invalidate(); // Оновлення вигляду
    }

    /**
     * Встановлює радіус прогресного круга.
     *
     * @param radius Радіус круга для відображення прогресу.
     */
    public void setProgressRadius(int radius) {
        this.progressRadius = radius;
        invalidate(); // Оновлення вигляду
    }

    /**
     * Генерує колір на основі хеш-значення.
     *
     * @param hash Хеш-рядок.
     * @return Згенерований колір або чорний у разі помилки.
     */
    private int generateColorFromHash(String hash) {
        try {
            return Color.parseColor("#" + hash.substring(0, 6));
        } catch (Exception e) {
            return Color.BLACK;
        }
    }

    /**
     * Метод для малювання кругів на Canvas.
     *
     * @param canvas Canvas, на якому відображається графіка.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        int circleRadius = 10; // Радіус основного круга

        if (!colors.isEmpty()) {
            int firstCircleY = getHeight() / 2; // Вертикальна координата

            // Визначаємо початкову горизонтальну координату залежно від вирівнювання
            int firstCircleX = alignStart ? circleRadius + 15 : getWidth() - circleRadius - 15;

            // Малюємо перший круг
            paint.setColor(colors.get(0));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(firstCircleX, firstCircleY, circleRadius, paint);

            // Малюємо прогрес-індикатор
            if (progress > 0) {
                paint.setColor(colors.get(0));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);

                int progressCircleRadius = progressRadius > 0 ? progressRadius : circleRadius + 5;
                float sweepAngle = (float) (360 * progress / 100);

                canvas.drawArc(
                        firstCircleX - progressCircleRadius, firstCircleY - progressCircleRadius,
                        firstCircleX + progressCircleRadius, firstCircleY + progressCircleRadius,
                        -90, sweepAngle, false, paint
                );
            }

            // Малюємо решту кругів
            int direction = alignStart ? 1 : -1;
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
