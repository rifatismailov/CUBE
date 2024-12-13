package com.example.cube;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;


import java.util.Objects;

public class ContactCreator implements View.OnClickListener {

    AlertDialog alertDialog;
    Context context;
    Activity activity;
    CreatorOps creatorOps;
    ImageButton qr_code_scanner;
    LinearLayout save_contact;
    EditText name_contact;
    EditText id_contact;
    EditText public_key_contact;

    public ContactCreator(Context context) {
        this.context = context;
        this.creatorOps = (CreatorOps) context;
        activity = (Activity) context;
    }

    public void showCreator() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
        View linearlayout = activity.getLayoutInflater().inflate(R.layout.dialog_addcontact, null);
        dialog.setView(linearlayout);
        alertDialog = dialog.create();
        // Встановлюємо прозорий фон
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); // Налаштуйте розмір, якщо потрібно
        qr_code_scanner = linearlayout.findViewById(R.id.qrCode_addScan);
        save_contact = linearlayout.findViewById(R.id.save_contact);
        name_contact = linearlayout.findViewById(R.id.name_contact);
        id_contact = linearlayout.findViewById(R.id.id_contact);
        public_key_contact = linearlayout.findViewById(R.id.public_key_contact);
        qr_code_scanner.setOnClickListener(this);
        save_contact.setOnClickListener(this);
        // Налаштування позиції діалогу
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;

        layoutParams.gravity = Gravity.BOTTOM; // Встановлюємо позицію знизу
        alertDialog.getWindow().setAttributes(layoutParams);

        alertDialog.show();
    }

    @Override
    public void onClick(View view) {
        if (view == qr_code_scanner) {
            creatorOps.scannerQrContact();
        } else if (view == save_contact) {
            creatorOps.saveContact(
                    "{ \"name_contact\":\"" + name_contact.getText()
                            + "\",\"id_contact\":\"" + id_contact.getText()
                            + "\",\"public_key_contact\":\"" + public_key_contact.getText() + "\"}");
        }
        alertDialog.cancel(); // Cancel dialog on the UI thread
    }

    public interface CreatorOps {
         void saveContact(String contact);
         void scannerQrContact();
    }
}
