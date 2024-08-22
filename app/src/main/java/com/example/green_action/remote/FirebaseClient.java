package com.example.green_action.remote;

import android.util.Log;

import com.example.green_action.Ranking;
import com.example.green_action.DailyQuiz;
import com.example.green_action.Post;
import com.example.green_action.Comment;
import com.example.green_action.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseClient {

    private final FirebaseDatabase database;
    private final DatabaseReference dbRef;
    private final DatabaseReference postsRef;
    private final DatabaseReference usersRef;
    private final DatabaseReference dailyQuizRef;  // 추가된 부분

    private static final String TAG = "FirebaseClient";

    public FirebaseClient() {
        database = FirebaseDatabase.getInstance(); // Firebase Database 인스턴스 생성
        dbRef = database.getReference(); // 기본 경로 설정
        usersRef = database.getReference("users");
        postsRef = dbRef.child("posts"); // "posts" 경로 설정
        dailyQuizRef = database.getReference("daily_quiz");  // 추가된 부분

    }
    public void saveUserData(String userId, User user) {
        if (userId != null && user != null) {
            usersRef.child(userId).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    System.out.println("User data saved successfully.");
                } else {
                    System.out.println("Failed to save user data.");
                }
            });
        }
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
    public DatabaseReference getUsersRef() {
        return usersRef;
    }

    // 게시글 데이터를 Firebase에서 불러오는 메서드
    public void loadPostData(String postId, ValueEventListener listener) {
        DatabaseReference postRef = postsRef.child(postId);
        postRef.addListenerForSingleValueEvent(listener);
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
        DatabaseReference commentRef = dbRef.child("comments").child(commentId);
        commentRef.addListenerForSingleValueEvent(listener);
    }

    // 랭킹 데이터를 저장하는 메서드
    public void saveRankingData(String userId, Ranking rank) {
        dbRef.child("ranking").child(userId).setValue(rank).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Ranking data saved successfully.");
            } else {
                Log.e(TAG, "Failed to save ranking data", task.getException());
            }
        });
    }

    // 랭킹 데이터를 Firebase에서 불러오는 메서드
    public void loadRankingData(String userId, ValueEventListener listener) {
        DatabaseReference rankingRef = dbRef.child("ranking").child(userId);
        rankingRef.addListenerForSingleValueEvent(listener);
    }

    // 일일 퀴즈를 저장하는 메서드
    public void saveDailyQuizData(String quizId, DailyQuiz dailyQuiz) {
        dbRef.child("daily_quiz").child(quizId).setValue(dailyQuiz).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Daily quiz data saved successfully.");
            } else {
                Log.e(TAG, "Failed to save daily quiz data", task.getException());
            }
        });
    }
    // 사용자 데이터를 Firebase에서 로드하는 메서드
    public void loadUserData(String userId, ValueEventListener listener) {
        if (userId != null && !userId.isEmpty()) {
            DatabaseReference userRef = usersRef.child(userId);
            userRef.addListenerForSingleValueEvent(listener);
        }
    }
    // 일일 퀴즈 데이터를 Firebase에서 불러오는 메서드
    public void loadDailyQuizData(String quizId, ValueEventListener listener) {
        DatabaseReference dailyQuizRef = dbRef.child("daily_quiz").child(quizId);
        dailyQuizRef.addListenerForSingleValueEvent(listener);
    }

    // 퀴즈 진행 상태 저장 메서드
    public void saveQuizProgress(String userId, int quizId, boolean isSolved) {
        DatabaseReference userQuizRef = dbRef.child("users").child(userId).child("quiz_progress").child(String.valueOf(quizId));
        userQuizRef.setValue(isSolved ? 1 : 0).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Quiz progress saved successfully.");
            } else {
                Log.e(TAG, "Failed to save quiz progress", task.getException());
            }
        });
    }

    // 퀴즈 진행 상태 전체를 Firebase에서 불러오는 메서드
    public void loadAllQuizProgress(String userId, ValueEventListener listener) {
        DatabaseReference quizProgressRef = dbRef.child("users").child(userId).child("quiz_progress");
        quizProgressRef.addListenerForSingleValueEvent(listener);
    }

}
