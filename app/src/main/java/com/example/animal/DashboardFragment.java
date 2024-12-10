package com.example.animal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {


    private RecyclerView recyclerView;
    private dashboardadapter adapter;
    private List<String> itemList = new ArrayList<>();

    private FirebaseFirestore db;
    private Button upload;

    private String currentCategory = "community"; // 초기 카테고리


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Fragment의 레이아웃 설정
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // RecyclerView 설정
        recyclerView = view.findViewById(R.id.dashboardlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new dashboardadapter(itemList, currentCategory, new dashboardadapter.OnItemClickListener() {
            @Override
            public void onItemClick(String category,String title) {
                openDashboardDetail(category,title);  // 제목 클릭 시 세부 내용 보여주는 Fragment 호출
            }
        });
        recyclerView.setAdapter(adapter);

        // 카테고리 TextView 초기화
        TextView findTextView = view.findViewById(R.id.find);
        TextView communityTextView = view.findViewById(R.id.comunity);
        TextView questionTextView = view.findViewById(R.id.quetion);
        upload = view.findViewById(R.id.dashboard_write_button);


        upload.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new DashboardWriteFragment()).commitAllowingStateLoss();
        });
        // 카테고리 클릭 리스너 설정
        findTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCategoryData("find");
            }
        });

        communityTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCategoryData("community");
            }
        });

        questionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCategoryData("question");
            }
        });

        // 기본 데이터 로드 (예: 자유게시판)
        loadCategoryData("community");

        return view;
    }

    private void loadCategoryData(String category) {

        currentCategory = category; // 현재 카테고리 업데이트

        CollectionReference collection = db.collection("dashboard").document(category).collection("items");

        collection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    itemList.clear(); // 기존 데이터 제거
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String item = document.getString("title"); // Firestore 문서에서 'title' 필드 가져오기
                        if (item != null) {
                            itemList.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged(); // 어댑터에 데이터 변경 알림
                } else {
                    Toast.makeText(getContext(), "데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openDashboardDetail(String category,String title) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        DashboardDetail fragment = DashboardDetail.newInstance(category,title);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // 백스택에 추가
        transaction.commit(); // 안전하게 트랜잭션 커밋
    }
}
