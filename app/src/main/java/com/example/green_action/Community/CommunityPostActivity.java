package com.example.green_action.Community;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.green_action.DataBaseHandler;
import com.example.green_action.LoginActivity;
import com.example.green_action.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommunityPostActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_EDIT_POST = 1;

    private DataBaseHandler db_handler;
    private List<CommunityPostItem> postItemList;
    private RecyclerView recyclerView;
    private CommunityPostAdapter adapter;
    private String loggedInUserId;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_post);

        ImageButton buttonback = findViewById(R.id.backButton);
        buttonback.setOnClickListener(v -> finish());

        db_handler = new DataBaseHandler(this);
        firebaseAuth = FirebaseAuth.getInstance(); // FirebaseAuth 인스턴스 초기화
        postItemList = new ArrayList<>();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            loggedInUserId = currentUser.getUid(); // Firebase로부터 현재 로그인된 사용자 ID를 가져옴
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            loggedInUserId = sharedPreferences.getString("loggedInUserId", ""); // SharedPreferences에서 사용자 ID를 불러옴
        }

        recyclerView = findViewById(R.id.community_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommunityPostAdapter(postItemList, this, loggedInUserId);
        recyclerView.setAdapter(adapter);

        Button writePostButton = findViewById(R.id.write_post_button);
        writePostButton.setOnClickListener(v -> {
            if (isLoggedIn()) {
                Intent intent = new Intent(CommunityPostActivity.this, WritePostActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(CommunityPostActivity.this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(CommunityPostActivity.this, "로그인 후 글을 작성할 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        Button loginLogoutButton = findViewById(R.id.login_logout_button);
        if (isLoggedIn()) {
            loginLogoutButton.setText("로그아웃");
            loginLogoutButton.setOnClickListener(v -> {
                SharedPreferences.Editor editor = getSharedPreferences("LoginPrefs", MODE_PRIVATE).edit();
                editor.remove("loggedInUserId");  // 로그인 ID 제거
                editor.apply();
                firebaseAuth.signOut();  // Firebase 인증 로그아웃
                Toast.makeText(CommunityPostActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                recreate(); // 액티비티 재시작
            });
        } else {
            loginLogoutButton.setText("로그인");
            loginLogoutButton.setOnClickListener(v -> {
                Intent intent = new Intent(CommunityPostActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        }

        // 초기화 시 모든 게시물 로드
        db_handler.getAllPosts(posts -> {
            postItemList.clear();
            postItemList.addAll(posts);
            Collections.reverse(postItemList); // 최신 게시물이 맨 위로 오도록 역순 정렬
            adapter.notifyDataSetChanged();
        });
    }

    private boolean isLoggedIn() {
        // 로그인된 사용자 ID가 있는지 또는 Firebase 인증에 사용자가 있는지 확인
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        Log.d("CommunityPostActivity", "CurrentUser: " + currentUser);
        return currentUser != null || !loggedInUserId.isEmpty();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 새로고침 시 게시물 다시 로드
        db_handler.getAllPosts(posts -> {
            postItemList.clear();
            postItemList.addAll(posts);
            Collections.reverse(postItemList); // 최신 게시물이 맨 위로 오도록 역순 정렬
            adapter.notifyDataSetChanged();
        });

        // onResume 시 로그인 상태를 다시 확인하여 버튼 텍스트 갱신
        Button loginLogoutButton = findViewById(R.id.login_logout_button);
        if (isLoggedIn()) {
            loginLogoutButton.setText("로그아웃");
        } else {
            loginLogoutButton.setText("로그인");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT_POST && resultCode == RESULT_OK) {
            if (data != null) {
                String postId = data.getStringExtra("postId");
                String updatedTitle = data.getStringExtra("title");
                String updatedContent = data.getStringExtra("content");
                long updatedTimestamp = data.getLongExtra("timestamp", -1);

                // 수정된 게시물을 리스트에서 찾아 업데이트
                for (CommunityPostItem post : postItemList) {
                    if (post.getPostId().equals(postId)) {
                        post.setTitle(updatedTitle);
                        post.setContent(updatedContent);
                        post.setTimestamp(String.valueOf(updatedTimestamp));  // long을 String으로 변환하여 설정
                        break;
                    }
                }

                // 리스트 갱신
                adapter.notifyDataSetChanged();
            }
        }
    }
}
