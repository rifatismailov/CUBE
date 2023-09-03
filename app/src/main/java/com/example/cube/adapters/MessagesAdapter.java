package com.example.cube.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.cube.emoji.DialogEmojiOne;
import com.example.cube.models.Message;
import com.example.cube.R;
import com.example.cube.holder.ReceiverViewHolder;
import com.example.cube.holder.SentViewHolder;
import com.example.emoji.emoji.MyEmoji;
import com.example.emoji.models.EmojiMany;
import com.example.textvisualization.visualization.Watcher;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter  {

    Context context;
    ArrayList<Message> messages;
    List<EmojiMany> manyReaction = MyEmoji.getManyReaction();

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;
    final int DELETE=3;//влияет на скорлинг сообшения если менше 3 то начинает тормозит
    boolean show = false;
    RecyclerView.ViewHolder holder;

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

        this.holder=holder;
        int[] reaction = manyReaction.get(message.getEmojisPosition()).getManySubject();
        if (holder.getClass().equals(SentViewHolder.class)) {
            SentViewHolder viewHolder = (SentViewHolder) holder;
            viewHolder.binding.message.addTextChangedListener(new Watcher((Activity) context));
            viewHolder.binding.recyclerView2.setVisibility(View.GONE);

            /***/
            if (message.getCheck().equals(Check.ImageNoText) && message.getMessage().isEmpty()) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                //viewHolder.binding.image.setImageURI(Uri.parse(message.getImageUrl()));
                //Picasso.with(context).load(new File(message.getImageUrl())).into(viewHolder.binding.image);
                Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
                viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth() / DELETE, message.getImageHeight() / DELETE, false));
            } else if (message.getCheck().equals(Check.ImageAndText) && !message.getMessage().isEmpty()) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.VISIBLE);
                //viewHolder.binding.image.setImageURI(Uri.parse(message.getImageUrl()));
                //Picasso.with(context).load(new File(message.getImageUrl())).into(viewHolder.binding.image);
                Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
                viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth() / DELETE, message.getImageHeight() / DELETE, false));
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
                    new DialogEmojiOne(context, x, y, message, holder);

                }
            });
            viewHolder.binding.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] location = new int[2];
                    viewHolder.binding.message.getLocationOnScreen(location);
                    int x = location[0];
                    int y = location[1];
                    new DialogEmojiOne(context, x, y, message, holder);

                }
            });


        } else {

            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.binding.message.addTextChangedListener(new Watcher((Activity) context));

            viewHolder.binding.recyclerView2.setVisibility(View.GONE);

            if (message.getCheck().equals(Check.ImageNoText) && message.getMessage().isEmpty()) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                //viewHolder.binding.image.setImageURI(Uri.parse(message.getImageUrl()));
                //Picasso.with(context).load(new File(message.getImageUrl())).into(viewHolder.binding.image);
                Picasso.with(context).cancelRequest(viewHolder.binding.image);
                Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
                viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth() / DELETE, message.getImageHeight() / DELETE, false));
            } else if (message.getCheck().equals(Check.ImageAndText) && !message.getMessage().isEmpty()) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.VISIBLE);
                //viewHolder.binding.image.setImageURI(Uri.parse(message.getImageUrl()));
                //Picasso.with(context).load(new File(message.getImageUrl())).into(viewHolder.binding.image);
                Picasso.with(context).cancelRequest(viewHolder.binding.image);

                Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
                viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth() / DELETE, message.getImageHeight() / DELETE, false));

            } else {
                viewHolder.binding.image.setVisibility(View.GONE);
                viewHolder.binding.message.setVisibility(View.VISIBLE);
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
                    new DialogEmojiOne(context,x, y, message, holder);

                }
            });
            viewHolder.binding.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] location = new int[2];
                    viewHolder.binding.message.getLocationOnScreen(location);
                    int x = location[0];
                    int y = location[1];
                    new DialogEmojiOne(context,x, y, message, holder);

                }
            });


        }
    }



    @Override
    public int getItemCount() {
        return messages.size();
    }


    }
