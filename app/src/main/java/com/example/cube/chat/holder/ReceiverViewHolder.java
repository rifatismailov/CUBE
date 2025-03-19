package com.example.cube.chat.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.chat.message.Message;
import com.example.cube.databinding.ItemReceiveBinding;

/**
 * ViewHolder to display received messages in RecyclerView.
 * ViewHolder для відображення отриманих повідомлень у RecyclerView.
 */
public class ReceiverViewHolder extends RecyclerView.ViewHolder {

    /**
     * Object for linking layout elements.
     * Об'єкт для зв'язування елементів макета.
     */
    public ItemReceiveBinding binding;

    /**
     * The message object that is displayed in this ViewHolder.
     * Об'єкт повідомлення, яке відображається в даному ViewHolder.
     */
    private Message message;

    /**
     * Constructor for initializing ViewHolder.
     * Конструктор для ініціалізації ViewHolder.
     *
     * @param itemView List item representation
     *                 Представлення елемента списку.
     *
     */
    public ReceiverViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemReceiveBinding.bind(itemView);
    }

    /**
     * Returns the message contained in this ViewHolder.
     * Повертає повідомлення, яке міститься в цьому ViewHolder.
     *
     * @return Message.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Sets the message for this ViewHolder
     * Встановлює повідомлення для цього ViewHolder.
     *
     * @param message The Message object to display
     *                Об'єкт Message, який потрібно відобразити.
     */
    public void setMessage(Message message) {
        this.message = message;
    }
}