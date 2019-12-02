package com.example.sns;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.sns.JoinActivity.IP_ADDRESS;
import static com.example.sns.PostActivity.likeListFragment;


public class LikeListFragment extends Fragment implements LikeListAdapter.LikeListRecyclerViewListener, HttpRequest.OnHttpResponseListener {
    private String TAG = "LikeListFragment";
    private TextView tv_like;
    private ImageButton im_back;
    private Button bt_tothetop;
    private ConstraintLayout container;
    private SwipeRefreshLayout swipeRefreshLayout;

    //리사이클러뷰
    private RecyclerView rv_likelist;
    //리사이클러뷰 레이아웃 매니저
    private LinearLayoutManager linearLayoutManager;
    //리사이클러뷰 어댑터
    private LikeListAdapter likeListAdapter;
    //리사이클러뷰 아이템 arraylist
    private ArrayList<LikeListItem> likeListItemArrayList;

    //게시물 번호
    private int postNum;

    //부모 액티비티;
    private String parentActivity;

    private FragmentManager fragmentManager;

    private LoginUser loginUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("좋아요 리스트 onCreateView", "호출");
        View rootView = (ViewGroup) inflater.inflate(R.layout.fragment_likelist, container, false);
        loginUser = LoginUser.getInstance();
        tv_like = rootView.findViewById(R.id.textview_like);
        im_back = rootView.findViewById(R.id.imagebutton_back);
        bt_tothetop = rootView.findViewById(R.id.button_tothetop);

        swipeRefreshLayout = rootView.findViewById(R.id.refresh_layout);

        fragmentManager = getFragmentManager();

        //새로고침 리스너
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //리스트 아이템 arraylist를 다 비워주고 다시 채워준다.
                likeListItemArrayList.clear();

                //서버에서 처음 데이터 겟
                getLikeList(postNum, 0, loginUser.getAccount());
                //새로 고침 종료
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        if (getArguments() != null) {
            //게시물 액티비티에서 번들로 넘긴 게시물 번호를 받아준다.
            postNum = getArguments().getInt("postNum", 0);
            parentActivity = getArguments().getString("parentActivity");
        }

        //뒤로가기 버튼 클릭 리스너
        im_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (parentActivity.equals("PostActivity")) {
                    if (PostActivity.fragmentManager.getBackStackEntryCount() == 0) {
                        PostActivity.fragmentManager.beginTransaction().remove(likeListFragment).commit();
                        PostActivity.fragmentManager.popBackStack();
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);
                    } else {
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙인다.
                        PostActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getActivity(), EmptyActivity.class);
                        startActivity(intent);
                    }
                } else if (parentActivity.equals("SearchActivity")) {
                    if (SearchActivity.fragmentManager.getBackStackEntryCount() == 0) {
                        SearchActivity.fragmentManager.beginTransaction().remove(likeListFragment).commit();
                        SearchActivity.fragmentManager.popBackStack();
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);
                    } else {
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙인다.
                        SearchActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getActivity(), EmptyActivity.class);
                        startActivity(intent);
                    }
                } else if (parentActivity.equals("NotificationActivity")) {
                    if (NotificationActivity.fragmentManager.getBackStackEntryCount() == 0) {
                        NotificationActivity.fragmentManager.beginTransaction().remove(likeListFragment).commit();
                        NotificationActivity.fragmentManager.popBackStack();
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);
                    } else {
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙인다.
                        NotificationActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getActivity(), EmptyActivity.class);
                        startActivity(intent);
                    }
                } else {
                    if (MypageActivity.fragmentManager.getBackStackEntryCount() == 0) {
                        MypageActivity.fragmentManager.beginTransaction().remove(likeListFragment).commit();
                        MypageActivity.fragmentManager.popBackStack();
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);
                    } else {
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙인다.
                        MypageActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getActivity(), EmptyActivity.class);
                        startActivity(intent);
                    }
                }


            }
        });


        rv_likelist = rootView.findViewById(R.id.recyclerview_likelist);

        //리사이클러뷰 셋
        setRecyclerView();

        return rootView;
    }

    private void setRecyclerView() {

        rv_likelist.setHasFixedSize(true);
        likeListItemArrayList = new ArrayList<>();

        //레이아웃 메니저 설정
        linearLayoutManager = new LinearLayoutManager(getContext());
        rv_likelist.setLayoutManager(linearLayoutManager);

        //어댑터 설정
        likeListAdapter = new LikeListAdapter(getContext(), likeListItemArrayList);
        likeListAdapter.setOnClickListener(this);
        rv_likelist.setAdapter(likeListAdapter);
    }


    private void getLikeList(int postNum, int lastId, String account) {
        try {
            JSONObject requetsBody = new JSONObject();
            requetsBody.put("postNum", postNum);
            requetsBody.put("lastId", lastId);
            requetsBody.put("myAccount", account);
            HttpRequest httpRequest = new HttpRequest("GET", requetsBody.toString(), "getlikelist.php", this);
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

    @Override
    public void onHttpResponse(String result) {
        Log.d(TAG, "통신 성공");
        Log.d(TAG, "서버에서 넘어온 json데이터- " + result);
        if (result != null) {
            try {
                JSONObject responseBody = new JSONObject(result);
                String requestType = responseBody.getString("requestType");
                if (requestType.equals("getLikeList")) {//좋아요 목록을 가져오는 통신
                    JSONArray jsonArray = responseBody.getJSONArray("likelist");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        int id = data.getInt("id");
                        String account = data.getString("account");
                        String profile = data.getString("profile");
                        String nickname = data.getString("nickname");
                        boolean isFollowing = data.getBoolean("isFollowing");

                        LikeListItem likeListItem = new LikeListItem();
                        likeListItem.setId(id);
                        likeListItem.setAccount(account);
                        likeListItem.setPostNum(postNum);
                        likeListItem.setNickname(nickname);
                        likeListItem.setProfile(profile);
                        likeListItem.setFollowing(isFollowing);

                        likeListItemArrayList.add(likeListItem);
                    }

                    likeListAdapter.notifyDataSetChanged();
                } else if (requestType.equals("processFollow")) {
                    boolean isFollowing = responseBody.getBoolean("isFollowing");
                    int position = responseBody.getInt("position");
                    String nickname = responseBody.getString("followedNickname");
                    //팔로우를 한 경우
                    if (isFollowing) {
                        //팔로우 상태를 true로 만들고
                        likeListItemArrayList.get(position).setFollowing(true);
                        //어댑터에 notify
                        likeListAdapter.notifyItemChanged(position, "true");
                        Toast.makeText(getContext(), nickname + "님을 팔로우 하셨습니다!", Toast.LENGTH_SHORT).show();
                        //팔로우를 당한 사용자 단말에 push알림을 보내준다.
                        String receiver = likeListItemArrayList.get(position).account;
                        String title = "SNS";
                        String body = loginUser.getNickname() + "님이 회원님을 팔로우하기 시작했습니다.";
                        String click_action = "AccountPageFragment";
                        String category = "follow";
                        String sender = loginUser.getAccount();
                        pushNotification(receiver, title, body, click_action, category, sender);
                    }
                    //팔로우를 취소한 경우
                    else {
                        //팔로우 상태를 false로 만들고
                        likeListItemArrayList.get(position).setFollowing(false);
                        //어댑터에 notify
                        likeListAdapter.notifyItemChanged(postNum, "false");
                        Toast.makeText(getContext(), nickname + "님을 언팔로우 하셨습니다", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onContainerClicked(int position) {

        if (parentActivity.equals("PostActivity")) {
            //사용자 계정 프래그먼트 객체 선언
            AccountPageFragment accountPageFragment = new AccountPageFragment();

            Fragment fragment = PostActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

            //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
            Bundle bundle = new Bundle();
            bundle.putString("account", likeListItemArrayList.get(position).getAccount());
            bundle.putString("parentActivity", parentActivity);
            //좋아요 리스트의 계정이 로그인한 사용자의 계정과 같으면
            if (loginUser.getAccount().equals(likeListItemArrayList.get(position).getAccount())) {
                //나의 게시물을 true
                bundle.putBoolean("isMyPost", true);
            }
            //좋아요 리스트의 계정이 로그인한 사용자의 계정과 다르면
            else {
                //나의 게시물을 false
                bundle.putBoolean("isMyPost", false);
            }

            accountPageFragment.setArguments(bundle);

            PostActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, accountPageFragment).show(accountPageFragment).addToBackStack(null).commit();
            PostActivity.fragmentManager.beginTransaction().show(accountPageFragment).commit();
        } else if (parentActivity.equals("SearchActivity")) {
            //사용자 계정 프래그먼트 객체 선언
            AccountPageFragment accountPageFragment = new AccountPageFragment();

            Fragment fragment = SearchActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

            //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
            Bundle bundle = new Bundle();
            bundle.putString("account", likeListItemArrayList.get(position).getAccount());
            bundle.putString("parentActivity", parentActivity);
            //좋아요 리스트의 계정이 로그인한 사용자의 계정과 같으면
            if (loginUser.getAccount().equals(likeListItemArrayList.get(position).getAccount())) {
                //나의 게시물을 true
                bundle.putBoolean("isMyPost", true);
            }
            //좋아요 리스트의 계정이 로그인한 사용자의 계정과 다르면
            else {
                //나의 게시물을 false
                bundle.putBoolean("isMyPost", false);
            }

            accountPageFragment.setArguments(bundle);

            SearchActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, accountPageFragment).show(accountPageFragment).addToBackStack(null).commit();
            SearchActivity.fragmentManager.beginTransaction().show(accountPageFragment).commit();
        } else if (parentActivity.equals("NotificationActivity")) {
            //사용자 계정 프래그먼트 객체 선언
            AccountPageFragment accountPageFragment = new AccountPageFragment();

            Fragment fragment = NotificationActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

            //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
            Bundle bundle = new Bundle();
            bundle.putString("account", likeListItemArrayList.get(position).getAccount());
            bundle.putString("parentActivity", parentActivity);
            //좋아요 리스트의 계정이 로그인한 사용자의 계정과 같으면
            if (loginUser.getAccount().equals(likeListItemArrayList.get(position).getAccount())) {
                //나의 게시물을 true
                bundle.putBoolean("isMyPost", true);
            }
            //좋아요 리스트의 계정이 로그인한 사용자의 계정과 다르면
            else {
                //나의 게시물을 false
                bundle.putBoolean("isMyPost", false);
            }

            accountPageFragment.setArguments(bundle);

            NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, accountPageFragment).show(accountPageFragment).addToBackStack(null).commit();
            NotificationActivity.fragmentManager.beginTransaction().show(accountPageFragment).commit();
        } else {
            //사용자 계정 프래그먼트 객체 선언
            AccountPageFragment accountPageFragment = new AccountPageFragment();

            Fragment fragment = MypageActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

            //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
            Bundle bundle = new Bundle();
            bundle.putString("account", likeListItemArrayList.get(position).getAccount());
            bundle.putString("parentActivity", parentActivity);
            //좋아요 리스트의 계정이 로그인한 사용자의 계정과 같으면
            if (loginUser.getAccount().equals(likeListItemArrayList.get(position).getAccount())) {
                //나의 게시물을 true
                bundle.putBoolean("isMyPost", true);
            }
            //좋아요 리스트의 계정이 로그인한 사용자의 계정과 다르면
            else {
                //나의 게시물을 false
                bundle.putBoolean("isMyPost", false);
            }

            accountPageFragment.setArguments(bundle);

            MypageActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, accountPageFragment).show(accountPageFragment).addToBackStack(null).commit();
            MypageActivity.fragmentManager.beginTransaction().show(accountPageFragment).commit();
        }
    }

    @Override
    public void onFollowClicked(int position) {
        //해당 position의 사용자 account
        String followedAccount = likeListItemArrayList.get(position).account;

        //해당 position의 사용자 nickname
        String followedNickname = likeListItemArrayList.get(position).nickname;

        //팔로우를 하지 않은 상태에서 팔로우를 하려는 경우
        if (likeListItemArrayList.get(position).isFollowing == false) {

            //팔로잉 상태로 전환
            processFollow(true, followedAccount, followedNickname, loginUser.getAccount(), position);
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

            //프로필 이미지
            String image = likeListItemArrayList.get(position).profile;


            //프로필 사진 설정
            Glide.with(getContext())
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
                    processFollow(false, followedAccount, followedNickname, loginUser.getAccount(), position);
                }
            });

            dialog.show();

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("좋아요 리스트 onResume", "호출");
        likeListItemArrayList.clear();
        //서버에서 데이터 겟
        getLikeList(postNum, 0, loginUser.getAccount());
    }
}
