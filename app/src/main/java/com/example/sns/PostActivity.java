package com.example.sns;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.sns.LoginActivity.account;
import static com.example.sns.LoginActivity.nickname;


public class PostActivity extends AppCompatActivity implements PostAdapter.PostRecyclerViewListener, HttpRequest.OnHttpResponseListener {
    String TAG = "PostActivity";

    ImageButton btn_chat, btn_streaming;
    TextView tv_title, tv_article, tv_messageCount;

    Button btn_tothetop;

    //새로고침 레이아웃
    SwipeRefreshLayout swipeRefreshLayout;

    RecyclerView rv_post;
    LinearLayoutManager layoutManager;
    PostAdapter postAdapter;
    public ArrayList<PostItem> postItemArrayList;

    //자신에게 온 총 메세지 수
    int unCheckedMessageCount = 0;


    //게시물을 업로드 한 경우를 캐치하기 위한 static boolean(메인 액티비티에서 사용)
    public static boolean isUploaded = false;
    //게시물을 수정한 경우를 캐지하기 위한 static boolean(메인 액티비티에서 사용)
    public static boolean isEditted = false;

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


    //채팅 서비스로부터 메세지를 받는 핸들러
    public static Handler handler;

    private boolean loadPossible = true;
    //동영상 게시물의 비디오 활성화 여부
    private boolean isMuted = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Log.d("게시물 액티비티 onCreate", "호출");

        //프래그먼트 객체 선언
        likeListFragment = new LikeListFragment();
        accountPageFragment = new AccountPageFragment();
        postDetailFragment = new PostDetailFragment();
        followingListFragment = new FollowingListFragment();
        followerListFragment = new FollowerListFragment();
        hashTagPostListFragment = new HashTagPostListFragment();
        searchedPlaceListFragment = new SearchedPlaceListFragment();


        btn_chat = findViewById(R.id.btn_chat);
        btn_streaming = findViewById(R.id.btn_streaming);
        btn_tothetop = findViewById(R.id.button_tothetop);


        tv_title = findViewById(R.id.textview_title);
        tv_article = findViewById(R.id.textview_article);
        tv_messageCount = findViewById(R.id.textview_totalmessagecount);

        swipeRefreshLayout = findViewById(R.id.refresh_layout);

        //포스트 리사이클러뷰 세팅
        setRecyclerView();

        //프래그먼트 매니저 초기화
        fragmentManager = getSupportFragmentManager();

        //채팅 서비스로부터 메세지를 받는 핸들러
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //서비스로부터 메세지가 도착하면 미확인 메세지 수를 증가시킨다.
                if (tv_messageCount.getVisibility() == View.GONE) {
                    tv_messageCount.setVisibility(View.VISIBLE);
                }
                unCheckedMessageCount += 1;
                tv_messageCount.setText(String.valueOf(unCheckedMessageCount));
            }
        };

        //최상단의 투명 버튼 리스너
        btn_tothetop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //최상단의 투명 버튼을 누르면 제일 첫 포스트로 이동
                        rv_post.smoothScrollToPosition(0);
                    }
                }, 100);

            }
        });


        btn_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                startActivity(intent);
            }
        });

        btn_streaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StreamingActivity.class);
                startActivity(intent);
            }
        });

        //새로고침 스와이프 리스너
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            //새로고침을 위해서 아래로 스와이프했을 때 호출되는 콜백 메소드
            @Override
            public void onRefresh() {


                getPost();//게시물 데이터를 서버로부터 가져오는 메소드

                //새로고침이 완료되면 새로고침 완료
                //이 메소드를 호출하지 않으면 새로고침 아이콘이 사라지지 않는다.
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        //addOnScrollListener는 리사이클러뷰를 스크롤하는 순간을 캐치한다.
        rv_post.addOnScrollListener(new RecyclerView.OnScrollListener() {
            //리사이클러뷰가 스크롤된 후 콜백되는 메소드
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                //화면에서 가장 처음으로 보이는 아이템의 index
                int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
//                Log.d("현재 화면의 첫번째 index", String.valueOf(firstVisibleItemPosition));
                //findLastVisibleItemPosition은 현재 화면에 보이는 뷰 중 가장 마지막 뷰의 position을 리턴해준다.
                //즉 이 변수는 현재 화면에 보이는 아이템 중 가장 마지막 아이템의 index를 담는다
                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
//                Log.d("현재 화면의 마지막 index", String.valueOf(lastVisibleItemPosition));
                //이 변수는 전체 리사이클러뷰의 아이템 개수를 담는다.
                int totalCount = recyclerView.getAdapter().getItemCount();
//                Log.d("전체 아이템 개수", String.valueOf(totalCount));
                //이 변수는 현재 화면에 보이는 아이템의 개수를 담는다.(내 경우에는 1~2를 왔다갔다 함)
                int visibleItemCount = recyclerView.getChildCount();
//                Log.d("화면에 보여지는 아이템 개수", String.valueOf(visibleItemCount));

                //가장 첫번째로 보이는 뷰
                View firstItemView = ((LinearLayoutManager) recyclerView.getLayoutManager()).findViewByPosition(firstVisibleItemPosition);
                //가장 마지막으로 보이는 뷰
                View lastItemView = ((LinearLayoutManager) recyclerView.getLayoutManager()).findViewByPosition(lastVisibleItemPosition);
                //첫번째 아이템이 화면에 보이는 비중
                float firstItemPercentage = ((firstItemView.getHeight() - Math.abs(firstItemView.getY())) / firstItemView.getHeight()) * 100;
                //두번째 아이템이 화면에 보이는 비중
                float lastItemPercentage = ((lastItemView.getHeight() - Math.abs(lastItemView.getY())) / lastItemView.getHeight()) * 100;
                if (postItemArrayList.get(lastVisibleItemPosition) != null) {

                    //첫번째로 보이는 아이템이 동영상 게시물이고 재생중이지 않은 경우
                    if (postItemArrayList.get(firstVisibleItemPosition).type.equals("video") && !postItemArrayList.get(firstVisibleItemPosition).isPlaying) {
                        //첫번재로 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                        if (firstItemPercentage >= 50 && lastItemPercentage < 50) {
                            postAdapter.notifyItemChanged(firstVisibleItemPosition, "playVideo");//처음으로 보이는 아이템 play
                            postItemArrayList.get(firstVisibleItemPosition).setPlaying(true);
                        }
                    }

                    //마지막으로 보이는 아이템이 동영상 게시물이고 재생중이지 않은 경우
                    if (postItemArrayList.get(lastVisibleItemPosition).type.equals("video") && !postItemArrayList.get(lastVisibleItemPosition).isPlaying) {
                        //첫번재로 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                        if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                            postAdapter.notifyItemChanged(lastVisibleItemPosition, "playVideo");//처음으로 보이는 아이템 play
                            postItemArrayList.get(lastVisibleItemPosition).setPlaying(true);
                        }
                    }

                    //첫번째로 보이는 아이템이 동영상 게시물이고 현재 재생중인 경우
                    if (postItemArrayList.get(firstVisibleItemPosition).type.equals("video") && postItemArrayList.get(firstVisibleItemPosition).isPlaying) {
                        //마지막에 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                        if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                            //마지막으로 보이는 아이템이 동영상 게시물인 경우
                            if (postItemArrayList.get(lastVisibleItemPosition).type.equals("video")) {
                                postAdapter.notifyItemChanged(lastVisibleItemPosition, "playVideo");//마지막으로 보이는 아이템을 play
                                postItemArrayList.get(firstVisibleItemPosition).setPlaying(false);//첫번째 아이템의 플레이상태를 false
                                postItemArrayList.get(lastVisibleItemPosition).setPlaying(true);//마지막 아이템의 플레이상태를 true
                            }
                            //마지막으로 보이는 아이템이 이미지 게시물인 경우
                            else {
                                //재생중인 동영상을 release 해준다.
                                postItemArrayList.get(firstVisibleItemPosition).setPlaying(false);
                                postAdapter.releasevideo();
                            }
                        }
                    }

                    //마지막으로 보이는 아이템이 동영상 게시물이고 현재 재생중인 경우
                    if (postItemArrayList.get(lastVisibleItemPosition).type.equals("video") && postItemArrayList.get(lastVisibleItemPosition).isPlaying) {
                        //첫번째로 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                        if (lastItemPercentage < 50 && firstItemPercentage >= 50) {
                            //첫번째로 보이는 아이템이 동영상 게시물인 경우
                            if (postItemArrayList.get(firstVisibleItemPosition).type.equals("video")) {
                                postAdapter.notifyItemChanged(firstVisibleItemPosition, "playVideo");//첫번째로 보이는 아이템을 play
                                postItemArrayList.get(lastVisibleItemPosition).setPlaying(false);//마지막 아이템의 플레이상태를 false
                                postItemArrayList.get(firstVisibleItemPosition).setPlaying(true);//첫번째 아이템의 플레이상태를 true
                            }
                            //첫번째로 보이는 아이템이 이미지 게시물인 경우
                            else {
                                //재생중인 동영상을 release해준다.
                                postItemArrayList.get(lastVisibleItemPosition).setPlaying(false);
                                postAdapter.releasevideo();
                            }
                        }
                    }

                    //첫번째로 보이는 아이템이 이미지 게시물이고 마지막으로 보이는 아이템이 동영상이고 그 동영상이 재생중인 경우
                    if (postItemArrayList.get(firstVisibleItemPosition).type.equals("image") && postItemArrayList.get(lastVisibleItemPosition).type.equals("video") && postItemArrayList.get(lastVisibleItemPosition).isPlaying) {
                        //동영상 게시물이 화면에 보이는 비중이 더 낮아지는 경우
                        if (firstItemPercentage >= 50 && lastItemPercentage < 50) {
                            postAdapter.releasevideo();//동영상 release
                            postItemArrayList.get(lastVisibleItemPosition).setPlaying(false);//플레이 상태 false
                        }
                    }

                    //첫번째로 보이는 아이템이 이미지 게시물이고 마지막으로 보이는 아이템이 동영상이고 그 동영상이 재생중이지 않은 경우
                    if (postItemArrayList.get(firstVisibleItemPosition).type.equals("image") && postItemArrayList.get(lastVisibleItemPosition).type.equals("video") && !postItemArrayList.get(lastVisibleItemPosition).isPlaying) {
                        //동영상 게시물이 화면에 보이는 비중이 더 높아지는 경우
                        if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                            postAdapter.notifyItemChanged(lastVisibleItemPosition, "playVideo");//동영상 play
                            postItemArrayList.get(lastVisibleItemPosition).setPlaying(true);//플레이 상태 true
                        }
                    }

                    //마지막으로 보이는 아이템이 이미지 게시물이고 첫번째로 보이는 아이템이 동영상이고 그 동영상이 재생중인 경우
                    if (postItemArrayList.get(lastVisibleItemPosition).type.equals("image") && postItemArrayList.get(firstVisibleItemPosition).type.equals("video") && postItemArrayList.get(firstVisibleItemPosition).isPlaying) {
                        //동영상 게시물이 화면에 보이는 비중이 더 낮아지는 경우
                        if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                            postAdapter.releasevideo();//동영상 release
                            postItemArrayList.get(firstVisibleItemPosition).setPlaying(false);//플레이 상태 false

                        }
                    }

                    //마지막으로 보이는 아이템이 이미지 게시물이고 첫번째로 보이는 아이템이 동영상이고 그 동영상이 재생중이지 않은 경우
                    if (postItemArrayList.get(lastVisibleItemPosition).type.equals("image") && postItemArrayList.get(firstVisibleItemPosition).type.equals("video") && !postItemArrayList.get(firstVisibleItemPosition).isPlaying) {
                        //동영상 게시물이 화면에 보이는 비중이 더 높아지는 경우
                        if (firstItemPercentage >= 50 && lastItemPercentage < 50) {
                            postAdapter.notifyItemChanged(firstVisibleItemPosition, "playVideo");//동영상 play
                            postItemArrayList.get(firstVisibleItemPosition).setPlaying(true);//플레이 상태 true
                        }
                    }
                }

//                Log.d(TAG, "첫번째 뷰의 y축: "+String.valueOf(Math.abs(firstItemView.getY())));
//                Log.d(TAG, "첫번째 뷰의 height: "+String.valueOf(firstItemView.getHeight()));
//                Log.d(TAG, "마지막 뷰의 y축: "+String.valueOf(Math.abs(lastItemView.getY())));
//                Log.d(TAG, "마지막 뷰의 height: "+String.valueOf(lastItemView.getHeight()));
//                Log.d(TAG, "첫번째 뷰의 percentage: "+String.valueOf(((firstItemView.getHeight() - Math.abs(firstItemView.getY()))/firstItemView.getHeight())*100)+"%");
//                Log.d(TAG, "마지막 뷰의 percentage: "+String.valueOf(((lastItemView.getHeight() - Math.abs(lastItemView.getY()))/lastItemView.getHeight())*100)+"%");


                //화면에 보이는 마지막 아이템의 index가 마지막 아이템의 인덱스와 같고, 현재 화면에 보이는 아이템의 개수가 1인 경우
                //다음 게시물 10개를 가져오는 메소드를 실행한다.
                //현재 화면에 보이는 아이템의 개수가 1인 경우를 조건에 추가하지 않으면 스크롤을 할때마다 화면에 보이는 마지막 아이템의 인덱스와
                //마지막 아이템의 인덱스가 계속 같기 때문에 해당 조건문을 계속 타버리는 문제가 생긴다.
                if ((lastVisibleItemPosition == totalCount - 1) && visibleItemCount == 1 && loadPossible == true && postItemArrayList.size() >= 1) {
                    Log.d("페이징 조건", "부합");
                    //다음 페이지를 로드한다.
                    //param:현재 로드되어있는 데이터의 수(다음 페이지에 로드되어야 할 첫번째 게시물의 index)
                    loadNextPage();
                }


            }
        });

    }

    public void setRecyclerView() {
        //아이템을 담을 어레이리스트 생성
        postItemArrayList = new ArrayList<>();

        rv_post = findViewById(R.id.recyclerview_post);
        rv_post.setHasFixedSize(true);

        //레이아웃 메니저 설정
        layoutManager = new LinearLayoutManager(this);
        rv_post.setLayoutManager(layoutManager);

        //어댑터 설정
        postAdapter = new PostAdapter(this, postItemArrayList, getSupportFragmentManager());
        //인터페이스 리스너 설정
        postAdapter.setOnClickListener(PostActivity.this);
        rv_post.setAdapter(postAdapter);

    }

    //비동기 네트워크 통신을 통해서 서버로부터 미확인 메세지(채팅)의 수를 json형태로 받아오는 메소드
    private void getMessageCount() {
        //전체 채팅 메세지 수를 가져오는 스레드 실행
        JSONObject messageCountRequestBody = new JSONObject();
        try {
            messageCountRequestBody.put("account", LoginActivity.account);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //서버에 요청
        HttpRequest messageCountHttpRequest = new HttpRequest("GET", messageCountRequestBody.toString(), "getmessagecount.php", this);
        messageCountHttpRequest.execute();
    }

    //비동기 네트워크 통신을 통해서 서버로부터 게시물의 데이터를 json형태로 받아오는 메소드
    private void getPost() {

        //서버에서 게시물 데이터를 받아오기 위한 requestbody설정
        JSONObject postRequestBody = new JSONObject();
        try {
            postRequestBody.put("requestType", "getPost");
            postRequestBody.put("lastPostNum", 0);
            if (postItemArrayList.isEmpty()) {//최초로 onResume을 타서 게시물 데이터를 가져오는 경우
                postRequestBody.put("postCount", 10);//로드할 게시물 수
            } else {//이미 게시물 데이터가 있는 상태에서 다시 onResume을 타는 경우
                postRequestBody.put("postCount", postItemArrayList.size());//로드할 게시물 수
            }
            postRequestBody.put("account", LoginActivity.account);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //서버에 요청
        HttpRequest postHttpRequest = new HttpRequest("GET", postRequestBody.toString(), "getpost.php", this);
        postHttpRequest.execute();
    }

    //게시물 리스트의 최하단으로 이동하면 페이징 처리를 위해서 실행될 메소드
    public void loadNextPage() {
        loadPossible = false;
        //null을 넣어서 뷰타입이 프로그래스 아이템이 추가되게 한다.
        postItemArrayList.add(null);
        postAdapter.notifyItemInserted(postItemArrayList.size() - 1);
        //핸들러를 통해서 2초 뒤에 해당 기능을 실행한다.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("requestType", "loadNextPage");
                    requestBody.put("lastPostNum", postItemArrayList.get(postItemArrayList.size() - 2).postNum);//가장 마지막 게시물 id
                    requestBody.put("postCount", 10);//로드할 게시물 수
                    requestBody.put("account", LoginActivity.account);
                    HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getpost.php", PostActivity.this);
                    httpRequest.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, 1500);

    }

    //비동기 네트워크 통신을 통해서 게시물을 삭제하는 메소드
    private void deletePost(int postNum, int position, String type) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("postNum", postNum);
            requestBody.put("type", type);
            requestBody.put("position", position);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "deletepost.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void processLike(boolean isLike, int postNum, int position) {
        try {
            JSONObject requestBody = new JSONObject();
            if (isLike) {
                requestBody.put("likeState", true);
            } else {
                requestBody.put("likeState", false);
            }
            requestBody.put("account", account);
            requestBody.put("postNum", postNum);
            requestBody.put("position", position);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "processlike.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //좋아요를 누른 경우 fcm을 통해서 좋아요를 당한 사용자에게 알림을 전달하기 위한 메소드
    private void pushNotification(int position) {
        try {
            Log.d(TAG, "pushNotification 메소드 호출");
            JSONObject requestBody = new JSONObject();
            requestBody.put("account", postItemArrayList.get(position).account);//알림의 대상이 되는 사람
            requestBody.put("title", "SNS");//알림의 제목
            requestBody.put("body", nickname + "님이 회원님의 게시물에 좋아요를 눌렀습니다.");//알림의 내용
            requestBody.put("click_action", "PostDetailFragment");//푸시알림을 눌렀을 때 이동할 액티비티 혹은 프래그먼트
            requestBody.put("category", "like");//알림의 카테고리
            requestBody.put("userAccount", account);//좋아요를 누른 사람
            requestBody.put("postNum", postItemArrayList.get(position).postNum);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "pushnotification.php", this);
            httpRequest.execute();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSON ERROR:" + e.getMessage());
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("게시물 액티비티 onStart", "호출");
        //게시물이 업로드되어 onStart를 타는 경우
        if (isUploaded) {
            Log.d("게시물 새로고침", "호출");
            rv_post.smoothScrollToPosition(0);//게시물이 업로드되었기 때문에 리사이클러뷰의 최상단으로 이동시킨다.
            isUploaded = false;
        }

        //게시물이 수정되어 onStart를 타는 경우
        if (isEditted) {
            isEditted = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume호출");
        getPost();//게시물 데이터를 서버에서 가져오는 메소드
        getMessageCount();//미확인 메세지 데이터를 서버에서 가져오는 메소드

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause호출");

        //비디오 정지

        postAdapter.releasevideo();


        super.onPause();
    }

    //다음 나오는 메소드들은 포스트 리사이클러뷰의 인터페이스의 메소드

    //프로필 사진 클릭 콜백 메소드
    @Override
    public void onProfileClicked(int position) {

        //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
        Bundle bundle = new Bundle();
        bundle.putString("account", postItemArrayList.get(position).getAccount());
        bundle.putString("parentActivity", "PostActivity");
        bundle.putBoolean("isMyPost", postItemArrayList.get(position).getIsMyPost());
        accountPageFragment.setArguments(bundle);
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                .replace(R.id.frame_parent_container, accountPageFragment)
                .addToBackStack(null)
                .commit();

    }

    //닉네임 클릭 콜백 메소드
    @Override
    public void onNicknameClicked(int position) {

        //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
        Bundle bundle = new Bundle();
        bundle.putString("account", postItemArrayList.get(position).getAccount());
        bundle.putString("parentActivity", "PostActivity");
        bundle.putBoolean("isMyPost", postItemArrayList.get(position).getIsMyPost());
        accountPageFragment.setArguments(bundle);
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                .replace(R.id.frame_parent_container, accountPageFragment)
                .addToBackStack(null)
                .commit();
    }

    //더 보기 버튼 클릭 콜백 메소드
    @Override
    public void onMoreClicked(int position) {
        Dialog dialog = new Dialog(PostActivity.this);
        //더 보기를 누른 게시물이 나의 게시물인 경우
        if (postItemArrayList.get(position).getIsMyPost()) {
            dialog.setContentView(R.layout.mypost_more_box);

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);

            TextView tv_profile, tv_editpost, tv_deletepost;
            //프로필 보기
            tv_profile = dialog.findViewById(R.id.textview_profile);
            //게시물 수정
            tv_editpost = dialog.findViewById(R.id.textview_editpost);
            //게시물 삭제
            tv_deletepost = dialog.findViewById(R.id.textview_deletepost);

            tv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();

                    //사용자 계정 프래그먼트 객체 선언
                    AccountPageFragment accountPageFragment = new AccountPageFragment();

                    //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                    Bundle bundle = new Bundle();
                    bundle.putString("account", postItemArrayList.get(position).getAccount());
                    bundle.putBoolean("isMyPost", postItemArrayList.get(position).getIsMyPost());
                    accountPageFragment.setArguments(bundle);
                    //프래그먼트를 framelayout에 붙여준다.
                    //이때 addToBackStack메소드는 트랜잭션 스택에 해당 프래그먼트를 추가해주는 것이다. 이 스택은 액티비티가 관리하기 때문에
                    //뒤로가기 버튼을 누르면 화면에 보이는 프래그먼트를 차례로 지워줄 수 있다.
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                            .replace(R.id.frame_parent_container, accountPageFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });

            tv_editpost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = null;
                    //수정하려는 게시물이 이미지 게시물인 경우
                    if (postItemArrayList.get(position).type.equals("image")) {
                        intent = new Intent(getApplicationContext(), EditPostFirstActivity.class);
                        //이미지의 개수를 인텐트로 넘겨준다.
                        intent.putExtra("imageCount", postItemArrayList.get(position).imageList.size());
                        //게시물의 이미지 파일명을 이미지의 개수만큼 인텐트로 넘겨준다.
                        for (int i = 0; i < postItemArrayList.get(position).imageList.size(); i++) {
                            intent.putExtra("image" + (i + 1), "http://13.124.105.47/uploadimage/" + postItemArrayList.get(position).imageList.get(i));
                        }
                    }
                    //수정하려는 게시물이 동영상 게시물인 경우
                    else {
                        intent = new Intent(getApplicationContext(), EditVideoPostFirstActivity.class);
                        intent.putExtra("uri", "http://13.124.105.47/uploadvideo/" + postItemArrayList.get(position).video);//게시물의 동영상 파일명
                    }
                    //게시물 번호를 넘겨준다.
                    intent.putExtra("postNum", postItemArrayList.get(position).getPostNum());

                    //게시글이 존재하면 인텐트로 넘겨준다.
                    if (!TextUtils.isEmpty(postItemArrayList.get(position).getArticle())) {
                        intent.putExtra("article", postItemArrayList.get(position).getArticle());
                        Log.d("게시글 존재:", "yes");
                    }

                    //주소가 존재하면 인텐트로 넘겨준다.
                    if (!TextUtils.isEmpty(postItemArrayList.get(position).getAddress())) {
                        Log.d("주소 존재:", "yes");
                        intent.putExtra("address", postItemArrayList.get(position).getAddress());
                        intent.putExtra("latitude", postItemArrayList.get(position).getLatitude());
                        intent.putExtra("longitude", postItemArrayList.get(position).getLongitude());
                    }


                    startActivity(intent);
                    dialog.dismiss();
                }
            });

            tv_deletepost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    //삭제를 다시 확인하는 다이얼로그
                    Dialog deleteDialog = new Dialog(PostActivity.this);
                    deleteDialog.setContentView(R.layout.post_delete_check_box);
                    deleteDialog.show();

                    TextView tv_yes, tv_no;

                    //예 버튼
                    tv_yes = deleteDialog.findViewById(R.id.textview_yes);
                    //아니오 버튼
                    tv_no = deleteDialog.findViewById(R.id.textview_no);

                    //예 버튼 클릭 리스너
                    tv_yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //다이얼로그를 지우고
                            deleteDialog.dismiss();
                            deletePost(postItemArrayList.get(position).postNum, position, postItemArrayList.get(position).type);//게시물 삭제 메소드
                        }
                    });

                    //아니오 버튼 클릭 리스너
                    tv_no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //다이얼로그를 지워준다.
                            deleteDialog.dismiss();
                        }
                    });

                }
            });

        }
        //더 보기를 누른 게시물이 타인의 게시물인 경우
        else {
            dialog.setContentView(R.layout.otherspost_more_box);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
            TextView tv_profile;
            //프로필 보기
            tv_profile = dialog.findViewById(R.id.textview_profile);

            tv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();

                    //사용자 계정 프래그먼트 객체 선언
                    AccountPageFragment accountPageFragment = new AccountPageFragment();

                    //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                    Bundle bundle = new Bundle();
                    bundle.putString("account", postItemArrayList.get(position).getAccount());
                    bundle.putBoolean("isMyPost", postItemArrayList.get(position).getIsMyPost());
                    accountPageFragment.setArguments(bundle);
                    //프래그먼트를 framelayout에 붙여준다.
                    //이때 addToBackStack메소드는 트랜잭션 스택에 해당 프래그먼트를 추가해주는 것이다. 이 스택은 액티비티가 관리하기 때문에
                    //뒤로가기 버튼을 누르면 화면에 보이는 프래그먼트를 차례로 지워줄 수 있다.
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                            .replace(R.id.frame_parent_container, accountPageFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });

        }
        dialog.show();

    }


    //서버와 통신에 성공했을 때 호출되는 콜백 메소드(HttpRequest클래스(AsyncTask클래스 상속)의 onPostExecute()메소드에서 호출)
    //모든 통신에 대한 결과는 이 콜백 메소드에서 처리해준다.
    @Override
    public void onHttpResponse(String result) {
        Log.d(TAG, "통신 성공");
        Log.d(TAG, "서버에서 넘어온 json데이터- " + result);

        try {
            JSONObject responseBody = new JSONObject(result);
            String requestType = responseBody.getString("requestType");//서버통신 요청 타입

            if (requestType.equals("messageCount")) {//메세지 개수를 받아오는 통신을 한 경우
                int messageCount = responseBody.getInt("messageCount");
                //미확인 메세지가 존재하는 경우
                if (messageCount != 0) {
                    tv_messageCount.setVisibility(View.VISIBLE);
                    tv_messageCount.setText(String.valueOf(messageCount));
                    unCheckedMessageCount = messageCount;
                }
                //미확인 메세지가 존재하지 않는 경우
                else {
                    tv_messageCount.setVisibility(View.GONE);//메세지 수가 안 보이게 처리
                    unCheckedMessageCount = 0;//미확인 메세지 수 0으로 초기화

                }
            } else if (requestType.equals("getPost")) {//게시물 데이터를 받아오는 통신을 한 경우
                postItemArrayList.clear();

                //json오브잭트를 선언하고
                JSONObject jsonObject = new JSONObject(result);
                //jsonarray를 선언해서
                JSONArray jsonArray = jsonObject.getJSONArray("post");
                Log.d("게시물 수", String.valueOf(jsonArray.length()));

                //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                for (int i = 0; i < jsonArray.length(); i++) {
                    //서버로부터 넘어온 데이터를 변수에 정의
                    JSONObject data = jsonArray.getJSONObject(i);
                    PostItem postItem = new Gson().fromJson(data.toString(), PostItem.class);
                    if (!postAdapter.isMuted) {
                        postItem.setMuted(false);
                    }
                    //모든 데이터를 다 담았으면 이제 그 객체를 리사이클러뷰 어레이리스트에 추가해준다.
                    postItemArrayList.add(postItem);
                    //arraylist가 모두 추가됐으면 어댑터 notify
                    postAdapter.notifyDataSetChanged();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int firstVisibleItemPosition = ((LinearLayoutManager) rv_post.getLayoutManager()).findFirstVisibleItemPosition();
                        int lastVisibleItemPosition = ((LinearLayoutManager) rv_post.getLayoutManager()).findLastVisibleItemPosition();
                        //가장 첫번째로 보이는 뷰
                        View firstItemView = rv_post.getLayoutManager().findViewByPosition(firstVisibleItemPosition);
                        //가장 마지막으로 보이는 뷰
                        View lastItemView = rv_post.getLayoutManager().findViewByPosition(lastVisibleItemPosition);
                        //첫번째 아이템이 화면에 보이는 비중
                        float firstItemPercentage = ((firstItemView.getHeight() - Math.abs(firstItemView.getY())) / firstItemView.getHeight()) * 100;
                        //두번째 아이템이 화면에 보이는 비중
                        float lastItemPercentage = ((lastItemView.getHeight() - Math.abs(lastItemView.getY())) / lastItemView.getHeight()) * 100;
                        if (postItemArrayList.get(lastVisibleItemPosition) != null) {
                            //첫번째로 보이는 아이템이 동영상 게시물이고 재생중이지 않은 경우
                            if (postItemArrayList.get(firstVisibleItemPosition).type.equals("video") && !postItemArrayList.get(firstVisibleItemPosition).isPlaying) {
                                //첫번재로 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                                if (firstItemPercentage >= 50 && lastItemPercentage < 50) {
                                    postAdapter.notifyItemChanged(firstVisibleItemPosition, "playVideo");//처음으로 보이는 아이템 play
                                    postItemArrayList.get(firstVisibleItemPosition).setPlaying(true);
                                }
                            }

                            //마지막으로 보이는 아이템이 동영상 게시물이고 재생중이지 않은 경우
                            if (postItemArrayList.get(lastVisibleItemPosition).type.equals("video") && !postItemArrayList.get(lastVisibleItemPosition).isPlaying) {
                                //첫번재로 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                                if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                                    postAdapter.notifyItemChanged(lastVisibleItemPosition, "playVideo");//처음으로 보이는 아이템 play
                                    postItemArrayList.get(lastVisibleItemPosition).setPlaying(true);
                                }
                            }

                            //첫번째로 보이는 아이템이 동영상 게시물이고 현재 재생중인 경우
                            if (postItemArrayList.get(firstVisibleItemPosition).type.equals("video") && postItemArrayList.get(firstVisibleItemPosition).isPlaying) {
                                //마지막에 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                                if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                                    //마지막으로 보이는 아이템이 동영상 게시물인 경우
                                    if (postItemArrayList.get(lastVisibleItemPosition).type.equals("video")) {
                                        postAdapter.notifyItemChanged(lastVisibleItemPosition, "playVideo");//마지막으로 보이는 아이템을 play
                                        postItemArrayList.get(firstVisibleItemPosition).setPlaying(false);//첫번째 아이템의 플레이상태를 false
                                        postItemArrayList.get(lastVisibleItemPosition).setPlaying(true);//마지막 아이템의 플레이상태를 true
                                    }
                                    //마지막으로 보이는 아이템이 이미지 게시물인 경우
                                    else {
                                        //재생중인 동영상을 release 해준다.
                                        postItemArrayList.get(firstVisibleItemPosition).setPlaying(false);
                                        postAdapter.notifyItemChanged(firstVisibleItemPosition, "releaseVideo");

                                    }
                                }
                            }

                            //마지막으로 보이는 아이템이 동영상 게시물이고 현재 재생중인 경우
                            if (postItemArrayList.get(lastVisibleItemPosition).type.equals("video") && postItemArrayList.get(lastVisibleItemPosition).isPlaying) {
                                //첫번째로 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                                if (lastItemPercentage < 50 && firstItemPercentage >= 50) {
                                    //첫번째로 보이는 아이템이 동영상 게시물인 경우
                                    if (postItemArrayList.get(firstVisibleItemPosition).type.equals("video")) {
                                        postAdapter.notifyItemChanged(firstVisibleItemPosition, "playVideo");//첫번째로 보이는 아이템을 play
                                        postItemArrayList.get(lastVisibleItemPosition).setPlaying(false);//마지막 아이템의 플레이상태를 false
                                        postItemArrayList.get(firstVisibleItemPosition).setPlaying(true);//첫번째 아이템의 플레이상태를 true
                                    }
                                    //첫번째로 보이는 아이템이 이미지 게시물인 경우
                                    else {
                                        //재생중인 동영상을 release해준다.
                                        postItemArrayList.get(lastVisibleItemPosition).setPlaying(false);
                                        postAdapter.notifyItemChanged(lastVisibleItemPosition, "releaseVideo");

                                    }
                                }
                            }

                            //첫번째로 보이는 아이템이 이미지 게시물이고 마지막으로 보이는 아이템이 동영상이고 그 동영상이 재생중인 경우
                            if (postItemArrayList.get(firstVisibleItemPosition).type.equals("image") && postItemArrayList.get(lastVisibleItemPosition).type.equals("video") && postItemArrayList.get(lastVisibleItemPosition).isPlaying) {
                                //동영상 게시물이 화면에 보이는 비중이 더 낮아지는 경우
                                if (firstItemPercentage >= 50 && lastItemPercentage < 50) {
                                    postAdapter.notifyItemChanged(lastVisibleItemPosition, "releaseVideo");//동영상 release
                                    postItemArrayList.get(lastVisibleItemPosition).setPlaying(false);//플레이 상태 false

                                }
                            }

                            //첫번째로 보이는 아이템이 이미지 게시물이고 마지막으로 보이는 아이템이 동영상이고 그 동영상이 재생중이지 않은 경우
                            if (postItemArrayList.get(firstVisibleItemPosition).type.equals("image") && postItemArrayList.get(lastVisibleItemPosition).type.equals("video") && !postItemArrayList.get(lastVisibleItemPosition).isPlaying) {
                                //동영상 게시물이 화면에 보이는 비중이 더 높아지는 경우
                                if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                                    postAdapter.notifyItemChanged(lastVisibleItemPosition, "playVideo");//동영상 play
                                    postItemArrayList.get(lastVisibleItemPosition).setPlaying(true);//플레이 상태 true

                                }
                            }

                            //마지막으로 보이는 아이템이 이미지 게시물이고 첫번째로 보이는 아이템이 동영상이고 그 동영상이 재생중인 경우
                            if (postItemArrayList.get(lastVisibleItemPosition).type.equals("image") && postItemArrayList.get(firstVisibleItemPosition).type.equals("video") && postItemArrayList.get(firstVisibleItemPosition).isPlaying) {
                                //동영상 게시물이 화면에 보이는 비중이 더 낮아지는 경우
                                if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                                    postAdapter.notifyItemChanged(firstVisibleItemPosition, "releaseVideo");//동영상 release
                                    postItemArrayList.get(firstVisibleItemPosition).setPlaying(false);//플레이 상태 false

                                }
                            }

                            //마지막으로 보이는 아이템이 이미지 게시물이고 첫번째로 보이는 아이템이 동영상이고 그 동영상이 재생중이지 않은 경우
                            if (postItemArrayList.get(lastVisibleItemPosition).type.equals("image") && postItemArrayList.get(firstVisibleItemPosition).type.equals("video") && !postItemArrayList.get(firstVisibleItemPosition).isPlaying) {
                                //동영상 게시물이 화면에 보이는 비중이 더 높아지는 경우
                                if (firstItemPercentage >= 50 && lastItemPercentage < 50) {
                                    postAdapter.notifyItemChanged(firstVisibleItemPosition, "playVideo");//동영상 play
                                    postItemArrayList.get(firstVisibleItemPosition).setPlaying(true);//플레이 상태 true

                                }
                            }
                        }


                    }
                }, 500);

            } else if (requestType.equals("loadNextPage")) {
                try {
                    //json오브잭트를 선언하고
                    JSONObject jsonObject = new JSONObject(result);
                    //jsonarray를 선언해서
                    JSONArray jsonArray = jsonObject.getJSONArray("post");
                    Log.d("게시물 수", String.valueOf(jsonArray.length()));

                    boolean isSubstituted = true;
                    if (jsonArray.length() != 0) {
                        //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                        for (int i = 0; i < jsonArray.length(); i++) {
                            //서버로부터 넘어온 데이터를 변수에 정의
                            JSONObject data = jsonArray.getJSONObject(i);
                            PostItem postItem = new Gson().fromJson(data.toString(), PostItem.class);

                            //로딩바를 다음 페이지의 첫번째 게시물로 교체하기 위한 작업
                            if (isSubstituted) {
                                if (!postAdapter.isMuted) {
                                    postItem.setMuted(false);
                                }
                                //프로그래스바 아이템을 게시물 아이템으로 교체해준다.
                                postItemArrayList.set(postItemArrayList.size() - 1, postItem);
                                //어댑터 notify
                                postAdapter.notifyItemChanged(postItemArrayList.size() - 1);
                                Log.d("게시물 사이즈:", String.valueOf(postItemArrayList.size()));

                                isSubstituted = false;
                            }
                            //최초에 로딩바와 게시물이 교체가 끝났으면 그 뒤로는 게시물 추가만 해준다.
                            else {
                                if (!postAdapter.isMuted) {
                                    postItem.setMuted(false);
                                }
                                postItemArrayList.add(postItem);
                                postAdapter.notifyItemInserted(postItemArrayList.size() - 1);
                            }

                        }
                    } else {//게시물이 존재하지 않아서 서버에서 게시물 데이터가 넘어오지 않은 경우
                        postItemArrayList.remove(postItemArrayList.size() - 1);
                        postAdapter.notifyItemRemoved(postItemArrayList.size());
                        Toast.makeText(getApplicationContext(), "더 이상 게시물이 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                    loadPossible = true;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int firstVisibleItemPosition = ((LinearLayoutManager) rv_post.getLayoutManager()).findFirstVisibleItemPosition();
                            int lastVisibleItemPosition = ((LinearLayoutManager) rv_post.getLayoutManager()).findLastVisibleItemPosition();
                            //가장 첫번째로 보이는 뷰
                            View firstItemView = rv_post.getLayoutManager().findViewByPosition(firstVisibleItemPosition);
                            //가장 마지막으로 보이는 뷰
                            View lastItemView = rv_post.getLayoutManager().findViewByPosition(lastVisibleItemPosition);
                            //첫번째 아이템이 화면에 보이는 비중
                            float firstItemPercentage = ((firstItemView.getHeight() - Math.abs(firstItemView.getY())) / firstItemView.getHeight()) * 100;
                            //두번째 아이템이 화면에 보이는 비중
                            float lastItemPercentage = ((lastItemView.getHeight() - Math.abs(lastItemView.getY())) / lastItemView.getHeight()) * 100;
                            if (postItemArrayList.get(lastVisibleItemPosition) != null) {
                                //첫번째로 보이는 아이템이 동영상 게시물이고 재생중이지 않은 경우
                                if (postItemArrayList.get(firstVisibleItemPosition).type.equals("video") && !postItemArrayList.get(firstVisibleItemPosition).isPlaying) {
                                    //첫번재로 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                                    if (firstItemPercentage >= 50 && lastItemPercentage < 50) {
                                        postAdapter.notifyItemChanged(firstVisibleItemPosition, "playVideo");//처음으로 보이는 아이템 play
                                        postItemArrayList.get(firstVisibleItemPosition).setPlaying(true);
                                    }
                                }

                                //마지막으로 보이는 아이템이 동영상 게시물이고 재생중이지 않은 경우
                                if (postItemArrayList.get(lastVisibleItemPosition).type.equals("video") && !postItemArrayList.get(lastVisibleItemPosition).isPlaying) {
                                    //첫번재로 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                                    if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                                        postAdapter.notifyItemChanged(lastVisibleItemPosition, "playVideo");//처음으로 보이는 아이템 play
                                        postItemArrayList.get(lastVisibleItemPosition).setPlaying(true);
                                    }
                                }

                                //첫번째로 보이는 아이템이 동영상 게시물이고 현재 재생중인 경우
                                if (postItemArrayList.get(firstVisibleItemPosition).type.equals("video") && postItemArrayList.get(firstVisibleItemPosition).isPlaying) {
                                    //마지막에 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                                    if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                                        //마지막으로 보이는 아이템이 동영상 게시물인 경우
                                        if (postItemArrayList.get(lastVisibleItemPosition).type.equals("video")) {
                                            postAdapter.notifyItemChanged(lastVisibleItemPosition, "playVideo");//마지막으로 보이는 아이템을 play
                                            postItemArrayList.get(firstVisibleItemPosition).setPlaying(false);//첫번째 아이템의 플레이상태를 false
                                            postItemArrayList.get(lastVisibleItemPosition).setPlaying(true);//마지막 아이템의 플레이상태를 true
                                        }
                                        //마지막으로 보이는 아이템이 이미지 게시물인 경우
                                        else {
                                            //재생중인 동영상을 release 해준다.
                                            postItemArrayList.get(firstVisibleItemPosition).setPlaying(false);
                                            postAdapter.notifyItemChanged(firstVisibleItemPosition, "releaseVideo");
                                        }
                                    }
                                }

                                //마지막으로 보이는 아이템이 동영상 게시물이고 현재 재생중인 경우
                                if (postItemArrayList.get(lastVisibleItemPosition).type.equals("video") && postItemArrayList.get(lastVisibleItemPosition).isPlaying) {
                                    //첫번째로 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                                    if (lastItemPercentage < 50 && firstItemPercentage >= 50) {
                                        //첫번째로 보이는 아이템이 동영상 게시물인 경우
                                        if (postItemArrayList.get(firstVisibleItemPosition).type.equals("video")) {
                                            postAdapter.notifyItemChanged(firstVisibleItemPosition, "playVideo");//첫번째로 보이는 아이템을 play
                                            postItemArrayList.get(lastVisibleItemPosition).setPlaying(false);//마지막 아이템의 플레이상태를 false
                                            postItemArrayList.get(firstVisibleItemPosition).setPlaying(true);//첫번째 아이템의 플레이상태를 true
                                        }
                                        //첫번째로 보이는 아이템이 이미지 게시물인 경우
                                        else {
                                            //재생중인 동영상을 release해준다.
                                            postItemArrayList.get(lastVisibleItemPosition).setPlaying(false);
                                            postAdapter.notifyItemChanged(lastVisibleItemPosition, "releaseVideo");

                                        }
                                    }
                                }

                                //첫번째로 보이는 아이템이 이미지 게시물이고 마지막으로 보이는 아이템이 동영상이고 그 동영상이 재생중인 경우
                                if (postItemArrayList.get(firstVisibleItemPosition).type.equals("image") && postItemArrayList.get(lastVisibleItemPosition).type.equals("video") && postItemArrayList.get(lastVisibleItemPosition).isPlaying) {
                                    //동영상 게시물이 화면에 보이는 비중이 더 낮아지는 경우
                                    if (firstItemPercentage >= 50 && lastItemPercentage < 50) {
                                        postAdapter.notifyItemChanged(lastVisibleItemPosition, "releaseVideo");//동영상 release
                                        postItemArrayList.get(lastVisibleItemPosition).setPlaying(false);//플레이 상태 false
                                    }
                                }

                                //첫번째로 보이는 아이템이 이미지 게시물이고 마지막으로 보이는 아이템이 동영상이고 그 동영상이 재생중이지 않은 경우
                                if (postItemArrayList.get(firstVisibleItemPosition).type.equals("image") && postItemArrayList.get(lastVisibleItemPosition).type.equals("video") && !postItemArrayList.get(lastVisibleItemPosition).isPlaying) {
                                    //동영상 게시물이 화면에 보이는 비중이 더 높아지는 경우
                                    if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                                        postAdapter.notifyItemChanged(lastVisibleItemPosition, "playVideo");//동영상 play
                                        postItemArrayList.get(lastVisibleItemPosition).setPlaying(true);//플레이 상태 true

                                    }
                                }

                                //마지막으로 보이는 아이템이 이미지 게시물이고 첫번째로 보이는 아이템이 동영상이고 그 동영상이 재생중인 경우
                                if (postItemArrayList.get(lastVisibleItemPosition).type.equals("image") && postItemArrayList.get(firstVisibleItemPosition).type.equals("video") && postItemArrayList.get(firstVisibleItemPosition).isPlaying) {
                                    //동영상 게시물이 화면에 보이는 비중이 더 낮아지는 경우
                                    if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                                        postAdapter.notifyItemChanged(firstVisibleItemPosition, "releaseVideo");//동영상 release
                                        postItemArrayList.get(firstVisibleItemPosition).setPlaying(false);//플레이 상태 false

                                    }
                                }

                                //마지막으로 보이는 아이템이 이미지 게시물이고 첫번째로 보이는 아이템이 동영상이고 그 동영상이 재생중이지 않은 경우
                                if (postItemArrayList.get(lastVisibleItemPosition).type.equals("image") && postItemArrayList.get(firstVisibleItemPosition).type.equals("video") && !postItemArrayList.get(firstVisibleItemPosition).isPlaying) {
                                    //동영상 게시물이 화면에 보이는 비중이 더 높아지는 경우
                                    if (firstItemPercentage >= 50 && lastItemPercentage < 50) {
                                        postAdapter.notifyItemChanged(firstVisibleItemPosition, "playVideo");//동영상 play
                                        postItemArrayList.get(firstVisibleItemPosition).setPlaying(true);//플레이 상태 true

                                    }
                                }
                            }

                        }
                    }, 500);

                } catch (Exception e) {
                    Log.d("데이터 셋 오류", e.getMessage());
                    postItemArrayList.remove(postItemArrayList.size() - 1);
                    postAdapter.notifyItemRemoved(postItemArrayList.size());
                    Toast.makeText(getApplicationContext(), "더 이상 게시물이 없습니다.", Toast.LENGTH_SHORT).show();
                    loadPossible = true;
                }
            } else if (requestType.equals("deletePost")) {//게시물을 삭제하는 통신을 한 경우
                int position = responseBody.getInt("position");//삭제한 게시물의 index

                //아이템 리스트에서 해당 게시물 데이터 객체를 삭제하고
                postItemArrayList.remove(position);
                postAdapter.notifyItemRemoved(position);

                //핸들러로 200밀리세컨 뒤에 전체 데이터를 리셋해주는 notify메소드를 실행한다.
                //이렇게 서브 스레드에서 잠깐 차이를 두고 실행시키는 이유는 그냥 notifyitemremoved를 해주면 이상하게 그 다음 게시물의
                //사진을 글라이드가 가져와서 보여주지 못하는 문제가 발생하기 때문임.
                //근데 notifydatasetchanged를 해주면 사진이 바로 로드됨.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        postAdapter.notifyDataSetChanged();
                    }
                }, 700);

            } else if (requestType.equals("likeOk")) {//좋아요를 하는 처리를 하는 통신
                int position = responseBody.getInt("position");
                //좋아요 상태를 true로 만들고
                postItemArrayList.get(position).setIsLike(true);
                //좋아요 개수를 +1
                postItemArrayList.get(position).setLikeCount(postItemArrayList.get(position).getLikeCount() + 1);
                //어댑터에 notify
                postAdapter.notifyItemChanged(position, "true");

                //내가 내 게시물에 좋아요를 누른 경우에는 따로 알림을 보내지 않는다.
                if (!account.equals(postItemArrayList.get(position).account)) {
                    //좋아요를 당한 사용자 단말에 push알림을 보내준다.
                    pushNotification(position);
                }
            } else if (requestType.equals("likeCancel")) {//좋아요를 취소하는 처리를 하는 통신
                int position = responseBody.getInt("position");
                //좋아요 상태를 false로 만들고
                postItemArrayList.get(position).setIsLike(false);
                //좋아요 개수를 -1
                postItemArrayList.get(position).setLikeCount(postItemArrayList.get(position).getLikeCount() - 1);
                //어댑터에 notify
                postAdapter.notifyItemChanged(position, "false");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//end of onHttpResponse method


    //좋아요 버튼 클릭 콜백 메소드
    @Override
    public void onLikeClicked(int position) {
        Log.d("좋아요 버튼", "클릭");

        int postNum = postItemArrayList.get(position).getPostNum();
        //좋아요를 누르지 않은 상태인 경우(좋아요를 하려는 경우)
        if (postItemArrayList.get(position).isLike == false) {
            //좋아요 상태를 true로 전환
            processLike(true, postNum, position);

        }
        //좋아요를 누른 상태인 경우(좋아요를 취소하려는 경우)
        else {
            processLike(false, postNum, position);
        }

    }


    //좋아요 개수 클릭 콜백 메소드
    @Override
    public void onLikeCountClicked(int position) {

        Bundle bundle = new Bundle();
        bundle.putInt("postNum", postItemArrayList.get(position).getPostNum());
        bundle.putString("parentActivity", "PostActivity");
        likeListFragment.setArguments(bundle);
        //좋아요 리스트 프래그먼트로 바꿔준다.
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                .replace(R.id.frame_parent_container, likeListFragment)
                .addToBackStack(null)
                .commit();
    }

    //댓글 클릭 콜백 메소드
    @Override
    public void onCommentClick(int position) {

        Intent intent = new Intent(getApplicationContext(), CommentActivity.class);
        //해당 게시물과 게시물 작성자 데이터를 댓글 화면에서 보여주기 위해서 인텐트로 데이터들을 넘겨준다.
        intent.putExtra("postNum", postItemArrayList.get(position).getPostNum());
        intent.putExtra("postProfile", postItemArrayList.get(position).getProfile());
        intent.putExtra("postAccount", postItemArrayList.get(position).getAccount());
        intent.putExtra("postNickname", postItemArrayList.get(position).getNickname());
        intent.putExtra("postArticle", postItemArrayList.get(position).getArticle());
        intent.putExtra("postTime", postItemArrayList.get(position).getTime());
        startActivity(intent);
    }

    //댓글 개수 클릭 콜백 메소드
    @Override
    public void onCommentCountClicked(int position) {

        Intent intent = new Intent(getApplicationContext(), CommentActivity.class);
        //해당 게시물과 게시물 작성자 데이터를 댓글 화면에서 보여주기 위해서 인텐트로 데이터들을 넘겨준다.
        intent.putExtra("postNum", postItemArrayList.get(position).getPostNum());
        intent.putExtra("postProfile", postItemArrayList.get(position).getProfile());
        intent.putExtra("postAccount", postItemArrayList.get(position).getAccount());
        intent.putExtra("postNickname", postItemArrayList.get(position).getNickname());
        intent.putExtra("postArticle", postItemArrayList.get(position).getArticle());
        intent.putExtra("postTime", postItemArrayList.get(position).getTime());
        startActivity(intent);
    }

    //해시태그 클릭 콜백 메소드
    @Override
    public void onHashTagClicked(int position, String hashTag) {

        HashTagPostListFragment hashTagPostListFragment = new HashTagPostListFragment();
        Log.d("해시태그", hashTag);
        Bundle bundle = new Bundle();
        bundle.putString("hashTag", hashTag);
        bundle.putString("parentActivity", "PostActivity");
        hashTagPostListFragment.setArguments(bundle);
        //좋아요 리스트 프래그먼트로 바꿔준다.
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                .replace(R.id.frame_parent_container, hashTagPostListFragment)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void onPlaceClicked(int position) {
        SearchedPlaceListFragment searchedPlaceListFragment = new SearchedPlaceListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("address", postItemArrayList.get(position).address);
        bundle.putDouble("latitude", Double.parseDouble(postItemArrayList.get(position).latitude));
        bundle.putDouble("longitude", Double.parseDouble(postItemArrayList.get(position).longitude));
        bundle.putString("parentActivity", "PostActivity");
        searchedPlaceListFragment.setArguments(bundle);
        //해시태그 리스트 프래그먼트로 바꿔준다.
//        PostActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, searchedPlaceListFragment).addToBackStack(null).commit();
        PostActivity.fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                .replace(R.id.frame_parent_container, searchedPlaceListFragment)
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void onBackPressed() {

        //프래그먼트 스택에 프래그먼트가 존재하면
        if (fragmentManager.getBackStackEntryCount() != 0) {//스택에 프래그먼트가 존재하는 경우
            Log.d("프래그먼트 스택 존재", "호출");
            //스택의 최상단에 있는 프래그먼트를 화면에 붙이고 그 전에 붙어있던 fragment는 destroy.
            fragmentManager.popBackStack();
            //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
            Intent intent = new Intent(getApplicationContext(), EmptyActivity.class);
            startActivity(intent);


        }
        //스택에 프래그먼트가 존재하지 않는 경우
        else {
            super.onBackPressed();
        }
    }

}
