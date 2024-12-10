package com.example.animal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

public class loginActivity extends AppCompatActivity {
    private Button login;
    private Button signup;
    private EditText login_id;
    private EditText login_pw;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;


    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        login_id = findViewById(R.id.login_id);
        login_pw = findViewById(R.id.login_pw);
        login = findViewById(R.id.login);
        signup = findViewById(R.id.signup);

        login.setOnClickListener(v -> loginEvent());
        signup.setOnClickListener(v -> {
            startActivity(new Intent(loginActivity.this, SignupActivity.class));
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    checkAndFetchData();
                    Intent intent = new Intent(loginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        };

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // SharedPreferences 초기화 (로컬 저장을 위해 사용)
        sharedPreferences = getSharedPreferences("local_data", Context.MODE_PRIVATE);


        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.clear();  // 모든 항목 삭제

    }

    void loginEvent() {
        firebaseAuth.signInWithEmailAndPassword(login_id.getText().toString(), login_pw.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(loginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkAndFetchData() {
        String cachedData = sharedPreferences.getString("hospital_data", null);

        if (cachedData == null || cachedData.isEmpty()) {
            // SharedPreferences에 데이터가 없는 경우 -> Firebase에서 가져오기
            Log.i("pass", "downloading map data");
            fetchDataFromFirestore();
        } else {
            Log.i("pass", "already downloaded");
        }
    }

    private void fetchDataFromFirestore() {
        db.collection("animal") // "locations"라는 컬렉션에서 데이터를 가져옵니다.
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            // 데이터를 로컬 SharedPreferences에 저장
                            saveDataLocally(querySnapshot);
                        }
                    } else {
                        Toast.makeText(this, "Firestore 데이터 가져오기 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveDataLocally(QuerySnapshot querySnapshot) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray allData = new JSONArray();
        Log.i("pass", "Saving data locally");

        for (DocumentSnapshot document : querySnapshot) {
            try {
                // Firestore에서 데이터를 가져옵니다.
                double latitude = document.getDouble("Latitude");
                double longitude = document.getDouble("Longitude");
                String hour = document.getString("24hour");
                String storeName = document.getString("store_name");
                String address = document.getString("address");
                String phoneNum;
                if (document.contains("phone_num")) {
                    Object phoneNumObj = document.get("phone_num");
                    if (phoneNumObj instanceof String) {
                        phoneNum = (String) phoneNumObj; // 문자열로 저장된 경우
                    } else if (phoneNumObj instanceof Number) {
                        phoneNum = String.valueOf(phoneNumObj); // 숫자로 저장된 경우 문자열로 변환
                    } else {
                        phoneNum = "N/A"; // 알 수 없는 타입인 경우 기본값
                    }
                } else {
                    phoneNum = "N/A"; // 필드가 없는 경우 기본값
                }                String rating = document.getString("rating");
                String businessHours = document.getString("business_hours");

                // 데이터를 JSON 객체로 묶기
                JSONObject locationData = new JSONObject();
                locationData.put("Latitude", latitude);
                locationData.put("Longitude", longitude);
                locationData.put("24hour", hour);
                locationData.put("store_name", storeName);
                locationData.put("address", address);
                locationData.put("phone_num", phoneNum);
                locationData.put("rating", rating);
                locationData.put("business_hours", businessHours); // 이 부분은 문자열로 저장

                allData.put(locationData); // 데이터를 JSONArray에 추가

                // JSON 객체를 문자열로 변환하여 저장

                editor.putString(document.getId(), locationData.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        editor.putString("hospital_data", allData.toString());

        editor.apply();  // 변경 사항 적용

        Toast.makeText(this, "데이터를 로컬에 저장했습니다.", Toast.LENGTH_SHORT).show();
    }



    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}