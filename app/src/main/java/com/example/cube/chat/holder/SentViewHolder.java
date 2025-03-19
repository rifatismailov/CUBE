package com.example.cube.chat.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.chat.message.Message;
import com.example.cube.databinding.ItemSentBinding;

/**
 * ViewHolder to display sent messages in RecyclerView
 * ViewHolder для відображення надісланих повідомлень у RecyclerView.
 */
public class SentViewHolder extends RecyclerView.ViewHolder {

    /**
     * Binding to a list item display component
     * Прив'язка до компонента відображення елемента списку.
     */
    public ItemSentBinding binding;

    /**
     * Displayed message object
     * Об'єкт повідомлення, що відображається.
     */
    private Message message;

    /**
     * Constructor that initializes the binding to the element's layout
     * Конструктор, який ініціалізує прив'язку до макету елемента.
     *
     * @param itemView list item view
     *                 представлення (View) елемента списку.
     */
    public SentViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemSentBinding.bind(itemView);
    }

    /**
     * Gets a message object
     * Отримує об'єкт повідомлення.
     *
     * @return message stored in this ViewHolder
     *                 повідомлення, що зберігається у цьому ViewHolder.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Sets a new message for this ViewHolder
     * Встановлює нове повідомлення для цього ViewHolder.
     *
     * @param message the message object that will be bound to this ViewHolder
     *                об'єкт повідомлення, яке буде прив'язане до цього ViewHolder.
     */
    public void setMessage(Message message) {
        this.message = message;
    }
}