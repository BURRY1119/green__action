package com.example.green_action.Community;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.green_action.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.ViewHolder> {

    private final List<CommunityPostItem> postItemList;
    private final Context context;
    private final String loggedInUserId;

    public CommunityPostAdapter(List<CommunityPostItem> postItemList, Context context, String loggedInUserId) {
        this.postItemList = postItemList;
        this.context = context;
        this.loggedInUserId = loggedInUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommunityPostItem postItem = postItemList.get(position);

        holder.title.setText(postItem.getTitle());
        holder.content.setText(postItem.getContent());
        holder.timestamp.setText(convertTimestampToDate(postItem.getTimestamp()));

        String userId = postItem.getUserId();
        if (userId != null && userId.length() >= 7) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userName = snapshot.child("username").getValue(String.class);
                    if (userName == null) {
                        userName = "Unknown User";
                    }
                    holder.userInfo.setText(userName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.userInfo.setText("Unknown User (" + userId.substring(0, 7) + ")");
                }
            });
        } else {
            holder.userInfo.setText("Unknown User");
        }

        // 좋아요 수를 Firebase에서 가져와 설정
        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("posts").child(postItem.getPostId()).child("likes");
        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int likeCount = 0;
                if (snapshot.exists()) {
                    likeCount = snapshot.getValue(Integer.class);
                }
                holder.likeCount.setText(String.valueOf(likeCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.likeCount.setText("0"); // 오류 발생 시 기본값 설정
            }
        });

        // 게시물 클릭 시 PostDetailActivity로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postId", postItem.getPostId());
            intent.putExtra("userId", postItem.getUserId());
            intent.putExtra("title", postItem.getTitle());
            intent.putExtra("content", postItem.getContent());
            intent.putExtra("timestamp", postItem.getTimestamp());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postItemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, timestamp, userInfo, likeCount;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.post_title);
            content = itemView.findViewById(R.id.post_content);
            timestamp = itemView.findViewById(R.id.post_timestamp);
            userInfo = itemView.findViewById(R.id.post_user_info);
            likeCount = itemView.findViewById(R.id.like_count_view); // Like Count View 추가
        }
    }

    private String convertTimestampToDate(String timestamp) {
        long time = Long.parseLong(timestamp);
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }
}
