package com.example.cube.chat.message;

import android.content.Context;

import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.chat.holder.ReceiverViewHolder;
import com.example.cube.chat.holder.SentViewHolder;
import com.example.folder.download.Downloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The ClickListeners class is responsible for handling clicks on message items
 * in the RecyclerView. It configures event handlers for sent and received
 * messages, including interaction with text, images, and files.
 */
public class ClickListeners {

    /**
     * Sets event handlers for a specific message list item.
     *
     * @param context The application context.
     * @param holder  ViewHolder that contains the message element.
     * @param message Message object that contains the information.
     */
    void setClickListeners(Context context, RecyclerView.ViewHolder holder, Message message) {
        int position = holder.getLayoutPosition();

        if (holder.getClass().equals(SentViewHolder.class)) {
            SentViewHolder viewHolder = (SentViewHolder) holder;

            // Message text click handler
            viewHolder.binding.message.setOnClickListener(v -> {
                // Message click logic
            });

            // Image click handler
            viewHolder.binding.image.setOnClickListener(v -> {
                // Image click logic
                Toast.makeText(context, message.getUrl().toString(), Toast.LENGTH_LONG).show();

            });

            viewHolder.binding.image.setOnCancelListener(() -> {
                Toast.makeText(context, "[ End of process ] ", Toast.LENGTH_LONG).show();

            });
            // Handler for clicking on a file in a message
            viewHolder.binding.file.setOnClickListener(v -> {
                // Logic for clicking on an image
                Toast.makeText(context, message.getUrl().toString(), Toast.LENGTH_LONG).show();
            });
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            // Handler for clicking on the text of a received message
            viewHolder.binding.message.setOnClickListener(v -> {
            // Logic for clicking on a received message
            });

            // Image click handler
            viewHolder.binding.image.setOnClickListener(v -> {
            // Logic for clicking on the image of the received message
                Toast.makeText(context, message.getUrl().toString(), Toast.LENGTH_LONG).show();
            });

            viewHolder.binding.image.setOnCancelListener(() -> {
                Toast.makeText(context, "[ End of process ] " + position, Toast.LENGTH_LONG).show();

            });
            // Handler for clicking on a file in a message
            viewHolder.binding.file.setOnClickListener(v -> {
                if (!message.getUrl().toString().startsWith("http")) {
                    File file = new File(message.getUrl().toString());
                    if (file.exists() && file.isFile()) {
                        Toast.makeText(context, message.getUrl().toString(), Toast.LENGTH_LONG).show();
                    }
                    // Open using the appropriate application
                } else {
                    try {
                        URL url = new URL(message.getUrl().toString());
                        File externalDir = new File(context.getExternalFilesDir(null), "cube");
                        new Downloader(context, url, externalDir, position, message.getMessageId());
//                        if (message.getMessageStatus().equals("ready")) {
//                            // Get the URL of the file to download
//                            URL url = new URL(message.getUrl().toString());
//                            File externalDir = new File(context.getExternalFilesDir(null), "cube");
//                            new Downloader(context, url, externalDir, position, message.getMessageId());
//                        } else {
//                            Toast.makeText(context, "The file is not ready for download yet.\n" +
//                                    " Wait for the status to be ready to load ", Toast.LENGTH_LONG).show();
//                        }
                    } catch (MalformedURLException e) {
                        Log.e("ClickListeners","error while trying to download file: "+e);
                    }
                }
            });
        }
    }
}