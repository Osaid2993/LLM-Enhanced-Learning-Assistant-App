package com.osaid.learningassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.osaid.learningassistant.databinding.ActivityTaskBinding;
import com.osaid.learningassistant.network.ApiClient;
import com.osaid.learningassistant.network.ApiService;
import com.osaid.learningassistant.network.QuizResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskActivity extends AppCompatActivity {

    private ActivityTaskBinding binding;
    private QuestionAdapter adapter;
    private final List<QuizResponse.Question> questions = new ArrayList<>();
    private String taskTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.getRoot().startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));

        Intent incoming = getIntent();
        String title = incoming.getStringExtra("task_title");
        String description = incoming.getStringExtra("task_description");
        taskTopic = incoming.getStringExtra("task_topic");

        if (taskTopic == null) {
            Toast.makeText(this, "No topic provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.taskTitleText.setText(title != null ? title : "Generated Task");
        binding.taskDescriptionText.setText(description != null ? description : "");

        adapter = new QuestionAdapter(questions);
        binding.questionsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.questionsRecycler.setAdapter(adapter);

        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAnswers();
            }
        });

        binding.retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchQuiz();
            }
        });

        fetchQuiz();
    }

    private void fetchQuiz() {
        showLoading();

        ApiService api = ApiClient.getApiService();
        api.getQuiz(taskTopic).enqueue(new Callback<QuizResponse>() {
            @Override
            public void onResponse(@NonNull Call<QuizResponse> call, @NonNull Response<QuizResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().quiz == null || response.body().quiz.isEmpty()) {
                    showError("The AI didn't return any questions. Try again.");
                    return;
                }

                questions.clear();
                questions.addAll(response.body().quiz);
                adapter.notifyDataSetChanged();
                showContent();
            }

            @Override
            public void onFailure(@NonNull Call<QuizResponse> call, @NonNull Throwable t) {
                showError("Could not reach the server. Make sure the Flask backend is running.");
            }
        });
    }

    private void submitAnswers() {
        Map<Integer, String> answers = adapter.getSelectedAnswers();

        if (answers.size() < questions.size()) {
            Toast.makeText(this, "Please answer all questions before submitting", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> userAnswersOrdered = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            String ans = answers.get(i);
            userAnswersOrdered.add(ans != null ? ans : "");
        }

        Gson gson = new Gson();
        String questionsJson = gson.toJson(questions);
        String userAnswersJson = gson.toJson(userAnswersOrdered);

        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("questions_json", questionsJson);
        intent.putExtra("user_answers_json", userAnswersJson);
        startActivity(intent);
    }

    private void showLoading() {
        binding.loadingContainer.setVisibility(View.VISIBLE);
        binding.questionsRecycler.setVisibility(View.GONE);
        binding.errorContainer.setVisibility(View.GONE);
        binding.submitContainer.setVisibility(View.GONE);
    }

    private void showContent() {
        binding.loadingContainer.setVisibility(View.GONE);
        binding.errorContainer.setVisibility(View.GONE);
        binding.questionsRecycler.setVisibility(View.VISIBLE);
        binding.submitContainer.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        binding.errorText.setText(message);
        binding.loadingContainer.setVisibility(View.GONE);
        binding.questionsRecycler.setVisibility(View.GONE);
        binding.submitContainer.setVisibility(View.GONE);
        binding.errorContainer.setVisibility(View.VISIBLE);
    }
}