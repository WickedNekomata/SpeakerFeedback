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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_room);

        edit_room = findViewById(R.id.edit_room);
    }

    public void onEnterRoom(View view) {

        db.collection("rooms").document(edit_room.getText().toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists() && documentSnapshot.contains("password")) {
                    String password = documentSnapshot.getString("password");

                    if (!password.isEmpty())
                        // Introduce the password
                        enterPassword(documentSnapshot.getString("name"), password);
                    else
                        // Enter the room
                        enterRoom();
                }
            }
        });
    }


    private void enterPassword(String name, final String password) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(String.format("Please enter the password for the room '%s':", name));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().equals(password)) {
                    // Enter the room
                    enterRoom();
                }
                else
                    Toast.makeText(ChooseRoomActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void enterRoom() {

        Intent data = new Intent();
        //data.putExtra("name", name);
        setResult(RESULT_OK, data);
        finish();
    }
}