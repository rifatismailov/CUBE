package com.example.cube.chat.message;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.holder.ReceiverViewHolder;
import com.example.cube.holder.SentViewHolder;
import com.example.emoji.emoji.MyEmoji;
import com.example.folder.download.Downloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class ClickListeners {

    void setClickListeners(Context context, RecyclerView.ViewHolder holder, Message message) {
        int position = holder.getLayoutPosition();
        //List<EmojiMany> manyReaction = MyEmoji.getManyReaction();
        int[] reaction = MyEmoji.emoji_blue;

        if (holder.getClass().equals(SentViewHolder.class)) {
            SentViewHolder viewHolder = (SentViewHolder) holder;
            viewHolder.binding.message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] location = new int[2];
                    viewHolder.binding.message.getLocationOnScreen(location);
                    int x = location[0];
                    int y = location[1];
                    message.setFeeling(reaction[0]);
                    int emoji = message.getFeeling();
                  //  viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);
                  //  viewHolder.binding.feeling.setImageResource(emoji);
                  //  viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                }
            });

            viewHolder.binding.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] location = new int[2];
                    viewHolder.binding.image.getLocationOnScreen(location);
                    int x = location[0];
                    int y = location[1];
                    message.setFeeling(reaction[0]);
                    int emoji = message.getFeeling();
                  //  viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);
                  //  viewHolder.binding.feeling.setImageResource(emoji);
                  //  viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                }
            });
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.binding.message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] location = new int[2];
                    viewHolder.binding.message.getLocationOnScreen(location);
                    int x = location[0];
                    int y = location[1];
                    message.setFeeling(reaction[0]);
                    int emoji = message.getFeeling();
                   // viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);
                   // viewHolder.binding.feeling.setImageResource(emoji);
                   // viewHolder.binding.feeling.setVisibility(View.VISIBLE);

                }
            });

            viewHolder.binding.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] location = new int[2];
                    viewHolder.binding.image.getLocationOnScreen(location);
                    int x = location[0];
                    int y = location[1];
                    message.setFeeling(reaction[0]);
                    int emoji = message.getFeeling();
                   // viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);
                   // viewHolder.binding.feeling.setImageResource(emoji);
                   // viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                }
            });
            viewHolder.binding.file.setOnClickListener(v -> {
                if (!message.getUrl().toString().startsWith("http")) {
                    if (new File(message.getUrl().toString()).exists() && new File(message.getUrl().toString()).isFile()) {
//                        URL url = null; // Змініть IP на ваш
//                        try {
//                            url = new URL(new File(message.getUrl().toString()).getName());
//                            Log.e("Listener",url.toString());
//                        } catch (MalformedURLException e) {
//                            throw new RuntimeException(e);
//                        }
//                        new Open(context,url,position);
                        Toast.makeText(context, "[ " + message.getUrl().toString() + " ] " + position, Toast.LENGTH_LONG).show();
                    }
                    //відкриваємо за допомогою додатку для файлу
                } else {
                    try {
                        /*По факту ми отримаємо назву файлу так як їм на дуже буде зручно оперувати
                         * Ми вже знаємо на який сервер звертатися та по якоми параметру */
                        //Toast.makeText(context, "[ " + message.getUrl().toString() + " ] " + position, Toast.LENGTH_LONG).show();
                        URL url = new URL(message.getUrl().toString()); // Змініть IP на ваш
                        Log.e("Listener",url.toString());

                        new Downloader(context, url,position,message.getMessageId());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

    }
}
