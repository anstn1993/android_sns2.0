package com.example.sns.post.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.sns.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AddLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private Button btn_apply, btn_cancel;
    private TextView tv_location;

    private String address;
    private String markerTitle;
    private String markerSnippet;
    private double latitude, longitude;

    private GoogleMap mGoogleMap = null;
    private Marker currentMarker = null;

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;

    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;


    // 앱을 실행하기 위해 필요한 퍼미션을 정의
    private String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

    private LatLng currentPosition;


    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;


    //스낵바를 사용하기 위해서는 스낵바가 실행될 현재 view를 가져와야 한다.
    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        //place초기화(api가 새롭게 업그레이드 되면서 무조건 이 절차를 거쳐줘야 함)
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyD7t_2m-6ZE8blxYMdkTdBm_BU5VgULe7k");
        }

        mLayout = findViewById(R.id.ConstraintLayout_map);
        tv_location = findViewById(R.id.tv_location);
        btn_apply = findViewById(R.id.btn_apply);
        btn_cancel = findViewById(R.id.btn_cancel);
        //현재의 위치를 요청할 때 설정을 하는 객체로 여러 설정을 통해서 현재 위치를 가져올 수 있다.
        locationRequest = new LocationRequest()
                //위치를 요청할 때는 4가지 방식으로 요청할 수 있는데 지금은 배터리소모는 고려하지 않고 최대한 정확한 위치를 가져오라는 것
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //실제로 설정된 request를 LocationSettingsRequest를 통해서 위치를 요청한다.
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        //param:설정값이 담긴 객체
        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //지도를 프래그먼트에 뿌려준다.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(this);


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });

        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("address", address);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                setResult(RESULT_OK, intent);
                finish();
                try {
                    Log.d("주소", address);
                    Log.d("위도", String.valueOf(latitude));
                    Log.d("경도", String.valueOf(longitude));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });


        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            //자동완성 검색에서 선택한 장소 데이터가 이 메소드로 넘어온다. 여기서 데이터 캐치
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("장소 선택 이후", "Place: " + place.getName() + ", " + place.getId());

                try {
                    mGoogleMap.clear();
                    tv_location.setText(place.getName());
                    address = place.getName();
                    Log.d("검색된 장소", address);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    List<Address> searchedLocation = geocoder.getFromLocationName(address, 10);
                    latitude = searchedLocation.get(0).getLatitude();
                    Log.d("검색된 장소의 위도", String.valueOf(latitude));
                    longitude = searchedLocation.get(0).getLongitude();
                    Log.d("검색된 장소의 경도", String.valueOf(longitude));


                    //검색된 장소의 위도 경도 정보를 담는 객체 생성
                    LatLng location = new LatLng(latitude, longitude);
                    //마커 옵션 설정 객체
                    MarkerOptions markerOptions = new MarkerOptions();
                    //마커의 타이틀은 검색한 장소의 이름으로
                    markerOptions.title(address);
                    //마커 찍힐 장소
                    markerOptions.position(location);
                    //구글맵에 마커로 추가
                    mGoogleMap.addMarker(markerOptions);
                    //검색된 장소로 지도 이동
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                    //검색된 장소 줌인
                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //기능이 정상 작동하지 않는 경우 호출되는 메소드. 오류의 내용을 여기서 확인하면 됨.
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("장소 선택 에러", "An error occurred: " + status);
            }
        });
    }

    //클라이언트에서 위치를 가져올 때 호출되는 콜백 객체다.
    LocationCallback locationCallback = new LocationCallback() {
        //단말의 위치 정보를 사용할 수 있을 때 호출된다.
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            //위치 정보를 list에 담는데 이때 가장 최신에 업데이트 된 위치가 제일 마지막 index로 들어간다.
            List<Location> locationList = locationResult.getLocations();

            //위치 정보가 있는 경우
            if (locationList.size() > 0) {
                //위치 변수에 가장 최신 위치를 넣고
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);
                //현재 위치의 위도 경도 객체에 현재 위치의 위도와 경도를 저장해준다.
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());


                markerTitle = getCurrentAddress(currentPosition);
                markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                Log.d(TAG, "onLocationResult : " + markerSnippet);


                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);
            }


        }

    };

    private void startLocationUpdates() {

        //위치 서비스 상태가 정상이 아닐 때
        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);


            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }


            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mGoogleMap.setMyLocationEnabled(true);

        }

    }

    //지도가 로드되면 호출되는 함수
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        //지도를 클릭했을 때 동작은 밑에 onMapClick메소드를 통해서 구현된다. 구글맵이 그 클릭 리스너가 되는 것
        mGoogleMap.setOnMapClickListener(this);

        //현재 위치 버튼을 누르면 벌어지는 동작 설정
        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mGoogleMap.clear();
                setCurrentLocation(location, markerTitle, markerSnippet);

                return false;
            }
        });

        //런타임 퍼미션 요청 대화상자나 GPS활성 요청 대화상자가 보이기 전에 지도의 초기 위치를 서울로 이동
        setDefaultLocation();

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        // 2. 이미 퍼미션을 가지고 있다면
        // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식)
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {


            startLocationUpdates(); // 3. 위치 업데이트 시작


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요하다. 2가지 경우(3-1, 4-1)가 있음

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있음
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신
                        ActivityCompat.requestPermissions(AddLocationActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청
                // 요청 결과는 onRequestPermissionResult에서 수신
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

        //구글맵 화면에 내 위치 버튼을 활성화시킨다.
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        //내 위치를 15만큼 확대해서 보이게 한다.
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));


    }

    //지도를 클릭했을 때 동작 정의
    //param은 클릭된 위치의 위도, 경도 값
    @Override
    public void onMapClick(LatLng latLng) {
        mGoogleMap.clear();

        //현재 컨텍스트에서 지오 코더를 사용하여 좌표를 주소명으로 변환
        Geocoder geocoder = new Geocoder(getApplicationContext());

        //전역변수(intent로 넘겨줄 변수값)의 위도, 경도에 클릭한 장소의 위도 경도를 넣어준다.
        latitude = latLng.latitude;
        longitude = latLng.longitude;


        try {
            //새로운 리스트를 생성해서 지오코더를 통해서 위도 경도를 통해 반환되는 주소명을 가져온다.
            List<Address> locationAddress = geocoder.getFromLocation(latitude, longitude, 1);
            //마커 옵션 설정
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);

            //전역변수로 설정한 address에 클릭한 장소의 주소를 넣어준다.
            address = locationAddress.get(0).getAddressLine(0);
            markerOptions.title(address);
            //마커 생성
            mGoogleMap.addMarker(markerOptions);
            //지도 이동
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //지도 줌인
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            //위치를 나타내주는 칸에 주소 셋
            tv_location.setText(address);


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {

            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mGoogleMap != null)
                mGoogleMap.setMyLocationEnabled(true);

        }

    }

    //앱을 나간 상태에서는 위치 업데이트를 멈춤
    @Override
    protected void onStop() {
        super.onStop();

        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        //현재 위치 저장
        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
            latitude = latlng.latitude;
            longitude = latlng.longitude;
            address = addresses.get(0).getAddressLine(0);
            tv_location.setText(address);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            Log.d("인덱스 에러", indexOutOfBoundsException.getMessage());
            return "인덱스 에러";
        }


        if (addresses == null || addresses.size() == 0) {

            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }

    //현재 위치 서비스 상태 체크
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    //현재 위치 설정 메소드
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        //현재 마커가 존재한다면 마커를 지운다.
        if (currentMarker != null) currentMarker.remove();

        //현재 위치를 위도 경도 객체로 선언
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        //지오 코더 선언
        Geocoder geocoder = new Geocoder(getApplicationContext());

        //현재 위치의 위도 경도를 전역변수로 설정한 위도 경도 변수에 넣어준다.
        latitude = currentLatLng.latitude;
        longitude = currentLatLng.longitude;


        List<Address> locationAddress = null;
        try {
            locationAddress = geocoder.getFromLocation(latitude, longitude, 1);
            //전역변수로 설정한 주소에 현재 위치의 주소를 넣어준다.
            address = locationAddress.get(0).getAddressLine(0);
            //위치를 보여주는 부분에 주소 셋
            tv_location.setText(address);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException indexOutBoundsException) {
            Log.d("인덱스 에러", indexOutBoundsException.getMessage());
        }


        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.draggable(true);


        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mGoogleMap.moveCamera(cameraUpdate);

    }

    //지도가 처음 실행되면 이동시킬 포커스를 지정하기 위해 존재하는 메소드
    //하지만 현재 위치가 파악되면 그 곳으로 포커스가 이동됨
    public void setDefaultLocation() {


        //디폴트 위치, Seoul
        //gps가 현재 위치를 가져오지 못할 때를 대비해서 만든 초기 위치
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 여부를 확인하세요";

        //현재 마커가 존재한다면 마커를 지워준다.
        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        //마커의 위치를 서울로 잡아주고
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        //전역변수로 설정해둔 마커 변수에 마커 옵션으로 지정이 끝난 마커를 추가해서 넣어줌
        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mGoogleMap.moveCamera(cameraUpdate);

    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;

    }


    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {
                // 퍼미션을 허용했다면 위치 업데이트를 시작
                startLocationUpdates();
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료. 2 가지 경우가 존재.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();

                } else {


                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }

        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddLocationActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");
                        needRequest = true;
                        return;
                    }
                }
                break;
        }
    }
}
