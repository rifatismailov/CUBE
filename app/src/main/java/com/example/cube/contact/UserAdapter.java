package com.example.cube.contact;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.cube.R;
import com.example.cube.draw.ColorfulDotsView;
import com.example.cube.encryption.Encryption;
import com.example.qrcode.QRCode;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends ArrayAdapter<UserData> {
    private List<UserData> mList;
    private Context mContext;
    UserData userData;
    private int layout;

    public UserAdapter(@NonNull Context context, int layout, List<UserData> objects) {

        super(context, layout, objects);
        this.mList = objects;
        this.mContext = context;
        this.layout = layout;

    }

    @NonNull
    @SuppressLint("SuspiciousIndentation")
    public View getView(final int position, View view, final ViewGroup parent) {

        if (view == null) view = LayoutInflater.from(mContext).inflate(layout, null);
        userData = mList.get(position);
        ImageView image = view.findViewById(R.id.qrCodeUser);
        image.setImageBitmap(QRCode.getQRCode(userData.getId()));

        TextView userName = view.findViewById(R.id.userName);
        userName.setText(userData.getName());

        TextView idNumber = view.findViewById(R.id.idNumber);
        idNumber.setText(userData.getId());

        ColorfulDotsView rPublicKey = view.findViewById(R.id.rPublicKey);
        List<String> chunksPublicKey = splitHash(Encryption.getHash(userData.getReceiverPublicKey()), 10);
        rPublicKey.setHashes(chunksPublicKey);
        //rPublicKey.setText(Encryption.getHash(userData.getReceiverPublicKey()));

        ColorfulDotsView receiverKey = view.findViewById(R.id.receiverKey);
        //receiverKey.setText( Encryption.getHash(userData.getReceiverKey()));
        List<String> chunksReceiverKey = splitHash(Encryption.getHash(userData.getReceiverKey()), 10);
        receiverKey.setHashes(chunksReceiverKey);


        TextView messageSize = view.findViewById(R.id.messageSize);
        messageSize.setText(userData.getMessageSize());
        if (messageSize.getText().toString().isEmpty()) {
            messageSize.setVisibility(View.GONE);
        } else {
            messageSize.setVisibility(View.VISIBLE);
        }

        return view;
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
