package com.example.animal;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class DashboardDetail extends Fragment {

    private TextView postTitle, postContent;
    private ImageView imageView;
    private Button backback;
    private FirebaseFirestore db;

    public static DashboardDetail newInstance(String category, String title) {
        DashboardDetail fragment = new DashboardDetail();
        Bundle args = new Bundle();
        args.putString("category", category);
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_detail, container, false);

        db = FirebaseFirestore.getInstance();
        postTitle = view.findViewById(R.id.detail_title);
        postContent = view.findViewById(R.id.detail_content);
        imageView = view.findViewById(R.id.image_view);
        backback = view.findViewById(R.id.backback);


        String title = getArguments() != null ? getArguments().getString("title") : null;
        String category = getArguments().getString("category");

        backback.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack(); // 이전 Fragment로 돌아가기
            } else {
                if (getActivity() != null) {
                    getActivity().onBackPressed(); // 기본 뒤로가기 동작
                }
            }
        });
        if (title != null) {
            loadPostDetails(title,category);
        }

        return view;
    }

    private void loadPostDetails(String title, String category) {
        db.collection("dashboard")
                .document(category)  // 카테고리 이름 (예시: community)
                .collection("items")
                .whereEqualTo("title", title)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                            postTitle.setText(document.getString("title"));
                            postContent.setText(document.getString("content"));


                            String encodedImage = document.getString("image");
                            if (encodedImage != null && !encodedImage.isEmpty()) {
                                // 이미지가 있을 경우 ImageView에 표시
                                Bitmap decodedImage = decodeBase64(encodedImage);
                                imageView.setImageBitmap(decodedImage);

                                // ImageView 보이기
                                imageView.setVisibility(View.VISIBLE);
                            } else {
                                // 이미지가 없으면 ImageView 숨기기
                                imageView.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "게시물을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public static Bitmap decodeBase64(String encodedImage) {
        byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}

