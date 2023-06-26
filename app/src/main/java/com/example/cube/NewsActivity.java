package com.example.cube;

import android.os.AsyncTask;
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
import android.widget.Toast;

import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cube.databinding.ActivityNewsBinding;

import java.util.ArrayList;

public class NewsActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityNewsBinding binding;
    NewsAdapter adapter;
    ArrayList<News> news;
    ArrayList<News> new_news;
    String receiverUid;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        news = new ArrayList<>();
        new_news = new ArrayList<>();
        adapter = new NewsAdapter(this, news);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        showNews(i + " This is a photo of just one young family killed by a Russian missile attack " +
                "on Kryvyi Rih. Tonight, there was another terrorist attack – and again three dead, this time in #Odesa. " +
                "All the missiles fired by #Russia were produced in the spring of 2023. In all of them, " +
                "without… https://t.co/InqQMPl3xr https://t.co/ByK8CN1dyA");
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNews(i + " This is a photo of just one young family killed by a Russian missile attack " +
                        "on Kryvyi Rih. Tonight, there was another terrorist attack – and again three dead, this time in #Odesa. " +
                        "All the missiles fired by #Russia were produced in the spring of 2023. In all of them, " +
                        "without… https://t.co/InqQMPl3xr https://t.co/ByK8CN1dyA");
                i++;
            }

        });
        binding.adding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNews(i + " This is a photo of just one young family killed by a Russian missile attack " +
                        "on Kryvyi Rih. Tonight, there was another terrorist attack – and again three dead, this time in #Odesa. " +
                        "All the missiles fired by #Russia were produced in the spring of 2023. In all of them, " +
                        "without… https://t.co/InqQMPl3xr https://t.co/ByK8CN1dyA");
            }
        });
        binding.show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newNewsShow();
            }
        });

    }
    int k=0;
    private void setNews(String news_message){
        k++;
        new_news.add(new News(news_message, Check.Other, Side.News));
        Toast.makeText(this, k+" new news", Toast.LENGTH_SHORT).show();
    }
    private void newNewsShow(){
        for (News news1:new_news){
            news.add(news1);
            //binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
            binding.recyclerView.setLayoutManager(reverseLayout());
            adapter.notifyDataSetChanged();
        }
    }
    private void showNews(String news_message) {
        news.add(new News(news_message, Check.Other, Side.News));
        //binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
        binding.recyclerView.setLayoutManager(reverseLayout());
        adapter.notifyDataSetChanged();

    }

    /**
     * Method for reverse recycler View
     */
    public LinearLayoutManager reverseLayout() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        return linearLayoutManager;
    }

    //loading = new Loading(Download.this,Connection_Data[2], Connection_Data[3], Connection_Data[0], Integer.parseInt(Connection_Data[1]));
    //loading.executeOnExecutor(Executors.newScheduledThreadPool(1));
    public class NewsStatus extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {

            return null;
        }
    }

}