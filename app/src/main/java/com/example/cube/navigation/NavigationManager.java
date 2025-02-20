package com.example.cube.navigation;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Button;
import android.widget.ImageView;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.cube.R;

import java.io.File;

public class NavigationManager {

    private Navigation navigation;
    private final ImageView avatarImage;
    private final ImageView accountImage;
    private final Button accountButton;
    private final Button settingsButton;
    private final Button logoutButton;
    private final File externalDir;

    public NavigationManager(Activity activity, DrawerLayout drawerLayout, ImageView avatarImage, ImageView accountImage, Button accountButton, Button settingsButton, Button logoutButton) {
        if (!(activity instanceof Navigation)) {
            //Перевірка типу activity у конструкторі: Якщо активність не імплементує Navigation, це може викликати ClassCastException.
            throw new IllegalArgumentException("Activity must implement Navigation interface");
        }
        this.navigation = (Navigation) activity;
        this.avatarImage = avatarImage;
        this.accountImage = accountImage;
        this.accountButton = accountButton;
        this.settingsButton = settingsButton;
        this.logoutButton = logoutButton;
        externalDir = new File(activity.getExternalFilesDir(null), "imageProfile");
        if (!externalDir.exists()) {
            boolean mkdirs = externalDir.mkdirs();
        }
        setupButtons();
    }

    private void setupButtons() {
        accountButton.setOnClickListener(v -> handleMenuItemClick(R.id.nav_account));
        settingsButton.setOnClickListener(v -> handleMenuItemClick(R.id.nav_settings));
        accountImage.setOnClickListener(v -> handleMenuItemClick(R.id.accountImage));
        logoutButton.setOnClickListener(v -> handleMenuItemClick(R.id.nav_logout));
    }

    private void handleMenuItemClick(int itemId) {
        switch (itemId) {
            case R.id.nav_account:
                // Логіка для "Акаунт"
                navigation.showAccount();
                break;
            case R.id.nav_settings:
                // Логіка для "Налаштування"
                navigation.showSetting();
                break;
            case R.id.nav_logout:
                navigation.logout();
                // Логіка для "Вихід"
                break;
            case R.id.accountImage:
                navigation.imageNavigation();
                break;
        }
        // Закрити Drawer після вибору
        // drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void setAvatarImage(String image) {
        File file = new File(externalDir + "/" + image);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            avatarImage.setImageBitmap(bitmap);
        } else {
            avatarImage.setImageResource(R.color.blue); // Резервне зображення
        }
    }

    public void setAccountImage(String image) {
        File file = new File(externalDir + "/" + image);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            accountImage.setImageBitmap(bitmap);
        } else {
            accountImage.setImageResource(R.color.yellow); // Резервне зображення
        }
    }

    public interface Navigation {
        void scannerQrAccount();

        void imageNavigation();

        void showSetting();

        void showAccount();
        void logout();
    }
}
