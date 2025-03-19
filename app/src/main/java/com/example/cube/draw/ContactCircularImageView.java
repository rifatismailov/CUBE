package com.example.cube.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * ContactCircularImageView is a special ImageView for displaying a circular image
 * with the ability to draw a border line, progress indicator, and status circle.
 */
public class ContactCircularImageView extends AppCompatImageView {
    private Path path;
    private Paint imagePaint;
    private Paint borderPaint;
    private Paint progressPaint;
    private Paint backgroundPaint;
    private Paint statusPaint;

    private RectF rect;
    private int borderColor = 0xFF000000; // Default black color
    private float borderWidth = 4f;
    private float currentProgress = 0f; // Progress from 0 to 100
    private int progressColor = 0xFF049FD9; // Orange progress color
    private int backgroundColor = 0xFF049fd9; // Blue background color
    private int statusColor = 0xFF000000; // Black status circle color

    /**
     * Class constructor.
     * @param context Application context.
     * @param attrs XML attributes.
     */
    public ContactCircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize drawing objects.
     */
    private void init() {
        path = new Path();

        imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        imagePaint.setFilterBitmap(true);
        imagePaint.setDither(true);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(borderWidth);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeWidth(10f);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(backgroundColor);

        statusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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
        canvas.drawOval(rect, backgroundPaint);
        canvas.save();
        canvas.clipPath(path);
        super.onDraw(canvas);
        canvas.restore();
        canvas.drawOval(rect, borderPaint);

        if (currentProgress > 0 && currentProgress <= 100) {
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            float radius = Math.min(centerX, centerY) - borderWidth;
            float sweepAngle = (currentProgress / 100f) * 360f;
            canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, -90, sweepAngle, false, progressPaint);
        }

        float smallCircleRadius = getWidth() / 10f;
        float smallCircleX = getWidth() - smallCircleRadius - borderWidth;
        float smallCircleY = getHeight() - smallCircleRadius - borderWidth;
        canvas.drawCircle(smallCircleX, smallCircleY, smallCircleRadius, statusPaint);
    }

    /**
     * Sets the current progress as a percentage.
     * @param progress value from 0 to 100.
     */
    public void setProgress(float progress) {
        this.currentProgress = Math.max(0, Math.min(progress, 100));
        invalidate();
    }

    /**
     * Changes the color of the progress bar.
     * @param color new progress color.
     */
    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }

    /**
     * Changes the background color.
     * @param color new background color.
     */
    @Override
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        backgroundPaint.setColor(color);
        invalidate();
    }

    /**
     * Changes the color of the small status circle.
     * @param color new status color.
     */
    public void setStatusColor(int color) {
        this.statusColor = color;
        statusPaint.setColor(color);
        invalidate();
    }

    /**
     * Updates the status circle color according to the command.
     * @param command the command string ("10", "01", "A0111" or whatever).
     */
    public void updateStatusColor(String command) {
        switch (command) {
            case "10":
                setStatusColor(0xFF00FF00); // Green
                break;
            case "01":
                setStatusColor(0xFFFFFF00); // Yellow
                break;
            case "A0111":
                setStatusColor(0xFF888888); // Medium gray
                break;
            default:
                setStatusColor(0xFF000000); // Default black
                break;
        }
    }

    /**
     * Clears the progress (sets it to 0).
     */
    public void clearProgress() {
        this.currentProgress = 0;
        invalidate();
    }
}