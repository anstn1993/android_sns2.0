package com.example.sns;

import android.Manifest;
import android.app.ActivityGroup;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import gun0912.tedimagepicker.builder.TedImagePicker;
import gun0912.tedimagepicker.builder.listener.OnMultiSelectedListener;
import gun0912.tedimagepicker.builder.listener.OnSelectedListener;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class MainActivity extends ActivityGroup {
    private String TAG = "MainActivity";
    private final String INTENT_FILTER_ACTION = "com.example.sns.SNS_Notification";
    public static boolean uploadClicked = false;

    public static TabHost tabHost;



    //알림이 왔을 때 생성되는 알림 표시 점
    public static ImageView iv_notificationDot;

    //푸시알림을 받는 브로드캐스트 리시버
    BroadcastReceiver notificationReceiver;

    public static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate 호출");

        //탭호스트에 들어갈 버튼에 아이콘 이미지가 담긴 뷰를 넣어주기 위해 선언하는 뷰 인스턴스
        //포스트 뷰
        View postView = LayoutInflater.from(MainActivity.this).inflate(R.layout.postbutton, null);
        //검색 뷰
        View searchView = LayoutInflater.from(MainActivity.this).inflate(R.layout.searchbutton, null);
        //업로드 뷰
        View uploadView = LayoutInflater.from(MainActivity.this).inflate(R.layout.uploadbutton, null);
        //알림 뷰
        View notificationView = LayoutInflater.from(MainActivity.this).inflate(R.layout.notificationbutton, null);
        //마이 페이지 뷰
        View mypageView = LayoutInflater.from(MainActivity.this).inflate(R.layout.mypagebutton, null);

        iv_notificationDot = findViewById(R.id.imageview_notification_dot);

        //탭 호스트 레이아웃과 연결
        tabHost = (TabHost) findViewById(R.id.tabHost);
        //탭 호스트에 탭뷰를 추가해주기 전에 셋업 반드시!!
        tabHost.setup(getLocalActivityManager());


        //게시물 탭뷰 추가
        tabHost.addTab(tabHost.newTabSpec("post")
                .setIndicator(postView)
                .setContent(new Intent(this, PostActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));


        //검색 탭뷰 추가
        tabHost.addTab(tabHost.newTabSpec("search")
                .setIndicator(searchView)
                .setContent(new Intent(this, SearchActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));

        //업로드 버튼의 경우 클릭했을 때 다른 액티비티로 이동하기 때문에 굳이 selector를 넣어서 아이콘 구분을 할 필요가 없다.
        //그래서 그냥 setIndicator 메소드의 param으로 drawable파일을 넣어준다.
        tabHost.addTab(tabHost.newTabSpec("upload")
                .setIndicator("", getResources().getDrawable(R.drawable.upload))
                .setContent(new Intent(this, UploadActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));

        tabHost.getTabWidget().getChildTabViewAt(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadClicked = true;
                Thread permissionThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //권한 요청
                        TedPermission.with(getApplicationContext())
                                .setPermissionListener(new PermissionListener() {
                                    @Override
                                    public void onPermissionGranted() {
                                        Dialog dialog = new Dialog(MainActivity.this);
                                        dialog.setContentView(R.layout.upload_content_select_box);
                                        //다이얼로그 크기 전체화면으로 채우기
                                        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                                        layoutParams.copyFrom(dialog.getWindow().getAttributes());
                                        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                                        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                        dialog.getWindow().setAttributes(layoutParams);

                                        Button btn_photo, btn_video;//사진, 동영상 버튼

                                        btn_photo = dialog.findViewById(R.id.button_photo);
                                        btn_video = dialog.findViewById(R.id.button_video);

                                        Log.d(TAG, "onPermissionGranted");
                                        Intent intent = new Intent(getApplicationContext(), UploadActivity.class);
                                        //사진 버튼 클릭 리스너
                                        btn_photo.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();//다이얼로그 지우고
//                                                intent.putExtra("content", "photo");
//                                                startActivity(intent);//업로드 액티비티로 인텐트

                                                TedImagePicker.with(MainActivity.this)
                                                        .zoomIndicator(false)
                                                        .title("이미지 선택")
                                                        .buttonText("완료")
                                                        .image()
                                                        .max(6, "더 이상 선택할 수 없습니다.")
                                                        .min(1, "이미지를 선택해주세요.")
                                                        .startMultiImage(new OnMultiSelectedListener() {
                                                            @Override
                                                            public void onSelected(List<? extends Uri> uriList) {
                                                                Intent intent = new Intent(MainActivity.this, UploadFirstActivity.class);

                                                                for (int i = 0; i < uriList.size(); i++) {

                                                                    Log.d("uri" + (i + 1), uriList.get(i).toString());
                                                                    intent.putExtra("image" + (i + 1), uriList.get(i).toString());
                                                                }
                                                                intent.putExtra("imageCount", uriList.size());
                                                                startActivity(intent);
                                                            }
                                                        });
                                            }
                                        });
                                        //동영상 버튼 클릭 리스너
                                        btn_video.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();//다이얼로그 지우고
//                                                intent.putExtra("content", "video");
//                                                startActivity(intent);//업로드 액티비티로 인텐트
                                                TedImagePicker.with(MainActivity.this)
                                                        .zoomIndicator(false)
                                                        .title("동영상 선택")
                                                        .video()
                                                        .start(new OnSelectedListener() {
                                                            @Override
                                                            public void onSelected(Uri uri) {
                                                                Log.d(TAG, "video uri: "+uri.toString());
                                                                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                                                                retriever.setDataSource(uri.getPath());
                                                                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                                                Log.d(TAG, "bitrate: "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
                                                                Log.d(TAG, "mimetype: "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE));
                                                                Log.d(TAG, "capture framerate: "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE));
                                                                Log.d(TAG, "imagecount: "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_COUNT));
                                                                Log.d(TAG, "imageheight: "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_HEIGHT));
                                                                Log.d(TAG, "imagewidth: "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH));
                                                                Log.d(TAG, "videoframecount: "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT));
                                                                Log.d(TAG, "videoheight: "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                                                                Log.d(TAG, "videowidth: "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                                                                Log.d(TAG, "videowidth: "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));

                                                                long timeInmillisec = Long.parseLong(time);
                                                                long duration = timeInmillisec / 1000;
                                                                long hours = duration / 3600;
                                                                long minutes = (duration - hours * 3600) / 60;

                                                                if(minutes > 2) {//동영상의 길이가 5분을 넘어가면
                                                                    Toast.makeText(getApplicationContext(),"2분 이하의 동영상만 업로드 가능합니다.", Toast.LENGTH_SHORT).show();
                                                                }else{
                                                                    Intent intent = new Intent(MainActivity.this, UploadVideoFirstActivity.class);
                                                                    intent.putExtra("uri", uri.toString());
                                                                    startActivity(intent);
                                                                }
                                                            }
                                                        });
                                            }
                                        });

                                        dialog.show();


                                    }

                                    @Override
                                    public void onPermissionDenied(List<String> deniedPermissions) {
                                        Log.d(TAG, "onPermissionDenied");
                                    }
                                })
                                .setDeniedMessage("권한을 승인하지 않으면 해당 기능은 사용할 수 없습니다.\n해당 기능을 사용하고 싶다면 설정에서 권한을 허용해주세요.")
                                .setPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
                                .check();
                    }
                });

                permissionThread.start();
                try {
                    permissionThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });


        //알림 탭뷰 추가
        tabHost.addTab(tabHost.newTabSpec("notification")
                .setIndicator(notificationView)
                .setContent(new Intent(this, NotificationActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));


        //마이 페이지 탭뷰 추가
        tabHost.addTab(tabHost.newTabSpec("mypage")
                .setIndicator(mypageView)
                .setContent(new Intent(this, MypageActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
        tabHost.setCurrentTab(3);
        tabHost.setCurrentTab(0);
        //브로드캐스트 리시버 등록
        registerReceiver();

        //푸시알림으로 인해서 앱이 최초 실행되어서 메인액티비티로 진입을 한 경우
        if(getIntent().getAction() != null){

            String action = getIntent().getAction();
            Log.d(TAG,"action- "+action);
            if (action.equals(INTENT_FILTER_ACTION)) {

                //shared에 저장된 세션 쿠키값을 불러온다.
                SharedPreferences sharedPreferences = getSharedPreferences("sessionCookie", MODE_PRIVATE);
                LoginActivity.account = sharedPreferences.getString("userAccount", null);
                LoginActivity.nickname = sharedPreferences.getString("userNickname", null);
                LoginActivity.profile = sharedPreferences.getString("userProfile", null);
                getIntent().setAction(INTENT_FILTER_ACTION);
                broadcastNotification(getIntent());
            }
        }

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    String jsonString = msg.obj.toString();
                    JSONObject faceChatRequestResult = new JSONObject(jsonString);
                    String result = faceChatRequestResult.getString("type");
                    if("successFaceChatRequest".equals(result)) {//영상통화 요청이 잘 전달된 경우
                        Intent intent = new Intent(MainActivity.this, FaceChatResponseWaitingActivity.class);
                        intent.putExtra("screenOn", true);//화면 켜짐 상태에서 call 액티비티 진입
                        intent.putExtra("roomName", faceChatRequestResult.getString("roomName"));//방 번호
                        intent.putExtra("receiverAccount", faceChatRequestResult.getString("receiverAccount"));//수신자 계정
                        intent.putExtra("receiverNickname", faceChatRequestResult.getString("receiverNickname"));//수신자 닉네임
                        intent.putExtra("receiverProfile", faceChatRequestResult.getString("receiverProfile"));//수신자 프로필
                        startActivity(intent);  //통화화면으로 이동
                    }else if ("failFaceChatRequest".equals(result)){//영상통화 요청이 전달되지 않은 경우
                        Toast.makeText(getApplicationContext(), "현재 상대방이 통화 불가능한 상태 입니다.", Toast.LENGTH_LONG).show();//통화 불가능 토스트
                    }else {//상대방이 통화중인 경우
                        Toast.makeText(getApplicationContext(), "상대방이 통화중 입니다.", Toast.LENGTH_LONG).show();//통화 불가능 토스트
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart 호출");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume 호출");


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause 호출");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop 호출");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart 호출");
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //게시물 업로드나 수정을 한 후에 호출되는 경우는 제외
        if (!PostActivity.isUploaded && !PostActivity.isEditted) {
            Log.d(TAG, "onNewIntent 호출");
            Log.d(TAG, "action- "+intent.getAction());
            intent.setAction(INTENT_FILTER_ACTION);
            broadcastNotification(intent);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("메인 화면 on Destroy", "호출");
        unregisterReceiver();
    }
    //브로드캐스트 리시버 동적 등록 메소드
    private void registerReceiver(){
        if(notificationReceiver != null) return;//이미 객체가 존재한다면 등록된 것이기 때문에 함수를 빠져나간다.
        //생성자의 param은 푸시알림이 오는 경우 알림 액티비티에 구현해야 할 onPushReceived 콜백 메소드의 인터페이스 리스너 객체
        notificationReceiver = new NotificationReceiver(NotificationActivity.notificationActivity);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_FILTER_ACTION);//인텐트 필터 등록
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(notificationReceiver, intentFilter);//리시버 등록
    };
    //브로드캐스트 리시버 해제 메소드
    private void unregisterReceiver(){
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(notificationReceiver);
        notificationReceiver = null;
    }

    //알림이 오면 브로드캐스트를 하고 다시 비활성화하는 메소드
    private void broadcastNotification(Intent intent) {
        Log.d(TAG, "broadcastNotification 호출");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);//인텐트로 broadcast
    }
}