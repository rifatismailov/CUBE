package com.example.folder.explorer;

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


import com.example.folder.R;

import java.util.List;

public class ExplorerAdapter extends ArrayAdapter<Explorer> {
    private final List<Explorer> mList;
    private final Context mContext;
    private final int resourceLayout;

    public ExplorerAdapter(@NonNull Context context, int resource, List<Explorer> objects) {
        super(context, resource, objects);
        this.mList = objects;
        this.mContext = context;
        this.resourceLayout = resource;
    }

    @NonNull
    @SuppressLint("SuspiciousIndentation")
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(mContext).inflate(resourceLayout, null);
        Explorer explorer = mList.get(position);
        ImageView image = view.findViewById(R.id.imageView);
        image.setImageResource(explorer.getImage());
        TextView textNumber = view.findViewById(R.id.txtNomber);
        textNumber.setText(explorer.getNumber());
        TextView textEdd = view.findViewById(R.id.txtViewEdad);
        textEdd.setText(explorer.getTime());
        TextView information = view.findViewById(R.id.inform);
        information.setText(explorer.getInformation());

        final CheckBox checkBox = view.findViewById(R.id.check);
        if (!mList.get(position).getCheck()) {
            checkBox.setVisibility(View.INVISIBLE);
            checkBox.setChecked(false);

        } else {
            checkBox.setVisibility(View.VISIBLE);//
            checkBox.setChecked(true);
        }

        checkBox.setOnClickListener(view1 -> {
            if (!mList.get(position).getCheck()) {
                mList.get(position).setCheck(true);
            } else {
                mList.get(position).setCheck(false);
                checkBox.setVisibility(View.INVISIBLE);
            }
        });

        return view;
    }
}
