package com.example.animal;



import android.os.Bundle;
import android.view.MenuItem;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private SettingFragment fragmentSetting = new SettingFragment();
    private LocationFragment fragmentLocation = new LocationFragment();
    private DiaryFragment fragmentDiary = new DiaryFragment();
    private DashboardFragment fragmentDashboard = new DashboardFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragmentDashboard).commitAllowingStateLoss();

        BottomNavigationView bottomNavigationView = findViewById(R.id.navi);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            switch (item.getItemId()) {
                case R.id.item_dashboard:
                    transaction.replace(R.id.fragment_container, fragmentDashboard).commitAllowingStateLoss();
                    break;
                case R.id.item_location:
                    transaction.replace(R.id.fragment_container, fragmentLocation).commitAllowingStateLoss();
                    break;
                case R.id.item_diary:
                    transaction.replace(R.id.fragment_container, fragmentDiary).commitAllowingStateLoss();
                    break;
                case R.id.item_settings:
                    transaction.replace(R.id.fragment_container, fragmentSetting).commitAllowingStateLoss();
                    break;
            }
            return true;
        }
    };
}