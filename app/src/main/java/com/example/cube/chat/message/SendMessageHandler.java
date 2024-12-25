package com.example.cube.chat.message;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.example.cube.control.Check;
import com.example.cube.holder.SentViewHolder;
import com.example.textvisualization.visualization.Watcher;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.Arrays;
import java.util.List;

public class SendMessageHandler {
    private Context context;

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
            viewHolder.binding.aboutFile.setVisibility(View.GONE);
            Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
            if (message.getImageWidth() > 2000)
                viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth() / 4, message.getImageHeight() / 4, false));
            else
                viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth() / 2, message.getImageHeight() / 2, false));
            viewHolder.binding.message.setText(message.getMessage());
        } else if (message.getCheck().equals(Check.Image) && message.getMessage().isEmpty()) {
            viewHolder.binding.image.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setVisibility(View.GONE);
            viewHolder.binding.file.setVisibility(View.GONE);
            viewHolder.binding.aboutFile.setVisibility(View.GONE);

            Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);

            if (message.getImageWidth() > 2000)
                viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth() / 4, message.getImageHeight() / 4, false));
            else
                viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth() / 2, message.getImageHeight() / 2, false));
        } else if (message.getCheck().equals(Check.File)) {
            viewHolder.binding.file.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setVisibility(View.VISIBLE);
            viewHolder.binding.image.setVisibility(View.VISIBLE);

            if (message.getImage() != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
                viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth(), message.getImageHeight(), false));
            }
            viewHolder.binding.fileHash.setText(message.getHas());
            viewHolder.binding.fileType.setText(message.getTypeFile());
            viewHolder.binding.fileSize.setText(message.getFileSize());
            viewHolder.binding.fileDateCreate.setText(message.getDataCreate());
            viewHolder.binding.file.setText(message.getFileName());
            viewHolder.binding.message.setText(message.getMessage());
        } else {
            viewHolder.binding.image.setVisibility(View.GONE);
            viewHolder.binding.file.setVisibility(View.GONE);
            viewHolder.binding.aboutFile.setVisibility(View.GONE);

            viewHolder.binding.message.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setText(message.getMessage());
        }

        // Оновлюємо messageNotifier
        if (message.getMessageStatus() != null && message.getMessageStatus().equals("server")) {
            List<String> hashes = Arrays.asList("abcdef123456", "123456abcdef");
            viewHolder.binding.messageNotifier.setVisibility(View.VISIBLE);
            viewHolder.binding.messageNotifier.setHashes(hashes);
            viewHolder.binding.feeling.setVisibility(View.GONE);
            ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, 58f) // Верхній лівий кут
                    .setTopRightCorner(CornerFamily.ROUNDED, 0f) // Верхній правий кут
                    .setBottomLeftCorner(CornerFamily.ROUNDED, 10f)  // Нижній лівий кут (прямий)
                    .setBottomRightCorner(CornerFamily.ROUNDED, 10f) // Нижній правий кут (прямий)
                    .build();
            viewHolder.binding.image.setShapeAppearanceModel(shapeAppearanceModel);
            // viewHolder.binding.cardView.setCar
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
        viewHolder.binding.messageNotifier.setProgressRadius(30);
        viewHolder.binding.time.setText(message.getTimestamp());
        if (message.getProgress() == 100) {
            viewHolder.binding.time.setText(message.getTimestamp());
            List<String> hashes = Arrays.asList("d3a523", "123456abcdef");

            viewHolder.binding.messageNotifier.setHashes(hashes);

            viewHolder.binding.messageNotifier.setProgress(0);
        } else {
            viewHolder.binding.messageNotifier.setProgress(message.getProgress());
        }

        new ClickListeners().setClickListeners(context, viewHolder, message);

    }
}
