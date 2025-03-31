package com.example.setting.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.qrcode.QRCode;
import com.example.setting.R;
import com.example.setting.UserSetting;

import java.io.File;

/**
 * Фрагмент, який відображає QR-код з можливістю використання зображення акаунта та генерування QR-коду.
 * Якщо зображення акаунта не знайдено, буде створене чорне зображення.
 */
public class ImageQRFragment extends Fragment {

    private final ChangeFragment changeFragment;
    private final UserSetting userSetting;
    private final File accountImage;

    /**
     * Конструктор фрагмента, ініціалізує фрагмент з необхідними параметрами.
     *
     * @param changeFragment Інтерфейс для зміни фрагментів.
     * @param userSetting Об'єкт налаштувань користувача.
     * @param accountImage Файл зображення акаунта.
     */
    public ImageQRFragment(ChangeFragment changeFragment, UserSetting userSetting, File accountImage) {
        this.changeFragment = changeFragment;
        this.userSetting = userSetting;
        this.accountImage = accountImage;
    }

    /**
     * Створює та повертає вигляд фрагмента.
     *
     * @param inflater LayoutInflater для інфлюсації макету.
     * @param container Контейнер для фрагмента.
     * @param savedInstanceState Стан фрагмента.
     * @return Повертає корінний вигляд фрагмента.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Підключення макета з XML
        return inflater.inflate(R.layout.fragment_qrcode, container, false);
    }

    /**
     * Обробляє вигляд фрагмента після його створення, встановлюючи зображення QR-коду.
     *
     * @param view Вигляд фрагмента.
     * @param savedInstanceState Стан фрагмента.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imageView = view.findViewById(R.id.qrCodeImage);
        imageView.setImageResource(R.color.blue);

        // Створення JSON з даними користувача
        String jsonData = new UserSetting.Builder()
                .setId(userSetting.getId())
                .setName(userSetting.getName())
                .setLastName(userSetting.getLastName())
                .build().toJson("userId", "name", "lastName").toString();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2; // Зменшити розмір у два рази

        File file = new File(accountImage.toString());
        Bitmap bitmap;

        // Якщо файл не існує, створюємо чорний bitmap
        if (!file.isFile()) {
            bitmap = createBlackBitmap(100, 100);
        } else {
            // Інакше, декодуємо зображення з файлу
            bitmap = BitmapFactory.decodeFile(accountImage.toString(), options);
        }

        // Встановлюємо зображення QR-коду з даними користувача та зображення акаунта
        imageView.setImageBitmap(QRCode.getQRCode(jsonData, bitmap));

        // Додаємо обробник кліку для зміни фрагмента
        imageView.setOnClickListener(view1 -> changeFragment.changeFragment());
    }

    /**
     * Інтерфейс для зміни фрагментів.
     */
    public interface ChangeFragment {
        void changeFragment();
    }

    /**
     * Створює чорне зображення типу Bitmap.
     *
     * @param width Ширина зображення.
     * @param height Висота зображення.
     * @return Повертає Bitmap чорного кольору.
     */
    public Bitmap createBlackBitmap(int width, int height) {
        // Створюємо новий Bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Створюємо Canvas для малювання на Bitmap
        Canvas canvas = new Canvas(bitmap);

        // Створюємо Paint для малювання чорним кольором
        Paint paint = new Paint();
        paint.setColor(Color.BLACK); // Колір чорний

        // Малюємо чорний прямокутник, який покриває всю площу Bitmap
        canvas.drawRect(0, 0, width, height, paint);

        return bitmap;
    }
}
