package com.example.sns;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class HashTagPostListFragment extends Fragment implements GridPostAdapter.GridPostRecyclerViewListener, HttpRequest.OnHttpResponseListener {
    private final String TAG = HashTagPostListFragment.class.getSimpleName();
    private TextView tv_hashtag;
    private ImageButton ib_back;
    private SwipeRefreshLayout swipeRefreshLayout;

    //번들로 넘어온 hashtag
    private String hashTag;
    //부모 액티비니
    private String parentActivity;

    //현재 로드된 게시물 수
    private int currentCount;

    //한번에 로드할 데이터의 개수
    private int listSize;

    private RecyclerView rv_hashtagpost;
    private GridPostAdapter gridPostAdapter;
    private GridLayoutManager gridLayoutManager;
    private ArrayList<PostItem> postItemArrayList;

    private LoginUser loginUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_hashtagpostlist, container, false);
        loginUser = LoginUser.getInstance();
        //상단의 해시태그 타이틀
        tv_hashtag = rootView.findViewById(R.id.textview_hashtag);
        //뒤로가기 버튼
        ib_back = rootView.findViewById(R.id.imagebutton_back);
        //그리드 리사이클러뷰
        rv_hashtagpost = rootView.findViewById(R.id.recyclerview_hashtagpost);
        //새로고침
        swipeRefreshLayout = rootView.findViewById(R.id.refresh_layout);

        listSize = 15;

        //새로고침 리스너
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                GetHashTagPost getHashTagPost = new GetHashTagPost();
//                getHashTagPost.execute(String.valueOf(0), account, hashTag, String.valueOf(15));
                getHashTagPost(loginUser.getAccount(), hashTag, 0, 15);
                swipeRefreshLayout.setRefreshing(false);
                listSize = 15;
            }
        });

        //번들 데이터 저장
        if (getArguments() != null) {
            hashTag = getArguments().getString("hashTag");
            parentActivity = getArguments().getString("parentActivity");
        }

        tv_hashtag.setText("#" + hashTag);

        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (parentActivity.equals("PostActivity")) {
                    if (PostActivity.fragmentManager.getBackStackEntryCount() == 0) {
                        PostActivity.fragmentManager.beginTransaction().remove(PostActivity.hashTagPostListFragment).commit();
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
                } else if (parentActivity.equals("MyPageActivity")) {
                    if (MypageActivity.fragmentManager.getBackStackEntryCount() == 0) {
                        MypageActivity.fragmentManager.beginTransaction().remove(MypageActivity.hashTagPostListFragment).commit();
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
                } else if (parentActivity.equals("SearchActivity")) {
                    if (SearchActivity.fragmentManager.getBackStackEntryCount() == 0) {
                        SearchActivity.fragmentManager.beginTransaction().remove(SearchActivity.hashTagPostListFragment).commit();
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
                } else {
                    if (NotificationActivity.fragmentManager.getBackStackEntryCount() == 0) {
                        NotificationActivity.fragmentManager.beginTransaction().remove(NotificationActivity.hashTagPostListFragment).commit();
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
                }

            }
        });

        setRecyclerView();

        //addOnScrollListener는 리사이클러뷰를 스크롤하는 순간을 캐치한다.
        rv_hashtagpost.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                if ((lastVisibleItemPosition == totalCount - 1) && visibleItemCount == 12 && lastCompletelyVisibleItemPosition == totalCount - 1) {
                    Log.d("페이징 조건", "부합");
                    //다음 페이지를 로드한다.
                    //param:현재 로드되어있는 데이터의 수(다음 페이지에 로드되어야 할 첫번째 게시물의 index)
                    loadNextPage();
                }


            }
        });

        return rootView;
    }

    public void setRecyclerView() {
        postItemArrayList = new ArrayList<>();

        rv_hashtagpost.setHasFixedSize(true);

        //어댑터 설정
        gridPostAdapter = new GridPostAdapter(postItemArrayList, getContext());
        gridPostAdapter.setOnClickListener(this);
        rv_hashtagpost.setAdapter(gridPostAdapter);

        //레이아웃 메니저 설정
        gridLayoutManager = new GridLayoutManager(getContext(), 3);


        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {

                int viewType = gridPostAdapter.getItemViewType(position);

                if (viewType == gridPostAdapter.VIEW_PROGRESS) {
                    return 3;
                } else {
                    return 1;
                }

            }
        });
        rv_hashtagpost.setLayoutManager(gridLayoutManager);

    }

    private void getHashTagPost(String account, String hashTag, int lastId, int listSize) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("requestType", "getPost");
            requestBody.put("account", account);
            requestBody.put("hashTag", hashTag);
            requestBody.put("lastId", lastId);
            requestBody.put("listSize", listSize);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "gethashtagpost.php", this::onHttpResponse);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //게시물 리스트의 최하단으로 이동하면 페이징 처리를 위해서 실행될 메소드
    public void loadNextPage() {
        //null을 넣어서 뷰타입이 프로그래스 아이템이 추가되게 한다.
        postItemArrayList.add(null);
        gridPostAdapter.notifyItemInserted(postItemArrayList.size() - 1);
        //핸들러를 통해서 2초 뒤에 해당 기능을 실행한다.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("requestType", "loadNextPage");
                    requestBody.put("account", loginUser.getAccount());
                    requestBody.put("hashTag", hashTag);
                    requestBody.put("lastId", currentCount);
                    requestBody.put("listSize", 15);
                    HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "gethashtagpost.php", HashTagPostListFragment.this::onHttpResponse);
                    httpRequest.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 1500);

    }

    @Override
    public void onHttpResponse(String result) {
        Log.d(TAG, "서버에서 넘어온 json데이터- " + result);
        if (result != null) {
            try {
                JSONObject responseBody = new JSONObject(result);
                String requestType = responseBody.getString("requestType");
                if (requestType.equals("getPost")) {
                    currentCount = 0;
                    postItemArrayList.clear();
                    //jsonarray를 선언해서
                    JSONArray jsonArray = responseBody.getJSONArray("post");
                    Log.d("게시물 수", String.valueOf(jsonArray.length()));

                    //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        PostItem postItem = new Gson().fromJson(data.toString(), PostItem.class);

                        //모든 데이터를 다 담았으면 이제 그 객체를 리사이클러뷰 어레이리스트에 추가해준다.
                        postItemArrayList.add(postItem);
                        //arraylist가 모두 추가됐으면 어댑터 notify
                        gridPostAdapter.notifyDataSetChanged();

                        currentCount += 1;
                    }
                }
                else if(requestType.equals("loadNextPage")) {
                    boolean isSubstituted = true;
                    try {
                        JSONArray jsonArray = responseBody.getJSONArray("post");
                        Log.d("게시물 수", String.valueOf(jsonArray.length()));

                        //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject data = jsonArray.getJSONObject(i);
                            PostItem postItem = new Gson().fromJson(data.toString(), PostItem.class);

                            //로딩바를 다음 페이지의 첫번째 게시물로 교체하기 위한 작업
                            if (isSubstituted) {
                                //프로그래스바 아이템을 게시물 아이템으로 교체해준다.
                                postItemArrayList.set(postItemArrayList.size() - 1, postItem);
                                //어댑터 notify
                                gridPostAdapter.notifyItemChanged(postItemArrayList.size() - 1);
                                Log.d("게시물 사이즈:", String.valueOf(postItemArrayList.size()));
                                isSubstituted = false;
                            }
                            //최초에 로딩바와 게시물이 교체가 끝났으면 그 뒤로는 게시물 추가만 해준다.
                            else {
                                postItemArrayList.add(postItem);
                                gridPostAdapter.notifyItemInserted(postItemArrayList.size() - 1);
                            }
                            listSize += 1;
                            currentCount += 1;
                        }
                    } catch (Exception e) {
                        Log.d("데이터 셋 오류", e.getMessage());
                        postItemArrayList.remove(postItemArrayList.size() - 1);
                        gridPostAdapter.notifyItemRemoved(postItemArrayList.size());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onGridPictureClicked(int position) {
        //부모 프래그먼트가 PostActivity인 경우
        if (parentActivity.equals("PostActivity")) {
            //그리드 이미지를 누르면 보여줄 프레그먼트 선언
            PostDetailFragment postDetailFragment = new PostDetailFragment();

            Fragment fragment = PostActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);


            //서버에서 데이터를 조회해서 가져오기 위해 필요한 게시물 번호를 번들에 담아서 넘겨준다.
            Bundle bundle = new Bundle();
            bundle.putInt("postNum", postItemArrayList.get(position).getPostNum());
            bundle.putString("parentActivity", parentActivity);
            postDetailFragment.setArguments(bundle);

            //프래그먼트를 프래임 레이아웃에 붙여준다.
            PostActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, postDetailFragment).addToBackStack(null).commit();
            PostActivity.fragmentManager.beginTransaction().show(postDetailFragment).commit();
        }
        //부모 프래그먼트가 MyPageActivity인 경우
        else if (parentActivity.equals("MyPageActivity")) {
            //그리드 이미지를 누르면 보여줄 프레그먼트 선언
            PostDetailFragment postDetailFragment = new PostDetailFragment();

            Fragment fragment = MypageActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);


            //서버에서 데이터를 조회해서 가져오기 위해 필요한 게시물 번호를 번들에 담아서 넘겨준다.
            Bundle bundle = new Bundle();
            bundle.putInt("postNum", postItemArrayList.get(position).getPostNum());
            bundle.putString("parentActivity", parentActivity);
            postDetailFragment.setArguments(bundle);

            //프래그먼트를 프래임 레이아웃에 붙여준다.
            MypageActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, postDetailFragment).addToBackStack(null).commit();
            MypageActivity.fragmentManager.beginTransaction().show(postDetailFragment).commit();
        }
        //부모 프래그먼트가 SearchActivity인 경우
        else if (parentActivity.equals("SearchActivity")) {
            //그리드 이미지를 누르면 보여줄 프레그먼트 선언
            PostDetailFragment postDetailFragment = new PostDetailFragment();

            Fragment fragment = SearchActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);


            //서버에서 데이터를 조회해서 가져오기 위해 필요한 게시물 번호를 번들에 담아서 넘겨준다.
            Bundle bundle = new Bundle();
            bundle.putInt("postNum", postItemArrayList.get(position).getPostNum());
            bundle.putString("parentActivity", parentActivity);
            postDetailFragment.setArguments(bundle);

            //프래그먼트를 프래임 레이아웃에 붙여준다.
            SearchActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, postDetailFragment).addToBackStack(null).commit();
            SearchActivity.fragmentManager.beginTransaction().show(postDetailFragment).commit();
        }
        //부모 프래그먼트가 NotificationActivity인 경우
        else {
            //그리드 이미지를 누르면 보여줄 프레그먼트 선언
            PostDetailFragment postDetailFragment = new PostDetailFragment();

            Fragment fragment = NotificationActivity.fragmentManager.findFragmentById(R.id.frame_parent_container);


            //서버에서 데이터를 조회해서 가져오기 위해 필요한 게시물 번호를 번들에 담아서 넘겨준다.
            Bundle bundle = new Bundle();
            bundle.putInt("postNum", postItemArrayList.get(position).getPostNum());
            bundle.putString("parentActivity", parentActivity);
            postDetailFragment.setArguments(bundle);

            //프래그먼트를 프래임 레이아웃에 붙여준다.
            NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, postDetailFragment).addToBackStack(null).commit();
            NotificationActivity.fragmentManager.beginTransaction().show(postDetailFragment).commit();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("해시태그 리스트 onResume", "호출");
        getHashTagPost(loginUser.getAccount(), hashTag, 0, listSize);
    }
}
