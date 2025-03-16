package com.example.cube.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class MessageNotifierView extends View {
    private String text = "";
    private int backgroundColor = 0xFFABCDEF; // Default color
    private Paint backgroundPaint;
    private Paint textPaint;
    private RectF rect;
    private int padding = 20;

    public MessageNotifierView(Context context) {
        super(context);
        init();
    }

    public MessageNotifierView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(25);

        // Встановлюємо шрифт як monospace та стиль як bold
        textPaint.setTypeface(Typeface.MONOSPACE);  // Для шрифта monospace
        textPaint.setTextSkewX(-0.25f);  // Для моноширинних шрифтів можна додавати інші ефекти (опційно)
        textPaint.setFakeBoldText(true);  // Для жирного шрифта

        textPaint.setTextAlign(Paint.Align.CENTER);
        rect = new RectF();
    }

    public void setMessage(String message) {
        this.text = message;
        requestLayout(); // Перерахунок розміру при зміні тексту
        invalidate();
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minWidth = padding * 2;
        int minHeight = padding * 2 + (int) textPaint.getTextSize();

        // Вимірювання тексту
        float textWidth = textPaint.measureText(text) + padding * 2;

        int width = resolveSize((int) textWidth, widthMeasureSpec);
        int height = resolveSize(minHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        rect.set(0, 0, width, height);

        // Просто встановлюємо однорідний колір
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setShader(null);  // Важливо прибрати градієнт

        // Малюємо заокруглений прямокутник
        canvas.drawRoundRect(rect, 40, 40, backgroundPaint);

        // Малюємо текст по центру
        float x = width / 2f;
        float y = (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText(text, x, y, textPaint);
    }
}
