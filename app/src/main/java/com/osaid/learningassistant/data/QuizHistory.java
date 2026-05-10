package com.osaid.learningassistant.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_history")
public class QuizHistory {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String topic;
    public String question;
    public String optionsJson;
    public String userAnswer;
    public String correctAnswer;
    public boolean isCorrect;
    public long timestamp;

    public QuizHistory(int userId, String topic, String question, String optionsJson,
                       String userAnswer, String correctAnswer, boolean isCorrect, long timestamp) {
        this.userId = userId;
        this.topic = topic;
        this.question = question;
        this.optionsJson = optionsJson;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.isCorrect = isCorrect;
        this.timestamp = timestamp;
    }
}