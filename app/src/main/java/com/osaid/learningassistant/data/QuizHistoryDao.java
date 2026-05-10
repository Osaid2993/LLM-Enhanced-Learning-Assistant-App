package com.osaid.learningassistant.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface QuizHistoryDao {

    @Query("SELECT * FROM quiz_history WHERE userId = :userId AND topic = :topic ORDER BY timestamp DESC")
    List<QuizHistory> getHistoryByTopic(int userId, String topic);

    @Insert
    void insert(QuizHistory entry);

    @Query("SELECT * FROM quiz_history WHERE userId = :userId ORDER BY timestamp DESC")
    List<QuizHistory> getHistoryForUser(int userId);

    @Query("SELECT COUNT(*) FROM quiz_history WHERE userId = :userId")
    int getTotalQuestions(int userId);

    @Query("SELECT COUNT(*) FROM quiz_history WHERE userId = :userId AND isCorrect = 1")
    int getCorrectCount(int userId);

    @Query("SELECT COUNT(*) FROM quiz_history WHERE userId = :userId AND isCorrect = 0")
    int getIncorrectCount(int userId);

    @Query("DELETE FROM quiz_history WHERE userId = :userId")
    void clearHistory(int userId);
}