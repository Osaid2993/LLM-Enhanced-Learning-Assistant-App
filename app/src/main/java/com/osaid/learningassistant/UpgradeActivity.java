package com.osaid.learningassistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.osaid.learningassistant.databinding.ActivityUpgradeBinding;

public class UpgradeActivity extends AppCompatActivity {

    private ActivityUpgradeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpgradeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.getRoot().startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.starterPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPaymentDialog("Starter Plan", "$2.99");
            }
        });

        binding.intermediatePurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPaymentDialog("Intermediate Plan", "$5.99");
            }
        });

        binding.advancedPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPaymentDialog("Advanced Plan", "$9.99");
            }
        });
    }

    private void showPaymentDialog(String tierName, String amount) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_google_pay, null);

        TextView tierText = dialogView.findViewById(R.id.payTierName);
        TextView amountText = dialogView.findViewById(R.id.payAmount);
        Button confirmButton = dialogView.findViewById(R.id.confirmPayButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelPayButton);

        tierText.setText(tierName);
        amountText.setText(amount);
        confirmButton.setText("Pay " + amount);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(UpgradeActivity.this, "Payment successful! " + tierName + " activated.", Toast.LENGTH_LONG).show();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}