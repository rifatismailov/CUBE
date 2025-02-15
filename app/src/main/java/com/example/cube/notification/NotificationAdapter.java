package com.example.cube.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter {
    Context context;
    List<NotificationLogger> notificationLoggers;
    RecyclerView.ViewHolder holder;

    public NotificationAdapter(Context context, List<NotificationLogger> notificationLoggers) {
        this.context = context;
        this.notificationLoggers = notificationLoggers;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_log, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationLogger logger = notificationLoggers.get(position);
        this.holder = holder;
        if (holder.getClass().equals(NotificationViewHolder.class)) {
            NotificationViewHolder viewHolder = (NotificationViewHolder) holder;
            new NotificationHandler().setLog(viewHolder, logger);
        }
    }

    @Override
    public int getItemCount() {
        int size = notificationLoggers.size();
        return size;
    }
}
