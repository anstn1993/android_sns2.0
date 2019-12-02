package com.example.sns;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class GridPostFragment extends Fragment implements GridPostAdapter.GridPostRecyclerViewListener, HttpRequest.OnHttpResponseListener {

    private String TAG = "GridPostFragment";

    //리사이클러뷰
    private RecyclerView rv_gridpost;
    //리사이클러뷰 어댑터
    private GridPostAdapter gridpostAdapter;
    //그리드뷰 레이아웃 매니저
    private GridLayoutManager gridLayoutManager;

    //리사이클러뷰 아이템 arraylist
    private ArrayList<PostItem> postItemArrayList;

    //프래그먼트 매니저
    private FragmentManager fragmentManager;

    //현재 로드되어있는 게시물의 수
    private int currentPostCount;

    private boolean isFirstLoad = false;

    //해당 페이지의 주인 account
    private String hostAccount;

    //부모 액티비티
    private String parentActivity;

    private boolean loadPossible = true;

    private LoginUser loginUser;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("onCreateView", "호출");

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_gridpost, container, false);
        loginUser = LoginUser.getInstance();
        currentPostCount = 0;

        if (getArguments() != null) {
            hostAccount = getArguments().getString("account");
            parentActivity = getArguments().getString("parentActivity");
        }


        //리사이클러뷰 선언
        rv_gridpost = rootView.findViewById(R.id.recyclerview_grid);

        setRecyclerView();

        fragmentManager = getFragmentManager();


        //addOnScrollListener는 리사이클러뷰를 스크롤하는 순간을 캐치한다.
        rv_gridpost.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                if (postItemArrayList.size() >= 15 && lastCompletelyVisibleItemPosition == totalCount - 1 && loadPossible == true) {
                    Log.d("페이징 조건", "부합");
                    //다음 페이지를 로드한다.
                    //param:현재 로드되어있는 데이터의 수(다음 페이지에 로드되어야 할 첫번째 게시물의 index)
                    loadNextPage(loginUser.getAccount(), hostAccount, postItemArrayList.get(postItemArrayList.size() - 1).postNum, 15);
                }


            }
        });


        return rootView;
    }

    private void setRecyclerView() {

        //아이템 arraylist를 초기화해준다.
        postItemArrayList = new ArrayList<>();

        rv_gridpost.setHasFixedSize(true);

        //어댑터 설정
        gridpostAdapter = new GridPostAdapter(postItemArrayList, getContext());
        gridpostAdapter.setOnClickListener(this);
        rv_gridpost.setAdapter(gridpostAdapter);

        //레이아웃 메니저 설정
        gridLayoutManager = new GridLayoutManager(getContext(), 3);


        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {

                int viewType = gridpostAdapter.getItemViewType(position);

                if (viewType == gridpostAdapter.VIEW_PROGRESS) {
                    return 3;
                } else {
                    return 1;
                }

            }
        });
        rv_gridpost.setLayoutManager(gridLayoutManager);


    }

    //게시물 리스트의 최하단으로 이동하면 페이징 처리를 위해서 실행될 메소드
    public void loadNextPage(String account, String hostAccount, int lastId, int listSize) {
        loadPossible = false;
        //null을 넣어서 뷰타입이 프로그래스 아이템이 추가되게 한다.
        postItemArrayList.add(null);
        gridpostAdapter.notifyItemInserted(postItemArrayList.size() - 1);
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
                    HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "gettargetpost.php", GridPostFragment.this);
                    httpRequest.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, 1500);
    }

    private void getPost(String account, String hostAccount, int lastId, int listSize) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("requestType", "getPost");
            requestBody.put("myAccount", account);//자신의 계정
            requestBody.put("hostAccount", hostAccount);//페이지의 주인 계정
            requestBody.put("lastId", lastId);
            requestBody.put("listSize", listSize);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "gettargetpost.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
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
                if (requestType.equals("getPost")) {//게시물을 가져오기 위한 통신
                    postItemArrayList.clear();
                    JSONArray jsonArray = responseBody.getJSONArray("post");//jsonArray선언
                    Log.d("게시물 수", String.valueOf(jsonArray.length()));

                    //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                    for (int i = 0; i < jsonArray.length(); i++) {
                        //서버로부터 넘어온 데이터를 변수에 정의
                        JSONObject data = jsonArray.getJSONObject(i);
                        PostItem postItem = new Gson().fromJson(data.toString(), PostItem.class);
                        //모든 데이터를 다 담았으면 이제 그 객체를 리사이클러뷰 어레이리스트에 추가해준다.
                        postItemArrayList.add(postItem);
                        //arraylist가 모두 추가됐으면 어댑터 notify
                        gridpostAdapter.notifyDataSetChanged();
                        //현재 로르되어있는 게시물 +1
                        currentPostCount += 1;
                    }
                } else if (requestType.equals("loadNextPage")) { //페이징 처리를 위한 통신

                    JSONArray jsonArray = responseBody.getJSONArray("post");

                    if (jsonArray.length() != 0) {

                        boolean isSubstituted = true;

                        //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                        for (int i = 0; i < jsonArray.length(); i++) {
                            //서버로부터 넘어온 데이터를 변수에 정의
                            JSONObject data = jsonArray.getJSONObject(i);
                            PostItem postItem = new Gson().fromJson(data.toString(), PostItem.class);

                            //로딩바를 다음 페이지의 첫번째 게시물로 교체하기 위한 작업
                            if (isSubstituted) {
                                //프로그래스바 아이템을 게시물 아이템으로 교체해준다.
                                postItemArrayList.set(postItemArrayList.size() - 1, postItem);


                                //어댑터 notify
                                gridpostAdapter.notifyItemChanged(postItemArrayList.size() - 1);
                                Log.d("게시물 사이즈:", String.valueOf(postItemArrayList.size()));

                                isSubstituted = false;
                            }
                            //최초에 로딩바와 게시물이 교체가 끝났으면 그 뒤로는 게시물 추가만 해준다.
                            else {
                                postItemArrayList.add(postItem);
                                gridpostAdapter.notifyItemInserted(postItemArrayList.size() - 1);
                            }

                            //현재 로드되어있는 게시물 수 +1
                            currentPostCount += 1;

                        }
                    } else {//더이상 불러올 게시물이 없는 경우 그냥 프로그래스 바만 지워준다.
                        postItemArrayList.remove(postItemArrayList.size() - 1);
                        gridpostAdapter.notifyItemRemoved(postItemArrayList.size());
                    }

                    loadPossible = true;//다시 페이징 시도가 가능한 상태로 전환
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    @Override
    public void onResume() {
        Log.d(TAG, "onResume 호출");
        super.onResume();

        int listSize = 15;
        if (!postItemArrayList.isEmpty()) {//게시물이 처음 로드되어서 하나도 없는 경우
            listSize = postItemArrayList.size();
        }
        getPost(loginUser.getAccount(), hostAccount, 0, listSize);

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged 호출");
        if (!hidden) {//화면에서 사라지는 경우에 새롭게 데이터를 가져와서 화면에 나타날때 바로 데이터가 보일 수 있게 해준다.
            int listSize = 15;
            if (!postItemArrayList.isEmpty()) {//게시물이 처음 로드되어서 하나도 없는 경우
                listSize = postItemArrayList.size();
            }
            getPost(loginUser.getAccount(), hostAccount, 0, listSize);
        }
    }

    //그리드 이미지 클릭 콜백 매소드
    @Override
    public void onGridPictureClicked(int position) {

        if (parentActivity.equals("PostActivity")) {
            //그리드 이미지를 누르면 보여줄 프레그먼트 선언
            PostDetailFragment postDetailFragment = new PostDetailFragment();


            //서버에서 데이터를 조회해서 가져오기 위해 필요한 게시물 번호를 번들에 담아서 넘겨준다.
            Bundle bundle = new Bundle();
            bundle.putInt("postNum", postItemArrayList.get(position).getPostNum());
            bundle.putString("parentActivity", parentActivity);
            postDetailFragment.setArguments(bundle);


            //프래그먼트를 프래임 레이아웃에 붙여준다.
            PostActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, postDetailFragment).addToBackStack(null).commit();
            PostActivity.fragmentManager.beginTransaction().show(postDetailFragment).commit();
        } else if (parentActivity.equals("SearchActivity")) {
            //그리드 이미지를 누르면 보여줄 프레그먼트 선언
            PostDetailFragment postDetailFragment = new PostDetailFragment();


            //서버에서 데이터를 조회해서 가져오기 위해 필요한 게시물 번호를 번들에 담아서 넘겨준다.
            Bundle bundle = new Bundle();
            bundle.putInt("postNum", postItemArrayList.get(position).getPostNum());
            bundle.putString("parentActivity", parentActivity);
            postDetailFragment.setArguments(bundle);
//        changeFragment(FRAGMENT_POSTDETAIL);

            //프래그먼트를 프래임 레이아웃에 붙여준다.
            SearchActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, postDetailFragment).addToBackStack(null).commit();
            SearchActivity.fragmentManager.beginTransaction().show(postDetailFragment).commit();
        } else if (parentActivity.equals("NotificationActivity")) {
            //그리드 이미지를 누르면 보여줄 프레그먼트 선언
            PostDetailFragment postDetailFragment = new PostDetailFragment();


            //서버에서 데이터를 조회해서 가져오기 위해 필요한 게시물 번호를 번들에 담아서 넘겨준다.
            Bundle bundle = new Bundle();
            bundle.putInt("postNum", postItemArrayList.get(position).getPostNum());
            bundle.putString("parentActivity", parentActivity);
            postDetailFragment.setArguments(bundle);

            //프래그먼트를 프래임 레이아웃에 붙여준다.
            NotificationActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, postDetailFragment).addToBackStack(null).commit();
            NotificationActivity.fragmentManager.beginTransaction().show(postDetailFragment).commit();
        } else {
            //그리드 이미지를 누르면 보여줄 프레그먼트 선언
            PostDetailFragment postDetailFragment = new PostDetailFragment();


            //서버에서 데이터를 조회해서 가져오기 위해 필요한 게시물 번호를 번들에 담아서 넘겨준다.
            Bundle bundle = new Bundle();
            bundle.putInt("postNum", postItemArrayList.get(position).getPostNum());
            bundle.putString("parentActivity", parentActivity);
            postDetailFragment.setArguments(bundle);

            //프래그먼트를 프래임 레이아웃에 붙여준다.
            MypageActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, postDetailFragment).addToBackStack(null).commit();
            MypageActivity.fragmentManager.beginTransaction().show(postDetailFragment).commit();
        }
    }
}
