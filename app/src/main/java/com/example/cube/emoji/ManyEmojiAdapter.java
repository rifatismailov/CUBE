package com.example.cube.emoji;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.cube.R;
import com.example.cube.models.Message;
import com.example.emoji.models.EmojiMany;
import com.example.emoji.models.EmojiOn;

import java.util.ArrayList;
import java.util.List;

public     class ManyEmojiAdapter extends RecyclerView.Adapter<ManyEmojiAdapter.ViewHolder> {
    List<EmojiMany> manySubjects;
    RecyclerView.ViewHolder OnHolder;
    Message message;
    AlertDialog alertDialog;
    Context context;

    public ManyEmojiAdapter(Context context, Message message, RecyclerView.ViewHolder OnHolder, List<EmojiMany> manySubjects, AlertDialog alertDialog) {
        this.context=context;
        this.message = message;
        this.OnHolder = OnHolder;
        this.manySubjects = manySubjects;
        this.alertDialog = alertDialog;

    }

    @NonNull
    @Override
    public ManyEmojiAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View contactView = LayoutInflater.from(parent.getContext()).inflate(R.layout.iteam_many_emoji, parent, false);
        ManyEmojiAdapter.ViewHolder OnHolder = new ManyEmojiAdapter.ViewHolder(contactView);
        return OnHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ManyEmojiAdapter.ViewHolder holder, int position) {

        int[] array = manySubjects.get(position).getManySubject();
        RecyclerView Emoji = holder.recyclerView;
        ArrayList<EmojiOn> subjectArrayList = new ArrayList<>();
        for (int reaction : array) {
            subjectArrayList.add(new EmojiOn(reaction));
        }

        Emoji.setAdapter(new EmojiAdapter(message, OnHolder, subjectArrayList, alertDialog, position));
        Emoji.setLayoutManager(new GridLayoutManager(context, 3, LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    public int getItemCount() {
        return manySubjects.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RecyclerView recyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.show_many_emoji);

        }
    }
}