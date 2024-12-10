package com.example.animal;


import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiaryFragment extends Fragment {

    private ImageButton shorts_upload;
    private ViewPager2 viewPager2;
    private Uri selectedImageUri;  // 선택한 이미지의 URI
private ShortsAdapter shortsAdapter;


    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_diary, container, false);

        db = FirebaseFirestore.getInstance();
        shorts_upload = rootView.findViewById(R.id.shorts_upload);
        viewPager2 = rootView.findViewById(R.id.imageViewPager);

        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        // Firestore에서 이미지를 직접 가져오는 ShortsAdapter 설정
        ShortsAdapter shortsAdapter = new ShortsAdapter(getContext()); // Firestore에서 데이터 자동 로딩
        viewPager2.setAdapter(shortsAdapter);

        shorts_upload.setOnClickListener(v -> {
            // 이미지 선택을 위한 인텐트 호출
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 100); // 100은 요청 코드
        });

        return rootView;
    }

    // 게시글을 Firestore에 추가하는 메소드
    private void addPostToFirestore(Uri selectedImageUri) {
        if (selectedImageUri != null) {
            CollectionReference shortsCollection = db.collection("shorts"); // Firestore 경로
            DocumentReference docRef = shortsCollection.document();  // 문서 ID 자동 생성

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 800, 600, true);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream); // 품질을 낮추어 압축
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                // Base64로 인코딩
                String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                Map<String, Object> newPost = new HashMap<>();
                newPost.put("author", "User123"); // 예: 사용자 ID
                newPost.put("timestamp", com.google.firebase.Timestamp.now());
                newPost.put("image", encodedImage);  // Base64로 인코딩된 이미지 데이터

                // Firestore에 데이터 저장
                docRef.set(newPost, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firestore", "데이터 추가 성공");
                            Toast.makeText(getContext(), "게시글이 성공적으로 작성되었습니다.", Toast.LENGTH_SHORT).show();


                            // 이미지 업로드 후 RecyclerView를 새로 고침
                            if (shortsAdapter != null) {
                                shortsAdapter.notifyDataSetChanged();  // RecyclerView 업데이트
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.w("Firestore", "데이터 추가 실패", e);
                            Toast.makeText(getContext(), "게시글 작성에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        });
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "이미지 처리 오류", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == getActivity().RESULT_OK) {
            selectedImageUri = data.getData();
            addPostToFirestore(selectedImageUri);  // 이미지 선택 후 Firestore에 추가
        }
    }
}

