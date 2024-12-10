package com.example.animal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class DashboardWriteFragment extends Fragment {
    private Button button_submit, button_cancel;
    private EditText editText_title, editText_content;
    private FirebaseFirestore db;
    private Spinner spinnerCategory;
    private ImageButton image_dashboard;
    private Uri selectedImageUri;  // 선택한 이미지의 URI

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_dashboard_write, container, false);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // 뷰 초기화
        button_submit = rootView.findViewById(R.id.button_submit);
        editText_title = rootView.findViewById(R.id.edit_title);
        editText_content = rootView.findViewById(R.id.edit_content);
        spinnerCategory = rootView.findViewById(R.id.spinner_category);
        button_cancel = rootView.findViewById(R.id.button_cancel);
        image_dashboard = rootView.findViewById(R.id.image_dashboard);

        // 카테고리 설정
        String[] categories = {"community", "question", "find"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // 이미지 버튼 클릭 리스너
        image_dashboard.setOnClickListener(v -> {
            // 이미지 선택을 위한 인텐트 호출
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 100); // 100은 요청 코드
        });

        // 취소 버튼 클릭 시 이전 화면으로 돌아가기
        button_cancel.setOnClickListener(v -> getActivity().onBackPressed());

        // 업로드 버튼 클릭 시 Firestore에 데이터 추가
        button_submit.setOnClickListener(v -> {
            String title = editText_title.getText().toString().trim();
            String content = editText_content.getText().toString().trim();
            String selectedCategory = spinnerCategory.getSelectedItem().toString();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(getContext(), "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 게시글을 Firestore에 추가
            addPostToFirestore(title, content, selectedCategory, selectedImageUri);
        });

        return rootView;
    }

    // 게시글을 Firestore에 추가하는 메소드
    private void addPostToFirestore(String title, String content, String category, Uri selectedImageUri) {
        // Firestore 경로 설정
        CollectionReference itemsRef = db.collection("dashboard")
                .document(category)
                .collection("items");

        // Firestore에서 마지막 게시글 번호를 가져와서 증가시키는 방법
        db.collection("dashboard")
                .document(category)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    long postNumber = 1; // 기본값: 첫 번째 게시글 번호

                    // 기존에 게시글 번호가 있다면 그 번호를 가져와 증가시킴
                    if (documentSnapshot.exists() && documentSnapshot.contains("post_number")) {
                        postNumber = documentSnapshot.getLong("post_number") + 1;
                    }

                    // 게시글 번호를 업데이트
                    documentSnapshot.getReference().update("post_number", postNumber);

                    // Firestore 문서 데이터 구성
                    Map<String, Object> newPost = new HashMap<>();
                    newPost.put("title", title);
                    newPost.put("content", content);
                    newPost.put("author", "User123"); // 예: 사용자 ID
                    newPost.put("timestamp", com.google.firebase.Timestamp.now());
                    newPost.put("category", category);
                    newPost.put("post_number", postNumber);  // 글 번호 추가

                    // 사진이 선택되었으면 Base64로 변환하여 저장
                    if (selectedImageUri != null) {
                        try {
                            // 이미지 비트맵으로 변환
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            byte[] byteArray = byteArrayOutputStream.toByteArray();

                            // Base64로 인코딩
                            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                            // 이미지 데이터 Firestore에 추가
                            newPost.put("image", encodedImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "이미지 처리 오류", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // 이미지가 첨부되지 않았으면 null로 저장
                        newPost.put("image", null);
                    }


                    itemsRef.add(newPost)
                            .addOnSuccessListener(documentReference -> {
                                String generatedId = documentReference.getId();
                                Log.d("Firestore", "문서 추가 성공: " + generatedId);
                                Toast.makeText(getContext(), "게시글이 성공적으로 작성되었습니다.", Toast.LENGTH_SHORT).show();

                                // 작성 완료 후 입력 필드 초기화
                                editText_title.setText("");
                                editText_content.setText("");
                                spinnerCategory.setSelection(0);

                                // 작성 후 DashboardFragment로 이동
                                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, new DashboardFragment()).commitAllowingStateLoss();
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Firestore", "문서 추가 실패", e);
                                Toast.makeText(getContext(), "게시글 작성에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            });
                });
    }

//
//        public static Bitmap decodeBase64 (String encodedImage){
//            byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
//            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
//        }

        // 이미지 선택 후 처리하는 메소드
        @Override
        public void onActivityResult ( int requestCode, int resultCode, @NonNull Intent data){
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 100 && resultCode == getActivity().RESULT_OK) {
                selectedImageUri = data.getData();
            }
        }
    }
