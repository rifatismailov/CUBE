package com.example.cube.draw;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.Nullable;

import com.example.cube.R;
import com.google.android.material.imageview.ShapeableImageView;

/**
 * LoadingImageView is a special image display widget
 * with the ability to show loading progress and a cancel button.
 *
 * It inherits from ShapeableImageView and adds the following functionality:
 * - Display a progress indicator (ring animation)
 * - Display a cancel button (cross) when progress is incomplete
 * - Handle cancel button click events
 */
public class LoadingImageView extends ShapeableImageView {

    private Paint progressPaint, crossPaint; // Objects for drawing progress and cancel buttons
    private RectF rectF; // Bounding rectangle for drawing progress
    private int progress = 0; // Current progress level (0-100)
    private OnCancelListener cancelListener; // Cancel button click listener
    private boolean showCancelButton = false; // Flag to specify whether to show the cancel button

    /**
     * Constructor to initialize the component in code.
     */
    public LoadingImageView(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * Constructor to initialize the component via XML.
     */
    public LoadingImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Constructor to initialize the component via XML with style parameters.
     */
    public LoadingImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Method for initializing drawing objects.
     */
    private void init(Context context, AttributeSet attrs) {
        // Check for attributes in XML
        if (attrs != null) {
            // Get a TypedArray to access the attributes
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.LoadingImageView,
                    0, 0);

            try {
                // Read the attribute values from XML, if they exist, or use the default values
                int progressColor = typedArray.getColor(R.styleable.LoadingImageView_progressColor, Color.BLUE);
                float progressThickness = typedArray.getDimension(R.styleable.LoadingImageView_progressThickness, 10);
                int crossColor = typedArray.getColor(R.styleable.LoadingImageView_crossColor, Color.RED);
                float crossThickness = typedArray.getDimension(R.styleable.LoadingImageView_crossThickness, 8);

                // Setting the brush for the progress bar
                progressPaint = new Paint();
                progressPaint.setColor(progressColor); // Setting the color of the progress bar
                progressPaint.setStyle(Paint.Style.STROKE); // Drawing style - outline only
                progressPaint.setStrokeWidth(progressThickness); // Progress bar line thickness
                progressPaint.setAntiAlias(true); // Enabling anti-aliasing for better line appearance
                progressPaint.setStrokeCap(Paint.Cap.ROUND); // Round line ends for smoother transitions

                // Setting the brush for the cross (undo button)
                crossPaint = new Paint();
                crossPaint.setColor(crossColor); // Setting the color of the cross
                crossPaint.setStyle(Paint.Style.STROKE); // Drawing style - only outline
                crossPaint.setStrokeWidth(crossThickness); // Cross line thickness
                crossPaint.setAntiAlias(true); // Enable anti-aliasing for better appearance

            } finally {
                // Free resources associated with TypedArray
                typedArray.recycle();
            }
        }

        // Initialize rectangle for progress
        rectF = new RectF(); // Create a new RectF object that will be used to draw the progress bar
    }

    /**
     * Set the progress level (0-100).
     * @param progress progress value
     */
    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(progress, 100));
        showCancelButton = (progress > 0 && progress < 100);

        if (progress == 100) {
            progressPaint.setAlpha(0); // Hide the indicator
            showCancelButton = false;
        } else {
            progressPaint.setAlpha(255);
        }
        invalidate(); // Invoke repaint
    }

    /**
     * Set a listener for the cancel button.
     * @param listener an object that implements OnCancelListener
     */
    public void setOnCancelListener(OnCancelListener listener) {
        this.cancelListener = listener;
    }

    /**
     * Draws a component on the Canvas.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (progress > 0 && progress < 100) {
            int width = getWidth();
            int height = getHeight();
            int radius = Math.min(width, height) / 4;

            rectF.set(
                    width / 2f - radius,
                    height / 2f - radius,
                    width / 2f + radius,
                    height / 2f + radius
            );

            float sweepAngle = (progress / 100f) * 360;
            canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);

            if (showCancelButton) {
                drawCancelButton(canvas, width, height);
            }
        }
    }

    /**
     * Draws the cross for the cancel button.
     */
    private void drawCancelButton(Canvas canvas, int width, int height) {
        int size = Math.min(width, height) / 6;
        int cx = width / 2;
        int cy = height / 2;

        canvas.drawLine(cx - size, cy - size, cx + size, cy + size, crossPaint);
        canvas.drawLine(cx + size, cy - size, cx - size, cy + size, crossPaint);
    }

    /**
     * Handles user touches.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (showCancelButton && event.getAction() == MotionEvent.ACTION_DOWN) {
            int width = getWidth();
            int height = getHeight();
            int size = Math.min(width, height) / 6;
            int cx = width / 2;
            int cy = height / 2;

            // Check if the cross was clicked
            if (event.getX() > cx - size && event.getX() < cx + size &&
                    event.getY() > cy - size && event.getY() < cy + size) {

                if (cancelListener != null) {
                    cancelListener.onCancel();
                }
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * Interface for handling the cancel button click.
     */
    public interface OnCancelListener {
        void onCancel();
    }
}