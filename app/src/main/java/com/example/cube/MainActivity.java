package com.example.cube;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;

import com.example.cube.databinding.ActivityMainBinding;
import com.example.cube.permission.Permission;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Permission(this);

        binding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        startChat(binding.getRoot().getRootView());
        binding.btChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChat(binding.getRoot().getRootView());
            }
        });
        binding.btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNews(view);
            }
        });
        binding.btInstruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startInstruction(view);
            }
        });

    }
    public void startChat(View view) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("name", "Vasia Pupkin");
        startActivity(intent);
    }
    public void startNews(View view) {
        Intent intent = new Intent(this, NewsActivity.class);
        intent.putExtra("name", "Vasia Pupkin");
        startActivity(intent);
    }
    public void startInstruction(View view) {
        Intent intent = new Intent(this, Instruction_Activity.class);
        intent.putExtra("name", "Vasia Pupkin");
        startActivity(intent);
    }

}