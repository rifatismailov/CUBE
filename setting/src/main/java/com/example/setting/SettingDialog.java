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
    private JSONObject jsonObject;
    private TextInputEditText inputUsername;
    private TextInputEditText inputUserLastName;
    private TextInputEditText inputUserId;
    private TextInputEditText input_userPassword;
    private TextInputEditText inputMessagingServerIp;
    private TextInputEditText inputMessagingServerPort;
    private TextInputEditText inputFileServerIp;
    private TextInputEditText inputFileServerPort;
    private SwitchMaterial switchNotifications;
    private final File externalDir;
    private ImageView avatarImage;
    private ImageView accountImage;
    private IClassSetting iClassSetting;
    private UserSetting userSetting;

    public SettingDialog(@NonNull Context context, JSONObject jsonObject) {
        super(context, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        this.jsonObject = jsonObject;
        this.iClassSetting = (IClassSetting) context;
        externalDir = new File(context.getExternalFilesDir(null), "imageProfile");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_input);

        // Make dialog full screen and cover the top part of the screen
        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

            // Remove the status bar
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            // Set the window to be "immersive" (covering status bar)
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        userSetting = new UserSetting(jsonObject);
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

        setAvatarImage(userSetting.getAvatarImageUrl());
        setAccountImage(userSetting.getAccountImageUrl());
        buttonSave.setOnClickListener(v -> {
            saveSetting();

            dismiss();
        });
    }

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
            //JSONObject jsonObject = userSetting.toJson();


        } catch (Exception e) {
            e.printStackTrace();
        }
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
            accountImage.setImageResource(R.color.green); // Резервне зображення
        }
    }

    public interface IClassSetting {
        void onSetting(JSONObject jsonObject);
    }
}
