package com.example.cube;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.cube.adapters.MessagesAdapter;
import com.example.cube.control.Side;
import com.example.cube.models.Message;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.cube.databinding.ActivityChatBinding;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;

    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String receiverUid;

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
                    binding.messageBox.setText("");
                }
            }
        });

        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 25);
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
        messages.add(new Message(message, Side.Receiver));
        binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
        adapter.notifyDataSetChanged();
    }

    private void sender(String message) {
        messages.add(new Message(message, Side.Sender));
        binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
        adapter.notifyDataSetChanged();
        receiver(message);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 25) {
            if (data != null) {
                if (data.getData() != null) {
                    try {
                        Uri selectedUrl = data.getData();
                        String filePath = selectedUrl.toString();
                        Message message = new Message("",selectedUrl, Side.Sender);
                        //message.setImageUrl(filePath);
                        messages.add(message);
                        //binding.messageBox.setText("");
                        binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                    }
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

}