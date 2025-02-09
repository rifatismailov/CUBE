package com.example.setting.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.setting.R;

public class TextFragment extends Fragment {

    private ChangeFragment changeFragment;

    public TextFragment(ChangeFragment changeFragment) {
        this.changeFragment = changeFragment;
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
        TextView textView = view.findViewById(R.id.textView);
        ImageView imageView = view.findViewById(R.id.accountImage);
        textView.setText("Цей підхід дозволяє гнучко змінювати макет через XML та додавати нові елементи без змін у коді класу.\n" +
                "Для роботи з елементами макета в коді можна використовувати метод findViewById()");
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragment.changeFragment();
            }
        });
    }

    public interface ChangeFragment {
        void changeFragment();
    }

}
