package com.swift.birdsofafeather;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.swift.birdsofafeather.model.db.AppDatabase;
import com.swift.birdsofafeather.model.db.Session;
import com.swift.birdsofafeather.model.db.SessionStudent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StartSearchPage extends AppCompatActivity {
    private static final String TAG = "CourseDashboard";
    private AppDatabase db;
    private UUID studentId;
    private RecyclerView sessionRecyclerView;
    private RecyclerView.LayoutManager sessionLayoutManager;
    private SessionViewAdapter sessionViewAdapter;
    private final ExecutorService backgroundThreadExecutor = Executors.newSingleThreadExecutor();
    private Future future;

    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);



        SharedPreferences preferences = Utils.getSharedPreferences(this);
        if (!getIntent().hasExtra("viewing")) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title
            alertDialogBuilder.setTitle("Choose your session");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Do you want to continue from your existing session or start a new session?")
                    .setCancelable(false)
                    .setPositiveButton("New Session", (dialog, id) -> {
                        // if this button is clicked, close
                        // current activity
                        UUID newSessionId = UUID.randomUUID();
                        Session newSession = new Session(newSessionId);
                        db.sessionDao().insert(newSession);

                        Log.d(TAG, "Session object name: " + newSession.getName());
                        Log.d(TAG, "Session database name: " + db.sessionDao().getName(newSessionId));

                        SessionStudent studentInSession = new SessionStudent(newSessionId, studentId);
                        db.sessionStudentDao().insert(studentInSession);

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("current_session_id", newSessionId.toString());
                        editor.apply();

                        dialog.dismiss();

                        future.cancel(true);
                        finish();
                    }).setNegativeButton("Continue from existing session", null);

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

            Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setOnClickListener(view -> {
                if (db.sessionDao().getAllSessions().size() == 0) {
                    Toast.makeText(getApplicationContext(), "You have no existing sessions!", Toast.LENGTH_SHORT).show();
                } else {
                    alertDialog.dismiss();
                }
            });

            getIntent().removeExtra("viewing");
        }

        db = AppDatabase.singleton(getApplicationContext());

        backButton = findViewById(R.id.back_button);


        String UUIDString = preferences.getString("student_id", "");
        studentId = UUID.fromString(UUIDString);

        List<Session> mySessions = db.sessionDao().getAllSessions();

        if (!mySessions.isEmpty()) {
            backButton.setVisibility(View.GONE);
        }

        this.future = backgroundThreadExecutor.submit(() -> runOnUiThread(() -> {
            // Set up the recycler view to show our database contents
            sessionRecyclerView = findViewById(R.id.persons_view);

            sessionLayoutManager = new LinearLayoutManager(this);
            sessionRecyclerView.setLayoutManager(sessionLayoutManager);

            sessionViewAdapter = new SessionViewAdapter(mySessions);
            sessionRecyclerView.setAdapter(sessionViewAdapter);
        }));
    }

    public void onGoBackClicked(View view) {
        finish();
    }
}
