package com.example.cube.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.databinding.ItemNewsBinding;

public class NewsViewHolder extends RecyclerView.ViewHolder {
    public ItemNewsBinding binding;

    public NewsViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemNewsBinding.bind(itemView);
    }
}