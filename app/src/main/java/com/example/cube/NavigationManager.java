package com.example.cube;

import android.app.Activity;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class NavigationManager {

    private Activity activity;
    Navigation navigation;
    private DrawerLayout drawerLayout;
    private Button accountButton, settingsButton, logoutButton;
    ImageButton add_accounte;

    public NavigationManager(Activity activity, DrawerLayout drawerLayout, ImageButton add_accounte, Button accountButton, Button settingsButton, Button logoutButton) {
        this.activity = activity;
        this.navigation = (Navigation) activity;
        this.add_accounte = add_accounte;
        this.drawerLayout = drawerLayout;
        this.accountButton = accountButton;
        this.settingsButton = settingsButton;
        this.logoutButton = logoutButton;


        setupButtons();
    }

    private void setupButtons() {
        add_accounte.setOnClickListener(v -> handleMenuItemClick(R.id.add_account));
        accountButton.setOnClickListener(v -> handleMenuItemClick(R.id.nav_account));
        settingsButton.setOnClickListener(v -> handleMenuItemClick(R.id.nav_settings));
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
        }
        // Закрити Drawer після вибору
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public interface Navigation {
        void scannerQrAccount();
    }
}
