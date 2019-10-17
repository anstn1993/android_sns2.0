package com.example.sns;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.sns.JoinActivity.IP_ADDRESS;
import static com.example.sns.MainActivity.iv_notificationDot;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.NotificationRecyclerViewListener, HttpRequest.OnHttpResponseListener, NotificationReceiver.OnPushNotificationListener {

    private String TAG = "NotificationActivity";
    //프래그먼트 메니저
    public static FragmentManager fragmentManager;

    //좋아요 리스트 프래그먼트 객체 선언
    public static LikeListFragment likeListFragment;
    public static AccountPageFragment accountPageFragment;
    public static PostDetailFragment postDetailFragment;
    public static FollowingListFragment followingListFragment;
    public static FollowerListFragment followerListFragment;
    public static HashTagPostListFragment hashTagPostListFragment;
    public static SearchedPlaceListFragment searchedPlaceListFragment;

    //리사이클러뷰 셋
    private LinearLayoutManager linearLayoutManager;
    private NotificationAdapter notificationAdapter;
    private RecyclerView rv_notification;
    private ArrayList<NotificationItem> notificationItemArrayList;

    //직전에 로드된 알림의 개수
    protected int listSize;

    public static NotificationActivity notificationActivity;

    private boolean loadPossible = true;

    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean isFromPush = false;//푸시알림으로 인해서 이 액티비티로 진입한 경우
    private Intent intentFromPush;//푸시알림으로 전달된 intent데이터를 담을 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate 호출");
        setContentView(R.layout.activity_notification);

        swipeRefreshLayout = findViewById(R.id.refresh_layout);
        listSize = 20;
        notificationActivity = NotificationActivity.this;

        //프래그먼트 객체 선언
        likeListFragment = new LikeListFragment();
        accountPageFragment = new AccountPageFragment();
        postDetailFragment = new PostDetailFragment();
        followingListFragment = new FollowingListFragment();
        followerListFragment = new FollowerListFragment();
        hashTagPostListFragment = new HashTagPostListFragment();
        searchedPlaceListFragment = new SearchedPlaceListFragment();
        //프래그먼트 매니저 초기화
        fragmentManager = getSupportFragmentManager();

        setRecyclerView();


        //addOnScrollListener는 리사이클러뷰를 스크롤하는 순간을 캐치한다.
        rv_notification.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                //아아템의 수가 20개 이상인 경우에만 페이징을 실시한다.
                if (notificationItemArrayList.size() >= 20) {
                    //마지막으로 보이는 아이템의 index가 전체 아이템 수에서 1을 뺀 값과 같고 현재 화면에 보이는 아이템 수가 12개이며 마지막으로 완전히 보이는 아이템의 index가
                    //전체 아이템 수에서 1을 뺀 값과 같을 때만 페이징을 실행한다.
                    if (lastCompletelyVisibleItemPosition == totalCount - 1 && loadPossible == true) {
                        Log.d("페이징 조건", "부합");
                        //다음 페이지를 로드한다.
                        //param:현재 로드되어있는 데이터의 수(다음 페이지에 로드되어야 할 첫번째 게시물의 index)
                        loadNextPage();
                    }
                }


            }
        });

        //새로고침 리스너
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                listSize = 20;//가져올 아이템 수 초기화
                getNotificationData("getNotification", LoginActivity.account, 0, listSize);//알림 목록 데이터 가져오기
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    private void setRecyclerView() {
        notificationItemArrayList = new ArrayList<>();

        rv_notification = findViewById(R.id.recyclerview_notification);
        rv_notification.setHasFixedSize(true);
        //레이아웃 메니저 설정
        linearLayoutManager = new LinearLayoutManager(this);
        rv_notification.setLayoutManager(linearLayoutManager);
        //어댑터 설정
        notificationAdapter = new NotificationAdapter(notificationItemArrayList, this);
        notificationAdapter.setOnClickListener(this);
        rv_notification.setAdapter(notificationAdapter);

    }

    protected void getNotificationData(String requestType, String account, int lastId, int listSize) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("requestType", requestType);
            requestBody.put("account", account);
            requestBody.put("lastId", lastId);
            requestBody.put("listSize", listSize);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getnotification.php", this);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    httpRequest.execute();
                }
            });
            thread.start();
            try {
                thread.join(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    //알림 목록의 최하단으로 이동하면 페이징 처리를 위해서 실행될 메소드
    public void loadNextPage() {
        loadPossible = false;
        //null을 넣어서 뷰타입이 프로그래스 아이템이 추가되게 한다.
        notificationItemArrayList.add(null);
        notificationAdapter.notifyItemInserted(notificationItemArrayList.size() - 1);
        //핸들러를 통해서 2초 뒤에 해당 기능을 실행한다.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("requestType", "loadNextNotification");
                    requestBody.put("account", LoginActivity.account);
                    requestBody.put("lastId", notificationItemArrayList.get(notificationItemArrayList.size() - 2).id);
                    requestBody.put("listSize", 20);
                    HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getnotification.php", NotificationActivity.this::onHttpResponse);
                    httpRequest.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 1500);
    }

    //알림 리스트를 클릭하면 확인처리를 해주는 메소드
    private void updateCheckNotification(int id) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("id", id);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "updatechecknotification.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void processFollow(boolean followState, String followedAccount, String followedNickname, String followingAccount, int position) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("followState", followState);
            requestBody.put("followedAccount", followedAccount);
            requestBody.put("followedNickname", followedNickname);
            requestBody.put("followingAccount", followingAccount);
            requestBody.put("position", position);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "processfollow.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //팔로우를 한 경우 fcm을 통해서 팔로우를 당한 사용자에게 알림을 전달하기 위한 메소드
    private void pushNotification(String receiver, String title, String body, String click_action, String category, String sender) {
        try {
            Log.d(TAG, "pushNotification 메소드 호출");
            JSONObject requestBody = new JSONObject();
            requestBody.put("account", receiver);//알림의 수신자
            requestBody.put("title", title);//알림의 제목
            requestBody.put("body", body);//알림의 내용
            requestBody.put("click_action", click_action);//푸시알림을 눌렀을 때 이동할 액티비티 혹은 프래그먼트
            requestBody.put("category", category);//알림의 카테고리
            requestBody.put("userAccount", sender);//알림 송신자
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "pushnotification.php", this);
            httpRequest.execute();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSON ERROR:" + e.getMessage());
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
                if (requestType.equals("getNotification")) {//알림 데이터들을 가져오는 통신
                    notificationItemArrayList.clear();
                    JSONArray jsonArray = responseBody.getJSONArray("notificationList");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        NotificationItem notificationItem = new Gson().fromJson(data.toString(), NotificationItem.class);
                        notificationItemArrayList.add(notificationItem);
                    }

                    notificationAdapter.notifyDataSetChanged();

                    //푸시알림으로 넘어온 경우
                    if (isFromPush) {
                        String userAccount = intentFromPush.getStringExtra("userAccount");
                        String profile = intentFromPush.getStringExtra("profile");
                        String category = intentFromPush.getStringExtra("category");
                        String click_action = intentFromPush.getStringExtra("click_action");
                        String message = intentFromPush.getStringExtra("message");
                        String image = null;
                        if (intentFromPush.getStringExtra("image") != null) {
                            image = intentFromPush.getStringExtra("image");
                        }

                        if (notificationItemArrayList.size() != 0) {
                            //해당 알림을 확인한 것으로 처리하기 위해 서버의 notification 테이블의 is_checked 필드의 값을 1(true)로 update해준다.
                            updateCheckNotification(notificationItemArrayList.get(0).id);
                        }

                        //좋아요 알림인 경우
                        if (category.equals("like")) {
                            //아직 알림을 확인하지 않은 경우
                            if (notificationItemArrayList.get(0).isChecked == false) {
                                //해당 알림을 확인했기 때문에 알림리스트의 배경 색이 흰색으로 바뀌게 한다.
                                notificationItemArrayList.get(0).setChecked(true);
                                notificationAdapter.notifyItemChanged(0, "checked");
                            }

                            PostDetailFragment postDetailFragment = new PostDetailFragment();//그리드 이미지를 누르면 보여줄 프레그먼트 선언

                            Bundle bundle = new Bundle(); //서버에서 데이터를 조회해서 가져오기 위해 필요한 게시물 번호를 번들에 담아서 넘겨준다.
                            bundle.putInt("postNum", notificationItemArrayList.get(0).getPostNum());
                            bundle.putString("parentActivity", "NotificationActivity");
                            postDetailFragment.setArguments(bundle);
                            //프래그먼트를 프래임 레이아웃에 붙여준다.
                            NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, postDetailFragment, "fromNotification").addToBackStack(null).commit();
                            NotificationActivity.fragmentManager.beginTransaction().show(postDetailFragment).commit();
                        }
                        //팔로우 알림인 경우
                        else if (category.equals("follow")) {

                            if (notificationItemArrayList.get(0).isChecked == false) {//아직 알림을 확인하지 않은 경우
                                notificationItemArrayList.get(0).setChecked(true);//해당 알림을 확인했기 때문에 알림리스트의 배경 색이 흰색으로 바뀌게 한다.
                                notificationAdapter.notifyItemChanged(0, "checked");
                            }

                            //팔로우를 한 사용자의 페이지로 이동하게 된다.
                            //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                            Bundle bundle = new Bundle();
                            bundle.putString("account", userAccount);
                            bundle.putString("parentActivity", "NotificationActivity");
                            bundle.putBoolean("isMyPost", false);
                            NotificationActivity.accountPageFragment.setArguments(bundle);
                            NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, NotificationActivity.accountPageFragment).addToBackStack(null).commit();
                            NotificationActivity.fragmentManager.beginTransaction().show(NotificationActivity.accountPageFragment).commit();
                        }
                        //댓글, 대댓글 알림인 경우
                        else {
                            //아직 알림을 확인하지 않은 경우
                            if (notificationItemArrayList.get(0).isChecked == false) {
                                //해당 알림을 확인했기 때문에 알림리스트의 배경 색이 흰색으로 바뀌게 한다.
                                notificationItemArrayList.get(0).setChecked(true);
                                notificationAdapter.notifyItemChanged(0, "checked");
                            }
                            //그리드 이미지를 누르면 보여줄 프레그먼트 선언
                            PostDetailFragment postDetailFragment = new PostDetailFragment();

                            //서버에서 데이터를 조회해서 가져오기 위해 필요한 게시물 번호를 번들에 담아서 넘겨준다.
                            Bundle bundle = new Bundle();
                            bundle.putInt("postNum", notificationItemArrayList.get(0).getPostNum());
                            bundle.putString("parentActivity", "NotificationActivity");
                            bundle.putBoolean("isCommentNotification", true);
                            postDetailFragment.setArguments(bundle);
                            //프래그먼트를 프래임 레이아웃에 붙여준다.
                            NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, postDetailFragment, "fromNotification").addToBackStack(null).commit();
                            NotificationActivity.fragmentManager.beginTransaction().show(postDetailFragment).commit();
                        }
                        isFromPush = false;
                    }
                } else if (requestType.equals("loadNextNotification")) {//페이징 통신
                    JSONArray jsonArray = responseBody.getJSONArray("notificationList");
                    if (jsonArray.length() != 0) {
                        boolean isSubstituted = true;
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject data = jsonArray.getJSONObject(i);
                            NotificationItem notificationItem = new Gson().fromJson(data.toString(), NotificationItem.class);
                            //로딩바를 다음 페이지의 첫번째 게시물로 교체하기 위한 작업
                            if (isSubstituted) {
                                //프로그래스바 아이템을 게시물 아이템으로 교체해준다.
                                notificationItemArrayList.set(notificationItemArrayList.size() - 1, notificationItem);
                                //어댑터 notify
                                notificationAdapter.notifyItemChanged(notificationItemArrayList.size() - 1);
                                Log.d("사람검색 사이즈:", String.valueOf(notificationItemArrayList.size()));

                                isSubstituted = false;
                            }
                            //최초에 로딩바와 게시물이 교체가 끝났으면 그 뒤로는 게시물 추가만 해준다.
                            else {
                                notificationItemArrayList.add(notificationItem);
                                notificationAdapter.notifyItemInserted(notificationItemArrayList.size() - 1);
                            }

                            listSize += 1;
                            loadPossible = true;
                        }

                    } else {
                        loadPossible = true;
                        notificationItemArrayList.remove(notificationItemArrayList.size() - 1);
                        notificationAdapter.notifyItemRemoved(notificationItemArrayList.size());
                    }


                } else if (requestType.equals("processFollow")) {//팔로우 통신
                    boolean isFollowing = responseBody.getBoolean("isFollowing");
                    int position = responseBody.getInt("position");
                    String nickname = responseBody.getString("followedNickname");
                    //팔로우를 한 경우
                    if (isFollowing) {
                        //팔로우 상태를 true로 만든다.
                        String userAccount = notificationItemArrayList.get(position).userAccount;
                        for (int i = 0; i < notificationItemArrayList.size() - 1; i++) {
                            if (notificationItemArrayList.get(i).userAccount.equals(userAccount)) {
                                notificationItemArrayList.get(i).setFollowing(true);
                                //어댑터에 notify
                                notificationAdapter.notifyItemChanged(i, "true");
                            }
                        }


                        Toast.makeText(getApplicationContext(), nickname + "님을 팔로우 하셨습니다!", Toast.LENGTH_SHORT).show();


                        //팔로우를 당한 사용자 단말에 push알림을 보내준다.
                        String receiver = notificationItemArrayList.get(position).userAccount;
                        String title = "SNS";
                        String body = LoginActivity.nickname + "님이 회원님을 팔로우하기 시작했습니다.";
                        String click_action = "AccountPageFragment";
                        String category = "follow";
                        String sender = LoginActivity.account;
                        pushNotification(receiver, title, body, click_action, category, sender);

                    }
                    //팔로우를 취소한 경우
                    else {
                        //팔로우 상태를 true로 만든다.
                        String userAccount = notificationItemArrayList.get(position).userAccount;
                        for (int i = 0; i < notificationItemArrayList.size() - 1; i++) {
                            if (notificationItemArrayList.get(i).userAccount.equals(userAccount)) {
                                notificationItemArrayList.get(i).setFollowing(false);
                                //어댑터에 notify
                                notificationAdapter.notifyItemChanged(i, "false");
                            }
                        }


                        Toast.makeText(getApplicationContext(), nickname + "님을 언팔로우 하셨습니다", Toast.LENGTH_SHORT).show();
                    }
                } else if (requestType.equals("updateCheck")) {//알림 확인 업데이트 통신
                    //딱히 처리해줄 게 ....
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {//서버로부터 respose가 없는 경우
            Toast.makeText(getApplicationContext(), "문제가 생겼습니다", Toast.LENGTH_SHORT).show();
        }
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
        iv_notificationDot.setVisibility(View.INVISIBLE);//알림 표시 점을 없애준다.
        getNotificationData("getNotification", LoginActivity.account, 0, listSize);//알림 목록 데이터 가져오기
    }


    //알림리스트에서 프로필 사진을 클릭할 때 호출되는 메소드
    @Override
    public void onProfileClicked(int position) {
        Log.d("알림 화면 onProfileClicked", "호출");
        //프로필 사진을 클릭하면 해당 사용자의 페이지로 이동하게 된다.
        //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
        Bundle bundle = new Bundle();
        bundle.putString("account", notificationItemArrayList.get(position).userAccount);
        bundle.putString("parentActivity", "NotificationActivity");
        bundle.putBoolean("isMyPost", false);
        NotificationActivity.accountPageFragment.setArguments(bundle);
        NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, NotificationActivity.accountPageFragment).addToBackStack(null).commit();
        NotificationActivity.fragmentManager.beginTransaction().show(NotificationActivity.accountPageFragment).commit();
    }

    //알림리스트에서 닉네임을 클릭할 때 호출되는 메소드
    @Override
    public void onNicknameClicked(int position) {
        Log.d("알림 화면 onNicknameClicked", "호출");
        //프로필 사진을 클릭하면 해당 사용자의 페이지로 이동하게 된다.
        //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
        Bundle bundle = new Bundle();
        bundle.putString("account", notificationItemArrayList.get(position).userAccount);
        bundle.putString("parentActivity", "NotificationActivity");
        bundle.putBoolean("isMyPost", false);
        NotificationActivity.accountPageFragment.setArguments(bundle);
        NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, NotificationActivity.accountPageFragment).addToBackStack(null).commit();
        NotificationActivity.fragmentManager.beginTransaction().show(NotificationActivity.accountPageFragment).commit();
    }

    //알림리스트에서 본문을 클릭할 때 호출되는 메소드
    @Override
    public void onBodyClicked(int position, String category) {
        Log.d("알림 화면 onBodyClicked", "호출");

        //해당 알림을 확인한 것으로 처리하기 위해 서버의 notification 테이블의 is_checked 필드의 값을 1(true)로 update해준다.
        updateCheckNotification(notificationItemArrayList.get(position).id);


        //좋아요, 댓글, 대댓글 알림인 경우 게시물 상세 페이지로 이동
        if (category.equals("like") || category.equals("comment") || category.equals("childcomment")) {

            //아직 알림을 확인하지 않은 경우
            if (notificationItemArrayList.get(position).isChecked == false) {
                //해당 알림을 확인했기 때문에 알림리스트의 배경 색이 흰색으로 바뀌게 한다.
                notificationItemArrayList.get(position).setChecked(true);
                notificationAdapter.notifyItemChanged(position, "checked");
            }

            //그리드 이미지를 누르면 보여줄 프레그먼트 선언
            PostDetailFragment postDetailFragment = new PostDetailFragment();
            //서버에서 데이터를 조회해서 가져오기 위해 필요한 게시물 번호를 번들에 담아서 넘겨준다.
            Bundle bundle = new Bundle();
            bundle.putInt("postNum", notificationItemArrayList.get(position).getPostNum());
            bundle.putString("parentActivity", "NotificationActivity");
            if(category.equals("comment") || category.equals("childcomment")){//댓글, 대댓글 알림의 경우 바로 댓글 액티비티로 이동하기 위한 boolean값 추가
                bundle.putBoolean("isCommentNotification", true);
            }
            postDetailFragment.setArguments(bundle);

            //프래그먼트를 프래임 레이아웃에 붙여준다.
            NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, postDetailFragment).addToBackStack(null).commit();
            NotificationActivity.fragmentManager.beginTransaction().show(postDetailFragment).commit();
        }
        //팔로우 알림인 경우 팔로우를 한 사람의 페이지로 이동
        else if (category.equals("follow")) {

            //아직 알림을 확인하지 않은 경우
            if (notificationItemArrayList.get(position).isChecked == false) {
                //해당 알림을 확인했기 때문에 알림리스트의 배경 색이 흰색으로 바뀌게 한다.
                notificationItemArrayList.get(position).setChecked(true);
                notificationAdapter.notifyItemChanged(position, "checked");
            }
            //팔로우를 한 사용자의 페이지로 이동하게 된다.
            //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
            Bundle bundle = new Bundle();
            bundle.putString("account", notificationItemArrayList.get(position).userAccount);
            bundle.putString("parentActivity", "NotificationActivity");
            bundle.putBoolean("isMyPost", false);
            NotificationActivity.accountPageFragment.setArguments(bundle);
            NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, NotificationActivity.accountPageFragment).addToBackStack(null).commit();
            NotificationActivity.fragmentManager.beginTransaction().show(NotificationActivity.accountPageFragment).commit();
        }
    }

    //알림리스트에서 우측의 이미지를 클릭할 때 호출되는 메소드
    @Override
    public void onImageClicked(int position) {
        Log.d("알림 화면 onImageClicked", "호출");

        //해당 알림을 확인한 것으로 처리하기 위해 서버의 notification 테이블의 is_checked 필드의 값을 1(true)로 update해준다.
        updateCheckNotification(notificationItemArrayList.get(position).id);

        //좋아요 알림인 경우
        if (notificationItemArrayList.get(position).category.equals("like")) {
            //아직 알림을 확인하지 않은 경우
            if (notificationItemArrayList.get(position).isChecked == false) {
                //해당 알림을 확인했기 때문에 알림리스트의 배경 색이 흰색으로 바뀌게 한다.
                notificationItemArrayList.get(position).setChecked(true);
                notificationAdapter.notifyItemChanged(position, "checked");
            }
        }
        //팔로우 알림인 경우
        else if (notificationItemArrayList.get(position).category.equals("follow")) {
            //아직 알림을 확인하지 않은 경우
            if (notificationItemArrayList.get(position).isChecked == false) {
                //해당 알림을 확인했기 때문에 알림리스트의 배경 색이 흰색으로 바뀌게 한다.
                notificationItemArrayList.get(position).setChecked(true);
                notificationAdapter.notifyItemChanged(position, "checked");
            }
        }
        //댓글 알림인 경우
        else {
            //아직 알림을 확인하지 않은 경우
            if (notificationItemArrayList.get(position).isChecked == false) {
                //해당 알림을 확인했기 때문에 알림리스트의 배경 색이 흰색으로 바뀌게 한다.
                notificationItemArrayList.get(position).setChecked(true);
                notificationAdapter.notifyItemChanged(position, "checked");
            }
        }


        //그리드 이미지를 누르면 보여줄 프레그먼트 선언
        PostDetailFragment postDetailFragment = new PostDetailFragment();

        //서버에서 데이터를 조회해서 가져오기 위해 필요한 게시물 번호를 번들에 담아서 넘겨준다.
        Bundle bundle = new Bundle();
        bundle.putInt("postNum", notificationItemArrayList.get(position).getPostNum());
        bundle.putString("parentActivity", "NotificationActivity");
        postDetailFragment.setArguments(bundle);
        //프래그먼트를 프래임 레이아웃에 붙여준다.
        NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, postDetailFragment, "fromNotification").addToBackStack(null).commit();
        NotificationActivity.fragmentManager.beginTransaction().show(postDetailFragment).commit();
    }

    //알림리스트에서 우측의 팔로우 버튼을 클릭할 때 호출되는 메소드
    @Override
    public void onFollowClicked(int position) {
        Log.d("알림 화면 onFollowClicked", "호출");

        //해당 position의 사용자 account
        String followedAccount = notificationItemArrayList.get(position).userAccount;

        //해당 position의 사용자 nickname
        String followedNickname = notificationItemArrayList.get(position).nickname;

        //팔로우를 하지 않은 상태에서 팔로우를 하려는 경우
        if (notificationItemArrayList.get(position).isFollowing == false) {

            //팔로잉 상태로 전환
            processFollow(true, followedAccount, followedNickname, LoginActivity.account, position);
        }
        //팔로우를 했던 상태에서 팔로우를 취소하려는 경우
        else {

            Dialog dialog = new Dialog(NotificationActivity.this);
            dialog.setContentView(R.layout.unfollow_check_box);

            //다이얼로그의 크기 조정
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            Window window = dialog.getWindow();
            window.setAttributes(layoutParams);

            CircleImageView cv_profile;
            TextView tv_ment, tv_cancel, tv_unfollow;

            cv_profile = dialog.findViewById(R.id.circleimageview_profile);
            tv_ment = dialog.findViewById(R.id.textView_ment);
            tv_cancel = dialog.findViewById(R.id.textview_cancel);
            tv_unfollow = dialog.findViewById(R.id.textview_unfollow);

            //프로필 이미지
            String image = notificationItemArrayList.get(position).profile;


            //프로필 사진 설정
            Glide.with(getApplicationContext())
                    .load("http://" + IP_ADDRESS + "/profileimage/" + image)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop())
                    .into(cv_profile);

            //언팔로우 다이얼로그 멘트 설정
            tv_ment.setText("생각이 바뀌시면 " + followedNickname + "님을 다시 팔로우할 수 있습니다.");

            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            tv_unfollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    //언팔로우 상태로 전환
                    processFollow(false, followedAccount, followedNickname, LoginActivity.account, position);
                }
            });

            dialog.show();

        }
    }

    @Override
    public void onBackPressed() {

        //프래그먼트 스택에 프래그먼트가 존재하면
        if (NotificationActivity.fragmentManager.getBackStackEntryCount() != 0) {
            Log.d("프래그먼트 스택 존재", "호출");
            NotificationActivity.fragmentManager.popBackStackImmediate();//스택의 최상단에 있는 프래그먼트를 화면에 붙이고 그 전에 붙어있던 fragment는 destroy.
            Intent intent = new Intent(getApplicationContext(), EmptyActivity.class);//프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
            startActivity(intent);


        }
        //현재 화면에 액티비티라면 PostActivity로 이동
        else {
//            super.onBackPressed();
            MainActivity.tabHost.setCurrentTab(0);
        }
    }

    //푸시알림으로 접근하면 콜백되는 메소드
    @Override
    public void onPushReceived(Intent intent) {
        isFromPush = true;
        intentFromPush = intent;
    }
}

