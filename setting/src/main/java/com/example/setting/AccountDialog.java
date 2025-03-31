package com.example.setting;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.folder.file.FilePathBuilder;
import com.example.setting.fragment.ImageQRFragment;
import com.example.setting.fragment.TextFragment;

import org.json.JSONObject;

import java.io.File;

/**
 * AccountDialog - клас, що представляє діалогове вікно для відображення даних акаунту.
 * Реалізує інтерфейси ImageQRFragment.ChangeFragment і TextFragment.ChangeFragment
 * для перемикання між фрагментами.
 */
public class AccountDialog extends DialogFragment implements ImageQRFragment.ChangeFragment, TextFragment.ChangeFragment {

    private final File externalDir; // Каталог для збереження зображень акаунту
    private boolean checkFragment = false; // Позначка активного фрагмента
    private final UserSetting userSetting; // Об'єкт налаштувань користувача
    private File accountImage; // Файл зображення акаунту

    /**
     * Конструктор класу AccountDialog.
     *
     * @param context   Контекст додатку.
     * @param jsonObject JSON-об'єкт з дані користувача.
     */
    public AccountDialog(@NonNull Context context, JSONObject jsonObject) {
        this.externalDir = FilePathBuilder.getDirectory(context, "imageProfile");
        userSetting = new UserSetting(jsonObject);
    }

    /**
     * Метод викликається під час створення діалогу.
     * Встановлює стиль повноекранного відображення.
     *
     * @param savedInstanceState Збережений стан.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
    }

    /**
     * Створює та повертає вигляд діалогового вікна.
     *
     * @param inflater  Об'єкт для розгортання макету.
     * @param container Батьківський контейнер.
     * @param savedInstanceState Збережений стан.
     * @return View для відображення.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_accaunt, container, false);

        // Налаштування параметрів вікна
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        try {
            // Створення або отримання файлу зображення акаунту
            accountImage = FilePathBuilder
                    .withDirectory(externalDir)
                    .setFileName(userSetting.getAccountImageUrl())
                    .newFile();
            // Завантаження початкового фрагменту
            replaceFragment(new ImageQRFragment(this, userSetting, accountImage));
        } catch (Exception e) {
            Log.e("AccountDialog", e.toString());
        }
        return view;
    }

    /**
     * Замінює поточний фрагмент на вказаний.
     *
     * @param fragment Новий фрагмент для відображення.
     */
    private void replaceFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.frame_content, fragment)
                .commit();
    }

    /**
     * Метод для перемикання між фрагментами (ImageQRFragment та TextFragment).
     */
    @Override
    public void changeFragment() {
        if (!checkFragment) {
            checkFragment = true;
            replaceFragment(new TextFragment(this, userSetting, accountImage));
        } else {
            checkFragment = false;
            replaceFragment(new ImageQRFragment(this, userSetting, accountImage));
        }
    }
}
