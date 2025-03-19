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
import com.example.cube.chat.holder.SentViewHolder;
import com.example.cube.visualization.Watcher;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * This class handles the logic for displaying and managing sent messages. It takes care of
 * processing different types of messages, such as text, image, and file messages, and updating
 * the UI accordingly. The message's attributes are displayed in a specific way depending on
 * the message type and content.
 */
public class SendMessageHandler {
    private final Context context;

    /**
     * Constructor for initializing the SendMessageHandler.
     *
     * @param context The context in which the handler operates.
     */
    public SendMessageHandler(Context context) {
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
    public void setMessage(SentViewHolder viewHolder, Message message) {
        // Add text change listener for the message
        viewHolder.binding.message.addTextChangedListener(new Watcher());

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
     * Handles the display and processing of image messages.
     *
     * @param viewHolder The view holder to update.
     * @param message    The image message.
     */
    private void handleImageMessage(SentViewHolder viewHolder, Message message) {
        // Show image, hide file
        viewHolder.binding.image.setVisibility(View.VISIBLE);
        viewHolder.binding.aboutFile.setVisibility(View.GONE);

        // Create Bitmap from byte array and scale it
        Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
        int scaledWidth = message.getImageWidth() > 2000 ? message.getImageWidth() / 4 : message.getImageWidth() / 2;
        int scaledHeight = message.getImageWidth() > 2000 ? message.getImageHeight() / 4 : message.getImageHeight() / 2;
        viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, scaledWidth, scaledHeight, false));

        // If the message contains text, display it with appropriate visibility
        if (message.getMessage() != null && !message.getMessage().isEmpty()) {
            viewHolder.binding.file.setVisibility(View.GONE);
            viewHolder.binding.message.setVisibility(View.VISIBLE);
            viewHolder.binding.message.setText(message.getMessage());
            viewHolder.binding.image.setShapeAppearanceModel(createShapeModel(58f, 0f, 10f, 10f));
        } else {
            viewHolder.binding.file.setVisibility(View.GONE);
            viewHolder.binding.message.setVisibility(View.GONE);
            viewHolder.binding.image.setShapeAppearanceModel(createShapeModel(58f, 0f, 10f, 10f));
        }

        // Update various file details
        updateFileDetails(viewHolder, message);
        aLLtoDoImage(viewHolder, message);
    }

    /**
     * Handles the display and processing of file messages.
     *
     * @param viewHolder The view holder to update.
     * @param message    The file message.
     */
    private void handleFileMessage(SentViewHolder viewHolder, Message message) {
        // Show file-related UI components
        viewHolder.binding.file.setVisibility(View.VISIBLE);
        viewHolder.binding.message.setVisibility(View.VISIBLE);
        viewHolder.binding.image.setVisibility(View.VISIBLE);
        viewHolder.binding.aboutFile.setVisibility(View.VISIBLE);
        viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);

        // Display image if available
        if (message.getImage() != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(message.getImage(), 0, message.getImage().length);
            viewHolder.binding.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, message.getImageWidth(), message.getImageHeight(), false));
        }

        // Update file details
        updateFileDetails(viewHolder, message);
        aLLtoDoFile(viewHolder, message);
    }

    /**
     * Handles the display and processing of text messages.
     *
     * @param viewHolder The view holder to update.
     * @param message    The text message.
     */
    private void handleTextMessage(SentViewHolder viewHolder, Message message) {
        // Hide image and file components, show text message
        viewHolder.binding.image.setVisibility(View.GONE);
        viewHolder.binding.file.setVisibility(View.GONE);
        viewHolder.binding.aboutFile.setVisibility(View.GONE);
        viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);
        viewHolder.binding.message.setVisibility(View.VISIBLE);

        // Set the text of the message
        if (!viewHolder.binding.message.getText().toString().equals(message.getMessage())) {
            viewHolder.binding.message.setText(message.getMessage());
        }
        aLLtoDoMessage(viewHolder, message);
    }

    /**
     * Updates file details such as hash, type, size, and date.
     *
     * @param viewHolder The view holder to update.
     * @param message    The message containing file details.
     */
    private void updateFileDetails(SentViewHolder viewHolder, Message message) {
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
    }

    private void updateFeelLayout(SentViewHolder viewHolder, Message message) {
        viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);

//        if (message.getFeeling() >= 0 || "server".equals(message.getMessageStatus())) {
//            viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);
//        } else {
//            viewHolder.binding.feelLayout.setVisibility(View.GONE);
//        }
    }

    /**
     * Performs actions related to image messages, including updating timestamps and progress.
     *
     * @param viewHolder The view holder to update.
     * @param message    The message to process.
     */
    private void aLLtoDoImage(SentViewHolder viewHolder, Message message) {
        updateTimestamp(viewHolder, message);
        updateProgress(viewHolder, message);
        updateMessageNotifier(viewHolder, message);
        updateFeelLayout(viewHolder, message);
        updateFeeling(viewHolder, message);
    }

    /**
     * Performs actions related to file messages, including updating timestamps and progress.
     *
     * @param viewHolder The view holder to update.
     * @param message    The message to process.
     */
    private void aLLtoDoFile(SentViewHolder viewHolder, Message message) {
        updateTimestamp(viewHolder, message);
        updateProgress(viewHolder, message);
        updateMessageNotifier(viewHolder, message);
        viewHolder.binding.feelLayout.setVisibility(View.VISIBLE);
        updateFeeling(viewHolder, message);
    }

    /**
     * Performs actions related to text messages, including updating timestamps and progress.
     *
     * @param viewHolder The view holder to update.
     * @param message    The message to process.
     */
    private void aLLtoDoMessage(SentViewHolder viewHolder, Message message) {
        updateTimestamp(viewHolder, message);
        updateMessageNotifier(viewHolder, message);
        updateFeelLayout(viewHolder, message);
        updateFeeling(viewHolder, message);
    }

    /**
     * Updates the timestamp displayed in the message.
     *
     * @param viewHolder The view holder to update.
     * @param message    The message containing the timestamp.
     */
    private void updateTimestamp(SentViewHolder viewHolder, Message message) {
        if (message.getTimestamp() != null) {
            if (message.getTimestamp().contains(" ")) {
                String[] time = message.getTimestamp().split(" ");
                viewHolder.binding.time.setText(time[1]);
            } else {
                viewHolder.binding.time.setText(message.getTimestamp());
            }
        }
    }

    /**
     * Updates the progress bar for the message.
     *
     * @param viewHolder The view holder to update.
     * @param message    The message containing progress data.
     */
    private void updateProgress(SentViewHolder viewHolder, Message message) {
        if (message.getProgress() == 100) {
            viewHolder.binding.image.setProgress(0);
        } else {
            viewHolder.binding.image.setProgress(message.getProgress());
        }
    }

    /**
     * Updates the message notifier component with the relevant information.
     *
     * @param viewHolder The view holder to update.
     * @param message    The message containing notifier data.
     */
    private void updateMessageNotifier(SentViewHolder viewHolder, Message message) {

        if ("server".equals(message.getMessageStatus())) {
            viewHolder.binding.messageNotifier.setVisibility(View.VISIBLE);
            viewHolder.binding.messageNotifier.setMessage(message.getMessageStatus());
            int color = ContextCompat.getColor(context, R.color.light_notifier_one);
            viewHolder.binding.messageNotifier.setBackgroundColor(color);
        } else if ("delivered".equals(message.getMessageStatus())) {
            viewHolder.binding.messageNotifier.setVisibility(View.VISIBLE);
            viewHolder.binding.messageNotifier.setMessage(message.getMessageStatus());
            int color = ContextCompat.getColor(context, R.color.light_notifier_one);
            viewHolder.binding.messageNotifier.setBackgroundColor(color);
        }  else if ("received".equals(message.getMessageStatus())) {
            viewHolder.binding.messageNotifier.setVisibility(View.VISIBLE);
            viewHolder.binding.messageNotifier.setMessage(message.getMessageStatus());
            int color = ContextCompat.getColor(context, R.color.light_notifier_one);
            viewHolder.binding.messageNotifier.setBackgroundColor(color);
        } else {
            viewHolder.binding.messageNotifier.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the feeling (emotion icon) associated with the message.
     *
     * @param viewHolder The view holder to update.
     * @param message    The message containing feeling data.
     */
    private void updateFeeling(SentViewHolder viewHolder, Message message) {
        if (message.getFeeling() >= 0) {
            viewHolder.binding.feeling.setImageResource(message.getFeeling());
            viewHolder.binding.feeling.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.feeling.setVisibility(View.GONE);
        }
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


