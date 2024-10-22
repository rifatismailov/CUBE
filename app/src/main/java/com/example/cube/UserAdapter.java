package com.example.cube;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.qrcode.QRCode;

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


        TextView messageSize = view.findViewById(R.id.messageSize);
        messageSize.setText(userData.getMessageSize());
        if (messageSize.getText().toString().isEmpty()) {
            messageSize.setVisibility(View.GONE);
        } else {
            messageSize.setVisibility(View.VISIBLE);
        }

        return view;
    }
}
