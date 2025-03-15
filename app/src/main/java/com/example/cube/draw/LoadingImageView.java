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
 * LoadingImageView - це спеціальний віджет для відображення зображення
 * з можливістю показу прогресу завантаження та кнопки скасування.
 *
 * Він успадковується від ShapeableImageView і додає функціональність:
 * - Відображення індикатора прогресу (кільцева анімація)
 * - Відображення кнопки скасування (хрестик), коли прогрес неповний
 * - Обробка подій натискання на кнопку скасування
 */
public class LoadingImageView extends ShapeableImageView {

    private Paint progressPaint, crossPaint; // Об'єкти для малювання прогресу та кнопки скасування
    private RectF rectF; // Обмежуючий прямокутник для малювання прогресу
    private int progress = 0; // Поточний рівень прогресу (0-100)
    private OnCancelListener cancelListener; // Слухач натискання на кнопку скасування
    private boolean showCancelButton = false; // Прапорець для визначення, чи потрібно показувати кнопку скасування

    /**
     * Конструктор для ініціалізації компонента в коді.
     */
    public LoadingImageView(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * Конструктор для ініціалізації компонента через XML.
     */
    public LoadingImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Конструктор для ініціалізації компонента через XML з параметрами стилю.
     */
    public LoadingImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Метод ініціалізації об'єктів малювання.
     */
    private void init(Context context, AttributeSet attrs) {
        // Перевірка на наявність атрибутів в XML
        if (attrs != null) {
            // Отримуємо TypedArray для доступу до атрибутів
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.LoadingImageView,
                    0, 0);

            try {
                // Зчитуємо значення атрибутів з XML, якщо вони є, або використовуємо значення за замовчуванням
                int progressColor = typedArray.getColor(R.styleable.LoadingImageView_progressColor, Color.BLUE);
                float progressThickness = typedArray.getDimension(R.styleable.LoadingImageView_progressThickness, 10);
                int crossColor = typedArray.getColor(R.styleable.LoadingImageView_crossColor, Color.RED);
                float crossThickness = typedArray.getDimension(R.styleable.LoadingImageView_crossThickness, 8);

                // Налаштування пензля для прогрес-бару
                progressPaint = new Paint();
                progressPaint.setColor(progressColor); // Встановлюємо колір прогрес-бару
                progressPaint.setStyle(Paint.Style.STROKE); // Стиль малювання - лише контур
                progressPaint.setStrokeWidth(progressThickness); // Товщина лінії прогрес-бару
                progressPaint.setAntiAlias(true); // Включаємо згладжування для кращого вигляду ліній
                progressPaint.setStrokeCap(Paint.Cap.ROUND); // Круглі кінці лінії для м'якших переходів

                // Налаштування пензля для хрестика (кнопка скасування)
                crossPaint = new Paint();
                crossPaint.setColor(crossColor); // Встановлюємо колір хрестика
                crossPaint.setStyle(Paint.Style.STROKE); // Стиль малювання - лише контур
                crossPaint.setStrokeWidth(crossThickness); // Товщина лінії хрестика
                crossPaint.setAntiAlias(true); // Включаємо згладжування ліній для кращого вигляду

            } finally {
                // Вивільняємо ресурси, пов'язані з TypedArray
                typedArray.recycle();
            }
        }

        // Ініціалізація прямокутника для прогресу
        rectF = new RectF(); // Створюємо новий об'єкт RectF, який буде використовуватися для малювання прогрес-бару
    }

    /**
     * Встановлення рівня прогресу (0-100).
     * @param progress значення прогресу
     */
    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(progress, 100));
        showCancelButton = (progress > 0 && progress < 100);

        if (progress == 100) {
            progressPaint.setAlpha(0); // Приховуємо індикатор
            showCancelButton = false;
        } else {
            progressPaint.setAlpha(255);
        }
        invalidate(); // Викликаємо перерисовку
    }

    /**
     * Встановлення слухача на кнопку скасування.
     * @param listener об'єкт, що реалізує OnCancelListener
     */
    public void setOnCancelListener(OnCancelListener listener) {
        this.cancelListener = listener;
    }

    /**
     * Малює компонент на Canvas.
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
     * Малює хрестик кнопки скасування.
     */
    private void drawCancelButton(Canvas canvas, int width, int height) {
        int size = Math.min(width, height) / 6;
        int cx = width / 2;
        int cy = height / 2;

        canvas.drawLine(cx - size, cy - size, cx + size, cy + size, crossPaint);
        canvas.drawLine(cx + size, cy - size, cx - size, cy + size, crossPaint);
    }

    /**
     * Обробляє дотики користувача.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (showCancelButton && event.getAction() == MotionEvent.ACTION_DOWN) {
            int width = getWidth();
            int height = getHeight();
            int size = Math.min(width, height) / 6;
            int cx = width / 2;
            int cy = height / 2;

            // Перевіряємо, чи натиснуто на хрестик
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
     * Інтерфейс для обробки натискання на кнопку скасування.
     */
    public interface OnCancelListener {
        void onCancel();
    }
}
