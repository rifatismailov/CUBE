package com.example.cube;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.cube.control.FIELD;
import com.example.cube.db.AccountManager;
import com.example.setting.UserSetting;

import org.json.JSONException;
import org.json.JSONObject;


import javax.crypto.SecretKey;

public class Manager {
    AccountOps accountOps;
    SecretKey secretKey;
    AccountManager accountManager;

    public Manager(AccountOps accountOps, SQLiteDatabase db, SecretKey secretKey) {
        this.accountOps = accountOps;
        accountManager = new AccountManager(db);
        this.secretKey = secretKey;
    }

    public void readAccount() {

        JSONObject jsonObject = accountManager.getAccount(secretKey);
        if (jsonObject != null) {
            createAccount(jsonObject);
        } else {
            Log.e("Manager", "Manager JSONObject null.");
        }
    }

    public JSONObject getAccount() {
        return accountManager.getAccount(secretKey);
    }

    public UserSetting userSetting() {
        return new UserSetting(getAccount());
    }

    public void writeAccount(JSONObject jsonObject) {
        if (jsonObject != null) {
            accountManager.setAccount(jsonObject, secretKey);
        }
        createAccount(jsonObject);
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
            String imageOrgName = jsonObject.optString("avatarImageUrl", "");
            String imageName = jsonObject.optString("accountImageUrl", "");

            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(name) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(password)) {
                Toast.makeText((Context) accountOps, "Невірні дані у файлі JSON.", Toast.LENGTH_SHORT).show();
            } else {
                accountOps.setAccount(userId, name, lastName, password, imageOrgName, imageName);
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

    public JSONObject getJson(String result) {
        JSONObject jsonObject = null;
        try {
            // Створюємо JSONObject з JSON-рядка, отриманого з QR-коду
            jsonObject = new JSONObject(result);
        } catch (JSONException e) {
            return null;
        }
        return jsonObject;
    }


    public interface AccountOps {
        void setAccount(String userId, String name, String lastName, String password, String imageOrgName, String imageName);

        void setContact(String id_contact, String public_key_contact, String name_contact);
    }
}
