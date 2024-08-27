package com.example.green_action.remote;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.green_action.DataBaseHandler;
import com.example.green_action.User;
import com.example.green_action.Ranking;
import com.example.green_action.DailyQuiz;
import com.example.green_action.QuizDetail;
import com.example.green_action.Post;
import com.example.green_action.Comment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Transaction;

public class FirebaseClient {

    private final DatabaseReference dbRef;
    private final DatabaseReference usersRef;
    private final DatabaseReference postsRef;
    private final DatabaseReference dailyQuizRef;

    private static final String TAG = "FirebaseClient";

    public FirebaseClient() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        usersRef = dbRef.child("users");
        postsRef = dbRef.child("posts");
        dailyQuizRef = dbRef.child("daily_quiz");
    }

    // 사용자 데이터를 Firebase에 저장하는 메서드
    public void saveUserData(String userId, User user) {
        if (userId != null && user != null) {
            usersRef.child(userId).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User data saved successfully.");
                } else {
                    Log.e(TAG, "Failed to save user data", task.getException());
                }
            });
        }
    }

    // 사용자 데이터를 불러오는 메서드
    public void loadUserData(String userId, ValueEventListener listener) {
        if (userId != null && !userId.isEmpty()) {
            Log.d(TAG, "Loading user data for userId: " + userId);
            DatabaseReference userRef = usersRef.child(userId);
            userRef.addListenerForSingleValueEvent(listener);
        } else {
            Log.e(TAG, "User ID is null or empty");
            listener.onDataChange(null);
        }
    }

    // 사용자 데이터 참조 반환 메서드
    public DatabaseReference getUsersRef() {
        return usersRef;
    }

    // 사용자별 퀴즈 진행 상태를 저장하는 메소드 (오염 유형별)
    public void saveQuizProgress(String userId, String pollutionType, int quizId) {
        if (userId != null && pollutionType != null) {
            DatabaseReference userQuizProgressRef = usersRef.child(userId).child("quiz_progress").child(pollutionType);

            userQuizProgressRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Integer lastSolvedQuiz = currentData.getValue(Integer.class);
                    if (lastSolvedQuiz == null || quizId > lastSolvedQuiz) {
                        currentData.setValue(quizId);
                    }
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot currentData) {
                    if (databaseError != null) {
                        Log.e(TAG, "Failed to update quiz progress", databaseError.toException());
                    } else {
                        Log.d(TAG, "Quiz progress successfully updated for " + pollutionType + " with quizId " + quizId);
                    }
                }
            });
        } else {
            Log.e(TAG, "User ID or Pollution Type is null or empty");
        }
    }

    // 사용자별 퀴즈 진행 상태를 불러오는 메소드 (오염 유형별)
    public void loadQuizProgress(String userId, String pollutionType, ValueEventListener listener) {
        if (userId != null && pollutionType != null && !userId.isEmpty()) {
            DatabaseReference userQuizProgressRef = usersRef.child(userId).child("quiz_progress").child(pollutionType);
            userQuizProgressRef.addListenerForSingleValueEvent(listener);
        } else {
            Log.e(TAG, "User ID or Pollution Type is null or empty");
            listener.onDataChange(null);
        }
    }

    // 퀴즈 디테일을 Firebase에서 불러오는 메서드 (오염 유형별)
    public void loadQuizDetail(String pollutionType, String quizId, ValueEventListener listener) {
        DatabaseReference quizDetailRef = dbRef.child("quiz_details").child(pollutionType).child(quizId);
        quizDetailRef.addListenerForSingleValueEvent(listener);
    }

    // 퀴즈 디테일을 Firebase에 저장하는 메서드
    public void saveQuizDetail(String pollutionType, QuizDetail quizDetail) {
        if (pollutionType != null && quizDetail != null) {
            String quizId = quizDetail.getQuizId();
            DatabaseReference quizDetailRef = dbRef.child("quiz_details").child(pollutionType).child(quizId);
            quizDetailRef.setValue(quizDetail).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Quiz detail data saved successfully for " + pollutionType);
                } else {
                    Log.e(TAG, "Failed to save quiz detail data for " + pollutionType, task.getException());
                }
            });
        } else {
            Log.e(TAG, "Pollution Type or Quiz Detail is null");
        }
    }

    // 사용자 점수를 업데이트하는 메서드 추가
    public void updateUserScore(String userId, int score) {
        usersRef.child(userId).child("score").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer currentScore = dataSnapshot.getValue(Integer.class);
                if (currentScore == null) {
                    currentScore = 0;
                }
                usersRef.child(userId).child("score").setValue(currentScore + score);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to update user score", databaseError.toException());
            }
        });
    }

    // 사용자 퀴즈 상태를 Firebase에 저장하는 메서드
    public void saveQuizState(String userId, int quizId, int maxScore, int attemptsLeft) {
        if (userId != null) {
            DatabaseReference userQuizRef = usersRef.child(userId).child("quizzes").child(String.valueOf(quizId));
            userQuizRef.child("maxScore").setValue(maxScore);
            userQuizRef.child("attemptsLeft").setValue(attemptsLeft);
        }
    }

    // 사용자 퀴즈 상태를 Firebase에서 불러오는 메서드
    public void loadQuizState(String userId, int quizId, ValueEventListener listener) {
        if (userId != null) {
            DatabaseReference userQuizRef = usersRef.child(userId).child("quizzes").child(String.valueOf(quizId));
            userQuizRef.addListenerForSingleValueEvent(listener);
        }
    }

    // 기타 메서드들 (퀴즈와 관련 없는 부분은 유지)
    // 아이디 중복 확인 함수 (Firebase에서 체크)
    public void isIDExists(String id, final DataBaseHandler.OnCheckUserExistsListener listener) {
        usersRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listener.onCheck(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onCheck(false);
            }
        });
    }

    // 게시글 참조 가져오기
    public DatabaseReference getPostsRef() {
        return postsRef;
    }

    // 게시글을 저장하는 메서드
    public void savePostData(String postId, Post post) {
        postsRef.child(postId).setValue(post).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Post data saved successfully.");
            } else {
                Log.e(TAG, "Failed to save post data", task.getException());
            }
        });
    }

    // 게시글 데이터를 Firebase에서 불러오는 메서드
    public void loadPostData(String postId, ValueEventListener listener) {
        postsRef.child(postId).addListenerForSingleValueEvent(listener);
    }

    // 댓글 참조 가져오기
    public DatabaseReference getCommentsRef(String postId) {
        return postsRef.child(postId).child("comments");
    }

    // 댓글을 저장하는 메서드
    public void saveCommentData(String commentId, Comment comment) {
        dbRef.child("comments").child(commentId).setValue(comment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Comment data saved successfully.");
            } else {
                Log.e(TAG, "Failed to save comment data", task.getException());
            }
        });
    }

    // 댓글 데이터를 Firebase에서 불러오는 메서드
    public void loadCommentData(String commentId, ValueEventListener listener) {
        dbRef.child("comments").child(commentId).addListenerForSingleValueEvent(listener);
    }

    // 랭킹 데이터를 저장하는 메서드
    public void saveRankingData(String rankingId, Ranking ranking) {
        dbRef.child("ranking").child(rankingId).setValue(ranking).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Ranking data saved successfully.");
            } else {
                Log.e(TAG, "Failed to save ranking data", task.getException());
            }
        });
    }

    // 일일 퀴즈 데이터를 Firebase에 저장하는 메서드
    public void saveDailyQuizData(String dailyQuizId, DailyQuiz dailyQuiz) {
        dailyQuizRef.child(dailyQuizId).setValue(dailyQuiz).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Daily quiz data saved successfully.");
            } else {
                Log.e(TAG, "Failed to save daily quiz data", task.getException());
            }
        });
    }

    // 일일 퀴즈 데이터를 Firebase에서 불러오는 메서드
    public void loadDailyQuizData(String dailyQuizId, ValueEventListener listener) {
        dailyQuizRef.child(dailyQuizId).addListenerForSingleValueEvent(listener);
    }
}