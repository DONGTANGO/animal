package com.example.animal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class SettingFragment extends Fragment {

    private Button logout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_setting, container, false);


        logout = rootView.findViewById(R.id.logout); // 로그아웃 버튼 참조


        logout.setOnClickListener(v -> logoutUser());

        return rootView;
    }


    // Firebase 로그아웃 처리 메소드
    private void logoutUser() {
        // Firebase에서 로그아웃
        FirebaseAuth.getInstance().signOut();

        // 로그아웃 후 로그인 화면으로 이동
        Intent intent = new Intent(getActivity(), loginActivity.class);
        startActivity(intent);
        getActivity().finish(); // 현재 액티비티 종료
    }

}