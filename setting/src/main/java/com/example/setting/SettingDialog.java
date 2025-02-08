package com.example.setting;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import org.json.JSONObject;

public class SettingDialog extends Dialog {
    private OnInputListener onInputListener;

    public SettingDialog(@NonNull Context context) {
        super(context, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_input);

        // Make dialog full screen
        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        TextInputEditText inputUsername = findViewById(R.id.input_username);
        TextInputEditText inputUserLastName = findViewById(R.id.input_userLastName);
        TextInputEditText inputUserId = findViewById(R.id.input_userId);
        TextInputEditText inputMessagingServerIp = findViewById(R.id.input_messaging_server_ip);
        TextInputEditText inputMessagingServerPort = findViewById(R.id.input_messaging_server_port);
        TextInputEditText inputFileServerIp = findViewById(R.id.input_file_server_ip);
        TextInputEditText inputFileServerPort = findViewById(R.id.input_file_server_port);
        SwitchMaterial switchNotifications = findViewById(R.id.switch_notifications);
        MaterialButton buttonSave = findViewById(R.id.button_save);

        buttonSave.setOnClickListener(v -> {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", inputUsername.getText().toString());
                jsonObject.put("userLastName", inputUserLastName.getText().toString());
                jsonObject.put("userId", inputUserId.getText().toString());
                jsonObject.put("messagingServerIp", inputMessagingServerIp.getText().toString());
                jsonObject.put("messagingServerPort", inputMessagingServerPort.getText().toString());
                jsonObject.put("fileServerIp", inputFileServerIp.getText().toString());
                jsonObject.put("fileServerPort", inputFileServerPort.getText().toString());
                jsonObject.put("notifications", switchNotifications.isChecked());

                if (onInputListener != null) {
                    onInputListener.onInput(jsonObject.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            dismiss();
        });
    }

    public void setOnInputListener(OnInputListener onInputListener) {
        this.onInputListener = onInputListener;
    }

    public interface OnInputListener {
        void onInput(String jsonData);
    }
}
