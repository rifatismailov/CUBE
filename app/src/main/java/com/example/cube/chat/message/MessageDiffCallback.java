package com.example.cube.chat.message;

import androidx.recyclerview.widget.DiffUtil;
import java.util.List;

/**
 * Клас {@code MessageDiffCallback} використовується для оптимізованого оновлення списку повідомлень
 * у {@link androidx.recyclerview.widget.RecyclerView}. Він порівнює старий і новий список
 * повідомлень, визначаючи, які елементи змінилися.
 */
public class MessageDiffCallback extends DiffUtil.Callback {
    private final List<Message> oldList;
    private final List<Message> newList;

    /**
     * Конструктор для ініціалізації старого та нового списку повідомлень.
     *
     * @param oldList старий список повідомлень
     * @param newList новий список повідомлень
     */
    public MessageDiffCallback(List<Message> oldList, List<Message> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    /**
     * Повертає розмір старого списку.
     *
     * @return кількість елементів у старому списку
     */
    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    /**
     * Повертає розмір нового списку.
     *
     * @return кількість елементів у новому списку
     */
    @Override
    public int getNewListSize() {
        return newList.size();
    }

    /**
     * Перевіряє, чи два елементи є однаковими за унікальним ідентифікатором повідомлення.
     *
     * @param oldItemPosition позиція елемента у старому списку
     * @param newItemPosition позиція елемента у новому списку
     * @return {@code true}, якщо елементи мають однаковий ідентифікатор, інакше {@code false}
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getMessageId().equals(newList.get(newItemPosition).getMessageId());
    }

    /**
     * Перевіряє, чи вміст двох елементів однаковий.
     *
     * @param oldItemPosition позиція елемента у старому списку
     * @param newItemPosition позиція елемента у новому списку
     * @return {@code true}, якщо вміст елементів однаковий, інакше {@code false}
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
