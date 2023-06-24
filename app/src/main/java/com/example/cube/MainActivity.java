package com.example.cube;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;

import com.example.cube.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        startActivity(binding.getRoot().getRootView());
        binding.btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(view);
            }
        });

    }
    public void startActivity(View view) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("name", "Vasia Pupkin");
        startActivity(intent);
    }
}