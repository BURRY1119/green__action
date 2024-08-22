package com.example.green_action;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.green_action.Community.CommunityFragment;
import com.example.green_action.air_pollution.AirPollutionFragment;
import com.example.green_action.plastic_pollution.PlasticPollutionFragment;
import com.example.green_action.soil_pollution.SoilPollutionFragment;
import com.example.green_action.water_pollution.WaterPollutionFragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 버튼 클릭 이벤트 설정
        setButtonTouchListener(view.findViewById(R.id.air_pollution));
        setButtonTouchListener(view.findViewById(R.id.water_pollution));
        setButtonTouchListener(view.findViewById(R.id.soil_pollution));
        setButtonTouchListener(view.findViewById(R.id.plastic_pollution));
        setButtonTouchListener(view.findViewById(R.id.more));

        // TextView 클릭 이벤트 설정
        setTextViewClickListener(view.findViewById(R.id.tv_issue_board), com.example.green_action.Community.IssueBoardActivity.class);
        setTextViewClickListener(view.findViewById(R.id.tv_free_board), com.example.green_action.Community.FreeBoardActivity.class);
        setTextViewClickListener(view.findViewById(R.id.tv_notice_board), com.example.green_action.Community.NoticeBoardActivity.class);
        setTextViewClickListener(view.findViewById(R.id.tv_qna_board), com.example.green_action.Community.QnaBoardActivity.class);


        return view;
    }

    // 터치 이벤트 설정
    private void setButtonTouchListener(View button) {
        button.setOnTouchListener(new OnTouchListener() {
            private float startX;
            private float startY;
            private boolean isClick = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        isClick = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getX() - startX) > 10 || Math.abs(event.getY() - startY) > 10) {
                            isClick = false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isClick) {
                            onButtonClicked(v);
                        }
                        break;
                }
                return true;
            }
        });
    }

    // 버튼 클릭 이벤트 처리
    private void onButtonClicked(View view) {
        Fragment fragment = null;
        if (view.getId() == R.id.air_pollution) {
            fragment = new AirPollutionFragment();
        } else if (view.getId() == R.id.water_pollution) {
            fragment = new WaterPollutionFragment();
        } else if (view.getId() == R.id.soil_pollution) {
            fragment = new SoilPollutionFragment();
        } else if (view.getId() == R.id.plastic_pollution) {
            fragment = new PlasticPollutionFragment();
        } else if (view.getId() == R.id.more) {
            fragment = new CommunityFragment();
        }

        if (fragment != null) {
            replaceFragment(fragment);
        }
    }

    // Fragment를 교체하는 메서드
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null); // Back stack에 추가
        fragmentTransaction.commit();
    }

    // TextView 클릭 이벤트 설정
    private void setTextViewClickListener(TextView textView, Class<?> activityClass) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), activityClass);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 앱 종료
                requireActivity().finish();
            }
        });
    }
}
