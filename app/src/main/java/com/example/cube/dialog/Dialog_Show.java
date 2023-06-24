package com.example.cube.dialog;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.R;
import com.example.cube.adapters.EmojiAdapter;
import com.example.cube.adapters.ManyEmojiAdapter;
import com.example.cube.emoji.MyEmoji;
import com.example.cube.models.Dialog;
import com.example.cube.models.EmojiMany;
import com.example.cube.models.EmojiOn;
import com.example.cube.models.Message;

import java.util.ArrayList;
import java.util.List;

public class Dialog_Show {
    List<EmojiMany> manyReaction = MyEmoji.getManyReaction();

    public void DialogOn(Context context, int x, int y, Message message, RecyclerView.ViewHolder holder) {
        Dialog dialog = new Dialog(context, R.layout.dialog_emoji, R.style.DialogAnimation, x, y);
        View show_emoji = dialog.getLinearlayout().findViewById(R.id.show_emoji);
        RecyclerView Emoji = dialog.getLinearlayout().findViewById(R.id.recyclerEmoji);
        ArrayList<EmojiOn> subjectArrayList = new ArrayList<>();
        for (int reaction : manyReaction.get(0).getManySubject()) {
            subjectArrayList.add(new EmojiOn(reaction));
        }

        Emoji.setAdapter(new EmojiAdapter(message, holder, subjectArrayList, dialog.getAlertDialog(), 0));
        Emoji.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));

        show_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.getAlertDialog().cancel();
                new Dialog_Show().Dialog_Many(context, x, y, message, holder);
            }
        });
    }

    public void Dialog_Many(Context context, int x, int y, Message message, RecyclerView.ViewHolder holder) {
        Dialog dialog = new Dialog(context, R.layout.dialog_many_emoji, R.style.DialogAnimation, x, y);
        RecyclerView Many_Emoji = dialog.getLinearlayout().findViewById(R.id.show_many_emoji);
        View close_emoji = dialog.getLinearlayout().findViewById(R.id.close_emoji);
        /** manyReaction содержит массив из емодж их 4*/
        Many_Emoji.setAdapter(new ManyEmojiAdapter(context, message, holder, manyReaction, dialog.getAlertDialog()));
        Many_Emoji.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

        close_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.getAlertDialog().cancel();
                new Dialog_Show().DialogOn(context, x, y, message, holder);
            }
        });
    }
}