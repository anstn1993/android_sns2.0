package com.example.sns.post.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sns.*;
import com.example.sns.comment.CommentActivity;
import com.example.sns.like.fragment.LikeListFragment;
import com.example.sns.login.model.LoginUser;
import com.example.sns.mypage.activity.MypageActivity;
import com.example.sns.notification.activity.NotificationActivity;
import com.example.sns.post.activity.PostActivity;
import com.example.sns.post.edit.activity.EditPostFirstActivity;
import com.example.sns.search.activity.SearchActivity;
import com.example.sns.search.fragment.SearchedPlaceListFragment;
import com.example.sns.util.EmptyActivity;
import com.example.sns.util.HttpRequest;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

class PostData {
    //데이터 변수들
    String type;
    String account;
    String nickname;
    String profile;
    String article;
    String time;
    String address;
    String latitude;
    String longitude;
    Boolean isMyPost;
    Boolean isLike;
    int commentCount;
    int likeCount;
    //이미지 경로를 담을 arraylist;
    ArrayList<String> imageList = new ArrayList<>();
    String video;//서버에 저장된 비디오 파일 명
}


public class PostDetailFragment extends Fragment implements HttpRequest.OnHttpResponseListener {
    private String TAG = PostDetailFragment.class.getSimpleName();
    private CircleImageView cv_profile;
    private TextView tv_nickname, tv_place, tv_article, tv_likeCount, tv_commentCount, tv_uploadTime, tv_readmore;
    private ImageButton ib_more, ib_comment, ib_like, ib_back, ib_mute;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private PlayerView playerView;
    private SimpleExoPlayer player;

    //게시물 번호
    private int postNum;
    //부모 액티비티
    private String parentActivity;

    //프래그먼트 매니저
    private FragmentManager fragmentManager;

    //해시태그 라이브러리
    private HashTagHelper hashTagHelper;

    //댓글(대댓글) 알림으로 인해서 진입한 프래그먼트인지를 가려내는 boolean
    private boolean isCommentNotification = false;

    private PostData postData = new PostData();

    private LoginUser loginUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView 호출");
        //프레그먼트 레이아웃 설정
        View rootView = (ViewGroup) inflater.inflate(R.layout.fragment_postdetail, container, false);
        loginUser = LoginUser.getInstance();
        //번들로 넘어온 데이터를 받는다.
        if (getArguments() != null) {
            //번들로 넘어온 게시물 번호를 설정
            postNum = getArguments().getInt("postNum");
            parentActivity = getArguments().getString("parentActivity");
            isCommentNotification = getArguments().getBoolean("isCommentNotification", false);
            Log.d("게시물 번호", String.valueOf(postNum));
            Log.d("부모 액티비티", parentActivity);
        }


        //프래그먼트 매니저 설정
        fragmentManager = getChildFragmentManager();

        //뒤로가기 버튼
        ib_back = rootView.findViewById(R.id.imagebutton_back);

        //프로필 사진
        cv_profile = rootView.findViewById(R.id.circleImageView);
        //닉네임
        tv_nickname = rootView.findViewById(R.id.textview_nickname);
        //게시글
        tv_article = rootView.findViewById(R.id.textview_article);
        //장소
        tv_place = rootView.findViewById(R.id.textview_place);
        //좋아요 개수
        tv_likeCount = rootView.findViewById(R.id.textview_like_count);
        //댓글 개수
        tv_commentCount = rootView.findViewById(R.id.textview_comment_count);
        //업로드 시간
        tv_uploadTime = rootView.findViewById(R.id.textview_upload_time);
        //더 보기
        tv_readmore = rootView.findViewById(R.id.textview_readmore);
        //설정 버튼
        ib_more = rootView.findViewById(R.id.imagebutton_more);
        //댓글 버튼
        ib_comment = rootView.findViewById(R.id.imagebutton_comment);
        //좋아요 버튼
        ib_like = rootView.findViewById(R.id.imagebutton_like);
        //뮤트 버튼
        ib_mute = rootView.findViewById(R.id.imagebutton_mute);
        //이미지 뷰 페이저
        viewPager = rootView.findViewById(R.id.viewpager);
        //이미지 네비게이트 탭
        tabLayout = rootView.findViewById(R.id.tab_layout);
        //비디오 뷰
        playerView = rootView.findViewById(R.id.exoplayerview);

        //새로고침
        swipeRefreshLayout = rootView.findViewById(R.id.refresh_layout);


        //새로고침 리스너
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //데이터를 새로 가져와서 화면에 뿌려준다
                getData();

                swipeRefreshLayout.setRefreshing(false);

            }
        });


        //뒤로가기 버튼 클릭 리스너
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (parentActivity.equals("PostActivity")) {
                    if (PostActivity.fragmentManager.getBackStackEntryCount() == 0) {
                        PostActivity.fragmentManager.beginTransaction().remove(PostActivity.postDetailFragment).commit();
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
                        SearchActivity.fragmentManager.beginTransaction().remove(SearchActivity.postDetailFragment).commit();
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
                        NotificationActivity.fragmentManager.beginTransaction().remove(NotificationActivity.postDetailFragment).commit();
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
                        MypageActivity.fragmentManager.beginTransaction().remove(MypageActivity.postDetailFragment).commit();
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

        //좋아요 버튼 클릭 리스너
        ib_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //좋아요가 아닌 상태에서 누르는 경우
                if (postData.isLike == false) {
                    //좋아요 상태를 true로 전환
                    processLike(true, postNum, 0);
                }
                //좋아요를 누른 상태에서 취소하는 경우
                else {
                    processLike(false, postNum, 0);
                }
            }
        });


        //설정 버튼 클릭 리스너
        ib_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(getContext());

                dialog.setContentView(R.layout.mypost_more_box_inmyfragment);
                TextView tv_profile, tv_editpost, tv_deletepost;

                //게시물 수정
                tv_editpost = dialog.findViewById(R.id.textview_editpost);
                //게시물 삭제
                tv_deletepost = dialog.findViewById(R.id.textview_deletepost);

                tv_editpost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(getContext(), EditPostFirstActivity.class);
                        //게시물 번호를 넘겨준다.
                        intent.putExtra("postNum", postNum);

                        //게시글이 존재하면 인텐트로 넘겨준다.
                        if (!postData.article.equals("")) {
                            intent.putExtra("article", postData.article);
                            Log.d("게시글 존재:", "yes");
                        }

                        //주소가 존재하면 인텐트로 넘겨준다.
                        if (!postData.address.equals("")) {
                            Log.d("주소 존재:", "yes");
                            intent.putExtra("address", postData.address);
                            intent.putExtra("latitude", postData.latitude);
                            intent.putExtra("longitude", postData.longitude);
                        }

                        //이미지의 개수를 인텐트로 넘겨준다.
                        intent.putExtra("imageCount", postData.imageList.size());
                        //게시물의 이미지 파일명을 이미지의 개수만큼 인텐트로 넘겨준다.
                        for (int i = 0; i < postData.imageList.size(); i++) {
                            intent.putExtra("image" + (i + 1), "http://13.124.105.47/uploadimage/" + postData.imageList.get(i));
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
                        Dialog deleteDialog = new Dialog(getContext());
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
                                deletePost(postNum);
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


                dialog.show();

            }
        });

        //좋아요 개수 클릭 리스너
        tv_likeCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //부모 액티비티가 PostActivity인 경우
                if (parentActivity.equals("PostActivity")) {
                    //좋아요 리스트 프래그먼트 객체 선언
                    LikeListFragment likeListFragment = new LikeListFragment();
                    Fragment fragment = PostActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

                    //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                    Bundle bundle = new Bundle();
                    bundle.putInt("postNum", postNum);
                    bundle.putString("parentActivity", parentActivity);
                    likeListFragment.setArguments(bundle);

                    PostActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).replace(R.id.frame_parent_container, likeListFragment).addToBackStack(null).commit();
                }
                //부모 액티비티가 SearchActivity인 경우
                else if (parentActivity.equals("SearchActivity")) {
                    //좋아요 리스트 프래그먼트 객체 선언
                    LikeListFragment likeListFragment = new LikeListFragment();
                    Fragment fragment = SearchActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

                    //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                    Bundle bundle = new Bundle();
                    bundle.putInt("postNum", postNum);
                    bundle.putString("parentActivity", parentActivity);
                    likeListFragment.setArguments(bundle);

                    SearchActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).replace(R.id.frame_parent_container, likeListFragment).addToBackStack(null).commit();
                } else if (parentActivity.equals("NotificationActivity")) {
                    //좋아요 리스트 프래그먼트 객체 선언
                    LikeListFragment likeListFragment = new LikeListFragment();
                    Fragment fragment = NotificationActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

                    //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                    Bundle bundle = new Bundle();
                    bundle.putInt("postNum", postNum);
                    bundle.putString("parentActivity", parentActivity);
                    likeListFragment.setArguments(bundle);

                    NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).replace(R.id.frame_parent_container, likeListFragment).addToBackStack(null).commit();
                } else {
                    //좋아요 리스트 프래그먼트 객체 선언
                    LikeListFragment likeListFragment = new LikeListFragment();
                    Fragment fragment = MypageActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

                    //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                    Bundle bundle = new Bundle();
                    bundle.putInt("postNum", postNum);
                    bundle.putString("parentActivity", parentActivity);
                    likeListFragment.setArguments(bundle);

                    MypageActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).replace(R.id.frame_parent_container, likeListFragment).addToBackStack(null).commit();
                }


            }
        });

        //장소 클릭 리스너
        tv_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //부모 액티비티가 PostActivity인 경우
                if (parentActivity.equals("PostActivity")) {
                    releasePlayer();//동영상 release

                    SearchedPlaceListFragment searchedPlaceListFragment = new SearchedPlaceListFragment();
                    Fragment fragment = PostActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

                    //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                    Bundle bundle = new Bundle();
                    bundle.putString("address", postData.address);
                    bundle.putDouble("latitude", Double.parseDouble(postData.latitude));
                    bundle.putDouble("longitude", Double.parseDouble(postData.longitude));
                    bundle.putString("parentActivity", parentActivity);
                    searchedPlaceListFragment.setArguments(bundle);


                    PostActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, searchedPlaceListFragment).addToBackStack(null).commit();
                    PostActivity.fragmentManager.beginTransaction().show(searchedPlaceListFragment).commit();
                }
                //부모 액티비티가 SearchActivity인 경우
                else if (parentActivity.equals("SearchActivity")) {
                    releasePlayer();//동영상 release

                    SearchedPlaceListFragment searchedPlaceListFragment = new SearchedPlaceListFragment();
                    Fragment fragment = SearchActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

                    //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                    Bundle bundle = new Bundle();
                    bundle.putString("address", postData.address);
                    bundle.putDouble("latitude", Double.parseDouble(postData.latitude));
                    bundle.putDouble("longitude", Double.parseDouble(postData.longitude));
                    bundle.putString("parentActivity", parentActivity);
                    searchedPlaceListFragment.setArguments(bundle);


                    SearchActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, searchedPlaceListFragment).addToBackStack(null).commit();
                    SearchActivity.fragmentManager.beginTransaction().show(searchedPlaceListFragment).commit();
                } else if (parentActivity.equals("NotificationActivity")) {
                    releasePlayer();//동영상 release

                    SearchedPlaceListFragment searchedPlaceListFragment = new SearchedPlaceListFragment();
                    Fragment fragment = NotificationActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

                    //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                    Bundle bundle = new Bundle();
                    bundle.putString("address", postData.address);
                    bundle.putDouble("latitude", Double.parseDouble(postData.latitude));
                    bundle.putDouble("longitude", Double.parseDouble(postData.longitude));
                    bundle.putString("parentActivity", parentActivity);
                    searchedPlaceListFragment.setArguments(bundle);


                    NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, searchedPlaceListFragment).addToBackStack(null).commit();
                    NotificationActivity.fragmentManager.beginTransaction().show(searchedPlaceListFragment).commit();
                } else {
                    releasePlayer();//동영상 release

                    SearchedPlaceListFragment searchedPlaceListFragment = new SearchedPlaceListFragment();
                    Fragment fragment = MypageActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);

                    //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
                    Bundle bundle = new Bundle();
                    bundle.putString("address", postData.address);
                    bundle.putDouble("latitude", Double.parseDouble(postData.latitude));
                    bundle.putDouble("longitude", Double.parseDouble(postData.longitude));
                    bundle.putString("parentActivity", parentActivity);
                    searchedPlaceListFragment.setArguments(bundle);


                    MypageActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, searchedPlaceListFragment).addToBackStack(null).commit();
                    MypageActivity.fragmentManager.beginTransaction().show(searchedPlaceListFragment).commit();
                }
            }
        });

        //댓글 버튼 클릭 리스너
        ib_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CommentActivity.class);
                //해당 게시물과 게시물 작성자 데이터를 댓글 화면에서 보여주기 위해서 인텐트로 데이터들을 넘겨준다.
                intent.putExtra("postNum", postNum);
                intent.putExtra("postProfile", postData.profile);
                intent.putExtra("postAccount", postData.account);
                intent.putExtra("postNickname", postData.nickname);
                intent.putExtra("postArticle", postData.article);
                intent.putExtra("postTime", postData.time);
                startActivity(intent);
            }
        });

        tv_commentCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CommentActivity.class);
                //해당 게시물과 게시물 작성자 데이터를 댓글 화면에서 보여주기 위해서 인텐트로 데이터들을 넘겨준다.
                intent.putExtra("postNum", postNum);
                intent.putExtra("postProfile", postData.profile);
                intent.putExtra("postAccount", postData.account);
                intent.putExtra("postNickname", postData.nickname);
                intent.putExtra("postArticle", postData.article);
                intent.putExtra("postTime", postData.time);
                startActivity(intent);
            }
        });

        //더 보기 버튼 리스너
        tv_readmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //게시글의 줄 수를 최대로 늘리고
                tv_article.setMaxLines(Integer.MAX_VALUE);
                //더 보기 버튼을 없앤다.
                tv_readmore.setVisibility(View.GONE);
            }
        });


        return rootView;
    }

    //서버로부터 데이터를 가져오는 메소드
    private void getData() {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("postNum", postNum);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getsinglepost.php", this);
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
    private void pushNotification(String receiver, String title, String body, String click_action, String category, String sender, int postNum) {
        try {
            Log.d(TAG, "pushNotification 메소드 호출");
            JSONObject requestBody = new JSONObject();
            requestBody.put("account", receiver);//알림의 대상이 되는 사람
            requestBody.put("title", title);//알림의 제목
            requestBody.put("body", body);//알림의 내용
            requestBody.put("click_action", click_action);//푸시알림을 눌렀을 때 이동할 액티비티 혹은 프래그먼트
            requestBody.put("category", category);//알림의 카테고리
            requestBody.put("userAccount", sender);//좋아요를 누른 사람
            requestBody.put("postNum", postNum);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "pushnotification.php", this);
            httpRequest.execute();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSON ERROR:" + e.getMessage());
        }

    }


    private void deletePost(int postNum) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("postNum", postNum);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "deletepost.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onHttpResponse(String result) {
        Log.d(TAG, "서버에서 넘어온 json데이터- " + result);
        if (result != null) {
            try {
                JSONObject responseBody = new JSONObject(result);
                String requestType = responseBody.getString("requestType");
                if (requestType.equals("getPost")) {
                    Log.d("서버로부터 넘어온 데이터", result);
                    //제이슨 배열을 통해서 post라는 키에 들어간 배열을 꺼낸다.
                    JSONArray jsonArray = responseBody.getJSONArray("post");
                    //그리고 그 배열에 담긴 데이터를 다시 제이슨 객체에 넣어준다.
                    JSONObject data = jsonArray.getJSONObject(0);
                    postData = new Gson().fromJson(data.toString(), PostData.class);
                    //프로필 사진 설정
                    Glide.with(getContext())
                            .load("http://13.124.105.47/profileimage/" + postData.profile)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                            .into(cv_profile);

                    //닉네임 설정
                    tv_nickname.setText(postData.nickname);

                    //장소 설정
                    tv_place.setText(postData.address);

                    if(postData.type.equals("image")) {//이미지 게시물인 경우

                        //뷰페이저 어댑터 설정
                        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(fragmentManager);
                        viewPager.setAdapter(viewPagerAdapter);
                        //뷰페이저 탭 설정
                        tabLayout.setupWithViewPager(viewPager, true);
                    }
                    else {//동영상 게시물인 경우
                        viewPager.setVisibility(View.INVISIBLE);
                        tabLayout.setVisibility(View.INVISIBLE);
                        playerView.setVisibility(View.VISIBLE);
                        ib_mute.setVisibility(View.VISIBLE);
                        initializePlayer(postData.video);//플레이어 초기화
                    }

                    ib_mute.setSelected(true);
                    //뮤트 버튼 클릭 리스너
                    playerView.getVideoSurfaceView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(ib_mute.isSelected()) {
                                player.setVolume(1);
                                ib_mute.setSelected(false);
                            }
                            else {
                                player.setVolume(0);
                                ib_mute.setSelected(true);
                            }
                        }
                    });



                    tv_article.post(new Runnable() {
                        @Override
                        public void run() {
                            //게시글 설정
                            tv_article.setText(postData.article);
                            //게시글이 두 줄을 초과하면
                            if (tv_article.getLineCount() > 2) {
                                //더 보기 버튼을 보이게 하고
                                tv_readmore.setVisibility(View.VISIBLE);
                                //게시글은 두줄로 줄인다.
                                tv_article.setMaxLines(2);
                            }
                            //게시글이 두 줄 이하면
                            else {
                                //더 보기 버튼을 없앤다.
                                tv_readmore.setVisibility(View.GONE);
                            }
                        }
                    });

                    //해시태그 클릭 리스너
                    hashTagHelper = HashTagHelper.Creator.create(Color.parseColor("#02B2ED"), new HashTagHelper.OnHashTagClickListener() {
                        @Override
                        public void onHashTagClicked(String hashTag) {
                            //부모 액티비티가 PostActivity일 경우
                            if (parentActivity.equals("PostActivity")) {
                                releasePlayer();//동영상 release
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
                                releasePlayer();//동영상 release

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
                                releasePlayer();//동영상 release

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
                                releasePlayer();//동영상 release

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
                        }
                    });

                    //해시태그 설정
                    hashTagHelper.handle(tv_article);

                    //댓글 개수 설정
                    tv_commentCount.setText("댓글 " + postData.commentCount + "개");

                    //좋아요 개수 설정
                    tv_likeCount.setText("좋아요 " + postData.likeCount + "개");

                    //좋아요 버튼 설정
                    //좋아요를 누른 상태면
                    if (postData.isLike == true) {
                        //좋아요 눌림 버튼 설정
                        ib_like.setImageResource(R.drawable.like_clicked);
                    }
                    //좋아요를 누르지 않은 상태이면
                    else {
                        //좋아요 누르지 않은 버튼 설정
                        ib_like.setImageResource(R.drawable.like_unclicked);
                    }

                    //설정 버튼
                    //해당 게시물이 나의 게시물인 경우에만 설정 버튼이 보이게 한다
                    if (postData.isMyPost) {
                        ib_more.setVisibility(View.VISIBLE);
                    }
                    //나의 게시물이 아닌 경우에는
                    else {
                        ib_more.setVisibility(View.GONE);
                    }

                    //게시물 업로드 시간 설정

                    //업로드 시간 설정
                    Date date = new Date();
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        date = dateFormat.parse(postData.time);
                        Log.d("시간", String.valueOf(date));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    tv_uploadTime.setText(beforeTime(date));

                    if(isCommentNotification) {

                        Intent intent = new Intent(getActivity(), CommentActivity.class);
                        //해당 게시물과 게시물 작성자 데이터를 댓글 화면에서 보여주기 위해서 인텐트로 데이터들을 넘겨준다.
                        intent.putExtra("postNum", postNum);
                        intent.putExtra("postProfile", postData.profile);
                        intent.putExtra("postAccount", postData.account);
                        intent.putExtra("postNickname", postData.nickname);
                        intent.putExtra("postArticle", postData.article);
                        intent.putExtra("postTime", postData.time);
                        startActivity(intent);
                        isCommentNotification = false;
                        getArguments().putBoolean("isCommentNotification", false);
                    }

                } else if (requestType.equals("deletePost")) {//게시물 삭제 통신
                    if (parentActivity.equals("PostActivity")) {
                        //스택의 최상단에 있는 프래그먼트를 꺼내서 날려준다.
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙인다.
                        PostActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getActivity(), EmptyActivity.class);
                        startActivity(intent);
                    } else if (parentActivity.equals("SearchActivity")) {
                        //스택의 최상단에 있는 프래그먼트를 꺼내서 날려준다.
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙인다.
                        SearchActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getActivity(), EmptyActivity.class);
                        startActivity(intent);
                    } else if (parentActivity.equals("NotificationActivity")) {
                        //스택의 최상단에 있는 프래그먼트를 꺼내서 날려준다.
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙인다.
                        NotificationActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getActivity(), EmptyActivity.class);
                        startActivity(intent);
                    } else {
                        //스택의 최상단에 있는 프래그먼트를 꺼내서 날려준다.
                        //스택의 최상단에 있는 프래그먼트를 화면에 붙인다.
                        MypageActivity.fragmentManager.popBackStack();
                        //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
                        Intent intent = new Intent(getActivity(), EmptyActivity.class);
                        startActivity(intent);
                    }


                    Toast.makeText(getContext(), "게시물 삭제", Toast.LENGTH_SHORT).show();
                }else if (requestType.equals("likeOk")) {//좋아요를 하는 처리를 하는 통신
                    //좋아요 상태를 true로 만들고
                    postData.isLike = true;
                    //좋아요 개수를 +1
                    postData.likeCount += 1;

                    ib_like.setImageResource(R.drawable.like_clicked);
                    tv_likeCount.setText("좋아요 " + postData.likeCount + "개");

                    //내가 내 게시물에 좋아요를 누른 경우에는 따로 알림을 보내지 않는다.
                    if (!loginUser.getAccount().equals(postData.account)) {
                        //좋아요를 당한 사용자 단말에 push알림을 보내준다.

                        pushNotification(
                                postData.account,
                                "SNS",
                                loginUser.getNickname() + "님이 회원님의 게시물에 좋아요를 눌렀습니다.",
                                "PostDetailFragment",
                                "like",
                                loginUser.getAccount(),
                                postNum);
                    }

                } else if (requestType.equals("likeCancel")) {//좋아요를 취소하는 처리를 하는 통신
                    //좋아요 상태를 false로 만들고
                    postData.isLike = false;
                    //좋아요 개수를 -1
                    postData.likeCount -= 1;

                    ib_like.setImageResource(R.drawable.like_unclicked);
                    tv_likeCount.setText("좋아요 " + postData.likeCount + "개");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializePlayer(String video) {
        if(player == null) {
            player = ExoPlayerFactory.newSimpleInstance(getContext());
            playerView.setPlayer(player);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.setVolume(0);
            String uri = "http://13.124.105.47/uploadvideo/" + video;
            MediaSource mediaSource = buildMediaSource(Uri.parse(uri));
            player.prepare(mediaSource);
            player.setPlayWhenReady(true);
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "sns"));
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    private void releasePlayer() {
        if(player != null) {
            player.release();
            player = null;
        }
    }

    //뷰페이저 어댑터
    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        //생성자
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        //포지션별로 각각의 이미지를 불러온다.
        @Override
        public Fragment getItem(int i) {
            return PostImageFragment.newInstance("http://13.124.105.47/uploadimage/" + postData.imageList.get(i));
        }

        //이미지의 총 수 반환
        @Override
        public int getCount() {
            return postData.imageList.size();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }

    //업로드된 게시물이 몇 분 전에 만들어진 게시물인지를 리턴해주는 메소드
    public String beforeTime(Date date) {

        //캘린더 클레스는 추상 클레스라서 객체를 생성할 수 없다.
        //대신 getInstance()메소드를 통해서 객체 생성이 가능하다.
        Calendar c = Calendar.getInstance();

        //캘린더 객체에서 getTimeInMillis()메소드를 사용해 현재 시간을 가져옴
        long now = c.getTimeInMillis();
        Log.d("현재 시간", String.valueOf(now));
        //date에서 시간만 가져온다. 여기서 중요한 점은 now변수는 계속해서 현재시간을 반환하기 때문에 변하는 수이고
        //date는 내가 선언한 순간의 시간을 가져오기 때문에 고정된 시간이다.
        long dateM = date.getTime();
        Log.d("입력된 날짜", String.valueOf(dateM));

        //이 변수는 위에서 봤듯이 현재의 시간에서 내가 이 메소드를 호출한 시간을 뺀 시간을 의미한다.
        long gap = now - dateM;

        String ret = "";

//        초       분   시
//        1000    60  60
        gap = (long) (gap / 1000);
        long hour = gap / 3600;
        gap = gap % 3600;
        long min = gap / 60;
        long sec = gap % 60;

        if (hour > 24) {
            ret = hour / 24 + "일 전";
        } else if (hour > 0) {
            ret = hour + "시간 전";
        } else if (min > 0) {
            ret = min + "분 전";
        } else if (sec > 0) {
            ret = sec + "초 전";
        } else {
            ret = new SimpleDateFormat("HH:mm").format(date);
        }
        return ret;

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged 호출");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume 호출");
        //게시물 번호를 전송해서 게시물 데이터를 서버로부터 가져온다.
        getData();

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause 호출");
        super.onPause();
        releasePlayer();//플레이어 release
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop 호출");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy 호출");
        super.onDestroy();
    }
}



