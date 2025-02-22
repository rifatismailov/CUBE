package com.example.cube.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

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
    private int statusColor = 0xFF000000; // Чорний колір за замовчуванням для маленького кола

    public ContactCircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        path = new Path();

        // Налаштування для зображення
        imagePaint = new Paint();
        imagePaint.setAntiAlias(true);
        imagePaint.setFilterBitmap(true);
        imagePaint.setDither(true);

        // Налаштування для обвідної лінії
        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(borderWidth);

        // Налаштування для прогрес-індикатора
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeWidth(10f);

        // Налаштування для заднього фону
        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(backgroundColor);

        // Налаштування для маленького кола
        statusPaint = new Paint();
        statusPaint.setAntiAlias(true);
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
        // Малювання заднього фону
        canvas.drawOval(rect, backgroundPaint);

        // Малювання зображення
        canvas.save();
        canvas.clipPath(path);
        super.onDraw(canvas);
        canvas.restore();

        // Малювання обвідної лінії
        canvas.drawOval(rect, borderPaint);

        // Малювання прогрес-кільця
        if (currentProgress > 0 && currentProgress <= 100) {
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            float radius = Math.min(centerX, centerY) - borderWidth;

            float sweepAngle = (currentProgress / 100f) * 360f;
            canvas.drawArc(
                    centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius,
                    -90,
                    sweepAngle,
                    false,
                    progressPaint
            );
        }

        // Малювання маленького кола
        float smallCircleRadius = getWidth() / 10f;
        float smallCircleX = getWidth() - smallCircleRadius - borderWidth;
        float smallCircleY = getHeight() - smallCircleRadius - borderWidth;

        canvas.drawCircle(smallCircleX, smallCircleY, smallCircleRadius, statusPaint);
    }

    // Метод для оновлення прогресу
    public void setProgress(float progress) {
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;

        this.currentProgress = progress;
        invalidate();
    }

    // Метод для зміни кольору прогресу
    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }

    // Метод для зміни кольору заднього фону
    @Override
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        backgroundPaint.setColor(color);
        invalidate();
    }

    // Метод для зміни кольору маленького кола
    public void setStatusColor(int color) {
        this.statusColor = color;
        statusPaint.setColor(color);
        invalidate();
    }

    // Метод для зміни кольору маленького кола за командою
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

    // Метод для очищення прогресу
    public void clearProgress() {
        this.currentProgress = 0;
        invalidate();
    }
}
