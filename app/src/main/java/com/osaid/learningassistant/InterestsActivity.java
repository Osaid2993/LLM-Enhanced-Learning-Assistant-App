package com.osaid.learningassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.osaid.learningassistant.data.AppDatabase;
import com.osaid.learningassistant.data.SessionManager;
import com.osaid.learningassistant.data.User;
import com.osaid.learningassistant.databinding.ActivityInterestsBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InterestsActivity extends AppCompatActivity {

    private ActivityInterestsBinding binding;
    private AppDatabase db;
    private SessionManager session;
    private int userId;

    private final List<String> topics = Arrays.asList(
            "Algorithms", "Data Structures",
            "Web Development", "Mobile Apps",
            "Machine Learning", "Databases",
            "Operating Systems", "Networking",
            "Cybersecurity", "Cloud Computing",
            "Software Testing", "UI/UX Design",
            "Game Development", "Computer Vision",
            "Data Science", "DevOps"
    );

    private final List<String> selected = new ArrayList<>();
    private static final int MAX_SELECTION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInterestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.getRoot().startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);
        userId = getIntent().getIntExtra("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        buildChips();

        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishSetup();
            }
        });
    }

    private void buildChips() {
        GridLayout grid = binding.interestsGrid;
        int screenPaddingDp = 28;
        int chipMarginDp = 6;

        for (int i = 0; i < topics.size(); i++) {
            final String topic = topics.get(i);
            final TextView chip = new TextView(this);

            chip.setText(topic);
            chip.setTextColor(getResources().getColor(R.color.text_primary));
            chip.setTextSize(14f);
            chip.setGravity(Gravity.CENTER);
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            chip.setPadding(dp(14), dp(14), dp(14), dp(14));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % 2, 1, 1f);
            params.rowSpec = GridLayout.spec(i / 2);
            params.setMargins(dp(chipMarginDp), dp(chipMarginDp), dp(chipMarginDp), dp(chipMarginDp));
            chip.setLayoutParams(params);

            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleChip(chip, topic);
                }
            });

            grid.addView(chip);
        }
    }

    private void toggleChip(TextView chip, String topic) {
        if (selected.contains(topic)) {
            selected.remove(topic);
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
        } else {
            if (selected.size() >= MAX_SELECTION) {
                Toast.makeText(this, "You can select up to " + MAX_SELECTION + " topics", Toast.LENGTH_SHORT).show();
                return;
            }
            selected.add(topic);
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
        }
    }

    private void finishSetup() {
        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select at least one topic", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = db.userDao().findById(userId);
        if (user == null) {
            Toast.makeText(this, "Account not found", Toast.LENGTH_SHORT).show();
            return;
        }

        user.interests = String.join(",", selected);
        db.userDao().update(user);

        session.saveSession(user.id, user.username);

        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }
}