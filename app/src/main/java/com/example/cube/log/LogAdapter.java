package com.example.cube.log;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.MainActivity;
import com.example.cube.R;
import com.example.cube.holder.SentViewHolder;

import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter {
    Context context;
    List<Logger> loggers;
    RecyclerView.ViewHolder holder;

    public LogAdapter(Context context, List<Logger> loggers) {
        this.context = context;
        this.loggers = loggers;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_log, parent, false);
        return new LoggerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Logger logger = loggers.get(position);
        this.holder = holder;
        if (holder.getClass().equals(LoggerViewHolder.class)) {
            LoggerViewHolder viewHolder = (LoggerViewHolder) holder;
            new LoggerHandler().setLog(viewHolder, logger);
        }
    }

    @Override
    public int getItemCount() {
        int size = loggers.size();
        return size;
    }
}
