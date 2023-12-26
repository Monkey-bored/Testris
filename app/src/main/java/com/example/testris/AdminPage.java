package com.example.testris;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private RecyclerView mRecyclerView;
    private MessageAdapter mAdapter;
    private List<Message> mMessagesList;
    private Button returnBtn;
    private String mAdminUID = "ayEkrmY0mKqaATAX3ujAfiGj3zSF2"; // The UID of the admin user
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(AdminPage.this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_page);
        returnBtn = findViewById(R.id.returnButton);
        returnBtn.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPage.this, MainActivity.class);
            startActivity(intent);
        });
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Find the RecyclerView in the layout
        mRecyclerView = findViewById(R.id.recycler_view);

        // Set the layout manager for the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);

        // Initialize the adapter and the list of messages
        mMessagesList = new ArrayList<>();
        mAdapter = new MessageAdapter(this, mMessagesList, mAdminUID);
        mRecyclerView.setAdapter(mAdapter);

        // Retrieve messages from Firestore and update the adapter
        CollectionReference messagesRef = mFirestore.collection("messages");
        messagesRef.orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                // Handle the error
                return;
            }
            mMessagesList.clear();
            for (QueryDocumentSnapshot document : querySnapshot) {
                Message message = document.toObject(Message.class);
                mMessagesList.add(message);
            }
            mAdapter.notifyDataSetChanged();
        });
    }

    // Method to delete a message from Firestore

}
