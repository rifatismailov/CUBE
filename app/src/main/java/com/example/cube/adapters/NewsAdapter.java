package com.example.cube.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.ChatActivity;
import com.example.cube.NewsActivity;
import com.example.cube.R;
import com.example.cube.control.Check;
import com.example.cube.control.Side;
import com.example.cube.emoji.MyEmoji;
import com.example.cube.holder.NewsViewHolder;
import com.example.cube.models.EmojiMany;
import com.example.cube.models.News;
import com.example.cube.visualization.Watcher;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter {


    Context context;
    ArrayList<News> news;

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;


    public NewsAdapter(Context context, ArrayList<News> news) {
        this.context = context;
        this.news = news;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
            return new NewsViewHolder(view);

    }

    @Override
    public int getItemViewType(int position) {
        News news_Person = news.get(position);
        if (news_Person.getSide().equals(Side.Sender)) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        News news_Person = news.get(position);

        NewsViewHolder viewHolder = (NewsViewHolder) holder;
        viewHolder.binding.news.addTextChangedListener(new Watcher((Activity) context));


        if (news_Person.getCheck().equals(Check.Image) && news_Person.getNews().isEmpty()) {
            viewHolder.binding.image.setVisibility(View.VISIBLE);
            viewHolder.binding.news.setVisibility(View.GONE);
            viewHolder.binding.image.setImageURI(Uri.parse(news_Person.getImageUrl()));
        } else if (news_Person.getCheck().equals(Check.Image) && !news_Person.getNews().isEmpty()) {
            viewHolder.binding.image.setVisibility(View.VISIBLE);
            viewHolder.binding.news.setVisibility(View.VISIBLE);
            viewHolder.binding.image.setImageURI(Uri.parse(news_Person.getImageUrl()));
        } else {
            viewHolder.binding.image.setVisibility(View.GONE);
            viewHolder.binding.news.setVisibility(View.VISIBLE);
        }

        viewHolder.binding.news.setText(news_Person.getNews());



    }

    @Override
    public int getItemCount() {
        return news.size();
    }

}
