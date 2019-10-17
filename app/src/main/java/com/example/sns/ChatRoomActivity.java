package com.example.sns;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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

import static com.example.sns.LoginActivity.account;
import static com.example.sns.LoginActivity.nickname;
import static com.example.sns.LoginActivity.profile;

public class ChatRoomActivity extends AppCompatActivity implements ChatContentAdapter.ChatContentRecyclerViewListener, ParticipantListAdapter.ParticipantListRecyclerViewListener, HttpRequest.OnHttpResponseListener {
    private String TAG = "ChatRoomActivity";
    private EditText et_chat;//댓글 입력 창
    private TextView tv_send, tv_participant, tv_messageCount, tv_newMessage;
    private Context context;
    private ImageButton ib_back, ib_addContent, ib_setting;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;



    //채팅방 번호
    public static int roomNum;
    //미확인 메세지 수
    int newMessageCount;
    //최초 로드 시에 가져올 채팅 아이템의 수
    int listSize;

    public static Handler handler;

    //전송할 이미지의 경로를 담을 어레이
    private ArrayList<String> imageArrayList;
    //서버로 전송할 때 사용될 이미지 파일의 이름을 담을 리스트
    private ArrayList<String> imageNameForServerList;
    //이 채팅방에 존재하는 모든 이미지 파일의 이름을 담을 리스트
    private ArrayList<String> totalImageArrayList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("채팅방 화면 onCreate", "호출");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        //와이파이 상태를 체크할 때 context정보가 필요하다
        context = this;
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
                    String nickname = chatParticipantItemArrayList.get(i).nickname;
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
        totalImageArrayList = new ArrayList<>();


        //채팅 서비스에서 넘어온 메세지를 받는 핸들러
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.d(TAG, msg.obj.toString());
                //채팅 내용이 문자인 경우
                if (msg.what == 1111) {
                    try {
                        String jsonString = msg.obj.toString();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        int roomNum_ = jsonObject.getInt("roomNum");
                        //넘어온 메세지가 현재 채팅방의 메세지인 경우에만 화면에 표시를 해준다.
                        if (roomNum == roomNum_) {
                            String account = jsonObject.getString("account");
                            String nickname = jsonObject.getString("nickname");
                            String profile = jsonObject.getString("profile");
                            String message = jsonObject.getString("message");
                            String time = jsonObject.getString("time");
                            //아이템 리스트에 아이템이 두개 이상이 되면 메세지의 전송 시간을 비교해서 두 아이템의 전송 날짜가 다른 경우
                            //시간 구분 아이템을 함께 추가해준다.
                            if (chatContentItemArrayList.size() >= 1) {
                                //지금 받은 메세지가 가장 늦은 시간
                                String laterTime = ChatImageDetailActivity.formatDate(time, fromFormat, toFormat);
                                //메세지를 받기 전에 있었던 가장 최하단의 메세지
                                String earlierTime = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).time, fromFormat, toFormat);
                                //연, 월, 일이 다르면 다른 날에 전송된 아이템이기 때문에 시간 구분 아이템을 추가하여 아이템에 구분을 준다.
                                if (!earlierTime.equals(laterTime)) {
                                    ChatContentItem chatContentItem = new ChatContentItem();
                                    chatContentItem.setTime(time);
                                    chatContentItem.setTimeDivider(true);
                                    chatContentItemArrayList.add(chatContentItem);
                                    chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                                }
                            }
                            boolean isMyContent = false;
                            boolean isSent = true;
                            ChatContentItem chatContentItem = new ChatContentItem();
                            chatContentItem.setRoomNum(roomNum_);
                            chatContentItem.setAccount(account);
                            chatContentItem.setNickname(nickname);
                            chatContentItem.setProfile(profile);
                            chatContentItem.setMessage(message);
                            chatContentItem.setMyContent(isMyContent);
                            chatContentItem.setSent(isSent);
                            chatContentItem.setTime(time);
                            //미확인자 리스트 스트링을 송신자의 계정을 빼고 만들어준다.
                            //최종적으로 아이템에 셋을 해줄 미확인자 리스트 스트링
                            String unCheckedParticipant = "null";
                            //송신자의 계정은 제외한다.
                            boolean isFirst = true;
                            boolean isAdded = false;
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                                if (!chatParticipantItemArrayList.get(i).account.equals(account)) {
                                    //송신자의 계정이 아닌 첫번째 계정인 경우
                                    if (isFirst) {
                                        stringBuilder.append(chatParticipantItemArrayList.get(i).account);
                                        isFirst = false;
                                    }
                                    //첫번째 계정이 아닌 경우
                                    else {
                                        stringBuilder.append("/" + chatParticipantItemArrayList.get(i).account);
                                    }
                                    isAdded = true;
                                }

                                if(isAdded) {//미확인 계정이 추가됐을 때만 stringBuilder로 값을 바꿔준다.
                                    unCheckedParticipant = stringBuilder.toString();
                                }
                            }
                            chatContentItem.setUnCheckedParticipant(unCheckedParticipant);


                            //현재 완전히 보이는 채팅 아이템이 최신 채팅 아이템인 경우
                            if(linearLayoutManager.findLastCompletelyVisibleItemPosition() == chatContentItemArrayList.size()-1) {
                                chatContentItemArrayList.add(chatContentItem);
                                chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                                //채팅 리사이클러뷰를 최하단으로 스크롤
                                rv_chatContent.scrollToPosition(chatContentItemArrayList.size() - 1);
                            }
                            //현재 완전히 보이는 채팅 아이템이 최신 채팅 아이템이 아닌 경우
                            else {
                                chatContentItemArrayList.add(chatContentItem);
                                chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                                tv_newMessage.setText(nickname+": "+message);//새로운 메세지 박스에 메세지를 set
                                tv_newMessage.setVisibility(View.VISIBLE);//새로운 메세지 박스 visible
                            }
                        }
                        //다른 채팅방의 메세지인 경우에는 안 읽은 메세지 수를 추가해준다.
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
                //채팅 내용이 이미지인 경우
                else if (msg.what == 2222) {
                    Log.d("채팅화면에 넘어온 메세지", msg.obj.toString());
                    try {
                        String jsonString = msg.obj.toString();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        int roomNum_ = jsonObject.getInt("roomNum");
                        //넘어온 메세지가 현재 채팅방의 메세지인 경우에만 화면에 표시를 해준다.
                        if (roomNum == roomNum_) {
                            String account = jsonObject.getString("account");
                            String nickname = jsonObject.getString("nickname");
                            String profile = jsonObject.getString("profile");
                            String time = jsonObject.getString("time");
                            //넘어온 이미지의 파일명을 담는 json배열
                            JSONArray imageArray = jsonObject.getJSONArray("imageList");
                            //채팅 아이템에 셋해줄 이미지 어레이
                            ArrayList<String> imageArrayList = new ArrayList<>();
                            //각 파일명을 어레이에 담아서 채팅 아이템에 셋해준다.
                            for (int i = 0; i < imageArray.length(); i++) {
                                imageArrayList.add(imageArray.getString(i));
                            }
                            //아이템 리스트에 아이템이 두개 이상이 되면 메세지의 전송 시간을 비교해서 두 아이템의 전송 날짜가 다른 경우
                            //시간 구분 아이템을 함께 추가해준다.
                            if (chatContentItemArrayList.size() >= 1) {
                                //지금 받은 메세지가 가장 늦은 시간
                                String laterTime = ChatImageDetailActivity.formatDate(time, fromFormat, toFormat);
                                //메세지를 받기 전에 있었던 가장 최하단의 메세지
                                String earlierTime = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).time, fromFormat, toFormat);
                                //연, 월, 일이 다르면 다른 날에 전송된 아이템이기 때문에 시간 구분 아이템을 추가하여 아이템에 구분을 준다.
                                if (!earlierTime.equals(laterTime)) {
                                    ChatContentItem chatContentItem = new ChatContentItem();
                                    chatContentItem.setTime(time);
                                    chatContentItem.setTimeDivider(true);
                                    chatContentItemArrayList.add(chatContentItem);
                                    chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                                }
                            }
                            boolean isMyContent = false;
                            boolean isSent = true;
                            ChatContentItem chatContentItem = new ChatContentItem();
                            chatContentItem.setRoomNum(roomNum_);
                            chatContentItem.setAccount(account);
                            chatContentItem.setNickname(nickname);
                            chatContentItem.setProfile(profile);
                            chatContentItem.setImageList(imageArrayList);
                            chatContentItem.setMyContent(isMyContent);
                            chatContentItem.setSent(isSent);
                            chatContentItem.setTime(time);
                            //미확인자 리스트 스트링을 송신자의 계정을 빼고 만들어준다.
                            //최종적으로 아이템에 셋을 해줄 미확인자 리스트 스트링
                            String unCheckedParticipant = "null";
                            //송신자의 계정은 제외한다.
                            boolean isFirst = true;
                            boolean isAdded = false;
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                                if (!chatParticipantItemArrayList.get(i).account.equals(account)) {
                                    //송신자의 계정이 아닌 첫번째 계정인 경우
                                    if (isFirst) {
                                        stringBuilder.append(chatParticipantItemArrayList.get(i).account);
                                        isFirst = false;
                                    }
                                    //첫번째 계정이 아닌 경우
                                    else {
                                        stringBuilder.append("/" + chatParticipantItemArrayList.get(i).account);
                                    }
                                    isAdded = true;
                                }
                            }
                            if(isAdded) {
                                unCheckedParticipant = stringBuilder.toString();
                            }
                            chatContentItem.setUnCheckedParticipant(unCheckedParticipant);

                            //현재 완전히 보이는 채팅 아이템이 최신 채팅 아이템인 경우
                            if(linearLayoutManager.findLastCompletelyVisibleItemPosition() == chatContentItemArrayList.size()-1) {
                                chatContentItemArrayList.add(chatContentItem);
                                chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                                //채팅 리사이클러뷰를 최하단으로 스크롤
                                rv_chatContent.scrollToPosition(chatContentItemArrayList.size() - 1);
                            }
                            //현재 완전히 보이는 채팅 아이템이 최신 채팅 아이템이 아닌 경우
                            else {
                                chatContentItemArrayList.add(chatContentItem);
                                chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                                tv_newMessage.setText(nickname+": 새로운 사진을 보냈습니다.");//새로운 메세지 박스에 메세지를 set
                                tv_newMessage.setVisibility(View.VISIBLE);//새로운 메세지 박스 visible
                            }
                            //사진 상세보기 페이지에서 이미지를 볼 수 있게 해주기 위해 채팅방의 전체 이미지 리스트에 추가를 해준다.
                            //ex) "{"account":"rangkim", "nickname":"뢩킴","profile":"...","time":"...","imageList":["image1","image2"...]}"
                            JSONObject imageData = new JSONObject();

                            //사용자가 전송한 이미지를 담을 json어레이
                            for (int i = 0; i < imageArrayList.size(); i++) {
                                imageData.put("account", account);
                                imageData.put("nickname", nickname);
                                imageData.put("profile", profile);
                                imageData.put("time", time);
                                imageData.put("image", imageArrayList.get(i));
                                totalImageArrayList.add(imageData.toString());
                            }
                        }
                        //다른 채팅방의 메세지인 경우에는 안 읽은 메세지 수를 추가해준다.
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
                //내가 보낸 메세지를 확인했다는 메세지를 받은 경우
                else if (msg.what == 3333) {
                    Log.d("메세지 확인", msg.obj.toString());

                    try {
                        //확인 메세지 데이터를 json객체로 변환해준다.
                        JSONObject checkData = new JSONObject(msg.obj.toString());
                        //방 번호
                        int roomNum_ = checkData.getInt("roomNum");
                        //나의 메세지를 확인한 사용자의 계정
                        String checkedAccount = checkData.getString("receiver");
                        //메세지 미확인자 스트링 변수를 업데이트 해준다.
                        for (int i = 0; i < chatContentItemArrayList.size(); i++) {
                            //채팅 메세지 전체를 돌면서 메세지 미확인자 리스트에 메세지를 확인한 사용자의 계정이 존재하는지 파악해서
                            //존재하는 경우 그 계정을 지워준다.
                            if (!chatContentItemArrayList.get(i).isTimeDivider && !chatContentItemArrayList.get(i).isExit && !chatContentItemArrayList.get(i).isAddedParticipantMessage) {
                                //메세지를 전송한 사람의 계정이 메세지 아이템의 주인 계정과 같지 않은 경우에만 수행한다.
                                if (chatContentItemArrayList.get(i).unCheckedParticipant.contains(checkedAccount) && !chatContentItemArrayList.get(i).account.equals(checkedAccount)) {
                                    Log.d("확인한 사람", checkedAccount);
                                    String unCheckedParticipant = chatContentItemArrayList.get(i).unCheckedParticipant;
                                    String[] unCheckedParticipantList = chatContentItemArrayList.get(i).unCheckedParticipant.split("/");
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
                                    chatContentAdapter.notifyItemChanged(i, "checked");
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                //특정 사용자가 입장했다는 메세지를 받는 경우
                else if (msg.what == 4444) {
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
                            //메세지 미확인자 스트링 변수를 업데이트 해준다.
                            for (int i = 0; i < chatContentItemArrayList.size(); i++) {
                                //채팅 메세지 전체를 돌면서 메세지 미확인자 리스트에 메세지를 확인한 사용자의 계정이 존재하는지 파악해서
                                //존재하는 경우 그 계정을 지워준다.
                                if (!chatContentItemArrayList.get(i).isTimeDivider && !chatContentItemArrayList.get(i).isExit && !chatContentItemArrayList.get(i).isAddedParticipantMessage) {
                                    //메세지를 전송한 사람의 계정이 메세지 아이템의 주인 계정과 같지 않은 경우에만 수행한다.
                                    if (chatContentItemArrayList.get(i).unCheckedParticipant.contains(enteredAccount) && !chatContentItemArrayList.get(i).account.equals(enteredAccount)) {
                                        Log.d("확인한 사람", enteredAccount);
                                        String unCheckedParticipant = chatContentItemArrayList.get(i).unCheckedParticipant;
                                        String[] unCheckedParticipantList = chatContentItemArrayList.get(i).unCheckedParticipant.split("/");
                                        //만약 미확인 리스트에 메세지를 확인한 사용자의 계정만 남아있으면 바로 null처리를 해준다.
                                        if (unCheckedParticipantList.length == 1) {
                                            chatContentItemArrayList.get(i).setUnCheckedParticipant("null");
                                        }
                                        //아직 복수의 사용자가 남아있다면 리스트 안에서 메세지를 확인한 사용자 계정의 index를 파악해서 index별로 문자열을 다르게 제거한다.
                                        else {
                                            //메세지를 확인한 사용자 계정의 인덱스
                                            int index = 0;
                                            for (int j = 0; j < unCheckedParticipantList.length; j++) {
                                                if (unCheckedParticipantList[j].equals(enteredAccount)) {
                                                    index = j;
                                                    break;
                                                }
                                            }
                                            //메세지를 확인한 사용자 계정이 리스트의 마지막에 있는 경우
                                            if (index == unCheckedParticipantList.length - 1) {
                                                unCheckedParticipant = unCheckedParticipant.replace("/" + enteredAccount, "");
                                            }
                                            //리스트의 처음이나 중간에 있는 경우
                                            else {
                                                unCheckedParticipant = unCheckedParticipant.replace(enteredAccount + "/", "");
                                            }
                                            chatContentItemArrayList.get(i).setUnCheckedParticipant(unCheckedParticipant);
                                        }
                                        chatContentAdapter.notifyItemChanged(i, "checked");
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //특정 참여자가 채팅방을 나갔다는 메세지
                else if (msg.what == 5555) {
                    try {
                        String jsonString = msg.obj.toString();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        //방 번호
                        int roomNum_ = jsonObject.getInt("roomNum");
                        //넘어온 메세지가 현재 채팅방의 메세지인 경우에만 화면에 표시를 해준다.
                        if (roomNum == roomNum_) {
                            //채팅방을 나간 사용자 계정
                            String exitAccount = jsonObject.getString("account");
                            //닉네임
                            String exitNickname = jsonObject.getString("nickname");
                            //프로필 사진 명
                            String exitProfile = jsonObject.getString("profile");
                            //메세지
                            String message = jsonObject.getString("message");
                            //전송된 시간
                            String time = jsonObject.getString("time");
                            //아이템 리스트에 아이템이 두개 이상이 되면 메세지의 전송 시간을 비교해서 두 아이템의 전송 날짜가 다른 경우
                            //시간 구분 아이템을 함께 추가해준다.
                            if (chatContentItemArrayList.size() >= 1) {
                                //지금 받은 메세지가 가장 늦은 시간
                                String laterTime = ChatImageDetailActivity.formatDate(time, fromFormat, toFormat);
                                //메세지를 받기 전에 있었던 가장 최하단의 메세지
                                String earlierTime = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).time, fromFormat, toFormat);
                                //연, 월, 일이 다르면 다른 날에 전송된 아이템이기 때문에 시간 구분 아이템을 추가하여 아이템에 구분을 준다.
                                if (!earlierTime.equals(laterTime)) {
                                    ChatContentItem chatContentItem = new ChatContentItem();
                                    chatContentItem.setTime(time);
                                    chatContentItem.setTimeDivider(true);
                                    chatContentItemArrayList.add(chatContentItem);
                                    chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                                }
                            }
                            boolean isMyContent = false;
                            boolean isSent = true;
                            boolean isExit = true;
                            ChatContentItem chatContentItem = new ChatContentItem();
                            chatContentItem.setRoomNum(roomNum_);
                            chatContentItem.setAccount(exitAccount);
                            chatContentItem.setNickname(exitNickname);
                            chatContentItem.setProfile(exitProfile);
                            chatContentItem.setMessage(message);
                            chatContentItem.setMyContent(isMyContent);
                            chatContentItem.setExit(isExit);
                            chatContentItem.setSent(isSent);
                            chatContentItem.setTime(time);

                            chatContentItemArrayList.add(chatContentItem);
                            chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                            rv_chatContent.scrollToPosition(chatContentItemArrayList.size() - 1);

                            //참여자 수를 로드된 채팅 아이템에 모두 반영해준다. 동시에 채팅 미확인자 목록에 나간 사용자가 포함되면 삭제해준다.
                            for (int i = 0; i < chatContentItemArrayList.size(); i++) {
                                //채팅 아이템이 상대방이 나간 메세지 혹은 시간 경계 아이템인 경우에는 해당 로직을 실행하지 않는다.
                                if (!chatContentItemArrayList.get(i).isExit && !chatContentItemArrayList.get(i).isTimeDivider && !chatContentItemArrayList.get(i).isAddedParticipantMessage) {
                                    if (chatContentItemArrayList.get(i).unCheckedParticipant.contains(exitAccount)) {
                                        String unCheckedParticipant = chatContentItemArrayList.get(i).unCheckedParticipant;
                                        String[] unCheckedParticipantList = unCheckedParticipant.split("/");
                                        //미확인자 리스트에 나간 사람의 계정만 있는 경우 바로 null처리
                                        if (unCheckedParticipantList.length == 1) {
                                            chatContentItemArrayList.get(i).setUnCheckedParticipant("null");
                                        }
                                        //다른 사용자도 존재하는 경우
                                        else {
                                            //확인자 리스트 안에서 나간 사용자의 계정 index
                                            int index = 0;
                                            for (int j = 0; j < unCheckedParticipantList.length; j++) {
                                                if (unCheckedParticipantList[j].equals(exitAccount)) {
                                                    index = j;
                                                    break;
                                                }
                                            }
                                            //나간 사용자가 리스트의 마지막에 있는 경우
                                            if (index == unCheckedParticipantList.length - 1) {
                                                unCheckedParticipant = unCheckedParticipant.replace("/" + exitAccount, "");
                                            }
                                            //리스트의 처음이나 중간에 있는 경우
                                            else {
                                                unCheckedParticipant = unCheckedParticipant.replace(exitAccount + "/", "");
                                            }
                                            chatContentItemArrayList.get(i).setUnCheckedParticipant(unCheckedParticipant);
                                        }
                                    }
                                }
                            }
                            chatContentAdapter.notifyItemRangeChanged(0, chatContentItemArrayList.size(), "exit");

                            //채팅방 서랍의 참여자 목록도 업데이트 해준다.
                            int index = 0;
                            for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                                if (exitNickname.equals(chatParticipantItemArrayList.get(i).nickname)) {
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
                                    String nickname = chatParticipantItemArrayList.get(i).nickname;
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
                else if (msg.what == 6666) {
                    try {
                        //사용자 초대 메세지를 채팅 아이템 리스트에 추가해준다.
                        String jsonString = msg.obj.toString();
                        JSONObject addParticipantData = new JSONObject(jsonString);
                        int id = addParticipantData.getInt("id");
                        String time = addParticipantData.getString("time");
                        int roomNum = addParticipantData.getInt("roomNum");
                        String message = addParticipantData.getString("message");
                        boolean isSent = true;
                        boolean isExit = false;
                        boolean isTimeDivider = false;
                        boolean isAddedParticipant = true;
                        ChatContentItem chatContentItem = new ChatContentItem();
                        chatContentItem.setId(id);
                        chatContentItem.setTime(time);
                        chatContentItem.setRoomNum(roomNum);
                        chatContentItem.setMessage(message);
                        chatContentItem.setSent(isSent);
                        chatContentItem.setExit(isExit);
                        chatContentItem.setTimeDivider(isTimeDivider);
                        chatContentItem.setAddedParticipantMessage(isAddedParticipant);
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
                        //채팅방 제목을 수정해준다.
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                            String nickname = chatParticipantItemArrayList.get(i).nickname;
                            if (i == 0) sb.append(nickname);
                            else sb.append("," + nickname);
                        }
                        tv_participant.setText(sb.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if(msg.what == 7777) {//영상통화 요청이 상대방에게 잘 전달됐는지에 대한 메세지
                    try {
                        String jsonString = msg.obj.toString();
                        JSONObject faceChatRequestResult = new JSONObject(jsonString);
                        String result = faceChatRequestResult.getString("type");
                        if("successFaceChatRequest".equals(result)) {//영상통화 요청이 잘 전달된 경우
                            Intent intent = new Intent(ChatRoomActivity.this, FaceChatResponseWaitingActivity.class);
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
            }
        };


        //전송버튼 클릭 리스너
        tv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String chat = et_chat.getText().toString();

                if (chat != null && !chat.trim().equals("")) {
                    if (chatParticipantItemArrayList.size() == 0) {
                        Toast.makeText(getApplicationContext(), "참여자가 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            //메세지 데이터를 json객체에 담아준다.
                            JSONObject messageData = new JSONObject();
                            messageData.put("type", "message");
                            messageData.put("roomNum", roomNum);
                            messageData.put("account", LoginActivity.account);
                            messageData.put("nickname", LoginActivity.nickname);
                            messageData.put("profile", LoginActivity.profile);
                            messageData.put("message", chat);

                            ChatContentItem chatContentItem = new ChatContentItem();
                            chatContentItem.setRoomNum(roomNum);
                            chatContentItem.setAccount(LoginActivity.account);
                            chatContentItem.setNickname(LoginActivity.nickname);
                            chatContentItem.setProfile(LoginActivity.profile);
                            chatContentItem.setMessage(chat);
                            chatContentItem.setImageList(imageArrayList);
                            chatContentItem.setMyContent(true);
                            chatContentItem.setSent(false);
                            chatContentItem.setExit(false);
                            chatContentItem.setAddedParticipantMessage(false);
                            chatContentItemArrayList.add(chatContentItem);
                            chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                            rv_chatContent.scrollToPosition(chatContentItemArrayList.size() - 1);
                            if(tv_newMessage.getVisibility() == View.VISIBLE) {//메세지 박스가 보이는 경우 메세지 박스를 지워준다
                                tv_newMessage.setVisibility(View.GONE);
                            }
                            //수신자 스트링을 만들어준다.
                            String receiverString = null;
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                                String account = chatParticipantItemArrayList.get(i).account;
                                if (i == 0) {
                                    stringBuilder.append(account);
                                } else {
                                    stringBuilder.append("/" + account);
                                }
                            }
                            receiverString = stringBuilder.toString();
                            addChat(String.valueOf(roomNum), LoginActivity.account, receiverString, chat, "message", messageData.toString(), imageArrayList);
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

        //이미지 추가 버튼 클릭 리스너
        ib_addContent.setOnClickListener(new View.OnClickListener() {
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
                                try {
                                    //소켓 서버로 전달할 json스트링
                                    JSONObject imageData = new JSONObject();
                                    imageData.put("type", "image");
                                    imageData.put("roomNum", roomNum);
                                    imageData.put("account", LoginActivity.account);
                                    imageData.put("nickname", LoginActivity.nickname);
                                    imageData.put("profile", LoginActivity.profile);
                                    imageData.put("message", LoginActivity.nickname + "님이 사진을 보냈습니다.");

                                    JSONArray imageList = new JSONArray();
                                    //선택한 이미지 경로를 json 어레이에 넣어준다.
                                    for (int i = 0; i < uriList.size(); i++) {
                                        imageArrayList.add(uriList.get(i).toString());
                                        //이미지 파일의 이름 설정
                                        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
                                        String imageFileName = account + timeStamp + (i + 1) + ".jpg";
                                        imageNameForServerList.add(imageFileName);
                                        Log.d("이미지 파일명", imageNameForServerList.get(i));
                                        imageList.put(imageNameForServerList.get(i));
                                    }
                                    imageData.put("imageList", imageList);
                                    ChatContentItem chatContentItem = new ChatContentItem();
                                    chatContentItem.setRoomNum(roomNum);
                                    chatContentItem.setAccount(LoginActivity.account);
                                    chatContentItem.setNickname(LoginActivity.nickname);
                                    chatContentItem.setProfile(LoginActivity.profile);
                                    chatContentItem.setMessage(LoginActivity.nickname + "님이 사진을 보냈습니다.");
                                    for (int j = 0; j < imageArrayList.size(); j++) {
                                        chatContentItem.imageList.add(imageArrayList.get(j));
                                    }
                                    chatContentItem.setMyContent(true);
                                    chatContentItem.setSent(false);
                                    chatContentItem.setImageFromServer(false);
                                    chatContentItem.setExit(false);
                                    chatContentItem.setAddedParticipantMessage(false);
                                    chatContentItemArrayList.add(chatContentItem);
                                    chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                                    rv_chatContent.scrollToPosition(chatContentItemArrayList.size() - 1);
                                    if(tv_newMessage.getVisibility() == View.VISIBLE) {//메세지 박스가 보이는 경우 메세지 박스를 지워준다
                                        tv_newMessage.setVisibility(View.GONE);
                                    }

                                    //수신자 스트링을 만들어준다.
                                    String receiverString = null;
                                    StringBuilder stringBuilder = new StringBuilder();
                                    for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                                        String account = chatParticipantItemArrayList.get(i).account;
                                        if (i == 0) {
                                            stringBuilder.append(account);
                                        } else {
                                            stringBuilder.append("/" + account);
                                        }
                                    }
                                    receiverString = stringBuilder.toString();
                                    Log.d("이미지 메세지 데이터", imageData.toString());
                                    addChat(String.valueOf(roomNum), LoginActivity.account, receiverString, LoginActivity.nickname + "님이 사진을 보냈습니다.", "image", imageData.toString(), imageArrayList);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }
        });


        //뒤로가기 버튼 클릭 리스너
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //방을 만들고 채팅을 보내지 않은 상태에서 그냥 나가는 경우
                if (chatContentItemArrayList.size() == 0) {
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
                                Message message = ChatIntentService.handler.obtainMessage();
                                message.what = 3333;
                                message.obj = jsonObject.toString();
                                ChatIntentService.handler.sendMessage(message);
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
                drawerLayout.openDrawer(Gravity.END);
            }
        });

        //새로운 메세지 박스 클릭 리스너
        tv_newMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rv_chatContent.scrollToPosition(chatContentItemArrayList.size()-1);
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
                    case R.id.photo:
                        //채팅방의 사진을 그리드 형식의 리사이클러뷰로 볼 수 있는 액티비티로 이동
                        Intent intent = new Intent(ChatRoomActivity.this, ChatImageListActivity.class);
                        intent.putExtra("totalImageCount", totalImageArrayList.size());//전체 이미지 수
                        for (int i = 0; i < totalImageArrayList.size(); i++) {
                            intent.putExtra("imageData" + i, totalImageArrayList.get(i));//json 스트링 이미지 데이터
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
                                drawerLayout.closeDrawer(Gravity.END);
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
            requestBody.put("account", account);
            requestBody.put("lastId", chatContentItemArrayList.get(0).id);
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
            requestBody.put("account", LoginActivity.account);
            requestBody.put("nickname", LoginActivity.nickname);
            requestBody.put("profile", LoginActivity.profile);
            requestBody.put("message", LoginActivity.nickname + "님이 채팅방을 나가셨습니다.");
            requestBody.put("position", 0);//리사이클러뷰 상에서 나간 채팅방의 index
            //chat테이블에 저장할 수신자 목록을 jsonArray에 넣어준다.
            JSONArray receiverList = new JSONArray();

            for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                //수신자 닉네임
                String receiverAccount = chatParticipantItemArrayList.get(i).account;
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
                            String earlierTime = ChatImageDetailActivity.formatDate(time, fromFormat, toFormat);
                            //현재 로드된 아이템 중 제일 시간이 빠른 아이템
                            String laterTime = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(0).time, fromFormat, toFormat);
                            //연, 월, 일이 다르면 다른 날에 전송된 아이템이기 때문에 시간 구분 아이템을 추가하여 아이템에 구분을 준다.
                            if (!earlierTime.equals(laterTime)) {
                                ChatContentItem chatContentItem = new ChatContentItem();
                                chatContentItem.setTime(chatContentItemArrayList.get(0).time);
                                chatContentItem.setTimeDivider(true);
                                chatContentItemArrayList.add(0, chatContentItem);
                            }
                        }

                        ChatContentItem chatContentItem = new Gson().fromJson(data.toString(), ChatContentItem.class);
                        chatContentItemArrayList.add(0, chatContentItem);

                    }

                    chatContentAdapter.notifyDataSetChanged();
                    rv_chatContent.scrollToPosition(chatContentItemArrayList.size()-1);

                    //현재 채팅방에 이미지가 존재한다면 전체 이미지를 조회할 수 있도록 리스트에 담아주는 작업을 한다.

                    if (!responseBody.isNull("totalImageData")) {
                        jsonArray = responseBody.getJSONArray("totalImageData");
                        Log.d("이미지 수", String.valueOf(jsonArray.length()));
                        Log.d("전체 이미지 데이터", jsonArray.toString());
                        if (jsonArray != null) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                totalImageArrayList.add(String.valueOf(jsonArray.get(i)));
                            }
                        }
                    }
                    //데이터 로드에 성공하면 서비스로 입장 메세지를 전달해서 방에 있는 다른 사용자들에게 메세지를 전달하도록 한다.
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                //데이터를 다 가져왔으면 현재 채팅방에 들어와있는 참여자들에게 입장사실을 전달해줘서 채팅을 확인했음을 알 수 잏게 해준다.
                                Message message = ChatIntentService.handler.obtainMessage();
                                //입장 메세지 데이터를 json스트링으로 만든다.
                                JSONObject enterData = new JSONObject();
                                enterData.put("type", "enter");
                                enterData.put("roomNum", roomNum);
                                enterData.put("account", LoginActivity.account);
                                enterData.put("nickname", LoginActivity.nickname);
                                enterData.put("profile", LoginActivity.profile);
                                message.what = 4444;
                                message.obj = enterData.toString();
                                ChatIntentService.handler.sendMessage(message);

                                //서버에 업데이트 해준다.
                                updateCheckMessage(roomNum, LoginActivity.account);
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
                            String earlierTime = ChatImageDetailActivity.formatDate(time, fromFormat, toFormat);
                            //현재 로드된 아이템 중 제일 시간이 빠른 아이템
                            String laterTime = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(0).time, fromFormat, toFormat);
                            //연, 월, 일이 다르면 다른 날에 전송된 아이템이기 때문에 시간 구분 아이템을 추가하여 아이템에 구분을 준다.
                            if (!earlierTime.equals(laterTime)) {
                                ChatContentItem chatContentItem = new ChatContentItem();
                                chatContentItem.setTime(chatContentItemArrayList.get(0).time);
                                chatContentItem.setTimeDivider(true);
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
                        Message message = ChatIntentService.handler.obtainMessage();
                        message.what = 5555;
                        message.obj = exitData.toString();
                        ChatIntentService.handler.sendMessage(message);
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
//                        participantListAdapter.notifyItemInserted(chatParticipantItemArrayList.size() - 1);
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
                        String nickname = chatParticipantItemArrayList.get(i).nickname;
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
                    messageData.put("account", LoginActivity.account);
                    messageData.put("nickname", LoginActivity.nickname);
                    messageData.put("profile", LoginActivity.profile);
                    messageData.put("message", LoginActivity.nickname + "님이 " + sb.toString() + "님을 초대하셨습니다.");
                    //수신자를 담을 json어레이
                    JSONArray receiverList = new JSONArray();
                    for (int i = 0; i < chatParticipantItemArrayList.size(); i++) {
                        String account = chatParticipantItemArrayList.get(i).account;
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
                    boolean isSent = true;
                    boolean isExit = false;
                    boolean isTimeDivider = false;
                    boolean isAddedParticipant = true;
                    ChatContentItem chatContentItem = new ChatContentItem();
                    chatContentItem.setId(id);
                    chatContentItem.setTime(time);
                    chatContentItem.setRoomNum(roomNum);
                    chatContentItem.setMessage(msg);
                    chatContentItem.setSent(isSent);
                    chatContentItem.setExit(isExit);
                    chatContentItem.setTimeDivider(isTimeDivider);
                    chatContentItem.setAddedParticipantMessage(isAddedParticipant);
                    chatContentItemArrayList.add(chatContentItem);
                    chatContentAdapter.notifyItemInserted(chatContentItemArrayList.size() - 1);
                    //새로운 사용자가 초대됐다는 사실을 그 채팅방에 있는 사용자들에게 전달
                    Message message = ChatIntentService.handler.obtainMessage();
                    message.what = 6666;
                    message.obj = messageData.toString();
                    ChatIntentService.handler.sendMessage(message);
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


    public void addChat(String roomNum, String sender, String receiverString, String message, String type, String messageData, ArrayList imageArrayList) {

        //레트로핏 세팅
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://13.124.105.47/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //필수로 넘어가야 하는 값은 모두 초기화해준다.
        RequestBody roomNumPart = RequestBody.create(MultipartBody.FORM, roomNum);
        RequestBody senderPart = RequestBody.create(MultipartBody.FORM, sender);
        RequestBody participantStringPart = RequestBody.create(MultipartBody.FORM, receiverString);
        RequestBody messagePart = RequestBody.create(MultipartBody.FORM, message);
        RequestBody typePart = RequestBody.create(MultipartBody.FORM, type);
        //필수가 아닌 값들은 선언만 해준다.
        //채팅 메세지


        //이미지1
        RequestBody imageFile1;
        MultipartBody.Part body1;

        //이미지2
        RequestBody imageFile2;
        MultipartBody.Part body2;

        //이미지3
        RequestBody imageFile3;
        MultipartBody.Part body3;

        //이미지4
        RequestBody imageFile4;
        MultipartBody.Part body4;

        //이미지5
        RequestBody imageFile5;
        MultipartBody.Part body5;

        //이미지6
        RequestBody imageFile6;
        MultipartBody.Part body6;

        //레트로핏 인터페이스 설정
        RetrofitService retrofitService;
        Call<AddChatResponse> call;

        //이미지 개수를 담는 변수
        int imageCount = imageArrayList.size();
        //이미지 처리 객체 초기화
        ProcessImage processImage = new ProcessImage(ChatRoomActivity.this);

        //이미지는 보내지 않는 경우
        if (imageCount == 0) {

            retrofitService = retrofit.create(RetrofitService.class);
            call = retrofitService.addChatResponse(roomNumPart, senderPart, participantStringPart, messagePart, typePart, null, null, null, null, null, null);

        }
        //사진 1장 선택했을 때
        else if (imageCount == 1) {
            //이미지 파일의 이름

            String imageFileName = imageNameForServerList.get(0);

            //이미지 파일 생성
            File file = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(0))), String.valueOf(imageArrayList.get(0)));
            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file);

            body1 = MultipartBody.Part.createFormData("image1", imageFileName, imageFile1);

            retrofitService = retrofit.create(RetrofitService.class);
            call = retrofitService.addChatResponse(roomNumPart, senderPart, participantStringPart, messagePart, typePart, body1, null, null, null, null, null);


        }
        //사진 2장 선택했을 때
        else if (imageCount == 2) {
            //이미지 파일1의 이름
            String imageFileName1 = imageNameForServerList.get(0);

            //이미지 파일2의 이름
            String imageFileName2 = imageNameForServerList.get(1);

            File file1 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(0))), String.valueOf(imageArrayList.get(0)));
            File file2 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(1))), String.valueOf(imageArrayList.get(1)));


            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
            imageFile2 = RequestBody.create(MediaType.parse("multipart/form-data"), file2);


            body1 = MultipartBody.Part.createFormData("image1", imageFileName1, imageFile1);
            body2 = MultipartBody.Part.createFormData("image2", imageFileName2, imageFile2);

            retrofitService = retrofit.create(RetrofitService.class);
            call = retrofitService.addChatResponse(roomNumPart, senderPart, participantStringPart, messagePart, typePart, body1, body2, null, null, null, null);


        }
        //사진 3장 선택했을 때
        else if (imageCount == 3) {

            //이미지 파일1의 이름
            String imageFileName1 = imageNameForServerList.get(0);

            //이미지 파일2의 이름
            String imageFileName2 = imageNameForServerList.get(1);

            //이미지 파일3의 이름
            String imageFileName3 = imageNameForServerList.get(2);

            File file1 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(0))), String.valueOf(imageArrayList.get(0)));
            File file2 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(1))), String.valueOf(imageArrayList.get(1)));
            File file3 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(2))), String.valueOf(imageArrayList.get(2)));


            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
            imageFile2 = RequestBody.create(MediaType.parse("multipart/form-data"), file2);
            imageFile3 = RequestBody.create(MediaType.parse("multipart/form-data"), file3);


            body1 = MultipartBody.Part.createFormData("image1", imageFileName1, imageFile1);
            body2 = MultipartBody.Part.createFormData("image2", imageFileName2, imageFile2);
            body3 = MultipartBody.Part.createFormData("image3", imageFileName3, imageFile3);

            retrofitService = retrofit.create(RetrofitService.class);
            call = retrofitService.addChatResponse(roomNumPart, senderPart, participantStringPart, messagePart, typePart, body1, body2, body3, null, null, null);


        }
        //사진4장 선택했을 때
        else if (imageCount == 4) {

            //이미지 파일1의 이름
            String imageFileName1 = imageNameForServerList.get(0);
            //이미지 파일2의 이름
            String imageFileName2 = imageNameForServerList.get(1);
            //이미지 파일3의 이름
            String imageFileName3 = imageNameForServerList.get(2);
            //이미지 파일4의 이름
            String imageFileName4 = imageNameForServerList.get(3);

            File file1 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(0))), String.valueOf(imageArrayList.get(0)));
            File file2 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(1))), String.valueOf(imageArrayList.get(1)));
            File file3 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(2))), String.valueOf(imageArrayList.get(2)));
            File file4 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(3))), String.valueOf(imageArrayList.get(3)));

            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
            imageFile2 = RequestBody.create(MediaType.parse("multipart/form-data"), file2);
            imageFile3 = RequestBody.create(MediaType.parse("multipart/form-data"), file3);
            imageFile4 = RequestBody.create(MediaType.parse("multipart/form-data"), file4);


            body1 = MultipartBody.Part.createFormData("image1", imageFileName1, imageFile1);
            body2 = MultipartBody.Part.createFormData("image2", imageFileName2, imageFile2);
            body3 = MultipartBody.Part.createFormData("image3", imageFileName3, imageFile3);
            body4 = MultipartBody.Part.createFormData("image4", imageFileName4, imageFile4);

            retrofitService = retrofit.create(RetrofitService.class);
            call = retrofitService.addChatResponse(roomNumPart, senderPart, participantStringPart, messagePart, typePart, body1, body2, body3, body4, null, null);

        }
        //사진 5장 선택했을 때
        else if (imageCount == 5) {
            //이미지 파일1의 이름
            String imageFileName1 = imageNameForServerList.get(0);
            //이미지 파일2의 이름
            String imageFileName2 = imageNameForServerList.get(1);
            //이미지 파일3의 이름
            String imageFileName3 = imageNameForServerList.get(2);
            //이미지 파일4의 이름
            String imageFileName4 = imageNameForServerList.get(3);
            //이미지 파일5의 이름
            String imageFileName5 = imageNameForServerList.get(4);


            File file1 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(0))), String.valueOf(imageArrayList.get(0)));
            File file2 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(1))), String.valueOf(imageArrayList.get(1)));
            File file3 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(2))), String.valueOf(imageArrayList.get(2)));
            File file4 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(3))), String.valueOf(imageArrayList.get(3)));
            File file5 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(4))), String.valueOf(imageArrayList.get(4)));

            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
            imageFile2 = RequestBody.create(MediaType.parse("multipart/form-data"), file2);
            imageFile3 = RequestBody.create(MediaType.parse("multipart/form-data"), file3);
            imageFile4 = RequestBody.create(MediaType.parse("multipart/form-data"), file4);
            imageFile5 = RequestBody.create(MediaType.parse("multipart/form-data"), file5);


            body1 = MultipartBody.Part.createFormData("image1", imageFileName1, imageFile1);
            body2 = MultipartBody.Part.createFormData("image2", imageFileName2, imageFile2);
            body3 = MultipartBody.Part.createFormData("image3", imageFileName3, imageFile3);
            body4 = MultipartBody.Part.createFormData("image4", imageFileName4, imageFile4);
            body5 = MultipartBody.Part.createFormData("image5", imageFileName5, imageFile5);

            retrofitService = retrofit.create(RetrofitService.class);
            call = retrofitService.addChatResponse(roomNumPart, senderPart, participantStringPart, messagePart, typePart, body1, body2, body3, body4, body5, null);

        }
        //사진 6장 선택했을 때
        else {

            //이미지 파일1의 이름
            String imageFileName1 = imageNameForServerList.get(0);
            //이미지 파일2의 이름
            String imageFileName2 = imageNameForServerList.get(1);
            //이미지 파일3의 이름
            String imageFileName3 = imageNameForServerList.get(2);
            //이미지 파일4의 이름
            String imageFileName4 = imageNameForServerList.get(3);
            //이미지 파일5의 이름
            String imageFileName5 = imageNameForServerList.get(4);
            //이미지 파일6의 이름
            String imageFileName6 = imageNameForServerList.get(5);


            File file1 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(0))), String.valueOf(imageArrayList.get(0)));
            File file2 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(1))), String.valueOf(imageArrayList.get(1)));
            File file3 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(2))), String.valueOf(imageArrayList.get(2)));
            File file4 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(3))), String.valueOf(imageArrayList.get(3)));
            File file5 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(4))), String.valueOf(imageArrayList.get(4)));
            File file6 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(4))), String.valueOf(imageArrayList.get(5)));

            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
            imageFile2 = RequestBody.create(MediaType.parse("multipart/form-data"), file2);
            imageFile3 = RequestBody.create(MediaType.parse("multipart/form-data"), file3);
            imageFile4 = RequestBody.create(MediaType.parse("multipart/form-data"), file4);
            imageFile5 = RequestBody.create(MediaType.parse("multipart/form-data"), file5);
            imageFile6 = RequestBody.create(MediaType.parse("multipart/form-data"), file6);


            body1 = MultipartBody.Part.createFormData("image1", imageFileName1, imageFile1);
            body2 = MultipartBody.Part.createFormData("image2", imageFileName2, imageFile2);
            body3 = MultipartBody.Part.createFormData("image3", imageFileName3, imageFile3);
            body4 = MultipartBody.Part.createFormData("image4", imageFileName4, imageFile4);
            body5 = MultipartBody.Part.createFormData("image5", imageFileName5, imageFile5);
            body6 = MultipartBody.Part.createFormData("image6", imageFileName6, imageFile6);


            retrofitService = retrofit.create(RetrofitService.class);
            call = retrofitService.addChatResponse(roomNumPart, senderPart, participantStringPart, messagePart, typePart, body1, body2, body3, body4, body5, body6);

        }

        call.enqueue(new Callback<AddChatResponse>() {
            @Override
            public void onResponse(Call<AddChatResponse> call, Response<AddChatResponse> response) {
                Log.d("레트로핏 통신", "성공");
                //채팅방에서 처음으로 메세지를 보내는 경우 방을 만들고 메세지를 보내는 것이기 때문에 채팅방을 활성화시켜준다.
                //chatroom 테이블의 activated_participant에 모든 참여자를 다 넣어준다.
                if (chatContentItemArrayList.size() == 1) {
                    avtivateChatRoom(Integer.parseInt(roomNum));//채팅방 활성화
                }
                //이미 만들어져있는 채팅방에 새로운 사용자를 초대하고 처음 메세지를 전송하는 경우
                //chatroom 테이블의 activated_participant필드의 값에 초대된 사용자들을 추가해준다.
                if (chatContentItemArrayList.size() > 1 && chatContentItemArrayList.get(chatContentItemArrayList.size() - 2).isAddedParticipantMessage) {
                    avtivateChatRoom(Integer.parseInt(roomNum));//채팅방 활성화
                }


                AddChatResponse addChatResponse = response.body();
                //메세지 전송 시간
                String time = addChatResponse.time;
                //메세지 미확인자 리스트 문자열
                String unCheckedParticipant = addChatResponse.unCheckedParticipant;
                Log.d("미확인자 리스트", unCheckedParticipant);
                //메세지의 id
                String id = addChatResponse.id;
                Log.d("전송한 채팅 id", id);
                //업로드한 이미지명
                String image1, image2, image3, image4, image5, image6;
                try {


                    //이미지를 uri가 아니라 서버의 url파일명으로 바꿔주기 위해서 이미지 어레이를 미리 비워둔다.
                    chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).imageList.clear();
                    String[] imageList;
                    //업로드한 이미지를 전체 이미지 리스트에 넣어줘서 이미지를 클릭했을 때 상세보기 페이지에 이미지가 보일 수 있게 해준다.
                    switch (imageCount) {
                        case 0:
                            break;
                        case 1:
                            image1 = addChatResponse.image1;
                            imageList = new String[]{image1};
                            //이미지를 전송한 사람의 계정, 닉네임, 프로필,전송 시간, 이미지을 담은 Json객체
                            for (int i = 0; i < imageList.length; i++) {
                                JSONObject imageData = new JSONObject();
                                imageData.put("account", LoginActivity.account);
                                imageData.put("nickname", LoginActivity.nickname);
                                imageData.put("profile", LoginActivity.profile);
                                imageData.put("time", time);
                                imageData.put("image", imageList[i]);
                                totalImageArrayList.add(imageData.toString());
                                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).imageList.add(imageList[i]);
                            }

                            break;
                        case 2:
                            image1 = addChatResponse.image1;
                            image2 = addChatResponse.image2;
                            imageList = new String[]{image1, image2};
                            for (int i = 0; i < imageList.length; i++) {
                                JSONObject imageData = new JSONObject();
                                imageData.put("account", LoginActivity.account);
                                imageData.put("nickname", LoginActivity.nickname);
                                imageData.put("profile", LoginActivity.profile);
                                imageData.put("time", time);
                                imageData.put("image", imageList[i]);
                                totalImageArrayList.add(imageData.toString());
                                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).imageList.add(imageList[i]);
                            }
                            break;
                        case 3:
                            image1 = addChatResponse.image1;
                            image2 = addChatResponse.image2;
                            image3 = addChatResponse.image3;
                            imageList = new String[]{image1, image2, image3};
                            for (int i = 0; i < imageList.length; i++) {
                                JSONObject imageData = new JSONObject();
                                imageData.put("account", LoginActivity.account);
                                imageData.put("nickname", LoginActivity.nickname);
                                imageData.put("profile", LoginActivity.profile);
                                imageData.put("time", time);
                                imageData.put("image", imageList[i]);
                                totalImageArrayList.add(imageData.toString());
                                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).imageList.add(imageList[i]);
                            }
                            break;
                        case 4:
                            image1 = addChatResponse.image1;
                            image2 = addChatResponse.image2;
                            image3 = addChatResponse.image3;
                            image4 = addChatResponse.image4;
                            imageList = new String[]{image1, image2, image3, image4};
                            for (int i = 0; i < imageList.length; i++) {
                                JSONObject imageData = new JSONObject();
                                imageData.put("account", LoginActivity.account);
                                imageData.put("nickname", LoginActivity.nickname);
                                imageData.put("profile", LoginActivity.profile);
                                imageData.put("time", time);
                                imageData.put("image", imageList[i]);
                                totalImageArrayList.add(imageData.toString());
//                                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).imageList.add(imageList[i]);
                            }
                            break;
                        case 5:
                            image1 = addChatResponse.image1;
                            image2 = addChatResponse.image2;
                            image3 = addChatResponse.image3;
                            image4 = addChatResponse.image4;
                            image5 = addChatResponse.image5;
                            imageList = new String[]{image1, image2, image3, image4, image5};
                            for (int i = 0; i < imageList.length; i++) {
                                JSONObject imageData = new JSONObject();
                                imageData.put("account", LoginActivity.account);
                                imageData.put("nickname", LoginActivity.nickname);
                                imageData.put("profile", LoginActivity.profile);
                                imageData.put("time", time);
                                imageData.put("image", imageList[i]);
                                totalImageArrayList.add(imageData.toString());
                                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).imageList.add(imageList[i]);
                            }
                            break;
                        case 6:
                            image1 = addChatResponse.image1;
                            image2 = addChatResponse.image2;
                            image3 = addChatResponse.image3;
                            image4 = addChatResponse.image4;
                            image5 = addChatResponse.image5;
                            image6 = addChatResponse.image6;
                            imageList = new String[]{image1, image2, image3, image4, image5, image6};
                            for (int i = 0; i < imageList.length; i++) {
                                JSONObject imageData = new JSONObject();
                                imageData.put("account", LoginActivity.account);
                                imageData.put("nickname", LoginActivity.nickname);
                                imageData.put("profile", LoginActivity.profile);
                                imageData.put("time", time);
                                imageData.put("image", imageList[i]);
                                totalImageArrayList.add(imageData.toString());
                                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).imageList.add(imageList[i]);
                            }
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //서버 이미지로 세팅
                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).setImageFromServer(true);
                //전송 완료 상태로 셋
                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).setSent(true);
                //전송 시간 셋
                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).setTime(time);
                //미확인자 수 셋
                chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).setUnCheckedParticipant(unCheckedParticipant);
                //아이템 리스트에 아이템이 두개 이상이 되면 메세지의 전송 시간을 비교해서 두 아이템의 전송 날짜가 다른 경우
                //시간 구분 아이템을 함께 추가해준다.
                if (chatContentItemArrayList.size() >= 2) {
                    //지금 보낸 메세지가 가장 늦은 시간
                    String laterTime = ChatImageDetailActivity.formatDate(time, fromFormat, toFormat);
                    //메세지를 보내기 전에 있었던 가장 최하단의 메세지
                    String earlierTime = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(chatContentItemArrayList.size() - 2).time, fromFormat, toFormat);
                    //연, 월, 일이 다르면 다른 날에 전송된 아이템이기 때문에 시간 구분 아이템을 추가하여 아이템에 구분을 준다.
                    if (!earlierTime.equals(laterTime)) {
                        ChatContentItem chatContentItem = new ChatContentItem();
                        chatContentItem.setTime(chatContentItemArrayList.get(chatContentItemArrayList.size() - 1).time);
                        chatContentItem.setTimeDivider(true);
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
                            Message message = ChatIntentService.handler.obtainMessage();
                            //메세지 식별자를 1111로 설정
                            message.what = 1111;
                            //메세지 내용을 넣는다.
                            message.obj = jsonObject.toString();
                            //채팅 서비스 클래스로 메세지를 보낸다.
                            ChatIntentService.handler.sendMessage(message);

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
            }
        });


    }

    @Override
    protected void onResume() {
        Log.d("채팅화면 onResume", "호출");
        super.onResume();
        //채팅 내용을 서버로부터 가져오는 asynctask
        if (totalImageArrayList.isEmpty()) {//전체 이미지 리스트에 값이 없으면 최초 로드로 간주해서 서버에서 전체 이미지 데이터를 가져온다.
            getChatContent(roomNum, LoginActivity.account, 0, listSize, true);
        } else {
            getChatContent(roomNum, LoginActivity.account, 0, listSize, false);
        }
    }


    //채팅방의 이미지를 클릭했을 때 호출되는 메소드
    @Override
    public void onImageclicked(int position, int imagePosition) {

        Intent intent = new Intent(this, ChatImageDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        //채팅창의 전체 이미지 수
        intent.putExtra("imageListCount", totalImageArrayList.size());
        for (int i = 0; i < totalImageArrayList.size(); i++) {
            //전체 이미지 데이터(json스트링)
            intent.putExtra("imageData" + i, totalImageArrayList.get(i));
        }
        //클릭한 이미지의 인덱스 구해준다.
        String image = chatContentItemArrayList.get(position).imageList.get(imagePosition);//선택한 이미지 파일 명
        int index = 0;//인덱스
        //전체 이미지 데이터를 돌면서
        for (int i = 0; i < totalImageArrayList.size(); i++) {
            try {
                //이미지의 파일명을 꺼내서 클릭한 이미지 파일 명과 같은지 비교를 해서
                JSONObject imageData = new JSONObject(totalImageArrayList.get(i));
                if (imageData.getString("image").equals(image)) {
                    //같은 경우 그 이미지의 인덱스를 넣어준다.
                    index = i;
                    break;
                }
                ;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        intent.putExtra("imageIndex", index);
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
                .load("http://13.124.105.47/profileimage/" + chatContentItemArrayList.get(position).profile)
                .thumbnail(0.1f)
                .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                .into(cv_profile);

        //닉네임 설정
        tv_nickname.setText(chatContentItemArrayList.get(position).nickname);

        //1:1채팅의 경우 이미 1:1채팅방에 들어와있는 경우에는 버튼을 없애준다.
        if(chatParticipantItemArrayList.size()<2) {
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
                                    jsonObject.put("roomName", account + chatContentItemArrayList.get(position).account);//영상통화의 방 이름을 보내준다. 이때 방은 두 peer의 계정을 합쳐서 구성한다.
                                    jsonObject.put("account", account);//발신자 계정
                                    jsonObject.put("nickname", nickname);//발신자 닉네임
                                    jsonObject.put("profile", profile);//발신자 프로필 사진
                                    jsonObject.put("receiver", chatContentItemArrayList.get(position).account);//수신자 계정
                                    //소켓 서버로 영상통화 토큰을 날려준다.
                                    Message message = ChatIntentService.handler.obtainMessage();
                                    message.what = 7777;
                                    message.obj = jsonObject.toString();
                                    ChatIntentService.handler.sendMessage(message);
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
                        .putExtra("selectedUserAccount", chatContentItemArrayList.get(position).account)
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
                    String account = chatParticipantItemArrayList.get(i).account;
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
                    .load("http://13.124.105.47/profileimage/" + chatParticipantItemArrayList.get(position-1).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(cv_profile);

            //닉네임 설정
            tv_nickname.setText(chatParticipantItemArrayList.get(position-1).nickname);

            //1:1채팅의 경우 이미 1:1채팅방에 들어와있는 경우에는 버튼을 없애준다.
            if(chatParticipantItemArrayList.size()<2) {
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
                                        jsonObject.put("roomName", account + chatParticipantItemArrayList.get(position-1).account);//영상통화의 방 이름을 보내준다. 이때 방은 두 peer의 계정을 합쳐서 구성한다.
                                        jsonObject.put("account", account);//발신자 계정
                                        jsonObject.put("nickname", nickname);//발신자 닉네임
                                        jsonObject.put("profile", profile);//발신자 프로필 사진
                                        jsonObject.put("receiver", chatParticipantItemArrayList.get(position-1).account);//수신자 계정
                                        //소켓 서버로 영상통화 토큰을 날려준다.
                                        Message message = ChatIntentService.handler.obtainMessage();
                                        message.what = 7777;
                                        message.obj = jsonObject.toString();
                                        ChatIntentService.handler.sendMessage(message);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    drawerLayout.closeDrawer(Gravity.END, true);
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
                            .putExtra("selectedUserAccount", chatParticipantItemArrayList.get(position-1).account)
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
        if (drawerLayout.isDrawerOpen(Gravity.END)) {
            //채팅방 서랍을 닫아준다.
            drawerLayout.closeDrawer(Gravity.END);
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
                            Message message = ChatIntentService.handler.obtainMessage();
                            message.what = 3333;
                            message.obj = jsonObject.toString();
                            ChatIntentService.handler.sendMessage(message);
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


