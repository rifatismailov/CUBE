package com.example.cube;

import android.os.Bundle;

import com.example.cube.adapters.MessagesAdapter;
import com.example.cube.adapters.NewsAdapter;
import com.example.cube.control.Check;
import com.example.cube.control.Side;
import com.example.cube.models.Message;
import com.example.cube.models.News;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cube.databinding.ActivityNewsBinding;

import java.util.ArrayList;

public class NewsActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityNewsBinding binding;
    NewsAdapter adapter;
    ArrayList<News> news;
    String receiverUid;
    int i=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        news = new ArrayList<>();
        adapter = new NewsAdapter(this, news);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNews(i+" This is a photo of just one young family killed by a Russian missile attack " +
                        "on Kryvyi Rih. Tonight, there was another terrorist attack – and again three dead, this time in #Odesa. " +
                        "All the missiles fired by #Russia were produced in the spring of 2023. In all of them, " +
                        "without… https://t.co/InqQMPl3xr https://t.co/ByK8CN1dyA");
                i++;
            }

        });

    }

    private void showNews(String news_message) {
        news.add(new News(news_message, Check.Other, Side.News));
        //binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
        binding.recyclerView.setLayoutManager(reverseLayout());
        adapter.notifyDataSetChanged();

    }
    public LinearLayoutManager reverseLayout(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        return  linearLayoutManager;
    }

}