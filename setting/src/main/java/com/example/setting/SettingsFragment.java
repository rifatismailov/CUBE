package com.example.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONObject;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        // Встановлення початкових значень
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());


        setupInitialValues(sharedPreferences);

        // Обробка натискання кнопки "Згенерувати JSON"
        Preference generateJsonButton = findPreference("generate_new_setting");
        if (generateJsonButton != null) {
            generateJsonButton.setOnPreferenceClickListener(preference -> {
                String jsonSettings = getPreferencesAsJson();
                // Можете зберегти JSON у базу даних або інше сховище
                saveSettingsToDatabase(jsonSettings);
                Toast.makeText(getContext(), "Налаштування збережено", Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        // Обробка натискання кнопки "Інформація про розробника"
        Preference developerInfoButton = findPreference("developer_info");
        if (developerInfoButton != null) {
            developerInfoButton.setOnPreferenceClickListener(preference -> {
                showDeveloperInfo();
                return true;
            });
        }
    }

    private void setupInitialValues(SharedPreferences sharedPreferences) {
        EditTextPreference username = findPreference("username");
        if (username != null) {
            username.setText(sharedPreferences.getString("username", ""));
        }

        EditTextPreference userLastName = findPreference("userLastName");
        if (userLastName != null) {
            userLastName.setText(sharedPreferences.getString("userLastName", ""));
        }

        EditTextPreference userId = findPreference("userId");
        if (userId != null) {
            userId.setText(sharedPreferences.getString("userId", ""));
        }

        EditTextPreference messagingServerIp = findPreference("messaging_server_ip");
        if (messagingServerIp != null) {
            messagingServerIp.setText(sharedPreferences.getString("messaging_server_ip", ""));
        }

        EditTextPreference messagingServerPort = findPreference("messaging_server_port");
        if (messagingServerPort != null) {
            messagingServerPort.setText(sharedPreferences.getString("messaging_server_port", ""));
        }

        EditTextPreference fileServerIp = findPreference("file_server_ip");
        if (fileServerIp != null) {
            fileServerIp.setText(sharedPreferences.getString("file_server_ip", ""));
        }

        EditTextPreference fileServerPort = findPreference("file_server_port");
        if (fileServerPort != null) {
            fileServerPort.setText(sharedPreferences.getString("file_server_port", ""));
        }
    }

    public String getPreferencesAsJson() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        String username = sharedPreferences.getString("username", "");
        String userLastName = sharedPreferences.getString("userLastName", "");
        String userId = sharedPreferences.getString("userId", "");
        String messagingServerIp = sharedPreferences.getString("messaging_server_ip", "");
        String messagingServerPort = sharedPreferences.getString("messaging_server_port", "");
        String fileServerIp = sharedPreferences.getString("file_server_ip", "");
        String fileServerPort = sharedPreferences.getString("file_server_port", "");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("userLastName", userLastName);
            jsonObject.put("userId", userId);
            jsonObject.put("messagingServerIp", messagingServerIp);
            jsonObject.put("messagingServerPort", messagingServerPort);
            jsonObject.put("fileServerIp", fileServerIp);
            jsonObject.put("fileServerPort", fileServerPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private void saveSettingsToDatabase(String jsonSettings) {
        // Реалізація збереження JSON у базу даних
    }

    private void showDeveloperInfo() {
        Toast.makeText(getContext(), "Розробник: Ім'я Розробника\nEmail: developer@example.com", Toast.LENGTH_LONG).show();
    }
}
