package com.example.testris;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore instance;
    Map<String, Object> details = new HashMap<>();
    TextView age,name,address;
    String newName, newAge, updatedAddress;
    Button updateData,deleteUser,returnButton, btnTakePic;
    ImageView currentImage, iv;
    Bitmap bitmap;
    boolean pictureTaken, checked = false;
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        // defining some variables
        instance = FirebaseFirestore.getInstance();
        name = findViewById(R.id.name);
        updateData = findViewById(R.id.commenceUpdate);
        age = findViewById(R.id.Age);
        address = findViewById(R.id.Address);
        RadioGroup radioGroup = findViewById(R.id.newAddress);
        deleteUser = findViewById(R.id.deleteUser);

        // update picture
        iv = findViewById(R.id.iv);
        btnTakePic=findViewById(R.id.btn_pic);
        btnTakePic.setOnClickListener(this);

        //Set the image to the current image if exists
        currentImage = findViewById(R.id.currentImage);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(
                "images/" +
                        mAuth.getUid() +
                        ".jpg");
        imageRef.toString();
        imageRef.getBytes(1024*1024)//1024*1024 = 1mb
                .addOnSuccessListener(bytes -> {
                    bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    currentImage.setImageBitmap(bitmap);
                });

        instance.collection("user").document(Objects.requireNonNull(mAuth.getUid())).get().addOnSuccessListener(task -> {
                details = task.getData();
                name.setText("current name: " + details.get("Name").toString());
                age.setText("current age: " + details.get("Age").toString());
                address.setText("current address: " + details.get("Address").toString());
            });
        returnButton = findViewById(R.id.returnButton);
        returnButton.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            });
            updateData.setOnClickListener(v -> {


                if (pictureTaken) {
                    FirebaseStorage storageUpdate = FirebaseStorage.getInstance();
                    StorageReference storageRefUpdate = storageUpdate.getReference();
                    StorageReference imageRefUpdate = storageRefUpdate.child("images/" + mAuth.getUid() + ".jpg");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();
                    UploadTask uploadTask = imageRefUpdate.putBytes(data);
                    uploadTask.addOnSuccessListener(taskSnapshot ->
                                    Log.d(TAG, "Image uploaded successfully: " + taskSnapshot.getMetadata().getPath()))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to upload image: " + e.getMessage()));
                }
                newName = ((EditText) findViewById(R.id.newName)).getText().toString();
                if (!newName.isEmpty()) details.put("Name",newName);

                newAge = ((EditText) findViewById(R.id.newAge)).getText().toString();
                if (!newAge.isEmpty()) details.put("Age", newAge);

                if (checked) details.put("Address", updatedAddress);

                if (newAge.isEmpty() && newName.isEmpty() && !checked && !pictureTaken){
                    Toast.makeText(ProfileActivity.this , "Please enter something to update", Toast.LENGTH_SHORT).show();
                } else {
                    instance.collection("user").document(mAuth.getUid()).set(details);
                    Intent main = new Intent(this, MainActivity.class);
                    startActivity(main);
                }
            });
            deleteUser.setOnClickListener(v -> createDeleteDialog());
    }
    public void createDeleteDialog() {
        Dialog dialog;
        Button yes,no;
        dialog = new Dialog(ProfileActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.deleteuserdialog);
        yes = dialog.findViewById(R.id.yes);
        no = dialog.findViewById(R.id.no);
        dialog.show();
        yes.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(ProfileActivity.this, "Deleting user, Bye bye", Toast.LENGTH_SHORT).show();
            deleteUser();
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
        });
        no.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(ProfileActivity.this, "User deletion cancelled", Toast.LENGTH_SHORT).show();
        });
    }
    public void deleteUser(){
        mAuth.getCurrentUser().delete();
        instance.collection("user").document(mAuth.getUid()).delete();
        instance.collection("user").get().addOnSuccessListener(collection ->{
            int counter = 0;
            for (QueryDocumentSnapshot i:collection) {
                i.getData().put("Index", counter);
                counter++;
            }
        }).addOnFailureListener(task -> Toast.makeText(ProfileActivity.this,
                "uh oh something went wrong looks like you cant delete your user",
                Toast.LENGTH_SHORT));
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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
    public void onRadioButtonClicked(View view) {
        checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radio_america:
                updatedAddress = "America";
                break;
            case R.id.radio_asia:
                updatedAddress = "Asia";
                break;
            case R.id.radio_australia:
                updatedAddress = "Australia";
                break;
            case R.id.radio_europe:
                updatedAddress = "Europe";
                break;
            case R.id.radio_else:
                updatedAddress = "else";
                break;
            default:
                Toast.makeText(ProfileActivity.this, "Please choose your location", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}