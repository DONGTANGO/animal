package com.example.animal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.animal.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private EditText id;
    private EditText pw;
    private EditText pwcheck;
    private EditText phone;
    private EditText name;
    private ImageButton back;
    private Button save;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        id = findViewById(R.id.email);
        pw = findViewById(R.id.pw);
        pwcheck = findViewById(R.id.pw_check);
        phone = findViewById(R.id.pn);
        name = findViewById(R.id.name);

        back = findViewById(R.id.back);
        save = findViewById(R.id.save);

        back.setOnClickListener(v -> finish());

        save.setOnClickListener(v -> {
            if (id.getText().toString().isEmpty() || pw.getText().toString().isEmpty() || phone.getText().toString().isEmpty() || name.getText().toString().isEmpty()) {
                Toast.makeText(SignupActivity.this, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show();
            } else if (!pw.getText().toString().equals(pwcheck.getText().toString())) {
                Toast.makeText(SignupActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            } else if (pw.getText().toString().length() < 6) {
                Toast.makeText(SignupActivity.this, "비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            } else {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(id.getText().toString(), pw.getText().toString())
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    UserModel userModel = new UserModel();
                                    userModel.userName = name.getText().toString();

//                                  String Uid = task.getResult().getUser().getUid();
                                    FirebaseDatabase.getInstance().getReference().child("users")
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(userModel)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(SignupActivity.this, "회원가입 완료", Toast.LENGTH_SHORT).show();
                                                        FirebaseAuth.getInstance().signOut();
                                                        Intent intent = new Intent(SignupActivity.this, loginActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(SignupActivity.this, "사용자 정보를 저장하는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(SignupActivity.this, "사용자 정보를 저장하는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }
}