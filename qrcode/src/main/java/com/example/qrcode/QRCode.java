package com.example.qrcode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.util.EnumMap;
import java.util.Map;

/**
 * QRCode class provides methods to generate QR codes with optional text overlays.
 */
public class QRCode {

    /**
     * Generates a QR code bitmap with a solid blue background using Canvas.
     *
     * @param id The data to encode in the QR code.
     * @return A bitmap representing the QR code.
     */
    public static Bitmap getQRCode(String id) {
        int size = 1000; // Розмір QR-коду

        try {
            // Налаштування для генерації QR-коду
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1); // Зменшення полів QR-коду

            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(id, BarcodeFormat.QR_CODE, size, size, hints);

            // Створюємо бітмап для результату
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            Paint paintBackground = new Paint();
            paintBackground.setColor(Color.parseColor("#89c9e1")); // Синій фон
            canvas.drawRect(0, 0, size, size, paintBackground);

            Paint paintQR = new Paint();
            paintQR.setColor(Color.BLACK); // Чорні квадрати QR-коду

            // Малювання чорних пікселів з BitMatrix
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if (bitMatrix.get(x, y)) {
                        canvas.drawPoint(x, y, paintQR);
                    }
                }
            }

            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Generates a QR code with a text overlay displayed inside a circle in the center.
     *
     * @param qrCodeData The data to encode in the QR code.
     * @param initials   The text to display in the center of the QR code.
     * @return A bitmap representing the QR code with a centered text overlay.
     */
    public static Bitmap getQRCode(String qrCodeData, String initials) {
        int qrCodeSize = 1000;  // QR code size in pixels
        int overlaySize = 300;  // Size of the text overlay

        try {
            // QR code generation settings with error correction
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // High error correction level

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(qrCodeData, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap qrCodeBitmap = barcodeEncoder.createBitmap(bitMatrix);

            // Create a new bitmap to draw QR code and overlay
            Bitmap finalBitmap = Bitmap.createBitmap(qrCodeBitmap.getWidth(), qrCodeBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(finalBitmap);

            // Draw QR code
            canvas.drawBitmap(qrCodeBitmap, 0, 0, null);

            // Draw a black rectangle at the center
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            float left = (qrCodeSize - overlaySize) / 2f;
            float top = (qrCodeSize - overlaySize) / 2f;
            float right = left + overlaySize;
            float bottom = top + overlaySize;
            canvas.drawRect(left, top, right, bottom, paint);

            // Create and draw the bitmap with text and rounded background
            Bitmap textBitmap = createTextBitmap(initials, overlaySize, overlaySize);

            // Position to draw the text overlay
            float xLogo = (qrCodeSize - overlaySize) / 2f;
            float yLogo = (qrCodeSize - overlaySize) / 2f;

            canvas.drawBitmap(textBitmap, xLogo, yLogo, null);

            return finalBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Generates a QR code with a text overlay displayed inside a circle in the center.
     *
     * @param qrCodeData The data to encode in the QR code.
     * @param image
     * @return A bitmap representing the QR code with a centered text overlay.
     */
    public static Bitmap getQRCode(String qrCodeData, Bitmap image) {
        int qrCodeSize = 1000;  // QR code size in pixels
        int overlaySize = 300;  // Size of the text overlay

        try {
            // QR code generation settings with error correction
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // High error correction level

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(qrCodeData, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap qrCodeBitmap = barcodeEncoder.createBitmap(bitMatrix);

            // Create a new bitmap to draw QR code and overlay
            Bitmap finalBitmap = Bitmap.createBitmap(qrCodeBitmap.getWidth(), qrCodeBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(finalBitmap);

            // Draw QR code
            canvas.drawBitmap(qrCodeBitmap, 0, 0, null);

            // Draw a black rectangle at the center
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            float left = (qrCodeSize - overlaySize) / 2f;
            float top = (qrCodeSize - overlaySize) / 2f;
            float right = left + overlaySize;
            float bottom = top + overlaySize;
            canvas.drawRect(left, top, right, bottom, paint);

            // Create and draw the bitmap with text and rounded background
            Bitmap textBitmap = createBitmap(image, overlaySize, overlaySize);

            // Position to draw the text overlay
            float xLogo = (qrCodeSize - overlaySize) / 2f;
            float yLogo = (qrCodeSize - overlaySize) / 2f;

            canvas.drawBitmap(textBitmap, xLogo, yLogo, null);

            return finalBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Creates a bitmap containing text inside a blue circular background.
     *
     * @param text   The text to display.
     * @param width  Width of the bitmap.
     * @param height Height of the bitmap.
     * @return A bitmap with text inside a circular background.
     */
    private static Bitmap createTextBitmap(String text, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        int blueColor = Color.parseColor("#89c9e1"); // Light blue color

        // Draw blue circular background
        paint.setColor(blueColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(width / 2f, height / 2f, Math.min(width, height) / 2f, paint);

        // Set up text paint properties
        paint.setColor(Color.WHITE); // White text
        paint.setTextSize(200); // Text size
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        // Draw text centered within the circle
        float xPos = width / 2f;
        float yPos = height / 2f - ((paint.descent() + paint.ascent()) / 2f);
        canvas.drawText(text, xPos, yPos, paint);

        return bitmap;
    }

    /**
     * Resizes the given bitmap to the specified width and height, cropping it to a circular shape.
     *
     * @param image  The original bitmap to be resized.
     * @param width  The desired width of the output bitmap.
     * @param height The desired height of the output bitmap.
     * @return A resized circular bitmap with the specified dimensions.
     */
    private static Bitmap createBitmap(Bitmap image, int width, int height) {
        if (image == null || width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid image or dimensions.");
        }

        // Масштабування зображення до необхідних розмірів
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(image, width, height, true);

        // Створення круглого бітмапу
        Bitmap circularBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);

        // Встановлення Paint з антиаліасінгом
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        // Малювання круглої області
        float radius = Math.min(width, height) / 2f;
        canvas.drawCircle(width / 2f, height / 2f, radius, paint);

        // Використання PorterDuff для обрізки зображення у круг
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(resizedBitmap, 0, 0, paint);

        return circularBitmap;
    }


}
