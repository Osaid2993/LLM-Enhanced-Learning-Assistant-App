package com.osaid.learningassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.osaid.learningassistant.data.AppDatabase;
import com.osaid.learningassistant.data.SessionManager;
import com.osaid.learningassistant.data.User;
import com.osaid.learningassistant.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;
    private AppDatabase db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.getRoot().startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        if (session.isLoggedIn()) {
            goToHome();
            return;
        }

        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        binding.needAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, SignupActivity.class));
            }
        });
    }

    private void attemptLogin() {
        String username = binding.usernameInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = db.userDao().login(username, password);
        if (user == null) {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        session.saveSession(user.id, user.username);
        goToHome();
    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}