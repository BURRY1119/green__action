package com.example.green_action;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.green_action.QuizDetail;
import com.example.green_action.QuizViewModel;
import com.example.green_action.R;
import com.example.green_action.remote.FirebaseClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class DailyQuizFragment extends Fragment {

    private static final String TAG = "DailyQuizFragment";

    private TextView quizTextView;
    private TextView scoreTextView;
    private TextView explanationTextView;
    private EditText answerEditText;
    private Button actionButton;
    private FirebaseClient firebaseClient;
    private String userId;
    private int quizId;
    private QuizViewModel quizViewModel;

    private long lastQuizTime;
    private static final long QUIZ_INTERVAL = 24 * 60 * 60 * 1000; // 24시간

    public DailyQuizFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_daily_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        quizTextView = view.findViewById(R.id.quizTextView);
        scoreTextView = view.findViewById(R.id.scoreTextView);
        explanationTextView = view.findViewById(R.id.explanationTextView);
        answerEditText = view.findViewById(R.id.answerEditText);
        actionButton = view.findViewById(R.id.submitButton);

        firebaseClient = new FirebaseClient();
        quizViewModel = new ViewModelProvider(this).get(QuizViewModel.class);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        if (getArguments() != null) {
            quizId = getArguments().getInt("QUIZ_NUMBER", -1); // 기본값을 -1로 설정하여 문제를 감지
            Log.d(TAG, "Received quizId: " + quizId);
        } else {
            Log.e(TAG, "No arguments received, setting quizId to default (-1)");
            quizId = -1;
        }

        if (quizId == -1) {
            Log.e(TAG, "Invalid quizId, cannot load quiz data.");
            Toast.makeText(getContext(), "잘못된 퀴즈 ID입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        checkQuizAvailability();

        actionButton.setOnClickListener(v -> handleButtonClick());
    }

    private void checkQuizAvailability() {
        firebaseClient.loadLastQuizTime(userId, quizId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long lastTime = snapshot.getValue(Long.class);
                if (lastTime != null) {
                    lastQuizTime = lastTime;
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastQuizTime < QUIZ_INTERVAL) {
                        disableQuiz();
                    } else {
                        loadQuiz();
                    }
                } else {
                    loadQuiz();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load last quiz time", error.toException());
            }
        });
    }

    private void disableQuiz() {
        quizTextView.setText("해당 문제는 이미 풀었습니다. 24시간 후에 새로운 문제를 풀 수 있습니다.");
        actionButton.setEnabled(false);
        answerEditText.setEnabled(false);
    }

    private void loadQuiz() {
        firebaseClient.loadQuizDetail(quizId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.exists()) { // Null 체크 추가
                    QuizDetail quizDetail = dataSnapshot.getValue(QuizDetail.class);
                    if (quizDetail != null) {
                        quizTextView.setText(quizDetail.getQuestion());
                        quizViewModel.setCurrentMaxScore(quizDetail.getMaxScore());
                        scoreTextView.setText("(" + quizViewModel.getCurrentMaxScore() + "점)");
                    } else {
                        Log.e(TAG, "QuizDetail is null for quizId: " + quizId);
                        Toast.makeText(getContext(), "퀴즈 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "DataSnapshot is null or does not exist for quizId: " + quizId);
                    Toast.makeText(getContext(), "퀴즈 데이터가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "퀴즈 데이터를 불러오는 중 오류 발생", databaseError.toException());
                Toast.makeText(getContext(), "데이터 로드 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleButtonClick() {
        submitAnswer();
    }

    private void submitAnswer() {
        String userAnswer = answerEditText.getText().toString().trim();
        if (TextUtils.isEmpty(userAnswer)) {
            Toast.makeText(getContext(), "정답을 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseClient.loadQuizDetail(quizId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                QuizDetail quizDetail = dataSnapshot.getValue(QuizDetail.class);
                if (quizDetail != null) {
                    if (quizDetail.checkAnswer(userAnswer)) {
                        int scoreToAdd = quizViewModel.getCurrentMaxScore();
                        Toast.makeText(getContext(), "정답입니다! " + scoreToAdd + "점 획득", Toast.LENGTH_SHORT).show();
                        displayExplanation(quizDetail);
                        saveUserScore(scoreToAdd);
                    } else {
                        displayExplanation(quizDetail);
                    }
                    saveQuizProgress();
                } else {
                    Log.e(TAG, "QuizDetail is null for quizId: " + quizId);
                    Toast.makeText(getContext(), "퀴즈 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load quiz data", databaseError.toException());
                Toast.makeText(getContext(), "데이터 로드 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayExplanation(QuizDetail quizDetail) {
        explanationTextView.setText("정답: " + quizDetail.getAnswer() + "\n해설: " + quizDetail.getExplanation());
        explanationTextView.setVisibility(View.VISIBLE);
        actionButton.setEnabled(false);
        answerEditText.setEnabled(false);
    }

    private void saveUserScore(int scoreToAdd) {
        if (userId != null) {
            firebaseClient.updateUserScore(userId, scoreToAdd);
        }
    }

    private void saveQuizProgress() {
        if (userId != null) {
            long currentTime = System.currentTimeMillis();
            firebaseClient.saveQuizProgress(userId, quizId, currentTime);
        }
    }
}