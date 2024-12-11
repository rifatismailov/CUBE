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
        int radius = 10; // Радіус точки
        int spacing = 21; // Відстань між точками

        int totalWidth = colors.size() * spacing; // Загальна ширина точок
        int startX = (getWidth() - totalWidth) / 2; // Початкова точка для центрованого вмісту
        int y = getHeight() / 2; // Центруємо точки по вертикалі

        for (int i = 0; i < colors.size(); i++) {
            paint.setColor(colors.get(i));
            canvas.drawCircle(startX + i * spacing, y, radius, paint);
        }
    }

}