package com.example.sns.facecall.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.example.sns.R;
import com.example.sns.chat.service.ChatService;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class FaceChatReceiveActivity extends AppCompatActivity {
    private String TAG = "FaceChatReceiveActivity";

    //뷰
    private CircleImageView cv_profile;//발신자 프로필
    private TextView tv_nickname;//발신자 닉네임
    private ImageButton ib_declineCall, ib_acceptCall;//통화 거절, 수락
    //발신자 데이터
    private String account;
    private String nickname;
    private String profile;
    //방 이름
    private String roomName;
    //수신자 계정
    private String receiver;
    //화면 on-off 상태
    private boolean screenOn;
    //바이브레이터 객체
    private Vibrator vibrator;
    //화면이 꺼진 상태에서 전화가 왔을 때 화면을 깨우기 위한 객체들
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    //발신자가 통화 취소를 누르면 수신자에게 전송되는 메세지를 받기 위한 헨들러
    public static Handler handler;

    //통화 수락을 누르면 홈버튼을 누르지 않아도 홈버튼을 눌렀을 때 호출되는 메소드가 호출되기 때문에 거절 메세지도 함께 전송된다.
    //그걸 방지하기 위해서 boolean으로 수락이나 거절 버튼을 눌렀을 때를 분기해준다.
    private boolean isResponsed = false;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_chat_receive);
        //인텐트 값 회수
        if (getIntent() != null) {
            account = getIntent().getStringExtra("account");
            nickname = getIntent().getStringExtra("nickname");
            profile = getIntent().getStringExtra("profile");
            roomName = getIntent().getStringExtra("roomName");
            receiver = getIntent().getStringExtra("receiver");
            screenOn = getIntent().getBooleanExtra("screenOn", true);
        }

        //진동 설정
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {1000, 500, 1000, 50};//진동 패턴 설정
        vibrator.vibrate(pattern, 0);//param2: 0-무한 반복, -1- 한번만

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//화면 켜짐 유지

        if (!screenOn) {//화면이 꺼진 상태에서 해당 액티비티로 접근한 경우
            powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);//파워 매니저
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "WAKE_LOCK");
            wakeLock.acquire();//화면 깨우기
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);//잠금화면위에 액티비티를 쌓아준다.
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);//잠금 해제

        }


        //뷰 초기화
        cv_profile = findViewById(R.id.circleimageview_profile);
        tv_nickname = findViewById(R.id.textview_nickname);
        ib_acceptCall = findViewById(R.id.imageview_acceptcall);
        ib_declineCall = findViewById(R.id.imageview_declinecall);

        //프로필 사진 설정
        Glide.with(this).load("http://13.124.105.47/profileimage/" + profile)
                .thumbnail(0.1f)
                .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                .into(cv_profile);
        //닉네임 설정
        tv_nickname.setText(nickname);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//영상통화 요청을 수신하면 화면이 꺼지지 않게 설정

        //통화 거절 버튼 클릭 리스너
        ib_declineCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isResponsed = true;
                JSONObject responseData = new JSONObject();
                try {
                    responseData.put("type", "declineFaceChat");
                    responseData.put("sender", receiver);
                    responseData.put("receiver", account);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //메세지 전송
                Message message = ChatService.handler.obtainMessage();
                message.what = 9999;
                message.obj = responseData.toString();
                ChatService.handler.sendMessage(message);
                vibrator.cancel();//진동 종료
                if (!screenOn) {
                    wakeLock.release();//화면깨움 release
                }
                finish();//액티비티 종료
            }
        });

        //통화 수락 버튼 클릭 리스너
        ib_acceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isResponsed = true;
                vibrator.cancel();//진동 종료
                if (!screenOn) {
                    wakeLock.release();//화면깨움 release
                }
                JSONObject responseData = new JSONObject();
                try {
                    responseData.put("type", "acceptFaceChat");//메세지 타입
                    responseData.put("sender", account);//발신자 계정
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //소켓서버로 수락 메세지 전달
                Message message = ChatService.handler.obtainMessage();
                message.what = 8888;
                message.obj = responseData.toString();
                ChatService.handler.sendMessage(message);


                Intent intent = new Intent(getApplicationContext(), CallActivity.class);
                intent.putExtra("roomName", roomName);//방 이름
                intent.putExtra("screenOn", screenOn);//call 액티비티에 접근시 화면이 꺼져있었는지 켜져있었는지 파악
                //어플이 실행중이지 않은 상태에서 해당 액티비티가 실행될 수도 있기 때문에 수신자의 계정을 sharedpreference에서 가져온다.
                intent.putExtra("receiver", getSharedPreferences("sessionCookie", MODE_PRIVATE).getString("userAccount", null));
                intent.putExtra("sender", account);
                startActivity(intent);
                finish();
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String jsonString = msg.obj.toString();
                try {
                    String type = new JSONObject(jsonString).getString("type");
                    if (type.equals("cancelFaceChat")) {//발신자가 통화를 취소한 경우
                        vibrator.cancel();//진동 종료
                        if (!screenOn && wakeLock.isHeld()) {
                            wakeLock.release();//화면깨움 release
                        }
                        finish();//액티비티 종료
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();뒤로가기 버튼 비활성화
    }

    @Override
    protected void onUserLeaveHint() {
        Log.d(TAG, "홈버튼 클릭");
        super.onUserLeaveHint();
        if(!isResponsed) {
            JSONObject responseData = new JSONObject();
            try {
                responseData.put("type", "declineFaceChat");
                responseData.put("sender", receiver);
                responseData.put("receiver", account);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //메세지 전송
            Message message = ChatService.handler.obtainMessage();
            message.what = 9999;
            message.obj = responseData.toString();
            ChatService.handler.sendMessage(message);
            vibrator.cancel();//진동 종료
            if (!screenOn && wakeLock.isHeld()) {//화면이 꺼진 상태였고 wakelock이 실행된 경우(isHeld를 if로 감싸주지 않으면 runtimeexception오류가 난다)
                wakeLock.release();//화면깨움 release
            }
            finish();//액티비티 종료
        }
    }
}
