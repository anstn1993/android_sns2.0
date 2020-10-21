package com.example.sns.chat.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sns.*;
import com.example.sns.chat.adapter.ChatContentAdapter;
import com.example.sns.chat.service.ChatService;
import com.example.sns.chat.adapter.ParticipantListAdapter;
import com.example.sns.chat.model.ChatType;
import com.example.sns.chat.model.AddChatResponse;
import com.example.sns.chat.model.ChatContentItem;
import com.example.sns.chat.model.ChatParticipantItem;
import com.example.sns.facecall.activity.FaceChatResponseWaitingActivity;
import com.example.sns.login.model.LoginUser;
import com.example.sns.util.CompressMedia;
import com.example.sns.util.HttpRequest;
import com.example.sns.util.ProcessImage;
import com.example.sns.util.RetrofitService;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatRoomActivity extends AppCompatActivity implements ChatContentAdapter.ChatContentRecyclerViewListener, ParticipantListAdapter.ParticipantListRecyclerViewListener, HttpRequest.OnHttpResponseListener {
    private String TAG = "ChatRoomActivity";
    private EditText et_chat;//댓글 입력 창
    private TextView tv_send, tv_participant, tv_messageCount, tv_newMessage;
    private ImageButton ib_back, ib_addContent, ib_setting;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    //채팅방 번호
    public static int roomNum;
    //미확인 메세지 수
    private int newMessageCount;
    //최초 로드 시에 가져올 채팅 아이템의 수
    private int listSize;

    public static Handler handler;

    //전송할 이미지의 경로를 담을 어레이
    private ArrayList<String> imageArrayList;
    //서버로 전송할 때 사용될 이미지 파일의 이름을 담을 리스트
    private ArrayList<String> imageNameForServerList;
    //이 채팅방에 존재하는 모든 이미지 파일의 이름을 담을 리스트
    private ArrayList<String> totalContentArrayList;
    //채팅 메세지를 표시해줄 리사이클러뷰
    private RecyclerView rv_chatContent;
    private LinearLayoutManager linearLayoutManager;
    private ChatContentAdapter chatContentAdapter;
    private ArrayList<ChatContentItem> chatContentItemArrayList;
    //채팅방 서랍 속 채팅방 구성원을 표시해준 리사이클러뷰
    private RecyclerView rv_participantList;
    private LinearLayoutManager linearLayoutManager1;
    private ParticipantListAdapter participantListAdapter;
    private ArrayList<ChatParticipantItem> chatParticipantItemArrayList;

    //페이징 조건을 제어하기 위한 boolean
    private boolean loadPossible = true;

    //채팅 아이템 간의 전송 시간 비교를 위한 시간 포맷
    private String fromFormat = "yyyy-MM-dd HH:mm:ss";
    private String toFormat = "yyyy-MM-dd";

    private boolean isNewRoom;//새로운 채팅방 여부
    private LoginUser loginUser;

    //패킷의 타입
    private final int TEXT_MESSAGE = 1111;//텍스트 메시지
    private final int IMAGE_MESSAGE = 2222;//이미지 메시지
    private final int VIDEO_MESSAGE = 8888;//비디오 메시지
    private final int CHECK_MESSAGE = 3333;//다른 사용자가 메세지를 확인했을 때 받게 되는 메시지
    private final int ENTRANCE_MESSAGE = 4444;//다른 사용자가 입장했을 때 전달 받는 메시지
    private final int EXIT_MESSAGE = 5555;//다른 사용자가 나갔을 때 전달 받는 메시지
    private final int INVITE_MESSAGE = 6666;//다른 사용자를 초대했을 때 전달 받는 메시지
    private final int FACECALL_REQUEST_RECEIVED = 7777;//상대방이 영상 통화 요청을 잘 받았음을 알려주는 메시지


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("채팅방 화면 onCreate", "호출");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        loginUser = LoginUser.getInstance();

        et_chat = findViewById(R.id.edittext_chat);
        tv_send = findViewById(R.id.textview_send);
        tv_participant = findViewById(R.id.textview_participant);
        tv_messageCount = findViewById(R.id.textview_messagecount);
        tv_newMessage = findViewById(R.id.textview_newmessage);
        ib_back = findViewById(R.id.imagebutton_back);
        ib_addContent = findViewById(R.id.imagebutton_addcontent);
        ib_setting = findViewById(R.id.imagebutton_setting);
        drawerLayout = findViewById(R.id.drawerlayout);
        navigationView = findViewById(R.id.navigationview);


        setRecyclerView();

        if (getIntent() != null) {

            roomNum = getIntent().getIntExtra("roomNum", 0);
            isNewRoom = getIntent().getBooleanExtra("isNewRoom", false);
            // ex) [{"account":"gggg","nickname":"진처리","profile":"gggg021103.jpg"},{"account":"rlarpdlcm","nickname":"정후이","profile":"rlarpdlcm025328.jpg"}]
            //참여자가 없는 경우에는 "null"
            String participantListString = getIntent().getStringExtra("participantList");

            if (!participantListString.equals("null")) {
                try {
                    //넘어온 json 어레이 스트링을 json객체로 변환해서 사용자 데이터 리스트에 넣어준다.
                    JSONArray jsonArray = new JSONArray(participantListString);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject userData = jsonArray.getJSONObject(i);
                        String account = userData.getString("account");
                        String nickname = userData.getString("nickname");
                        String profile = userData.getString("profile");
                        chatParticipantItemArrayList.add(new ChatParticipantItem(account, nickname, profile));
                    }
                    //채팅방 서랍의 참여자 목록 리사이클러뷰 어뎁터에 notify
                    participantListAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //채팅방 참여자들을 나열해서 문자열로 붙여 채팅방 제목을 형성하기 위한 stringbuilder
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                    String nickname = chatParticipantItemArrayList.get(i).getNickname();
                    if (i == 0) {
                        stringBuilder.append(nickname);
                    } else {
                        stringBuilder.append("," + nickname);
                    }
                }
                tv_participant.setText(stringBuilder.toString());
            } else {
                tv_participant.setText("참여자 없음");
            }

            newMessageCount = getIntent().getIntExtra("newMessageCount", 0);
            //미확인 메세지가 0개면 화면에서 보이지 않게
            if (newMessageCount == 0) {
                tv_messageCount.setVisibility(View.INVISIBLE);
            }
            //미확인 메세지가 1개 이상이면 개수를 셋
            else {
                tv_messageCount.setVisibility(View.VISIBLE);
                tv_messageCount.setText(String.valueOf(newMessageCount));
            }
        }
        //최초로 가져올 메세지 아이템 수 설정
        listSize = 20 + newMessageCount;

        imageArrayList = new ArrayList<>();
        imageNameForServerList = new ArrayList<>();
        totalContentArrayList = new ArrayList<>();

        //채팅 서비스에서 넘어온 메세지를 받는 핸들러
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.d(TAG, msg.obj.toString());
                //텍스트, 이미지, 비디오 메시지인 경우
                if (msg.what == TEXT_MESSAGE || msg.what == IMAGE_MESSAGE || msg.what == VIDEO_MESSAGE) {
                    try {
                        String jsonString = msg.obj.toString();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        int roomNum_ = jsonObject.getInt("roomNum");
                        //넘어온 메세지가 현재 채팅방의 메세지인 경우에만 화면에 표시를 해준다.
                        if (roomNum == roomNum_) {
                            ChatContentItem chatContentItem = new ChatContentItem();
                            String type = jsonObject.getString("type");
                            String account = jsonObject.getString("account");
                            String nickname = jsonObject.getString("nickname");
                            String profile = jsonObject.getString("profile");
                            String time = jsonObject.getString("time");
                            String message = null;
                            chatContentItem.setRoomNum(roomNum_);
                            chatContentItem.setType(type);
                            chatContentItem.setAccount(account);
                            chatContentItem.setNickname(nickname);
                            chatContentItem.setProfile(profile);
                            chatContentItem.setIsMyContent(false);
                            chatContentItem.setChatType(ChatType.CONTENT);
                            chatContentItem.setIsSent(true);
                            chatContentItem.setTime(time);
                            //컨튼츠가 텍스트 메시지인 경우
                            if (msg.what == TEXT_MESSAGE) {
                                message = jsonObject.getString("message");
                                chatContentItem.setMessage(message);
                            }
                            //컨텐츠가 이미지인 경우
                            else if (msg.what == IMAGE_MESSAGE) {
                                //넘어온 이미지의 파일명을 담는 json배열
                                JSONArray imageArray = jsonObject.getJSONArray("imageList");
                                //채팅 아이템에 셋해줄 이미지 어레이
                                ArrayList<String> imageArrayList = new ArrayList<>();
                                //각 파일명을 어레이에 담아서 채팅 아이템에 셋해준다.
                                for (int i = 0; i < imageArray.length(); i++) {
                                    imageArrayList.add(imageArray.getString(i));
                                }
                                chatContentItem.setImageList(imageArrayList);
                                message = "새로운 사진을 보냈습니다.";

                                //사진 상세보기 페이지에서 컨텐츠를 볼 수 있게 해주기 위해 채팅방의 전체 컨텐츠 리스트에 추가를 해준다.
                                //ex) "{"account":"rangkim", "nickname":"뢩킴","profile":"...","time":"...","imageList":["image1","image2"...]}"
                                JSONObject contentData = new JSONObject();
                                //사용자가 전송한 이미지를 담을 json어레이
                                for (int i = 0; i < imageArrayList.size(); i++) {
                                    contentData.put("account", account);
                                    contentData.put("nickname", nickname);
                                    contentData.put("profile", profile);
                                    contentData.put("time", time);
                                    contentData.put("type", type);
                                    contentData.put("content", imageArrayList.get(i));
                                    totalContentArrayList.add(contentData.toString());
                                }
                            }
                            //컨텐츠가 동영상인 경우
                            else {
                                String video = jsonObject.getString("video");
                                chatContentItem.setVideo(video);
                                message = "새로운 동영상을 보냈습니다.";
                                //사진 상세보기 페이지에서 컨텐츠를 볼 수 있게 해주기 위해 채팅방의 전체 컨텐츠 리스트에 추가를 해준다.
                                //ex) "{"account":"rangkim", "nickname":"뢩킴","profile":"...","time":"...","imageList":["image1","image2"...]}"
                                JSONObject contentData = new JSONObject();
                                //사용자가 전송한 이미지를 담을 json어레이
                                contentData.put("account", account);
                                contentData.put("nickname", nickname);
                                contentData.put("profile", profile);
                                contentData.put("time", time);
                                contentData.put("type", type);
                                contentData.put("content", video);
                                totalContentArrayList.add(contentData.toString());
                            }

                            //미확인자 리스트 스트링을 송신자의 계정을 빼고 만들어준다.
                            //최종적으로 아이템에 셋을 해줄 미확인자 리스트 스트링
                            String unCheckedParticipant = "null";
                            //송신자의 계정은 제외한다.
                            boolean isFirst = true;
                            boolean isAdded = false;
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                                if (!chatParticipantItemArrayList.get(i).getAccount().equals(account)) {
                                    //송신자의 계정이 아닌 첫번째 계정인 경우
                                    if (isFirst) {
                                        stringBuilder.append(chatParticipantItemArrayList.get(i).getAccount());
                                        isFirst = false;
                                    }
                                    //첫번째 계정이 아닌 경우
                                    else {
                                        stringBuilder.append("/" + chatParticipantItemArrayList.get(i).getAccount());
                                    }
                                    isAdded = true;
                                }

                                if (isAdded) {//미확인 계정이 추가됐을 때만 stringBuilder로 값을 바꿔준다.
                                    unCheckedParticipant = stringBuilder.toString();
                                }
                            }
                            chatContentItem.setUnCheckedParticipant(unCheckedParticipant);

                            //아이템 리스트에 아이템이 두개 이상이 되면 메세지의 전송 시간을 비교해서 두 아이템의 전송 날짜가 다른 경우
                            //시간 구분 아이템을 함께 추가해준다.
                            if (chatContentItemArrayList.size() >= 1) {
                                //지금 받은 메세지가 가장 늦은 시간
                                String laterTime = ChatContentDetailActivity.formatDate(time, fromFormat, toFormat);
                                //메세지를 받기 전에 있었던 가장 최하단의 메세지
                                String earlierTime = ChatContentDetailActivity.formatDate(chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).getTime(), fromFormat, toFormat);
                                //연, 월, 일이 다르면 다른 날에 전송된 아이템이기 때문에 시간 구분 아이템을 추가하여 아이템에 구분을 준다.
                                if (!earlierTime.equals(laterTime)) {
                                    ChatContentItem timeDividerItem = new ChatContentItem();
                                    timeDividerItem.setTime(time);
                                    timeDividerItem.setChatType(ChatType.TIMEDIVIDER);
                                    chatContentItemArrayList.add(timeDividerItem);
                                    chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                                }
                            }

                            //채팅 아이템 추가
                            chatContentItemArrayList.add(chatContentItem);
                            chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);

                            //현재 완전히 보이는 채팅 아이템이 최신 채팅 아이템인 경우
                            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == chatContentItemArrayList.size() - 1) {
                                //채팅 리사이클러뷰를 최하단으로 스크롤
                                rv_chatContent.scrollToPosition(chatContentItemArrayList.size() - 1);
                            }
                            //현재 완전히 보이는 채팅 아이템이 최신 채팅 아이템이 아닌 경우
                            else {
                                tv_newMessage.setText(nickname + ": " + message);//새로운 메세지 박스에 메세지를 set
                                tv_newMessage.setVisibility(View.VISIBLE);//새로운 메세지 박스 visible
                            }
                        }
                        //다른 방으로 온 메세지인 경우
                        else {
                            if (newMessageCount == 0) {
                                tv_messageCount.setVisibility(View.VISIBLE);
                            }
                            newMessageCount += 1;
                            tv_messageCount.setText(String.valueOf(newMessageCount));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //내가 보낸 메세지를 다른 사용자가 확인했다는 패킷를 받은 경우
                else if (msg.what == CHECK_MESSAGE) {
                    try {
                        //확인 메세지 데이터를 json객체로 변환해준다.
                        JSONObject checkData = new JSONObject(msg.obj.toString());
                        //나의 메세지를 확인한 사용자의 계정
                        String checkedAccount = checkData.getString("receiver");
                        updateUncheckedParticipant(checkedAccount, "checked");//미확인 사용자 목록 업데이터
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //특정 사용자가 방으로 입장했다는 메세지를 받는 경우
                else if (msg.what == ENTRANCE_MESSAGE) {
                    try {
                        //json스트링을 json객체로 변환
                        String jsonString = msg.obj.toString();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        //방 번호
                        int roomNum_ = jsonObject.getInt("roomNum");
                        //참여자의 계정
                        String enteredAccount = jsonObject.getString("account");
                        //현재 들어와있는 채팅방에서 날아온 메세지인 경우에 한해서 다음 작업을 해준다.
                        if (roomNum_ == roomNum) {
                            updateUncheckedParticipant(enteredAccount, "checked");//미확인 사용자 목록 업데이트
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //특정 참여자가 채팅방을 나갔다는 메세지
                else if (msg.what == EXIT_MESSAGE) {
                    try {
                        String jsonString = msg.obj.toString();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        //방 번호
                        int roomNum_ = jsonObject.getInt("roomNum");
                        //넘어온 메세지가 현재 채팅방의 메세지인 경우에만 화면에 표시를 해준다.
                        if (roomNum == roomNum_) {
                            String exitAccount = jsonObject.getString("account");//채팅방을 나간 사용자 계정
                            String exitNickname = jsonObject.getString("nickname");//닉네임
                            String exitProfile = jsonObject.getString("profile");//프로필 사진 명
                            String message = jsonObject.getString("message");//메세지
                            String time = jsonObject.getString("time");//전송된 시간
                            //아이템 리스트에 아이템이 두개 이상이 되면 메세지의 전송 시간을 비교해서 두 아이템의 전송 날짜가 다른 경우
                            //시간 구분 아이템을 함께 추가해준다.
                            if (chatContentItemArrayList.size() >= 1) {
                                //지금 받은 메세지가 가장 늦은 시간
                                String laterTime = ChatContentDetailActivity.formatDate(time, fromFormat, toFormat);
                                //메세지를 받기 전에 있었던 가장 최하단의 메세지
                                String earlierTime = ChatContentDetailActivity.formatDate(chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).getTime(), fromFormat, toFormat);
                                //연, 월, 일이 다르면 다른 날에 전송된 아이템이기 때문에 시간 구분 아이템을 추가하여 아이템에 구분을 준다.
                                if (!earlierTime.equals(laterTime)) {
                                    ChatContentItem chatContentItem = new ChatContentItem();
                                    chatContentItem.setTime(time);
                                    chatContentItem.setChatType(ChatType.TIMEDIVIDER);
                                    chatContentItemArrayList.add(chatContentItem);
                                    chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                                }
                            }

                            //리사이클러뷰에 아이템 추가
                            ChatContentItem chatContentItem = new ChatContentItem();
                            chatContentItem.setRoomNum(roomNum_);
                            chatContentItem.setAccount(exitAccount);
                            chatContentItem.setNickname(exitNickname);
                            chatContentItem.setProfile(exitProfile);
                            chatContentItem.setMessage(message);
                            chatContentItem.setIsMyContent(false);
                            chatContentItem.setChatType(ChatType.EXIT);
                            chatContentItem.setIsSent(true);
                            chatContentItem.setTime(time);
                            chatContentItemArrayList.add(chatContentItem);
                            chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                            rv_chatContent.scrollToPosition(chatContentItemArrayList.size() - 1);

                            updateUncheckedParticipant(exitAccount, "exit");

                            //채팅방 서랍의 참여자 목록 업데이트
                            int index = 0;
                            for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                                if (exitNickname.equals(chatParticipantItemArrayList.get(i).getNickname())) {
                                    index = i;
                                }
                            }
                            //나간 참여자 아이템을 삭제하고
                            chatParticipantItemArrayList.remove(index);
                            //어뎁터에 notify
                            participantListAdapter.notifyItemRemoved(index + 1);//리사이클러뷰 헤더때문에 어댑터에서는 +1을 해줘야 한다.
                            //채팅방 제목 업데이트
                            if (chatParticipantItemArrayList.size() == 0) {
                                tv_participant.setText("참여자 없음");
                            } else {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                                    String nickname = chatParticipantItemArrayList.get(i).getNickname();
                                    if (i == 0) {
                                        stringBuilder.append(nickname);
                                    } else {
                                        stringBuilder.append("," + nickname);
                                    }
                                }
                                tv_participant.setText(stringBuilder.toString());
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //새로운 사용자를 초대했다는 메세지
                else if (msg.what == INVITE_MESSAGE) {
                    try {
                        //사용자 초대 메세지를 채팅 아이템 리스트에 추가해준다.
                        String jsonString = msg.obj.toString();
                        JSONObject addParticipantData = new JSONObject(jsonString);
                        int id = addParticipantData.getInt("id");
                        String time = addParticipantData.getString("time");
                        int roomNum = addParticipantData.getInt("roomNum");
                        String message = addParticipantData.getString("message");
                        ChatContentItem chatContentItem = new ChatContentItem();
                        chatContentItem.setId(id);
                        chatContentItem.setTime(time);
                        chatContentItem.setRoomNum(roomNum);
                        chatContentItem.setMessage(message);
                        chatContentItem.setIsSent(true);
                        chatContentItem.setChatType(ChatType.INVITE);
                        chatContentItemArrayList.add(chatContentItem);
                        chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);

                        //채팅방 서랍의 대화상대 목록도 추가를 해준다.
                        //초대된 사용자들의 닉네임과 프로필사진명이 담긴 jsonArray
                        JSONArray userList = addParticipantData.getJSONArray("addedParticipantList");
                        for (int i = 0; i < userList.length(); i++) {
                            JSONObject userData = userList.getJSONObject(i);
                            String account = userData.getString("account");
                            String nickname = userData.getString("nickname");
                            String profile = userData.getString("profile");
                            chatParticipantItemArrayList.add(new ChatParticipantItem(account, nickname, profile));
                        }
                        participantListAdapter.notifyDataSetChanged();

                        //채팅방 제목 업데이트
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                            String nickname = chatParticipantItemArrayList.get(i).getNickname();
                            if (i == 0) sb.append(nickname);
                            else sb.append("," + nickname);
                        }
                        tv_participant.setText(sb.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (msg.what == FACECALL_REQUEST_RECEIVED) {//영상통화 요청이 상대방에게 잘 전달됐는지에 대한 메세지
                    try {
                        String jsonString = msg.obj.toString();
                        JSONObject faceChatRequestResult = new JSONObject(jsonString);
                        String result = faceChatRequestResult.getString("type");
                        if ("successFaceChatRequest".equals(result)) {//영상통화 요청이 잘 전달된 경우
                            Intent intent = new Intent(ChatRoomActivity.this, FaceChatResponseWaitingActivity.class);
                            intent.putExtra("screenOn", true);//화면 켜짐 상태에서 call 액티비티 진입
                            intent.putExtra("roomName", faceChatRequestResult.getString("roomName"));//방 번호
                            intent.putExtra("receiverAccount", faceChatRequestResult.getString("receiverAccount"));//수신자 계정
                            intent.putExtra("receiverNickname", faceChatRequestResult.getString("receiverNickname"));//수신자 닉네임
                            intent.putExtra("receiverProfile", faceChatRequestResult.getString("receiverProfile"));//수신자 프로필
                            startActivity(intent);  //통화화면으로 이동
                        } else if ("failFaceChatRequest".equals(result)) {//영상통화 요청이 전달되지 않은 경우
                            Toast.makeText(getApplicationContext(), "현재 상대방이 통화 불가능한 상태 입니다.", Toast.LENGTH_LONG).show();//통화 불가능 토스트
                        } else {//상대방이 통화중인 경우
                            Toast.makeText(getApplicationContext(), "상대방이 통화중 입니다.", Toast.LENGTH_LONG).show();//통화 불가능 토스트
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        //메시지 전송버튼 클릭 리스너
        tv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chat = et_chat.getText().toString();//메시지 내용
                if (TextUtils.isEmpty(chat)) {
                    if (chatParticipantItemArrayList.size() == 0) {
                        Toast.makeText(getApplicationContext(), "참여자가 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            //메세지 데이터를 json객체에 담아준다.
                            JSONObject messageData = new JSONObject();
                            messageData.put("type", "message");
                            messageData.put("roomNum", roomNum);
                            messageData.put("account", loginUser.getAccount());
                            messageData.put("nickname", loginUser.getNickname());
                            messageData.put("profile", loginUser.getProfile());
                            messageData.put("message", chat);

                            ChatContentItem chatContentItem = new ChatContentItem();
                            chatContentItem.setRoomNum(roomNum);
                            chatContentItem.setType("message");
                            chatContentItem.setAccount(loginUser.getAccount());
                            chatContentItem.setNickname(loginUser.getNickname());
                            chatContentItem.setProfile(loginUser.getProfile());
                            chatContentItem.setMessage(chat);
                            chatContentItem.setImageList(imageArrayList);
                            chatContentItem.setIsMyContent(true);
                            chatContentItem.setIsSent(false);
                            chatContentItem.setChatType(ChatType.CONTENT);
                            chatContentItemArrayList.add(chatContentItem);
                            chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                            rv_chatContent.scrollToPosition(chatContentItemArrayList.size() - 1);
                            if (tv_newMessage.getVisibility() == View.VISIBLE) {//메세지 박스가 보이는 경우 메세지 박스를 지워준다
                                tv_newMessage.setVisibility(View.GONE);
                            }
                            //수신자 스트링을 만들어준다.
                            String receiverString = null;
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                                String account = chatParticipantItemArrayList.get(i).getAccount();
                                if (i == 0) {
                                    stringBuilder.append(account);
                                } else {
                                    stringBuilder.append("/" + account);
                                }
                            }
                            receiverString = stringBuilder.toString();
                            addChat(String.valueOf(roomNum), loginUser.getAccount(), receiverString, chat, "message", messageData.toString(), imageArrayList, null, null, null);//채팅 메시지 전송
                            et_chat.setText("");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "문제가 생겼습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "채팅 내용을 입력하세요", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //컨텐츠 추가 버튼 클릭 리스너
        ib_addContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(ChatRoomActivity.this);
                dialog.setContentView(R.layout.upload_content_select_box);

                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(dialog.getWindow().getAttributes());
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setAttributes(layoutParams);


                Button btn_photo, btn_video;//사진, 동영상 버튼

                btn_photo = dialog.findViewById(R.id.button_photo);
                btn_video = dialog.findViewById(R.id.button_video);

                btn_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //tedbottompicker라이브러리
                        TedBottomPicker.with(ChatRoomActivity.this)
                                //이미지 피커액티비티가 올라오는 높이 설정
                                .setPeekHeight(2000)
                                .showTitle(false)
                                .setCompleteButtonText("완료")
                                .setEmptySelectionText("이미지 없음")
                                .setPreviewMaxCount(1000)
                                .setSelectMaxCount(6)
                                .setSelectMinCount(1)
                                .setEmptySelectionText("이미지를 선택해주세요.")
                                .showMultiImage(new TedBottomSheetDialogFragment.OnMultiImageSelectedListener() {
                                    //이미지를 선택한 후 완료버튼을 누르면 호출되는 메소드
                                    @Override
                                    public void onImagesSelected(List<Uri> uriList) {

                                        dialog.dismiss();

                                        try {
                                            //소켓 서버로 전달할 json스트링
                                            JSONObject imageData = new JSONObject();
                                            imageData.put("type", "image");
                                            imageData.put("roomNum", roomNum);
                                            imageData.put("account", loginUser.getAccount());
                                            imageData.put("nickname", loginUser.getNickname());
                                            imageData.put("profile", loginUser.getProfile());
                                            imageData.put("message", loginUser.getNickname() + "님이 사진을 보냈습니다.");

                                            JSONArray imageList = new JSONArray();
                                            //선택한 이미지 경로를 json 어레이에 넣어준다.
                                            for (int i = 0; i < uriList.size(); i++) {
                                                imageArrayList.add(uriList.get(i).toString());
                                                //이미지 파일의 이름 설정
                                                String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
                                                String imageFileName = loginUser.getAccount() + timeStamp + (i + 1) + ".jpg";
                                                imageNameForServerList.add(imageFileName);
                                                Log.d("이미지 파일명", imageNameForServerList.get(i));
                                                imageList.put(imageNameForServerList.get(i));
                                            }
                                            imageData.put("imageList", imageList);
                                            ChatContentItem chatContentItem = new ChatContentItem();
                                            chatContentItem.setRoomNum(roomNum);
                                            chatContentItem.setType("image");
                                            chatContentItem.setAccount(loginUser.getAccount());
                                            chatContentItem.setNickname(loginUser.getNickname());
                                            chatContentItem.setProfile(loginUser.getProfile());
                                            chatContentItem.setMessage(loginUser.getNickname() + "님이 사진을 보냈습니다.");
                                            for (int j = 0; j < imageArrayList.size(); j++) {
                                                chatContentItem.getImageList().add(imageArrayList.get(j));
                                            }
                                            chatContentItem.setIsMyContent(true);
                                            chatContentItem.setIsSent(false);
                                            chatContentItem.setIsImageFromServer(false);
                                            chatContentItem.setChatType(ChatType.CONTENT);
                                            chatContentItemArrayList.add(chatContentItem);
                                            chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                                            rv_chatContent.scrollToPosition(chatContentItemArrayList.size() - 1);
                                            if (tv_newMessage.getVisibility() == View.VISIBLE) {//메세지 박스가 보이는 경우 메세지 박스를 지워준다
                                                tv_newMessage.setVisibility(View.GONE);
                                            }

                                            //수신자 스트링을 만들어준다.
                                            String receiverString = null;
                                            StringBuilder stringBuilder = new StringBuilder();
                                            for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                                                String account = chatParticipantItemArrayList.get(i).getAccount();
                                                if (i == 0) {
                                                    stringBuilder.append(account);
                                                } else {
                                                    stringBuilder.append("/" + account);
                                                }
                                            }
                                            receiverString = stringBuilder.toString();
                                            Log.d("이미지 메세지 데이터", imageData.toString());
                                            addChat(String.valueOf(roomNum), loginUser.getAccount(), receiverString, loginUser.getNickname() + "님이 사진을 보냈습니다.", "image", imageData.toString(), imageArrayList, null, null, null);


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                    }
                });

                btn_video.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //tedbottompicker라이브러리
                        TedBottomPicker.with(ChatRoomActivity.this)
                                //이미지 피커액티비티가 올라오는 높이 설정
                                .setPeekHeight(2000)
                                .showTitle(true)
                                .setTitle("동영상")
                                .setCompleteButtonText("완료")
                                .setEmptySelectionText("동영상 없음")
                                .setPreviewMaxCount(1000)
                                .setEmptySelectionText("동영상을 선택해주세요.")
                                .showVideoMedia()
                                .setSelectMaxCount(1)
                                .setSelectMinCount(1)
                                .showMultiImage(new TedBottomSheetDialogFragment.OnMultiImageSelectedListener() {
                                    //이미지를 선택한 후 완료버튼을 누르면 호출되는 메소드
                                    @Override
                                    public void onImagesSelected(List<Uri> uriList) {

                                        int[] videoSize = new int[2];//비디오 해상도를 담는 배열
                                        //비디오 데이터의 해상도를 구하기 위해서 메타데이터 리트리버 사용
                                        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                                        mediaMetadataRetriever.setDataSource(uriList.get(0).getPath());//비디오 소스 셋
                                        //비디오 데이터의 ratation에 따라서 width, height값이 정확히 안 나올 수 있기 때문에 rotation에 따라 설정해준다.
                                        String metaRotation = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                                        int rotation = metaRotation == null ? 0 : Integer.parseInt(metaRotation);
                                        Log.d(TAG, "media rotation = " + rotation);
                                        //rotation이 90이나 270인 경우 width와 height값이 실제와 반대로 나타나기 때문에 width에 height값을 height에 width값을 넣는다.
                                        if (rotation == 90 || rotation == 270) {
                                            videoSize[0] = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                                            videoSize[1] = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                                        }
                                        //그렇지 않은 경우는 그냥 정상적으로 대입한다.
                                        else {
                                            videoSize[0] = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                                            videoSize[1] = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                                        }
                                        //비디오 데이터의 해상도를 구한다.
                                        dialog.dismiss();
                                        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
                                        String videoFileName = loginUser.getAccount() + timeStamp + ".mp4";//서버로 업로드할 비디오 파일 명
                                        //소켓 서버로 전달할 json스트링
                                        JSONObject videoData = new JSONObject();
                                        try {
                                            videoData.put("type", "video");
                                            videoData.put("roomNum", roomNum);
                                            videoData.put("account", loginUser.getAccount());
                                            videoData.put("nickname", loginUser.getNickname());
                                            videoData.put("profile", loginUser.getProfile());
                                            videoData.put("message", loginUser.getNickname() + "님이 동영상을 보냈습니다.");
                                            videoData.put("video", videoFileName);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        ChatContentItem chatContentItem = new ChatContentItem();
                                        chatContentItem.setRoomNum(roomNum);
                                        chatContentItem.setType("video");
                                        chatContentItem.setAccount(loginUser.getAccount());
                                        chatContentItem.setNickname(loginUser.getNickname());
                                        chatContentItem.setProfile(loginUser.getProfile());
                                        chatContentItem.setMessage(loginUser.getNickname() + "님이 동영상을 보냈습니다.");
                                        chatContentItem.setVideo(uriList.get(0).getPath());//아직 서버에 업로드하기 전이기 때문에 local uri에서 썸네일 추출
                                        chatContentItem.setIsMyContent(true);
                                        chatContentItem.setIsSent(false);
                                        chatContentItem.setIsVideoFromServer(false);
                                        chatContentItem.setChatType(ChatType.CONTENT);
                                        chatContentItemArrayList.add(chatContentItem);
                                        chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                                        rv_chatContent.scrollToPosition(chatContentItemArrayList.size() - 1);
                                        if (tv_newMessage.getVisibility() == View.VISIBLE) {//메세지 박스가 보이는 경우 메세지 박스를 지워준다
                                            tv_newMessage.setVisibility(View.GONE);
                                        }

                                        //수신자 스트링을 만들어준다.
                                        String receiverString = null;
                                        StringBuilder stringBuilder = new StringBuilder();
                                        for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                                            String account = chatParticipantItemArrayList.get(i).getAccount();
                                            if (i == 0) {
                                                stringBuilder.append(account);
                                            } else {
                                                stringBuilder.append("/" + account);
                                            }
                                        }
                                        receiverString = stringBuilder.toString();
                                        String finalReceiverString = receiverString;
                                        //비디오 압축을 해야 하기 때문에 따로 스레드를 생성해서 실행
                                        new Thread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addChat(String.valueOf(roomNum), loginUser.getAccount(), finalReceiverString, loginUser.getNickname() + "님이 동영상을 보냈습니다.", "video", videoData.toString(), imageArrayList, uriList.get(0), videoFileName, videoSize);
                                                    }
                                                }
                                        ).start();
                                    }
                                });
                    }
                });
                dialog.show();
            }
        });


        //뒤로가기 버튼 클릭 리스너
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //방을 만들고 채팅을 보내지 않은 상태에서 그냥 나가는 경우
                if (chatContentItemArrayList.size() == 0 && isNewRoom == true) {
                    try {
                        //채팅 방을 삭제
                        deleteChatRoom(roomNum);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("type", "removechatroom");
                        jsonObject.put("roomNum", roomNum);
                        //소켓 서버로 채팅방 삭제 토큰을 날러서 채팅방 리스트에서 채팅방을 지워준다.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Message message = ChatService.handler.obtainMessage();
                                message.what = 3333;
                                message.obj = jsonObject.toString();
                                ChatService.handler.sendMessage(message);
                            }
                        }).start();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                finish();
                return;
            }
        });

        //설정 버튼 클릭 리스너
        ib_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //채팅방 서랍 오픈
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        //새로운 메세지 박스 클릭 리스너
        tv_newMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rv_chatContent.scrollToPosition(chatContentItemArrayList.size() - 1);
                tv_newMessage.setVisibility(View.GONE);
            }
        });

        //리사이클러뷰 스크롤 리스너
        //addOnScrollListener는 리사이클러뷰를 스크롤하는 순간을 캐치한다.
        rv_chatContent.addOnScrollListener(new RecyclerView.OnScrollListener() {
            //리사이클러뷰가 스크롤된 후 콜백되는 메소드
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //현재 화면에서 보이는 첫번째 아이템의 index
                int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                Log.d("현재 화면의 첫번째 index", String.valueOf(firstVisibleItemPosition));

                //현재 화면에서 완전하게 보이는 첫번째 아이템의 index
                int firstCompletelyVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                Log.d("완전하게 보이는 첫번째 index", String.valueOf(firstCompletelyVisibleItemPosition));

                //findLastVisibleItemPosition은 현재 화면에 보이는 뷰 중 가장 마지막 뷰의 position을 리턴해준다.
                //즉 이 변수는 현재 화면에 보이는 아이템 중 가장 마지막 아이템의 index를 담는다
                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                Log.d("현재 화면의 마지막 index", String.valueOf(lastVisibleItemPosition));

                //현재 화면에서 완전하게 보이는 마지막 아이템의 index
                int lastCompletelyVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                Log.d("완전하게 보이는 마지막 index", String.valueOf(lastCompletelyVisibleItemPosition));
                //이 변수는 전체 리사이클러뷰의 아이템 개수를 담는다.
                int totalCount = recyclerView.getAdapter().getItemCount();
                Log.d("전체 아이템 개수", String.valueOf(totalCount));
                //이 변수는 현재 화면에 보이는 아이템의 개수를 담는다.(내 경우에는 1~2를 왔다갔다 함)
                int visibleItemCount = recyclerView.getChildCount();
                Log.d("화면에 보여지는 아이템 개수", String.valueOf(visibleItemCount));

                //마지막으로 보이는 아이템의 index가 전체 아이템 수에서 1을 뺀 값과 같고 현재 화면에 보이는 아이템 수가 12개이며 마지막으로 완전히 보이는 아이템의 index가
                //전체 아이템 수에서 1을 뺀 값과 같을 때만 페이징을 실행한다.
                if (firstCompletelyVisibleItemPosition == 0 && loadPossible == true && chatContentItemArrayList.size() >= 20) {
                    Log.d("페이징 조건", "부합");
                    //다음 페이지를 로드한다.
                    //param:현재 로드되어있는 데이터의 수(다음 페이지에 로드되어야 할 첫번째 게시물의 index)
                    loadNextPage();
                }


            }
        });


        //채팅방 서랍의 item(사진, 채팅방 나가기)클릭 리스너
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.content:
                        //채팅방의 사진을 그리드 형식의 리사이클러뷰로 볼 수 있는 액티비티로 이동
                        Intent intent = new Intent(ChatRoomActivity.this, ChatContentListActivity.class);
                        intent.putExtra("totalContentCount", totalContentArrayList.size());//전체 이미지 수
                        for (int i = 0; i < totalContentArrayList.size(); i++) {
                            intent.putExtra("contentData" + i, totalContentArrayList.get(i));//json 스트링 컨텐츠 데이터
                        }
                        startActivity(intent);

                        break;
                    case R.id.exit:
                        Log.d("채팅방 나가기", "클릭");
                        //채팅방 나가기 확인 다이얼로그 생성
                        Dialog dialog = new Dialog(ChatRoomActivity.this);
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
                                exitChatRoom();
                                //다이얼로그 종료
                                dialog.dismiss();
                                //채팅방 서랍 닫기
                                drawerLayout.closeDrawer(GravityCompat.END);
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
                        break;
                }

                return false;
            }
        });


    }


    private void updateUncheckedParticipant(String checkedAccount, String payload) {
        //메시지 미확인자 스트링 변수를 업데이트 해준다.
        for (int i = 0; i < chatContentItemArrayList.size(); i++) {
            //채팅 메세지 전체를 돌면서 메세지 미확인자 리스트에 메세지를 확인한 사용자의 계정이 존재하는지 파악해서
            //존재하는 경우 그 계정을 지워준다.
            if (!chatContentItemArrayList.get(i).getChatType().equals(ChatType.TIMEDIVIDER) && !chatContentItemArrayList.get(i).getChatType().equals(ChatType.EXIT) && !chatContentItemArrayList.get(i).getChatType().equals(ChatType.INVITE)) {
                //메세지를 전송한 사람의 계정이 메세지 아이템의 주인 계정과 같지 않은 경우에만 수행한다.
                if (chatContentItemArrayList.get(i).getUnCheckedParticipant().contains(checkedAccount) && !chatContentItemArrayList.get(i).getAccount().equals(checkedAccount)) {
                    Log.d(TAG, "확인한 사람" + checkedAccount);
                    String unCheckedParticipant = chatContentItemArrayList.get(i).getUnCheckedParticipant();
                    String[] unCheckedParticipantList = chatContentItemArrayList.get(i).getUnCheckedParticipant().split("/");
                    //만약 미확인 리스트에 메세지를 확인한 사용자의 계정만 남아있으면 바로 null처리를 해준다.
                    if (unCheckedParticipantList.length == 1) {
                        chatContentItemArrayList.get(i).setUnCheckedParticipant("null");
                    }
                    //아직 복수의 사용자가 남아있다면 리스트 안에서 메세지를 확인한 사용자 계정의 index를 파악해서 index별로 문자열을 다르게 제거한다.
                    else {
                        //메세지를 확인한 사용자 계정의 인덱스
                        int index = 0;
                        for (int j = 0; j < unCheckedParticipantList.length; j++) {
                            if (unCheckedParticipantList[j].equals(checkedAccount)) {
                                index = j;
                                break;
                            }
                        }
                        //메세지를 확인한 사용자 계정이 리스트의 마지막에 있는 경우
                        if (index == unCheckedParticipantList.length - 1) {
                            unCheckedParticipant = unCheckedParticipant.replace("/" + checkedAccount, "");
                        }
                        //리스트의 처음이나 중간에 있는 경우
                        else {
                            unCheckedParticipant = unCheckedParticipant.replace(checkedAccount + "/", "");
                        }
                        chatContentItemArrayList.get(i).setUnCheckedParticipant(unCheckedParticipant);
                    }
                }
            }
        }
        chatContentAdapter.notifyItemRangeChanged(0, chatContentItemArrayList.size(), payload);
    }


    private void setRecyclerView() {
        //사용자 데이터 리스트 초기화
        chatParticipantItemArrayList = new ArrayList<>();
        chatContentItemArrayList = new ArrayList<>();

        rv_chatContent = findViewById(R.id.recyclerview_chatcontent);
        rv_chatContent.setHasFixedSize(true);
        rv_participantList = findViewById(R.id.recyclerview_participantlist);
        rv_participantList.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager1 = new LinearLayoutManager(this);
        rv_chatContent.setLayoutManager(linearLayoutManager);
        rv_participantList.setLayoutManager(linearLayoutManager1);

        chatContentAdapter = new ChatContentAdapter(chatContentItemArrayList, this);
        chatContentAdapter.setOnClickListener(this);
        rv_chatContent.setAdapter(chatContentAdapter);

        participantListAdapter = new ParticipantListAdapter(chatParticipantItemArrayList, this);
        participantListAdapter.setOnClickListener(this);
        rv_participantList.setAdapter(participantListAdapter);
    }

    //채팅 데이터를 서버에 요청하는 메소드
    private void getChatContent(int roomNum, String account, int lastId, int listSize, boolean isFirstLoad) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("requestType", "getChat");
            requestBody.put("roomNum", roomNum);
            requestBody.put("account", account);
            requestBody.put("lastId", lastId);
            requestBody.put("listSize", listSize);
            requestBody.put("isFirstLoad", isFirstLoad);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getchatcontent.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //페이징을 통해 이전 채팅 데이터를 서버에 요청하는 메소드
    private void loadNextPage() {
        loadPossible = false;
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("requestType", "loadNextChat");
            requestBody.put("roomNum", roomNum);
            requestBody.put("account", loginUser.getAccount());
            requestBody.put("lastId", chatContentItemArrayList.get(0).getId());
            requestBody.put("listSize", listSize);
            requestBody.put("isFirstLoad", false);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getchatcontent.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //메세지를 읽으면 확인처리를 하기 위해 서버에 요청하는 메소드
    private void updateCheckMessage(int roomNum, String account) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("roomNum", roomNum);
            requestBody.put("account", account);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "updatecheckmessagebyenter.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //채팅방을 만들고 메세지를 최초로 보내는 경우 채팅방 활성화를 서버에 요청하는 메소드
    private void avtivateChatRoom(int roomNum) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("roomNum", roomNum);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "activatechatroom.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //채팅방을 만들고 새로운 메세지를 보내지 않고 그냥 나가는 경우 서버에 그 방을 없애는 요청을 하는 메소드
    private void deleteChatRoom(int roomNum) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("roomNum", roomNum);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "deletechatroom.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //채팅방을 나가는 로직을 구현한 메소드
    //param: 채팅방 리사이클러뷰의 채팅방 목록 index
    private void exitChatRoom() {

        //소켓에 전달할 데이터 json
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("type", "exit");
            requestBody.put("roomNum", roomNum);
            requestBody.put("account", loginUser.getAccount());
            requestBody.put("nickname", loginUser.getNickname());
            requestBody.put("profile", loginUser.getProfile());
            requestBody.put("message", loginUser.getNickname() + "님이 채팅방을 나가셨습니다.");
            requestBody.put("position", 0);//리사이클러뷰 상에서 나간 채팅방의 index
            //chat테이블에 저장할 수신자 목록을 jsonArray에 넣어준다.
            JSONArray receiverList = new JSONArray();

            for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                //수신자 닉네임
                String receiverAccount = chatParticipantItemArrayList.get(i).getAccount();
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

    //채팅방에서 사용자를 추가하면 그 내용을 반영하기 위해 서버에 요청하는 메소드
    private void addChatParticipant(int roomNum, JSONArray participantList) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("roomNum", roomNum);
            //ex) "[{"account":"anstn1993","nickname":"만수","profile":"..."},{...},{...}]"
            requestBody.put("participantList", participantList);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "addchatparticipant.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


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
                if (requestType.equals("getChat")) {
                    chatContentItemArrayList.clear();//채팅데이터를 다 지우고 다시 뿌려준다.
                    //jsonarray를 선언해서
                    JSONArray jsonArray = responseBody.getJSONArray("chatContentList");
                    //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        //서버로부터 넘어온 데이터를 변수에 정의
                        String time = data.getString("time");
                        //아이템 리스트에 아이템이 두개 이상이 되면 메세지의 전송 시간을 비교해서 두 아이템의 전송 날짜가 다른 경우
                        //시간 구분 아이템을 함께 추가해준다.
                        if (chatContentItemArrayList.size() >= 1) {
                            String earlierTime = ChatContentDetailActivity.formatDate(time, fromFormat, toFormat);
                            //현재 로드된 아이템 중 제일 시간이 빠른 아이템
                            String laterTime = ChatContentDetailActivity.formatDate(chatContentItemArrayList.get(0).getTime(), fromFormat, toFormat);
                            //연, 월, 일이 다르면 다른 날에 전송된 아이템이기 때문에 시간 구분 아이템을 추가하여 아이템에 구분을 준다.
                            if (!earlierTime.equals(laterTime)) {
                                ChatContentItem chatContentItem = new ChatContentItem();
                                chatContentItem.setTime(chatContentItemArrayList.get(0).getTime());
                                chatContentItem.setChatType(ChatType.TIMEDIVIDER);
                                chatContentItemArrayList.add(0, chatContentItem);
                            }
                        }

                        ChatContentItem chatContentItem = new Gson().fromJson(data.toString(), ChatContentItem.class);
                        chatContentItemArrayList.add(0, chatContentItem);

                    }

                    chatContentAdapter.notifyDataSetChanged();
                    rv_chatContent.scrollToPosition(chatContentItemArrayList.size() - 1);

                    //현재 채팅방에 이미지가 존재한다면 전체 이미지를 조회할 수 있도록 리스트에 담아주는 작업을 한다.

                    if (!responseBody.isNull("totalContentData")) {
                        jsonArray = responseBody.getJSONArray("totalContentData");
                        Log.d("컨텐츠 수", String.valueOf(jsonArray.length()));
                        Log.d("전체 컨텐츠 데이터", jsonArray.toString());
                        if (jsonArray != null) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                totalContentArrayList.add(String.valueOf(jsonArray.get(i)));
                            }
                        }
                    }
                    //데이터 로드에 성공하면 서비스로 입장 메세지를 전달해서 방에 있는 다른 사용자들에게 메세지를 전달하도록 한다.
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                //데이터를 다 가져왔으면 현재 채팅방에 들어와있는 참여자들에게 입장사실을 전달해줘서 채팅을 확인했음을 알 수 잏게 해준다.
                                Message message = ChatService.handler.obtainMessage();
                                //입장 메세지 데이터를 json스트링으로 만든다.
                                JSONObject enterData = new JSONObject();
                                enterData.put("type", "enter");
                                enterData.put("roomNum", roomNum);
                                enterData.put("account", loginUser.getAccount());
                                enterData.put("nickname", loginUser.getNickname());
                                enterData.put("profile", loginUser.getProfile());
                                message.what = ENTRANCE_MESSAGE;
                                message.obj = enterData.toString();
                                ChatService.handler.sendMessage(message);

                                //서버에 업데이트 해준다.
                                updateCheckMessage(roomNum, loginUser.getAccount());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } else if (requestType.equals("loadNextChat")) {
                    //jsonarray를 선언해서
                    JSONArray jsonArray = responseBody.getJSONArray("chatContentList");
                    Log.d("게시물 수", String.valueOf(jsonArray.length()));

                    //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        //서버로부터 넘어온 데이터를 변수에 정의
                        String time = data.getString("time");
                        //아이템 리스트에 아이템이 두개 이상이 되면 메세지의 전송 시간을 비교해서 두 아이템의 전송 날짜가 다른 경우
                        //시간 구분 아이템을 함께 추가해준다.
                        if (chatContentItemArrayList.size() >= 1) {
                            String earlierTime = ChatContentDetailActivity.formatDate(time, fromFormat, toFormat);
                            //현재 로드된 아이템 중 제일 시간이 빠른 아이템
                            String laterTime = ChatContentDetailActivity.formatDate(chatContentItemArrayList.get(0).getTime(), fromFormat, toFormat);
                            //연, 월, 일이 다르면 다른 날에 전송된 아이템이기 때문에 시간 구분 아이템을 추가하여 아이템에 구분을 준다.
                            if (!earlierTime.equals(laterTime)) {
                                ChatContentItem chatContentItem = new ChatContentItem();
                                chatContentItem.setTime(chatContentItemArrayList.get(0).getTime());
                                chatContentItem.setChatType(ChatType.TIMEDIVIDER);
                                chatContentItemArrayList.add(0, chatContentItem);
                                chatContentAdapter.notifyItemInserted(0);
                            }
                        }
                        ChatContentItem chatContentItem = new Gson().fromJson(data.toString(), ChatContentItem.class);

                        chatContentItemArrayList.add(0, chatContentItem);
                        chatContentAdapter.notifyItemInserted(0);
                        listSize += 1;
                    }
                    loadPossible = true;
                } else if (requestType.equals("exitChatRoom")) {//채팅방을 나가는 통신
                    int exitType = responseBody.getInt("exitType");//채팅방에서 마지막으로 나가는지 아닌지를 분별하기 위한 변수
                    if (exitType == 0) {//채팅방에 다른 사람이 존재하는 상태에서 나가는 경우
                        JSONObject exitData = responseBody.getJSONObject("returnData");
                        //채팅방에서 나갔다는 사실을 그 채팅방에 있는 사용자들에게 전달
                        Message message = ChatService.handler.obtainMessage();
                        message.what = EXIT_MESSAGE;
                        message.obj = exitData.toString();
                        ChatService.handler.sendMessage(message);
                    }
                    //채팅방 액티비티 종료
                    finish();
                } else if (requestType.equals("addParticipant")) {//채팅방에 사용자를 초대한 경우
                    //ex) "{"participantList": [{"account":"anstn1993","nickname":"만수","profile":"..."},{...},{...}]}"
                    JSONArray addedParticipantList = responseBody.getJSONArray("participantList");
                    //채팅방 서랍에 사용자를 추가해준다.
                    for (int i = 0; i < addedParticipantList.length(); i++) {
                        JSONObject participantData = addedParticipantList.getJSONObject(i);
                        String account = participantData.getString("account");
                        String nickname = participantData.getString("nickname");
                        String profile = participantData.getString("profile");
                        chatParticipantItemArrayList.add(new ChatParticipantItem(account, nickname, profile));
                    }
                    participantListAdapter.notifyDataSetChanged();
                    //초대한 사용자 목록을 만들어준다.
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < addedParticipantList.length(); i++) {
                        JSONObject participantData = addedParticipantList.getJSONObject(i);
                        String nickname = participantData.getString("nickname");
                        if (i == 0) sb.append(nickname);
                        else sb.append("," + nickname);
                    }

                    //채팅방 제목을 설정한다.
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                        String nickname = chatParticipantItemArrayList.get(i).getNickname();
                        if (i == 0) {
                            stringBuilder.append(nickname);
                        } else {
                            stringBuilder.append("," + nickname);
                        }
                    }
                    tv_participant.setText(stringBuilder.toString());

                    //chat 테이블에 사용자가 추가 메세지 데이터를 추가해준다.
                    //chat 테이블에 추가해줄 messageDATA
                    JSONObject messageData = new JSONObject();
                    messageData.put("type", "added");
                    messageData.put("roomNum", roomNum);
                    //사용자를 초대한 사용자
                    messageData.put("account", loginUser.getAccount());
                    messageData.put("nickname", loginUser.getNickname());
                    messageData.put("profile", loginUser.getProfile());
                    messageData.put("message", loginUser.getNickname() + "님이 " + sb.toString() + "님을 초대하셨습니다.");
                    //수신자를 담을 json어레이
                    JSONArray receiverList = new JSONArray();
                    for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                        String account = chatParticipantItemArrayList.get(i).getAccount();
                        receiverList.put(account);
                    }
                    messageData.put("receiverList", receiverList);
                    //추가된 사용자리스트도 담아준다.
                    messageData.put("addedParticipantList", addedParticipantList);
                    addParticipantAddedMessage(messageData.toString());
                } else if (requestType.equals("sendAddedMessage")) {
                    JSONObject messageData = responseBody.getJSONObject("messageData");
                    int id = messageData.getInt("id");
                    String time = messageData.getString("time");
                    String msg = messageData.getString("message");
                    ChatContentItem chatContentItem = new ChatContentItem();
                    chatContentItem.setId(id);
                    chatContentItem.setTime(time);
                    chatContentItem.setRoomNum(roomNum);
                    chatContentItem.setMessage(msg);
                    chatContentItem.setIsSent(true);
                    chatContentItem.setChatType(ChatType.INVITE);
                    chatContentItemArrayList.add(chatContentItem);
                    chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                    //새로운 사용자가 초대됐다는 사실을 그 채팅방에 있는 사용자들에게 전달
                    Message message = ChatService.handler.obtainMessage();
                    message.what = INVITE_MESSAGE;
                    message.obj = messageData.toString();
                    ChatService.handler.sendMessage(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "문제가 생겼습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private void addParticipantAddedMessage(String data) {
        HttpRequest httpRequest = new HttpRequest("POST", data, "addparticipantaddedmessage.php", this);
        httpRequest.execute();
    }


    private void addChat(String roomNum, String sender, String receiverString, String message, String type, String messageData, ArrayList imageArrayList, Uri videoUri, String video, int[] videoSize) {
        //필수로 넘어가야 하는 값은 모두 초기화해준다.
        RequestBody roomNumPart = RequestBody.create(MultipartBody.FORM, roomNum);
        RequestBody senderPart = RequestBody.create(MultipartBody.FORM, sender);
        RequestBody participantStringPart = RequestBody.create(MultipartBody.FORM, receiverString);
        RequestBody messagePart = RequestBody.create(MultipartBody.FORM, message);
        RequestBody typePart = RequestBody.create(MultipartBody.FORM, type);
        //필수가 아닌 값들은 선언만 해준다.
        //채팅 메세지
        ArrayList<MultipartBody.Part> imageMultipartBodyList = new ArrayList<>();//이미지 multipartbody를 담을 리스트

        RequestBody videoFile = null;
        MultipartBody.Part videoBody = null;
        File compressedFile = null;

        //레트로핏 인터페이스 설정
        RetrofitService retrofitService;
        Call<AddChatResponse> call;

        //이미지 개수를 담는 변수
        int imageCount = imageArrayList.size();
        //문자 텍스트를 보내는 경우
        if (imageCount == 0 && video == null) {
            for (int i = 0; i < 6; i++) {
                imageMultipartBodyList.add(null);
            }
        }
        //이미지는 보내지 않고 비디오를 보내는 경우
        if (imageCount == 0 && video != null) {
            int width = videoSize[0];
            int height = videoSize[1];
            //동영상을 압축해주는 객체 선언(압축 사이즈 1280X720 or 720X1280 or 720X720)
            CompressMedia compressMedia = new CompressMedia(
                    videoUri.toString(),
                    video,
                    ChatRoomActivity.this,
                    (width > height) ? 1280 : 720,
                    (height > width) ? 1280 : 720);
            String compressedFilePath = null;//압축된 파일의 경로
            try {
                compressedFilePath = compressMedia.startCompress();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            compressedFile = new File(compressedFilePath);//압축된 파일 객체
            videoFile = RequestBody.create(MediaType.parse("multipart/form-data"), compressedFile);
            videoBody = MultipartBody.Part.createFormData("video", video, videoFile);

            for (int i = 0; i < 6; i++) {
                imageMultipartBodyList.add(null);
            }
        }
        //이미지를 보내는 경우
        else if (imageCount != 0 && video == null) {
            imageMultipartBodyList = createImageMultipartBody(imageArrayList);//이미지의 수만큼 multipartbody를 생성
        }

        //레트로핏 세팅
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://13.124.105.47/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitService = retrofit.create(RetrofitService.class);
        call = retrofitService.addChatResponse(roomNumPart, senderPart, participantStringPart, messagePart, typePart, imageMultipartBodyList.get(0), imageMultipartBodyList.get(1), imageMultipartBodyList.get(2), imageMultipartBodyList.get(3), imageMultipartBodyList.get(4), imageMultipartBodyList.get(5), videoBody);

        File finalCompressedFile = compressedFile;
        call.enqueue(new Callback<AddChatResponse>() {
            @Override
            public void onResponse(Call<AddChatResponse> call, Response<AddChatResponse> response) {
                Log.d("레트로핏 통신", "성공");

                if (isNewRoom == true) {//처음 만들어진 방에서 채팅을 처음 보내는 경우
                    isNewRoom = false;//더 이상 새로운 방으로 간주하지 않는다.
                }

                //채팅방에서 처음으로 메세지를 보내는 경우 방을 만들고 메세지를 보내는 것이기 때문에 채팅방을 활성화시켜준다.
                //chatroom 테이블의 activated_participant에 모든 참여자를 다 넣어준다.
                if (chatContentItemArrayList.size() == 1) {
                    avtivateChatRoom(Integer.parseInt(roomNum));//채팅방 활성화
                }
                //이미 만들어져있는 채팅방에 새로운 사용자를 초대하고 처음 메세지를 전송하는 경우
                //chatroom 테이블의 activated_participant필드의 값에 초대된 사용자들을 추가해준다.
                if (chatContentItemArrayList.size() > 1 && chatContentItemArrayList.get(chatContentItemArrayList.size() - 2).getChatType().equals(ChatType.INVITE)) {
                    avtivateChatRoom(Integer.parseInt(roomNum));//채팅방 활성화
                }


                AddChatResponse addChatResponse = response.body();
                //메세지 전송 시간
                String time = addChatResponse.getTime();
                //메세지 미확인자 리스트 문자열
                String unCheckedParticipant = addChatResponse.getUnCheckedParticipant();
                Log.d("미확인자 리스트", unCheckedParticipant);
                //메세지의 id
                String id = addChatResponse.getId();
                Log.d("전송한 채팅 id", id);
                //업로드한 이미지명
                String image1, image2, image3, image4, image5, image6;
                try {


                    //이미지를 uri가 아니라 서버의 url파일명으로 바꿔주기 위해서 이미지 어레이를 미리 비워둔다.
                    chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).getImageList().clear();
                    String[] imageList;
                    //업로드한 이미지를 전체 이미지 리스트에 넣어줘서 이미지를 클릭했을 때 상세보기 페이지에 이미지가 보일 수 있게 해준다.
                    switch (imageCount) {
                        case 0:
                            break;
                        case 1:
                            image1 = addChatResponse.getImage1();
                            imageList = new String[]{image1};
                            //이미지를 전송한 사람의 계정, 닉네임, 프로필,전송 시간, 이미지을 담은 Json객체
                            for (int i = 0; i < imageList.length; i++) {
                                JSONObject contentData = new JSONObject();
                                contentData.put("type", "image");
                                contentData.put("account", loginUser.getAccount());
                                contentData.put("nickname", loginUser.getNickname());
                                contentData.put("profile", loginUser.getProfile());
                                contentData.put("time", time);
                                contentData.put("content", imageList[i]);
                                totalContentArrayList.add(contentData.toString());
                                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).getImageList().add(imageList[i]);
                            }

                            break;
                        case 2:
                            image1 = addChatResponse.getImage1();
                            image2 = addChatResponse.getImage2();
                            imageList = new String[]{image1, image2};
                            for (int i = 0; i < imageList.length; i++) {
                                JSONObject contentData = new JSONObject();
                                contentData.put("type", "image");
                                contentData.put("account", loginUser.getAccount());
                                contentData.put("nickname", loginUser.getNickname());
                                contentData.put("profile", loginUser.getProfile());
                                contentData.put("time", time);
                                contentData.put("content", imageList[i]);
                                totalContentArrayList.add(contentData.toString());
                                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).getImageList().add(imageList[i]);
                            }
                            break;
                        case 3:
                            image1 = addChatResponse.getImage1();
                            image2 = addChatResponse.getImage2();
                            image3 = addChatResponse.getImage3();
                            imageList = new String[]{image1, image2, image3};
                            for (int i = 0; i < imageList.length; i++) {
                                JSONObject contentData = new JSONObject();
                                contentData.put("type", "image");
                                contentData.put("account", loginUser.getAccount());
                                contentData.put("nickname", loginUser.getNickname());
                                contentData.put("profile", loginUser.getProfile());
                                contentData.put("time", time);
                                contentData.put("content", imageList[i]);
                                totalContentArrayList.add(contentData.toString());
                                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).getImageList().add(imageList[i]);
                            }
                            break;
                        case 4:
                            image1 = addChatResponse.getImage1();
                            image2 = addChatResponse.getImage2();
                            image3 = addChatResponse.getImage3();
                            image4 = addChatResponse.getImage4();
                            imageList = new String[]{image1, image2, image3, image4};
                            for (int i = 0; i < imageList.length; i++) {
                                JSONObject contentData = new JSONObject();
                                contentData.put("type", "image");
                                contentData.put("account", loginUser.getAccount());
                                contentData.put("nickname", loginUser.getNickname());
                                contentData.put("profile", loginUser.getProfile());
                                contentData.put("time", time);
                                contentData.put("content", imageList[i]);
                                totalContentArrayList.add(contentData.toString());
                                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).getImageList().add(imageList[i]);
                            }
                            break;
                        case 5:
                            image1 = addChatResponse.getImage1();
                            image2 = addChatResponse.getImage2();
                            image3 = addChatResponse.getImage3();
                            image4 = addChatResponse.getImage4();
                            image5 = addChatResponse.getImage5();
                            imageList = new String[]{image1, image2, image3, image4, image5};
                            for (int i = 0; i < imageList.length; i++) {
                                JSONObject contentData = new JSONObject();
                                contentData.put("type", "image");
                                contentData.put("account", loginUser.getAccount());
                                contentData.put("nickname", loginUser.getNickname());
                                contentData.put("profile", loginUser.getProfile());
                                contentData.put("time", time);
                                contentData.put("content", imageList[i]);
                                totalContentArrayList.add(contentData.toString());
                                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).getImageList().add(imageList[i]);
                            }
                            break;
                        case 6:
                            image1 = addChatResponse.getImage1();
                            image2 = addChatResponse.getImage2();
                            image3 = addChatResponse.getImage3();
                            image4 = addChatResponse.getImage4();
                            image5 = addChatResponse.getImage5();
                            image6 = addChatResponse.getImage6();
                            imageList = new String[]{image1, image2, image3, image4, image5, image6};
                            for (int i = 0; i < imageList.length; i++) {
                                JSONObject contentData = new JSONObject();
                                contentData.put("type", "image");
                                contentData.put("account", loginUser.getAccount());
                                contentData.put("nickname", loginUser.getNickname());
                                contentData.put("profile", loginUser.getProfile());
                                contentData.put("time", time);
                                contentData.put("content", imageList[i]);
                                totalContentArrayList.add(contentData.toString());
                                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).getImageList().add(imageList[i]);
                            }
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //서버 이미지로 세팅
                if (video == null) {
                    chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).setIsImageFromServer(true);
                }
                //서버 동영상으로 세팅
                else {
                    chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).setIsVideoFromServer(true);
                    chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).setVideo(video);
                    JSONObject contentData = new JSONObject();
                    try {
                        contentData.put("type", "video");
                        contentData.put("account", loginUser.getAccount());
                        contentData.put("nickname", loginUser.getNickname());
                        contentData.put("profile", loginUser.getProfile());
                        contentData.put("time", time);
                        contentData.put("content", video);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    totalContentArrayList.add(contentData.toString());
                    finalCompressedFile.delete();//압축된 동영상 파일 삭제
                }
                //전송 완료 상태로 셋
                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).setIsSent(true);
                //전송 시간 셋
                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).setTime(time);
                //미확인자 수 셋
                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).setUnCheckedParticipant(unCheckedParticipant);
                //아이템 리스트에 아이템이 두개 이상이 되면 메세지의 전송 시간을 비교해서 두 아이템의 전송 날짜가 다른 경우
                //시간 구분 아이템을 함께 추가해준다.
                if (chatContentItemArrayList.size() >= 2) {
                    //지금 보낸 메세지가 가장 늦은 시간
                    String laterTime = ChatContentDetailActivity.formatDate(time, fromFormat, toFormat);
                    //메세지를 보내기 전에 있었던 가장 최하단의 메세지
                    String earlierTime = ChatContentDetailActivity.formatDate(chatContentItemArrayList.get(chatContentItemArrayList.size() - 2).getTime(), fromFormat, toFormat);
                    //연, 월, 일이 다르면 다른 날에 전송된 아이템이기 때문에 시간 구분 아이템을 추가하여 아이템에 구분을 준다.
                    if (!earlierTime.equals(laterTime)) {
                        ChatContentItem chatContentItem = new ChatContentItem();
                        chatContentItem.setTime(chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).getTime());
                        chatContentItem.setChatType(ChatType.TIMEDIVIDER);
                        chatContentItemArrayList.add(chatContentItemArrayList.size() - 1, chatContentItem);
                        chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                    }
                }
                chatContentAdapter.notifyItemChanged(chatContentItemArrayList.size() - 1);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //메세지 데이터에 채팅 id와 전송 시간을 함께 담아서 전송
                        try {
                            JSONObject jsonObject = new JSONObject(messageData);
                            jsonObject.put("id", id);
                            jsonObject.put("time", time);
                            //채팅 서비스 클래스의 핸들러로 보낼 메세지 객체
                            Message message = ChatService.handler.obtainMessage();
                            //메세지 식별자를 1111로 설정
                            message.what = 1111;
                            //메세지 내용을 넣는다.
                            message.obj = jsonObject.toString();
                            //채팅 서비스 클래스로 메세지를 보낸다.
                            ChatService.handler.sendMessage(message);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();

                imageArrayList.clear();
                imageNameForServerList.clear();
            }

            @Override
            public void onFailure(Call<AddChatResponse> call, Throwable t) {
                Log.d("레트로핏 에러", t.getMessage());
                imageNameForServerList.clear();
                if (video != null) {
                    finalCompressedFile.delete();//압축된 동영상 파일 삭제
                }
            }
        });


    }

    private ArrayList createImageMultipartBody(ArrayList<String> imageArrayList) {//로컬 단말기의 uri가 담긴 리스트
        ArrayList<MultipartBody.Part> imageMultipartBodyList = new ArrayList<>();//이미지 멀티파트 리스트
        //이미지 처리 객체 초기화
        ProcessImage processImage = new ProcessImage(ChatRoomActivity.this);
        for (int i = 0; i < imageArrayList.size(); i++) {
            String imageFileName = imageNameForServerList.get(i);//서버의 이미지 파일 명
            File imageFile = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(i))), String.valueOf(imageArrayList.get(i)));
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);//리퀘스트 body
            MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("image" + (i + 1), imageFileName, requestBody);
            imageMultipartBodyList.add(multipartBody);//리스트에 추가
        }

        if (imageArrayList.size() != 6) {
            for (int i = imageArrayList.size(); i < 6; i++) {
                imageMultipartBodyList.add(null);
            }
        }

        return imageMultipartBodyList;
    }


    @Override
    protected void onResume() {
        Log.d("채팅화면 onResume", "호출");
        super.onResume();
        //채팅 내용을 서버로부터 가져오는 asynctask
        if (totalContentArrayList.isEmpty()) {//전체 이미지 리스트에 값이 없으면 최초 로드로 간주해서 서버에서 전체 이미지 데이터를 가져온다.
            getChatContent(roomNum, loginUser.getAccount(), 0, listSize, true);
        } else {
            getChatContent(roomNum, loginUser.getAccount(), 0, listSize, false);
        }
    }

    //채팅방의 이미지를 클릭했을 때 호출되는 메소드
    @Override
    public void onContentClicked(int position, int contentPosition) {

        Intent intent = new Intent(this, ChatContentDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        //채팅창의 전체 컨텐츠 수
        intent.putExtra("contentListCount", totalContentArrayList.size());
        for (int i = 0; i < totalContentArrayList.size(); i++) {
            //전체 컨텐츠 데이터(json스트링)
            intent.putExtra("contentData" + i, totalContentArrayList.get(i));
        }
        int index = 0;//인덱스
        String content = null;//클릭한 content의 파일 명
        if (chatContentItemArrayList.get(position).getType().equals("image")) {//클릭한 컨텐츠가 이미지 컨텐츠인 경우
            content = chatContentItemArrayList.get(position).getImageList().get(contentPosition);//선택한 이미지 파일 명
            intent.putExtra("type", "image");//컨텐츠 타입을 image로 설정
        } else {//동영상 컨텐츠인 경우
            content = chatContentItemArrayList.get(position).getVideo();//선택한 동영상 파일 명
            intent.putExtra("type", "video");//컨텐츠 타입을 video로 설정
        }
        //전체 컨텐츠 데이터를 돌면서
        for (int i = 0; i < totalContentArrayList.size(); i++) {
            try {
                //이미지의 파일명을 꺼내서 클릭한 이미지 파일 명과 같은지 비교를 해서
                JSONObject imageData = new JSONObject(totalContentArrayList.get(i));
                if (imageData.getString("content").equals(content)) {
                    //같은 경우 그 이미지의 인덱스를 넣어준다.
                    index = i;
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        intent.putExtra("contentIndex", index);//클릭한 컨텐츠의 index
        startActivity(intent);
    }

    //상대방 프로필 사진 클릭시 호출되는 메소드
    @Override
    public void onProfileClicked(int position) {
        Dialog dialog = new Dialog(ChatRoomActivity.this);
        dialog.setContentView(R.layout.call_chat_box);

        //다이얼로그의 크기 조정
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        Window window = dialog.getWindow();
        window.setAttributes(layoutParams);

        CircleImageView cv_profile;
        TextView tv_nickname, tv_call, tv_chat, tv_cancel;

        cv_profile = dialog.findViewById(R.id.circleimageview_profile);
        tv_nickname = dialog.findViewById(R.id.textview_nickname);
        tv_call = dialog.findViewById(R.id.textview_call);
        tv_chat = dialog.findViewById(R.id.textview_chat);
        tv_cancel = dialog.findViewById(R.id.textview_cancel);

        //프로필사진 설정
        Glide.with(getApplicationContext())
                .load("http://13.124.105.47/profileimage/" + chatContentItemArrayList.get(position).getProfile())
                .thumbnail(0.1f)
                .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                .into(cv_profile);

        //닉네임 설정
        tv_nickname.setText(chatContentItemArrayList.get(position).getNickname());

        //1:1채팅의 경우 이미 1:1채팅방에 들어와있는 경우에는 버튼을 없애준다.
        if (chatParticipantItemArrayList.size() < 2) {
            tv_chat.setVisibility(View.GONE);
        }

        //영상통화 버튼 리스너
        tv_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //권한 체크
                TedPermission.with(getApplicationContext())
                        .setPermissionListener(new PermissionListener() {
                            //권한 허가가 됐을 때 콜백
                            @Override
                            public void onPermissionGranted() {
                                //소켓서버를 통해서 수신자에게 영상통화 요청을 전달
                                try {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("type", "requestFaceChat");
                                    jsonObject.put("roomName", loginUser.getAccount() + chatContentItemArrayList.get(position).getAccount());//영상통화의 방 이름을 보내준다. 이때 방은 두 peer의 계정을 합쳐서 구성한다.
                                    jsonObject.put("account", loginUser.getAccount());//발신자 계정
                                    jsonObject.put("nickname", loginUser.getNickname());//발신자 닉네임
                                    jsonObject.put("profile", loginUser.getProfile());//발신자 프로필 사진
                                    jsonObject.put("receiver", chatContentItemArrayList.get(position).getAccount());//수신자 계정
                                    //소켓 서버로 영상통화 토큰을 날려준다.
                                    Message message = ChatService.handler.obtainMessage();
                                    message.what = FACECALL_REQUEST_RECEIVED;
                                    message.obj = jsonObject.toString();
                                    ChatService.handler.sendMessage(message);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            //권한 허가 거부가 됐을 때 콜백
                            @Override
                            public void onPermissionDenied(List<String> deniedPermissions) {

                            }
                        })
                        .setDeniedMessage("권한을 허가하지 않으면 영상통화 기능을 사용할 수 없습니다.\n만약 기능을 사용하고 싶다면 설정에서 권한을 허용해주세요.")
                        .setPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS})
                        .check();

                dialog.dismiss();

            }
        });

        //1:1채팅 버튼 리스너
        tv_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //chatactivity로 intent
                startActivity(new Intent(getApplicationContext(), ChatActivity.class)
                        .putExtra("isFromChatRoom", true)
                        .putExtra("selectedUserAccount", chatContentItemArrayList.get(position).getAccount())
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        //닫기 버튼 구현
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    //채팅방 서랍의 대화상대 추가 or 참여자 목록을 클릭하면 호출되는 메소드
    @Override
    public void onContainerClicked(int position) {
        if (position == 0) {//대화상대 추가 아이템 클릭
            Log.d("대화상대 추가", "클릭");
            Intent intent = new Intent(ChatRoomActivity.this, AddChatParticipantActivity.class);
            //이미 채팅방에 참여하고 있는 사용자들의 계정을 담은 스트링을 함께 전달해준다.
            String participantStirng = null;
            if (chatParticipantItemArrayList.size() == 0) {
                participantStirng = "null";
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                    String account = chatParticipantItemArrayList.get(i).getAccount();
                    if (i == 0) {
                        stringBuilder.append(account);
                    } else {
                        stringBuilder.append("/" + account);
                    }
                }
                participantStirng = stringBuilder.toString();
            }

            intent.putExtra("participantString", participantStirng);
            startActivityForResult(intent, 0);
        } else {//참여자 아이템 클릭
            Log.d("참여자 목록", "클릭");
            Dialog dialog = new Dialog(ChatRoomActivity.this);
            dialog.setContentView(R.layout.call_chat_box);

            //다이얼로그의 크기 조정
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            Window window = dialog.getWindow();
            window.setAttributes(layoutParams);

            CircleImageView cv_profile;
            TextView tv_nickname, tv_call, tv_chat, tv_cancel;

            cv_profile = dialog.findViewById(R.id.circleimageview_profile);
            tv_nickname = dialog.findViewById(R.id.textview_nickname);
            tv_call = dialog.findViewById(R.id.textview_call);
            tv_chat = dialog.findViewById(R.id.textview_chat);
            tv_cancel = dialog.findViewById(R.id.textview_cancel);

            //프로필사진 설정
            Glide.with(getApplicationContext())
                    .load("http://13.124.105.47/profileimage/" + chatParticipantItemArrayList.get(position - 1).getProfile())
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(cv_profile);

            //닉네임 설정
            tv_nickname.setText(chatParticipantItemArrayList.get(position - 1).getNickname());

            //1:1채팅의 경우 이미 1:1채팅방에 들어와있는 경우에는 버튼을 없애준다.
            if (chatParticipantItemArrayList.size() < 2) {
                tv_chat.setVisibility(View.GONE);
            }


            //영상통화 버튼 리스너
            tv_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //권한 체크
                    TedPermission.with(getApplicationContext())
                            .setPermissionListener(new PermissionListener() {
                                //권한 허가가 됐을 때 콜백
                                @Override
                                public void onPermissionGranted() {
                                    //소켓서버를 통해서 수신자에게 영상통화 요청을 전달
                                    try {
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("type", "requestFaceChat");
                                        jsonObject.put("roomName", loginUser.getAccount() + chatParticipantItemArrayList.get(position - 1).getAccount());//영상통화의 방 이름을 보내준다. 이때 방은 두 peer의 계정을 합쳐서 구성한다.
                                        jsonObject.put("account", loginUser.getAccount());//발신자 계정
                                        jsonObject.put("nickname", loginUser.getNickname());//발신자 닉네임
                                        jsonObject.put("profile", loginUser.getProfile());//발신자 프로필 사진
                                        jsonObject.put("receiver", chatParticipantItemArrayList.get(position - 1).getAccount());//수신자 계정
                                        //소켓 서버로 영상통화 토큰을 날려준다.
                                        Message message = ChatService.handler.obtainMessage();
                                        message.what = FACECALL_REQUEST_RECEIVED;
                                        message.obj = jsonObject.toString();
                                        ChatService.handler.sendMessage(message);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    drawerLayout.closeDrawer(GravityCompat.END, true);
                                }

                                //권한 허가 거부가 됐을 때 콜백
                                @Override
                                public void onPermissionDenied(List<String> deniedPermissions) {

                                }
                            })
                            .setDeniedMessage("권한을 허가하지 않으면 영상통화 기능을 사용할 수 없습니다.\n만약 기능을 사용하고 싶다면 설정에서 권한을 허용해주세요.")
                            .setPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS})
                            .check();

                    dialog.dismiss();

                }
            });

            //1:1채팅 버튼 리스너
            tv_chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    //chatactivity로 intent
                    startActivity(new Intent(getApplicationContext(), ChatActivity.class)
                            .putExtra("isFromChatRoom", true)
                            .putExtra("selectedUserAccount", chatParticipantItemArrayList.get(position - 1).getAccount())
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
            });

            //닫기 버튼 구현
            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {

            //사용자 추가 액티비티에서 넘어온 사용자 데이터를 담은 json스트링
            //ex) "{"participantList": [{"account":"anstn1993","nickname":"만수","profile":"..."},{...},{...}]}"
            String addedParticipantString = data.getStringExtra("participantList");
            try {
                JSONObject jsonObject = new JSONObject(addedParticipantString);
                JSONArray participantList = jsonObject.getJSONArray("participantList");
                //초대된 사용자들을 chatroom 테이블의 participant필드의 데이터에 추가해주는 로직 실행
                addChatParticipant(roomNum, participantList);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onBackPressed() {
        //채팅방 서랍이 열려있는 경우
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            //채팅방 서랍을 닫아준다.
            drawerLayout.closeDrawer(GravityCompat.END);
        }
        //채팅방 서랍이 열려있지 않은 경우
        else {
            //방을 만들고 채팅을 보내지 않은 상태에서 그냥 나가는 경우
            if (chatContentItemArrayList.size() == 0) {
                //채팅 방을 삭제
                deleteChatRoom(roomNum);
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("type", "removechatroom");
                    jsonObject.put("roomNum", roomNum);
                    //소켓 서버로 채팅방 삭제 토큰을 날러서 채팅방 리스트에서 채팅방을 지워준다.
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Message message = ChatService.handler.obtainMessage();
                            message.what = CHECK_MESSAGE;
                            message.obj = jsonObject.toString();
                            ChatService.handler.sendMessage(message);
                        }
                    }).start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            super.onBackPressed();
        }
    }
}


