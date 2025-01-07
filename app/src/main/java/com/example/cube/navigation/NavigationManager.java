package com.example.cube.navigation;

import static android.widget.Toast.*;

import android.app.Activity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.cube.R;

public class NavigationManager {

    private Activity activity;
    Navigation navigation;
    private DrawerLayout drawerLayout;
    ImageView avatarImage;
    ImageView accountImage;
    private Button accountButton, settingsButton, logoutButton;
    ImageButton add_accounte;

    public NavigationManager(Activity activity, DrawerLayout drawerLayout, ImageView avatarImage,ImageView accountImage, ImageButton add_accounte, Button accountButton, Button settingsButton, Button logoutButton) {
        this.activity = activity;
        if (!(activity instanceof Navigation)) {
            //Перевірка типу activity у конструкторі: Якщо активність не імплементує Navigation, це може викликати ClassCastException.
            throw new IllegalArgumentException("Activity must implement Navigation interface");
        }
        this.navigation = (Navigation) activity;
        this.drawerLayout = drawerLayout;
        this.add_accounte = add_accounte;
        this.avatarImage = avatarImage;
        this.accountImage=accountImage;
        this.accountButton = accountButton;
        this.settingsButton = settingsButton;
        this.logoutButton = logoutButton;
        setupButtons();
    }

    private void setupButtons() {
        add_accounte.setOnClickListener(v -> handleMenuItemClick(R.id.add_account));
        accountButton.setOnClickListener(v -> handleMenuItemClick(R.id.nav_account));
        settingsButton.setOnClickListener(v -> handleMenuItemClick(R.id.nav_settings));
        accountImage.setOnClickListener(v -> handleMenuItemClick(R.id.accountImage));

        logoutButton.setOnClickListener(v -> handleMenuItemClick(R.id.nav_logout));
    }

    private void handleMenuItemClick(int itemId) {
        switch (itemId) {
            case R.id.add_account:
                navigation.scannerQrAccount();
                // Логіка для "Акаунт"
                break;
            case R.id.nav_account:
                // Логіка для "Акаунт"
                break;
            case R.id.nav_settings:
                // Логіка для "Налаштування"
                break;
            case R.id.nav_logout:

                // Логіка для "Вихід"
                break;
            case R.id.accountImage:
                Log.e("MainActivity", "AAAA");
                navigation.imageNavigation();
                break;
        }
        // Закрити Drawer після вибору
       // drawerLayout.closeDrawer(GravityCompat.START);
    }

    public interface Navigation {
        void scannerQrAccount();
        void imageNavigation();
    }
}
