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

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends ArrayAdapter<ContactData> {
    private List<ContactData> mList;
    private Context mContext;
    private ContactInterface contactInterface;
    ContactData contactData;
    private ContactCircularImageView image;
    private int layout;

    public ContactAdapter(@NonNull Context context, int layout, List<ContactData> objects) {

        super(context, layout, objects);
        this.mList = objects;
        this.mContext = context;
        this.contactInterface = (ContactInterface) context;
        this.layout = layout;

    }

    @NonNull
    @SuppressLint("SuspiciousIndentation")
    public View getView(final int position, View view, final ViewGroup parent) {

        if (view == null) view = LayoutInflater.from(mContext).inflate(layout, null);
        contactData = mList.get(position);
        image = view.findViewById(R.id.qrCodeUser);
        if (contactData.getProgress() > 0) {
            image.setProgress(contactData.getProgress()); // Встановлюємо прогрес в circular image
        }
        if (contactData.getProgress() == 100) {
            image.clearProgress();
        }

        Log.e("MainActivity", "AccountImageUrl " + contactData.getAccountImageUrl());

        if (contactData.getAccountImageUrl() != null && !contactData.getAccountImageUrl().isEmpty()) {
            Log.e("MainActivity", "AccountImageUrl " + contactData.getAccountImageUrl());

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Зменшити розмір у два рази
            Bitmap bitmap = BitmapFactory.decodeFile(contactData.getAccountImageUrl(), options);
            image.setImageBitmap(bitmap);
        } else {
            String name = "Kiki";
            String lastName = "Kamureno";
            String jsonData = "{" +
                    "\"userId\":\"" + this.contactData.getId() + "\"," +
                    "\"name\":\"" + name + "\"," +
                    "\"lastName\":\"" + lastName + "\"" +
                    "}";
            String result = contactData.getName().substring(0, 2);  // Отримуємо перші дві букви

            image.setImageBitmap(QRCode.getQRCode(jsonData, result));
        }

        TextView userName = view.findViewById(R.id.userName);
        userName.setText(contactData.getName());

        TextView idNumber = view.findViewById(R.id.idNumber);
        idNumber.setText(contactData.getId());

        ColorfulDotsView rPublicKey = view.findViewById(R.id.rPublicKey);
        List<String> chunksPublicKey = splitHash(Encryption.getHash(contactData.getReceiverPublicKey()), 10);
        rPublicKey.setHashes(chunksPublicKey);
        //rPublicKey.setText(Encryption.getHash(userData.getReceiverPublicKey()));

        ColorfulDotsView receiverKey = view.findViewById(R.id.receiverKey);
        //receiverKey.setText( Encryption.getHash(userData.getReceiverKey()));
        List<String> chunksReceiverKey = splitHash(Encryption.getHash(contactData.getReceiverKey()), 10);
        receiverKey.setHashes(chunksReceiverKey);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactInterface.onImageClickContact(position);

                if (contactData.getAccountImageUrl() != null && !contactData.getAccountImageUrl().isEmpty()) {
                    // вікно відображення QR коду або зображення та повної інформації
                } else {
                    // вікно відображення QR коду або зображення та повної інформації
                    // Та якщо нема зображення робимо запит
                    contactInterface.onImageClickContact(position);
                }
            }
        });

        TextView messageSize = view.findViewById(R.id.messageSize);
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
        if (position >= 0 && position < mList.size()) {
            ContactData contactData = mList.get(position);
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
