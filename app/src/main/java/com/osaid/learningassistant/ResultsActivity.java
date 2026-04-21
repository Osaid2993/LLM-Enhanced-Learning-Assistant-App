package com.osaid.learningassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.osaid.learningassistant.databinding.ActivityResultsBinding;
import com.osaid.learningassistant.network.ApiClient;
import com.osaid.learningassistant.network.ApiService;
import com.osaid.learningassistant.network.ExplanationResponse;
import com.osaid.learningassistant.network.QuizResponse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultsActivity extends AppCompatActivity {

    private ActivityResultsBinding binding;
    private List<QuizResponse.Question> questions = new ArrayList<>();
    private List<String> userAnswers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent incoming = getIntent();
        String questionsJson = incoming.getStringExtra("questions_json");
        String userAnswersJson = incoming.getStringExtra("user_answers_json");

        Gson gson = new Gson();
        Type questionsType = new TypeToken<List<QuizResponse.Question>>() {}.getType();
        Type answersType = new TypeToken<List<String>>() {}.getType();

        questions = gson.fromJson(questionsJson, questionsType);
        userAnswers = gson.fromJson(userAnswersJson, answersType);

        if (questions == null || userAnswers == null || questions.isEmpty()) {
            finish();
            return;
        }

        int correctCount = 0;
        for (int i = 0; i < questions.size(); i++) {
            String correct = questions.get(i).correct_answer.trim().toUpperCase();
            String user = userAnswers.get(i).trim().toUpperCase();
            if (correct.equals(user)) {
                correctCount++;
            }
        }
        binding.scoreText.setText("You scored " + correctCount + " out of " + questions.size());

        ResultsAdapter adapter = new ResultsAdapter(questions, userAnswers);
        binding.resultsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.resultsRecycler.setAdapter(adapter);

        binding.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultsActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    static class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultViewHolder> {

        private final List<QuizResponse.Question> questions;
        private final List<String> userAnswers;

        ResultsAdapter(List<QuizResponse.Question> questions, List<String> userAnswers) {
            this.questions = questions;
            this.userAnswers = userAnswers;
        }

        @NonNull
        @Override
        public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
            return new ResultViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ResultViewHolder holder, int position) {
            final QuizResponse.Question q = questions.get(position);
            final String userAnswer = userAnswers.get(position);
            final String correctAnswer = q.correct_answer != null ? q.correct_answer.trim().toUpperCase() : "";
            boolean isCorrect = correctAnswer.equals(userAnswer.trim().toUpperCase());

            holder.questionNumber.setText((position + 1) + ".");
            holder.questionText.setText(q.question);
            holder.verdictBadge.setText(isCorrect ? "Correct" : "Incorrect");
            holder.answerSummary.setText("Your answer: " + (userAnswer.isEmpty() ? "-" : userAnswer)
                    + "   •   Correct answer: " + correctAnswer);

            holder.explanationText.setVisibility(View.GONE);
            holder.explanationProgress.setVisibility(View.VISIBLE);

            ApiService api = ApiClient.getApiService();
            ApiService.ExplanationRequest body = new ApiService.ExplanationRequest(
                    q.question, q.options, correctAnswer, userAnswer
            );

            api.explainAnswer(body).enqueue(new Callback<ExplanationResponse>() {
                @Override
                public void onResponse(@NonNull Call<ExplanationResponse> call, @NonNull Response<ExplanationResponse> response) {
                    holder.explanationProgress.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null && response.body().explanation != null) {
                        holder.explanationText.setText(response.body().explanation);
                    } else {
                        holder.explanationText.setText("Explanation unavailable.");
                    }
                    holder.explanationText.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure(@NonNull Call<ExplanationResponse> call, @NonNull Throwable t) {
                    holder.explanationProgress.setVisibility(View.GONE);
                    holder.explanationText.setText("Could not load explanation. Check your connection.");
                    holder.explanationText.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return questions.size();
        }

        static class ResultViewHolder extends RecyclerView.ViewHolder {
            TextView questionNumber;
            TextView verdictBadge;
            TextView questionText;
            TextView answerSummary;
            TextView explanationText;
            ProgressBar explanationProgress;

            ResultViewHolder(@NonNull View itemView) {
                super(itemView);
                questionNumber = itemView.findViewById(R.id.questionNumber);
                verdictBadge = itemView.findViewById(R.id.verdictBadge);
                questionText = itemView.findViewById(R.id.questionText);
                answerSummary = itemView.findViewById(R.id.answerSummary);
                explanationText = itemView.findViewById(R.id.explanationText);
                explanationProgress = itemView.findViewById(R.id.explanationProgress);
            }
        }
    }
}