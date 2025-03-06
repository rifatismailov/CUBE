package com.example.cube.contact;

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

import com.example.cube.R;
import com.example.setting.UserSetting;

import java.util.Objects;

/**
 * This class is responsible for creating and managing a dialog for adding a new contact.
 * It allows the user to input contact details like ID, name, and last name, as well as scan a QR code for contact information.
 * The class also handles saving the contact details and passing them to the implementing activity or fragment.
 */
public class ContactCreator implements View.OnClickListener {

    private AlertDialog alertDialog;
    private Context context;
    private CreatorOps creatorOps;
    private ImageButton qr_code_scanner;
    private LinearLayout save_contact;
    private EditText id;
    private EditText name;
    private EditText lastName;

    /**
     * Constructor for initializing the ContactCreator with the given context.
     * @param context the context in which the ContactCreator is used (usually an Activity).
     */
    public ContactCreator(Context context) {
        this.context = context;
        this.creatorOps = (CreatorOps) context; // The context must implement CreatorOps interface.
    }

    /**
     * Displays the dialog for adding a new contact.
     * Initializes the layout, sets up listeners, and customizes the dialog's appearance and positioning.
     */
    public void show() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
        View linearlayout = ((Activity) context).getLayoutInflater().inflate(R.layout.dialog_addcontact, null);
        dialog.setView(linearlayout);
        alertDialog = dialog.create();

        // Set transparent background for the dialog
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); // Adjust size if necessary

        // Initialize views from the dialog layout
        qr_code_scanner = linearlayout.findViewById(R.id.qrCode_addScan);
        save_contact = linearlayout.findViewById(R.id.save_contact);
        name = linearlayout.findViewById(R.id.name_contact);
        id = linearlayout.findViewById(R.id.id_contact);
        lastName = linearlayout.findViewById(R.id.last_name_contact);

        // Set listeners for the buttons
        qr_code_scanner.setOnClickListener(this);
        save_contact.setOnClickListener(this);

        // Set dialog position (bottom of the screen)
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.BOTTOM; // Position dialog at the bottom of the screen
        alertDialog.getWindow().setAttributes(layoutParams);

        alertDialog.show(); // Show the dialog
    }

    /**
     * Handles button clicks within the dialog.
     * If QR code scanner button is clicked, it triggers the scanner.
     * If save contact button is clicked, it validates input and triggers saving the contact.
     * @param view the clicked view (either QR code scanner or save contact button).
     */
    @Override
    public void onClick(View view) {
        if (view == qr_code_scanner) {
            creatorOps.scannerQrContact(); // Trigger QR code scanning
            alertDialog.cancel(); // Close the dialog

        } else if (view == save_contact) {
            // Validate if all fields are filled
            if (!id.getText().toString().isEmpty() && !name.getText().toString().isEmpty() && !lastName.getText().toString().isEmpty()) {
                // Save contact by passing the contact data as a JSON string
                creatorOps.saveContact(
                        new UserSetting.Builder()
                                .setId(id.getText().toString()) // Set contact ID
                                .setName(name.getText().toString()) // Set contact name
                                .setLastName(lastName.getText().toString()) // Set contact last name
                                .build().toJson("userId", "name", "lastName").toString()); // Convert to JSON string
                alertDialog.cancel(); // Close the dialog
            }
        }
    }

    /**
     * Interface that must be implemented by the context (Activity or Fragment) using the ContactCreator.
     * Provides methods for saving a contact and scanning a QR code.
     */
    public interface CreatorOps {
        /**
         * Saves the provided contact data.
         * @param contact the contact data as a JSON string.
         */
        void saveContact(String contact);

        /**
         * Triggers the QR code scanner for contact information.
         */
        void scannerQrContact();
    }
}
