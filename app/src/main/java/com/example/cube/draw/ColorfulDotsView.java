package com.example.cube.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * ColorfulDotsView - користувацький View для відображення кольорових квадратів.
 * Кольори генеруються на основі хешів, переданих у метод setHashes().
 */
public class ColorfulDotsView extends View {
    private List<Integer> colors = new ArrayList<>(); // Список кольорів для відображення

    /**
     * Конструктор класу ColorfulDotsView.
     * @param context Контекст застосунку
     * @param attrs Атрибути, задані в XML
     */
    public ColorfulDotsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Оновлює кольори на основі переданих хешів.
     * @param hashes Список хеш-рядків, з яких генеруються кольори
     */
    public void setHashes(List<String> hashes) {
        colors.clear();
        for (String hash : hashes) {
            int color = generateColorFromHash(hash);
            colors.add(color);
        }
        invalidate(); // Перемальовуємо View
    }

    /**
     * Генерує колір на основі першої частини хеш-рядка.
     * @param hash Хеш-рядок
     * @return Колір у форматі int
     */
    private int generateColorFromHash(String hash) {
        try {
            return Color.parseColor("#" + hash.substring(0, 6)); // Використовуємо перші 6 символів хешу
        } catch (Exception e) {
            return Color.BLACK; // Якщо помилка, повертаємо чорний колір
        }
    }

    /**
     * Метод для малювання кольорових квадратів.
     * @param canvas Canvas для малювання
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();

        int squareWidth = 10; // Ширина квадрату
        int squareHeight = 60; // Висота квадрату
        int spacing = 10; // Відстань між квадратами

        int totalWidth = colors.size() * (squareWidth + spacing) - spacing; // Загальна ширина всіх квадратів
        int startX = (getWidth() - totalWidth) / 2; // Вирівнювання по центру горизонтально
        int startY = (getHeight() - squareHeight) / 2; // Вирівнювання по центру вертикально

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
