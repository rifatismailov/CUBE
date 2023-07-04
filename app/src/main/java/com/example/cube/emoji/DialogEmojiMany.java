package com.example.cube.emoji;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.R;
import com.example.cube.models.Message;
import com.example.emoji.emoji.MyEmoji;
import com.example.emoji.models.EmojiMany;

import java.util.List;

public class DialogEmojiMany {
    List<EmojiMany> manyReaction = MyEmoji.getManyReaction();

    public DialogEmojiMany(Context context, int x, int y, Message message, RecyclerView.ViewHolder holder) {
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
                new DialogEmojiOne(context, x, y, message, holder);
            }
        });
    }
}
