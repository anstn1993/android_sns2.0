package com.example.sns;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SearchView;
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

public class FollowerListFragment extends Fragment implements FollowAdapter.FollowListRecyclerViewListener, HttpRequest.OnHttpResponseListener {
    private String TAG = "FollowerListFragment";
    private SearchView sv;
    private TextView tv_hint;
    private ImageButton ib_back;

    //사용자의 계정
    private String hostAccount;
    //부모 액티비티
    private String parentActivity;

    //리사이클러뷰
    private RecyclerView rv_follower;
    //리사이클러뷰 메니저
    private LinearLayoutManager linearLayoutManager;
    //리사이클러뷰 어댑터
    private FollowAdapter followAdapter;

    //팔로잉 아이템 arraylist
    private ArrayList<FollowListItem> followListItemArrayList;

    //현재 로드된 아이템의 마지막 index
    private int currentLastId;

    //검색어
    private String searchText;

    private LoginUser loginUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("팔로잉 프래그먼트 onCreateView", "호출");
        View rootView = inflater.inflate(R.layout.fragment_followerlist, container, false);
        loginUser = LoginUser.getInstance();
        sv = rootView.findViewById(R.id.searchview_search);
        tv_hint = rootView.findViewById(R.id.textview_hint);
        rv_follower = rootView.findViewById(R.id.recyclerview_follower);
        ib_back = rootView.findViewById(R.id.imagebutton_back);

        //서치뷰의 힌트 값 셋
        sv.setQueryHint("팔로워 검색");
        sv.setMaxWidth(Integer.MAX_VALUE);

        sv.setIconifiedByDefault(true);

        //번들에 담긴 데이터를 꺼낸다.
        if (getArguments() != null) {
            hostAccount = getArguments().getString("account");
            Log.d("넘어온 계정", hostAccount);
            parentActivity = getArguments().getString("parentActivity");
        }

        //검색창 클릭 리스너
        sv.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_hint.setVisibility(View.GONE);
            }
        });

        //검색 뷰 텍스트 입력 리스너
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("검색 제출", query);
                return true;
            }

            //텍스트가 입력될 때마다 호출된다.
            @Override
            public boolean onQueryTextChange(String newText) {
                //검색어가 없는 경우
                if (newText.length() == 0) {
                    Log.d("검색", "값 없음");
                    searchText = newText;
                    getFollower("getFollower", hostAccount, loginUser.getAccount(), false, searchText, 0);
                }
                //검색어가 존재하는 경우
                else {
                    Log.d("검색", newText);
                    searchText = newText;
                    getFollower("getFollower", hostAccount, loginUser.getAccount(), true, searchText, 0);
                }

                return true;
            }
        });

        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //부모 액티비티가 MyPageActivity인 경우
                if (parentActivity.equals("MyPageActivity")) {
                    if (MypageActivity.fragmentManager.getBackStackEntryCount() == 0) {
                        MypageActivity.fragmentManager.beginTransaction().remove(MypageActivity.followingListFragment).commit();
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙이고 그 전에 붙어있던 fragment는 destroy.
                        MypageActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);
                    } else {
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙이고 그 전에 붙어있던 fragment는 destroy.
                        MypageActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);

                    }
                }
                //부모 액티비티가 PostActivity인 경우
                else if (parentActivity.equals("PostActivity")) {
                    if (PostActivity.fragmentManager.getBackStackEntryCount() == 0) {
                        PostActivity.fragmentManager.beginTransaction().remove(PostActivity.followingListFragment).commit();
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙이고 그 전에 붙어있던 fragment는 destroy.
                        PostActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);
                    } else {
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙이고 그 전에 붙어있던 fragment는 destroy.
                        PostActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);

                    }
                }
                //부모 액티비티가 SearchActivity인 경우
                else if (parentActivity.equals("SearchActivity")) {
                    if (SearchActivity.fragmentManager.getBackStackEntryCount() == 0) {
                        SearchActivity.fragmentManager.beginTransaction().remove(SearchActivity.followingListFragment).commit();
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙이고 그 전에 붙어있던 fragment는 destroy.
                        SearchActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);
                    } else {
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙이고 그 전에 붙어있던 fragment는 destroy.
                        SearchActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);

                    }
                } else {
                    if (NotificationActivity.fragmentManager.getBackStackEntryCount() == 0) {
                        NotificationActivity.fragmentManager.beginTransaction().remove(NotificationActivity.followingListFragment).commit();
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙이고 그 전에 붙어있던 fragment는 destroy.
                        NotificationActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);
                    } else {
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙이고 그 전에 붙어있던 fragment는 destroy.
                        NotificationActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getContext(), EmptyActivity.class);
                        startActivity(intent);

                    }
                }

            }
        });

        //리사이클러뷰 셋팅
        setRecyclerView();


        return rootView;
    }

    public void setRecyclerView() {

        rv_follower.setHasFixedSize(true);

        followListItemArrayList = new ArrayList<>();

        //레이아웃 메니저 초기화 후 셋
        linearLayoutManager = new LinearLayoutManager(getContext());
        rv_follower.setLayoutManager(linearLayoutManager);

        //어댑터 초기화 후 셋
        followAdapter = new FollowAdapter(getContext(), followListItemArrayList);
        followAdapter.setOnClickListener(this);
        rv_follower.setAdapter(followAdapter);
    }


    private void getFollower(String requestType, String hostAccount, String myAccount, boolean isSearched, String searchText, int lastId) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("requestType", requestType);
            requestBody.put("hostAccount", hostAccount);
            requestBody.put("myAccount", myAccount);
            requestBody.put("isSearched", isSearched);
            requestBody.put("searchText", searchText);
            requestBody.put("lastId", lastId);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getfollower.php", this);
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
    public void onListClicked(int position) {

        if (followListItemArrayList.get(position).getAccount() != loginUser.getAccount()) {

            //부모 액티비티가 MyPageActivity인 경우
            if (parentActivity.equals("MyPageActivity")) {
                //사용자 계정 프래그먼트 객체 선언
                AccountPageFragment accountPageFragment = new AccountPageFragment();

                Fragment fragment = MypageActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

                //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                Bundle bundle = new Bundle();
                bundle.putString("account", followListItemArrayList.get(position).getAccount());
                bundle.putString("parentActivity", parentActivity);
                //좋아요 리스트의 계정이 로그인한 사용자의 계정과 같으면
                if (loginUser.getAccount().equals(followListItemArrayList.get(position).getAccount())) {
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

            }
            //부모 액티비티가 PostActivity인 경우
            else if (parentActivity.equals("PostActivity")) {
                //사용자 계정 프래그먼트 객체 선언
                AccountPageFragment accountPageFragment = new AccountPageFragment();

                Fragment fragment = PostActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

                //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                Bundle bundle = new Bundle();
                bundle.putString("account", followListItemArrayList.get(position).getAccount());
                bundle.putString("parentActivity", "PostActivity");
                //좋아요 리스트의 계정이 로그인한 사용자의 계정과 같으면
                if (loginUser.getAccount().equals(followListItemArrayList.get(position).getAccount())) {
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

            }
            //부모 액티비티가 SearchActivity인 경우
            else if (parentActivity.equals("SearchActivity")) {
                //사용자 계정 프래그먼트 객체 선언
                AccountPageFragment accountPageFragment = new AccountPageFragment();

                Fragment fragment = SearchActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

                //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                Bundle bundle = new Bundle();
                bundle.putString("account", followListItemArrayList.get(position).getAccount());
                bundle.putString("parentActivity", parentActivity);
                //좋아요 리스트의 계정이 로그인한 사용자의 계정과 같으면
                if (loginUser.getAccount().equals(followListItemArrayList.get(position).getAccount())) {
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

            } else {
                //사용자 계정 프래그먼트 객체 선언
                AccountPageFragment accountPageFragment = new AccountPageFragment();

                Fragment fragment = NotificationActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

                //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                Bundle bundle = new Bundle();
                bundle.putString("account", followListItemArrayList.get(position).getAccount());
                bundle.putString("parentActivity", parentActivity);
                //좋아요 리스트의 계정이 로그인한 사용자의 계정과 같으면
                if (loginUser.getAccount().equals(followListItemArrayList.get(position).getAccount())) {
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

            }

        }
    }//end of onListClicked


    @Override
    public void onFollowClicked(int position) {

        //해당 position의 사용자 account
        String followedAccount = followListItemArrayList.get(position).account;

        //해당 position의 사용자 followedNickname
        String followedNickname = followListItemArrayList.get(position).nickname;

        //팔로우를 하지 않은 상태에서 팔로우를 하려는 경우
        if (followListItemArrayList.get(position).isFollowing == false) {

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
            String image = followListItemArrayList.get(position).profile;


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
    }//end of onFollowClicked



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
                if (requestType.equals("getFollower")) {
                    followListItemArrayList.clear();
                    JSONArray jsonArray = responseBody.getJSONArray("followerlist");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        FollowListItem followListItem = new Gson().fromJson(data.toString(), FollowListItem.class);
                        followListItemArrayList.add(followListItem);
                    }

                    followAdapter.notifyDataSetChanged();
                    if (followListItemArrayList.size() != 0) {
                        currentLastId = followListItemArrayList.get(followListItemArrayList.size() - 1).id;
                    }
                }
                else if(requestType.equals("processFollow")) {
                    boolean isFollowing = responseBody.getBoolean("isFollowing");
                    int position = responseBody.getInt("position");
                    String nickname = responseBody.getString("followedNickname");
                    //팔로우를 한 경우
                    if (isFollowing) {
                        //팔로우 상태를 true로 만들고
                        followListItemArrayList.get(position).setFollowing(true);
                        //어댑터에 notify
                        followAdapter.notifyItemChanged(position, "true");

                        Toast.makeText(getContext(), nickname + "님을 팔로우 하셨습니다!", Toast.LENGTH_SHORT).show();


                        //팔로우를 당한 사용자 단말에 push알림을 보내준다.
                        String receiver = followListItemArrayList.get(position).account;
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
                        followListItemArrayList.get(position).setFollowing(false);
                        //어댑터에 notify
                        followAdapter.notifyItemChanged(position, "false");

                        Toast.makeText(getContext(), nickname + "님을 언팔로우 하셨습니다", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("팔로잉 리스트 onResume", "호출");
        //데이터 변경을 계속 반영해주기 위해서 프래그먼트가 화면에 나타나면 반드시 타게 되어있는 onResume단계에 데이터를 계속 새롭게 가져오는
        //비동기 클래스를 넣어준다.
        //검색어가 없는 경우에는 전체 데이터를 가져오고
        if (searchText == null) {
            //여기에 팔로잉 전체 데이터를 가져와서 셋해주는 코딩
            getFollower("getFollower", hostAccount, loginUser.getAccount(), false, searchText, 0);
        }
        //검색어가 입력되어있던 상태였으면 검색어에 맞는 데이터를 가져온다.
        else {
            //여기에 팔로잉 전체 데이터를 가져와서 셋해주는 코딩
            getFollower("getFollower", hostAccount, loginUser.getAccount(), true, searchText, 0);
        }

    }
}
