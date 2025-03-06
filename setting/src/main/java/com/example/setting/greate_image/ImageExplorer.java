package com.example.setting.greate_image;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;

import com.example.setting.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

/**
 * Клас для роботи з вибором та обрізкою зображень у діалоговому вікні.
 */
public class ImageExplorer {
    private final Context context;
    private CustomCropView cropFrame;
    private Bitmap selectedBitmap;
    private ImageView imageProfile;
    private ImageView imageAccount;
    private Button btnCrop;
    private String senderKey;
    private float dX, dY;
    private ActivityResultLauncher<Intent> launcher;
    private ImgExplorer imgExplorer;

    /**
     * Конструктор класу ImageExplorer.
     * Ініціалізує контекст і ключ відправника, а також викликає діалог для вибору та обрізки зображення.
     *
     * @param context   Контекст, у якому працює діалог.
     * @param senderKey Ключ відправника.
     */
    public ImageExplorer(Context context, String senderKey) {
        this.context = context;
        this.senderKey = senderKey;
        this.imgExplorer = (ImgExplorer) context;
        showDialog();
    }

    boolean isResizing = false;

    /**
     * Відображення діалогового вікна для вибору та обрізки зображення.
     * Відкриває діалог, налаштовує макет, та прив'язує елементи інтерфейсу.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void showDialog() {
        Dialog dialog = new Dialog(context);
        View layout = ((Activity) context).getLayoutInflater().inflate(R.layout.dialog_image, null);
        dialog.setContentView(layout);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); // Встановлюємо повний екран
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Прозорий фон
        }

        dialog.show();

        imageProfile = layout.findViewById(R.id.imageProfile);
        imageAccount = layout.findViewById(R.id.imageAccount);
        btnCrop = layout.findViewById(R.id.btnCrop);
        cropFrame = layout.findViewById(R.id.cropFrame);

        imageAccount.setOnClickListener(v -> imgExplorer.openImagePicker());

        cropFrame.setOnTouchListener((v, event) -> {

            /*
             * event.getAction() визначає тип сенсорної події. Це може бути:
             * MotionEvent.ACTION_DOWN: Користувач торкнувся елемента.
             * MotionEvent.ACTION_MOVE: Користувач рухає пальцем.
             * MotionEvent.ACTION_UP: Користувач відпустив палець.
             * event.getX() та event.getY() дають координати дотику відносно поточного елемента (тобто, cropFrame).
             */

            int action = event.getAction(); // Отримуємо тип події: натискання, рух або відпускання
            float touchX = event.getX(); // Координати дотику відносно View
            float touchY = event.getY();

            int moveZoneSize = 40; // Розмір зони, що визначає межу для перетягування або зміни розміру

            /*
             * Якщо точка дотику знаходиться всередині cropFrame, то запускається режим переміщення.
             * В іншому випадку, коли точка дотику знаходиться на межі або за її межами, запускається режим зміни розміру.
             * dX та dY зберігають відстань між поточною позицією елемента та місцем дотику,
             * що дозволяє правильно обчислювати зміщення при переміщенні.
             * */
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (touchX > moveZoneSize && touchX < v.getWidth() - moveZoneSize &&
                            touchY > moveZoneSize && touchY < v.getHeight() - moveZoneSize) {
                        isResizing = false; // Режим переміщення
                        dX = v.getX() - event.getRawX(); // Зберігаємо відстань для переміщення
                        dY = v.getY() - event.getRawY(); // Аналогічно по осі Y
                    } else {
                        isResizing = true; // Режим зміни розміру
                        dX = event.getRawX(); // Зберігаємо початкові координати для зміни розміру
                        dY = event.getRawY();
                    }
                    break;

                /*
                 * Якщо активовано зміну розміру, обчислюється різниця між поточними координатами дотику та попередніми (deltaX та deltaY).
                 * Для того, щоб розміри змінювались однаково по обох осях, використовується scaleFactor = Math.max(deltaX, deltaY).
                 * Обчислюються нові розміри елемента і застосовуються до cropFrame через setLayoutParams().
                 * Якщо активовано режим переміщення, то елемент рухається без зміни розміру, використовуючи анімацію для плавного переміщення.
                 * */
                case MotionEvent.ACTION_MOVE:
                    if (isResizing) {
                        float deltaX = event.getRawX() - dX; // Різниця між новими та старими координатами
                        float deltaY = event.getRawY() - dY;

                        float scaleFactor = Math.max(deltaX, deltaY); // Вибір найбільшої зміни для пропорційного зміщення

                        ViewGroup.LayoutParams params = v.getLayoutParams(); // Отримуємо поточні параметри розміру

                        // Змінюємо розміри
                        params.width += scaleFactor;
                        params.height += scaleFactor;

                        // Мінімальні обмеження для розміру
                        params.width = Math.max(params.width, moveZoneSize * 2);
                        params.height = Math.max(params.height, moveZoneSize * 2);

                        v.setLayoutParams(params); // Застосовуємо нові розміри

                        // Оновлюємо збережені координати для наступного руху
                        dX = event.getRawX();
                        dY = event.getRawY();
                    } else {
                        // Логіка переміщення (для переміщення без зміни розміру)
                        v.animate()
                                .x(event.getRawX() + dX) // Переміщаємо по осі X
                                .y(event.getRawY() + dY) // Переміщаємо по осі Y
                                .setDuration(0) // Швидке переміщення без анімації
                                .start();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    /*
                     * В цьому блоці можна було б реалізувати додаткову логіку після того, як користувач відпустив палець
                     */
                    break;
            }
            return true;
        });

        File externalDir = new File(context.getExternalFilesDir(null), "imageProfile");
        if (!externalDir.exists()) {
            boolean mkdirs = externalDir.mkdirs();
        }
        // Обрізання вибраної області
        btnCrop.setOnClickListener(v -> {
            if (selectedBitmap != null) {
                Bitmap croppedBitmap = cropImage();
                Log.e("MainActivity", "Base64: ");

                if (croppedBitmap != null) {
                    Bitmap scaledBitmap = resizeBitmap(selectedBitmap, 2.0f);
                    String imageOrgName = new BitmapToFile().saveBitmapToFile(scaledBitmap, UUID.randomUUID().toString(),externalDir);//compressImageToBase64(selectedBitmap);
                    String imageName =new BitmapToFile().saveBitmapToFile(croppedBitmap, UUID.randomUUID().toString(),externalDir); //resizeAndCompressImage(croppedBitmap, 200, 200); // конвертуємо в Base64
                    setImage(resizeAndCompressImage(croppedBitmap, 200, 200));
                    imgExplorer.setImageAccount(imageOrgName,imageName);
                }
            }
        });
    }

    public Bitmap resizeBitmap(Bitmap originalBitmap, float scaleFactor) {
        // Оригінальні розміри
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        // Розрахунок нових розмірів
        int targetWidth = (int) (originalWidth / scaleFactor);
        int targetHeight = (int) (originalHeight / scaleFactor);

        // Створення масштабованого Bitmap
        return Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true);
    }


    /**
     * Встановлює зображення у компонент ImageView.
     *
     * @param base64String Строка в форматі Base64 для відображення зображення.
     */
    private void setImage(String base64String) {
        Bitmap bitmap = decodeBase64ToBitmap(base64String);
        imageAccount.setImageBitmap(bitmap);
    }

    /**
     * Обрізає зображення відповідно до координат рамки.
     *
     * @return Вирізане зображення в форматі Bitmap.
     */
    private Bitmap cropImage() {
        /*
         * Перевіряємо, чи є вибране зображення
         * Якщо немає вибраного зображення (selectedBitmap == null), функція повертає null, що означає, що нічого обрізати.
         */
        if (selectedBitmap == null) return null;

        /*
         * Отримуємо координати рамки на екрані
         * Використовуємо getLocationOnScreen() для отримання координат cropFrame на екрані, щоб точно визначити її положення та розміри.
         */
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

        /*
         * Перевіряємо, чи рамка знаходиться над зображенням
         * Аналогічно отримуємо координати для imageProfile, щоб дізнатися розташування зображення на екрані.
         * Якщо координати рамки знаходяться поза зображенням, функція повертає null — не можна вирізати нічого за межами зображення.
         */
        if (frameX < imageX || frameY < imageY) {
            System.out.println("Frame is outside the image bounds.");
            return null;
        }

        /*
         * Обчислюємо масштабні коефіцієнти для осі X та Y, щоб правильно перевести координати зображення до Bitmap.
         * Потім перераховуємо координати та розміри для вирізання частини зображення відповідно до масштабованих значень.
         */

        // Обчислюємо пропорції для перетворення координат у Bitmap
        float scaleX = (float) selectedBitmap.getWidth() / imageProfile.getWidth();
        float scaleY = (float) selectedBitmap.getHeight() / imageProfile.getHeight();

        // Масштабовані координати для обрізки
        int cropX = (int) ((frameX - imageX) * scaleX);
        int cropY = (int) ((frameY - imageY) * scaleY);
        int cropWidth = (int) (frameWidth * scaleX);
        int cropHeight = (int) (frameHeight * scaleY);

        /*
         * Перевіряємо, чи координати не виходять за межі Bitmap
         * Перевіряється, чи не виходять координати та розміри за межі зображення. Якщо так, функція повертає null.
         */
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

    /**
     * Масштабує зображення до заданих максимальних розмірів, зберігаючи співвідношення сторін.
     *
     * @param originalBitmap Оригінальне зображення.
     * @param maxWidth       Максимальна ширина.
     * @param maxHeight      Максимальна висота.
     * @return Масштабоване зображення.
     */
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
        return Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, false);
    }

    /**
     * Стискає зображення до формату Base64.
     *
     * @param originalBitmap Оригінальне зображення.
     * @return Строка в форматі Base64.
     */
    private String compressImageToBase64(Bitmap originalBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream); // Якість 80% можна коригувати
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT); // Перетворення в Base64
    }

    /**
     * Масштабує зображення і стискає його до формату Base64.
     *
     * @param originalBitmap Оригінальне зображення.
     * @param maxWidth       Максимальна ширина.
     * @param maxHeight      Максимальна висота.
     * @return Стиснуте зображення у форматі Base64.
     */
    private String resizeAndCompressImage(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        Bitmap resizedBitmap = resizeImage(originalBitmap, maxWidth, maxHeight);
        return compressImageToBase64(resizedBitmap);
    }


    public Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void setImageBitmap(Bitmap selectedBitmap) {
        this.selectedBitmap = selectedBitmap;
        imageProfile.setImageBitmap(selectedBitmap);
    }

    public interface ImgExplorer {
        void openImagePicker();

        void setImageAccount(String base64StringOrg,String base64String);
    }
}
