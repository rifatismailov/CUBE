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
 * ColorfulDotsView is a custom View for displaying colored squares.
 * Colors are generated based on hashes passed to the setHashes() method.
 */
public class ColorfulDotsView extends View {
    private final List<Integer> colors = new ArrayList<>(); // List of colors to display

    /**
     * Constructor of the ColorfulDotsView class.
     * @param context Application context
     * @param attrs Attributes specified in XML
     */
    public ColorfulDotsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Updates colors based on the passed hashes.
     * @param hashes List of hash strings from which colors are generated
     */
    public void setHashes(List<String> hashes) {
        colors.clear();
        for (String hash : hashes) {
            int color = generateColorFromHash(hash);
            colors.add(color);
        }
        invalidate(); // Redraw View
    }

    /**
     * Generates color based on the first part of the hash string.
     * @param hash Hash string
     * @return Color in int format
     */
    private int generateColorFromHash(String hash) {
        try {
            return Color.parseColor("#" + hash.substring(0, 6)); // Use the first 6 characters of the hash
        } catch (Exception e) {
            return Color.BLACK; // If error, return black
        }
    }

    /**
     * Method for drawing colored squares.
     * @param canvas Canvas to draw
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();

        int squareWidth = 10; // Width of the square
        int squareHeight = 60; // Height of the square
        int spacing = 10; // Distance between squares

        int totalWidth = colors.size() * (squareWidth + spacing) - spacing; // Total width of all squares
        int startX = (getWidth() - totalWidth) / 2; // Center horizontally
        int startY = (getHeight() - squareHeight) / 2; // Center vertically

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