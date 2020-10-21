package com.example.sns.search.fragment;

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
import android.widget.Toast;

import com.example.sns.post.fragment.HashTagPostListFragment;
import com.example.sns.R;
import com.example.sns.search.activity.SearchActivity;
import com.example.sns.search.adapter.SearchedTagAdapter;
import com.example.sns.search.model.SearchedTagItem;
import com.example.sns.util.HttpRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchedTagFragment extends Fragment implements SearchActivity.SearchedTagListener, SearchedTagAdapter.SearchedTagRecyclerViewListener, HttpRequest.OnHttpResponseListener {
    private String TAG = "SearchedTagFragment";
    private RecyclerView rv_searchedTag;
    private LinearLayoutManager linearLayoutManager;
    private SearchedTagAdapter searchedTagAdapter;
    private ArrayList<SearchedTagItem> searchedTagItemArrayList;
    SearchActivity searchActivity;
    //서버에서 넘어온 게시글을 모두 합쳐서 담을 텍스트뷰
    TextView tv_tag;
    //검색어
    String searchText;
    //직전에 로드된 게시물 수
    int listSize;
    //현재 로드된 태그 수
    int currentCount;
    //서버에서 넘어온 모든 게시글들을 이어서 합쳐주는 StringBuilder
    private StringBuilder articleBuilder;
    boolean loadPossible = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_searchedtag, container, false);

        rv_searchedTag = rootView.findViewById(R.id.recyclerview_searchedtag);
        tv_tag = rootView.findViewById(R.id.textview_tag);

        articleBuilder = new StringBuilder();

        searchActivity = new SearchActivity();
        searchActivity.setOnSearchedTagListener(this);

        listSize = 20;

        setRecyclerView();

        //addOnScrollListener는 리사이클러뷰를 스크롤하는 순간을 캐치한다.
        rv_searchedTag.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                if ((lastVisibleItemPosition == totalCount - 1) && visibleItemCount == 8 && lastCompletelyVisibleItemPosition == totalCount - 1 && loadPossible == true) {
                    Log.d("페이징 조건", "부합");
                    //다음 페이지를 로드한다.
                    //param:현재 로드되어있는 데이터의 수(다음 페이지에 로드되어야 할 첫번째 게시물의 index)
                    loadNextPage();
                }
            }
        });


        return rootView;
    }

    private void setRecyclerView() {

        searchedTagItemArrayList = new ArrayList<>();

        rv_searchedTag.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());
        rv_searchedTag.setLayoutManager(linearLayoutManager);

        searchedTagAdapter = new SearchedTagAdapter(searchedTagItemArrayList, getContext());
        searchedTagAdapter.setOnClickListener(this);
        rv_searchedTag.setAdapter(searchedTagAdapter);

    }

    //서버와 통신하여 해시태그 리스트를 가져오는 메소드
    private void getSearchedTag(String searchText, boolean isSearched, int lastId, int listSize) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("requestType", "getTag");
            requestBody.put("searchText", searchText);
            requestBody.put("isSearched", isSearched);
            requestBody.put("lastId", lastId);
            requestBody.put("listSize", listSize);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "gettag.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //태그 검색 결과 목록의 최하단으로 이동하면 페이징 처리를 위해서 실행될 메소드
    public void loadNextPage() {
        loadPossible = false;
        //null을 넣어서 뷰타입이 프로그래스 아이템이 추가되게 한다.
        searchedTagItemArrayList.add(null);
        searchedTagAdapter.notifyItemInserted(searchedTagItemArrayList.size() - 1);
        //핸들러를 통해서 2초 뒤에 해당 기능을 실행한다.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("requestType", "loadNextTag");//요청 타입
                    requestBody.put("searchText", searchText);//검색어
                    requestBody.put("lastId", currentCount);//현재 아이템 수
                    requestBody.put("listSize", 20);//가져올 데이터 수
                    if (searchText == null) {
                        requestBody.put("isSearched", false);//검색하지 않은 경우
                    } else {
                        requestBody.put("isSearched", true);//검색한 경우
                    }
                    HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "gettag.php", SearchedTagFragment.this::onHttpResponse);
                    httpRequest.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, 1500);

    }

    @Override
    public void isSearched(String searchText) {
        Log.d("태그 검색 isSearched", "호출");
        Log.d("검색어", searchText);
        if (searchText.length() == 0) {
            Log.d("검색어", "없음");
            this.searchText = searchText;
            getSearchedTag(searchText, false, 0, listSize);
        } else {
            Log.d("검색어", searchText);
            this.searchText = searchText;
            getSearchedTag(searchText, true, 0, 20);
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
                if (requestType.equals("getTag")) {
                    searchedTagItemArrayList.clear();//아이템을 모두 비워준다.
                    currentCount = 0;
                    JSONArray jsonArray = responseBody.getJSONArray("tagList");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        SearchedTagItem searchedTagItem = new Gson().fromJson(data.toString(), SearchedTagItem.class);

                        searchedTagItemArrayList.add(searchedTagItem);
                        currentCount += 1;
                    }
                    searchedTagAdapter.notifyDataSetChanged();
                } else if (requestType.equals("loadNextTag")) {
                    boolean isSubstituted = true;
                    JSONArray jsonArray = responseBody.getJSONArray("tagList");

                    if (jsonArray.length() != 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject data = jsonArray.getJSONObject(i);
                            SearchedTagItem searchedTagItem = new Gson().fromJson(data.toString(), SearchedTagItem.class);
                            //로딩바를 다음 페이지의 첫번째 게시물로 교체하기 위한 작업
                            if (isSubstituted) {
                                //프로그래스바 아이템을 게시물 아이템으로 교체해준다.
                                searchedTagItemArrayList.set(searchedTagItemArrayList.size() - 1, searchedTagItem);
                                //어댑터 notify
                                searchedTagAdapter.notifyItemChanged(searchedTagItemArrayList.size() - 1);
                                Log.d("사람검색 사이즈:", String.valueOf(searchedTagItemArrayList.size()));
                                isSubstituted = false;
                            }
                            //최초에 로딩바와 게시물이 교체가 끝났으면 그 뒤로는 게시물 추가만 해준다.
                            else {
                                searchedTagItemArrayList.add(searchedTagItem);
                                searchedTagAdapter.notifyItemInserted(searchedTagItemArrayList.size() - 1);
                            }

                            listSize += 1;
                            currentCount += 1;
                            loadPossible = true;
                        }
                    } else {//데이터가 존재하지 않는 경우
                        loadPossible = true;
                        searchedTagItemArrayList.remove(searchedTagItemArrayList.size() - 1);//프로그래스바 삭제
                        searchedTagAdapter.notifyItemRemoved(searchedTagItemArrayList.size());
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(), "문제가 생겼습니다", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("태그검색 프래그먼트 onResume", "호출");
        //데이터 변경을 계속 반영해주기 위해서 프래그먼트가 화면에 나타나면 반드시 타게 되어있는 onResume단계에 데이터를 계속 새롭게 가져오는
        //비동기 클래스를 넣어준다.
        //검색어가 없는 경우에는 전체 데이터를 가져오고
        if (searchText == null) {
            //여기에 팔로잉 전체 데이터를 가져와서 셋해주는 코딩
            getSearchedTag(searchText, false, 0, listSize);
        }
        //검색어가 입력되어있던 상태였으면 검색어에 맞는 데이터를 가져온다.
        else {
            //여기에 팔로잉 전체 데이터를 가져와서 셋해주는 코딩
            getSearchedTag(searchText, true, 0, listSize);
        }
    }

    @Override
    public void onListClicked(int position) {
        HashTagPostListFragment hashTagPostListFragment = new HashTagPostListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("hashTag", searchedTagItemArrayList.get(position).getTag());
        bundle.putString("parentActivity", "SearchActivity");
        hashTagPostListFragment.setArguments(bundle);
        //해시태그 리스트 프래그먼트로 바꿔준다.
        SearchActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, hashTagPostListFragment).addToBackStack(null).commit();
        SearchActivity.fragmentManager.beginTransaction().show(hashTagPostListFragment).commit();
        //키보드 종료
        SearchActivity.softKeyboard.closeSoftKeyboard();
    }
}
