package com.osaid.learningassistant;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.osaid.learningassistant.data.QuizHistory;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<QuizHistory> items;
    private final Set<Integer> expandedPositions = new HashSet<>();
    private final Gson gson = new Gson();

    public HistoryAdapter(List<QuizHistory> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HistoryViewHolder holder, int position) {
        final QuizHistory entry = items.get(position);

        holder.topicBadge.setText(entry.topic);
        holder.questionText.setText((position + 1) + ". " + entry.question);

        boolean isExpanded = expandedPositions.contains(position);
        holder.body.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.expandArrow.setRotation(isExpanded ? 180f : 0f);

        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> options = gson.fromJson(entry.optionsJson, listType);

        TextView[] optionViews = {holder.optionA, holder.optionB, holder.optionC, holder.optionD};
        String[] letters = {"A", "B", "C", "D"};

        for (int i = 0; i < optionViews.length; i++) {
            if (options != null && i < options.size()) {
                optionViews[i].setText(letters[i] + ". " + options.get(i));
                optionViews[i].setVisibility(View.VISIBLE);

                if (letters[i].equals(entry.correctAnswer)) {
                    optionViews[i].setTextColor(Color.parseColor("#4CAF50"));
                } else if (letters[i].equals(entry.userAnswer) && !entry.isCorrect) {
                    optionViews[i].setTextColor(Color.parseColor("#FF5252"));
                } else {
                    optionViews[i].setTextColor(holder.itemView.getResources().getColor(R.color.text_secondary));
                }
            } else {
                optionViews[i].setVisibility(View.GONE);
            }
        }

        if (entry.isCorrect) {
            holder.userAnswerBadge.setText("Your Answer: " + entry.userAnswer);
            holder.userAnswerBadge.setTextColor(Color.parseColor("#4CAF50"));
            holder.correctAnswerBadge.setVisibility(View.GONE);
        } else {
            holder.userAnswerBadge.setText("Your Answer: " + entry.userAnswer);
            holder.userAnswerBadge.setTextColor(Color.parseColor("#FF5252"));
            holder.correctAnswerBadge.setText("Correct: " + entry.correctAnswer);
            holder.correctAnswerBadge.setTextColor(Color.parseColor("#4CAF50"));
            holder.correctAnswerBadge.setVisibility(View.VISIBLE);
        }

        holder.header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                if (expandedPositions.contains(pos)) {
                    expandedPositions.remove(pos);
                    holder.body.animate().alpha(0f).setDuration(200)
                            .setInterpolator(new DecelerateInterpolator())
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    holder.body.setVisibility(View.GONE);
                                    holder.body.setAlpha(1f);
                                }
                            }).start();
                    holder.expandArrow.animate().rotation(0f).setDuration(200).start();
                } else {
                    expandedPositions.add(pos);
                    holder.body.setVisibility(View.VISIBLE);
                    holder.body.setAlpha(0f);
                    holder.body.animate().alpha(1f).setDuration(250)
                            .setInterpolator(new DecelerateInterpolator()).start();
                    holder.expandArrow.animate().rotation(180f).setDuration(200).start();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        LinearLayout header;
        LinearLayout body;
        ImageView expandArrow;
        TextView topicBadge;
        TextView questionText;
        TextView optionA, optionB, optionC, optionD;
        TextView userAnswerBadge;
        TextView correctAnswerBadge;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.historyHeader);
            body = itemView.findViewById(R.id.historyBody);
            expandArrow = itemView.findViewById(R.id.historyExpandArrow);
            topicBadge = itemView.findViewById(R.id.topicBadge);
            questionText = itemView.findViewById(R.id.historyQuestionText);
            optionA = itemView.findViewById(R.id.optionA);
            optionB = itemView.findViewById(R.id.optionB);
            optionC = itemView.findViewById(R.id.optionC);
            optionD = itemView.findViewById(R.id.optionD);
            userAnswerBadge = itemView.findViewById(R.id.userAnswerBadge);
            correctAnswerBadge = itemView.findViewById(R.id.correctAnswerBadge);
        }
    }
}