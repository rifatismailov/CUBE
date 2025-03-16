package com.example.cube.chat.message;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.cube.R;
import com.example.cube.chat.holder.ReceiverViewHolder;
import com.example.cube.visualization.Watcher;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.Arrays;
import java.util.List;

/**
 * This class handles the logic for displaying and managing received messages. It takes care of
 * processing different types of messages, such as text, image, and file messages, and updating
 * the UI accordingly. The message's attributes are displayed in a specific way depending on
 * the message type and content.
 */
public class ReceiverMessageHandler {
    private final Context context;

    /**
     * Constructor for initializing the ReceiverMessageHandler.
     *
     * @param context The context in which the handler operates.
     */
    public ReceiverMessageHandler(Context context) {
        this.context = context;
    }

    /**
     * Sets the message in the appropriate view holder. The method differentiates between
     * text, image, and file messages, and updates the views accordingly.
     *
     * @param viewHolder The view holder that holds the UI components for the message.
     * @param message    The message object containing data to be displayed.
     */
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

    /**
     * Performs actions related to text messages, including updating timestamps and progress.
     *
     * @param viewHolder The view holder to update.
     * @param message    The message to process.
     */
    private void aLLtoDoMessage(ReceiverViewHolder viewHolder, Message message) {
        updateTimestamp(viewHolder, message);
        updateMessageNotifier(viewHolder, message);
        viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);
        updateFeeling(viewHolder, message);
    }

    /**
     * Оновлює статус повідомлення.
     */
    private void updateMessageNotifier(ReceiverViewHolder viewHolder, Message message) {
        viewHolder.binding.messageNotifier.setVisibility(View.VISIBLE);
        if(message.getMessageStatus()!=null) {
            if ("change".equals(message.getMessageStatus())) {
                viewHolder.binding.messageNotifier.setMessage(message.getMessageStatus());
                int color = ContextCompat.getColor(context, R.color.light_notifier_one);
                viewHolder.binding.messageNotifier.setBackgroundColor(color);
            } else {
                viewHolder.binding.messageNotifier.setMessage(message.getMessageStatus());
                int color = ContextCompat.getColor(context, R.color.light_notifier_one);
                viewHolder.binding.messageNotifier.setBackgroundColor(color);
            }
        }else {
            message.setMessageStatus("open");
            viewHolder.binding.messageNotifier.setMessage(message.getMessageStatus());
            int color = ContextCompat.getColor(context, R.color.light_notifier_one);
            viewHolder.binding.messageNotifier.setBackgroundColor(color);
        }
    }

    /**
     * Оновлює почуття у повідомленні.
     */
    private void updateFeeling(ReceiverViewHolder viewHolder, Message message) {
        if (message.getFeeling() >= 0) {
            viewHolder.binding.feeling.setImageResource(message.getFeeling());
            viewHolder.binding.feeling.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.feeling.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the timestamp displayed in the message.
     *
     * @param viewHolder The view holder to update.
     * @param message    The message containing the timestamp.
     */
    private void updateTimestamp(ReceiverViewHolder viewHolder, Message message) {
        if (message.getTimestamp() != null) {
            String[] time = message.getTimestamp().split(" ");
            viewHolder.binding.time.setText(time[1]);
        }
    }

    /**
     * Updates the progress bar for the message.
     *
     * @param viewHolder The view holder to update.
     * @param message    The message containing progress data.
     */
    private void updateProgress(ReceiverViewHolder viewHolder, Message message) {
        if (message.getProgress() == 100) {
            //  viewHolder.binding.messageNotifier.setProgress(0);
            viewHolder.binding.image.setProgress(0);
        } else {
            // viewHolder.binding.messageNotifier.setProgress(message.getProgress());
            viewHolder.binding.image.setProgress(message.getProgress());

        }
    }

    /**
     * Handles the display and processing of image messages.
     *
     * @param viewHolder The view holder to update.
     * @param message    The image message.
     */
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

    /**
     * Handles the display and processing of file messages.
     *
     * @param viewHolder The view holder to update.
     * @param message    The file message.
     */
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

    /**
     * Handles the display and processing of text messages.
     *
     * @param viewHolder The view holder to update.
     * @param message    The text message.
     */
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

    /**
     * Creates a ShapeAppearanceModel to define the rounded corners of a view.
     *
     * @param topLeft     The radius of the top-left corner.
     * @param topRight    The radius of the top-right corner.
     * @param bottomLeft  The radius of the bottom-left corner.
     * @param bottomRight The radius of the bottom-right corner.
     * @return A ShapeAppearanceModel with the defined corner radii.
     */
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
