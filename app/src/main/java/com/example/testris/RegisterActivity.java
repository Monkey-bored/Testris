package com.example.testris;


import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    Button register,returnButton, btnTakePic;
    String email,password,address;
    boolean checked, pictureTaken = false; //is radio button checked
    Bitmap bitmap;
    ImageView iv;
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
    }
    private void regWithFB(String email, String password, HashMap<String,Object> details){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful())
                    {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Success",
                                Toast.LENGTH_SHORT).show();
                        //Upload image
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference imageRef = storageRef.child("images/" + mAuth.getUid() + ".jpg");
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();
                        UploadTask uploadTask = imageRef.putBytes(data);
                        uploadTask.addOnSuccessListener(taskSnapshot ->
                                Log.d(TAG, "Image uploaded successfully: " + taskSnapshot.getMetadata().getPath()))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to upload image: " + e.getMessage()));


                        details.put("Address", address);
                        details.put("Age", ((EditText) findViewById(R.id.age)).getText().toString());
                        details.put("Name", ((EditText) findViewById(R.id.name)).getText().toString());
                        details.put("Score", 0);
                        FirebaseFirestore.getInstance()
                                .collection("user")
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    int size = queryDocumentSnapshots.size();
                                    details.put("index", String.valueOf(size));
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection("user")
                                            .document(Objects.requireNonNull(mAuth.getUid()))
                                            .set(details)
                                            .addOnSuccessListener(unused -> {
                                                Log.d("Success!", "ahhh");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("AHHH", e.toString());
                                            });
                                }).addOnFailureListener(e -> Log.e("Error", e.toString()));
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);

                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Toast.makeText(this, "You are already logged in!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
        }
        register = findViewById(R.id.register);
        returnButton = findViewById(R.id.returnButton);
        RadioGroup radioGroup = findViewById(R.id.address);

        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
        });
        final HashMap<String,Object> details = new HashMap<>();

        iv=findViewById(R.id.iv);
        btnTakePic=findViewById(R.id.btn_pic);
        btnTakePic.setOnClickListener(this);

        register.setOnClickListener(v ->{
            email = ((EditText) findViewById(R.id.email)).getText().toString();
            password = ((EditText)findViewById(R.id.password)).getText().toString();
            if (!pictureTaken) Toast.makeText(RegisterActivity.this, "please take a picture", Toast.LENGTH_SHORT).show();
            else {
                boolean hasName = !((EditText) findViewById(R.id.name)).getText().toString().isEmpty();
                boolean hasAge = !((EditText) findViewById(R.id.age)).getText().toString().isEmpty();
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()
                        && !password.contains(" ")
                        && password.length() > 6
                        && hasName
                        && checked
                        && hasAge
                ) {
                    regWithFB(email, password, details);
                } else if (hasName) {
                    Toast.makeText(this,
                            "Please enter a your age ,your email address,your location and your password must be over 6 letters ",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void onRadioButtonClicked(View view){
        checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radio_america:
                address = "America";
                break;
            case R.id.radio_asia:
                address = "Asia";
                break;
            case R.id.radio_australia:
                address = "Australia";
                break;
            case R.id.radio_europe:
                address = "Europe";
                break;
            case R.id.radio_else:
                address = "else";
                break;
            default:
                Toast.makeText(RegisterActivity.this, "Please choose your location", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    public void onClick(View v) {
        if(v==btnTakePic)
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent,0);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0)//coming from camera
        {
            if (resultCode == RESULT_OK) {
                pictureTaken = true;
                bitmap = (Bitmap) data.getExtras().get("data");
                iv.setImageBitmap(bitmap);
            }
        }
    }

}