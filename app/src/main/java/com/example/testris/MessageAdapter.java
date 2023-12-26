package com.example.testris;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Inner class for the adapter
class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context mContext;
    private List<Message> mMessagesList;
    private String mAdminUID;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private CollectionReference mMessagesRef = mFirestore.collection("messages");

    public MessageAdapter(Context context, List<Message> messagesList, String adminUID) {
        mContext = context;
        mMessagesList = messagesList;
        mAdminUID = adminUID;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = mMessagesList.get(position);

        // Set the message text
        holder.messageTextView.setText(message.getText());

        // Set the message timestamp
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
        String timestamp = formatter.format(new Date(message.getTimestamp()));
        holder.timestampTextView.setText(timestamp);

        // Hide the delete button if the user is not the admin
//        if (!mAuth.getCurrentUser().getUid().equals(mAdminUID)) {
//            holder.deleteButton.setVisibility(View.GONE);
//        }

        // Add a click listener to the delete button to delete the message
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true) {
                    mMessagesRef.document(message.getId()).delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(mContext, "Message deleted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(mContext, "Failed to delete message", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    // Inner class for the view holder
    static class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView messageTextView;
        private TextView timestampTextView;
        private ImageButton deleteButton;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageTextView = itemView.findViewById(R.id.message_text_view);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
