package com.example.image_account;

import static android.app.Activity.RESULT_OK;
import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;
import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;

import java.io.ByteArrayOutputStream;
import java.util.Objects;


public class ImageExplorer {
    private final Context context;

    private View cropFrame;
    private Bitmap selectedBitmap;
    private AlertDialog alertDialog;
    private ImageView imageProfile;
    private ImageView imageAccount;
    private Button btnCrop;
    String senderKey;
    private float dX, dY;
    private ActivityResultLauncher<Intent> launcher;
    private ImgExplorer imgExplorer;
    /**
     * Конструктор класу Open.
     *
     * @param context   Контекст, у якому працює діалог.
     * @param senderKey
     */
    public ImageExplorer(Context context, String senderKey) {
        this.context = context;
        this.senderKey = senderKey;
        this.imgExplorer=(ImgExplorer)context;
        showDialog();
    }

    /**
     * Відображення діалогового вікна.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void showDialog() {
        // Створюємо новий діалог
        Dialog dialog = new Dialog(context);
        View layout = ((Activity) context).getLayoutInflater().inflate(R.layout.dialog_image, null);
        dialog.setContentView(layout);

        // Налаштовуємо розмір діалогового вікна на весь екран
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); // Встановлюємо повний екран
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Прозорий фон
        }

        dialog.show();

        // Прив'язуємо елементи з макету
        imageProfile = layout.findViewById(R.id.imageProfile);
        imageAccount = layout.findViewById(R.id.imageAccount);
        btnCrop = layout.findViewById(R.id.btnCrop);
        cropFrame = layout.findViewById(R.id.cropFrame);

        imageProfile.setOnClickListener(v -> imgExplorer.openImagePicker());

        // Дозвіл переміщувати рамку
        cropFrame.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    v.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                    break;
            }
            return true;
        });

        // Обрізання вибраної області
        btnCrop.setOnClickListener(v -> {
            if (selectedBitmap != null) {
                Bitmap croppedBitmap = cropImage();
                Log.e("MainActivity", "Base64: ");

                if (croppedBitmap != null) {
                    String base64String = resizeAndCompressImage(croppedBitmap, 300, 300); //convertToBase64(croppedBitmap);
                    Log.e("MainActivity", "Base64: " + base64String);
                    setImage(base64String);
                }
            }
        });
    }



    /**
     * Закриття діалогового вікна та оновлення інформації у батьківському компоненті.
     */

    public void onFinish(String encFile) {
        alertDialog.cancel();
    }





    private void setImage(String base64String) {
        Bitmap bitmap = decodeBase64ToBitmap(base64String);
        imageAccount.setImageBitmap(bitmap);
    }

    // Обрізання зображення за рамкою
    // Обрізання зображення за рамкою
    private Bitmap cropImage() {
        if (selectedBitmap == null) return null;

        // Отримуємо координати рамки на екрані
        int[] frameLocation = new int[2];
        cropFrame.getLocationOnScreen(frameLocation);

        int frameX = frameLocation[0];
        int frameY = frameLocation[1];

        int frameWidth = cropFrame.getWidth();
        int frameHeight = cropFrame.getHeight();

        // Отримуємо координати ImageView на екрані
        int[] imageLocation = new int[2];
        imageProfile.getLocationOnScreen(imageLocation);

        int imageX = imageLocation[0];
        int imageY = imageLocation[1];

        // Перевіряємо, чи рамка знаходиться над зображенням
        if (frameX < imageX || frameY < imageY) {
            System.out.println("Frame is outside the image bounds.");
            return null;
        }

        // Обчислюємо пропорції для перетворення координат у Bitmap
        float scaleX = (float) selectedBitmap.getWidth() / imageProfile.getWidth();
        float scaleY = (float) selectedBitmap.getHeight() / imageProfile.getHeight();

        // Масштабовані координати для обрізки
        int cropX = (int) ((frameX - imageX) * scaleX);
        int cropY = (int) ((frameY - imageY) * scaleY);
        int cropWidth = (int) (frameWidth * scaleX);
        int cropHeight = (int) (frameHeight * scaleY);

        // Перевіряємо, чи координати не виходять за межі Bitmap
        if (cropX < 0 || cropY < 0 || cropX + cropWidth > selectedBitmap.getWidth() || cropY + cropHeight > selectedBitmap.getHeight()) {
            System.out.println("Crop area is out of bounds.");
            return null;
        }

        // Якщо вирізати занадто вузько, збільшуємо рамку
        if (cropWidth < cropHeight) {
            int newSize = cropHeight;  // робимо ширину рівною висоті
            int offsetX = (cropWidth - newSize) / 2;
            cropX += offsetX;
            cropWidth = newSize;
        } else if (cropHeight < cropWidth) {
            int newSize = cropWidth;  // робимо висоту рівною ширині
            int offsetY = (cropHeight - newSize) / 2;
            cropY += offsetY;
            cropHeight = newSize;
        }

        // Обрізаємо зображення
        try {
            return Bitmap.createBitmap(selectedBitmap, cropX, cropY, cropWidth, cropHeight);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Перетворення Bitmap у Base64
    private String convertToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap resizeImage(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        // Обчислюємо пропорції для збереження співвідношення сторін
        float ratioBitmap = (float) width / height;
        float ratioMax = (float) maxWidth / maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) (maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) (maxWidth / ratioBitmap);
        }

        // Масштабуємо зображення
        return Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, false);
    }

    private String compressImageToBase64(Bitmap originalBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Стиснення до формату JPEG
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream); // Якість 80% можна коригувати
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT); // Перетворення в Base64
    }

    private String resizeAndCompressImage(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        // Змінюємо розмір зображення
        Bitmap resizedBitmap = resizeImage(originalBitmap, maxWidth, maxHeight);

        // Перетворюємо зображення в Base64 після стиснення
        return compressImageToBase64(resizedBitmap);
    }

    public Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void setImageBitmap(Bitmap selectedBitmap) {
        this.selectedBitmap=selectedBitmap;
        imageProfile.setImageBitmap(selectedBitmap);

    }
    public interface ImgExplorer{
        void openImagePicker();
    }
}
