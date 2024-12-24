package com.example.cube.chat.preview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class HashBitmapGenerator {

    /**
     * Генерує Bitmap зображення на основі хеш-сумми
     *
     * @param hash   Хеш-сумма у вигляді рядка
     * @param width  Ширина зображення
     * @param height Висота зображення
     * @return Зображення у вигляді Bitmap
     */
    public static Bitmap generateHashBitmap(String fileName, String hash, int width, int height) {
        // Розмір одного "пікселя" в квадратах
        int pixelSize = 30;

        // Створення Bitmap з необхідними розмірами
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Перетворюємо хеш у масив байтів
        byte[] hashBytes = hash.getBytes();

        // Генеруємо кольори на основі байтів хешу
        for (int y = 0; y < height; y += pixelSize) {
            for (int x = 0; x < width; x += pixelSize) {
                int byteIndex = (x / pixelSize + (y / pixelSize) * (width / pixelSize)) % hashBytes.length;
                int color = getColorFromBytes(hashBytes, byteIndex);

                // Заповнюємо квадрат 2x2 пікселів
                for (int dy = 0; dy < pixelSize; dy++) {
                    for (int dx = 0; dx < pixelSize; dx++) {
                        if (x + dx < width && y + dy < height) {
                            bitmap.setPixel(x + dx, y + dy, color);
                        }
                    }
                }
            }
        }
        String result = addLineBreaks(fileName, 15);
        addCenterText(canvas, result, width, height);

        return bitmap;
    }

    /**
     * Перетворює байти у колір
     *
     * @param hashBytes Масив байтів хешу
     * @param index     Індекс байта у масиві
     * @return Колір
     */
    private static int getColorFromBytes(byte[] hashBytes, int index) {
        int red = hashBytes[index] & 0xFF;
        int green = hashBytes[(index + 1) % hashBytes.length] & 0xFF;
        int blue = hashBytes[(index + 2) % hashBytes.length] & 0xFF;

        return Color.rgb(red, green, blue);
    }

    /**
     * Додає текст у центр зображення
     *
     * @param canvas Canvas для малювання
     * @param text   Текст для додавання
     * @param width  Ширина зображення
     * @param height Висота зображення
     */
    private static void addCenterText(Canvas canvas, String text, int width, int height) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);
        String[] lines = text.split("\n");
        float lineHeight = paint.getTextSize() + 10; // Додатковий простір між рядками

        // Вимірюємо загальну висоту тексту для центрування
        float totalTextHeight = lines.length * lineHeight;
        float textX = width / 2;
        float textY = (height / 2) - (totalTextHeight / 2) + (lineHeight / 2);

        for (String line : lines) {
            canvas.drawText(line, textX, textY, paint);
            textY += lineHeight; // Перехід до наступного рядка
        }
    }


        public static String addLineBreaks (String input,int length){
            StringBuilder formattedString = new StringBuilder();
            for (int i = 0; i < input.length(); i += length) {
                int end = Math.min(i + length, input.length());
                formattedString.append(input.substring(i, end));
                if (end < input.length()) {
                    formattedString.append("\n");
                }
            }
            return formattedString.toString();
        }
    }
