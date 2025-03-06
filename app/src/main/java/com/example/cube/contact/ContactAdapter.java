package com.example.cube.contact;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.cube.R;
import com.example.cube.control.FIELD;
import com.example.cube.draw.ContactCircularImageView;
import com.example.cube.draw.ColorfulDotsView;
import com.example.cube.encryption.Encryption;
import com.example.folder.GetFileIcon;
import com.example.qrcode.QRCode;
import com.example.setting.UserSetting;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для відображення списку контактів у ListView.
 */
public class ContactAdapter extends ArrayAdapter<ContactData> {
    private final List<ContactData> contactList; // Список контактів
    private final Context mContext; // Контекст застосунку
    private final ContactInterface contactInterface; // Інтерфейс для обробки натискань
    private ContactData contactData; // Поточний контакт
    private final int layout; // Ідентифікатор макету

    /**
     * Конструктор адаптера.
     *
     * @param context      Контекст застосунку
     * @param layout       Макет елемента списку
     * @param contactList  Список контактів
     */
    public ContactAdapter(@NonNull Context context, int layout, List<ContactData> contactList) {
        super(context, layout, contactList);
        this.contactList = contactList;
        this.mContext = context;
        this.contactInterface = (ContactInterface) context;
        this.layout = layout;
    }

    /**
     * Метод для отримання вигляду елемента списку.
     *
     * @param position  Позиція елемента у списку
     * @param view      Існуючий вигляд (може бути null)
     * @param parent    Батьківський контейнер
     * @return          Заповнений вигляд елемента списку
     */
    @NonNull
    @SuppressLint({"SuspiciousIndentation", "SetTextI18n"})
    public View getView(final int position, View view, final ViewGroup parent) {
        if (view == null) view = LayoutInflater.from(mContext).inflate(layout, null);
        contactData = contactList.get(position);
        ContactCircularImageView image = view.findViewById(R.id.qrCodeUser);
        TextView messageSize = view.findViewById(R.id.messageSize);
        TextView userName = view.findViewById(R.id.userName);
        TextView idNumber = view.findViewById(R.id.idNumber);
        TextView message = view.findViewById(R.id.textMessage);
        ImageView messageType = view.findViewById(R.id.messageType);
        ColorfulDotsView rPublicKey = view.findViewById(R.id.rPublicKey);
        ColorfulDotsView receiverKey = view.findViewById(R.id.receiverKey);

        // Оновлення статусу контакту
        if (contactData.getStatusContact() != null) {
            image.updateStatusColor(contactData.getStatusContact());
        }

        // Встановлення типу повідомлення та тексту
        if (contactData.getMessageType() != null) {
            messageType.setImageResource(GetFileIcon.getIcon(contactData.getMessageType()));
            message.setText(contactData.getMessage());
        }

        // Оновлення прогресу (якщо є)
        if (contactData.getProgress() > 0) {
            image.setProgress(contactData.getProgress());
        }
        if (contactData.getProgress() == 100) {
            image.clearProgress();
        }

        // Завантаження зображення контакту або створення QR-коду
        if (contactData.getAccountImageUrl() != null && !contactData.getAccountImageUrl().isEmpty()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Зменшення розміру
            Bitmap bitmap = BitmapFactory.decodeFile(contactData.getAccountImageUrl(), options);
            image.setImageBitmap(bitmap);
        } else {
            String jsonData = new UserSetting.Builder()
                    .setId(this.contactData.getId())
                    .setName(this.contactData.getName())
                    .setLastName(this.contactData.getLastName())
                    .build().toJson("userId", "name", "lastName").toString();
            image.setImageBitmap(QRCode.getQRCode(jsonData, contactData.getName().substring(0, 2)));
        }

        // Встановлення текстових значень
        userName.setText(contactData.getName() + " " + contactData.getLastName());
        idNumber.setText(contactData.getId());

        // Обробка ключів безпеки
        List<String> chunksPublicKey = splitHash(Encryption.getHash(contactData.getReceiverPublicKey()), 10);
        rPublicKey.setHashes(chunksPublicKey);
        List<String> chunksReceiverKey = splitHash(Encryption.getHash(contactData.getReceiverKey()), 10);
        receiverKey.setHashes(chunksReceiverKey);

        // Обробка кліку по зображенню контакту
        image.setOnClickListener(view1 -> contactInterface.onImageClickContact(position));

        // Відображення розміру повідомлення
        messageSize.setText(contactData.getMessageSize());
        if (messageSize.getText().toString().isEmpty()) {
            messageSize.setVisibility(View.GONE);
        } else {
            messageSize.setVisibility(View.VISIBLE);
        }

        return view;
    }

    /**
     * Оновлює прогрес для конкретного контакту в списку.
     *
     * @param position Позиція контакту
     * @param progress Новий рівень прогресу
     */
    public void setProgressForPosition(int position, int progress) {
        if (position >= 0 && position < contactList.size()) {
            ContactData contactData = contactList.get(position);
            contactData.setProgress(progress);
            notifyDataSetChanged();
        }
    }

    /**
     * Розбиває хеш на частини заданого розміру.
     *
     * @param hash      Хеш-рядок
     * @param chunkSize Розмір кожної частини
     * @return Список частин хешу
     */
    public List<String> splitHash(String hash, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = hash.length();
        for (int i = 0; i < length; i += chunkSize) {
            chunks.add(hash.substring(i, Math.min(length, i + chunkSize)));
        }
        return chunks;
    }
}