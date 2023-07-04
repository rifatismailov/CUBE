package com.example.cube.emoji;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.cube.R;
import com.example.cube.models.Message;
import com.example.emoji.emoji.MyEmoji;
import com.example.emoji.models.EmojiMany;
import com.example.emoji.models.EmojiOn;

import java.util.ArrayList;
import java.util.List;

public class DialogEmojiOne {
    List<EmojiMany> manyReaction = MyEmoji.getManyReaction();

    public DialogEmojiOne(Context context, int x, int y, Message message, RecyclerView.ViewHolder holder) {
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
                new DialogEmojiMany(context, x, y, message, holder);
            }
        });
    }


}