package com.example.animal;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;


import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private NaverMap naverMap;
    private FusedLocationSource locationSource;
    private InfoWindow infoWindow; //


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_location, container, false);


        infoWindow = new InfoWindow();


                // FusedLocationSource 생성 (권한 요청 코드와 함께 Activity 객체 전달)
                locationSource = new FusedLocationSource(getActivity(), LOCATION_PERMISSION_REQUEST_CODE);

                // 이미 XML 레이아웃에 설정된 MapFragment를 가져오기
                FragmentManager fm = getChildFragmentManager();
                MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment);

                if (mapFragment == null) {
                    mapFragment = MapFragment.newInstance();
                    fm.beginTransaction().replace(R.id.map_fragment, mapFragment).commit();
                }
                // 지도 준비 작업
                mapFragment.getMapAsync(this);


                return rootView;
            }


            private void addMarkersFromLocalData() {
                // SharedPreferences에서 데이터 불러오기
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("local_data", Context.MODE_PRIVATE);
                String jsonString = sharedPreferences.getString("hospital_data", null); // "hospital_data"로 전체 JSONArray 가져오기

                if (jsonString != null) {
                    try {
                        // JSON 배열로 변환
                        JSONArray jsonArray = new JSONArray(jsonString);
                        Log.d("TAG", "addMarkersFromLocalData: 데이터 개수 - " + jsonArray.length());

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            // 좌표와 기타 정보를 읽음
                            double latitude = jsonObject.getDouble("Latitude");
                            double longitude = jsonObject.getDouble("Longitude");
                            String storeName = jsonObject.getString("store_name");
                            String address = jsonObject.getString("address");
                            String phoneNum = jsonObject.getString("phone_num");
                            String rating = jsonObject.getString("rating");
                            String businessHours = jsonObject.getString("business_hours");
                            String hour = jsonObject.getString("24hour");


                            // 마커 추가
                            Marker marker = new Marker();
                            marker.setPosition(new LatLng(latitude, longitude));
                            marker.setCaptionText(storeName); // 마커의 제목 표시
                            marker.setMap(naverMap);

                            marker.setOnClickListener(overlay -> {
                                if (marker.getInfoWindow() != null) {
                                    infoWindow.close();
                                } else {
                                    infoWindow.setAdapter(new InfoWindow.DefaultViewAdapter(getContext()) {
                                        @NonNull
                                        @Override
                                        protected View getContentView(@NonNull InfoWindow infoWindow) {
                                            View view = LayoutInflater.from(getContext()).inflate(R.layout.marker_info, null);



                                            TextView marker_24 = view.findViewById(R.id.marker_24);
                                            TextView marker_address = view.findViewById(R.id.marker_address);
                                            TextView marker_hours = view.findViewById(R.id.marker_hours);
                                            TextView marker_phone = view.findViewById(R.id.marker_phone);
                                            TextView marker_rating = view.findViewById(R.id.marker_rating);
                                            TextView marker_store = view.findViewById(R.id.marker_store);

                                            // 마커별 데이터 설정
                                            marker_store.setText("이름 "+storeName);
                                            marker_address.setText("도로명 주소 "+address);
                                            marker_phone.setText("전화번호 "+phoneNum);
                                            marker_rating.setText(rating);
                                            marker_hours.setText("영업시간 "+businessHours);
                                            marker_24.setText("24시간 영업 여부 "+hour);


                                            return view;
                                        }
                                    });
                                    infoWindow.open(marker);
                                }
                                return true;
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("TAG", "addMarkersFromLocalData: JSON 파싱 오류", e);
                    }
                } else {
                    Log.d("TAG", "addMarkersFromLocalData: 저장된 데이터 없음");
                }
            }


            @Override
            public void onMapReady(NaverMap naverMap) {
                this.naverMap = naverMap;

                // FusedLocationSource를 NaverMap에 설정
                naverMap.setLocationSource(locationSource);

                // 지도 UI 설정
                UiSettings uiSettings = naverMap.getUiSettings();
                uiSettings.setCompassEnabled(true);
                uiSettings.setScaleBarEnabled(true);
                uiSettings.setLocationButtonEnabled(true);
                uiSettings.setScrollGesturesEnabled(true);
                uiSettings.setRotateGesturesEnabled(true);
                uiSettings.setZoomGesturesEnabled(true);

                addMarkersFromLocalData();


                // 위치 추적 모드 설정 (예: 위치 추적을 켬)
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

                //카메라 위치 변경시 변경된 좌표
                naverMap.addOnCameraIdleListener(() -> {
                    CameraPosition cameraPosition = naverMap.getCameraPosition();
                });

            }


            @Override
            public void onRequestPermissionsResult(int requestCode,
                                                   @NonNull String[] permissions, @NonNull int[] grantResults) {
                if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
                    if (!locationSource.isActivated()) {  // 권한이 거부된 경우
                        if (naverMap != null) {
                            naverMap.setLocationTrackingMode(LocationTrackingMode.None);  // 위치 추적 모드 비활성화
                        }
                    }
                    return;
                }
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
