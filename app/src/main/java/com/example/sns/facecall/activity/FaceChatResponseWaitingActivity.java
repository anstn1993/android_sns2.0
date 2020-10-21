package com.example.sns.facecall.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.example.sns.R;
import com.example.sns.chat.service.ChatService;
import com.example.sns.login.model.LoginUser;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class FaceChatResponseWaitingActivity extends AppCompatActivity {
    private final String TAG = "FaceChatResponseWaitingActivity";
    private CircleImageView cv_profile;
    private TextView tv_nickname;
    private ImageButton ib_endCall;

    public static Handler handler;//소켓 서버로부터 메세지를 받기 위한 핸들러
    //통화 수락을 누르면 홈버튼을 누르지 않아도 홈버튼을 눌렀을 때 호출되는 메소드가 호출되기 때문에 거절 메세지도 함께 전송된다.
    //그걸 방지하기 위해서 boolean으로 수락이나 거절 버튼을 눌렀을 때를 분기해준다.
    private boolean isResponsed = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_chat_response_waiting);
        cv_profile = findViewById(R.id.circleimageview_profile);
        tv_nickname = findViewById(R.id.textview_nickname);
        ib_endCall = findViewById(R.id.imagebutton_declinecall);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//화면이 꺼지지 않게 설정

        if (getIntent() != null) {
            //프로필 사진 설정
            Glide.with(this).load("http://13.124.105.47/profileimage/" + getIntent().getStringExtra("receiverProfile"))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(cv_profile);
            //닉네임 설정
            tv_nickname.setText(getIntent().getStringExtra("receiverNickname"));

        }

        //통화 종료 버튼 클릭 리스너
        ib_endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isResponsed = true;
                JSONObject responseData = new JSONObject();
                try {
                    responseData.put("type", "cancelFaceChat");
                    responseData.put("sender", LoginUser.getInstance().getAccount());//발신자 계정(자신)
                    responseData.put("receiver", getIntent().getStringExtra("receiverAccount"));//수신자 계정
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Message message = ChatService.handler.obtainMessage();
                message.obj = responseData.toString();
                ChatService.handler.sendMessage(message);
                finish();//액티비티 종료
            }
        });




        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    String jsonString = msg.obj.toString();
                    String response = new JSONObject(jsonString).getString("type");
                    if (response.equals("acceptFaceChat")) {//수신자가 영상통화를 수락한 경우
                        isResponsed = true;
                        Intent intent = new Intent(FaceChatResponseWaitingActivity.this, CallActivity.class);
                        intent.putExtra("roomName", getIntent().getStringExtra("roomName"));
                        intent.putExtra("screenOn", true);
                        intent.putExtra("receiver", getIntent().getStringExtra("receiverAccount"));
                        intent.putExtra("sender", LoginUser.getInstance().getAccount());
                        startActivity(intent);
                        finish();
                    }else {//declineFaceChat(수신자가 영상통화를 거절한 경우)
                        isResponsed = true;
                        Toast.makeText(FaceChatResponseWaitingActivity.this, "상대방이 통화를 거절하였습니다.", Toast.LENGTH_SHORT).show();
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
                responseData.put("type", "cancelFaceChat");
                responseData.put("sender", LoginUser.getInstance().getAccount());//발신자 계정(자신)
                responseData.put("receiver", getIntent().getStringExtra("receiverAccount"));//수신자 계정
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Message message = ChatService.handler.obtainMessage();
            message.obj = responseData.toString();
            ChatService.handler.sendMessage(message);
            finish();//액티비티 종료
        }
    }
}
