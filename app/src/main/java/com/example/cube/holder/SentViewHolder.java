package com.example.cube.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.databinding.ItemSentBinding;

public class SentViewHolder extends RecyclerView.ViewHolder {
    public ItemSentBinding binding;

    public SentViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemSentBinding.bind(itemView);
    }
}