package com.example.qrcode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Даний  @AlertDialog відображає папки та файли
 * при натисканні на які фалй конвертується в массив байтів
 * за допомогою методів @convertImage
 *
 * @convertVideo
 * @convertFile За задумкою всі ці методи мають окремі класи  як @ConvertImage
 * @ConvertFile яка будуть конвертувати файли
 */
public class QR {
    private static final Logger LOGGER = Logger.getLogger(QR.class.getName());
    Context context;
    Activity activity;
    AlertDialog alertDialog;
    ImageButton back;
    ImageView qrCode;

    TextView idNumber;
    String id;
    private ActivityResultLauncher<ScanOptions> barLauncher;


    public QR(Context context) {
        this.context = context;
        activity = (Activity) context;
    }
    public QR(Context context, String id) {
        this.context = context;
        activity = (Activity) context;
        this.id = id;
        DialogShow();
    }

    public QR(ActivityResultLauncher<ScanOptions> barLauncher) {
        this.barLauncher = barLauncher;
        scanCode();
    }

    private void DialogShow() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        final View linearlayout = activity.getLayoutInflater().inflate(R.layout.dialog_qrcode, null);
        dialog.setView(linearlayout);
        alertDialog = dialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.create();
        back = linearlayout.findViewById(R.id.back);
        qrCode = linearlayout.findViewById(R.id.qrCode);//is:debug tag:MainActivity
        qrCode.setImageBitmap(QRCode.getQRCode(id));
        assert back != null;
        back.setOnClickListener(v -> alertDialog.cancel());
        idNumber = linearlayout.findViewById(R.id.idNumber);
        idNumber.setText(id);
        dialog.create();

    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }
}
