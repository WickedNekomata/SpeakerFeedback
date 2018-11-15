package edu.upc.citm.android.speakerfeedback;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ShowUsersActivity extends AppCompatActivity {

    private List<String> users;
    private RecyclerView users_list_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_users);

        users_list_view = findViewById(R.id.users_list_view);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView user_view;

        public ViewHolder(View itemView) {
            super(itemView);
            this.user_view = itemView.findViewById(R.id.user_view);
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.activity_show_users, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.user_view.setText(users.get(position));
        }

        @Override
        public int getItemCount() {
            return users.size();
        }
    }
}