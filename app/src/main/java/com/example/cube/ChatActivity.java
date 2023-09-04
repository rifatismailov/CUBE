package com.example.cube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.cube.adapters.MessagesAdapter;
import com.example.cube.control.Check;
import com.example.cube.control.Side;
import com.example.cube.models.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cube.databinding.ActivityChatBinding;
import com.example.cube.socket.Exchange;
import com.example.folder.Folder;
import com.example.folder.dialogwindows.Open;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements Folder {

    private ActivityChatBinding binding;

    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String receiverUid;
    List<Exchange> exchanges = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        messages = new ArrayList<>();
        binding.name.setText("name");
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // senderUid = FirebaseAuth.getInstance().getUid();
        Bundle bundle = getIntent().getExtras();
        receiverUid = bundle.getString("name");
        binding.status.setText("Online");
        binding.name.setText(receiverUid);
        adapter = new MessagesAdapter(this, messages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setItemViewCacheSize(20);
        binding.recyclerView.setDrawingCacheEnabled(true);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);


        receiver("Hello my friend American English varieties");
        receiver("This is a photo of just one young family killed by a Russian missile attack " +
                "on Kryvyi Rih. Tonight, there was another terrorist attack – and again three dead, this time in #Odesa. " +
                "All the missiles fired by #Russia were produced in the spring of 2023. In all of them, " +
                "without… https://t.co/InqQMPl3xr https://t.co/ByK8CN1dyA");


        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = binding.messageBox.getText().toString();
                if (!binding.messageBox.getText().toString().isEmpty()) {
                    sender(messageTxt);
                    //adapter.notifyDataSetChanged();

                    binding.messageBox.setText("");
                }
            }
        });
        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    new Open(ChatActivity.this);
                }
            }
        });

      /*  final Handler handler = new Handler();
        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //     database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    //       database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });
*/

        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    private void receiver(String message) {
        messages.add(new Message(message, Check.Other, Side.Receiver));
        binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
        adapter.notifyDataSetChanged();
    }

    private void sender(String message) {
        messages.add(new Message(message, Check.Other, Side.Sender));
        binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
        adapter.notifyDataSetChanged();
        receiver(message);
    }

     private void receiverFile(String message, Uri selectedUrl, Check fFile) {
          messages.add(new Message(message, selectedUrl, fFile, Side.Receiver));
          binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
          //adapter.notifyDataSetChanged();
      }

      private void sendFile(String message, Uri selectedUrl, Check fFile) {
          messages.add(new Message(message, selectedUrl, fFile, Side.Sender));
          binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
          //adapter.notifyDataSetChanged();
          receiverFile("Hello my friend", selectedUrl, Check.ImageAndText);
      }

    private void receiverFileBit(String message, byte[] image, int width, int height, Check fFile) {
        messages.add(new Message(message, image, width, height, fFile, Side.Receiver));
        binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
        //adapter.notifyDataSetChanged();
    }

    private void sendFileBit(String message, byte[] image, int width, int height, Check fFile) {
        messages.add(new Message(message, image, width, height, fFile, Side.Sender));
        binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
        //adapter.notifyDataSetChanged();
        receiverFileBit("Hello my friend", image, width, height, Check.ImageAndText);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void openFile(byte[] image, int width, int height) {
        sendFileBit("Hello my friend", image, width, height, Check.ImageAndText);
       binding.recyclerView.getAdapter().notifyItemInserted(messages.size() - 1);

        //adapter.notifyDataSetChanged();
    }

    @Override
    public void openFile(String url) {
        sendFile("Hello my friend", Uri.parse(url), Check.ImageAndText);
        //adapter.notifyDataSetChanged();

    }
}