package com.example.cube.navigation;

import static android.widget.Toast.*;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.cube.R;

import java.io.File;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class NavigationManager {

    private Activity activity;
    Navigation navigation;
    private DrawerLayout drawerLayout;
    ImageView avatarImage;
    ImageView accountImage;
    private Button accountButton, settingsButton, logoutButton;
    private final File externalDir;

    public NavigationManager(Activity activity, DrawerLayout drawerLayout, ImageView avatarImage, ImageView accountImage, Button accountButton, Button settingsButton, Button logoutButton) {
        this.activity = activity;
        if (!(activity instanceof Navigation)) {
            //Перевірка типу activity у конструкторі: Якщо активність не імплементує Navigation, це може викликати ClassCastException.
            throw new IllegalArgumentException("Activity must implement Navigation interface");
        }
        this.navigation = (Navigation) activity;
        this.drawerLayout = drawerLayout;
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
                break;
            case R.id.nav_settings:
                // Логіка для "Налаштування"
                navigation.showSetting();
                break;
            case R.id.nav_logout:
                navigation.scannerQrAccount();
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
            avatarImage.setImageResource(R.drawable.ukraineflag); // Резервне зображення
        }
    }

    public void setAccountImage(String image) {
        File file = new File(externalDir + "/" + image);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            accountImage.setImageBitmap(bitmap);
        } else {
            accountImage.setImageResource(R.drawable.ukraineflag); // Резервне зображення
        }
    }

    public interface Navigation {
        void scannerQrAccount();

        void imageNavigation();
        void showSetting();
    }
}
