package com.example.cube.chat.message;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

/**
 * The {@code MessageDiffCallback} class is used for optimized updating of the message list
 * in {@link androidx.recyclerview.widget.RecyclerView}. It compares the old and new list
 * of messages, determining which elements have changed.
 */
public class MessageDiffCallback extends DiffUtil.Callback {
    private final List<Message> oldList;
    private final List<Message> newList;

    /**
     * Constructor to initialize the old and new message lists.
     *
     * @param oldList old message list
     * @param newList new message list
     */
    public MessageDiffCallback(List<Message> oldList, List<Message> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    /**
     * Returns the size of the old list.
     *
     * @return number of items in the old list
     */
    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    /**
     * Returns the size of the new list.
     *
     * @return number of items in the new list
     */
    @Override
    public int getNewListSize() {
        return newList.size();
    }

    /**
     * Checks if two items are the same by unique message identifier.
     *
     * @param oldItemPosition the position of the item in the old list
     * @param newItemPosition the position of the item in the new list
     * @return {@code true} if the items have the same ID, otherwise {@code false}
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getMessageId().equals(newList.get(newItemPosition).getMessageId());
    }

    /**
     * Checks if the contents of two items are the same.
     *
     * @param oldItemPosition the position of the item in the old list
     * @param newItemPosition the position of the item in the new list
     * @return {@code true} if the contents of the items are the same, otherwise {@code false}
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}