package com.example.cube.adapters;

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
import com.example.cube.R;
import com.example.cube.holder.NewsViewHolder;
import com.example.cube.holder.instructionViewHolder;
import com.example.cube.models.Instruction;
import com.example.cube.visualization.Watcher;
import java.util.ArrayList;

public class InstructionAdapter extends RecyclerView.Adapter {


    Context context;
    ArrayList<Instruction> instructions;

    public InstructionAdapter(Context context, ArrayList<Instruction> instructions) {
        this.context = context;
        this.instructions = instructions;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_instruction, parent, false);
            return new instructionViewHolder(view);
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Instruction instruction = instructions.get(position);
        instructionViewHolder viewHolder = (instructionViewHolder) holder;
        viewHolder.binding.information.setVisibility(View.VISIBLE);
        viewHolder.binding.information.setVisibility(View.VISIBLE);
        viewHolder.binding.information.addTextChangedListener(new Watcher((Activity) context));
        viewHolder.binding.equipmentName.setText(instruction.getEquipment());
        viewHolder.binding.information.setText(instruction.getInformation());

    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }

}
