package com.example.sns.post.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sns.*;
import com.example.sns.comment.CommentActivity;
import com.example.sns.like.fragment.LikeListFragment;
import com.example.sns.login.model.LoginUser;
import com.example.sns.mypage.activity.MypageActivity;
import com.example.sns.notification.activity.NotificationActivity;
import com.example.sns.post.activity.PostActivity;
import com.example.sns.post.adapter.PostAdapter;
import com.example.sns.post.edit.activity.EditPostFirstActivity;
import com.example.sns.post.model.PostItem;
import com.example.sns.search.activity.SearchActivity;
import com.example.sns.search.fragment.SearchedPlaceListFragment;
import com.example.sns.util.HttpRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;



public class PostFragment extends Fragment implements PostAdapter.PostRecyclerViewListener, HttpRequest.OnHttpResponseListener {
    private String TAG = "PostFragment";
    //리사이클러뷰
    private RecyclerView rvPost;
    //리사이클러뷰 어댑터
    private PostAdapter postAdapter;
    //그리드뷰 레이아웃 매니저
    private LinearLayoutManager linearLayoutManager;

    //리사이클러뷰 아이템 arraylist
    private ArrayList<PostItem> postItemArrayList;

    //댓글 창으로 넘어가는 경우를 캐치하기 위한 boolean
    public boolean fromComment = false;

    private String hostAccount;//게시물의 주인 사용자 계정

    private String parentActivity;//부모 액티비티

    private boolean loadPossible = true;

    private LoginUser loginUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("onCreateView", "호출");

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_post, container, false);
        loginUser = LoginUser.getInstance();

        if (getArguments() != null) {
            hostAccount = getArguments().getString("account");
            parentActivity = getArguments().getString("parentActivity");
        }

        //리사이클러뷰 선언
        rvPost = rootView.findViewById(R.id.recyclerview_post);
        setRecyclerView();

        //addOnScrollListener는 리사이클러뷰를 스크롤하는 순간을 캐치한다.
        rvPost.addOnScrollListener(new RecyclerView.OnScrollListener() {
            //리사이클러뷰가 스크롤된 후 콜백되는 메소드
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //findLastVisibleItemPosition은 현재 화면에 보이는 뷰 중 가장 마지막 뷰의 position을 리턴해준다.
                int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                //즉 이 변수는 현재 화면에 보이는 아이템 중 가장 마지막 아이템의 index를 담는다
                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                Log.d("현재 화면의 마지막 index", String.valueOf(lastVisibleItemPosition));
                //이 변수는 전체 리사이클러뷰의 아이템 개수를 담는다.
                int totalCount = recyclerView.getAdapter().getItemCount();
                Log.d("전체 아이템 개수", String.valueOf(totalCount));
                //이 변수는 현재 화면에 보이는 아이템의 개수를 담는다.(내 경우에는 1~2를 왔다갔다 함)
                int visibleItemCount = recyclerView.getChildCount();
                Log.d("화면에 보여지는 아이템 개수", String.valueOf(visibleItemCount));

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
                    if (postItemArrayList.get(firstVisibleItemPosition).getType().equals("video") && !postItemArrayList.get(firstVisibleItemPosition).getIsPlaying()) {
                        //첫번재로 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                        if (firstItemPercentage >= 50 && lastItemPercentage < 50) {
                            postAdapter.notifyItemChanged(firstVisibleItemPosition, "playVideo");//처음으로 보이는 아이템 play
                            postItemArrayList.get(firstVisibleItemPosition).setIsPlaying(true);
                        }
                    }

                    //마지막으로 보이는 아이템이 동영상 게시물이고 재생중이지 않은 경우
                    if (postItemArrayList.get(lastVisibleItemPosition).getType().equals("video") && !postItemArrayList.get(lastVisibleItemPosition).getIsPlaying()) {
                        //첫번재로 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                        if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                            postAdapter.notifyItemChanged(lastVisibleItemPosition, "playVideo");//처음으로 보이는 아이템 play
                            postItemArrayList.get(lastVisibleItemPosition).setIsPlaying(true);
                        }
                    }

                    //첫번째로 보이는 아이템이 동영상 게시물이고 현재 재생중인 경우
                    if (postItemArrayList.get(firstVisibleItemPosition).getType().equals("video") && postItemArrayList.get(firstVisibleItemPosition).getIsPlaying()) {
                        //마지막에 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                        if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                            //마지막으로 보이는 아이템이 동영상 게시물인 경우
                            if (postItemArrayList.get(lastVisibleItemPosition).getType().equals("video")) {
                                postAdapter.notifyItemChanged(lastVisibleItemPosition, "playVideo");//마지막으로 보이는 아이템을 play
                                postItemArrayList.get(firstVisibleItemPosition).setIsPlaying(false);//첫번째 아이템의 플레이상태를 false
                                postItemArrayList.get(lastVisibleItemPosition).setIsPlaying(true);//마지막 아이템의 플레이상태를 true
                            }
                            //마지막으로 보이는 아이템이 이미지 게시물인 경우
                            else {
                                //재생중인 동영상을 release 해준다.
                                postItemArrayList.get(firstVisibleItemPosition).setIsPlaying(false);
                                postAdapter.notifyItemChanged(firstVisibleItemPosition, "releaseVideo");
                            }
                        }
                    }

                    //마지막으로 보이는 아이템이 동영상 게시물이고 현재 재생중인 경우
                    if (postItemArrayList.get(lastVisibleItemPosition).getType().equals("video") && postItemArrayList.get(lastVisibleItemPosition).getIsPlaying()) {
                        //첫번째로 보이는 아이템이 화면에 보이는 비중이 50%를 넘어가는 경우
                        if (lastItemPercentage < 50 && firstItemPercentage >= 50) {
                            //첫번째로 보이는 아이템이 동영상 게시물인 경우
                            if (postItemArrayList.get(firstVisibleItemPosition).getType().equals("video")) {
                                postAdapter.notifyItemChanged(firstVisibleItemPosition, "playVideo");//첫번째로 보이는 아이템을 play
                                postItemArrayList.get(lastVisibleItemPosition).setIsPlaying(false);//마지막 아이템의 플레이상태를 false
                                postItemArrayList.get(firstVisibleItemPosition).setIsPlaying(true);//첫번째 아이템의 플레이상태를 true
                            }
                            //첫번째로 보이는 아이템이 이미지 게시물인 경우
                            else {
                                //재생중인 동영상을 release해준다.
                                postItemArrayList.get(lastVisibleItemPosition).setIsPlaying(false);
                                postAdapter.notifyItemChanged(lastVisibleItemPosition, "releaseVideo");
                            }
                        }
                    }

                    //첫번째로 보이는 아이템이 이미지 게시물이고 마지막으로 보이는 아이템이 동영상이고 그 동영상이 재생중인 경우
                    if (postItemArrayList.get(firstVisibleItemPosition).getType().equals("image") && postItemArrayList.get(lastVisibleItemPosition).getType().equals("video") && postItemArrayList.get(lastVisibleItemPosition).getIsPlaying()) {
                        //동영상 게시물이 화면에 보이는 비중이 더 낮아지는 경우
                        if (firstItemPercentage >= 50 && lastItemPercentage < 50) {
                            postAdapter.notifyItemChanged(lastVisibleItemPosition, "releaseVideo");//동영상 release
                            postItemArrayList.get(lastVisibleItemPosition).setIsPlaying(false);//플레이 상태 false
                        }
                    }

                    //첫번째로 보이는 아이템이 이미지 게시물이고 마지막으로 보이는 아이템이 동영상이고 그 동영상이 재생중이지 않은 경우
                    if (postItemArrayList.get(firstVisibleItemPosition).getType().equals("image") && postItemArrayList.get(lastVisibleItemPosition).getType().equals("video") && !postItemArrayList.get(lastVisibleItemPosition).getIsPlaying()) {
                        //동영상 게시물이 화면에 보이는 비중이 더 높아지는 경우
                        if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                            postAdapter.notifyItemChanged(lastVisibleItemPosition, "playVideo");//동영상 play
                            postItemArrayList.get(lastVisibleItemPosition).setIsPlaying(true);//플레이 상태 true
                        }
                    }

                    //마지막으로 보이는 아이템이 이미지 게시물이고 첫번째로 보이는 아이템이 동영상이고 그 동영상이 재생중인 경우
                    if (postItemArrayList.get(lastVisibleItemPosition).getType().equals("image") && postItemArrayList.get(firstVisibleItemPosition).getType().equals("video") && postItemArrayList.get(firstVisibleItemPosition).getIsPlaying()) {
                        //동영상 게시물이 화면에 보이는 비중이 더 낮아지는 경우
                        if (firstItemPercentage < 50 && lastItemPercentage >= 50) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    postAdapter.notifyItemChanged(firstVisibleItemPosition, "releaseVideo");//동영상 release
                                    postItemArrayList.get(firstVisibleItemPosition).setIsPlaying(false);//플레이 상태 false

                                }
                            }).start();
                        }
                    }

                    //마지막으로 보이는 아이템이 이미지 게시물이고 첫번째로 보이는 아이템이 동영상이고 그 동영상이 재생중이지 않은 경우
                    if (postItemArrayList.get(lastVisibleItemPosition).getType().equals("image") && postItemArrayList.get(firstVisibleItemPosition).getType().equals("video") && !postItemArrayList.get(firstVisibleItemPosition).getIsPlaying()) {
                        //동영상 게시물이 화면에 보이는 비중이 더 높아지는 경우
                        if (firstItemPercentage >= 50 && lastItemPercentage < 50) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    postAdapter.notifyItemChanged(firstVisibleItemPosition, "playVideo");//동영상 play
                                    postItemArrayList.get(firstVisibleItemPosition).setIsPlaying(true);//플레이 상태 true
                                }
                            }).start();
                        }
                    }
                }

                //화면에 보이는 마지막 아이템의 index가 마지막 아이템의 인덱스와 같고, 현재 화면에 보이는 아이템의 개수가 1인 경우
                //다음 게시물 10개를 가져오는 메소드를 실행한다.
                //현재 화면에 보이는 아이템의 개수가 1인 경우를 조건에 추가하지 않으면 스크롤을 할때마다 화면에 보이는 마지막 아이템의 인덱스와
                //마지막 아이템의 인덱스가 계속 같기 때문에 해당 조건문을 계속 타버리는 문제가 생긴다.
                if ((lastVisibleItemPosition == totalCount - 1) && postItemArrayList.size() >= 10 && loadPossible == true) {
                    Log.d("페이징 조건", "부합");
                    //다음 페이지를 로드한다.
                    //param:현재 로드되어있는 데이터의 수(다음 페이지에 로드되어야 할 첫번째 게시물의 index)
                    loadNextPage(loginUser.getAccount(), hostAccount, postItemArrayList.get(postItemArrayList.size() - 1).getPostNum(), 10);
                }


            }
        });


        return rootView;
    }

    private void setRecyclerView() {

        //아이템 arraylist를 초기화해준다.
        postItemArrayList = new ArrayList<>();

        rvPost.setHasFixedSize(true);


        //레이아웃 메니저 설정
        linearLayoutManager = new LinearLayoutManager(getContext());
        rvPost.setLayoutManager(linearLayoutManager);

        //어댑터 설정
        postAdapter = new PostAdapter(getContext(), postItemArrayList, getFragmentManager());
        postAdapter.setOnClickListener(this);
        rvPost.setAdapter(postAdapter);
    }


    private void getPost(String myAccount, String hostAccount, int lastId, int listSize) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("requestType", "getPost");
            requestBody.put("myAccount", myAccount);//자신의 계정
            requestBody.put("hostAccount", hostAccount);//페이지의 주인 계정
            requestBody.put("lastId", lastId);
            requestBody.put("listSize", listSize);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "gettargetpost.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //게시물 리스트의 최하단으로 이동하면 페이징 처리를 위해서 실행될 메소드
    public void loadNextPage(String account, String hostAccount, int lastId, int listSize) {
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
                    requestBody.put("myAccount", account);//자신의 계정
                    requestBody.put("hostAccount", hostAccount);//페이지의 주인 계정
                    requestBody.put("lastId", lastId);
                    requestBody.put("listSize", listSize);
                    HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "gettargetpost.php", PostFragment.this);
                    httpRequest.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 1500);
    }

    //비동기 네트워크 통신을 통해서 게시물을 삭제하는 메소드
    private void deletePost(int postNum, int position) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("postNum", postNum);
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
            requestBody.put("account", loginUser.getAccount());
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
            requestBody.put("account", postItemArrayList.get(position).getAccount());//알림의 대상이 되는 사람
            requestBody.put("title", "SNS");//알림의 제목
            requestBody.put("body", loginUser.getNickname() + "님이 회원님의 게시물에 좋아요를 눌렀습니다.");//알림의 내용
            requestBody.put("click_action", "PostDetailFragment");//푸시알림을 눌렀을 때 이동할 액티비티 혹은 프래그먼트
            requestBody.put("category", "like");//알림의 카테고리
            requestBody.put("userAccount", loginUser.getAccount());//좋아요를 누른 사람
            requestBody.put("postNum", postItemArrayList.get(position).getPostNum());
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
                if (requestType.equals("getPost")) {//게시물을 가져오기 위한 통신
                    postItemArrayList.clear();
                    JSONArray jsonArray = responseBody.getJSONArray("post");

                    //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                    for (int i = 0; i < jsonArray.length(); i++) {
                        //서버로부터 넘어온 데이터를 변수에 정의
                        JSONObject data = jsonArray.getJSONObject(i);
                        PostItem postItem = new Gson().fromJson(data.toString(), PostItem.class);

                        //모든 데이터를 다 담았으면 이제 그 객체를 리사이클러뷰 어레이리스트에 추가해준다.
                        postItemArrayList.add(postItem);
                        //arraylist가 모두 추가됐으면 어댑터 notify
                        postAdapter.notifyDataSetChanged();
                    }


                } else if (requestType.equals("loadNextPage")) { //페이징 처리를 위한 통신
                    boolean isSubstituted = true;
                    JSONArray jsonArray = responseBody.getJSONArray("post");
                    Log.d("게시물 수", String.valueOf(jsonArray.length()));
                    if (jsonArray.length() != 0) {
                        //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                        for (int i = 0; i < jsonArray.length(); i++) {
                            //json에 있는 값을 아이템에 set
                            JSONObject data = jsonArray.getJSONObject(i);
                            PostItem postItem = new Gson().fromJson(data.toString(), PostItem.class);
                            //로딩바를 다음 페이지의 첫번째 게시물로 교체하기 위한 작업
                            if (isSubstituted) {
                                //프로그래스바 아이템을 게시물 아이템으로 교체해준다.
                                postItemArrayList.set(postItemArrayList.size() - 1, postItem);


                                //어댑터 notify
                                postAdapter.notifyItemChanged(postItemArrayList.size() - 1);
                                Log.d("게시물 사이즈:", String.valueOf(postItemArrayList.size()));

                                isSubstituted = false;
                            }
                            //최초에 로딩바와 게시물이 교체가 끝났으면 그 뒤로는 게시물 추가만 해준다.
                            else {
                                postItemArrayList.add(postItem);
                                postAdapter.notifyItemInserted(postItemArrayList.size() - 1);
                            }
                        }
                    } else {//더 이상 로드할 게시물이 없는 경우 프로그래스 바만 지워준다.
                        postItemArrayList.remove(postItemArrayList.size() - 1);
                        postAdapter.notifyItemRemoved(postItemArrayList.size());
                    }
                    loadPossible = true;//다시 페이징 시도가 가능한 상태로 전환

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
                    if (!loginUser.getAccount().equals(postItemArrayList.get(position).getAccount())) {
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onProfileClicked(int position) {

    }

    @Override
    public void onNicknameClicked(int position) {

    }

    @Override
    public void onMoreClicked(int position) {
        Dialog dialog = new Dialog(getActivity());
        //더 보기를 누른 게시물이 나의 게시물인 경우
        if (postItemArrayList.get(position).getIsMyPost()) {
            dialog.setContentView(R.layout.mypost_more_box_inmyfragment);
            TextView tv_editpost, tv_deletepost;
            //게시물 수정
            tv_editpost = dialog.findViewById(R.id.textview_editpost);
            //게시물 삭제
            tv_deletepost = dialog.findViewById(R.id.textview_deletepost);

            tv_editpost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), EditPostFirstActivity.class);
                    //게시물 번호를 넘겨준다.
                    intent.putExtra("postNum", postItemArrayList.get(position).getPostNum());

                    //게시글이 존재하면 인텐트로 넘겨준다.
                    if (!postItemArrayList.get(position).getArticle().equals("")) {
                        intent.putExtra("article", postItemArrayList.get(position).getArticle());
                        Log.d("게시글 존재:", "yes");
                    }

                    //주소가 존재하면 인텐트로 넘겨준다.
                    if (!postItemArrayList.get(position).getAddress().equals("")) {
                        Log.d("주소 존재:", "yes");
                        intent.putExtra("address", postItemArrayList.get(position).getAddress());
                        intent.putExtra("latitude", postItemArrayList.get(position).getLatitude());
                        intent.putExtra("longitude", postItemArrayList.get(position).getLongitude());
                    }

                    //이미지의 개수를 인텐트로 넘겨준다.
                    intent.putExtra("imageCount", postItemArrayList.get(position).getImageList().size());
                    //게시물의 이미지 파일명을 이미지의 개수만큼 인텐트로 넘겨준다.
                    for (int i = 0; i < postItemArrayList.get(position).getImageList().size(); i++) {
                        intent.putExtra("image" + (i + 1), "http://13.124.105.47/uploadimage/" + postItemArrayList.get(position).getImageList().get(i));
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
                    Dialog deleteDialog = new Dialog(getActivity());
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
                            //게시물을 삭제하는 스레드 실행
                            deletePost(postItemArrayList.get(position).getPostNum(), position);//게시물 삭제 메소드
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
        }
        dialog.show();

    }

    @Override
    public void onLikeClicked(int position) {

        int postNum = postItemArrayList.get(position).getPostNum();


        //좋아요를 누르지 않은 상태인 경우(좋아요를 하려는 경우)
        if (postItemArrayList.get(position).getIsLike() == false) {
            //좋아요 상태를 true로 전환
            processLike(true, postNum, position);
        }
        //좋아요를 누른 상태인 경우(좋아요를 취소하려는 경우)
        else {
            processLike(false, postNum, position);
        }
    }

    @Override
    public void onLikeCountClicked(int position) {

        if (parentActivity.equals("PostActivity")) {

            LikeListFragment likeListFragment = new LikeListFragment();

            //스택에 현재 프래그먼트를 넣어준다.
            Fragment fragment = PostActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

            //게시물 번호를 번들에 담아서 이동할 프래그먼트에 전달해준다.
            Bundle bundle = new Bundle();
            bundle.putInt("postNum", postItemArrayList.get(position).getPostNum());
            bundle.putString("parentActivity", parentActivity);
            likeListFragment.setArguments(bundle);

            //프래그먼트를 추가해서 스택에 추가한 후
            PostActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, likeListFragment).addToBackStack(null).commit();
            //화면에 보여준다.
            PostActivity.fragmentManager.beginTransaction().show(likeListFragment).commit();
        } else if (parentActivity.equals("SearchActivity")) {

            LikeListFragment likeListFragment = new LikeListFragment();
            //스택에 현재 프래그먼트를 넣어준다.
            Fragment fragment = SearchActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

            //게시물 번호를 번들에 담아서 이동할 프래그먼트에 전달해준다.
            Bundle bundle = new Bundle();
            bundle.putInt("postNum", postItemArrayList.get(position).getPostNum());
            bundle.putString("parentActivity", parentActivity);
            likeListFragment.setArguments(bundle);

            //프래그먼트를 추가해서 스택에 추가한 후
            SearchActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, likeListFragment).addToBackStack(null).commit();
            //화면에 보여준다.
            SearchActivity.fragmentManager.beginTransaction().show(likeListFragment).commit();
        } else if (parentActivity.equals("NotificationActivity")) {

            LikeListFragment likeListFragment = new LikeListFragment();

            //스택에 현재 프래그먼트를 넣어준다.
            Fragment fragment = NotificationActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

            //게시물 번호를 번들에 담아서 이동할 프래그먼트에 전달해준다.
            Bundle bundle = new Bundle();
            bundle.putInt("postNum", postItemArrayList.get(position).getPostNum());
            bundle.putString("parentActivity", parentActivity);
            likeListFragment.setArguments(bundle);

            //프래그먼트를 추가해서 스택에 추가한 후
            NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, likeListFragment).addToBackStack(null).commit();
            //화면에 보여준다.
            NotificationActivity.fragmentManager.beginTransaction().show(likeListFragment).commit();
        } else {
            LikeListFragment likeListFragment = new LikeListFragment();

            //스택에 현재 프래그먼트를 넣어준다.
            Fragment fragment = MypageActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

            //게시물 번호를 번들에 담아서 이동할 프래그먼트에 전달해준다.
            Bundle bundle = new Bundle();
            bundle.putInt("postNum", postItemArrayList.get(position).getPostNum());
            bundle.putString("parentActivity", parentActivity);
            likeListFragment.setArguments(bundle);

            //프래그먼트를 추가해서 스택에 추가한 후
            MypageActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, likeListFragment).addToBackStack(null).commit();
            //화면에 보여준다.
            MypageActivity.fragmentManager.beginTransaction().show(likeListFragment).commit();
        }
    }//end of onLikeCountClicked

    @Override
    public void onCommentClick(int position) {
        fromComment = true;
        Intent intent = new Intent(getActivity(), CommentActivity.class);
        //해당 게시물과 게시물 작성자 데이터를 댓글 화면에서 보여주기 위해서 인텐트로 데이터들을 넘겨준다.
        intent.putExtra("postNum", postItemArrayList.get(position).getPostNum());
        intent.putExtra("postProfile", postItemArrayList.get(position).getProfile());
        intent.putExtra("postAccount", postItemArrayList.get(position).getAccount());
        intent.putExtra("postNickname", postItemArrayList.get(position).getNickname());
        intent.putExtra("postArticle", postItemArrayList.get(position).getArticle());
        intent.putExtra("postTime", postItemArrayList.get(position).getTime());
        startActivity(intent);
    }

    @Override
    public void onCommentCountClicked(int position) {
        fromComment = true;
        Intent intent = new Intent(getActivity(), CommentActivity.class);
        //해당 게시물과 게시물 작성자 데이터를 댓글 화면에서 보여주기 위해서 인텐트로 데이터들을 넘겨준다.
        intent.putExtra("postNum", postItemArrayList.get(position).getPostNum());
        intent.putExtra("postProfile", postItemArrayList.get(position).getProfile());
        intent.putExtra("postAccount", postItemArrayList.get(position).getAccount());
        intent.putExtra("postNickname", postItemArrayList.get(position).getNickname());
        intent.putExtra("postArticle", postItemArrayList.get(position).getArticle());
        intent.putExtra("postTime", postItemArrayList.get(position).getTime());
        startActivity(intent);
    }

    @Override
    public void onHashTagClicked(int position, String hashTag) {
        //부모 액티비티가 PostActivity일 경우
        if (parentActivity.equals("PostActivity")) {
            //해시태그 리스트 프래그먼트 객체 선언
            HashTagPostListFragment hashTagPostListFragment = new HashTagPostListFragment();

            //현재 프래그먼트를 스택에 넣어준다.
            Fragment fragment = PostActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

            Log.d("해시태그", hashTag);
            Bundle bundle = new Bundle();
            bundle.putString("hashTag", hashTag);
            bundle.putString("parentActivity", parentActivity);
            hashTagPostListFragment.setArguments(bundle);

            PostActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, hashTagPostListFragment).addToBackStack(null).commit();
            PostActivity.fragmentManager.beginTransaction().show(hashTagPostListFragment).commit();
        }
        //부모 액티비티가 SearchActivity인 경우
        else if (parentActivity.equals("SearchActivity")) {
            //해시태그 리스트 프래그먼트 객체 선언
            HashTagPostListFragment hashTagPostListFragment = new HashTagPostListFragment();

            //현재 프래그먼트를 스택에 넣어준다.
            Fragment fragment = SearchActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

            Log.d("해시태그", hashTag);
            Bundle bundle = new Bundle();
            bundle.putString("hashTag", hashTag);
            bundle.putString("parentActivity", parentActivity);
            hashTagPostListFragment.setArguments(bundle);

            SearchActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, hashTagPostListFragment).addToBackStack(null).commit();
            SearchActivity.fragmentManager.beginTransaction().show(hashTagPostListFragment).commit();
        } else if (parentActivity.equals("NotificationActivity")) {
            //해시태그 리스트 프래그먼트 객체 선언
            HashTagPostListFragment hashTagPostListFragment = new HashTagPostListFragment();

            //현재 프래그먼트를 스택에 넣어준다.
            Fragment fragment = NotificationActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

            Log.d("해시태그", hashTag);
            Bundle bundle = new Bundle();
            bundle.putString("hashTag", hashTag);
            bundle.putString("parentActivity", parentActivity);
            hashTagPostListFragment.setArguments(bundle);

            NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, hashTagPostListFragment).addToBackStack(null).commit();
            NotificationActivity.fragmentManager.beginTransaction().show(hashTagPostListFragment).commit();
        } else {
            //해시태그 리스트 프래그먼트 객체 선언
            HashTagPostListFragment hashTagPostListFragment = new HashTagPostListFragment();

            //현재 프래그먼트를 스택에 넣어준다.
            Fragment fragment = MypageActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

            Log.d("해시태그", hashTag);
            Bundle bundle = new Bundle();
            bundle.putString("hashTag", hashTag);
            bundle.putString("parentActivity", parentActivity);
            hashTagPostListFragment.setArguments(bundle);

            MypageActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, hashTagPostListFragment).addToBackStack(null).commit();
            MypageActivity.fragmentManager.beginTransaction().show(hashTagPostListFragment).commit();
        }
    }//end of onHashTagClicked

    @Override
    public void onPlaceClicked(int position) {
        //SearchedPlaceListFragment를 번들과 함께 만들어준다.
        SearchedPlaceListFragment searchedPlaceListFragment = new SearchedPlaceListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("address", postItemArrayList.get(position).getAddress());
        bundle.putDouble("latitude", Double.parseDouble(postItemArrayList.get(position).getLatitude()));
        bundle.putDouble("longitude", Double.parseDouble(postItemArrayList.get(position).getLongitude()));
        bundle.putString("parentActivity", parentActivity);
        searchedPlaceListFragment.setArguments(bundle);

        //현재 프래그먼트의 부모 액티비티가 관리하는 프래그먼트 매니저에 프래그먼트를 추가하고 보여준다.
        if (parentActivity.equals("PostActivity")) {
            //해시태그 리스트 프래그먼트로 바꿔준다.
            PostActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, searchedPlaceListFragment).addToBackStack(null).commit();
            PostActivity.fragmentManager.beginTransaction().show(searchedPlaceListFragment).commit();
        } else if (parentActivity.equals("SearchActivity")) {
            //해시태그 리스트 프래그먼트로 바꿔준다.
            SearchActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, searchedPlaceListFragment).addToBackStack(null).commit();
            SearchActivity.fragmentManager.beginTransaction().show(searchedPlaceListFragment).commit();
        } else if (parentActivity.equals("NotificationActivity")) {
            //해시태그 리스트 프래그먼트로 바꿔준다.
            NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, searchedPlaceListFragment).addToBackStack(null).commit();
            NotificationActivity.fragmentManager.beginTransaction().show(searchedPlaceListFragment).commit();
        } else {
            //해시태그 리스트 프래그먼트로 바꿔준다.
            MypageActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, searchedPlaceListFragment).addToBackStack(null).commit();
            MypageActivity.fragmentManager.beginTransaction().show(searchedPlaceListFragment).commit();
        }
    }//end of onPlaceClicked


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume 호출");

        int listSize = 10;
        if (!postItemArrayList.isEmpty()) {//게시물이 처음 로드되어서 하나도 없는 경우
            listSize = postItemArrayList.size();
        }
        getPost(loginUser.getAccount(), hostAccount, 0, listSize);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onResume 호출");
        postAdapter.releasevideo();//현재 재생중인 동영상 release
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.d(TAG, "onHiddenChanged 호출");
        super.onHiddenChanged(hidden);
        if (!hidden) {//화면에서 사라지는 경우에 새롭게 데이터를 가져와서 화면에 나타날때 바로 데이터가 보일 수 있게 해준다.
            int listSize = 10;
            if (!postItemArrayList.isEmpty()) {//게시물이 처음 로드되어서 하나도 없는 경우
                listSize = postItemArrayList.size();
            }
            getPost(loginUser.getAccount(), hostAccount, 0, listSize);
        }

        if(hidden) {//화면에서 가려질 때 동영상을 release.
            postAdapter.releasevideo();
        }
    }
}
