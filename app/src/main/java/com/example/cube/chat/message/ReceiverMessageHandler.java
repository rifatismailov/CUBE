package com.example.cube.chat.message;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.example.cube.R;
import com.example.cube.control.Check;
import com.example.cube.holder.ReceiverViewHolder;
import com.example.textvisualization.visualization.Watcher;

public class ReceiverMessageHandler {
    private Context context;
    final int DELETE = 2;
    private ReceiverViewHolder viewHolder;

    public ReceiverMessageHandler(Context context) {
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    public void setMessage(ReceiverViewHolder viewHolder, Message message) {
        viewHolder.binding.message.addTextChangedListener(new Watcher((Activity) context));
        this.viewHolder = viewHolder;

        viewHolder.binding.message.addTextChangedListener(new Watcher((Activity) context));
        if (message.getCheck().equals(Check.Image) && !message.getMessage().isEmpty()) {
            viewHolder.binding.image.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setVisibility(View.VISIBLE);
            viewHolder.binding.file.setVisibility(View.GONE);
            Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
            viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth() / DELETE, message.getImageHeight() / DELETE, false));
            viewHolder.binding.message.setText(message.getMessage());
        } else if (message.getCheck().equals(Check.Image) && message.getMessage().isEmpty()) {
            viewHolder.binding.image.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setVisibility(View.GONE);
            viewHolder.binding.file.setVisibility(View.GONE);
            Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
            viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth() / DELETE, message.getImageHeight() / DELETE, false));
        } else if (message.getCheck().equals(Check.File) && !message.getUrl().toString().isEmpty()) {
            viewHolder.binding.file.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setVisibility(View.VISIBLE);
            viewHolder.binding.image.setVisibility(View.GONE);
            viewHolder.binding.file.setImageResource(R.drawable.ic_file_hex);
            viewHolder.binding.message.setText(message.getUrl().toString() + "\n" + message.getMessage());

        } else {
            viewHolder.binding.image.setVisibility(View.GONE);
            viewHolder.binding.file.setVisibility(View.GONE);
            viewHolder.binding.message.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setText(message.getMessage());
        }
        if (message.getFeeling() >= 0) {
            viewHolder.binding.feeling.setImageResource(message.getFeeling());
            viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);
            viewHolder.binding.feeling.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.feelLayout.setVisibility(View.GONE);
            viewHolder.binding.feeling.setVisibility(View.GONE);
        }


        new ClickListeners().setClickListeners(context, viewHolder, message);
    }
}
