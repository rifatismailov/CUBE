package com.example.cube.chat.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.chat.message.Message;
import com.example.cube.databinding.ItemReceiveBinding;

/**
 * ViewHolder для відображення отриманих повідомлень у RecyclerView.
 */
public class ReceiverViewHolder extends RecyclerView.ViewHolder {

    /**
     * Об'єкт для зв'язування елементів макета.
     */
    public ItemReceiveBinding binding;

    /**
     * Об'єкт повідомлення, яке відображається в даному ViewHolder.
     */
    private Message message;

    /**
     * Конструктор для ініціалізації ViewHolder.
     *
     * @param itemView Представлення елемента списку.
     */
    public ReceiverViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemReceiveBinding.bind(itemView);
    }

    /**
     * Повертає повідомлення, яке міститься в цьому ViewHolder.
     *
     * @return Об'єкт Message.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Встановлює повідомлення для цього ViewHolder.
     *
     * @param message Об'єкт Message, який потрібно відобразити.
     */
    public void setMessage(Message message) {
        this.message = message;
    }
}