package com.example.cube.chat.message;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.example.cube.chat.holder.ReceiverViewHolder;
import com.example.cube.visualization.Watcher;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.Arrays;
import java.util.List;

public class ReceiverMessageHandler {
    private Context context;


    public ReceiverMessageHandler(Context context) {
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    public void setMessage(ReceiverViewHolder viewHolder, Message message) {
        viewHolder.binding.message.addTextChangedListener(new Watcher((Activity) context));

        switch (message.getCheck()) {
            case Image:
                handleImageMessage(viewHolder, message);
                break;
            case File:
                handleFileMessage(viewHolder, message);
                break;
            default:
                handleTextMessage(viewHolder, message);
                break;
        }


        new ClickListeners().setClickListeners(context, viewHolder, message);
    }

    private void aLLtoDoMessage(ReceiverViewHolder viewHolder, Message message) {
        updateTimestamp(viewHolder, message);
        updateMessageNotifier(viewHolder, message);
        viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);
        updateFeeling(viewHolder, message);
    }

    private void updateMessageNotifier(ReceiverViewHolder viewHolder, Message message) {
        viewHolder.binding.messageNotifier.setVisibility(View.VISIBLE);
        if ("server".equals(message.getMessageStatus())) {
            List<String> hashes = Arrays.asList("abcdef123456");

            viewHolder.binding.messageNotifier.setHashes(hashes);
            //  viewHolder.binding.image.setShapeAppearanceModel(createShapeModel(0f, 10f, 10f, 10f));
        } else {
            List<String> hashes = Arrays.asList("abcdef123456");

            viewHolder.binding.messageNotifier.setHashes(hashes);
            //   viewHolder.binding.image.setShapeAppearanceModel(createShapeModel(0f, 10f, 10f, 10f));
        }
    }

    private void updateFeeling(ReceiverViewHolder viewHolder, Message message) {
        if (message.getFeeling() >= 0) {
            viewHolder.binding.feeling.setImageResource(message.getFeeling());
            viewHolder.binding.feeling.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.feeling.setVisibility(View.GONE);
        }
    }

    private void updateTimestamp(ReceiverViewHolder viewHolder, Message message) {
        if (message.getTimestamp() != null) {
            String[] time = message.getTimestamp().split(" ");
            viewHolder.binding.time.setText(time[1]);
        }
    }

    private void updateProgress(ReceiverViewHolder viewHolder, Message message) {
        if (message.getProgress() == 100) {
            viewHolder.binding.messageNotifier.setProgress(0);
        } else {
            viewHolder.binding.messageNotifier.setProgress(message.getProgress());
        }
    }

    private void handleImageMessage(ReceiverViewHolder viewHolder, Message message) {
        viewHolder.binding.image.setVisibility(View.VISIBLE);
        viewHolder.binding.aboutFile.setVisibility(View.GONE);

        Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
        int scaledWidth = message.getImageWidth() > 2000 ? message.getImageWidth() / 4 : message.getImageWidth() / 2;
        int scaledHeight = message.getImageWidth() > 2000 ? message.getImageHeight() / 4 : message.getImageHeight() / 2;
        viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, scaledWidth, scaledHeight, false));
        viewHolder.binding.file.setText(message.getFileName());

        if (message.getMessage() != null && !message.getMessage().isEmpty()) {
            viewHolder.binding.message.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setText(message.getMessage());
            viewHolder.binding.file.setVisibility(View.GONE);
            viewHolder.binding.image.setShapeAppearanceModel(createShapeModel(0f, 58f, 10f, 10f));
        } else {
            viewHolder.binding.messageLayout.setVisibility(View.GONE);
            viewHolder.binding.file.setVisibility(View.GONE);
            viewHolder.binding.image.setShapeAppearanceModel(createShapeModel(0f, 58f, 10f, 10f));
        }
        updateProgress(viewHolder, message);
        aLLtoDoMessage(viewHolder, message);
    }

    private void handleFileMessage(ReceiverViewHolder viewHolder, Message message) {
        viewHolder.binding.file.setVisibility(View.VISIBLE);
        viewHolder.binding.image.setVisibility(View.VISIBLE);
        viewHolder.binding.aboutFile.setVisibility(View.VISIBLE);
        viewHolder.binding.image.setShapeAppearanceModel(createShapeModel(0f, 10f, 10f, 10f));
        if (message.getMessage().isEmpty()) {
            viewHolder.binding.message.setVisibility(View.GONE);
        } else {
            viewHolder.binding.message.setVisibility(View.VISIBLE);
        }

        if (message.getImage() != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
            viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth(), message.getImageHeight(), false));
        }

        if (!viewHolder.binding.fileHash.getText().toString().equals(message.getHas())) {
            viewHolder.binding.fileHash.setText(message.getHas());
        }
        if (!viewHolder.binding.fileType.getText().toString().equals(message.getTypeFile())) {
            viewHolder.binding.fileType.setText(message.getTypeFile());
        }
        if (!viewHolder.binding.fileSize.getText().toString().equals(message.getFileSize())) {
            viewHolder.binding.fileSize.setText(message.getFileSize());
        }
        if (!viewHolder.binding.fileDateCreate.getText().toString().equals(message.getDataCreate())) {
            viewHolder.binding.fileDateCreate.setText(message.getDataCreate());
        }
        if (!viewHolder.binding.file.getText().toString().equals(message.getFileName())) {
            viewHolder.binding.file.setText(message.getFileName());
        }
        if (!viewHolder.binding.message.getText().toString().equals(message.getMessage())) {
            viewHolder.binding.message.setText(message.getMessage());
        }
        updateProgress(viewHolder, message);
        aLLtoDoMessage(viewHolder, message);

    }

    private void handleTextMessage(ReceiverViewHolder viewHolder, Message message) {
        viewHolder.binding.image.setVisibility(View.GONE);
        viewHolder.binding.file.setVisibility(View.GONE);
        viewHolder.binding.aboutFile.setVisibility(View.GONE);

        if (!viewHolder.binding.message.getText().toString().equals(message.getMessage())) {
            viewHolder.binding.message.setVisibility(View.VISIBLE);
            viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setText(message.getMessage());
        }
        aLLtoDoMessage(viewHolder, message);

    }

    private ShapeAppearanceModel createShapeModel(float topLeft, float topRight, float bottomLeft, float bottomRight) {
        return new ShapeAppearanceModel()
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, topLeft)
                .setTopRightCorner(CornerFamily.ROUNDED, topRight)
                .setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeft)
                .setBottomRightCorner(CornerFamily.ROUNDED, bottomRight)
                .build();
    }
}
