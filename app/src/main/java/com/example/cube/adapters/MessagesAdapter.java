package com.example.cube.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.ChatActivity;
import com.example.cube.control.Check;
import com.example.cube.control.Side;
import com.example.cube.dialog.Dialog_Show;
import com.example.cube.models.EmojiMany;
import com.example.cube.models.Message;
import com.example.cube.R;
import com.example.cube.emoji.MyEmoji;
import com.example.cube.holder.ReceiverViewHolder;
import com.example.cube.holder.SentViewHolder;
import com.example.cube.visualization.Watcher;


import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter {
    //https://icon-icons.com/ru/pack/Fluent-solid-(20px)/3249&page=1
    ///https://icon-icons.com/ru/pack/Heroicons-(Outline)/2551
    //https://icon-icons.com/ru/users/G0p1UTxX8cVjHAiG0VWCQ/icon-sets/
    //https://icon-icons.com/ru/users/gX1mWoxgWPZP2J3536YS4/icon-sets/
    //https://icon-icons.com/ru/users/yt6qUYDYtOMSe2XAcbw0m/icon-sets/


    Context context;
    ArrayList<Message> messages;
    List<EmojiMany> manyReaction = MyEmoji.getManyReaction();

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;
    boolean show = false;


    public MessagesAdapter(ChatActivity context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSide().equals(Side.Sender)) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Message message = messages.get(position);
        int[] reaction = manyReaction.get(message.getEmojisPosition()).getManySubject();


        if (holder.getClass().equals(SentViewHolder.class)) {
            SentViewHolder viewHolder = (SentViewHolder) holder;
            viewHolder.binding.message.addTextChangedListener(new Watcher((Activity) context));
            viewHolder.binding.recyclerView2.setVisibility(View.GONE);

            /***/
            if (message.getCheck().equals(Check.Image)&& message.getMessage().isEmpty()) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.image.setImageURI(Uri.parse(message.getImageUrl()));
            } else if (message.getCheck().equals(Check.Image) && !message.getMessage().isEmpty()) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.VISIBLE);
                viewHolder.binding.image.setImageURI(Uri.parse(message.getImageUrl()));
            } else {
                viewHolder.binding.image.setVisibility(View.GONE);
                viewHolder.binding.message.setVisibility(View.VISIBLE);
            }

            viewHolder.binding.message.setText(message.getMessage());
            if (message.getFeeling() >= 0) {
                viewHolder.binding.feel.setVisibility(View.VISIBLE);
                viewHolder.binding.feeling.setImageResource(reaction[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feel.setVisibility(View.GONE);
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            viewHolder.binding.message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] location = new int[2];
                    viewHolder.binding.message.getLocationOnScreen(location);
                    int x = location[0];
                    int y = location[1];
                    new Dialog_Show().DialogOn(context, x, y, message, holder);
                }
            });
            viewHolder.binding.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] location = new int[2];
                    viewHolder.binding.message.getLocationOnScreen(location);
                    int x = location[0];
                    int y = location[1];
                    new Dialog_Show().DialogOn(context, x, y, message, holder);
                }
            });
        } else {

            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.binding.message.addTextChangedListener(new Watcher((Activity) context));

            viewHolder.binding.recyclerView2.setVisibility(View.GONE);

            if (message.getMessage().equals("photo")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.image.setImageURI(Uri.parse(message.getImageUrl()));
            }

            if (!show) viewHolder.binding.recyclerView2.setVisibility(View.GONE);
            viewHolder.binding.message.setText(message.getMessage());

            if (message.getFeeling() >= 0) {
                viewHolder.binding.feel.setVisibility(View.VISIBLE);
                viewHolder.binding.feeling.setImageResource(reaction[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feel.setVisibility(View.GONE);
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            viewHolder.binding.message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] location = new int[2];
                    viewHolder.binding.message.getLocationOnScreen(location);
                    int x = location[0];
                    int y = location[1];
                    new Dialog_Show().DialogOn(context, x, y, message, holder);
                }
            });

        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    //public class SentViewHolder


   /* public class Dialog_Show {
        public void DialogOn(int x, int y, Message message, RecyclerView.ViewHolder holder) {
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
                    new Dialog_Show().Dialog_Many(x, y, message, holder);
                }
            });
        }


            Many_Emoji.setAdapter(new ManyEmojiAdapter(context, message, holder, manyReaction, dialog.getAlertDialog()));
            Many_Emoji.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

            close_emoji.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.getAlertDialog().cancel();
                    new Dialog_Show().DialogOn(x, y, message, holder);
                }
            });
        }
    }
    */


    //  class ManyEmojiAdapter
}
