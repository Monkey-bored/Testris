package com.example.testris;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class SendFeeback extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference messagesRef = db.collection("messages");

    private EditText mMessageInput;
    private Button mSendButton, returnButton;
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(SendFeeback.this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_feeback);

        mAuth = FirebaseAuth.getInstance();

        mMessageInput = findViewById(R.id.message_edit_text);
        mSendButton = findViewById(R.id.send_button);
        returnButton = findViewById(R.id.returnButton);
        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(SendFeeback.this, MainActivity.class);
            startActivity(intent);
        });
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mMessageInput.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                } else {
                    Toast.makeText(SendFeeback.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendMessage(String message) {
        if (!message.isEmpty()) {
            String id = messagesRef.document().getId();
            Date date = new Date();
            Message newMessage = new Message(id, message, date.getTime());

            messagesRef.document(id).set(newMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(SendFeeback.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                    mMessageInput.setText("");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SendFeeback.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(SendFeeback.this, "Message is empty", Toast.LENGTH_SHORT).show();
        }
    }
}