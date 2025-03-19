package com.example.cube.navigation;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.cube.R;
import com.example.cube.draw.ContactCircularImageView;

import java.io.File;

/**
 * Клас NavigationManager відповідає за керування навігацією у додатку.
 * Він взаємодіє з елементами UI, такими як кнопки, зображення аватара,
 * а також обробляє події навігації між екранами.
 */
public class NavigationManager {

    private final Navigation navigation;
    private final ImageView avatarImage;
    private final ContactCircularImageView accountImage;
    private final Button accountButton;
    private final Button settingsButton;
    private final Button logoutButton;
    private final File externalDir;

    /**
     * Конструктор ініціалізує менеджер навігації та прив'язує UI-елементи.
     *
     * @param activity Активність, яка повинна реалізовувати Navigation.
     */
    public NavigationManager(Activity activity) {
        if (!(activity instanceof Navigation)) {
            // Перевірка типу activity у конструкторі.
            Log.e("NavigationManager", "Activity must implement Navigation interface");
        }

        // Створення директорії для збереження зображень профілю.
        externalDir = new File(activity.getExternalFilesDir(null), "imageProfile");
        if (!externalDir.exists()) {
            externalDir.mkdirs();
        }

        this.navigation = (Navigation) activity;
        this.avatarImage = activity.findViewById(R.id.avatarImage);
        this.accountImage = activity.findViewById(R.id.accountImage);
        this.accountButton = activity.findViewById(R.id.nav_account);
        this.settingsButton = activity.findViewById(R.id.nav_settings);
        this.logoutButton = activity.findViewById(R.id.nav_logout);

        setupButtons();
    }

    /**
     * Метод встановлює обробники натискань для кнопок навігації.
     */
    private void setupButtons() {
        accountButton.setOnClickListener(v -> handleMenuItemClick(R.id.nav_account));
        settingsButton.setOnClickListener(v -> handleMenuItemClick(R.id.nav_settings));
        accountImage.setOnClickListener(v -> handleMenuItemClick(R.id.accountImage));
        logoutButton.setOnClickListener(v -> handleMenuItemClick(R.id.nav_logout));
    }

    /**
     * Обробник подій натискання на елементи навігації.
     *
     * @param itemId ID натиснутого елемента.
     */
    private void handleMenuItemClick(int itemId) {
        switch (itemId) {
            case R.id.nav_account:
                navigation.showAccount();
                break;
            case R.id.nav_settings:
                navigation.showSetting();
                break;
            case R.id.nav_logout:
                navigation.logout();
                break;
            case R.id.accountImage:
                navigation.imageNavigation();
                break;
        }
    }

    /**
     * Встановлює зображення аватара користувача.
     *
     * @param image Назва файлу зображення.
     */
    public void setAvatarImage(String image) {
        File file = new File(externalDir + "/" + image);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            avatarImage.setImageBitmap(bitmap);
        } else {
            avatarImage.setImageResource(R.color.blue); // Резервне зображення
        }
    }

    /**
     * Встановлює статус повідомлення (наприклад, підключення, помилка тощо).
     *
     * @param status Статус у вигляді рядка ("closed", "failed", "connected").
     */
    public void setNotification(String status) {
        if (status != null) {
            switch (status) {
                case "closed":
                    accountImage.updateStatusColor("00");
                    break;
                case "failed":
                    accountImage.updateStatusColor("01");
                    break;
                case "connected":
                    accountImage.updateStatusColor("10");
                    break;
            }
        }
    }

    /**
     * Встановлює зображення акаунта користувача.
     *
     * @param image Назва файлу зображення.
     */
    public void setAccountImage(String image) {
        File file = new File(externalDir + "/" + image);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            accountImage.setImageBitmap(bitmap);
        } else {
            accountImage.setImageResource(R.color.yellow); // Резервне зображення
        }
    }

    /**
     * Інтерфейс для визначення методів навігації між екранами.
     */
    public interface Navigation {
        void scannerQrAccount();

        void imageNavigation();

        void showSetting();

        void showAccount();

        void logout();
    }
}