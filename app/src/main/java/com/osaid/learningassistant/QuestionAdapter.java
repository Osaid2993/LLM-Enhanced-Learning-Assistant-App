package com.osaid.learningassistant;

import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.osaid.learningassistant.network.ApiClient;
import com.osaid.learningassistant.network.ApiService;
import com.osaid.learningassistant.network.HintResponse;
import com.osaid.learningassistant.network.QuizResponse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private final List<QuizResponse.Question> questions;
    private final Map<Integer, String> selectedAnswers = new HashMap<>();
    private final Set<Integer> expandedPositions = new HashSet<>();

    public QuestionAdapter(List<QuizResponse.Question> questions) {
        this.questions = questions;
        if (!questions.isEmpty()) {
            expandedPositions.add(0);
        }
    }

    public Map<Integer, String> getSelectedAnswers() {
        return selectedAnswers;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final QuestionViewHolder holder, int position) {
        final int questionIndex = position;
        final QuizResponse.Question q = questions.get(position);

        holder.questionText.setText((position + 1) + ". " + q.question);

        boolean isExpanded = expandedPositions.contains(position);
        holder.questionBody.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.expandArrow.setRotation(isExpanded ? 90f : 0f);

        holder.questionHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                boolean currentlyExpanded = expandedPositions.contains(pos);
                if (currentlyExpanded) {
                    expandedPositions.remove(pos);
                    animateCollapse(holder.questionBody);
                    holder.expandArrow.animate().rotation(0f).setDuration(200).start();

                } else {
                    expandedPositions.add(pos);
                    animateExpand(holder.questionBody);
                    holder.expandArrow.animate().rotation(90f).setDuration(200).start();
                }
            }
        });

        holder.optionsGroup.removeAllViews();
        holder.optionsGroup.setOnCheckedChangeListener(null);

        for (int i = 0; i < q.options.size(); i++) {
            RadioButton rb = new RadioButton(holder.itemView.getContext());
            String letter = String.valueOf((char) ('A' + i));
            rb.setText(letter + ". " + q.options.get(i));
            rb.setTextColor(holder.itemView.getResources().getColor(R.color.text_primary));
            rb.setButtonTintList(holder.itemView.getResources().getColorStateList(R.color.primary));
            rb.setTextSize(14f);
            rb.setPadding(dp(holder, 8), dp(holder, 6), 0, dp(holder, 6));
            rb.setId(View.generateViewId());
            rb.setTag(letter);
            holder.optionsGroup.addView(rb);

            String previouslySelected = selectedAnswers.get(questionIndex);
            if (previouslySelected != null && previouslySelected.equals(letter)) {
                rb.setChecked(true);
            }
        }

        holder.optionsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selected = group.findViewById(checkedId);
                if (selected != null && selected.getTag() != null) {
                    selectedAnswers.put(questionIndex, selected.getTag().toString());
                }
            }
        });

        holder.hintContainer.setVisibility(View.GONE);
        holder.hintProgress.setVisibility(View.GONE);
        holder.hintButton.setEnabled(true);

        holder.hintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestHint(holder, q);
            }
        });
    }

    private void animateExpand(final View view) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void animateCollapse(final View view) {
        view.animate()
                .alpha(0f)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        view.setVisibility(View.GONE);
                        view.setAlpha(1f);
                    }
                })
                .start();
    }

    private void requestHint(final QuestionViewHolder holder, QuizResponse.Question q) {
        holder.hintButton.setEnabled(false);
        holder.hintProgress.setVisibility(View.VISIBLE);
        holder.hintContainer.setVisibility(View.GONE);

        ApiService api = ApiClient.getApiService();
        ApiService.HintRequest body = new ApiService.HintRequest(q.question, q.options);

        api.getHint(body).enqueue(new Callback<HintResponse>() {
            @Override
            public void onResponse(@NonNull Call<HintResponse> call, @NonNull Response<HintResponse> response) {
                holder.hintProgress.setVisibility(View.GONE);
                holder.hintButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().hint != null) {
                    holder.hintText.setText(response.body().hint);
                    holder.hintContainer.setVisibility(View.VISIBLE);
                    holder.hintContainer.setAlpha(0f);
                    holder.hintContainer.animate().alpha(1f).setDuration(300).start();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Could not load hint", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<HintResponse> call, @NonNull Throwable t) {
                holder.hintProgress.setVisibility(View.GONE);
                holder.hintButton.setEnabled(true);
                Toast.makeText(holder.itemView.getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    private int dp(QuestionViewHolder holder, int value) {
        float density = holder.itemView.getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionText;
        LinearLayout questionHeader;
        LinearLayout questionBody;
        ImageView expandArrow;
        RadioGroup optionsGroup;
        Button hintButton;
        LinearLayout hintContainer;
        TextView hintText;
        ProgressBar hintProgress;

        QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.questionText);
            questionHeader = itemView.findViewById(R.id.questionHeader);
            questionBody = itemView.findViewById(R.id.questionBody);
            expandArrow = itemView.findViewById(R.id.expandArrow);
            optionsGroup = itemView.findViewById(R.id.optionsGroup);
            hintButton = itemView.findViewById(R.id.hintButton);
            hintContainer = itemView.findViewById(R.id.hintContainer);
            hintText = itemView.findViewById(R.id.hintText);
            hintProgress = itemView.findViewById(R.id.hintProgress);
        }
    }
}