package com.example.cube.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.R;

import java.util.List;

/**
 * Адаптер NotificationAdapter використовується для відображення списку логів
 * сповіщень у RecyclerView.
 */
public class NotificationAdapter extends RecyclerView.Adapter {

    private final Context context;
    private final List<NotificationLogger> notificationLoggers;

    /**
     * Конструктор адаптера.
     * @param context Контекст додатку.
     * @param notificationLoggers Список логів сповіщень.
     */
    public NotificationAdapter(Context context, List<NotificationLogger> notificationLoggers) {
        this.context = context;
        this.notificationLoggers = notificationLoggers;
    }

    /**
     * Метод створює новий ViewHolder при необхідності.
     * @param parent Батьківський контейнер.
     * @param viewType Тип представлення.
     * @return Новий ViewHolder.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_log, parent, false);
        return new NotificationViewHolder(view);
    }

    /**
     * Метод прив'язує дані до ViewHolder.
     * @param holder ViewHolder, який потрібно оновити.
     * @param position Позиція елемента в списку.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationLogger logger = notificationLoggers.get(position);
        if (holder.getClass().equals(NotificationViewHolder.class)) {
            NotificationViewHolder viewHolder = (NotificationViewHolder) holder;
            new NotificationHandler().setLog(viewHolder, logger);
        }
    }

    /**
     * Метод повертає загальну кількість елементів у списку.
     * @return Кількість елементів у списку.
     */
    @Override
    public int getItemCount() {
        return notificationLoggers.size();
    }
}
