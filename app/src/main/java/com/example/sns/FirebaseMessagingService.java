package com.example.sns;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.RemoteMessage;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static com.example.sns.LoginActivity.httpURLConnection;
import static com.example.sns.MainActivity.iv_notificationDot;
import static com.example.sns.MainActivity.tabHost;
import static com.example.sns.NotificationActivity.notificationActivity;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static String TAG = "FirebaseMessagingService";
    private final String INTENT_FILTER_ACTION = "com.example.sns.SNS_Notification";
    //알림의 클릭시 이동할 프래그먼트
    String click_action;
    //알림 메세지에 담긴 게시물 번호
    int postNum = 0;
    //알림을 유발한 사용자 계정
    String userAccount;
    //알림의 카테고리
    String category;
    //알림 촉발자의 프로필 사진
    String profile;
    //알림의 대상이 되는 게시물의 대표 이미지
    String image;
    //알림 메세지
    String message;

    Bitmap profileBitmap;
    Bitmap imageBitmap;


    //알림이 도착했을 때 호출되는 메소드로 메세지로 들어오는 내용을 받으면 된다.
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("onMessageReceived", "호출");
        Log.d("알림 내용", String.valueOf(remoteMessage.getData()));
        SharedPreferences sharedPreferences = getSharedPreferences("fcmToken", MODE_PRIVATE);
        Log.d("저장된 토큰", sharedPreferences.getString("token", null));
        //현재 토큰을 조회하기 위한 인터페이스
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w("getInstanceID failed", task.getException());
                    return;
                }

                //현재 토큰 받기
                String currentToken = task.getResult().getToken();
                Log.d("현재 토큰", currentToken);
            }
        });
        category = remoteMessage.getData().get("category");//알림의 카테고리
        userAccount = remoteMessage.getData().get("userAccount");//알림송신자 사용자 계정
        click_action = remoteMessage.getData().get("click_action");//넘어온 푸시 메세지를 클릭했을 때 실행될 프래그먼트
        //넘어온 푸시 메세지를 클랙했을 때 보여줄 게시물의 번호
        if (remoteMessage.getData().get("postNum") != null) {
            postNum = Integer.parseInt(remoteMessage.getData().get("postNum"));
        }
        //알림을 보낸 사람의 프로필 사진
        profile = remoteMessage.getData().get("profile");
        profileBitmap = getBitmapFromUrl("http://13.124.105.47/profileimage/" + profile);
        //알림의 대상이 되는 게시물의 이미지가 함께 넘어온 경우(팔로우 알림은 게시물 관련 알림이 아니기 때문에 이미지가 함께 넘어오지 않는다.)
        if (remoteMessage.getData().get("image") != null) {
            image = remoteMessage.getData().get("image");
            imageBitmap = getBitmapFromUrl("http://13.124.105.47/uploadimage/" + image);
        }
        message = remoteMessage.getData().get("body");

        Intent intent = new Intent();//브로드캐스트 리시버 클래스로 전달해줄 인텐트(param:인텐트 필터)
        intent.putExtra("userAccount", userAccount);//알림 송신자의 계정
        intent.putExtra("profile", profile);//알림 송신자의 프로필 사진
        intent.putExtra("category", category);//알림 카테고리(인텐트 필더)
        intent.putExtra("click_action", click_action);
        intent.putExtra("message", message);
        if (remoteMessage.getData().get("image") != null) {
            intent.putExtra("image", image);//게시물 이미지
        }
        if (remoteMessage.getData().get("postNum") != null) {
            intent.putExtra("postNum", postNum);//게시물 번호
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(INTENT_FILTER_ACTION);

        //현재 앱의 액티비티가 보이는 상태인지 아닌지를 판별하는 boolean
        boolean isRunning = isAppRunning(getApplicationContext());
        //현재 단말기의 스크린 on/off상태 판별 boolean
        boolean isScreenOn = isScreenOn(getApplicationContext());

        //단말기가 screen off상태인 경우
        if (!isScreenOn) {
            //푸시 알림을 실행한다.
            sendNotification(intent);
        }
        //단말기가 screen on상태인 경우
        else {
            Log.d("스크린 on/off", "on");
            //현재 앱의 액티비티가 보이는 경우
            if (isRunning) {
                Log.d("SNS어플 실행", "true");

                //현재 액티비티가 알림액티비티인 경우
                if (tabHost.getCurrentTabTag().equals("notification")) {
                    //처음에는 NotificationActivity의 onResume을 메인 스레드에서 호출했는데 에러가 떴음
                    //그래서 핸들러로 호출하니까 정상적으로 호출됐음
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //알림 액티비티의 생명주기 중 onResume을 호출해서 알림리스트를 서버에서 새롭게 가져오도록 한다.
                            notificationActivity.getNotificationData("getNotification", LoginActivity.account, 0, notificationActivity.listSize);//알림 목록 데이터 가져오기;
                        }
                    });

                }
                //현재 액티비티가 알림 액티비티가 아닌 경우
                else {
                    //fcm메세지가 온 경우 알림 탭 위에 알림표시 점을 표시해준다.
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            iv_notificationDot.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
            //현재 화면에서 앱의 액티비티가 보이지 않는 경우
            else {
                Log.d("SNS어플 실행", "false");
                //푸시 알림을 실행한다.
                sendNotification(intent);

            }
        }
    }

    //어플을 설치할 때 토큰(단말기 식별자)이 자동으로 생성된다.
    @Override
    public void onNewToken(String token) {
        Log.d("새로운 토큰", token);
        //새롭게 생성된 토큰을 로컬에 저장해준다.
        saveToken(token);
    }

    private void sendNotification(Intent intent) {

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        //다른 애플리케이션에서 이 앱을 열 수 있도록, 즉 앱간의 이동이 가능하게 해주는 intent
        //세번째 인자로 들어간 intent는 외부 앱에서 이 앱을 열었을 때 이동할 액티비티로 이동이 되게끔 해준다. 위에서 MainActivity로 설정되어있으니
        //MainActivity로 이동이 가능해지는 것.
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String channelId = getString(R.string.default_notification_channel_id);

        NotificationCompat.Builder notificationBuilder;
        //팔로우 알림이라 게시물 이미지가 없는 경우
        if (category.equals("follow")) {
            notificationBuilder = new NotificationCompat.Builder(this, channelId)
                    //알림의 아이콘
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    //알림의 제목
                    .setContentTitle("SNS")
                    //알림의 내용
                    .setContentText(message)
                    //자동으로 알림 바 사라짐 설정
                    .setAutoCancel(true)
                    //알림음
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    //진동
                    .setVibrate(new long[]{1, 1000})
                    //프로필 이미지
                    .setLargeIcon(profileBitmap)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentIntent(pendingIntent);
        }
        //게시물 이미지가 있는 경우
        else {
            notificationBuilder = new NotificationCompat.Builder(this, channelId)
                    //알림의 아이콘
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    //알림의 제목
                    .setContentTitle("SNS")
                    //알림의 내용
                    .setContentText(message)
                    //자동으로 알림 바 사라짐 설정
                    .setAutoCancel(true)
                    //알림음
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    //진동
                    .setVibrate(new long[]{1, 1000})
                    //프로필 이미지
                    .setLargeIcon(profileBitmap)
                    //게시물 이미지
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(imageBitmap))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentIntent(pendingIntent);
        }


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String channelName = getString(R.string.default_notification_channel_id);

            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);

            notificationManager.createNotificationChannel(channel);

        }

        //param1: 알림의 stack이라고 생각하면 된다. 이걸 상수로 주면 알림이 쌓이지 않고 계속해서 덮어쓰게 된다. 여기선 변수를 줘서 새로운 알림이 쌓이게 했다.
        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }

    //처음 토큰이 발행되면 토큰을 로컬에 저장해준다.
    private void saveToken(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences("fcmToken", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    //현재 이 앱이 화면에 보이는지 여부를 판별해주는 메소드
    //현재 화면에 이 앱이 보이면 푸시 알림은 뜨지 않고 메세지만 받는다.
    private boolean isAppRunning(Context context) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }

        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }

        return false;
    }

    //현재 단말기의 스크린 on/off상태를 판별해주는 메소드
    private boolean isScreenOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.isScreenOn();
    }

    //서버의 이미지를 비트맵으로 가져오는 메소드
    private Bitmap getBitmapFromUrl(String imageUrl) {

        try {
            URL url = new URL(imageUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(0);
            httpURLConnection.setConnectTimeout(0);
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);

            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
