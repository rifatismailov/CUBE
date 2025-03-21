package com.example.setting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.folder.file.FilePathBuilder;
import com.example.setting.fragment.ImageFragment;
import com.example.setting.fragment.TextFragment;


import org.json.JSONObject;

import java.io.File;

public class AccountDialog extends DialogFragment implements ImageFragment.ChangeFragment, TextFragment.ChangeFragment {
    private JSONObject jsonObject;
    private final File externalDir;
    private Context context;
    private boolean checkFragment = false;
    private final UserSetting userSetting;
    private File accountImage;

    public AccountDialog(@NonNull Context context, JSONObject jsonObject) {
        this.context = context;
        this.jsonObject = jsonObject;
        this.externalDir = FilePathBuilder.getDirectory(context, "imageProfile");
        userSetting = new UserSetting(jsonObject);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_accaunt, container, false);

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

            accountImage = FilePathBuilder
                    .withDirectory(externalDir)
                    .setFileName(userSetting.getAccountImageUrl())
                    .newFile();



            replaceFragment(new ImageFragment(this, userSetting, accountImage));
        } catch (Exception e) {
            Log.e("AccountDialog", e.toString());
        }


        return view;
    }

    private void replaceFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.frame_content, fragment)
                .commit();
    }

    @Override
    public void changeFragment() {
        if (!checkFragment) {
            checkFragment = true;
            replaceFragment(new TextFragment(this, userSetting, accountImage));
        } else {
            checkFragment = false;
            replaceFragment(new ImageFragment(this, userSetting, accountImage));

        }
    }
}
