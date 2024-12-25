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
            viewHolder.binding.aboutFile.setVisibility(View.GONE);
            Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
            if(message.getImageWidth()>2000)
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

            if(message.getImageWidth()>2000)
                viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth() / 4, message.getImageHeight() / 4, false));
            else
                viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth() / 2, message.getImageHeight() / 2, false));
        } else if (message.getCheck().equals(Check.File)) {
            ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, 0f) // Верхній лівий кут
                    .setTopRightCorner(CornerFamily.ROUNDED, 10f) // Верхній правий кут
                    .setBottomLeftCorner(CornerFamily.ROUNDED, 10f)  // Нижній лівий кут (прямий)
                    .setBottomRightCorner(CornerFamily.ROUNDED, 10f) // Нижній правий кут (прямий)
                    .build();
            viewHolder.binding.image.setShapeAppearanceModel(shapeAppearanceModel);
            viewHolder.binding.file.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setVisibility(View.VISIBLE);
            viewHolder.binding.image.setVisibility(View.VISIBLE);
            viewHolder.binding.aboutFile.setVisibility(View.VISIBLE);

            if(message.getImage()!=null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
                viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth() , message.getImageHeight() , false));
            }
            viewHolder.binding.fileHash.setText("AAA"+message.getHas());
            viewHolder.binding.fileType.setText("AAA"+message.getTypeFile());
            viewHolder.binding.fileSize.setText("AAA"+message.getFileSize());
            viewHolder.binding.fileDateCreate.setText("AAA"+message.getDataCreate());
            viewHolder.binding.file.setText(message.getFileName());
            viewHolder.binding.message.setText(message.getMessage());
        } else {
            viewHolder.binding.image.setVisibility(View.GONE);
            viewHolder.binding.file.setVisibility(View.GONE);
            viewHolder.binding.aboutFile.setVisibility(View.GONE);

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
