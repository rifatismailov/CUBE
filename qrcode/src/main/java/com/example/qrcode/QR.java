package com.example.qrcode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;

import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Objects;

/**
 * Клас QR відповідає за генерацію та сканування QR-кодів.
 */
public class QR {
    private Context context; // Контекст додатка
    private Activity activity; // Активність для роботи з UI
    private String jsonData; // Дані, які будуть закодовані в QR-код
    private String accountImageUrl; // URL або шлях до зображення акаунта
    private ActivityResultLauncher<ScanOptions> barLauncher; // Лаунчер для сканування QR-коду

    /**
     * Конструктор для ініціалізації QR-коду без даних.
     * @param context Контекст додатка
     */
    public QR(Context context) {
        this.context = context;
        activity = (Activity) context;
    }

    /**
     * Конструктор для ініціалізації QR-коду з JSON-даними та зображенням.
     * @param context Контекст додатка
     * @param jsonData JSON-дані для QR-коду
     * @param accountImageUrl URL або шлях до зображення акаунта
     */
    public QR(Context context, String jsonData, String accountImageUrl) {
        this.context = context;
        activity = (Activity) context;
        this.jsonData = jsonData;
        this.accountImageUrl = accountImageUrl;
    }

    /**
     * Конструктор для ініціалізації QR-сканера.
     * @param barLauncher Лаунчер для сканування QR-коду
     */
    public QR(ActivityResultLauncher<ScanOptions> barLauncher) {
        this.barLauncher = barLauncher;
        scan();
    }

    /**
     * Відображає діалогове вікно з QR-кодом.
     */
    public void show() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        View linearlayout = activity.getLayoutInflater().inflate(R.layout.dialog_qrcode, null);
        dialog.setView(linearlayout);
        // Діалогове вікно для відображення QR-коду
        AlertDialog alertDialog = dialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.create();
        // Поле для відображення QR-коду
        ImageView qrCode = linearlayout.findViewById(R.id.qrCode); // Пошук ImageView для відображення QR-коду

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2; // Зменшення розміру зображення у два рази
        Bitmap bitmap = BitmapFactory.decodeFile(accountImageUrl, options);

        qrCode.setImageBitmap(QRCode.getQRCode(jsonData, bitmap)); // Генерація та встановлення QR-коду
        dialog.create();
    }

    /**
     * Запускає сканування QR-коду.
     */
    private void scan() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on"); // Підказка для користувача
        options.setBeepEnabled(true); // Включити звук при скануванні
        options.setOrientationLocked(true); // Заблокувати орієнтацію екрану
        options.setCaptureActivity(CaptureAct.class); // Встановити активність для сканування
        barLauncher.launch(options); // Запуск сканування
    }
}
