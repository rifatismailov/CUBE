package com.example.cube.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * ContactCircularImageView - спеціальний ImageView для відображення зображення у формі кола
 * з можливістю малювання обвідної лінії, індикатора прогресу та статусного кола.
 */
public class ContactCircularImageView extends AppCompatImageView {
    private Path path;
    private Paint imagePaint;
    private Paint borderPaint;
    private Paint progressPaint;
    private Paint backgroundPaint;
    private Paint statusPaint;

    private RectF rect;
    private int borderColor = 0xFF000000; // Чорний колір за замовчуванням
    private float borderWidth = 4f;
    private float currentProgress = 0f; // Прогрес від 0 до 100
    private int progressColor = 0xFF049FD9; // Помаранчевий колір прогресу
    private int backgroundColor = 0xFF049fd9; // Синій колір заднього фону
    private int statusColor = 0xFF000000; // Чорний колір статусного кола

    /**
     * Конструктор класу.
     * @param context Контекст застосунку.
     * @param attrs Атрибути XML.
     */
    public ContactCircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Ініціалізація об'єктів малювання.
     */
    private void init() {
        path = new Path();

        imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        imagePaint.setFilterBitmap(true);
        imagePaint.setDither(true);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(borderWidth);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeWidth(10f);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(backgroundColor);

        statusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        statusPaint.setStyle(Paint.Style.FILL);
        statusPaint.setColor(statusColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rect = new RectF(borderWidth / 2, borderWidth / 2, w - borderWidth / 2, h - borderWidth / 2);
        path.reset();
        path.addOval(rect, Path.Direction.CCW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawOval(rect, backgroundPaint);
        canvas.save();
        canvas.clipPath(path);
        super.onDraw(canvas);
        canvas.restore();
        canvas.drawOval(rect, borderPaint);

        if (currentProgress > 0 && currentProgress <= 100) {
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            float radius = Math.min(centerX, centerY) - borderWidth;
            float sweepAngle = (currentProgress / 100f) * 360f;
            canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, -90, sweepAngle, false, progressPaint);
        }

        float smallCircleRadius = getWidth() / 10f;
        float smallCircleX = getWidth() - smallCircleRadius - borderWidth;
        float smallCircleY = getHeight() - smallCircleRadius - borderWidth;
        canvas.drawCircle(smallCircleX, smallCircleY, smallCircleRadius, statusPaint);
    }

    /**
     * Встановлює поточний прогрес у відсотках.
     * @param progress значення від 0 до 100.
     */
    public void setProgress(float progress) {
        this.currentProgress = Math.max(0, Math.min(progress, 100));
        invalidate();
    }

    /**
     * Змінює колір прогрес-індикатора.
     * @param color новий колір прогресу.
     */
    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }

    /**
     * Змінює колір заднього фону.
     * @param color новий колір фону.
     */
    @Override
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        backgroundPaint.setColor(color);
        invalidate();
    }

    /**
     * Змінює колір маленького статусного кола.
     * @param color новий колір статусу.
     */
    public void setStatusColor(int color) {
        this.statusColor = color;
        statusPaint.setColor(color);
        invalidate();
    }

    /**
     * Оновлює колір статусного кола відповідно до команди.
     * @param command рядок команди ("10", "01", "A0111" або інше).
     */
    public void updateStatusColor(String command) {
        switch (command) {
            case "10":
                setStatusColor(0xFF00FF00); // Зелений
                break;
            case "01":
                setStatusColor(0xFFFFFF00); // Жовтий
                break;
            case "A0111":
                setStatusColor(0xFF888888); // Середньо сірий
                break;
            default:
                setStatusColor(0xFF000000); // Чорний за замовчуванням
                break;
        }
    }

    /**
     * Очищає прогрес (встановлює його на 0).
     */
    public void clearProgress() {
        this.currentProgress = 0;
        invalidate();
    }
}
