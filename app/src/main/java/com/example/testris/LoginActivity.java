package com.example.testris;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Button loginButton,returnButton;
    String email,password;
    private void loginWithFB(String email, String password){
        mAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(this , task ->{
            Toast.makeText(this,"Successfully logged in", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }).addOnFailureListener(e -> {
            Log.e("Error", e.toString());
            Toast.makeText(this,
                    "Something went wrong, make sure your email and password are correct",
                    Toast.LENGTH_SHORT);
        });
    }
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Toast.makeText(this, "You are already logged in!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
        loginButton = findViewById(R.id.loginButton);
        returnButton = findViewById(R.id.returnButton);
        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        });
        loginButton.setOnClickListener(v -> {
            email =  ((EditText) findViewById(R.id.email)).getText().toString();
            password =  ((EditText) findViewById(R.id.password)).getText().toString();
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    && !password.contains(" ")
                    && password.length() > 6) {
                loginWithFB(email,password);
            } else {
                Toast.makeText(this, "Email/password are not valid", Toast.LENGTH_SHORT);
            }
        });
    }
}