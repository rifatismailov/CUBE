package com.example.cube.chat.message;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import androidx.recyclerview.widget.DiffUtil;
import java.util.List;

public class MessageDiffCallback extends DiffUtil.Callback {
    private final List<Message> oldList;
    private final List<Message> newList;

    public MessageDiffCallback(List<Message> oldList, List<Message> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    // Перевіряє, чи два об'єкти є однаковими (за унікальним `id`)
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getMessageId().equals(newList.get(newItemPosition).getMessageId());
    }

    // Перевіряє, чи дані у двох елементах однакові (текст, дата тощо)
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}

