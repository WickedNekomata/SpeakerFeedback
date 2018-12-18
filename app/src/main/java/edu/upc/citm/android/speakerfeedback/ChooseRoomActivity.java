package edu.upc.citm.android.speakerfeedback;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChooseRoomActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText edit_room;

    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_room);

        app = (App)getApplication();
        edit_room = findViewById(R.id.edit_room);
    }

    public void onEnterRoom(View view) {

        final String roomID = edit_room.getText().toString();

        db.collection("rooms").document(roomID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists() && documentSnapshot.contains("password")) {
                    if (!documentSnapshot.getString("password").isEmpty())
                        // Introduce the password
                        enterPassword(documentSnapshot, roomID);
                    else
                        // Enter the room
                        enterRoom(roomID);
                }
            }
        });
    }


    private void enterPassword(final DocumentSnapshot documentSnapshot, final String roomID) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(String.format("Please enter the password for the room '%s':", documentSnapshot.getString("name")));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().equals(documentSnapshot.getString("password")))
                    // Enter the room
                    enterRoom(roomID);
                else
                    Toast.makeText(ChooseRoomActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void enterRoom(String roomID) {
        app.setRoomId(roomID);
        Intent data = new Intent(this, MainActivity.class);
        startActivity(data);
        finish();
    }
}