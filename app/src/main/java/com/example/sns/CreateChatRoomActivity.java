package com.example.sns;

import android.content.Intent;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CreateChatRoomActivity extends AppCompatActivity implements SelectedUserAdapter.SelectedUserRecyclerViewListener, UserAdapter.UserRecyclerViewListener, HttpRequest.OnHttpResponseListener {
    private final String TAG = "CreateChatRoomActivity";
    private EditText et_search;
    private ImageButton ib_back;
    private TextView tv_apply;

    //상단에 선택된 사용자 리사이클러뷰
    private RecyclerView rv_selectedUser;
    private LinearLayoutManager linearLayoutManager_selectedUser;
    private SelectedUserAdapter selectedUserAdapter;
    private ArrayList<SelectedUserItem> selectedUserItemArrayList;

    //전체 사용자 리스트를 보여줄 리사이클러뷰
    private RecyclerView rv_user;
    private LinearLayoutManager linearLayoutManager_user;
    private UserAdapter userAdapter;
    private ArrayList<UserItem> userItemArrayList;

    //현재 로드된 아이템의 마지막 index
    private int currentLastId;

    //검색어
    private String searchText;

    private boolean loadPossible = true;

    private LoginUser loginUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chat_participant);

        loginUser = LoginUser.getInstance();

        et_search = findViewById(R.id.edittext_search);
        ib_back = findViewById(R.id.imagebutton_back);
        tv_apply = findViewById(R.id.textview_apply);

        setRecyclerView();

        //검색창 텍스트 입력 리스너
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchText = et_search.getText().toString();
                //검색어가 존재하지 않는 경우
                if (searchText.length() == 0) {
                    getUser(loginUser.getAccount(), searchText, 0, false);
                    searchText = null;
                }
                //검색어가 존재하는 경우
                else {
                    getUser(loginUser.getAccount(), searchText, 0, true);
                }


            }
        });

        //적용버튼 클릭 리스너
        tv_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUserItemArrayList.size() != 0) {
//                    //새롭게 만들고자 하는 채팅방의 참여자 리스트를 스트링 형태로 만들어서 넘겨준다(닉네임 포함).
//                    StringBuilder stringBuilder = new StringBuilder();
                    //아이디로만 형성(서버로 전송할 데이터)
                    StringBuilder stringBuilder1 = new StringBuilder();
                    //이 데이터는 서버에 채팅방 테이블에 저장시킬 데이터이기 때문에 내 계정도 포함시킨다.
                    stringBuilder1.append(loginUser.getAccount() + "/");
                    for (int i = 0; i < selectedUserItemArrayList.size(); i++) {
                        if (i != selectedUserItemArrayList.size() - 1) {
                            stringBuilder1.append(selectedUserItemArrayList.get(i).account + "/");
                        } else {
                            stringBuilder1.append(selectedUserItemArrayList.get(i).account);
                        }
                    }

                    Intent intent = new Intent();
                    //ex) "계정/계정/계정/계정...."
                    intent.putExtra("participantString", stringBuilder1.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "대화 상대를 선택해주세요.", Toast.LENGTH_SHORT).show();
                }


            }
        });

        //뒤로가기 클릭 리스너
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });

        //addOnScrollListener는 리사이클러뷰를 스크롤하는 순간을 캐치한다.
        rv_user.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                if ((lastVisibleItemPosition == totalCount - 1) && visibleItemCount == 7 && lastCompletelyVisibleItemPosition == totalCount - 1 && loadPossible == true) {
                    Log.d("페이징 조건", "부합");
                    //다음 페이지를 로드한다.
                    //param:현재 로드되어있는 데이터의 수(다음 페이지에 로드되어야 할 첫번째 게시물의 index)
                    loadNextPage();
                }


            }
        });


        //여기에 팔로잉 전체 데이터를 가져와서 셋해주는 코딩
        getUser(loginUser.getAccount(), searchText, 0, false);

    }


    private void setRecyclerView() {
        //선택된 사용자 리스트
        selectedUserItemArrayList = new ArrayList<>();
        //선택된 사용자 리사이클러뷰 설정
        rv_selectedUser = findViewById(R.id.recyclerview_selecteduser);
        rv_selectedUser.setHasFixedSize(true);

        linearLayoutManager_selectedUser = new LinearLayoutManager(this);
        //선택된 사용자 리스트는 가로형태로 보여줄 것
        linearLayoutManager_selectedUser.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_selectedUser.setLayoutManager(linearLayoutManager_selectedUser);

        //선택된 사용자 리사이클러뷰 어댑터 설정
        selectedUserAdapter = new SelectedUserAdapter(this, selectedUserItemArrayList);
        selectedUserAdapter.setOnClickListener(this);
        rv_selectedUser.setAdapter(selectedUserAdapter);

        //전체 사용자 리스트
        userItemArrayList = new ArrayList<>();
        //사용자 리사이클러뷰 설정
        rv_user = findViewById(R.id.recyclerview_userlist);
        rv_user.setHasFixedSize(true);

        linearLayoutManager_user = new LinearLayoutManager(this);
        rv_user.setLayoutManager(linearLayoutManager_user);

        userAdapter = new UserAdapter(this, userItemArrayList);
        userAdapter.setOnClickListener(this);
        rv_user.setAdapter(userAdapter);

    }

    private void getUser(String account, String searchText, int lastId, boolean isSearched) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("requestType", "getChatUser");
            requestBody.put("account", account);
            requestBody.put("searchText", searchText);
            requestBody.put("lastId", lastId);
            requestBody.put("isSearched", isSearched);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getchatuser.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    //사람 검색 결과 목록의 최하단으로 이동하면 페이징 처리를 위해서 실행될 메소드
    private void loadNextPage() {
        loadPossible = false;
        //null을 넣어서 뷰타입이 프로그래스 아이템이 추가되게 한다.
        userItemArrayList.add(null);
        userAdapter.notifyItemInserted(userItemArrayList.size() - 1);
        //핸들러를 통해서 2초 뒤에 해당 기능을 실행한다.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("requestType", "loadNextChatUser");
                    requestBody.put("account", loginUser.getAccount());
                    requestBody.put("searchText", searchText);
                    requestBody.put("lastId", userItemArrayList.get(userItemArrayList.size() - 2).id);
                    if (searchText == null) {
                        requestBody.put("isSearched", false);
                    } else {
                        requestBody.put("isSearched", true);
                    }
                    HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getchatuser.php", CreateChatRoomActivity.this::onHttpResponse);
                    httpRequest.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 1500);

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
                if (requestType.equals("getChatUser")) {
                    userItemArrayList.clear();//채팅 사용자 아이템을 전부 삭제후 다시 추가해준다.

                    Log.d("서버로부터 넘어온 데이터", result);
                    JSONArray jsonArray = responseBody.getJSONArray("userList");
                    if (jsonArray.length() != 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject data = jsonArray.getJSONObject(i);
                            int id = data.getInt("id");
                            String account = data.getString("account");
                            //넘어온 계정이 자기 자신인 경우에는 리스트에 추가되면 안 되기 때문에 반복문 구문의 최하단으로 이동시켜
                            //다음 반복문이 실행되게 한다.
                            if (account.equals(loginUser.getAccount())) {
                                continue;
                            }
                            String profile = data.getString("profile");
                            String nickname = data.getString("nickname");
                            String name = data.getString("name");
                            boolean isSelected = false;
                            if (selectedUserItemArrayList.size() != 0) {
                                for (int j = 0; j < selectedUserItemArrayList.size(); j++) {
                                    if (selectedUserItemArrayList.get(j).nickname.equals(nickname)) {
                                        isSelected = true;
                                        break;
                                    }
                                }
                            }

                            UserItem userItem = new UserItem();
                            userItem.setAccount(account);
                            userItem.setNickname(nickname);
                            userItem.setProfile(profile);
                            userItem.setId(id);
                            userItem.setSelected(isSelected);
                            userItem.setCurrentParticipant(false);
                            userItem.setName(name);

                            userItemArrayList.add(userItem);

                        }

                        userAdapter.notifyDataSetChanged();
                        if (userItemArrayList.size() != 0) {
                            currentLastId = userItemArrayList.get(userItemArrayList.size() - 1).id;
                        }

                    } else {//데이터가 없는 경우
                        //데이터가 하나도 없는 경우에는 예외가 발생하기 때문에 예외처리 구문에 사용자 리스트를 비워주는 작업을 해줘서
                        //ArrayIndexOutBount 에러를 없애준다.
                        userItemArrayList.clear();
                        userAdapter.notifyDataSetChanged();
                    }

                } else if (requestType.equals("loadNextChatUser")) {
                    boolean isSubstituted = true;
                    JSONArray jsonArray = responseBody.getJSONArray("userList");
                    if (jsonArray.length() != 0) {
                        //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject data = jsonArray.getJSONObject(i);
                            //서버로부터 넘어온 데이터를 변수에 정의
                            int id = data.getInt("id");
                            String account = data.getString("account");
                            if (account.equals(loginUser.getAccount())) {
                                continue;
                            }
                            String profile = data.getString("profile");
                            String nickname = data.getString("nickname");
                            String name = data.getString("name");
                            boolean isSelected = false;
                            if (selectedUserItemArrayList.size() != 0) {
                                for (int j = 0; j < selectedUserItemArrayList.size(); j++) {
                                    if (selectedUserItemArrayList.get(j).nickname.equals(nickname)) {
                                        isSelected = true;
                                    }
                                }
                            }
                            UserItem userItem = new UserItem();
                            userItem.setAccount(account);
                            userItem.setNickname(nickname);
                            userItem.setProfile(profile);
                            userItem.setId(id);
                            userItem.setSelected(isSelected);
                            userItem.setCurrentParticipant(false);
                            userItem.setName(name);


                            //로딩바를 다음 페이지의 첫번째 게시물로 교체하기 위한 작업
                            if (isSubstituted) {
                                //프로그래스바 아이템을 게시물 아이템으로 교체해준다.
                                userItemArrayList.set(userItemArrayList.size() - 1, userItem);


                                //어댑터 notify
                                userAdapter.notifyItemChanged(userItemArrayList.size() - 1);
                                Log.d("사람검색 사이즈:", String.valueOf(userItemArrayList.size()));

                                isSubstituted = false;
                            }
                            //최초에 로딩바와 게시물이 교체가 끝났으면 그 뒤로는 게시물 추가만 해준다.
                            else {
                                userItemArrayList.add(userItem);
                                userAdapter.notifyItemInserted(userItemArrayList.size() - 1);
                            }
                        }
                        loadPossible = true;
                    } else {
                        loadPossible = true;
                        userItemArrayList.remove(userItemArrayList.size() - 1);
                        userAdapter.notifyItemRemoved(userItemArrayList.size());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(getApplicationContext(), "문제가 생겼습니다", Toast.LENGTH_SHORT).show();
        }
    }

    //선택된 사용자 리스트의 아이템에서 삭제 버튼을 클릭하면 호출되는 메소드
    @Override
    public void onRemoveClicked(int position) {

        String nickname = selectedUserItemArrayList.get(position).nickname;
        //체크박스 해제를 해야할 리스트의 index를 구하기 위해 반복문으로 index를 찾는다.
        int index = 0;
        for (int i = 0; i < userItemArrayList.size(); i++) {
            if (userItemArrayList.get(i).nickname.equals(nickname)) {
                index = i;
                break;
            }
        }
        selectedUserItemArrayList.remove(position);
        selectedUserAdapter.notifyItemRemoved(position);
        selectedUserAdapter.notifyDataSetChanged();

        userItemArrayList.get(index).setSelected(false);
        userAdapter.notifyItemChanged(index, "false");

    }

    //전체 사용자 리스트의 아이템을 클릭하면 호출되는 메소드
    @Override
    public void onContainerClicked(int position) {
        if (userItemArrayList.get(position).isSelected) {
            userItemArrayList.get(position).setSelected(false);
            userAdapter.notifyItemChanged(position, "false");
            String nickname = userItemArrayList.get(position).nickname;
            //인덱스를 찾기 위한 반복문
            int index = 0;
            for (int i = 0; i < selectedUserItemArrayList.size(); i++) {
                if (nickname.equals(selectedUserItemArrayList.get(i).nickname)) {
                    index = i;
                    break;
                }
            }
            selectedUserItemArrayList.remove(index);
            selectedUserAdapter.notifyItemRemoved(index);
            selectedUserAdapter.notifyDataSetChanged();
        } else {
            userItemArrayList.get(position).setSelected(true);
            userAdapter.notifyItemChanged(position, "true");
            String profile = userItemArrayList.get(position).profile;
            String account = userItemArrayList.get(position).account;
            String nickname = userItemArrayList.get(position).nickname;
            SelectedUserItem selectedUserItem = new SelectedUserItem();
            selectedUserItem.setProfile(profile);
            selectedUserItem.setAccount(account);
            selectedUserItem.setNickname(nickname);
            selectedUserItemArrayList.add(selectedUserItem);
            selectedUserAdapter.notifyItemInserted(selectedUserItemArrayList.size() - 1);
        }
    }

    @Override
    public void onCheckBoxClicked(int position) {
        if (userItemArrayList.get(position).isSelected) {
            userItemArrayList.get(position).setSelected(false);
            userAdapter.notifyItemChanged(position, "false");
            String nickname = userItemArrayList.get(position).nickname;
            //인덱스를 찾기 위한 반복문
            int index = 0;
            for (int i = 0; i < selectedUserItemArrayList.size(); i++) {
                if (nickname.equals(selectedUserItemArrayList.get(i).nickname)) {
                    index = i;
                    break;
                }
            }
            selectedUserItemArrayList.remove(index);
            selectedUserAdapter.notifyItemRemoved(index);
            selectedUserAdapter.notifyDataSetChanged();
        } else {
            userItemArrayList.get(position).setSelected(true);
            userAdapter.notifyItemChanged(position, "true");
            String profile = userItemArrayList.get(position).profile;
            String account = userItemArrayList.get(position).account;
            String nickname = userItemArrayList.get(position).nickname;
            SelectedUserItem selectedUserItem = new SelectedUserItem();
            selectedUserItem.setProfile(profile);
            selectedUserItem.setAccount(account);
            selectedUserItem.setNickname(nickname);
            selectedUserItemArrayList.add(selectedUserItem);
            selectedUserAdapter.notifyItemInserted(selectedUserItemArrayList.size() - 1);
        }
    }
}
