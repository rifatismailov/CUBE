package com.example.cube.chat.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.chat.message.Message;
import com.example.cube.databinding.ItemSentBinding;

/**
 * ViewHolder для відображення надісланих повідомлень у RecyclerView.
 */
public class SentViewHolder extends RecyclerView.ViewHolder {

    /**
     * Прив'язка до компонента відображення елемента списку.
     */
    public ItemSentBinding binding;

    /**
     * Об'єкт повідомлення, що відображається.
     */
    private Message message;

    /**
     * Конструктор, який ініціалізує прив'язку до макету елемента.
     *
     * @param itemView представлення (View) елемента списку.
     */
    public SentViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemSentBinding.bind(itemView);
    }

    /**
     * Отримує об'єкт повідомлення.
     *
     * @return повідомлення, що зберігається у цьому ViewHolder.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Встановлює нове повідомлення для цього ViewHolder.
     *
     * @param message об'єкт повідомлення, яке буде прив'язане до цього ViewHolder.
     */
    public void setMessage(Message message) {
        this.message = message;
    }
}