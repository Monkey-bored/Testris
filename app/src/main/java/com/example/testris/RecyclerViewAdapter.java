package com.example.testris;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>   {
    private List<User> users;

    public RecyclerViewAdapter(List<User> users){
        this.users = users;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView,emailTextView,scoreTextView;

        public ViewHolder(ViewGroup container) {
            super(LayoutInflater.from(container.getContext()).inflate((R.layout.recyclerlayout), container, false));
            nameTextView = itemView.findViewById(R.id.name);
            emailTextView = itemView.findViewById(R.id.email);
            scoreTextView = itemView.findViewById(R.id.scoreText);
        }

        public void bind(User user){
            nameTextView.setText(user.getName());
            emailTextView.setText(user.getAddress());
            scoreTextView.setText(user.getScore());
        }
    }
}
