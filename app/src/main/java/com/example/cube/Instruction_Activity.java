package com.example.cube;

import android.os.Bundle;

import com.example.cube.adapters.InstructionAdapter;
import com.example.cube.adapters.MessagesAdapter;
import com.example.cube.control.Check;
import com.example.cube.control.Side;
import com.example.cube.models.Instruction;
import com.example.cube.models.Message;
import com.example.cube.models.News;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cube.databinding.ActivityIinstructionBinding;

import java.util.ArrayList;

public class Instruction_Activity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityIinstructionBinding binding;
    InstructionAdapter adapter;
    ArrayList<Instruction> instructions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityIinstructionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        instructions = new ArrayList<>();

        adapter = new InstructionAdapter(this, instructions);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setInstructions("Cisco Systems, Inc","Cisco Systems, Inc. — американська транснаціональна корпорація, " +
                        "яка є найбільшим у світі виробником мережевого обладнання, призначеного для обслуговування мереж віддаленого доступу, " +
                        "сервісів безпеки, мереж зберігання даних, маршрутизації та комутації, а також для потреб комерційного ринку ");
            }
        });
    }
    private void setInstructions(String equipment, String information) {
        instructions.add(new Instruction(equipment, information));
        //binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
        binding.recyclerView.setLayoutManager(reverseLayout());
        adapter.notifyDataSetChanged();

    }

    /**
     * Method for reverse recycler View
     */
    public LinearLayoutManager reverseLayout() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        return linearLayoutManager;
    }

}