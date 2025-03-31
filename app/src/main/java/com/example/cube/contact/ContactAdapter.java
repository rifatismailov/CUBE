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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.cube.R;
import com.example.cube.control.FIELD;
import com.example.cube.draw.ContactCircularImageView;
import com.example.cube.draw.ColorfulDotsView;
import com.example.cube.encryption.Encryption;
import com.example.folder.GetFileIcon;
import com.example.qrcode.QRCode;
import com.example.setting.UserSetting;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of contacts in a ListView.
 */
public class ContactAdapter extends ArrayAdapter<ContactData> {
    private final List<ContactData> contactList; // List of contacts
    private final Context mContext; // Application context
    private final ContactInterface contactInterface; // Interface for handling clicks
    private ContactData contactData; // Current contact
    private final int layout; // Layout identifier

    /**
     * Adapter constructor.
     *
     * @param context Application context
     * @param layout List item layout
     * @param contactList List of contacts
     */
    public ContactAdapter(@NonNull Context context, int layout, List<ContactData> contactList) {
        super(context, layout, contactList);
        this.contactList = contactList;
        this.mContext = context;
        this.contactInterface = (ContactInterface) context;
        this.layout = layout;
    }

    /**
     * Method for getting the layout of a list item.
     *
     * @param position The position of the item in the list
     * @param view The existing view (may be null)
     * @param parent The parent container
     * @return The filled view of the list item
     */
    @NonNull
    @SuppressLint({"SuspiciousIndentation", "SetTextI18n"})
    public View getView(final int position, View view, final ViewGroup parent) {
        if (view == null) view = LayoutInflater.from(mContext).inflate(layout, null);
        contactData = contactList.get(position);
        ContactCircularImageView image = view.findViewById(R.id.qrCodeUser);
        TextView messageSize = view.findViewById(R.id.messageSize);
        TextView userName = view.findViewById(R.id.userName);
        TextView idNumber = view.findViewById(R.id.idNumber);
        TextView message = view.findViewById(R.id.textMessage);
        ImageView messageType = view.findViewById(R.id.messageType);
        ColorfulDotsView rPublicKey = view.findViewById(R.id.rPublicKey);
        ColorfulDotsView receiverKey = view.findViewById(R.id.receiverKey);

        // Update contact status
        if (contactData.getStatusContact() != null) {
                switch (contactData.getStatusContact()) {
                    case "disconnect":
                        image.updateStatusColor("00");
                        break;
                    case "died":
                        image.updateStatusColor("01");
                        break;
                    case "reborn":
                        image.updateStatusColor("10");
                        break;
                }
        }

        // Set message type and text
        if (contactData.getMessageType() != null) {
            messageType.setImageResource(GetFileIcon.getIcon(contactData.getMessageType()));
            message.setText(contactData.getMessage());
        }

        // Update progress (if any)
        if (contactData.getProgress() > 0) {
            image.setProgress(contactData.getProgress());
        }
        if (contactData.getProgress() == 100) {
            image.clearProgress();
        }

        // Loading contact image or creating QR code
        if (contactData.getAccountImageUrl() != null && !contactData.getAccountImageUrl().isEmpty()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Reducing size
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

        // Setting text values
        userName.setText(contactData.getName() + " " + contactData.getLastName());
        idNumber.setText(contactData.getId());

        // Handling security keys
        List<String> chunksPublicKey = splitHash(Encryption.getHash(contactData.getReceiverPublicKey()), 10);
        rPublicKey.setHashes(chunksPublicKey);
        List<String> chunksReceiverKey = splitHash(Encryption.getHash(contactData.getReceiverKey()), 10);
        receiverKey.setHashes(chunksReceiverKey);

        // Handling clicks on contact image
        image.setOnClickListener(view1 -> contactInterface.onImageClickContact(position));

        // Display message size
        messageSize.setText(contactData.getMessageSize());
        if (messageSize.getText().toString().isEmpty()) {
            messageSize.setVisibility(View.GONE);
        } else {
            messageSize.setVisibility(View.VISIBLE);
        }

        return view;
    }

    /**
     * Updates the progress for a specific contact in the list.
     *
     * @param position The position of the contact
     * @param progress The new progress level
     */
    public void setProgressForPosition(int position, int progress) {
        if (position >= 0 && position < contactList.size()) {
            ContactData contactData = contactList.get(position);
            contactData.setProgress(progress);
            notifyDataSetChanged();
        }
    }

    /**
     * Splits the hash into chunks of the specified size.
     *
     * @param hash The hash string
     * @param chunkSize The size of each chunk
     * @return A list of hash chunks
     */
    public List<String> splitHash(String hash, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = hash.length();
        for (int i = 0; i < length; i += chunkSize) {
            chunks.add(hash.substring(i, Math.min(length, i + chunkSize)));
        }
        return chunks;
    }
}