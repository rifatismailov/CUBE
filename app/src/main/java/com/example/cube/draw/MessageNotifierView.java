package com.example.cube.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MessageNotifierView extends View{

        private List<Integer> colors = new ArrayList<>();

        public MessageNotifierView(Context context, AttributeSet attrs) {
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
            int radius = 15; // Розмір точки
            int spacing = 25; // Відстань між точками
            int x = radius, y = radius;

            for (int color : colors) {
                paint.setColor(color);
                canvas.drawCircle(x, y, radius, paint);
                x += spacing;

                // Якщо досягнуто межі ширини екрана, переносимо на новий рядок
                if (x + radius > getWidth()) {
                    x = radius;
                    y += spacing;
                }
            }
        }
    }

