package com.example.setting.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.setting.R;
import com.example.setting.UserSetting;

import java.io.File;

public class TextFragment extends Fragment {

    private final ChangeFragment changeFragment;
    private final UserSetting userSetting;
    private final File accountImage;

    public TextFragment(ChangeFragment changeFragment, UserSetting userSetting, File accountImage) {
        this.changeFragment = changeFragment;
        this.userSetting = userSetting;
        this.accountImage = accountImage;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Підключення макета з XML
        return inflater.inflate(R.layout.fragment_text, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView idNumber = view.findViewById(R.id.idNumber);
        TextView name = view.findViewById(R.id.name);
        TextView lastName = view.findViewById(R.id.lastName);
        ImageView imageView = view.findViewById(R.id.accountImage);
        idNumber.setText(userSetting.getId());
        name.setText(userSetting.getName());
        lastName.setText(userSetting.getLastName());
        if (accountImage.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(accountImage.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.color.blue); // Default image
        }
        imageView.setOnClickListener(view1 -> changeFragment.changeFragment());
    }

    public interface ChangeFragment {
        void changeFragment();
    }

}
