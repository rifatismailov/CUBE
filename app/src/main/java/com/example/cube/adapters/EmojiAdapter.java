package com.example.cube.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.models.EmojiMany;
import com.example.cube.models.EmojiOn;
import com.example.cube.models.Message;
import com.example.cube.R;
import com.example.cube.emoji.MyEmoji;
import com.example.cube.holder.ReceiverViewHolder;
import com.example.cube.holder.SentViewHolder;

import java.util.List;

public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {

    List<EmojiOn> list;
    RecyclerView.ViewHolder OnHolder;
    Message message;
    AlertDialog alertDialog;
    int emojisPosition;

    List<EmojiMany> manyReaction = MyEmoji.getManyReaction();
    public EmojiAdapter(Message message, RecyclerView.ViewHolder OnHolder, List<EmojiOn> list, AlertDialog alertDialog, int emojisPosition) {
        this.message = message;
        this.OnHolder = OnHolder;
        this.list = list;
        this.alertDialog = alertDialog;
        this.emojisPosition = emojisPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View contactView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emoji, parent, false);
        ViewHolder OnHolder = new ViewHolder(contactView);
        return OnHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        EmojiOn currentItem = list.get(position);
        int[] reaction = manyReaction.get(emojisPosition).getManySubject();
        if (holder.getClass() == ViewHolder.class) {
            // Setting views with the corresponding data
            ImageView imageView = holder.ImageView;
            imageView.setImageResource(currentItem.getImageId());
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (OnHolder.getClass().equals(SentViewHolder.class)) {
                        SentViewHolder viewHolder = (SentViewHolder) OnHolder;
                        viewHolder.binding.feel.setVisibility(View.VISIBLE);
                        viewHolder.binding.feeling.setImageResource(reaction[position]);
                        viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                        viewHolder.binding.recyclerView2.setVisibility(View.GONE);

                    } else {
                        ReceiverViewHolder viewHolder = (ReceiverViewHolder) OnHolder;
                        viewHolder.binding.feel.setVisibility(View.VISIBLE);
                        viewHolder.binding.feeling.setImageResource(reaction[position]);
                        viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                        viewHolder.binding.recyclerView2.setVisibility(View.GONE);

                    }
                    message.setFeeling(position);
                    message.setEmojisPosition(emojisPosition);
                    alertDialog.cancel();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ImageView = itemView.findViewById(R.id.show_emoji);
        }
    }
}
