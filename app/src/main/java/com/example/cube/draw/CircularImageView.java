package com.example.cube.draw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

public class CircularImageView extends AppCompatImageView {
    private Path path;
    private Paint imagePaint;
    private Paint borderPaint;
    private RectF rect;

    // Значення для обвідного кольору та ширини
    private int borderColor = 0xFF000000; // Чорний колір за замовчуванням
    private float borderWidth = 4f; // Ширина обвідної лінії

    public CircularImageView(Context context, AttributeSet attrs) {
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
        // Малювання зображення
        canvas.save();
        canvas.clipPath(path);
        super.onDraw(canvas);
        canvas.restore();

        // Малювання обвідної лінії
        canvas.drawOval(rect, borderPaint);
    }

    // Метод для зміни кольору обвідної лінії
    public void setBorderColor(int color) {
        this.borderColor = color;
        borderPaint.setColor(color);
        invalidate();
    }

    // Метод для зміни ширини обвідної лінії
    public void setBorderWidth(float width) {
        this.borderWidth = width;
        borderPaint.setStrokeWidth(width);
        invalidate();
    }
}
