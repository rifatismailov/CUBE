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
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.Arrays;
import java.util.List;

public class ReceiverMessageHandler {
    private final Context context;

    public ReceiverMessageHandler(Context context) {
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    public void setMessage(ReceiverViewHolder viewHolder, Message message) {
        // Перевірка на наявність Activity
        if (context instanceof Activity) {
            viewHolder.binding.message.addTextChangedListener(new Watcher((Activity) context));
        }

        // Обробка повідомлення
        if (message.getCheck().equals(Check.Image)) {
            handleImageMessage(viewHolder, message);
        } else if (message.getCheck().equals(Check.File)) {
            handleFileMessage(viewHolder, message);
        } else {
            handleTextMessage(viewHolder, message);
        }

        // Загальне налаштування feelLayout
        configureFeelLayout(viewHolder, message);

        // Налаштування прогрес-бара
        configureMessageNotifier(viewHolder, message);

        // Додавання обробників кліків
        new ClickListeners().setClickListeners(context, viewHolder, message);
    }

    private void handleImageMessage(ReceiverViewHolder viewHolder, Message message) {
        Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
        setImage(bmp, message.getImageWidth(), message.getImageHeight(), viewHolder.binding.image);

        viewHolder.binding.image.setVisibility(View.VISIBLE);
        viewHolder.binding.file.setVisibility(View.GONE);
        viewHolder.binding.aboutFile.setVisibility(View.GONE);

        if (!message.getMessage().isEmpty()) {
            viewHolder.binding.message.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setText(message.getMessage());
        } else {
            viewHolder.binding.message.setVisibility(View.GONE);
        }
    }

    private void handleFileMessage(ReceiverViewHolder viewHolder, Message message) {
        ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel()
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 0f)
                .setTopRightCorner(CornerFamily.ROUNDED, 10f)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 10f)
                .setBottomRightCorner(CornerFamily.ROUNDED, 10f)
                .build();

        viewHolder.binding.image.setShapeAppearanceModel(shapeAppearanceModel);

        if (message.getImage() != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
            viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth(), message.getImageHeight(), false));
        }
        viewHolder.binding.image.setVisibility(View.VISIBLE);
        viewHolder.binding.file.setVisibility(View.VISIBLE);
        viewHolder.binding.aboutFile.setVisibility(View.VISIBLE);

        viewHolder.binding.fileHash.setText(message.getHas());
        viewHolder.binding.fileType.setText(message.getTypeFile());
        viewHolder.binding.fileSize.setText(message.getFileSize());
        viewHolder.binding.fileDateCreate.setText(message.getDataCreate());
        viewHolder.binding.file.setText(message.getFileName());

        if (message.getMessage() != null && !message.getMessage().isEmpty()) {
            viewHolder.binding.message.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setText(message.getMessage());
        } else {
            viewHolder.binding.message.setVisibility(View.GONE);
        }
    }

    private void handleTextMessage(ReceiverViewHolder viewHolder, Message message) {
        viewHolder.binding.image.setVisibility(View.GONE);
        viewHolder.binding.file.setVisibility(View.GONE);
        viewHolder.binding.aboutFile.setVisibility(View.GONE);

        viewHolder.binding.message.setVisibility(View.VISIBLE);
        viewHolder.binding.message.setText(message.getMessage());
    }

    private void configureFeelLayout(ReceiverViewHolder viewHolder, Message message) {
        if (message.getFeeling() >= 0 || message.getProgress() > 0 || message.getTimestamp() != null) {
            viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);
            if (message.getTimestamp() != null) {
                String[] time = message.getTimestamp().split(" ");
                viewHolder.binding.time.setText(time[1]);
            }
        } else {
            viewHolder.binding.feelLayout.setVisibility(View.GONE);
        }
    }

    private void configureMessageNotifier(ReceiverViewHolder viewHolder, Message message) {
        viewHolder.binding.messageNotifier.setProgressRadius(30);

        List<String> hashes = getHashes(message.getProgress());
        viewHolder.binding.messageNotifier.setHashes(hashes);
        viewHolder.binding.messageNotifier.setProgress(
                message.getProgress() == 100 ? 0 : message.getProgress()
        );
    }

    private List<String> getHashes(int progress) {
        return progress == 100
                ? Arrays.asList("d3a523", "123456abcdef")
                : Arrays.asList("abcdef123456", "123456abcdef");
    }

    private void setImage(Bitmap image, int width, int height, View imageView) {
        int scaleFactor = (width > 2000) ? 4 : 2;
        ((android.widget.ImageView) imageView).setImageBitmap(
                Bitmap.createScaledBitmap(image, width / scaleFactor, height / scaleFactor, false)
        );
    }
}
