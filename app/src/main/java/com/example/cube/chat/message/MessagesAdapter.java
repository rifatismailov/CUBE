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
 * Адаптер для відображення списку повідомлень у RecyclerView.
 * Відповідає за створення та оновлення елементів списку, а також за їхній вигляд.
 */
public class MessagesAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<Message> messages;
    private HashMap<String, Boolean> expandedStates = new HashMap<>(); // Сховище стану розкриття повідомлень

    private static final int ITEM_SENT = 1;
    private static final int ITEM_RECEIVE = 2;

    /**
     * Конструктор адаптера.
     *
     * @param context  Контекст активності чату.
     * @param messages Список повідомлень.
     */
    public MessagesAdapter(ChatActivity context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    /**
     * Оновлює список повідомлень з використанням DiffUtil для оптимізації оновлень.
     *
     * @param newMessages Новий список повідомлень.
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
        String messageId = message.getMessageId(); // Отримуємо унікальний ідентифікатор повідомлення

        if (holder instanceof SentViewHolder) {
            SentViewHolder viewHolder = (SentViewHolder) holder;
            new SendMessageHandler(context).setMessage(viewHolder, message);

            // Переконайтеся, що всі поля встановлені
            viewHolder.binding.message.setText(message.getMessage() != null ? message.getMessage() : "");
            viewHolder.binding.file.setText(message.getFileName() != null ? message.getFileName() : "");

            // Встановлюємо видимість полів
            viewHolder.binding.messageLayout.setVisibility(message.getMessage() != null ? View.VISIBLE : View.GONE);
            viewHolder.binding.fileLayout.setVisibility(message.getFileName() != null ? View.VISIBLE : View.GONE);

        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            new ReceiverMessageHandler(context).setMessage(viewHolder, message);
            // Переконайтеся, що всі поля встановлені
            viewHolder.binding.message.setText(message.getMessage() != null ? message.getMessage() : "");
            // Встановлюємо видимість полів
            viewHolder.binding.messageLayout.setVisibility(message.getMessage() != null ? View.VISIBLE : View.GONE);
            new ReceiverMessageHandler(context).setMessage(viewHolder, message);

        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }


    /**
     * Додає нове повідомлення та автоматично розгортає його.
     *
     * @param newMessage Нове повідомлення.
     */
    public void addMessage(Message newMessage) {
        messages.add(newMessage);
        expandedStates.put(newMessage.getMessageId(), true);
        notifyItemInserted(messages.size() - 1);
    }

    /**
     * Оновлює повідомлення на певній позиції.
     *
     * @param position Позиція в списку.
     * @param message  Оновлене повідомлення.
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
     * Видаляє повідомлення на заданій позиції.
     *
     * @param position Позиція повідомлення у списку.
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
