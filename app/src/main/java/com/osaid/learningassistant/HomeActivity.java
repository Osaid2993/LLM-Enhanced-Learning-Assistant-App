package com.osaid.learningassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.osaid.learningassistant.data.AppDatabase;
import com.osaid.learningassistant.data.SessionManager;
import com.osaid.learningassistant.data.User;
import com.osaid.learningassistant.databinding.ActivityHomeBinding;
import com.osaid.learningassistant.model.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private SessionManager session;
    private AppDatabase db;

    private final List<Task> tasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.getRoot().startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        if (!session.isLoggedIn()) {
            goToWelcome();
            return;
        }

        User user = db.userDao().findById(session.getUserId());
        if (user == null) {
            session.clearSession();
            goToWelcome();
            return;
        }

        binding.userNameText.setText(user.username);
        generateTasksFromInterests(user.interests);
        binding.tasksDueText.setText(getString(R.string.tasks_due, tasks.size()));

        binding.profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            }
        });

        TaskAdapter adapter = new TaskAdapter(tasks, new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                Intent intent = new Intent(HomeActivity.this, TaskActivity.class);
                intent.putExtra("task_title", task.title);
                intent.putExtra("task_description", task.description);
                intent.putExtra("task_topic", task.topic);
                startActivity(intent);
            }
        });

        binding.tasksRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.tasksRecycler.setAdapter(adapter);
    }

    private void generateTasksFromInterests(String interestsCsv) {
        tasks.clear();
        if (interestsCsv == null || interestsCsv.isEmpty()) {
            return;
        }

        List<String> interests = new ArrayList<>(Arrays.asList(interestsCsv.split(",")));
        Collections.shuffle(interests);

        int count = Math.min(interests.size(), 5);
        for (int i = 0; i < count; i++) {
            String topic = interests.get(i).trim();
            String title = topic + " Quiz";
            String description = "A short AI generated quiz to test your knowledge of " + topic + ".";
            tasks.add(new Task(title, description, topic));
        }
    }

    private void goToWelcome() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}