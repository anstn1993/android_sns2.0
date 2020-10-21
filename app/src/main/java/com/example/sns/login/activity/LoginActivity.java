package com.example.sns.login.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sns.*;
import com.example.sns.chat.service.ChatService;
import com.example.sns.join.activity.JoinActivity;
import com.example.sns.login.model.LoginUser;
import com.example.sns.main.activity.MainActivity;
import com.example.sns.util.HttpRequest;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements HttpRequest.OnHttpResponseListener {

    private final String TAG = "LoginActivity";
    private EditText et_account, et_password;//아이디, 비밀번호 입력 edit text
    private Button btn_login, btn_join;//로그인, 회원가입 버튼

    //서버와 http통신을 계속해서 하는데 세션id
    public static HttpURLConnection httpURLConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //자동로그인 관련 처리 메소드
        autoLogin();

        //아이디, 비밀번호 입력 칸
        et_account = findViewById(R.id.edittext_account);
        et_password = findViewById(R.id.edittext_password);

        //로그인, 회원가입 버튼
        btn_login = findViewById(R.id.button_login);
        btn_join = findViewById(R.id.button_join);

        //로그인 버튼 클릭 리스너
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //입력받은 아이디와 비밀번호
                String account = et_account.getText().toString();
                String password = et_password.getText().toString();
                SharedPreferences sharedPreferences = getSharedPreferences("fcmToken", MODE_PRIVATE);
                //단말기의 fcm 토큰을 로그인하려는 사용자의 계정에 연동해준다.
                String token = sharedPreferences.getString("token", null);
                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("account", account);
                    requestBody.put("password", password);
                    requestBody.put("token", token);
                    HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "logincheck.php", LoginActivity.this);
                    httpRequest.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        //회원가입 버튼 클릭 리스너
        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
                startActivity(intent);
            }
        });


    }


    //자동로그인 메소드
    private void autoLogin() {

        //shared에 저장된 로그인된 사용자 정보 호출
        SharedPreferences sharedPreferences = getSharedPreferences("loginUser", MODE_PRIVATE);

        //로그인 유저 정보가 존재하면 자동으로 메인 액티비티로 연결시켜준다.
        String account = sharedPreferences.getString("account", null);
        if (account != null) {
            JSONObject requestBody = new JSONObject();//requestBody생성
            try {
                requestBody.put("account", account);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //자동로그인을 위한 서버 통신
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "autologin.php", this);
            httpRequest.execute();
        }
        //쿠키가 null이면 로그아웃 상태
        else {
            Log.d("세션 쿠키", "null");
        }
    }

    //통신에 성공했을 때 호출되는 콜백 메소드
    @Override
    public void onHttpResponse(String result) {
        Log.d(TAG, "통신 성공");
        Log.d(TAG, "서버에서 넘어온 값: " + result);
        if (result != null) {
            try {
                JSONObject responseBody = new JSONObject(result);
                String requestType = responseBody.getString("requestType");
                if (requestType.equals("autoLogin")) {//자동로그인을 위한 통신을 한 경우
                    String account = responseBody.getString("account");
                    String nickname = responseBody.getString("nickname");
                    String profile = responseBody.getString("profile");

                    LoginUser.initInstance(account, nickname, profile);//사용자 정보 초기화

                    //메인화면 접근
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    //채팅 서비스 실행
                    if (isChatServiceRunning()) {
                        Log.d("채팅 서비스", "실행중");
                    } else {
                        Log.d("채팅 서비스", "최초 실행");
                        Intent serviceIntent = new Intent(getApplicationContext(), ChatService.class);
                        startService(serviceIntent);
                    }
                    this.finish();
                    Toast.makeText(getApplicationContext(), "환영합니다. " + nickname + "님", Toast.LENGTH_SHORT).show();


                } else {//일반 로그인을 위한 통신인 경우
                    String loginResult = responseBody.getString("loginResult");//로그인 결과
                    if (loginResult.equals("fillCompletely")) {//아이디나 비밀번호중 적어도 하나를 입력하지 않은 경우
                        Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 모두 입력해주세요", Toast.LENGTH_SHORT).show();
                    } else if (loginResult.equals("checkAgain")) {//아이디나 비밀번호를 잘못 입력한 경우
                        Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show();
                    } else {//아이디와 비밀번호를 모두 잘 입력한 경우

                        String account = responseBody.getString("account");
                        String nickname = responseBody.getString("nickname");
                        String profile = responseBody.getString("profile");
                        LoginUser.initInstance(account, nickname, profile);//사용자 정보 초기화

                        //사용자 정보 로컬에 저장
                        SharedPreferences sharedPreferences = getSharedPreferences("loginUser", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.putString("account", account);
                        editor.putString("nickname", nickname);
                        editor.putString("profile", profile);

                        editor.apply();

                        //권한 체크
                        TedPermission.with(getApplicationContext())
                                .setPermissionListener(new PermissionListener() {
                                    //권한 허가가 됐을 때 콜백
                                    @Override
                                    public void onPermissionGranted() {
                                        try {
                                            Toast.makeText(getApplicationContext(), "환영합니다. " + responseBody.getString("nickname") + "님", Toast.LENGTH_SHORT).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        //메인 액티비티로 이동
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        //채팅 서비스 실행
                                        if (isChatServiceRunning()) {
                                            Log.d("채팅 서비스", "실행중");
                                        } else {
                                            Log.d("채팅 서비스", "최초 실행");
                                            Intent serviceIntent = new Intent(getApplicationContext(), ChatService.class);
                                            startService(serviceIntent);
                                        }
                                        finish();
                                    }

                                    //권한 허가 거부가 됐을 때 콜백
                                    @Override
                                    public void onPermissionDenied(List<String> deniedPermissions) {
                                        try {
                                            Toast.makeText(getApplicationContext(), "환영합니다. " + responseBody.getString("nickname") + "님", Toast.LENGTH_SHORT).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        //메인 액티비티로 이동
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        //채팅 서비스 실행
                                        if (isChatServiceRunning()) {
                                            Log.d("채팅 서비스", "실행중");
                                        } else {
                                            Log.d("채팅 서비스", "최초 실행");
                                            Intent serviceIntent = new Intent(getApplicationContext(), ChatService.class);
                                            startService(serviceIntent);
                                        }
                                        finish();
                                    }
                                })
                                .setDeniedMessage("권한을 허가하지 않으면 일부 기능을 사용할 수 없습니다.\n만약 기능을 사용하고 싶다면 설정에서 권한을 허용해주세요.")
                                .setPermissions(new String[]{
                                        Manifest.permission.CAMERA,//카메라
                                        Manifest.permission.RECORD_AUDIO,//오디오
                                        Manifest.permission.MODIFY_AUDIO_SETTINGS,//오디오 세팅
                                        Manifest.permission.ACCESS_COARSE_LOCATION,//위치
                                        Manifest.permission.ACCESS_FINE_LOCATION,//위치
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE//외부 저장소 사용
                                })
                                .check();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //서비스 실행 여부를 가리기 위한 메소드
    private boolean isChatServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getApplication().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (ChatService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
