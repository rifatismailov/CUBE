package com.example.cube.chat.message;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.example.cube.R;
import com.example.cube.control.Check;
import com.example.cube.holder.SentViewHolder;
import com.example.textvisualization.visualization.Watcher;

import java.util.Arrays;
import java.util.List;

public class SendMessageHandler {
    private Context context;
    final int DELETE = 2;

    public SendMessageHandler(Context context) {
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    public void setMessage(SentViewHolder viewHolder, Message message) {
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

// Оновлюємо messageNotifier
        if (message.getMessageStatus() != null && message.getMessageStatus().equals("server")) {
            List<String> hashes = Arrays.asList("abcdef123456", "123456abcdef");
            viewHolder.binding.messageNotifier.setVisibility(View.VISIBLE);
            viewHolder.binding.messageNotifier.setHashes(hashes);
            viewHolder.binding.feeling.setVisibility(View.GONE);
        } else if (message.getFeeling() >= 0) {
            viewHolder.binding.feeling.setImageResource(message.getFeeling());
            viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            viewHolder.binding.messageNotifier.setVisibility(View.GONE);
        } else {
            viewHolder.binding.feeling.setVisibility(View.GONE);
            viewHolder.binding.messageNotifier.setVisibility(View.GONE);
        }

// Загальне налаштування feelLayout
        if (message.getFeeling() >= 0 || (message.getMessageStatus() != null && message.getMessageStatus().equals("server"))) {
            viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.feelLayout.setVisibility(View.GONE);
        }


        new ClickListeners().setClickListeners(context, viewHolder, message);

    }
}
