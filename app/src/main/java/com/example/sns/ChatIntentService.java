package com.example.sns;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;

import androidx.core.app.NotificationCompat;

import static com.example.sns.LoginActivity.httpURLConnection;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ChatIntentService extends Service implements HttpRequest.OnHttpResponseListener {
    private final String TAG = "ChatIntentService";
    //클라이언트 소켓
    private Socket userSocket = null;
    //접속할 서버의 주소
    private String IP_ADDRESS = "13.124.105.47";
    //접근할 서버의 포트
    private int PORT = 8888;
    //서버로 데이터를 전송하는 스트림
    private DataOutputStream dataOutputStream;
    //서버에서 데이터를 받는 스트림
    private DataInputStream dataInputStream;
    //
    //소켓 서버에서 메세지를 받는 스레드
    private ReceiveMessage receiveMessage;

    public static Handler handler;

    private LoginUser loginUser;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("채팅 서비스 실행", "onStartCommand 호출");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //클라이언트 소켓을 만들어서 서버로 접근을 해준다.
                    Log.d(TAG, "서버 소켓 접속 시도");
                    userSocket = new Socket();

                    userSocket.connect(new InetSocketAddress(IP_ADDRESS, PORT));

                    Log.d(TAG, "서버 소켓 접근 성공");
                    //서버와의 데이터 송수신을 위한 스트림 객체 선언
                    dataOutputStream = new DataOutputStream(userSocket.getOutputStream());
                    //사용자의 정보를 소켓서버로 보내서 데이터 생성
                    SharedPreferences sharedPreferences = getSharedPreferences("loginUser", MODE_PRIVATE);
                    String account = sharedPreferences.getString("account", null);
                    String nickname = sharedPreferences.getString("nickname", null);
                    String profile = sharedPreferences.getString("profile", null);
                    if(LoginUser.getInstance() == null) {
                        LoginUser.initInstance(account, nickname, profile);
                    }
                    loginUser = LoginUser.getInstance();
                    //사용자 정보를 json객체로 생성
                    JSONObject jsonObject = new JSONObject();
                    try {

                        //소켓에서 어떤 작업을 처리할지 분기해주는 type
                        jsonObject.put("type", "createuser");
                        //소켓 서버로 접근하는 사용자 정보
                        jsonObject.put("account", account);
                        jsonObject.put("nickname", nickname);
                        jsonObject.put("profile", profile);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dataOutputStream.writeUTF(jsonObject.toString());
                    dataOutputStream.flush();

                    //메세지 수신 스레드 실행
                    receiveMessage = new ReceiveMessage(userSocket);
                    receiveMessage.start();


//                    GetChatRoomList getChatRoomList = new GetChatRoomList();
//                    getChatRoomList.execute(account);
                    getChatRoomList(account);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        //채팅 화면 액티비티에서 전송하는 메세지를 서비스에서 받기 위한 핸들러 작성
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Log.d(TAG, "메세지 전송 핸들러 호출");
                Log.d(TAG, "받은 메세지: " + msg.obj.toString());
                String token = msg.obj.toString();
                //소켓서버로 메세지를 전달해준다.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (dataOutputStream != null) {
                                dataOutputStream.writeUTF(token);
                                dataOutputStream.flush();
                                Log.d(TAG, "소켓서버로 메세지 전송 성공-" + token);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        };


        return START_STICKY;
    }

    //참여중인 채팅방 목록을 서버에 요청하는 메소드
    private void getChatRoomList(String account) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("account", account);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getchatroomlist.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //채팅 메세지를 확인하면 미확인자에서 자신의 계정을 지우는 요청을 서버에 하는 메소드
    private void updateCheckMessage(String id, String myAccount, JSONObject checkData) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("id", id);
            requestBody.put("account", myAccount);
            requestBody.put("checkData", checkData);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "updatecheckmessage.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //푸시알림을 보내기 위해 필요한 데이터를 서버에 요청하는 메소드
    private void getChatDataForNotification(int roomNum, String account, JSONObject messageData) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("roomNum", roomNum);
            requestBody.put("account", account);
            requestBody.put("messageData", messageData);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getchatdatafornotification.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onHttpResponse(String result) {
        Log.d(TAG, "통신 성공");
        Log.d(TAG, "서버에서 넘어온 json데이터- " + result);
        if (result != null) {
            try {
                JSONObject responseBody = new JSONObject(result);
                String requestType = responseBody.getString("requestType");
                if (requestType.equals("getChatRoomList")) {//소켓서버에 사용자 정보와 참여중인 채팅방 번호를 전달
                    JSONObject roomData = responseBody.getJSONObject("roomData");
                    //서버에서 전달된 json 스트링을 바로 소케 서버로 전달해준다.
                    SetUser setUser = new SetUser(dataOutputStream, roomData.toString());
                    setUser.start();
                } else if (requestType.equals("checkedMessage")) {
                    JSONObject checkData = responseBody.getJSONObject("checkData");
                    //서버에 채팅을 확인한 참가자를 업데이트했으면 소켓서버로 메세지를 전송해서 메세지를 보낸 사람에게 확인 사실을 알린다.
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                dataOutputStream.writeUTF(checkData.toString());
                                dataOutputStream.flush();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } else if (requestType.equals("getDataForNotification")) {//푸시알림을 위한 notification

                    //원래 데이터에 추가
                    JSONObject messageData = responseBody.getJSONObject("messageData");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            pushNotification(messageData);
                        }
                    }).start();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public class SetUser extends Thread {
        DataOutputStream dataOutputStream;
        String sendMessage;

        public SetUser(DataOutputStream dataOutputStream, String sendMessage) {
            this.dataOutputStream = dataOutputStream;
            this.sendMessage = sendMessage;
        }

        @Override
        public void run() {

            try {
                dataOutputStream.writeUTF(sendMessage);
                dataOutputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class ReceiveMessage extends Thread {

        Socket userSocket = null;

        public ReceiveMessage(Socket userSocket) {
            this.userSocket = userSocket;
            try {
                dataInputStream = new DataInputStream(this.userSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            while (dataInputStream != null) {
                try {
                    String jsonString = dataInputStream.readUTF();//json 스트링
                    //화면이 켜져있는 경우
                    if (isScreenOn(getApplicationContext())) {

                        //메세지가 도착한 당시 화면에 보이는 액티비티
                        String currentActivity = getActivity(getApplicationContext());
                        Log.d(TAG, "현재 액티비티: " + currentActivity);
                        Log.d(TAG, "서버에서 전달된 메세지: " + jsonString);
                        //현재 액티비티가 MainActivity인 경우
                        if (".MainActivity".equals(currentActivity)) {
                            JSONObject messageData = new JSONObject(jsonString);
                            String type = messageData.getString("type");//메세지 타입
                            //메세지 타입이 message, image, video, added, exit인 경우에만 메인 액티비티로 핸들러 메세지를 전송해줘서 메세지 수를 증가시킨다.
                            if (type.equals("message") || type.equals("image") || type.equals("video") || type.equals("added") || type.equals("exit")) {
                                Message message = PostActivity.handler.obtainMessage();
                                message.obj = "newMessage";
                                PostActivity.handler.sendMessage(message);
                            } else if ("requestFaceChat".equals(type)) {//영상통화 요청 메세지
                                //영상통화 수신 화면으로 전환
                                Intent intent = new Intent(getApplicationContext(), FaceChatReceiveActivity.class);
                                intent.putExtra("account", messageData.getString("account"));
                                intent.putExtra("nickname", messageData.getString("nickname"));
                                intent.putExtra("profile", messageData.getString("profile"));
                                intent.putExtra("roomName", messageData.getString("roomName"));
                                intent.putExtra("receiver", messageData.getString("receiver"));
                                intent.putExtra("screenOn", true);
                                startActivity(intent);
                            } else if ("successFaceChatRequest".equals(type) || "failFaceChatRequest".equals(type) || "isFaceChatting".equals(type)) {//영상 통화 요청의 성공 실패유무를 알려주는 메세지
                                Message message = MainActivity.handler.obtainMessage();
                                message.what = 7777;
                                message.obj = jsonString;
                                MainActivity.handler.sendMessage(message);
                            }
                        }
                        //현재 액티비티가 ChatActivity인 경우
                        else if (".ChatActivity".equals(currentActivity)) {
                            JSONObject messageData = new JSONObject(jsonString);
                            String type = messageData.getString("type");
                            if ("join".equals(type)) {

                            }
                            //채팅내용이 문자인 경우
                            else if ("message".equals(type)) {
                                Message message = ChatActivity.handler.obtainMessage();
                                message.what = 1111;
                                message.obj = jsonString;
                                ChatActivity.handler.sendMessage(message);
                            }
                            //채팅내용이 이미지인 경우
                            else if ("image".equals(type)) {
                                Message message = ChatActivity.handler.obtainMessage();
                                message.what = 2222;
                                message.obj = jsonString;
                                ChatActivity.handler.sendMessage(message);
                            }
                            //채팅 내용이 동영상인 경우
                            else if("video".equals(type)) {
                                Message message = ChatActivity.handler.obtainMessage();
                                message.what = 8888;
                                message.obj = jsonString;
                                ChatActivity.handler.sendMessage(message);
                            }
                            //특정 사용자가 채팅방에서 나갔다는 메세지인 경우
                            else if ("exit".equals(type)) {
                                Message message = ChatActivity.handler.obtainMessage();
                                message.what = 5555;
                                message.obj = jsonString;
                                ChatActivity.handler.sendMessage(message);
                            }
                            //특정 사용자(들)이 채팅방에 추가되었음을 알리는 메세지인 경우
                            else if ("added".equals(type)) {
                                Message message = ChatActivity.handler.obtainMessage();
                                message.what = 6666;
                                message.obj = jsonString;
                                ChatActivity.handler.sendMessage(message);
                            } else if ("requestFaceChat".equals(type)) {//영상통화 요청 메세지
                                //영상통화 수신 화면으로 전환
                                Intent intent = new Intent(getApplicationContext(), FaceChatReceiveActivity.class);
                                intent.putExtra("account", messageData.getString("account"));
                                intent.putExtra("nickname", messageData.getString("nickname"));
                                intent.putExtra("profile", messageData.getString("profile"));
                                intent.putExtra("roomName", messageData.getString("roomName"));
                                intent.putExtra("receiver", messageData.getString("receiver"));
                                intent.putExtra("screenOn", true);
                                startActivity(intent);
                            }
                        }
                        //현재 액티비티가 ChatRoomActivity인 경우
                        else if (".ChatRoomActivity".equals(currentActivity)) {
                            JSONObject messageData = new JSONObject(jsonString);
                            String type = messageData.getString("type");
                            if ("join".equals(type)) {

                            }
                            //채팅내용이 문자인 경우
                            else if ("message".equals(type)) {
                                Message message = ChatRoomActivity.handler.obtainMessage();
                                message.what = 1111;
                                message.obj = jsonString;
                                ChatRoomActivity.handler.sendMessage(message);
                                //채팅방에 있는 경우에는 상대방이 보낸 문자를 받자마자 확인하게 되기 때문에 상대방에게 확인했다는 것을 알려서
                                //상대방 메세지 옆에 확인하지 않은 사람의 수를 바로 줄여줘야 한다.
                                String id = messageData.getString("id");
                                String roomNum = messageData.getString("roomNum");
                                String myAccount = loginUser.getAccount();
                                //현재 채팅방 번호가 수신된 메세지의 채팅방 번호와 같을 때만 확인 처리
                                if (ChatRoomActivity.roomNum == Integer.parseInt(roomNum)) {
                                    //확인 메세지 데이터를 json스트링으로 만든다.
                                    JSONObject checkData = new JSONObject();
                                    checkData.put("type", "check");
                                    checkData.put("roomNum", Integer.parseInt(roomNum));
                                    checkData.put("sender", loginUser.getAccount());
                                    checkData.put("receiver", loginUser.getAccount());

                                    //서버에 채팅 내용을 확인했기때문에 chat 테이블의 unchecked_participant에 계정을 삭제해줘야 한다.
                                    updateCheckMessage(id, myAccount, checkData);
                                }

                            }
                            //채팅내용이 이미지인 경우
                            else if ("image".equals(type)) {
                                Message message = ChatRoomActivity.handler.obtainMessage();
                                message.what = 2222;
                                message.obj = jsonString;
                                ChatRoomActivity.handler.sendMessage(message);
                                //채팅방에 있는 경우에는 상대방이 보낸 문자를 받자마자 확인하게 되기 때문에 상대방에게 확인했다는 것을 알려서
                                //상대방 메세지 옆에 확인하지 않은 사람의 수를 바로 줄여줘야 한다.
                                String id = messageData.getString("id");
                                String roomNum = messageData.getString("roomNum");
                                String myAccount = loginUser.getAccount();
                                if (ChatRoomActivity.roomNum == Integer.parseInt(roomNum)) {
                                    //확인 메세지 데이터를 json스트링으로 만든다.
                                    JSONObject checkData = new JSONObject();
                                    checkData.put("type", "check");
                                    checkData.put("roomNum", Integer.parseInt(roomNum));
                                    checkData.put("sender", loginUser.getAccount());
                                    checkData.put("receiver", loginUser.getAccount());

                                    //서버에 채팅 내용을 확인했기때문에 chat 테이블의 unchecked_participant에 계정을 삭제해줘야 한다.
                                    updateCheckMessage(id, myAccount, checkData);
                                }
                            }
                            //채팅내용이 동영상인 경우
                            else if("video".equals(type)) {
                                Message message = ChatRoomActivity.handler.obtainMessage();
                                message.what = 8888;
                                message.obj = jsonString;
                                ChatRoomActivity.handler.sendMessage(message);
                                //채팅방에 있는 경우에는 상대방이 보낸 문자를 받자마자 확인하게 되기 때문에 상대방에게 확인했다는 것을 알려서
                                //상대방 메세지 옆에 확인하지 않은 사람의 수를 바로 줄여줘야 한다.
                                String id = messageData.getString("id");
                                String roomNum = messageData.getString("roomNum");
                                String myAccount = loginUser.getAccount();
                                if (ChatRoomActivity.roomNum == Integer.parseInt(roomNum)) {
                                    //확인 메세지 데이터를 json스트링으로 만든다.
                                    JSONObject checkData = new JSONObject();
                                    checkData.put("type", "check");
                                    checkData.put("roomNum", Integer.parseInt(roomNum));
                                    checkData.put("sender", loginUser.getAccount());
                                    checkData.put("receiver", loginUser.getAccount());

                                    //서버에 채팅 내용을 확인했기때문에 chat 테이블의 unchecked_participant에 계정을 삭제해줘야 한다.
                                    updateCheckMessage(id, myAccount, checkData);
                                }
                            }
                            //특정 사용자가 채팅 내용을 확인했다는 메세지
                            else if ("check".equals(type)) {
                                Message message = ChatRoomActivity.handler.obtainMessage();
                                message.what = 3333;
                                message.obj = jsonString;
                                ChatRoomActivity.handler.sendMessage(message);
                            }
                            //특정 사용자가 채팅방에 접근했음을 알리는 메세지
                            else if ("enter".equals(type)) {
                                Message message = ChatRoomActivity.handler.obtainMessage();
                                message.what = 4444;
                                message.obj = jsonString;
                                ChatRoomActivity.handler.sendMessage(message);
                            }
                            //특정 참여자가 채팅방을 나갔음을 알리는 메세지
                            else if ("exit".equals(type)) {
                                Message message = ChatRoomActivity.handler.obtainMessage();
                                message.what = 5555;
                                message.obj = jsonString;
                                ChatRoomActivity.handler.sendMessage(message);
                                String roomNum = messageData.getString("roomNum");
                                String account = messageData.getString("account");
                                String id = messageData.getString("id");
                                //현재 채팅방 번호가 수신된 메세지의 채팅방 번호와 같을 때만 확인 처리
                                if (ChatRoomActivity.roomNum == Integer.parseInt(roomNum)) {
                                    //확인 메세지 데이터를 json스트링으로 만든다.
                                    JSONObject checkData = new JSONObject();
                                    checkData.put("type", "check");
                                    checkData.put("roomNum", Integer.parseInt(roomNum));
                                    checkData.put("sender", account);
                                    checkData.put("receiver", loginUser.getAccount());
                                    //사용자가 나갔기 때문에 chat 테이블의 unchecked_participant에 계정을 삭제해줘야 한다.
                                    updateCheckMessage(id, account, checkData);
                                }
                            }
                            //특정 사용자(들)이 채팅방에 추가되었음을 알리는 메세지
                            else if ("added".equals(type)) {
                                Message message = ChatRoomActivity.handler.obtainMessage();
                                message.what = 6666;
                                message.obj = jsonString;
                                ChatRoomActivity.handler.sendMessage(message);
                                String roomNum = messageData.getString("roomNum");
                                String account = messageData.getString("account");
                                String id = messageData.getString("id");
                                //현재 채팅방 번호가 수신된 메세지의 채팅방 번호와 같을 때만 확인 처리
                                if (ChatRoomActivity.roomNum == Integer.parseInt(roomNum)) {
                                    //확인 메세지 데이터를 json스트링으로 만든다.
                                    JSONObject checkData = new JSONObject();
                                    checkData.put("type", "check");
                                    checkData.put("roomNum", Integer.parseInt(roomNum));
                                    checkData.put("sender", account);
                                    checkData.put("receiver", loginUser.getAccount());
                                    //사용자가 나갔기 때문에 chat 테이블의 unchecked_participant에 계정을 삭제해줘야 한다.
                                    updateCheckMessage(id, account, checkData);
                                }
                            } else if ("successFaceChatRequest".equals(type) || "failFaceChatRequest".equals(type) || "isFaceChatting".equals(type)) {//영상 통화 요청의 성공 실패유무를 알려주는 메세지
                                Message message = ChatRoomActivity.handler.obtainMessage();
                                message.what = 7777;
                                message.obj = jsonString;
                                ChatRoomActivity.handler.sendMessage(message);
                            } else if ("requestFaceChat".equals(type)) {//영상통화 요청 메세지
                                //영상통화 수신 화면으로 전환
                                Intent intent = new Intent(getApplicationContext(), FaceChatReceiveActivity.class);
                                intent.putExtra("account", messageData.getString("account"));
                                intent.putExtra("nickname", messageData.getString("nickname"));
                                intent.putExtra("profile", messageData.getString("profile"));
                                intent.putExtra("roomName", messageData.getString("roomName"));
                                intent.putExtra("receiver", messageData.getString("receiver"));
                                intent.putExtra("screenOn", true);
                                startActivity(intent);
                            }
                        }
                        //FaceChatResponseWaitingActivity에서 메세지를 받은 경우
                        else if (".FaceChatResponseWaitingActivity".equals(currentActivity)) {
                            JSONObject messageData = new JSONObject(jsonString);
                            String type = messageData.getString("type");//메세지 타입

                            if ("declineFaceChat".equals(type) || "acceptFaceChat".equals(type)) {
                                Message message = FaceChatResponseWaitingActivity.handler.obtainMessage();
                                message.obj = jsonString;
                                FaceChatResponseWaitingActivity.handler.sendMessage(message);
                            }
                        }
                        //FaceChatReceiveActivity에서 메세지를 받은 경우
                        else if (".FaceChatReceiveActivity".equals(currentActivity)) {
                            JSONObject messageData = new JSONObject(jsonString);
                            String type = messageData.getString("type");
                            if ("cancelFaceChat".equals(type)) {
                                Message message = FaceChatReceiveActivity.handler.obtainMessage();
                                message.obj = jsonString;
                                FaceChatReceiveActivity.handler.sendMessage(message);
                            }
                        }
                        //앱 밖에서 메세지를 받은 경우
                        else if (".activities.LauncherActivity".equals(currentActivity) || ".NexusLauncherActivity".equals(currentActivity)) {
                            JSONObject messageData = new JSONObject(jsonString);
                            String type = messageData.getString("type");//메세지 타입
                            if ("requestFaceChat".equals(type)) {//영상통화 요청인 경우
                                //영상통화 수신 화면으로 전환
                                Intent intent = new Intent(getApplicationContext(), FaceChatReceiveActivity.class);
                                intent.putExtra("account", messageData.getString("account"));
                                intent.putExtra("nickname", messageData.getString("nickname"));
                                intent.putExtra("profile", messageData.getString("profile"));
                                intent.putExtra("roomName", messageData.getString("roomName"));
                                intent.putExtra("receiver", messageData.getString("receiver"));
                                intent.putExtra("screenOn", true);
                                startActivity(intent);
                            } else {//채팅 수신인 경우
                                int roomNum = messageData.getInt("roomNum");
                                //메세지 타입이 message, image,video, added, exit인 경우에만 푸시알림을 보낸다.
                                if (type.equals("message") || type.equals("image") || type.equals("video") || type.equals("added") || type.equals("exit")) {
                                    //채팅방의 참여자 목록과 미확인 메세지 수를 서버로부터 가져와서 넘어온 데이터에 붙여주고 알림을 보내는 스레드
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            getChatDataForNotification(roomNum, loginUser.getAccount(), messageData);
                                        }
                                    }).start();
                                }
                            }
                        }

                    }
                    //화면이 꺼져있는 경우
                    else {
                        JSONObject messageData = new JSONObject(jsonString);
                        String type = messageData.getString("type");//메세지 타입
                        if ("requestFaceChat".equals(type)) {//영상통화 요청인 경우
                            //영상통화 수신 화면으로 전환
                            Intent intent = new Intent(getApplicationContext(), FaceChatReceiveActivity.class);
                            intent.putExtra("account", messageData.getString("account"));
                            intent.putExtra("nickname", messageData.getString("nickname"));
                            intent.putExtra("profile", messageData.getString("profile"));
                            intent.putExtra("roomName", messageData.getString("roomName"));
                            intent.putExtra("receiver", messageData.getString("receiver"));
                            intent.putExtra("screenOn", false);
                            startActivity(intent);
                        } else {//채팅 수신인 경우
                            int roomNum = messageData.getInt("roomNum");
                            //메세지 타입이 message, image, added, exit인 경우에만 푸시알림을 보낸다.
//                        if (type.equals("message") || type.equals("image") || type.equals("added") || type.equals("exit")) {
//                        }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    getChatDataForNotification(roomNum, loginUser.getAccount(), messageData);
                                }
                            }).start();
                        }
                    }
                    //스레드의 인터럽트를 위해서 슬립을 준다.
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                    //inputstream EOF에러가 발생하면 인터럽트를 시켜서 스레드를 종료시켜준다.
                    Thread.currentThread().interrupt();
                    //소켓연결을 해제하고 스트림도 모두 닫아준다.
                    try {
                        dataInputStream.close();
                        try {
                            JSONObject closeData = new JSONObject();
                            closeData.put("type", "close");
                            closeData.put("account", loginUser.getAccount());
                            closeData.put("nickname", loginUser.getNickname());
                            closeData.put("profile", loginUser.getProfile());
                            dataOutputStream.writeUTF(closeData.toString());
                            dataOutputStream.flush();
                            dataOutputStream.close();
                            userSocket.close();
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    private void pushNotification(JSONObject messageData) {
        //넘어온 메세지 데이터를 통해서 필요한 데이터 추출
        try {
            String type = messageData.getString("type");//메세지 타입
            int roomNum = messageData.getInt("roomNum");//채팅방 번호
            String message = messageData.getString("message");//메세지
            String nickname = messageData.getString("nickname");
            String profile = messageData.getString("profile");
            JSONArray participantList = messageData.getJSONArray("participantList");//참여자 리스트
            int newMessageCount = messageData.getInt("newMessageCount");

            Intent intent = new Intent(this, ChatRoomActivity.class);
            //푸시알림을 클릭하면 알림 액티비티가 바로 로드되는데 이때 알림의 카테고리, 알림을 촉발한 사용자 계정, 클릭 액션, 게시물 번호를 함께 넘겨준다.
            intent.putExtra("roomNum", roomNum);
            intent.putExtra("participantList", participantList.toString());
            intent.putExtra("newMessageCount", newMessageCount);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            //param은 ChatRoomActivity가 부모 액티비티라는 게 아니라 매니페스트에 설정된 ChatRoomActivity의 부모 액티비티 즉 ChatActivity가 설정된다.
            stackBuilder.addParentStack(ChatRoomActivity.class);
            stackBuilder.addNextIntent(intent);//ChatRoomActivity의 부모 액티비티를 스택에 쌓고 ChatRoomActivity로 이동
            //다른 애플리케이션에서 이 앱을 열 수 있도록, 즉 앱간의 이동이 가능하게 해주는 intent
            //세번째 인자로 들어간 intent는 외부 앱에서 이 앱을 열었을 때 이동할 액티비티로 이동이 되게끔 해준다. 위에서 MainActivity로 설정되어있으니
            //MainActivity로 이동이 가능해지는 것.
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            String channelId = "chat";

            NotificationCompat.Builder notificationBuilder;
            if ("message".equals(type)) {//텍스트 메세지인 경우
                notificationBuilder = new NotificationCompat.Builder(this, channelId)
                        //알림의 아이콘
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        //알림의 제목
                        .setContentTitle("SNS 채팅 메세지")
                        //알림의 내용
                        .setContentText(nickname + "님이 메세지를 보냈습니다: " + message)
                        //자동으로 알림 바 사라짐 설정
                        .setAutoCancel(true)
                        //알림음
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        //진동
                        .setVibrate(new long[]{1, 1000})
                        //프로필 이미지
                        .setLargeIcon(getBitmapFromUrl("http://13.124.105.47/profileimage/" + profile))
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentIntent(pendingIntent);
            } else {//이미지 메세지인 경우
                notificationBuilder = new NotificationCompat.Builder(this, channelId)
                        //알림의 아이콘
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        //알림의 제목
                        .setContentTitle("SNS 채팅 메세지")
                        //알림의 내용
                        .setContentText(message)
                        //자동으로 알림 바 사라짐 설정
                        .setAutoCancel(true)
                        //알림음
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        //진동
                        .setVibrate(new long[]{1, 1000})
                        //프로필 이미지
                        .setLargeIcon(getBitmapFromUrl("http://13.124.105.47/profileimage/" + profile))
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentIntent(pendingIntent);
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("chat", "chat", NotificationManager.IMPORTANCE_HIGH);
                channel.enableLights(true);
                channel.setLightColor(Color.GREEN);

                notificationManager.createNotificationChannel(channel);

            }

            //param1: 알림의 stack이라고 생각하면 된다. 이걸 상수로 주면 알림이 쌓이지 않고 계속해서 덮어쓰게 된다. 여기선 변수를 줘서 새로운 알림이 쌓이게 했다.
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //현재 액티비티 가져오기
    public String getActivity(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = activityManager.getRunningTasks(1);
        return runningTaskInfo.get(0).topActivity.getShortClassName();
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy 호출");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}