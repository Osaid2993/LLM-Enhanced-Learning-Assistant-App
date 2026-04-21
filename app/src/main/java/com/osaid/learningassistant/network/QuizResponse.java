package com.osaid.learningassistant.network;

import java.util.List;

public class QuizResponse {

    public List<Question> quiz;

    public static class Question {
        public String question;
        public List<String> options;
        public String correct_answer;
    }
}