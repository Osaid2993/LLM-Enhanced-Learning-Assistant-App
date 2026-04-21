package com.osaid.learningassistant.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @GET("getQuiz")
    Call<QuizResponse> getQuiz(@Query("topic") String topic);

    @POST("getHint")
    Call<HintResponse> getHint(@Body HintRequest request);

    @POST("explainAnswer")
    Call<ExplanationResponse> explainAnswer(@Body ExplanationRequest request);

    class HintRequest {
        public String question;
        public List<String> options;

        public HintRequest(String question, List<String> options) {
            this.question = question;
            this.options = options;
        }
    }

    class ExplanationRequest {
        public String question;
        public List<String> options;
        public String correct_answer;
        public String user_answer;

        public ExplanationRequest(String question, List<String> options, String correct_answer, String user_answer) {
            this.question = question;
            this.options = options;
            this.correct_answer = correct_answer;
            this.user_answer = user_answer;
        }
    }
}