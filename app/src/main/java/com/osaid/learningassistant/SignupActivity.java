package com.osaid.learningassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.osaid.learningassistant.data.AppDatabase;
import com.osaid.learningassistant.data.User;
import com.osaid.learningassistant.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.getRoot().startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));

        db = AppDatabase.getInstance(this);

        binding.createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignup();
            }
        });
    }

    private void attemptSignup() {
        String username = binding.usernameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String confirmEmail = binding.confirmEmailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordInput.getText().toString().trim();
        String phone = binding.phoneInput.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(confirmEmail)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.equals(confirmEmail)) {
            Toast.makeText(this, "Emails do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 4) {
            Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        User existing = db.userDao().findByUsername(username);
        if (existing != null) {
            Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(username, email, password, phone, "");
        long newId = db.userDao().insert(newUser);

        Intent intent = new Intent(SignupActivity.this, InterestsActivity.class);
        intent.putExtra("user_id", (int) newId);
        startActivity(intent);
        finish();
    }
}