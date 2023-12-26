package com.example.testris;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShowUsers extends AppCompatActivity {
    private FirebaseFirestore instance;
    private RecyclerView userList;
    private List<User> users;
    Button returnButton;
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(ShowUsers.this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_users);
        instance = FirebaseFirestore.getInstance();
        userList = findViewById(R.id.usersList);
        userList.setLayoutManager(new LinearLayoutManager(this));
        returnButton = findViewById(R.id.returnButton);
        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(ShowUsers.this, MainActivity.class);
            startActivity(intent);
        });


        FirebaseFirestore.getInstance().collection("user")
                .orderBy("Score", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(task -> {
                    users = new ArrayList<>();
                    for (QueryDocumentSnapshot i : task) {
                        User user = new User(i.getData().get("Name").toString(),
                                i.getData().get("Address").toString(),
                                i.getData().get("Score").toString());
                        users.add(user);
                    }
                    Collections.sort(users, new Comparator<User>() {
                        @Override
                        public int compare(User o1, User o2) {
                            return Integer.parseInt(o2.getScore()) - Integer.parseInt(o1.getScore());
                        }
                    });
                    userList.setAdapter(new RecyclerViewAdapter(users));
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
                });
    }
}
