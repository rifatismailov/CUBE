package com.example.cube.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.databinding.ItemReceiveBinding;

public class ReceiverViewHolder extends RecyclerView.ViewHolder {
    public ItemReceiveBinding binding;

    public ReceiverViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemReceiveBinding.bind(itemView);
    }
}