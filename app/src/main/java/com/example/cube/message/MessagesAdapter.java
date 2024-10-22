package com.example.cube.message;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.ChatActivity;
import com.example.cube.control.Side;
import com.example.cube.R;
import com.example.cube.holder.ReceiverViewHolder;
import com.example.cube.holder.SentViewHolder;

import java.util.ArrayList;


public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Message> messages;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    RecyclerView.ViewHolder holder;

    public MessagesAdapter(ChatActivity context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
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
        if (message.getSide().equals(Side.Sender)) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        this.holder = holder;
        if (holder.getClass().equals(SentViewHolder.class)) {
            SentViewHolder viewHolder = (SentViewHolder) holder;
            new SendMessageHandler(context).setMessage((SentViewHolder) viewHolder, message);
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            new ReceiverMessageHandler(context).setMessage((ReceiverViewHolder) viewHolder, message);
        }
    }
    // Метод для оновлення конкретної позиції
    public void updateItem(int position, Message message) {
        messages.set(position, message);
        notifyItemChanged(position);
        notifyDataSetChanged();// Оновлюємо конкретну позицію
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}