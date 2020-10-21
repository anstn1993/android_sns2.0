package com.example.sns.chat.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.sns.*;
import com.example.sns.chat.adapter.ChatRoomAdapter;
import com.example.sns.chat.service.ChatService;
import com.example.sns.chat.model.ChatRoomItem;
import com.example.sns.chat.model.ChatUser;
import com.example.sns.login.model.LoginUser;
import com.example.sns.util.HttpRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class ChatActivity extends AppCompatActivity implements ChatRoomAdapter.ChatRoomRecyclerViewListener, HttpRequest.OnHttpResponseListener {

    private final String TAG = "ChatActivity";

    private ImageButton btn_createChatRoom, btn_cancel;

    private RecyclerView rv_chat;
    private LinearLayoutManager linearLayoutManager;
    private ChatRoomAdapter chatRoomAdapter;
    private ArrayList<ChatRoomItem> chatRoomItemArrayList;

    //채팅 메세지를 받을 핸들러
    public static Handler handler;

    private boolean isFromChatRoom = false;//채팅방 안에서 특정 사용자와 1:1채팅을 하기 위해서 chatActivity로 접근하는 경우 true

    private LoginUser loginUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Log.d(TAG, "onCreate 호출");

        loginUser = LoginUser.getInstance();

        btn_createChatRoom = findViewById(R.id.imagebutton_createchatroom);
        btn_cancel = findViewById(R.id.btn_cancel);


        btn_createChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateChatRoomActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
        //리사이클러뷰 셋
        setRecyclerView();


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //메세지가 문자인 경우
                if (msg.what == 1111) {
                    String messageData = msg.obj.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(messageData);

                        int roomNum = jsonObject.getInt("roomNum");
                        //넘어온 메세지가 기존에 존재하는 채팅방의 메세지인 경우인지 아닌지를 파악하기 위한 불린 변수
                        boolean isRoomExist = false;
                        //채팅방 리사이클러뷰에서 메세지를 수신한 채팅방의 index를 알아내기 위한 변수
                        int position = 0;
                        //새롭게 채팅방을 만든 사람이 최초로 보낸 메세지
                        String message = jsonObject.getString("message");
                        //메세지 타입
                        String type = jsonObject.getString("type");
                        //메세지를 보낸 시간
                        String time = jsonObject.getString("time");
                        //반복문을 통해서 메세지가 기존에 존재하는 채팅방인지 파악
                        for (int i = 0; i < chatRoomItemArrayList.size(); i++) {
                            if (chatRoomItemArrayList.get(i).getRoomNum() == roomNum) {
                                //기존에 존재하는 방이면 true로 변환 후 반복문 탈출
                                isRoomExist = true;
                                position = i;
                                break;
                            }
                        }
                        //메세지가 기존에 존재하는 채팅방의 메세지인 경우
                        if (isRoomExist) {
                            //기존 채팅방 아이템 리스트를 수정해준다.
                            updateChatRoom(position, message, type, time);
                        }
                        //새로운 채팅방의 메세지인 경우
                        else {
                            //새로운 채팅방 생성
                            createNewChatRoom(loginUser.getAccount(), roomNum, message, time);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //메세지가 이미지인 경우
                else if (msg.what == 2222) {
                    String messageData = msg.obj.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(messageData);
                        int roomNum = jsonObject.getInt("roomNum");
                        //넘어온 메세지가 기존에 존재하는 채팅방의 메세지인 경우인지 아닌지를 파악하기 위한 불린 변수
                        boolean isRoomExist = false;
                        //채팅방 리사이클러뷰에서 메세지를 수신한 채팅방의 index를 알아내기 위한 변수
                        int position = 0;
                        //새롭게 채팅방을 만든 사람이 최초로 보낸 메세지
                        String message = jsonObject.getString("message");
                        //메세지 타입
                        String type = jsonObject.getString("type");
                        //메세지를 보낸 시간
                        String time = jsonObject.getString("time");
                        //반복문을 통해서 메세지가 기존에 존재하는 채팅방인지 파악
                        for (int i = 0; i < chatRoomItemArrayList.size(); i++) {
                            if (chatRoomItemArrayList.get(i).getRoomNum() == roomNum) {
                                //기존에 존재하는 방이면 true로 변환 후 반복문 탈출
                                isRoomExist = true;
                                position = i;
                                break;
                            }
                        }
                        //메세지가 기존에 존재하는 채팅방의 메세지인 경우
                        if (isRoomExist) {
                            //기존 채팅방 아이템 리스트를 수정해준다.
                            updateChatRoom(position, message, type, time);

                        }
                        //새로운 채팅방의 메세지인 경우
                        else {
                            //새로운 채팅방 생성
                            createNewChatRoom(loginUser.getAccount(), roomNum, message, time);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //특정 사용자가 채팅방을 나갔다는 메세지인 경우
                else if (msg.what == 5555) {
                    String messageData = msg.obj.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(messageData);
                        int roomNum = jsonObject.getInt("roomNum");
                        String account = jsonObject.getString("account");//나간 사용자의 계정
                        //채팅방 리사이클러뷰에서 메세지를 수신한 채팅방의 index를 알아내기 위한 변수
                        int position = 0;
                        //새롭게 채팅방을 만든 사람이 최초로 보낸 메세지
                        String message = jsonObject.getString("message");
                        //메세지 타입
                        String type = jsonObject.getString("type");
                        //메세지를 보낸 시간
                        String time = jsonObject.getString("time");
                        //반복문을 통해서 메세지가 기존에 존재하는 채팅방인지 파악
                        for (int i = 0; i < chatRoomItemArrayList.size(); i++) {
                            if (chatRoomItemArrayList.get(i).getRoomNum() == roomNum) {
                                position = i;
                                break;
                            }
                        }

                        //기존 채팅방 아이템 리스트를 수정해준다.
                        updateChatRoom(position, message, type, time, account);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //특정 사용자를 채팅방에 초대했다는 메세지인 경우
                else if (msg.what == 6666) {
                    String messageData = msg.obj.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(messageData);
                        JSONArray addedParticipantList = jsonObject.getJSONArray("addedParticipantList");//초대된 사용자 계정 리스트
                        int roomNum = jsonObject.getInt("roomNum");
                        //채팅방 리사이클러뷰에서 메세지를 수신한 채팅방의 index를 알아내기 위한 변수
                        int position = 0;
                        //새롭게 채팅방을 만든 사람이 최초로 보낸 메세지
                        String message = jsonObject.getString("message");
                        //메세지 타입
                        String type = jsonObject.getString("type");
                        //메세지를 보낸 시간
                        String time = jsonObject.getString("time");
                        //반복문을 통해서 메세지가 기존에 존재하는 채팅방인지 파악
                        for (int i = 0; i < chatRoomItemArrayList.size(); i++) {
                            if (chatRoomItemArrayList.get(i).getRoomNum() == roomNum) {
                                position = i;
                                break;
                            }
                        }

                        //기존 채팅방 아이템 리스트를 수정해준다.
                        updateChatRoom(position, message, type, time, addedParticipantList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //메세지가 동영상인 경우
                else if (msg.what == 8888) {
                    String messageData = msg.obj.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(messageData);
                        int roomNum = jsonObject.getInt("roomNum");
                        //넘어온 메세지가 기존에 존재하는 채팅방의 메세지인 경우인지 아닌지를 파악하기 위한 불린 변수
                        boolean isRoomExist = false;
                        //채팅방 리사이클러뷰에서 메세지를 수신한 채팅방의 index를 알아내기 위한 변수
                        int position = 0;
                        //새롭게 채팅방을 만든 사람이 최초로 보낸 메세지
                        String message = jsonObject.getString("message");
                        //메세지 타입
                        String type = jsonObject.getString("type");
                        //메세지를 보낸 시간
                        String time = jsonObject.getString("time");
                        //반복문을 통해서 메세지가 기존에 존재하는 채팅방인지 파악
                        for (int i = 0; i < chatRoomItemArrayList.size(); i++) {
                            if (chatRoomItemArrayList.get(i).getRoomNum() == roomNum) {
                                //기존에 존재하는 방이면 true로 변환 후 반복문 탈출
                                isRoomExist = true;
                                position = i;
                                break;
                            }
                        }
                        //메세지가 기존에 존재하는 채팅방의 메세지인 경우
                        if (isRoomExist) {
                            //기존 채팅방 아이템 리스트를 수정해준다.
                            updateChatRoom(position, message, type, time);

                        }
                        //새로운 채팅방의 메세지인 경우
                        else {
                            //새로운 채팅방 생성
                            createNewChatRoom(loginUser.getAccount(), roomNum, message, time);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
    }

    private void setRecyclerView() {
        chatRoomItemArrayList = new ArrayList<>();

        rv_chat = findViewById(R.id.recyclerview_chatroom);
        rv_chat.setHasFixedSize(true);
        //레이아웃메이저 설정
        linearLayoutManager = new LinearLayoutManager(this);
        rv_chat.setLayoutManager(linearLayoutManager);
        //어댑터 설정
        chatRoomAdapter = new ChatRoomAdapter(chatRoomItemArrayList, this);
        chatRoomAdapter.setOnClickListener(this);

        rv_chat.setAdapter(chatRoomAdapter);
    }

    //새로 만들어진 방에서 최초의 메세지를 받는 경우 새로운 채팅방을 생성하도록 요청하는 메소드
    private void createNewChatRoom(String account, int roomNum, String message, String time) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("account", account);//계정
            requestBody.put("roomNum", roomNum);//방 번호
            requestBody.put("message", message);//메세지
            requestBody.put("time", time);//메세지 시간
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getnewchatroomuserlist.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //채팅방 목록을 서버에 요청하는 메소드
    private void getChatRoom(String account) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("account", account);//사용자 계정
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getchatroomonui.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //채팅방을 새롭게 생성할 때 서버에 채팅방을 생성하도록 요청하는 메소드
    private void addChatRoom(String participantString, String account) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("participantString", participantString);
            requestBody.put("account", account);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "addchatroom.php", this);
            httpRequest.execute();

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    //기존 채팅방에 메세지가 오면 해당 채팅방을 새롭게 갱신하는 메소드
    private void updateChatRoom(int position, String message, String type, String time) {
        //읽지 않은 메세지 수 +1
        chatRoomItemArrayList.get(position).setNewMessageCount(chatRoomItemArrayList.get(position).getNewMessageCount() + 1);
        //새로운 메세지 수신 시간 설정
        chatRoomItemArrayList.get(position).setTime(time);
        //새로운 메세지 설정
        chatRoomItemArrayList.get(position).setMessage(message);
        //메세지 타입 설정
        chatRoomItemArrayList.get(position).setType(type);
        //데이터 셋 변경 notify
        chatRoomAdapter.notifyItemChanged(position, "updateNewMessage");
        //채팅방의 최상단으로 이동
        Collections.swap(chatRoomItemArrayList, position, 0);
        chatRoomAdapter.notifyItemMoved(position, 0);
    }

    //특정 채팅방에서 사용자가 나갔다는 메세지를 받았을 때 그 채팅방을 새롭게 갱신해줄 메소드
    private void updateChatRoom(int position, String message, String type, String time, String account) {
        //나간 사용자의 데이터를 삭제.
        for (int i = 0; i < chatRoomItemArrayList.get(position).getUserList().size(); i++) {
            if (account.equals(chatRoomItemArrayList.get(position).getUserList().get(i).getAccount())) {
                chatRoomItemArrayList.get(position).getUserList().remove(i);
            }
        }
        //채팅방 참여 인원에 따라 뷰홀더 클래스를 다르게 할당하고 있기 때문에 notify를 해주기 전에 뷰홀더 클래스를 교체해줘야 한다.
        chatRoomAdapter.onCreateViewHolder(rv_chat, chatRoomAdapter.getItemViewType(position));
        //읽지 않은 메세지 수 +1
        chatRoomItemArrayList.get(position).setNewMessageCount(chatRoomItemArrayList.get(position).getNewMessageCount() + 1);
        //새로운 메세지 수신 시간 설정
        chatRoomItemArrayList.get(position).setTime(time);
        //새로운 메세지 설정
        chatRoomItemArrayList.get(position).setMessage(message);
        //메세지 타입 설정
        chatRoomItemArrayList.get(position).setType(type);
        //데이터 셋 변경 notify
        chatRoomAdapter.notifyItemChanged(position, "userChanged");
        //채팅방의 최상단으로 이동
        Collections.swap(chatRoomItemArrayList, position, 0);
        chatRoomAdapter.notifyItemMoved(position, 0);
    }

    //특정 채팅방에서 사용자가 입장했다는 메세지가 생겼을 때 그 채팅방을 새롭게 갱신해줄 메소드
    private void updateChatRoom(int position, String message, String type, String time, JSONArray addedParticipantList) {
        //새롭게 추가된 사용자들의 데이터를 채팅방 리스트의 사용자 리스트에 추가해준다.
        for (int i = 0; i < addedParticipantList.length(); i++) {
            try {
                JSONObject userData = addedParticipantList.getJSONObject(i);
                String account = userData.getString("account");
                String nickname = userData.getString("nickname");
                String profile = userData.getString("profile");
                chatRoomItemArrayList.get(position).getUserList().add(new ChatUser(account, nickname, profile));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //채팅방 참여 인원에 따라 뷰홀더 클래스를 다르게 할당하고 있기 때문에 notify를 해주기 전에 뷰홀더 클래스를 교체해줘야 한다.
        chatRoomAdapter.onCreateViewHolder(rv_chat, chatRoomAdapter.getItemViewType(position));

        //읽지 않은 메세지 수 +1
        chatRoomItemArrayList.get(position).setNewMessageCount(chatRoomItemArrayList.get(position).getNewMessageCount() + 1);
        //새로운 메세지 수신 시간 설정
        chatRoomItemArrayList.get(position).setTime(time);
        //새로운 메세지 설정
        chatRoomItemArrayList.get(position).setMessage(message);
        //메세지 타입 설정
        chatRoomItemArrayList.get(position).setType(type);
        //데이터 셋 변경 notify
        chatRoomAdapter.notifyItemChanged(position, "userChanged");
        //채팅방의 최상단으로 이동
        Collections.swap(chatRoomItemArrayList, position, 0);
        chatRoomAdapter.notifyItemMoved(position, 0);
    }

    //채팅방을 나가는 로직을 구현한 메소드
    //param: 채팅방 리사이클러뷰의 채팅방 목록 index
    private void exitChatRoom(int position) {
        int roomNum = chatRoomItemArrayList.get(position).getRoomNum();//나가는 채팅방 번호
        String account = loginUser.getAccount();//사용자 계정
        String nickname = loginUser.getNickname();//사용자 닉네임
        String profile = loginUser.getProfile();//사용자 프로필

        //소켓에 전달할 데이터 json
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("type", "exit");
            requestBody.put("roomNum", roomNum);
            requestBody.put("account", account);
            requestBody.put("nickname", nickname);
            requestBody.put("profile", profile);
            requestBody.put("message", nickname + "님이 채팅방을 나가셨습니다.");
            requestBody.put("position", position);//리사이클러뷰 상에서 나간 채팅방의 index
            //chat테이블에 저장할 수신자 목록을 jsonArray에 넣어준다.
            JSONArray receiverList = new JSONArray();

            for (int i = 0; i < chatRoomItemArrayList.get(position).getUserList().size(); i++) {
                //수신자 닉네임
                String receiverAccount = chatRoomItemArrayList.get(position).getUserList().get(i).getAccount();
                receiverList.put(receiverAccount);
            }
            requestBody.put("receiverList", receiverList);
            Log.d("채팅방 나가기 json 데이터", requestBody.toString());
            //서버 db에 채팅방 참여자의 변화를 update
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "exitchatroom.php", this);
            httpRequest.execute();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }//end of exitChatRoom


    //서버와 통신에 성공했을 때 호출되는 콜백 메소드(HttpRequest클래스(AsyncTask클래스 상속)의 onPostExecute()메소드에서 호출)
    //모든 통신에 대한 결과는 이 콜백 메소드에서 처리해준다.
    @Override
    public void onHttpResponse(String result) {
        Log.d(TAG, "통신 성공");
        Log.d(TAG, "서버에서 넘어온 json데이터- " + result);
        if (result != null) {
            try {
                JSONObject responseBody = new JSONObject(result);
                String requestType = responseBody.getString("requestType");
                if (requestType.equals("getChatRoom")) {//채팅방 목록을 가져오는 통신
                    chatRoomItemArrayList.clear();//채팅방 아이템을 비워준다.
                    //chatroomlist키값에 들어있는 배열을 jsonArray에 넣어준다.
                    JSONArray jsonArray = responseBody.getJSONArray("chatroomList");
                    //댓글 수만큼 반복문을 돌리면서 각 댓글에 데이터를 넣어준다.
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);

                        //채팅방 리스트아이템 객체에 데이터를 셋하고
                        ChatRoomItem chatRoomItem = new Gson().fromJson(data.toString(), ChatRoomItem.class);

                        //모든 아이템 셋이 끝나면 리사이클러뷰 어레이리스트에 넣어준다.
                        chatRoomItemArrayList.add(chatRoomItem);
                    }
                    //어댑터에 notify
                    chatRoomAdapter.notifyDataSetChanged();

                    //채팅방에서 1:1채팅을 하기 위해서 ChatActivity로 돌아온 경우 바로 ChatRoomActivity로 이동하는 로직 실행
                    if (isFromChatRoom) {
                        String selectedUserAccount = getIntent().getStringExtra("selectedUserAccount");
                        Log.d(TAG, "isFromChatRoom: true");
                        isFromChatRoom = false;
                        getIntent().putExtra("isFromChatRoom", false);
                        for (int i = 0; i < chatRoomItemArrayList.size(); i++) {
                            //존재하는 채팅방이 1:1 채팅방인 경우
                            if (chatRoomItemArrayList.get(i).getUserList().size() == 1) {
                                //기존에 존재하는 1:1채팅방의 상대방 계정
                                String account = chatRoomItemArrayList.get(i).getUserList().get(0).getAccount();
                                //내가 선택한 사용자와의 채팅방이 이미 존재하는 경우 그 방으로 이동시킨다.
                                if (selectedUserAccount.equals(account)) {
                                    Intent intent = new Intent(this, ChatRoomActivity.class);
                                    intent.putExtra("roomNum", chatRoomItemArrayList.get(i).getRoomNum());
                                    //대화 상대의 정보를 json스트링으로 만들어서 intent
                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("account", account);
                                        jsonObject.put("nickname", chatRoomItemArrayList.get(i).getUserList().get(0).getNickname());
                                        jsonObject.put("profile", chatRoomItemArrayList.get(i).getUserList().get(0).getProfile());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    JSONArray participantData = new JSONArray();
                                    participantData.put(jsonObject);
                                    intent.putExtra("participantList", participantData.toString());
                                    Log.d("참여자 리스트", participantData.toString());

                                    //미확인 메세지 개수
                                    int newMessageCount = 0;
                                    for (int j = 0; j < chatRoomItemArrayList.size(); j++) {
                                        if (chatRoomItemArrayList.get(j).getNewMessageCount() != 0) {
                                            newMessageCount += chatRoomItemArrayList.get(j).getNewMessageCount();
                                        }
                                    }
                                    //미확인 메세지가 있는 방을 들어가는 경우에는 그 방의 미확인 메세지 수만큼 빼서 넘겨준다.
                                    if (chatRoomItemArrayList.get(i).getNewMessageCount() != 0) {
                                        newMessageCount -= chatRoomItemArrayList.get(i).getNewMessageCount();
                                        intent.putExtra("newMessageCount", newMessageCount);
                                    }
                                    //그렇지 않은 경우에는 그냥 넘겨준다.
                                    else {
                                        intent.putExtra("newMessageCount", newMessageCount);
                                    }

                                    startActivity(intent);
                                    return;
                                }
                            }


                        }
                        String participantString = loginUser.getAccount() + "/" + selectedUserAccount;
                        addChatRoom(participantString, loginUser.getAccount());
                    }

                } else if (requestType.equals("createNewChatRoom")) {//다른 사람이 만든 채팅방에서 최초의 메세지가 날아올때 채팅방이 생기도록 하는 통신
                    JSONObject data = responseBody.getJSONObject("chatRoomData");
                    //채팅방 리스트아이템 객체에 데이터를 셋하고
                    ChatRoomItem chatRoomItem = new Gson().fromJson(data.toString(), ChatRoomItem.class);
                    //모든 아이템 셋이 끝나면 리사이클러뷰 어레이리스트의 제일 처음에 넣어준다.
                    chatRoomItemArrayList.add(0, chatRoomItem);
                    //어댑터에 notify
                    chatRoomAdapter.notifyItemInserted(0);
                } else if (requestType.equals("addChatRoom")) {//자신이 채팅방을 새로 만드는 통신
                    JSONObject data = responseBody.getJSONObject("addedChatRoom");

                    //채팅방 리스트아이템 객체에 데이터를 셋하고
                    ChatRoomItem chatRoomItem = new Gson().fromJson(data.toString(), ChatRoomItem.class);

                    //소켓 서버로 전달해줄 json 데이터를 생성한다.
                    JSONObject createdRoomData = new JSONObject();//전체 데이터를 담을 json 객체
                    createdRoomData.put("type", "addroom");
                    createdRoomData.put("roomNum", chatRoomItem.getRoomNum());
                    //채팅방 참여자의 계정, 닉네임, 프로필 데이터를 담는 json 객체를 담을 json배열
                    JSONArray participantList = new JSONArray();

                    for (int j = 0; j < chatRoomItem.getUserList().size(); j++) {
                        //참여자의 계정, 닉네임, 프로필 데이터를 담는 json
                        JSONObject userDataJSON = new JSONObject();
                        userDataJSON.put("account", chatRoomItem.getUserList().get(j).getAccount());
                        userDataJSON.put("nickname", chatRoomItem.getUserList().get(j).getNickname());
                        userDataJSON.put("profile", chatRoomItem.getUserList().get(j).getProfile());
                        //사용자 정보를 담는 json을 json배열에 넣어준다.
                        participantList.put(userDataJSON);
                    }
                    //마지막에는 자신의 정보를 추가로 넣어준다.
                    JSONObject userDataJSON = new JSONObject();
                    userDataJSON.put("account", loginUser.getAccount());
                    userDataJSON.put("nickname", loginUser.getNickname());
                    userDataJSON.put("profile", loginUser.getProfile());
                    participantList.put(userDataJSON);
                    //사용자 정보들을 담는 json배열을 채팅방의 전체 데이터를 담는 json객체에 넣어준다.
                    createdRoomData.put("participantList", participantList);
                    //ex) {"type":"addroom","roomNum":162,"participantList":[{"account":"gggg","nickname":"진처리","profile":"gggg021103.jpg"},{"account":"rlarpdlcm","nickname":"정후이","profile":"rlarpdlcm025328.jpg"},{"account":"rangkim","nickname":"뢩킴","profile":"rangkim020850.jpg"}]}
                    Log.d("생성된 채팅방 데이터", createdRoomData.toString());
                    //새롭게 추가된 사용자들을 소켓 서버에 등록시키기 위해서 채팅 서비스로 핸들러 메세지를 전달한다.
                    NotifyRoomAddedToChatServer notifyRoomAddedToChatServer = new NotifyRoomAddedToChatServer(createdRoomData.toString());
                    notifyRoomAddedToChatServer.start();
                    //모든 아이템 셋이 끝나면 리사이클러뷰 어레이리스트에 넣어준다.
                    chatRoomItemArrayList.add(0, chatRoomItem);
                    //어댑터에 알리고
                    chatRoomAdapter.notifyItemInserted(0);
                    //생성된 채팅방 액티비티로 넘겨준다.
                    Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                    intent.putExtra("roomNum", chatRoomItem.getRoomNum());//채팅방 번호
                    intent.putExtra("isNewRoom", true);//새롭게 생성된 채팅방 여부
                    //자기 자신의 데이터는 다시 지워주고 인텐트
                    participantList.remove(participantList.length() - 1);
                    // ex) [{"account":"gggg","nickname":"진처리","profile":"gggg021103.jpg"},{"account":"rlarpdlcm","nickname":"정후이","profile":"rlarpdlcm025328.jpg"}]
                    Log.d("참여자 목록", participantList.toString());
                    intent.putExtra("participantList", participantList.toString());
                    startActivity(intent);
                } else if (requestType.equals("exitChatRoom")) {//채팅방을 나가는 통신
                    int exitType = responseBody.getInt("exitType");//채팅방에서 마지막으로 나가는지 아닌지를 분별하기 위한 변수
                    int position = responseBody.getInt("position");//리사이클러뷰 index
                    //채팅방 목록에서 지워준다.
                    chatRoomItemArrayList.remove(position);
                    chatRoomAdapter.notifyItemRemoved(position);
                    if (exitType == 0) {//채팅방에 다른 사람이 존재하는 상태에서 나가는 경우
                        JSONObject exitData = responseBody.getJSONObject("returnData");
                        //채팅방에서 나갔다는 사실을 그 채팅방에 있는 사용자들에게 전달
                        Message message = ChatService.handler.obtainMessage();
                        message.what = 5555;
                        message.obj = exitData.toString();
                        ChatService.handler.sendMessage(message);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }//end of onHttpResonse

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            //서버로 전송할 채팅방 참여자 리스트("계정/계정/계정")
            String participantString = data.getStringExtra("participantString");
            Log.d("채팅 참여자 리스트", participantString);
            String[] participantList = participantString.split("/");
            //대화상대를 한명만 선택한 경우에는 기존에 선택한 상대와의 대화방이 있는 경우와 없는 경우를 다시 분기해서 처리한다.
            if (participantList.length == 2) {//자기자신도 리스트에 포함되기 때문에 2
                Log.d("대화 상대", "1명");
                //사용자가 선택한 대화 상대의 닉네임
                String selectedUserAccount = participantList[1];
                Log.d("선택한 상대방 계정", selectedUserAccount);
                //채팅방이 하나도 없던 상태에서 만드는 경우
                if (chatRoomItemArrayList.size() == 0) {
                    addChatRoom(participantString, loginUser.getAccount());
                }
                //채팅방이 존재하는 상태에서 만드는 경우
                else {
                    for (int i = 0; i < chatRoomItemArrayList.size(); i++) {
                        //존재하는 채팅방이 1:1 채팅방인 경우
                        if (chatRoomItemArrayList.get(i).getUserList().size() == 1) {
                            //기존에 존재하는 1:1채팅방의 상대방 계정
                            String account = chatRoomItemArrayList.get(i).getUserList().get(0).getAccount();
                            //내가 선택한 사용자와의 채팅방이 이미 존재하는 경우 그 방으로 이동시킨다.
                            if (selectedUserAccount.equals(account)) {
                                Intent intent = new Intent(this, ChatRoomActivity.class);
                                intent.putExtra("roomNum", chatRoomItemArrayList.get(i).getRoomNum());
                                intent.putExtra("isNewRoom", false);//새로운 채팅방인지 여부
                                //대화 상대의 정보를 json스트링으로 만들어서 intent
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("account", account);
                                    jsonObject.put("nickname", chatRoomItemArrayList.get(i).getUserList().get(0).getNickname());
                                    jsonObject.put("profile", chatRoomItemArrayList.get(i).getUserList().get(0).getProfile());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                JSONArray jsonArray = new JSONArray();
                                jsonArray.put(jsonObject);
                                intent.putExtra("participantList", jsonArray.toString());
                                Log.d("참여자 리스트", jsonArray.toString());
                                startActivity(intent);
                                return;//함수 탈출
                            }
                        }
                    }
                    //존재하는 채팅방이 1:N채팅방인 경우
                    addChatRoom(participantString, loginUser.getAccount());
                }
            }
            //대화상대를 두명 이상 선택했을 때는 같은 조합의 구성원으로 된 방의 존재 유무과 무관하게 새롭게 방을 생성한다.
            else {
                Log.d("대화 상대", "복수");
                addChatRoom(participantString, loginUser.getAccount());
            }
        }
    }//end of onActicityResult


    //채팅방 리스트를 클릭하면 호출되는 메소드
    @Override
    public void onContainerClicked(int position) {
        Log.d("채팅방 리스트", "클릭");
        Intent intent = new Intent(this, ChatRoomActivity.class);
        //채팅방 번호
        intent.putExtra("roomNum", chatRoomItemArrayList.get(position).getRoomNum());
        intent.putExtra("isNewRoom", false);//새롭게 생성된 채팅방 여부
        int size = chatRoomItemArrayList.get(position).getUserList().size();
        //자신을 제외한 참여자가 존재하는 경우
        if (size > 0) {
            //참여자 리스트를 담을 jsonArray
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < size; i++) {
                //참여자들의 데이터를 json스트링으로 만들어 intent
                JSONObject jsonObject = new JSONObject();
                String account = chatRoomItemArrayList.get(position).getUserList().get(i).getAccount();
                String nickname = chatRoomItemArrayList.get(position).getUserList().get(i).getNickname();
                String profile = chatRoomItemArrayList.get(position).getUserList().get(i).getProfile();
                try {
                    jsonObject.put("account", account);
                    jsonObject.put("nickname", nickname);
                    jsonObject.put("profile", profile);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(jsonObject);
            }
            Log.d("참여자 리스트", jsonArray.toString());
            //채팅방 참여자 리스트
            intent.putExtra("participantList", jsonArray.toString());
        }
        //자기 자신만 채팅방에 남은 경우
        else {
            intent.putExtra("participantList", "null");
        }

        //미확인 메세지 개수
        int newMessageCount = 0;
        for (int i = 0; i < chatRoomItemArrayList.size(); i++) {
            if (chatRoomItemArrayList.get(i).getNewMessageCount() != 0) {
                newMessageCount += chatRoomItemArrayList.get(i).getNewMessageCount();
            }
        }
        //미확인 메세지가 있는 방을 들어가는 경우에는 그 방의 미확인 메세지 수만큼 빼서 넘겨준다.
        if (chatRoomItemArrayList.get(position).getNewMessageCount() != 0) {
            newMessageCount -= chatRoomItemArrayList.get(position).getNewMessageCount();
            intent.putExtra("newMessageCount", newMessageCount);
        }
        //그렇지 않은 경우에는 그냥 넘겨준다.
        else {
            intent.putExtra("newMessageCount", newMessageCount);
        }

        startActivity(intent);
    }//end of onContainerClicked

    //스와이프를 하면 나오는 채팅방 나가기 버튼 클릭시 호출되는 메소드
    @Override
    public void onExitRoomClicked(int position) {
        Log.d("채팅방 나가기", "클릭");
        //채팅방 나가기 확인 다이얼로그 생성
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.exit_chatroom_check_box);
        //예, 아니요 버튼
        TextView tv_yes, tv_no;
        tv_yes = dialog.findViewById(R.id.textview_yes);
        tv_no = dialog.findViewById(R.id.textview_no);
        //예 클릭 리스너
        tv_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //채팅방 나가기
                exitChatRoom(position);
                dialog.dismiss();
            }
        });
        //아니요 클릭 리스너
        tv_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //다이얼로그 종료
                dialog.dismiss();
            }
        });
        //다이얼로그 실행
        dialog.show();
    }//end of onExitRoomClicked

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume 호출");
        if (getIntent() != null) {
            isFromChatRoom = getIntent().getBooleanExtra("isFromChatRoom", false);
        }
        getChatRoom(loginUser.getAccount());//채팅방을 가져온다.
    }

    private class NotifyRoomAddedToChatServer extends Thread {
        String token;

        private NotifyRoomAddedToChatServer(String token) {
            this.token = token;
        }

        @Override
        public void run() {
            Message message = ChatService.handler.obtainMessage();
            message.what = 2222;
            message.obj = token;
            ChatService.handler.sendMessage(message);
        }
    }
}
