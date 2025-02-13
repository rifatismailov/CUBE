package com.example.cube.log;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.databinding.ItemLogBinding;

public class NotificationViewHolder extends RecyclerView.ViewHolder {
    public ItemLogBinding binding;

    public NotificationViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemLogBinding.bind(itemView);
    }
}
