package com.example.cube.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.chat.message.Message;
import com.example.cube.databinding.ItemReceiveBinding;

public class ReceiverViewHolder extends RecyclerView.ViewHolder {
    public ItemReceiveBinding binding;
    private Message currentMessage;

    public ReceiverViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemReceiveBinding.bind(itemView);
    }


    public Message getCurrentMessage() {
        return currentMessage;
    }

    public void setCurrentMessage(Message message) {
        this.currentMessage = message;
    }

}