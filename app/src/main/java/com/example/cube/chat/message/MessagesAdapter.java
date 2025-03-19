package com.example.cube.chat.message;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.chat.ChatActivity;
import com.example.cube.control.Side;
import com.example.cube.R;
import com.example.cube.chat.holder.ReceiverViewHolder;
import com.example.cube.chat.holder.SentViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Adapter for displaying a list of messages in a RecyclerView.
 * Responsible for creating and updating list items, as well as their appearance.
 */
public class MessagesAdapter extends RecyclerView.Adapter {

    private final Context context;
    private final ArrayList<Message> messages;
    private final HashMap<String, Boolean> expandedStates = new HashMap<>(); // Store message expansion state

    private static final int ITEM_SENT = 1;
    private static final int ITEM_RECEIVE = 2;

    /**
     * Adapter constructor.
     *
     * @param context The context of the chat activity.
     * @param messages The list of messages.
     */
    public MessagesAdapter(ChatActivity context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    /**
     * Updates the list of messages using DiffUtil to optimize updates.
     *
     * @param newMessages The new list of messages.
     */
    public void updateMessages(List<Message> newMessages) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MessageDiffCallback(messages, newMessages));
        messages.clear();
        messages.addAll(newMessages);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.getSide().equals(Side.Sender) ? ITEM_SENT : ITEM_RECEIVE;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        String messageId = message.getMessageId(); // Get the unique message identifier

        if (holder instanceof SentViewHolder) {
            SentViewHolder viewHolder = (SentViewHolder) holder;
            new SendMessageHandler(context).setMessage(viewHolder, message);

            // Make sure all fields are set
            viewHolder.binding.message.setText(message.getMessage() != null ? message.getMessage() : "");
            viewHolder.binding.file.setText(message.getFileName() != null ? message.getFileName() : "");

            // Set the visibility of the fields
            viewHolder.binding.messageLayout.setVisibility(message.getMessage() != null ? View.VISIBLE : View.GONE);

            viewHolder.binding.fileLayout.setVisibility(message.getFileName() != null ? View.VISIBLE : View.GONE);

        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            new ReceiverMessageHandler(context).setMessage(viewHolder, message);
            // Make sure all the fields are set
            viewHolder.binding.message.setText(message.getMessage() != null ? message.getMessage() : "");
            // Set the visibility of the fields
            viewHolder.binding.messageLayout.setVisibility(message.getMessage() != null ? View.VISIBLE : View.GONE);
            new ReceiverMessageHandler(context).setMessage(viewHolder, message);

        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * Adds a new message and automatically expands it.
     *
     * @param newMessage The new message.
     */
    public void addMessage(Message newMessage) {
        messages.add(newMessage);
        expandedStates.put(newMessage.getMessageId(), true);
        notifyItemInserted(messages.size() - 1);
    }

    /**
     * Updates a message at a specific position.
     *
     * @param position The position in the list.
     * @param message The updated message.
     */
    public void updateItem(int position, Message message) {
        try {
            messages.set(position, message);
            notifyItemChanged(position);
        } catch (Exception e) {
            Log.e("Listener", e.toString());
        }
    }

    /**
     * Removes the message at the given position.
     *
     * @param position The position of the message in the list.
     */
    public void removeItem(int position) {
        if (position >= 0 && position < messages.size()) {
            Message removedMessage = messages.get(position);
            messages.remove(position);
            expandedStates.remove(removedMessage.getMessageId());
            notifyItemRemoved(position);
        }
    }
}