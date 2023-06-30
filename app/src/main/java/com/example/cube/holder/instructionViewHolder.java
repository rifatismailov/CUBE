package com.example.cube.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.databinding.ItemInstructionBinding;
import com.example.cube.databinding.ItemNewsBinding;

public class instructionViewHolder extends RecyclerView.ViewHolder {
    public ItemInstructionBinding binding;

    public instructionViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemInstructionBinding.bind(itemView);
    }
}