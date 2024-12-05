package com.example.cube;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.cube.control.FIELD;
import com.example.folder.file.FileDetect;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class AccountManager {
    AccountOps accountOps;

    public AccountManager(AccountOps accountOps) {
        this.accountOps = accountOps;
    }

    public void readAccount(File externalDir) {

        JSONObject jsonObject = new FileDetect().readJsonFromFile(externalDir, "cube.json");
        if (jsonObject != null) {
            createAccount(jsonObject);
        }
    }
    public void writeAccount(File externalDir,String result){
        try {
                // Створюємо JSONObject з JSON-рядка, отриманого з QR-коду
                JSONObject jsonObject = new JSONObject(result);
                // Зберігаємо JSON-дані в файл "cube.json"
                new FileDetect().saveJsonToFile(externalDir, "cube.json", jsonObject);
                // Читаємо дані з JSON-об'єкта
                createAccount(jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
            // Тут можна також відобразити повідомлення про помилку користувачу
        }
    }
    /**
     * Метод для зчитування даних з JSON-файлу та їх обробки.
     *
     * @param jsonObject JSON-об'єкт, що містить дані користувача.
     */

    private void createAccount(JSONObject jsonObject) {
        if (jsonObject == null) {
            Toast.makeText((Context) accountOps, "Не вдалося завантажити файл JSON.", Toast.LENGTH_SHORT).show();
        } else {
            String userId = jsonObject.optString(FIELD.USER_ID.getFIELD(), "");
            String name = jsonObject.optString(FIELD.NAME.getFIELD(), "");
            String lastName = jsonObject.optString(FIELD.LAST_NAME.getFIELD(), "");
            String password = jsonObject.optString(FIELD.PASSWORD.getFIELD(), "");

            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(name) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(password)) {
                Toast.makeText((Context) accountOps, "Невірні дані у файлі JSON.", Toast.LENGTH_SHORT).show();
            } else {
                accountOps.setAccount(userId, name, lastName, password);
            }
        }
    }

    /**
     * Додає новий контакт, отриманий через QR-код або інші джерела.
     *
     * @param contact дані контакту у вигляді рядка JSON.
     */
    public void createContact(String contact) {
        try {
            // Створюємо JSONObject з контактних даних
            JSONObject jsonObject = new JSONObject(contact);
            String name_contact = jsonObject.getString(FIELD.CONTACT_NAME.getFIELD());
            String id_contact = jsonObject.getString(FIELD.CONTACT_ID.getFIELD());
            String public_key_contact = jsonObject.getString(FIELD.CONTACT_PUBLIC_KEY.getFIELD());
            // Додаємо новий контакт до списку користувачів
            accountOps.setContact(id_contact, public_key_contact, name_contact);
            // Оновлюємо мапу контактів

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public interface AccountOps {
        void setAccount(String userId, String name, String lastName, String password);

        void setContact(String id_contact, String public_key_contact, String name_contact);
    }
}
