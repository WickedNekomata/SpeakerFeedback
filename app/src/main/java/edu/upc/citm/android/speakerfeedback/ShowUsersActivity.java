package edu.upc.citm.android.speakerfeedback;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ShowUsersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_users);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView user_view;

        public ViewHolder(View itemView){
            super(itemView);

        }

    }
}
