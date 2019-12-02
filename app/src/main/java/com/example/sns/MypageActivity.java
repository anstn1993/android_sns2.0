package com.example.sns;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.sns.JoinActivity.IP_ADDRESS;


public class MypageActivity extends AppCompatActivity implements HttpRequest.OnHttpResponseListener {

    ImageButton btn_more, ib_grid, ib_vertical;
    TextView tv_name, tv_nickname, tv_introduce, tv_postcount, tv_post, tv_followercount, tv_follower, tv_followingcount, tv_following;
    LinearLayout follower, following;
    //세션 쿠키
    String cookie;
    //사용자 계정
    String account;
    CircleImageView img_profile;


    //그리드 리사이클러뷰를 담는 프레그먼스
    Fragment gridPostFragment;

    //수직 리사이클러뷰를 담는 프레그먼트
    Fragment postFragment;

    //게시물 뷰 프레그먼트 매니저
    FragmentManager childFragmentManager;

    //새로고침
    SwipeRefreshLayout swipeRefreshLayout;

    //현재 화면에 보이는 프래그먼트
    String currentFragment = "grid";

    //프래그먼트 메니저
    public static FragmentManager fragmentManager;

    public static HashTagPostListFragment hashTagPostListFragment;
    public static SearchedPlaceListFragment searchedPlaceListFragment;

    public static LikeListFragment likeListFragment;
    public static AccountPageFragment accountPageFragment;
    public static PostDetailFragment postDetailFragment;
    public static FollowingListFragment followingListFragment;
    public static FollowerListFragment followerListFragment;


    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        Log.d("마이페이지 onCreate", "호출");

        context = getApplicationContext();

        fragmentManager = getSupportFragmentManager();

        likeListFragment = new LikeListFragment();
        accountPageFragment = new AccountPageFragment();
        postDetailFragment = new PostDetailFragment();
        followingListFragment = new FollowingListFragment();
        followerListFragment = new FollowerListFragment();
        hashTagPostListFragment = new HashTagPostListFragment();
        searchedPlaceListFragment = new SearchedPlaceListFragment();

        account = LoginUser.getInstance().getAccount();

        //프로필 사진
        img_profile = findViewById(R.id.img_profile);

        //게시물 수
        tv_postcount = findViewById(R.id.textview_postcount);
        //게시물
        tv_post = findViewById(R.id.textview_post);

        //팔로워 수
        tv_followercount = findViewById(R.id.textview_followercount);
        //팔로워
        tv_follower = findViewById(R.id.textview_follower);

        //팔로잉 수
        tv_followingcount = findViewById(R.id.textview_followingcount);
        //팔로잉
        tv_following = findViewById(R.id.textview_following);


        ib_grid = findViewById(R.id.imagebutton_grid);
        ib_vertical = findViewById(R.id.imagebutton_vertical);

        follower = findViewById(R.id.follower);
        following = findViewById(R.id.following);

        ib_grid.setSelected(true);
        ib_vertical.setSelected(false);


        //리사이클러뷰 프래그먼트 초기화.
        postFragment = new PostFragment();
        gridPostFragment = new GridPostFragment();

        //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
        Bundle bundle = new Bundle();
        bundle.putString("account", account);
        bundle.putString("parentActivity", "MyPageActivity");
        postFragment.setArguments(bundle);
        gridPostFragment.setArguments(bundle);

        swipeRefreshLayout = findViewById(R.id.refresh_layout);


        childFragmentManager = getSupportFragmentManager();
        //처음에는 그리드 리사이클러뷰를 화면에 뿌려준다.
        childFragmentManager.beginTransaction().add(R.id.frame_container, gridPostFragment).commit();
        childFragmentManager.beginTransaction().add(R.id.frame_container, postFragment).commit();

        childFragmentManager.beginTransaction().hide(postFragment).commit();
        childFragmentManager.beginTransaction().show(gridPostFragment).commit();

        //수직 게시물 보기 버튼 클릭 리스너
        ib_vertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ib_grid.setSelected(false);
                ib_vertical.setSelected(true);

                //수직 리사이클러뷰 프래그먼트가 초기화된적이 없다면
                if (postFragment == null) {
                    //새롭게 초기화를 해서
                    postFragment = new PostFragment();
                    //프래그먼트 리스트에 추가
                    fragmentManager.beginTransaction().add(R.id.frame_container, postFragment).commit();
                }

                //그리드 리사이클러뷰 프래그먼트는 숨기고 수직 프래그먼트만 show다.
                if (gridPostFragment != null)
                    fragmentManager.beginTransaction().hide(gridPostFragment).commit();
                if (postFragment != null)
                    fragmentManager.beginTransaction().show(postFragment).commit();

                currentFragment = "vertical";
            }
        });

        //그리드 게시물 보기 버튼 클릭 리스너
        ib_grid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ib_grid.setSelected(true);
                ib_vertical.setSelected(false);

                //그리드 리사이클러뷰 프래그먼트가 초기화된적이 없다면
                if (gridPostFragment == null) {
                    //새롭게 프래그먼트를 초기화하고
                    gridPostFragment = new GridPostFragment();
                    //프래그먼트 리스트에 추가해준다.
                    fragmentManager.beginTransaction().add(R.id.frame_container, gridPostFragment).commit();
                }

                //수직 프래그먼트는 숨기고 그리드 리사이클러뷰를 show
                if (postFragment != null)
                    fragmentManager.beginTransaction().hide(postFragment).commit();
                if (gridPostFragment != null)
                    fragmentManager.beginTransaction().show(gridPostFragment).commit();

                currentFragment = "grid";
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                setMyData(account);

                //두 프래그먼트를 제거하고
                fragmentManager.beginTransaction().detach(gridPostFragment).commit();
                fragmentManager.beginTransaction().detach(postFragment).commit();

                //새롭게 추가해준다
                fragmentManager.beginTransaction().attach(postFragment).commit();
                fragmentManager.beginTransaction().attach(gridPostFragment).commit();

                //새로고침 이전에 화면에 보이던 프래그먼트를 다시 화면에 뿌려준다.
                if (currentFragment.equals("vertical")) {
                    fragmentManager.beginTransaction().hide(gridPostFragment).commit();
                    fragmentManager.beginTransaction().show(postFragment).commit();

                } else {
                    fragmentManager.beginTransaction().hide(postFragment).commit();
                    fragmentManager.beginTransaction().show(gridPostFragment).commit();

                }

                swipeRefreshLayout.setRefreshing(false);
            }
        });


        //더 보기 버튼
        btn_more = findViewById(R.id.button_more);

        //사용자 이름과 닉네임과 자기소개
        tv_name = findViewById(R.id.textview_name);
        tv_nickname = findViewById(R.id.textview_nickname);
        tv_introduce = findViewById(R.id.text_introduction);


        //더 보기 버튼 리스너
        btn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //팝업메뉴 객체 생성
                //param1: 현재 컨텍스트
                //param2: 팝업메뉴를 호출하는 버튼
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);

                //xml파일로 만들어둔 메뉴 레이아웃을 인플레이트 한다.
                popupMenu.getMenuInflater().inflate(R.menu.more_menu, popupMenu.getMenu());

                //팝업 메뉴의 아이템 클릭 리스너
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //xml파일에 설정해둔 아이템의 id에 따라 분기
                        switch (item.getItemId()) {
                            //id가 edit_profile인 경우
                            case R.id.edit_profile:
                                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                                startActivity(intent);
                                break;

                            //id가 logout인 경우
                            case R.id.logout:
                                //로그아웃을 하면 세션과의 연결을 끊어줘야 하기 때문에 세션id값을 담아둔 쿠키를 삭제해준다.
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    //현재 저장되어 있는 쿠키를 지워준다.
                                    CookieManager.getInstance().removeAllCookies(null);
                                    CookieManager.getInstance().flush();

                                    logOut(account);//로그아웃


                                    //sharedpreference에 저장된 쿠키도 삭제해준다.
                                    SharedPreferences sharedPreferences = getSharedPreferences("loginUser", MODE_PRIVATE);
                                    SharedPreferences.Editor cookieEditor = sharedPreferences.edit();
                                    cookieEditor.clear();
                                    cookieEditor.apply();
                                }

                                //로그인 액티비티로 이동한다.
                                Intent intent1 = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent1);
                                Toast.makeText(getApplicationContext(), "로그아웃", Toast.LENGTH_SHORT).show();
                                finish();

                                break;

                            default:
                                break;

                        }

                        return false;
                    }
                });
                //팝업 메뉴 실행
                popupMenu.show();
            }
        });

        //팔로잉 클릭 리스너
        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FollowingListFragment myFollowingListFragment = new FollowingListFragment();

                //서버에서 데이터를 조회해서 가져오기 위해 필요한 로그인한 사용자 계정을 프래그먼트로 넘겨준다.
                Bundle bundle = new Bundle();
                bundle.putString("account", account);
                bundle.putString("parentActivity", "MyPageActivity");
                myFollowingListFragment.setArguments(bundle);
                //프래그먼트를 프래임 레이아웃에 붙여준다.
                MypageActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, myFollowingListFragment).addToBackStack(null).commit();
                MypageActivity.fragmentManager.beginTransaction().show(myFollowingListFragment).commit();

            }
        });


        //팔로워 클릭 리스너
        follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FollowerListFragment myFollowerListFragment = new FollowerListFragment();

                //서버에서 데이터를 조회해서 가져오기 위해 필요한 로그인한 사용자 계정을 프래그먼트로 넘겨준다.
                Bundle bundle = new Bundle();
                bundle.putString("account", account);
                bundle.putString("parentActivity", "MyPageActivity");
                myFollowerListFragment.setArguments(bundle);
                //프래그먼트를 프래임 레이아웃에 붙여준다.
                MypageActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, myFollowerListFragment).addToBackStack(null).commit();
                MypageActivity.fragmentManager.beginTransaction().show(myFollowerListFragment).commit();
            }
        });
    }


    //마이페이지에서 자신의 데이터를 서버에서 가져와서 화면에 보이게 하는 메소드
    public void setMyData(String account) {


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://13.124.105.47/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        Call<ProfileResponse> call = retrofitService.getProfileResponse(account, account);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                ProfileResponse profileResponse = response.body();
                Log.d("리턴 값", profileResponse.toString());


                int postCount = profileResponse.getPostCount();
                int followerCount = profileResponse.getFollowerCount();
                int followingCount = profileResponse.getFollowingCount();
                String name = profileResponse.getName();
                String nickname = profileResponse.getNickname();
                String email = profileResponse.getEmail();
                String introduce = profileResponse.getIntroduce();
                String image = profileResponse.getImage();


                tv_name.setText(name);
                tv_nickname.setText(nickname);

                if (introduce != null) {
                    tv_introduce.setText(introduce);
                }

                tv_postcount.setText(String.valueOf(postCount));
                tv_followercount.setText(String.valueOf(followerCount));
                tv_followingcount.setText(String.valueOf(followingCount));


                //프로필 사진도 필수 항목이 아니라서 null값일 수도 있기 때문에 null체크를 해준다.
                if (image != null) {
                    Glide.with(MypageActivity.this)
                            .load("http://" + IP_ADDRESS + "/profileimage/" + image)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop())
                            .into(img_profile);
                }
                //만약 프로필 사진이 null이라면 default사진 설정
                else {
                    Glide.with(MypageActivity.this)
                            .load(R.drawable.profile)
                            .into(img_profile);
                }

            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "문제가 생겼습니다.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //로그아웃을 하기 위한 통신을 담당하는 메소드
    private void logOut(String account) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("account", account);//로그아웃하는 계정
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "logout.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    ;

    //서버와 통신에 성공했을 때 호출되는 콜백 메소드(HttpRequest클래스(AsyncTask클래스 상속)의 onPostExecute()메소드에서 호출)
    //모든 통신에 대한 결과는 이 콜백 메소드에서 처리해준다.
    @Override
    public void onHttpResponse(String result) {
        if (result != null) {
            Toast.makeText(getApplicationContext(), "로그아웃", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("마이페이지 onResume", "호출");
        setMyData(account);

    }

    @Override
    public void onBackPressed() {
        //프래그먼트 스택에 프래그먼트가 존재하면
        if (MypageActivity.fragmentManager.getBackStackEntryCount() != 0) {
            Log.d("프래그먼트 스택 존재", "호출");
            //스택의 최상단에 있는 프래그먼트를 화면에 붙이고 그 전에 붙어있던 fragment는 destroy.
            MypageActivity.fragmentManager.popBackStackImmediate();
            //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
            Intent intent = new Intent(getApplicationContext(), EmptyActivity.class);
            startActivity(intent);


        }
        //현재 화면에 액티비티라면 PostActivity로 이동.
        else {
//            super.onBackPressed();
            MainActivity.tabHost.setCurrentTab(0);
        }
    }
}
