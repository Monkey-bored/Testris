package com.example.testris;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Button register,login,logOut,profilePage,usersPage, adminPage, sendFeedback;
    String greeting;
    TextView helloMsg;
    Map<String, Object> details = new HashMap<>();
    public BottomNavigationView navigationView;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        helloMsg = findViewById(R.id.greetings);
        register = findViewById(R.id.register);
        login = findViewById(R.id.login);
        logOut = findViewById(R.id.logout);
        profilePage = findViewById(R.id.profile);
        usersPage = findViewById(R.id.showUsers);
        adminPage = findViewById(R.id.adminPage);
        sendFeedback = findViewById(R.id.feedbackPage);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("user")
                .orderBy("Score", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the user ID and score of the highest scorer
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String userId = document.getId();
                        int score = Integer.parseInt(document.get("Score").toString());

                        // Check if the highest score is different from the current high scorer
                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        int currentHighScore = prefs.getInt("highScore", 0);
                        String currentHighScorer = prefs.getString("highScorer", "");

                        if (currentHighScorer.equals("")) {
                            // If this is the first time, store the highest score and scorer in SharedPreferences
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("highScore", score);
                            editor.putString("highScorer", userId);
                            editor.apply();

                        } else if (score > currentHighScore && !userId.equals(currentHighScorer)) {
                            // Store the new high score and scorer in SharedPreferences
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("highScore", score);
                            editor.putString("highScorer", userId);
                            editor.apply();

                            // Save the userId locally in SharedPreferences
                            SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor userEditor = userPrefs.edit();
                            userEditor.putString("userId", userId);
                            userEditor.apply();

                            // Show a dialog message indicating that a new high scorer has been crowned
                            Dialog dialog;
                            dialog = new Dialog(MainActivity.this);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.scoredialog);
                            Button closeDialog = dialog.findViewById(R.id.closeDialog);
                            dialog.show();
                            closeDialog.setOnClickListener(v -> dialog.dismiss());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                });
        //sets the bottom navigation menu

        navigationView = findViewById(R.id.bottomNav);
        navigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.gameButton) {
                startIntent(GameActivity.class);
                return true;
            }
            return false;
        });
        invalidateOptionsMenu();

        //Add a greeting message with the users name
        if (mAuth.getUid() != null) {
            register.setVisibility(View.GONE);
            login.setVisibility(View.GONE);
            if (mAuth.getUid().equals("yEkrmY0mKqaATAX3ujAfiGj3zSF2")){
                adminPage.setOnClickListener(v -> startIntent(AdminPage.class));
                adminPage.setVisibility(View.VISIBLE);
                greeting = "HELLO ADMINISTRATOR";
                helloMsg.setText(greeting);
                register.setOnClickListener(v -> startIntent(RegisterActivity.class));
                login.setOnClickListener(v-> startIntent(LoginActivity.class));
            } else {
                adminPage.setVisibility(View.GONE);
                FirebaseFirestore.getInstance().collection("user").document(Objects.requireNonNull(mAuth.getUid())).get().addOnSuccessListener(task -> {
                    details = task.getData();
                    if (details == null) {
                        startIntent(MainActivity.class);
                    } else {
                        greeting = "Hello " + details.get("Name");
                        helloMsg.setText(greeting);
                    }
                });
            }
            logOut.setOnClickListener(v -> logOut());
            profilePage.setOnClickListener(v->{startIntent(ProfileActivity.class);});
            sendFeedback.setOnClickListener(v -> startIntent(SendFeeback.class));
        } else {
            sendFeedback.setVisibility(View.GONE);
            adminPage.setVisibility(View.GONE);
            helloMsg.setText("Hey Gamer");
            logOut.setVisibility(View.GONE);
            profilePage.setVisibility(View.GONE);
            register.setOnClickListener(v -> startIntent(RegisterActivity.class));
            login.setOnClickListener(v-> startIntent(LoginActivity.class));

        }

        usersPage.setOnClickListener(v-> startIntent(ShowUsers.class));

    }


    private void startIntent(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        startActivity(intent);
        finish();
    }
    private void logOut(){
        if (FirebaseAuth.getInstance().getUid() == null) {
            Toast.makeText(this,"You are not logged in", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this,"Signed out", Toast.LENGTH_SHORT).show();
            startIntent(MainActivity.class);
        }
    }

}