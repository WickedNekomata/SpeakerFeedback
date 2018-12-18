package edu.upc.citm.android.speakerfeedback;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChooseRoomActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText edit_room;

    private App app;

    private Adapter adapter;
    private RecyclerView rooms_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_room);

        app = (App)getApplication();
        edit_room = findViewById(R.id.edit_room);

        rooms_view = findViewById(R.id.rooms_view);
        adapter = new Adapter();

        rooms_view.setLayoutManager(new LinearLayoutManager(this));
        rooms_view.setAdapter(adapter);
    }

    public void onEnterRoom(View view) {

        final String roomID = edit_room.getText().toString();

        db.collection("rooms").document(roomID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists() && documentSnapshot.contains("open") && documentSnapshot.getBoolean("open")) {

                    if (documentSnapshot.contains("password") && !documentSnapshot.getString("password").isEmpty())
                        // Introduce the password
                        enterPassword(documentSnapshot, roomID);

                    else
                        // Enter the room
                        enterRoom(documentSnapshot.getString("name"), roomID);
                }
            }
        });
    }

    private void enterPassword(final DocumentSnapshot documentSnapshot, final String roomID) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final String roomName = documentSnapshot.getString("name");
        builder.setTitle(String.format("Please enter the password for the room '%s':", roomName));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().equals(documentSnapshot.getString("password")))
                    // Enter the room
                    enterRoom(roomName, roomID);
                else
                    Toast.makeText(ChooseRoomActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void enterRoom(String roomName, String roomID) {
        app.setRoomId(roomID);

        App.Room room = new App.Room(roomName, roomID);
        app.addRoom(room);

        Intent data = new Intent(this, MainActivity.class);
        startActivity(data);
        finish();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView room_name_view;
        private TextView room_id_view;

        public ViewHolder(View itemView) {
            super(itemView);

            room_name_view = itemView.findViewById(R.id.room_name_view);
            room_id_view = itemView.findViewById(R.id.room_id_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final App.Room room = app.recentRooms.get(getAdapterPosition());

                    db.collection("rooms").document(room.id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists() && documentSnapshot.contains("open") && documentSnapshot.getBoolean("open"))
                                enterRoom(room.name, room.id);
                        }
                    });
                }
            });
        }
    }

    class Adapter extends RecyclerView.Adapter<ChooseRoomActivity.ViewHolder> {

        @NonNull
        @Override
        public ChooseRoomActivity.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.room_view, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ChooseRoomActivity.ViewHolder holder, int position) {
            App.Room room = app.recentRooms.get(position);

            holder.room_name_view.setText(room.name);
            holder.room_id_view.setText(room.id);
        }

        @Override
        public int getItemCount() {
            return app.recentRooms.size();
        }
    }
}