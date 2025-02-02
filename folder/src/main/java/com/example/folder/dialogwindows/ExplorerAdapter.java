package com.example.folder.dialogwindows;

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
    private List<Explorer> mList;
    private Context mContext;
    Explorer explorer;
    private int resourceLayout;
    public ExplorerAdapter(@NonNull Context context, int resource, List<Explorer> objects){

        super(context,resource,objects);
        this.mList=objects;
        this.mContext=context;
        this.resourceLayout =resource;

    }
    @NonNull
    @SuppressLint("SuspiciousIndentation")
    public View getView(final int position, View convertView, final ViewGroup parent)
    {
        View view=convertView;
        if(view==null)
            view= LayoutInflater.from(mContext).inflate(resourceLayout,null);
           explorer =mList.get(position);
        ImageView image=view.findViewById(R.id.imageView);
        image.setImageResource(explorer.getImage());
        TextView textNomber=view.findViewById(R.id.txtNomber);
        textNomber.setText(explorer.getNumber());
        TextView textEdad=view.findViewById(R.id.txtViewEdad);
        textEdad.setText(explorer.getTime());
        TextView infirmation=view.findViewById(R.id.inform);
        infirmation.setText(explorer.getInformation());

        final CheckBox checkBox=view.findViewById(R.id.check);
        // пока Чек бокс мы не будет задействовать он нам будет нужен попоже
        if(mList.get(position).getCheck()==false) { //закріваем все чекбоксы так как будут мешать для кликабельности

            checkBox.setVisibility(view.INVISIBLE);//block checkBox
            checkBox.setChecked(false);

        }
        else {
            checkBox.setVisibility(view.VISIBLE);//
            checkBox.setChecked(true);
        }

        final View finalView = view;
        checkBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //   checked = read.isChecked();
                if(mList.get(position).getCheck()==false) {
                    //   checkBox.setVisibility(view.INVISIBLE);//block checkBox
                    //  checkBox.setChecked(true);
                    mList.get(position).setCheck(true);


                }
                else {
                    //   checkBox.setVisibility(view.VISIBLE);//block checkBox
                    //  checkBox.setChecked(false);

                    mList.get(position).setCheck(false);
                    checkBox.setVisibility(view.INVISIBLE);//block checkBox
                }
            }
        });


        return view;
    }
}
