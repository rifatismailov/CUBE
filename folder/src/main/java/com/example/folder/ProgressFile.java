package com.example.folder;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressFile extends Dialog {
    private ProgressBar progressBar;
    private TextView customMessage;


    public ProgressFile(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // Скрыть заголовок
        setContentView(R.layout.progressfile); // Установить ваш кастомный макет с ProgressBar
        setCancelable(false); // Запретить закрытие ProgressDialog по нажатию вне него
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressBar = findViewById(R.id.customProgressBar);
        customMessage= findViewById(R.id.customMessage);
    }
    public void setMessage(String message){
        customMessage.setText(message);
    }
    public void setProgress(int progress) {
            progressBar.setProgress(progress);
    }
}