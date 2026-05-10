package com.osaid.learningassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.osaid.learningassistant.data.AppDatabase;
import com.osaid.learningassistant.data.QuizHistory;
import com.osaid.learningassistant.data.SessionManager;
import com.osaid.learningassistant.data.User;
import com.osaid.learningassistant.databinding.ActivityProfileBinding;
import com.osaid.learningassistant.network.ApiClient;
import com.osaid.learningassistant.network.ApiService;
import com.osaid.learningassistant.network.SummaryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private AppDatabase db;
    private SessionManager session;
    private int totalQuestions;
    private int correctCount;
    private int incorrectCount;
    private String username;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.getRoot().startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        int userId = session.getUserId();
        if (userId == -1) {
            finish();
            return;
        }

        User user = db.userDao().findById(userId);
        if (user == null) {
            finish();
            return;
        }

        username = user.username;
        email = user.email;

        binding.profileUsername.setText(username);
        binding.profileEmail.setText(email);

        loadStats(userId);

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.summarizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSummary();
            }
        });

        binding.historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, HistoryActivity.class));
            }
        });

        binding.upgradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, UpgradeActivity.class));
            }
        });

        binding.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareProfile();
            }
        });
    }

    private void loadStats(int userId) {
        totalQuestions = db.quizHistoryDao().getTotalQuestions(userId);
        correctCount = db.quizHistoryDao().getCorrectCount(userId);
        incorrectCount = db.quizHistoryDao().getIncorrectCount(userId);

        binding.totalQuestionsCount.setText(String.valueOf(totalQuestions));
        binding.correctCount.setText(String.valueOf(correctCount));
        binding.incorrectCount.setText(String.valueOf(incorrectCount));
    }

    private void requestSummary() {
        int userId = session.getUserId();
        List<QuizHistory> incorrectList = db.quizHistoryDao().getHistoryForUser(userId);

        StringBuilder wrongQuestions = new StringBuilder();
        int count = 0;
        for (QuizHistory entry : incorrectList) {
            if (!entry.isCorrect) {
                wrongQuestions.append("Q: ").append(entry.question)
                        .append(" | Your answer: ").append(entry.userAnswer)
                        .append(" | Correct: ").append(entry.correctAnswer)
                        .append("\n");
                count++;
                if (count >= 10) break;
            }
        }

        if (count == 0) {
            Toast.makeText(this, "No incorrect answers to summarize", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.summaryProgress.setVisibility(View.VISIBLE);
        binding.aiSummaryText.setVisibility(View.GONE);

        ApiService api = ApiClient.getApiService();
        ApiService.SummaryRequest body = new ApiService.SummaryRequest(wrongQuestions.toString());

        api.getSummary(body).enqueue(new Callback<SummaryResponse>() {
            @Override
            public void onResponse(@NonNull Call<SummaryResponse> call, @NonNull Response<SummaryResponse> response) {
                binding.summaryProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().summary != null) {
                    binding.aiSummaryText.setText(response.body().summary);
                    binding.aiSummaryText.setVisibility(View.VISIBLE);
                    binding.aiSummaryText.setAlpha(0f);
                    binding.aiSummaryText.animate().alpha(1f).setDuration(300).start();
                } else {
                    Toast.makeText(ProfileActivity.this, "Could not load summary", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SummaryResponse> call, @NonNull Throwable t) {
                binding.summaryProgress.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareProfile() {
        String shareText = "Learning Assistant Profile\n\n"
                + "Student: " + username + "\n"
                + "Total Questions: " + totalQuestions + "\n"
                + "Correct Answers: " + correctCount + "\n"
                + "Incorrect Answers: " + incorrectCount + "\n\n"
                + "Shared from Learning Assistant App";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Learning Assistant Profile");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share your profile"));
    }
}