package edu.upc.citm.android.speakerfeedback;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REGISTER_USER = 0;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private TextView num_users_view;
    private String userId;
    private List<Poll> polls = new ArrayList<>();

    private RecyclerView polls_view;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        num_users_view = findViewById(R.id.num_users_view);

        getOrRegistrerUser();
        startFirestoreListenerService();

        polls_view = findViewById(R.id.polls_view);
        adapter = new Adapter();

        polls_view.setLayoutManager(new LinearLayoutManager(this));
        polls_view.setAdapter(adapter);
    }

    private void getOrRegistrerUser() {
        // Busquem a les preferències de l'app l'ID de l'usuari per saber si ja s'havia registrat
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        userId = prefs.getString("userId", null);
        if (userId == null) {
            // Hem de registrar l'usuari, demanem el nom
            Intent intent = new Intent(this, RegisterUserActivity.class);
            startActivityForResult(intent, REGISTER_USER);
            Toast.makeText(this, "Encara t'has de registrar", Toast.LENGTH_SHORT).show();
        }
        else {
            // Ja està registrat, mostrem el id al Log
            Log.i("SpeakerFeedback", "userId = " + userId);
            db.collection("users").document(userId).update("room", "testroom");
        }

        db.collection("users").document(userId).update("last_active", new Date());
    }

    private void startFirestoreListenerService() {
        Intent intent = new Intent(this, FirestoreListenerService.class);
        intent.putExtra("room", "testroom");
        startService(intent);
    }

    private void stopFirestoreListenerService() {
        Intent intent = new Intent(this, FirestoreListenerService.class);
        stopService(intent);
    }

    private EventListener<DocumentSnapshot> roomListener = new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("SpeakerFeedback","Error al rebre els 'rooms'",e);
                    return;
                }
                String name = documentSnapshot.getString("name");
                setTitle(name);
            }
        };

    private EventListener<QuerySnapshot> usersListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFeedback","Error al rebre els 'users'",e);
                return;
            }
            num_users_view.setText(String.format("Num users: %d", documentSnapshots.size()));

            String nomUsuaris = "";
            for (DocumentSnapshot doc : documentSnapshots) {
                nomUsuaris += doc.getString("name") + "\n";
            }
            //num_users_view.setText(nomUsuaris);
        }
    };

    private EventListener<QuerySnapshot> pollsListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFeedback", "Error al rebre els 'polls'", e);
                return;
            }
            polls.clear();
            for (DocumentSnapshot doc : documentSnapshots) {
                Poll poll = doc.toObject(Poll.class);
                poll.setId(doc.getId());
                polls.add(poll);
            }
            Log.i("SpeakerFeedback", String.format("He carregat %d polls", polls.size()));
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onStart() {
        db.collection("rooms").document("testroom")
                .addSnapshotListener(this, roomListener);
        db.collection("users").whereEqualTo("room", "testroom")
                .addSnapshotListener(this, usersListener);
        db.collection("rooms").document("testroom").collection("polls").orderBy("start", Query.Direction.DESCENDING)
                .addSnapshotListener(this, pollsListener);

        //db.collection("rooms").document("testroom").collection("votes").document(userId);
        super.onStart();
    }

    protected void onDestroy() {
        db.collection("users").document(userId).update("room", FieldValue.delete());
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REGISTER_USER:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra("name");
                    registerUser(name);
                } else {
                    Toast.makeText(this, "Has de registrar un nom", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void registerUser(String name) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", name);
        db.collection("users").add(fields).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                // textview.setText(documentReference.getId());
                userId = documentReference.getId();
                SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
                prefs.edit()
                        .putString("userId", userId)
                        .commit();
                Log.i("SpeakerFeedback", "New user: userId = " + userId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("SpeakerFeedback", "Error creant objecte", e);
                Toast.makeText(MainActivity.this,
                        "No s'ha pogut registrar l'usuari, intenta-ho més tard", Toast.LENGTH_SHORT).show();
                db.collection("users").document(userId).update("room", "testroom");
                finish();
            }
        });
    }

    public void ShowUsers(View view) {
        Intent intent = new Intent(this, ShowUsersActivity.class);
        startActivity(intent);
    }

    public void onAnswerPoll(final Poll poll) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(poll.getQuestion());
        builder.setIcon(R.drawable.ic_format_list_bulleted_white_24dp);

        List<String> pollOptions = poll.getOptions();
        String[] options = new String[pollOptions.size()];
        options = pollOptions.toArray(options);

        builder.setSingleChoiceItems(options, poll.getOptionClicked(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                poll.setOptionClicked(which);
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);

        builder.setPositiveButton("Answer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Vote vote = new Vote(poll.getId(), poll.getOptionClicked());
                db.collection("rooms").document("testroom").collection("votes").document(userId).set(vote);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        // Pas 1. Definim les views del ViewHolder
        private TextView question_view;
        private TextView options_view;
        private TextView label_view;
        private CardView card_view;

        public ViewHolder(View itemView) {
            super(itemView);

            question_view = itemView.findViewById(R.id.question_view);
            options_view = itemView.findViewById(R.id.options_view);
            label_view = itemView.findViewById(R.id.label_view);
            card_view = itemView.findViewById(R.id.card_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Poll poll = polls.get(getAdapterPosition());
                    if (poll.isOpen())
                        onAnswerPoll(poll);
                }
            });
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {

        // Pas 2. Creem el ViewHolder
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.poll_view, parent, false);
            return new ViewHolder(itemView);
        }

        // Pas 3. Omplim el ViewHolder creat amb les dades corresponents
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Poll poll = polls.get(position);
            if (position == 0) {
                holder.label_view.setVisibility(View.VISIBLE);
                if (poll.isOpen()) {
                    holder.label_view.setText("Active");
                } else {
                    holder.label_view.setText("Previous");
                }
            } else {
                if (!poll.isOpen() && polls.get(position - 1).isOpen()) {
                    holder.label_view.setVisibility(View.VISIBLE);
                    holder.label_view.setText("Previous");
                }
                else {
                    holder.label_view.setVisibility(View.GONE);
                }
            }

            holder.card_view.setCardElevation(poll.isOpen() ? 5.0f : 0.0f);

            if (!poll.isOpen()) {
                holder.card_view.setCardBackgroundColor(0XFFE0E0E0);
            }

            // Omplim la pregunta
            holder.question_view.setText(poll.getQuestion());

            // Omplim les opcions
            holder.options_view.setText(poll.getOptionsAsString());
        }

        @Override
        public int getItemCount() {
            return polls.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout_item:
                stopFirestoreListenerService();
                finish();
                break;
        }
        return true;
    }
}