package com.example.sns;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.sns.JoinActivity.IP_ADDRESS;



public class AccountPageFragment extends Fragment implements HttpRequest.OnHttpResponseListener {
    private final String TAG = "AccountPageFragment";
    private ImageButton btn_more, ib_grid, ib_vertical;
    private TextView tv_name, tv_nickname, tv_introduce, tv_postcount, tv_post, tv_followercount, tv_follower, tv_followingcount, tv_following;
    private LinearLayout follower, following, button_container;


    //사용자 계정
    private String hostAccount;
    private CircleImageView img_profile;
    private Button btn_follow, btn_message, btn_faceChat;


    //그리드 리사이클러뷰를 담는 프레그먼스
    private Fragment gridPostFragment;

    //수직 리사이클러뷰를 담는 프레그먼트
    private Fragment postFragment;

    //프레그먼트 매니저
    private FragmentManager fragmentManager;

    private FragmentManager childFragmentManager;

    //새로고침
    private SwipeRefreshLayout swipeRefreshLayout;

    //현재 화면에 보이는 프래그먼트
    private String currentFragment = "grid";

    private boolean isMyPost;

    private boolean isFollowing;

    //사용자 닉네임
    private String nickname;
    //사용자 프로필 이미지
    private String image;

    //부모 액티비티
    private String parentActivity;

    private int followingCount;
    private int followerCount;

    private LoginUser loginUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView 호출");
        View rootView = inflater.inflate(R.layout.fragment_accountpage, container, false);

        loginUser = LoginUser.getInstance();

        img_profile = rootView.findViewById(R.id.img_profile);

        //게시물 수
        tv_postcount = rootView.findViewById(R.id.textview_postcount);
        //게시물
        tv_post = rootView.findViewById(R.id.textview_post);

        //팔로워 수
        tv_followercount = rootView.findViewById(R.id.textview_followercount);
        //팔로워
        tv_follower = rootView.findViewById(R.id.textview_follower);

        //팔로잉 수
        tv_followingcount = rootView.findViewById(R.id.textview_followingcount);
        //팔로잉
        tv_following = rootView.findViewById(R.id.textview_following);

        follower = rootView.findViewById(R.id.follower);
        following = rootView.findViewById(R.id.following);

        ib_grid = rootView.findViewById(R.id.imagebutton_grid);
        ib_vertical = rootView.findViewById(R.id.imagebutton_vertical);

        btn_follow = rootView.findViewById(R.id.button_follow);
        btn_message = rootView.findViewById(R.id.button_message);
        btn_faceChat = rootView.findViewById(R.id.button_facechat);

        button_container = rootView.findViewById(R.id.button_container);

        ib_grid.setSelected(true);
        ib_vertical.setSelected(false);


        if (getArguments() != null) {
            hostAccount = getArguments().getString("account");
            isMyPost = getArguments().getBoolean("isMyPost");
            parentActivity = getArguments().getString("parentActivity");
        }

        //리사이클러뷰 프래그먼트 초기화.
        postFragment = new PostFragment();
        gridPostFragment = new GridPostFragment();

        //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
        Bundle bundle = new Bundle();
        bundle.putString("account", hostAccount);
        bundle.putString("parentActivity", parentActivity);
        postFragment.setArguments(bundle);

        gridPostFragment.setArguments(bundle);

        swipeRefreshLayout = rootView.findViewById(R.id.refresh_layout);

        fragmentManager = getFragmentManager();
        //이 경우
        childFragmentManager = getChildFragmentManager();
        //처음에는 그리드 리사이클러뷰를 화면에 뿌려준다.
        childFragmentManager.beginTransaction().add(R.id.frame_container, gridPostFragment).commit();
        childFragmentManager.beginTransaction().add(R.id.frame_container, postFragment).commit();

        childFragmentManager.beginTransaction().hide(postFragment).commit();
        childFragmentManager.beginTransaction().show(gridPostFragment).commit();

        //수직 게시물 보기 버튼 클릭 리스너
        ib_vertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ib_vertical.setSelected(true);
                ib_grid.setSelected(false);

                //수직 리사이클러뷰 프래그먼트가 초기화된적이 없다면
                if (postFragment == null) {
                    //새롭게 초기화를 해서
                    postFragment = new PostFragment();
                    //프래그먼트 리스트에 추가
                    childFragmentManager.beginTransaction().add(R.id.frame_container, postFragment).commit();
                }

                //그리드 리사이클러뷰 프래그먼트는 숨기고 수직 프래그먼트만 show다.
                if (gridPostFragment != null)
                    childFragmentManager.beginTransaction().hide(gridPostFragment).commit();
                if (postFragment != null)
                    childFragmentManager.beginTransaction().show(postFragment).commit();

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
                    childFragmentManager.beginTransaction().add(R.id.frame_container, gridPostFragment).commit();
                }

                //수직 프래그먼트는 숨기고 그리드 리사이클러뷰를 show
                if (postFragment != null)
                    childFragmentManager.beginTransaction().hide(postFragment).commit();
                if (gridPostFragment != null)
                    childFragmentManager.beginTransaction().show(gridPostFragment).commit();

                currentFragment = "grid";
            }
        });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                setData(hostAccount, loginUser.getAccount());
                //두 프래그먼트를 제거하고
                childFragmentManager.beginTransaction().detach(gridPostFragment).commit();
                childFragmentManager.beginTransaction().detach(postFragment).commit();

                //새롭게 추가해준다
                childFragmentManager.beginTransaction().attach(postFragment).commit();
                childFragmentManager.beginTransaction().attach(gridPostFragment).commit();

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
        btn_more = rootView.findViewById(R.id.button_more);
        if (!isMyPost) {
            btn_more.setVisibility(View.GONE);
        }

        //팔로우 버튼
        //접근한 사용자 페이지가 내 계정이 아닐 때만 팔로우 버튼이 보이게 한다.
        if (isMyPost) {
            button_container.setVisibility(View.GONE);
        }


        //사용자 이름과 닉네임과 자기소개
        tv_name = rootView.findViewById(R.id.textview_name);
        tv_nickname = rootView.findViewById(R.id.textview_nickname);
        tv_introduce = rootView.findViewById(R.id.text_introduction);


        //더 보기 버튼 리스너
        btn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //팝업메뉴 객체 생성
                //param1: 현재 컨텍스트
                //param2: 팝업메뉴를 호출하는 버튼
                PopupMenu popupMenu = new PopupMenu(getContext(), v);

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
                                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                                startActivity(intent);
                                break;

                            //id가 logout인 경우
                            case R.id.logout:
                                //로그아웃을 하면 세션과의 연결을 끊어줘야 하기 때문에 세션id값을 담아둔 쿠키를 삭제해준다.
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    //현재 저장되어 있는 쿠키를 지워준다.
                                    CookieManager.getInstance().removeAllCookies(null);
                                    CookieManager.getInstance().flush();


                                    //세션을 지우기 위한 서브 스레드
//                                    Logout logout = new Logout();
//                                    logout.execute("http://" + IP_ADDRESS + "/logout.php");
                                    logOut(loginUser.getAccount());//로그아웃

                                    //sharedpreference에 저장된 쿠키도 삭제해준다.
                                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("loginUser", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor cookieEditor = sharedPreferences.edit();
                                    cookieEditor.clear();
                                    cookieEditor.apply();
                                }

                                //로그인 액티비티로 이동한다.
                                Intent intent1 = new Intent(getContext(), LoginActivity.class);
                                startActivity(intent1);
                                Toast.makeText(getContext(), "로그아웃", Toast.LENGTH_SHORT).show();
                                getActivity().finish();
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

        //팔로우버튼 클릭 리스너
        btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //팔로우를 하지 않은 상태에서 팔로우를 하려는 경우
                if (isFollowing == false) {
                    //팔로잉 상태로 전환
                    processFollow(true, hostAccount, nickname, loginUser.getAccount(), 0);
                }
                //팔로우를 했던 상태에서 팔로우를 취소하려는 경우
                else {

                    Dialog dialog = new Dialog(getContext());
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

                    //프로필 사진 설정
                    Glide.with(getContext())
                            .load("http://" + IP_ADDRESS + "/profileimage/" + image)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop())
                            .into(cv_profile);

                    //언팔로우 다이얼로그 멘트 설정
                    tv_ment.setText("생각이 바뀌시면 " + nickname + "님을 다시 팔로우할 수 있습니다.");

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
                            processFollow(false, hostAccount, nickname, loginUser.getAccount(), 0);
                        }
                    });

                    dialog.show();

                }
            }
        });

        //팔로잉 버튼 클릭 리스너
        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (parentActivity.equals("PostActivity")) {
                    FollowingListFragment followingListFragment = new FollowingListFragment();

                    Fragment fragment = PostActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);


                    //서버에서 데이터를 조회해서 가져오기 위해 필요한 로그인한 사용자 계정을 프래그먼트로 넘겨준다.
                    Bundle bundle = new Bundle();
                    bundle.putString("account", hostAccount);
                    bundle.putString("parentActivity", parentActivity);
                    followingListFragment.setArguments(bundle);

                    //프래그먼트를 프래임 레이아웃에 붙여준다.
                    PostActivity.fragmentManager.beginTransaction().add(R.id.frame_parent_container, followingListFragment).addToBackStack(null).commit();
                    PostActivity.fragmentManager.beginTransaction().show(followingListFragment).commit();
                } else if (parentActivity.equals("SearchActivity")) {
                    FollowingListFragment followingListFragment = new FollowingListFragment();

                    Fragment fragment = SearchActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);


                    //서버에서 데이터를 조회해서 가져오기 위해 필요한 로그인한 사용자 계정을 프래그먼트로 넘겨준다.
                    Bundle bundle = new Bundle();
                    bundle.putString("account", hostAccount);
                    bundle.putString("parentActivity", parentActivity);
                    followingListFragment.setArguments(bundle);

                    //프래그먼트를 프래임 레이아웃에 붙여준다.
                    SearchActivity.fragmentManager.beginTransaction().add(R.id.frame_parent_container, followingListFragment).addToBackStack(null).commit();
                    SearchActivity.fragmentManager.beginTransaction().show(followingListFragment).commit();
                } else if (parentActivity.equals("NotificationActivity")) {
                    FollowingListFragment followingListFragment = new FollowingListFragment();

                    Fragment fragment = NotificationActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

                    //서버에서 데이터를 조회해서 가져오기 위해 필요한 로그인한 사용자 계정을 프래그먼트로 넘겨준다.
                    Bundle bundle = new Bundle();
                    bundle.putString("account", hostAccount);
                    bundle.putString("parentActivity", parentActivity);
                    followingListFragment.setArguments(bundle);

                    //프래그먼트를 프래임 레이아웃에 붙여준다.
                    NotificationActivity.fragmentManager.beginTransaction().add(R.id.frame_parent_container, followingListFragment).addToBackStack(null).commit();
                    NotificationActivity.fragmentManager.beginTransaction().show(followingListFragment).commit();
                } else {
                    FollowingListFragment followingListFragment = new FollowingListFragment();

                    Fragment fragment = MypageActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);


                    //서버에서 데이터를 조회해서 가져오기 위해 필요한 로그인한 사용자 계정을 프래그먼트로 넘겨준다.
                    Bundle bundle = new Bundle();
                    bundle.putString("account", hostAccount);
                    bundle.putString("parentActivity", parentActivity);
                    followingListFragment.setArguments(bundle);

                    //프래그먼트를 프래임 레이아웃에 붙여준다.
                    MypageActivity.fragmentManager.beginTransaction().add(R.id.frame_parent_container, followingListFragment).addToBackStack(null).commit();
                    MypageActivity.fragmentManager.beginTransaction().show(followingListFragment).commit();
                }
            }
        });

        //팔로워 버튼 클릭 리스너
        follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (parentActivity.equals("PostActivity")) {
                    FollowerListFragment followerListFragment = new FollowerListFragment();

                    Fragment fragment = PostActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);


                    //서버에서 데이터를 조회해서 가져오기 위해 필요한 로그인한 사용자 계정을 프래그먼트로 넘겨준다.
                    Bundle bundle = new Bundle();
                    bundle.putString("account", hostAccount);
                    bundle.putString("parentActivity", parentActivity);
                    followerListFragment.setArguments(bundle);

                    //프래그먼트를 프래임 레이아웃에 붙여준다.
                    PostActivity.fragmentManager.beginTransaction().add(R.id.frame_parent_container, followerListFragment).addToBackStack(null).commit();
                    PostActivity.fragmentManager.beginTransaction().show(followerListFragment).commit();
                } else if (parentActivity.equals("SearchActivity")) {
                    FollowerListFragment followerListFragment = new FollowerListFragment();

                    Fragment fragment = SearchActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);


                    //서버에서 데이터를 조회해서 가져오기 위해 필요한 로그인한 사용자 계정을 프래그먼트로 넘겨준다.
                    Bundle bundle = new Bundle();
                    bundle.putString("account", hostAccount);
                    bundle.putString("parentActivity", parentActivity);
                    followerListFragment.setArguments(bundle);

                    //프래그먼트를 프래임 레이아웃에 붙여준다.
                    SearchActivity.fragmentManager.beginTransaction().add(R.id.frame_parent_container, followerListFragment).addToBackStack(null).commit();
                    SearchActivity.fragmentManager.beginTransaction().show(followerListFragment).commit();
                } else if (parentActivity.equals("NotificationActivity")) {
                    FollowerListFragment followerListFragment = new FollowerListFragment();

                    Fragment fragment = NotificationActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);


                    //서버에서 데이터를 조회해서 가져오기 위해 필요한 로그인한 사용자 계정을 프래그먼트로 넘겨준다.
                    Bundle bundle = new Bundle();
                    bundle.putString("account", hostAccount);
                    bundle.putString("parentActivity", parentActivity);
                    followerListFragment.setArguments(bundle);

                    //프래그먼트를 프래임 레이아웃에 붙여준다.
                    NotificationActivity.fragmentManager.beginTransaction().add(R.id.frame_parent_container, followerListFragment).addToBackStack(null).commit();
                    NotificationActivity.fragmentManager.beginTransaction().show(followerListFragment).commit();
                } else {
                    FollowerListFragment followerListFragment = new FollowerListFragment();

                    Fragment fragment = MypageActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);


                    //서버에서 데이터를 조회해서 가져오기 위해 필요한 로그인한 사용자 계정을 프래그먼트로 넘겨준다.
                    Bundle bundle = new Bundle();
                    bundle.putString("account", hostAccount);
                    bundle.putString("parentActivity", parentActivity);
                    followerListFragment.setArguments(bundle);

                    //프래그먼트를 프래임 레이아웃에 붙여준다.
                    MypageActivity.fragmentManager.beginTransaction().add(R.id.frame_parent_container, followerListFragment).addToBackStack(null).commit();
                    MypageActivity.fragmentManager.beginTransaction().show(followerListFragment).commit();
                }
            }
        });

        //메세지 버튼 클릭 리스너
        btn_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //chatactivity로 intent
                startActivity(new Intent(getActivity(), ChatActivity.class)
                        .putExtra("isFromChatRoom", true)
                        .putExtra("selectedUserAccount", hostAccount));
            }
        });

        //영상통화 버튼 클릭 리스너
        btn_faceChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //권한 체크
                TedPermission.with(getActivity())
                        .setPermissionListener(new PermissionListener() {
                            //권한 허가가 됐을 때 콜백
                            @Override
                            public void onPermissionGranted() {
                                //소켓서버를 통해서 수신자에게 영상통화 요청을 전달
                                try {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("type", "requestFaceChat");
                                    jsonObject.put("roomName", loginUser.getAccount() + hostAccount);//영상통화의 방 이름을 보내준다. 이때 방은 두 peer의 계정을 합쳐서 구성한다.
                                    jsonObject.put("account", loginUser.getAccount());//발신자 계정
                                    jsonObject.put("nickname", loginUser.getNickname());//발신자 닉네임
                                    jsonObject.put("profile", loginUser.getProfile());//발신자 프로필 사진
                                    jsonObject.put("receiver", hostAccount);//수신자 계정
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
            }
        });

        return rootView;
    }


    @Override
    public void onResume() {
        Log.d(TAG, "onResume 호출");
        super.onResume();
        //데이터를 가져온다.
        setData(hostAccount, loginUser.getAccount());
    }

    //마이페이지에서 자신의 데이터를 서버에서 가져와서 화면에 보이게 하는 메소드
    public void setData(String userAccount, String myAccount) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://13.124.105.47/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        Call<ProfileResponse> call = retrofitService.getProfileResponse(userAccount, myAccount);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                ProfileResponse profileResponse = response.body();
                Log.d("리턴 값", profileResponse.toString());
                String account = profileResponse.getAccount();
                int postCount = profileResponse.getPostCount();
                followerCount = profileResponse.getFollowerCount();
                followingCount = profileResponse.getFollowingCount();
                String name = profileResponse.getName();
                nickname = profileResponse.getNickname();
                String email = profileResponse.getEmail();
                String introduce = profileResponse.getIntroduce();
                image = profileResponse.getImage();
                isFollowing = profileResponse.getIsFollowing();


                tv_name.setText(name);
                tv_nickname.setText(nickname);

                if (introduce != null) {
                    tv_introduce.setText(introduce);
                }
                tv_postcount.setText(String.valueOf(postCount));
                tv_followercount.setText(String.valueOf(followerCount));
                tv_followingcount.setText(String.valueOf(followingCount));

                //팔로우 버튼 설정
                //만약 내가 해당 사용자를 팔로우하고 있다면 팔로우 버튼을 팔로잉 버튼으로 교체한다.
                if (isFollowing) {
                    btn_follow.setText("팔로잉");
                    btn_follow.setTextColor(Color.parseColor("#000000"));
                    btn_follow.setBackgroundResource(R.drawable.et_border);
                } else {
                    btn_follow.setText("팔로우");
                    btn_follow.setTextColor(Color.parseColor("#ffffff"));
                    btn_follow.setBackgroundResource(R.drawable.bluebutton);
                }


                //프로필 사진도 필수 항목이 아니라서 null값일 수도 있기 때문에 null체크를 해준다.
                if (image != null) {

                    Glide.with(getContext())
                            .load("http://" + IP_ADDRESS + "/profileimage/" + image)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop())
                            .into(img_profile);
                }
                //만약 프로필 사진이 null이라면 default사진 설정
                else {
                    Glide.with(getContext())
                            .load(R.drawable.profile)
                            .into(img_profile);
                }

            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(getContext(), "문제가 생겼습니다.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //팔로우 프로세스 통신을 담당하는 메소드
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

    //서버와 통신에 성공했을 때 호출되는 콜백 메소드(HttpRequest클래스(AsyncTask클래스 상속)의 onPostExecute()메소드에서 호출)
    //모든 통신에 대한 결과는 이 콜백 메소드에서 처리해준다.
    @Override
    public void onHttpResponse(String result) {
        if (result != null) {
            Log.d(TAG, "통신 성공");
            Log.d(TAG, "서버에서 넘어온 json데이터- " + result);
            try {
                JSONObject responseBody = new JSONObject(result);
                String requestType = responseBody.getString("requestType");
                if (requestType.equals("logOut")) {//로그아웃 통신
                    Toast.makeText(getContext(), "로그아웃", Toast.LENGTH_SHORT).show();
                } else if (requestType.equals("processFollow")) {//팔로우 프로세스 통신
                    boolean isFollowing = responseBody.getBoolean("isFollowing");//팔로우 상태
                    //팔로우를 한 경우
                    if (isFollowing) {
                        //팔로우 상태를 true로 만들고
                        this.isFollowing = true;
                        //팔로우 버튼을 팔로잉 버튼으로 전환
                        btn_follow.setBackgroundResource(R.drawable.et_border);
                        btn_follow.setText("팔로잉");
                        btn_follow.setTextColor(Color.parseColor("#000000"));
                        followerCount += 1;
                        tv_followercount.setText(String.valueOf(followerCount));

                        //팔로우를 당한 사용자 단말에 push알림을 보내준다.
                        String receiver = hostAccount;
                        String title = "SNS";
                        String body = loginUser.getNickname() + "님이 회원님을 팔로우하기 시작했습니다.";
                        String click_action = "AccountPageFragment";
                        String category = "follow";
                        String sender = loginUser.getAccount();
                        pushNotification(receiver, title, body, click_action, category, sender);

                        Toast.makeText(getContext(), nickname + "님을 팔로우 하셨습니다!", Toast.LENGTH_SHORT).show();

                    }
                    //팔로우를 취소한 경우
                    else {
                        //팔로우 상태를 false로 만들고
                        this.isFollowing = false;
                        //팔로잉 버튼을 팔로우 버튼으로 전환
                        btn_follow.setBackgroundResource(R.drawable.bluebutton);
                        btn_follow.setText("팔로우");
                        btn_follow.setTextColor(Color.parseColor("#ffffff"));
                        followerCount -= 1;
                        tv_followercount.setText(String.valueOf(followerCount));
                        Toast.makeText(getContext(), nickname + "님을 언팔로우 하셨습니다", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
