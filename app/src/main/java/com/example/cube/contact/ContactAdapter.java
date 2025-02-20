package com.example.cube.contact;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.cube.R;
import com.example.cube.draw.ContactCircularImageView;
import com.example.cube.draw.ColorfulDotsView;
import com.example.cube.encryption.Encryption;
import com.example.qrcode.QRCode;
import com.example.setting.UserSetting;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends ArrayAdapter<ContactData> {
    private final List<ContactData> contactList;
    private final Context mContext;
    private final ContactInterface contactInterface;
    private ContactData contactData;
    private final int layout;

    public ContactAdapter(@NonNull Context context, int layout, List<ContactData> contactList) {
        super(context, layout, contactList);
        this.contactList = contactList;
        this.mContext = context;
        this.contactInterface = (ContactInterface) context;
        this.layout = layout;
    }

    @NonNull
    @SuppressLint({"SuspiciousIndentation", "SetTextI18n"})
    public View getView(final int position, View view, final ViewGroup parent) {
        if (view == null) view = LayoutInflater.from(mContext).inflate(layout, null);
        contactData = contactList.get(position);
        ContactCircularImageView image = view.findViewById(R.id.qrCodeUser);
        TextView messageSize = view.findViewById(R.id.messageSize);
        TextView userName = view.findViewById(R.id.userName);
        TextView idNumber = view.findViewById(R.id.idNumber);
        ColorfulDotsView rPublicKey = view.findViewById(R.id.rPublicKey);
        ColorfulDotsView receiverKey = view.findViewById(R.id.receiverKey);

        if (contactData.getProgress() > 0) {
            image.setProgress(contactData.getProgress()); // Встановлюємо прогрес в circular image
        }
        if (contactData.getProgress() == 100) {
            image.clearProgress();
        }
        if (contactData.getAccountImageUrl() != null && !contactData.getAccountImageUrl().isEmpty()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Зменшити розмір у два рази
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

        userName.setText(contactData.getName() + " " + contactData.getLastName());
        idNumber.setText(contactData.getId());

        List<String> chunksPublicKey = splitHash(Encryption.getHash(contactData.getReceiverPublicKey()), 10);
        rPublicKey.setHashes(chunksPublicKey);
        List<String> chunksReceiverKey = splitHash(Encryption.getHash(contactData.getReceiverKey()), 10);
        receiverKey.setHashes(chunksReceiverKey);
        image.setOnClickListener(view1 -> {
            contactInterface.onImageClickContact(position);
            contactInterface.onImageClickContact(position);

            if (contactData.getAccountImageUrl() != null && !contactData.getAccountImageUrl().isEmpty()) {
                // вікно відображення QR коду або зображення та повної інформації
            } else {
                // вікно відображення QR коду або зображення та повної інформації
                // Та якщо нема зображення робимо запит
                contactInterface.onImageClickContact(position);
            }
        });

        messageSize.setText(contactData.getMessageSize());
        if (messageSize.getText().toString().isEmpty()) {
            messageSize.setVisibility(View.GONE);
        } else {
            messageSize.setVisibility(View.VISIBLE);
        }
        return view;
    }

    public void setProgressForPosition(int position, int progress) {
        // Оновлюємо конкретну позицію
        if (position >= 0 && position < contactList.size()) {
            ContactData contactData = contactList.get(position);
            contactData.setProgress(progress);  // Оновлюємо прогрес для конкретного користувача

            // Оновлюємо тільки цю позицію
            notifyDataSetChanged();  // або notifyItemChanged(position);
        }
    }

    public List<String> splitHash(String hash, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = hash.length();

        for (int i = 0; i < length; i += chunkSize) {
            // Беремо підрядок розміром chunkSize або до кінця
            chunks.add(hash.substring(i, Math.min(length, i + chunkSize)));
        }

        return chunks;
    }

}
