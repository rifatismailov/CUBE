/**
 * SettingDialog is a custom full-screen dialog for managing user settings.
 * It allows users to input and save data such as user credentials,
 * server configurations, and toggle notification settings.
 */
package com.example.setting;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.File;

public class SettingDialog extends Dialog {
    /** JSON object containing user settings data. */
    private JSONObject jsonObject;

    /** Input fields for user data and server configuration. */
    private TextInputEditText inputUsername, inputUserLastName, inputUserId, input_userPassword;
    private TextInputEditText inputMessagingServerIp, inputMessagingServerPort;
    private TextInputEditText inputFileServerIp, inputFileServerPort;

    /** Switch to enable or disable notifications. */
    private SwitchMaterial switchNotifications;

    /** Directory for storing user profile images. */
    private final File externalDir;

    /** Image views for displaying user avatar and account images. */
    private ImageView avatarImage, accountImage;

    /** Callback interface for saving user settings. */
    private IClassSetting iClassSetting;

    /** User settings object. */
    private UserSetting userSetting;

    /**
     * Constructor for creating the dialog.
     *
     * @param context   Context for accessing resources and UI elements.
     * @param jsonObject JSON object containing initial user settings.
     */
    public SettingDialog(@NonNull Context context, JSONObject jsonObject) {
        super(context, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        this.jsonObject = jsonObject;
        this.iClassSetting = (IClassSetting) context;
        this.externalDir = new File(context.getExternalFilesDir(null), "imageProfile");
    }

    /**
     * Called when the dialog is created to initialize UI elements and configure settings.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_input);

        // Configure dialog to be full screen and immersive.
        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        // Initialize user settings
        userSetting = new UserSetting(jsonObject);

        // Bind UI elements to fields
        inputUserId = findViewById(R.id.input_userId);
        inputUsername = findViewById(R.id.input_username);
        inputUserLastName = findViewById(R.id.input_userLastName);
        input_userPassword = findViewById(R.id.input_userPassword);
        inputMessagingServerIp = findViewById(R.id.input_messaging_server_ip);
        inputMessagingServerPort = findViewById(R.id.input_messaging_server_port);
        inputFileServerIp = findViewById(R.id.input_file_server_ip);
        inputFileServerPort = findViewById(R.id.input_file_server_port);
        switchNotifications = findViewById(R.id.switch_notifications);
        MaterialButton buttonSave = findViewById(R.id.button_save);
        avatarImage = findViewById(R.id.avatarImage);
        accountImage = findViewById(R.id.accountImage);

        // Set initial values from user settings
        inputUserId.setText(userSetting.getId());
        inputUsername.setText(userSetting.getName());
        inputUserLastName.setText(userSetting.getLastName());
        input_userPassword.setText(userSetting.getPassword());
        inputMessagingServerIp.setText(userSetting.getServerIp());
        inputMessagingServerPort.setText(userSetting.getServerPort());
        inputFileServerIp.setText(userSetting.getFileServerIp());
        inputFileServerPort.setText(userSetting.getFileServerPort());
        switchNotifications.setChecked(userSetting.isNotifications());

        Log.e("UserSetting", userSetting.getAvatarImageUrl() + " " + userSetting.getAccountImageUrl());

        // Load user profile images
        setAvatarImage(userSetting.getAvatarImageUrl());
        setAccountImage(userSetting.getAccountImageUrl());

        // Handle save button click
        buttonSave.setOnClickListener(v -> {
            saveSetting();
            dismiss();
        });
    }

    /**
     * Saves user settings and triggers the callback.
     */
    public void saveSetting() {
        try {
            UserSetting saveSetting = new UserSetting.Builder()
                    .setId(inputUserId.getText().toString())
                    .setName(inputUsername.getText().toString())
                    .setLastName(inputUserLastName.getText().toString())
                    .setPassword(input_userPassword.getText().toString())
                    .setAvatarImageUrl(userSetting.getAvatarImageUrl())
                    .setAccountImageUrl(userSetting.getAccountImageUrl())
                    .setServerIp(inputMessagingServerIp.getText().toString())
                    .setServerPort(inputMessagingServerPort.getText().toString())
                    .setFileServerIp(inputFileServerIp.getText().toString())
                    .setFileServerPort(inputFileServerPort.getText().toString())
                    .setNotifications(switchNotifications.isChecked())
                    .build();
            iClassSetting.onSetting(saveSetting.toJson());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the avatar image if the file exists, otherwise uses a default image.
     *
     * @param image Name of the image file.
     */
    public void setAvatarImage(String image) {
        File file = new File(externalDir + "/" + image);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            avatarImage.setImageBitmap(bitmap);
        } else {
            avatarImage.setImageResource(R.color.blue); // Default image
        }
    }

    /**
     * Sets the account image if the file exists, otherwise uses a default image.
     *
     * @param image Name of the image file.
     */
    public void setAccountImage(String image) {
        File file = new File(externalDir + "/" + image);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            accountImage.setImageBitmap(bitmap);
        } else {
            accountImage.setImageResource(R.color.green); // Default image
        }
    }

    /**
     * Callback interface for handling settings updates.
     */
    public interface IClassSetting {
        void onSetting(JSONObject jsonObject);
    }
}
