package edu.upc.citm.android.speakerfeedback;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ChooseRoomActivity extends AppCompatActivity {

    private EditText edit_room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_room);

        edit_room = findViewById(R.id.edit_room);
    }

    public void onEnterRoom(View view) {
        String name = edit_room.getText().toString();

        //Intent data = new Intent();
        //data.putExtra("name", name);
        //setResult(RESULT_OK, data);
        //finish();
    }
}
