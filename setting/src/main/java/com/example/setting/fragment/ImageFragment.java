package com.example.setting.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.qrcode.QRCode;
import com.example.setting.R;
import com.example.setting.UserSetting;

import java.io.File;


public class ImageFragment extends Fragment {
    private final ChangeFragment changeFragment;
    private final UserSetting userSetting;
    private final File accountImage;

    public ImageFragment(ChangeFragment changeFragment, UserSetting userSetting, File accountImage) {
        this.changeFragment = changeFragment;
        this.userSetting = userSetting;
        this.accountImage = accountImage;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Підключення макета з XML
        return inflater.inflate(R.layout.fragment_qrcode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imageView = view.findViewById(R.id.qrCodeImage);
        imageView.setImageResource(R.color.blue);
        String jsonData = new UserSetting.Builder()
                .setId(userSetting.getId())
                .setName(userSetting.getName())
                .setLastName(userSetting.getLastName())
                .build().toJson("userId", "name", "lastName").toString();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2; // Зменшити розмір у два рази
        Bitmap bitmap = BitmapFactory.decodeFile(accountImage.toString(), options);
        imageView.setImageBitmap(QRCode.getQRCode(jsonData, bitmap));
        imageView.setOnClickListener(view1 -> changeFragment.changeFragment());
    }

    public interface ChangeFragment {
        void changeFragment();
    }
}
