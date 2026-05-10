package com.osaid.learningassistant;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.osaid.learningassistant.data.AppDatabase;
import com.osaid.learningassistant.data.QuizHistory;
import com.osaid.learningassistant.data.SessionManager;
import com.osaid.learningassistant.databinding.ActivityHistoryBinding;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.getRoot().startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));

        SessionManager session = new SessionManager(this);
        int userId = session.getUserId();

        if (userId == -1) {
            finish();
            return;
        }

        AppDatabase db = AppDatabase.getInstance(this);
        List<QuizHistory> history = db.quizHistoryDao().getHistoryForUser(userId);

        if (history.isEmpty()) {
            binding.emptyText.setVisibility(View.VISIBLE);
            binding.historyRecycler.setVisibility(View.GONE);
        } else {
            binding.emptyText.setVisibility(View.GONE);
            binding.historyRecycler.setVisibility(View.VISIBLE);
            HistoryAdapter adapter = new HistoryAdapter(history);
            binding.historyRecycler.setLayoutManager(new LinearLayoutManager(this));
            binding.historyRecycler.setAdapter(adapter);
        }

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}