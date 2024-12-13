package com.example.cube.draw;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ColorfulDotsView extends View {
    private List<Integer> colors = new ArrayList<>();

    public ColorfulDotsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setHashes(List<String> hashes) {
        colors.clear();
        for (String hash : hashes) {
            int color = generateColorFromHash(hash);
            colors.add(color);
        }
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

        int squareWidth = 10; // Ширина квадрату
        int squareHeight = 60; // Висота квадрату
        int spacing = 10; // Відстань між квадратами

        int totalWidth = colors.size() * (squareWidth + spacing) - spacing; // Загальна ширина квадратів
        int startX = (getWidth() - totalWidth) / 10; // Початкова точка для центрованого вмісту
        int startY = (getHeight() - squareHeight) / 2; // Центруємо квадрати по вертикалі

        for (int i = 0; i < colors.size(); i++) {
            paint.setColor(colors.get(i));
            float left = startX + i * (squareWidth + spacing);
            float top = startY;
            float right = left + squareWidth;
            float bottom = top + squareHeight;

            canvas.drawRect(left, top, right, bottom, paint);
        }
    }


}