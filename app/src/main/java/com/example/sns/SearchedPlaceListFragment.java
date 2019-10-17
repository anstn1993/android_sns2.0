package com.example.sns;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class SearchedPlaceListFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    TextView tv_place, tv_placeTab, tv_nearTab;
    ImageButton ib_back;

    MapView mapView;
    Button btn_distance;

    //넘어온 장소명
    String address;
    //넘어온 위도와 경도
    double latitude;
    double longitude;

    //장소기반으로 가져온 그리드 게시물, 주변 게시물 프래그먼트를 제어할 메니저
    FragmentManager childFragmentManager;

    //장소 기반으로 가져온 그리드 게시물 프래그먼트
    GridPlacePostFragment gridPlacePostFragment;
    //검색된 장소의 주변 게시물 프래그먼트
    GridNearPostFragment gridNearPostFragment;

    //구글맵 제어 객체
    public static GoogleMap mGoogleMap;

    //부모 액티비티
    String parentActivity;

    //주변 게시물 기준 거리
    public static int setDistance;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //처음 기준거리는 1km로 잡는다.
        setDistance =1000;

        if (getArguments() != null) {
            address = getArguments().getString("address");
            latitude = getArguments().getDouble("latitude");
            longitude = getArguments().getDouble("longitude");
            parentActivity = getArguments().getString("parentActivity");
        }

        View rootView = inflater.inflate(R.layout.fragment_searchedplacelist, container, false);

        mapView = rootView.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);


        tv_place = rootView.findViewById(R.id.textview_place);
        tv_place.setText(address);

        tv_placeTab = rootView.findViewById(R.id.textview_place_tab);
        tv_placeTab.setTextColor(Color.parseColor("#0092EF"));
        tv_nearTab = rootView.findViewById(R.id.textview_near_tab);
        ib_back = rootView.findViewById(R.id.imagebutton_back);

        //주변 게시물 기준 거리 설정 버튼
        btn_distance = rootView.findViewById(R.id.button_distance);
        btn_distance.setVisibility(View.GONE);

        childFragmentManager = getChildFragmentManager();
        gridPlacePostFragment = new GridPlacePostFragment();
        gridNearPostFragment = new GridNearPostFragment();

        Bundle bundle = new Bundle();
        bundle.putString("address", address);
        bundle.putDouble("latitude", latitude);
        bundle.putDouble("longitude",longitude);
        bundle.putString("parentActivity", parentActivity);
        gridPlacePostFragment.setArguments(bundle);
        gridNearPostFragment.setArguments(bundle);

        childFragmentManager.beginTransaction().add(R.id.frame_container, gridPlacePostFragment).commit();
        childFragmentManager.beginTransaction().add(R.id.frame_container, gridNearPostFragment).commit();

        childFragmentManager.beginTransaction().hide(gridNearPostFragment).commit();
        childFragmentManager.beginTransaction().show(gridPlacePostFragment).commit();

        tv_placeTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //검색된 장소의 게시물 리스트 프래그먼트로 교체를 해준다.
                childFragmentManager.beginTransaction().hide(gridNearPostFragment).commit();
                childFragmentManager.beginTransaction().show(gridPlacePostFragment).commit();
                tv_placeTab.setTextColor(Color.parseColor("#0092EF"));
                tv_nearTab.setTextColor(Color.parseColor("#000000"));
                btn_distance.setVisibility(View.GONE);
            }
        });

        tv_nearTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //검색된 장소의 주변 게시물 리스트 프래그먼트로 교체를 해준다.
                childFragmentManager.beginTransaction().hide(gridPlacePostFragment).commit();
                childFragmentManager.beginTransaction().show(gridNearPostFragment).commit();
                tv_placeTab.setTextColor(Color.parseColor("#000000"));
                tv_nearTab.setTextColor(Color.parseColor("#0092EF"));
                btn_distance.setVisibility(View.VISIBLE);
            }
        });

        //뒤로가기 클릭 리스너
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(parentActivity.equals("PostActivity")){
                    if(PostActivity.fragmentManager.getBackStackEntryCount()==0){
                        PostActivity.fragmentManager.beginTransaction().remove(PostActivity.searchedPlaceListFragment).commit();
                        PostActivity.fragmentManager.popBackStack();
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);
                    }else {
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙인다.
                        PostActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getActivity(), EmptyActivity.class);
                        startActivity(intent);
                    }
                }
                else if(parentActivity.equals("SearchActivity")){
                    if(SearchActivity.fragmentManager.getBackStackEntryCount()==0){
                        SearchActivity.fragmentManager.beginTransaction().remove(SearchActivity.searchedPlaceListFragment).commit();
                        SearchActivity.fragmentManager.popBackStack();
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);
                    }else {
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙인다.
                        SearchActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getActivity(), EmptyActivity.class);
                        startActivity(intent);
                    }
                }
                else if(parentActivity.equals("MyPageActivity")){
                    if(MypageActivity.fragmentManager.getBackStackEntryCount()==0){
                        MypageActivity.fragmentManager.beginTransaction().remove(MypageActivity.searchedPlaceListFragment).commit();
                        MypageActivity.fragmentManager.popBackStack();
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);
                    }else {
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙인다.
                        MypageActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getActivity(), EmptyActivity.class);
                        startActivity(intent);
                    }
                }
                else {
                    if(NotificationActivity.fragmentManager.getBackStackEntryCount()==0){
                        NotificationActivity.fragmentManager.beginTransaction().remove(NotificationActivity.searchedPlaceListFragment).commit();
                        NotificationActivity.fragmentManager.popBackStack();
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);
                    }else {
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙인다.
                        NotificationActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getActivity(), EmptyActivity.class);
                        startActivity(intent);
                    }
                }

            }
        });

        //주변 게시물 기준 거리 설정 버튼 클릭 리스너
        btn_distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.distance_setting_box);

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if(parentActivity.equals("SearchActivity")){
                            SearchActivity.softKeyboard.closeSoftKeyboard();
                        }
                    }
                });

                //라디오 버튼
                RadioButton rb_100m, rb_300m, rb_500m, rb_1000m;
                //적용, 취소 버튼
                TextView tv_apply, tv_cancel;

                rb_100m = dialog.findViewById(R.id.radiobutton_100m);
                rb_300m = dialog.findViewById(R.id.radiobutton_300m);
                rb_500m = dialog.findViewById(R.id.radiobutton_500m);
                rb_1000m = dialog.findViewById(R.id.radiobutton_1000m);
                tv_apply = dialog.findViewById(R.id.textview_apply);
                tv_cancel = dialog.findViewById(R.id.textview_cancel);

                //설정된 거리가 100m이면
                if(setDistance==100){
                    //100미터에 자동 체크
                    rb_100m.setChecked(true);
                }
                //설정된 거리가 300m이면
                else if(setDistance == 300){
                    //300m에 자동 체크
                    rb_300m.setChecked(true);
                }
                //설정된 거리가 500m이면
                else if(setDistance == 500){
                    //500m에 자동 체크
                    rb_500m.setChecked(true);
                }
                //설정된 거리가 1km이면
                else {
                    //1km에 자동 체크
                    rb_1000m.setChecked(true);
                }

                //적용버튼 클릭 리스너
                tv_apply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //100미터로 체크한 경우
                        if(rb_100m.isChecked()){
                            setDistance = 100;
                            setMap(setDistance, 16, latitude, longitude);
                            Intent intent = new Intent(getActivity(), EmptyActivity.class);
                            startActivity(intent);
                            btn_distance.setText("100m");
                        }
                        //300미터로 체크한 경우
                        else if(rb_300m.isChecked()){
                            setDistance = 300;
                            setMap(setDistance, 15, latitude, longitude);
                            Intent intent = new Intent(getActivity(), EmptyActivity.class);
                            startActivity(intent);
                            btn_distance.setText("300m");
                        }
                        //500m로 체크한 경우
                        else if(rb_500m.isChecked()){
                            setDistance = 500;
                            setMap(setDistance, 14, latitude, longitude);
                            Intent intent = new Intent(getActivity(), EmptyActivity.class);
                            startActivity(intent);
                            btn_distance.setText("500m");
                        }
                        //1km로 체크한 경우
                        else {
                            setDistance = 1000;
                            setMap(setDistance, 13, latitude, longitude);
                            Intent intent = new Intent(getActivity(), EmptyActivity.class);
                            startActivity(intent);
                            btn_distance.setText("1km");
                        }

                        dialog.dismiss();
                    }
                });

                //취소버튼 클릭 리스너
                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();

            }
        });

        return rootView;
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onMapReady","호출");
        mGoogleMap = googleMap;

        setMap(1000, 13, latitude, longitude);

    }
    //param1: 기준 거리
    //param2: 카메라 줌 값
    //param3: 검색된 장소의 위도
    //param4: 검색된 장소의 경도
    public void setMap(int distance, int zoom, double latitude, double longitude){

        //다른 화면에 갔다가 돌아오면 모두 클리어를 해준 후 다시 마커와 반경원을 추가해준다.
        //안 그러면 기존 마커와 반경원 위에 누적이 되는 문제가 생김.
        mGoogleMap.clear();

        MarkerOptions markerOptions = new MarkerOptions();
        //검색된 장소의 위치 객체
        LatLng latLng = new LatLng(latitude, longitude);
        markerOptions.position(latLng);
        //지도 이동
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //지도 줌인
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
        //마커 추가
        mGoogleMap.addMarker(markerOptions);

        CircleOptions circleOptions = new CircleOptions();
        circleOptions
                //중점
                .center(latLng)
                //반지름(단위:미터)
                .radius(distance)
                //원의 선 너비
                .strokeWidth(0f)
                //원의 배경색
                .fillColor(Color.parseColor("#880000ff"));
        mGoogleMap.addCircle(circleOptions);

    }
}
