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

/**
 * MessageNotifierView - кастомний View для відображення текстового повідомлення на кольоровому фоні.
 */
public class MessageNotifierView extends View {
    private String text = ""; // Текстове повідомлення
    private int backgroundColor = 0xFFABCDEF; // Колір фону (за замовчуванням)
    private Paint backgroundPaint; // Об'єкт для малювання фону
    private Paint textPaint; // Об'єкт для малювання тексту
    private RectF rect; // Прямокутник для визначення області малювання
    private int padding = 20; // Відступи

    /**
     * Конструктор, що використовується при створенні View в коді.
     * @param context Контекст застосунку.
     */
    public MessageNotifierView(Context context) {
        super(context);
        init();
    }

    /**
     * Конструктор, що використовується при створенні View через XML.
     * @param context Контекст застосунку.
     * @param attrs Атрибути, задані в XML.
     */
    public MessageNotifierView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Ініціалізація параметрів малювання.
     */
    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(25);

        // Встановлення моноширинного шрифту та жирного стилю
        textPaint.setTypeface(Typeface.MONOSPACE);
        textPaint.setTextSkewX(-0.25f);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        rect = new RectF();
    }

    /**
     * Встановлює текстове повідомлення.
     * @param message Текст, який буде відображено.
     */
    public void setMessage(String message) {
        this.text = message;
        requestLayout(); // Перерахувати розміри
        invalidate(); // Викликати перерисовку
    }

    /**
     * Встановлює колір фону.
     * @param color Колір у форматі ARGB.
     */
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        invalidate(); // Викликати перерисовку
    }

    /**
     * Визначає розміри компонента, беручи до уваги текст і відступи.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minWidth = padding * 2;
        int minHeight = padding * 2 + (int) textPaint.getTextSize();

        // Визначаємо ширину тексту
        float textWidth = textPaint.measureText(text) + padding * 2;

        int width = resolveSize((int) textWidth, widthMeasureSpec);
        int height = resolveSize(minHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    /**
     * Малює фон і текст у середині View.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        rect.set(0, 0, width, height);

        // Малюємо фон
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setShader(null); // Видаляємо можливі ефекти градієнта
        canvas.drawRoundRect(rect, 40, 40, backgroundPaint);

        // Малюємо текст у центрі
        float x = width / 2f;
        float y = (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText(text, x, y, textPaint);
    }
}