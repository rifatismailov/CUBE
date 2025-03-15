package com.example.cube.chat.message;

import android.content.Context;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cube.chat.holder.ReceiverViewHolder;
import com.example.cube.chat.holder.SentViewHolder;
import com.example.folder.download.Downloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Клас ClickListeners відповідає за обробку натискань на елементи повідомлення
 * у RecyclerView. Він налаштовує обробники подій для відправлених та отриманих
 * повідомлень, включаючи взаємодію з текстом, зображеннями та файлами.
 */
public class ClickListeners {

    /**
     * Встановлює обробники подій для конкретного елемента списку повідомлень.
     *
     * @param context Контекст додатка.
     * @param holder  ViewHolder, який містить елемент повідомлення.
     * @param message Об'єкт повідомлення, що містить інформацію.
     */
    void setClickListeners(Context context, RecyclerView.ViewHolder holder, Message message) {
        int position = holder.getLayoutPosition();

        if (holder.getClass().equals(SentViewHolder.class)) {
            SentViewHolder viewHolder = (SentViewHolder) holder;

            // Обробник натискання на текст повідомлення
            viewHolder.binding.message.setOnClickListener(v -> {
                // Логіка для натискання на повідомлення
            });

            // Обробник натискання на зображення
            viewHolder.binding.image.setOnClickListener(v -> {
                // Логіка для натискання на зображення
                Toast.makeText(context, "[ " + message.getUrl().toString() + " ] " + position, Toast.LENGTH_LONG).show();

            });

            viewHolder.binding.image.setOnCancelListener(() -> {
                Toast.makeText(context, "[ Завершення процесу  ] " + position, Toast.LENGTH_LONG).show();

            });
            // Обробник натискання на файл у повідомленні
            viewHolder.binding.file.setOnClickListener(v -> {
                // Логіка для натискання на зображення
                Toast.makeText(context, "[ " + message.getUrl().toString() + " ] " + position, Toast.LENGTH_LONG).show();

            });
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;

            // Обробник натискання на текст отриманого повідомлення
            viewHolder.binding.message.setOnClickListener(v -> {
                // Логіка для натискання на отримане повідомлення
            });

            // Обробник натискання на зображення
            viewHolder.binding.image.setOnClickListener(v -> {
                // Логіка для натискання на зображення отриманого повідомлення
                Toast.makeText(context, "[ " + message.getUrl().toString() + " ] " + position, Toast.LENGTH_LONG).show();

            });

            viewHolder.binding.image.setOnCancelListener(() -> {
                Toast.makeText(context, "[ Завершення процесу  ] " + position, Toast.LENGTH_LONG).show();

            });
            // Обробник натискання на файл у повідомленні
            viewHolder.binding.file.setOnClickListener(v -> {
                if (!message.getUrl().toString().startsWith("http")) {
                    File file = new File(message.getUrl().toString());
                    if (file.exists() && file.isFile()) {
                        Toast.makeText(context, "[ " + message.getUrl().toString() + " ] " + position, Toast.LENGTH_LONG).show();
                    }
                    // Відкриваємо за допомогою відповідного додатку
                } else {
                    try {
                        // Отримання URL файлу для завантаження
                        URL url = new URL(message.getUrl().toString());
                        File externalDir = new File(context.getExternalFilesDir(null), "cube");
                        new Downloader(context, url, externalDir, position, message.getMessageId());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
}
