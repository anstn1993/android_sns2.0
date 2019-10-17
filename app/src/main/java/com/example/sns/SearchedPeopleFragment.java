package com.example.sns;

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
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.sns.LoginActivity.account;

public class SearchedPeopleFragment extends Fragment implements SearchedPeopleAdapter.SearchedPeopleRecyclerViewListener, SearchActivity.SearchedPeopleListener, HttpRequest.OnHttpResponseListener {
    private String TAG = "SearchedPeopleFragment";
    private RecyclerView rv_searchedPeople;
    private LinearLayoutManager linearLayoutManager;
    private SearchedPeopleAdapter searchedPeopleAdapter;
    private ArrayList<SearchedPeopleItem> searchedPeopleItemArrayList;

    SearchActivity searchActivity;

    String searchText;

    //현재 로드되어있는 마지막 사용자 데이터의 id값
    int currentLastId;
    //직전에 로드된 게시물의 수
    int listSize;

    boolean loadPossible = true;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_searchedpeople, container, false);

        //리사이클러뷰
        rv_searchedPeople = rootView.findViewById(R.id.recyclerview_searchedpeople);

        //리사이클러뷰 셋
        setRecyclerView();

        searchActivity = new SearchActivity();
        searchActivity.setOnSearchedPeopleListener(this);

        listSize = 20;

        //addOnScrollListener는 리사이클러뷰를 스크롤하는 순간을 캐치한다.
        rv_searchedPeople.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        searchedPeopleItemArrayList = new ArrayList<>();

        rv_searchedPeople.setHasFixedSize(true);
        //레이아웃 메니저 설정
        linearLayoutManager = new LinearLayoutManager(getContext());
        rv_searchedPeople.setLayoutManager(linearLayoutManager);
        //어댑터 설정
        searchedPeopleAdapter = new SearchedPeopleAdapter(searchedPeopleItemArrayList, getContext());
        searchedPeopleAdapter.setOnClickListener(this);
        rv_searchedPeople.setAdapter(searchedPeopleAdapter);


    }

    private void getSearchedPeople(String account, String searchText, boolean isSearched, int lastId, int listSize) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("requestType", "getUser");
            requestBody.put("account", account);
            requestBody.put("searchText", searchText);
            requestBody.put("isSearched", isSearched);
            requestBody.put("lastId", lastId);
            requestBody.put("listSize", listSize);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getuser.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    //사람 검색 결과 목록의 최하단으로 이동하면 페이징 처리를 위해서 실행될 메소드
    public void loadNextPage() {
        loadPossible = false;
        //null을 넣어서 뷰타입이 프로그래스 아이템이 추가되게 한다.
        searchedPeopleItemArrayList.add(null);
        searchedPeopleAdapter.notifyItemInserted(searchedPeopleItemArrayList.size() - 1);
        //핸들러를 통해서 2초 뒤에 해당 기능을 실행한다.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("requestType", "loadNextUser");//요청타입 페이징
                    requestBody.put("account", account);
                    requestBody.put("searchText", searchText);//검색어
                    requestBody.put("lastId", searchedPeopleItemArrayList.get(searchedPeopleItemArrayList.size()-2).id);//현재 로드된 사용자 리스트의 마지막 id
                    requestBody.put("listSize", 20);//가져올 데이터 개수
                    HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getuser.php", SearchedPeopleFragment.this::onHttpResponse);
                    httpRequest.execute();
                    if (searchText == null) {
                        requestBody.put("isSearched", false);//검색어가 존재하지 않는 경우
                    } else {
                        requestBody.put("isSearched", true);//검색어가 존재하는 경우
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 1500);

    }


    //SearchActivity에서 검색어를 입력할 때마다 콜백되는 메소드
    @Override
    public void isSearched(String searchText) {
        Log.d("사람 검색 isSearched", "호출");
        Log.d("검색어", searchText);
        if (searchText.length() == 0) {
            Log.d("검색어", "없음");
            this.searchText = searchText;
            getSearchedPeople(account, searchText, false, 0, listSize);
        } else {
            Log.d("검색어", searchText);
            this.searchText = searchText;
            getSearchedPeople(account, searchText, true, 0, listSize);
        }

    }


    //서버와 통신에 성공했을 때 호출되는 콜백 메소드(HttpRequest클래스(AsyncTask클래스 상속)의 onPostExecute()메소드에서 호출)
    //모든 통신에 대한 결과는 이 콜백 메소드에서 처리해준다.
    @Override
    public void onHttpResponse(String result) {
        Log.d(TAG, "통신 성공");
        Log.d(TAG, "서버에서 넘어온 json데이터- " + result);
        if (result != null) {//response가 존재할 때
            try {
                JSONObject responseBody = new JSONObject(result);
                String requestType = responseBody.getString("requestType");
                if (requestType.equals("getUser")) {
                    searchedPeopleItemArrayList.clear();//아이템을 비워준다.
                    JSONArray jsonArray = responseBody.getJSONArray("userlist");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        SearchedPeopleItem searchedPeopleItem = new Gson().fromJson(data.toString(), SearchedPeopleItem.class);
                        searchedPeopleItemArrayList.add(searchedPeopleItem);
                    }

                    searchedPeopleAdapter.notifyDataSetChanged();
                    if (searchedPeopleItemArrayList.size() != 0) {
                        currentLastId = searchedPeopleItemArrayList.get(searchedPeopleItemArrayList.size() - 1).id;
                    }
                } else if (requestType.equals("loadNextUser")) {
                    boolean isSubstituted = true;
                    JSONArray jsonArray = responseBody.getJSONArray("userlist");
                    if (jsonArray.length() != 0) {//사용자 데이터가 존재하는 경우
                        //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject data = jsonArray.getJSONObject(i);
                            SearchedPeopleItem searchedPeopleItem = new Gson().fromJson(data.toString(), SearchedPeopleItem.class);

                            //로딩바를 다음 페이지의 첫번째 게시물로 교체하기 위한 작업
                            if (isSubstituted) {
                                //프로그래스바 아이템을 게시물 아이템으로 교체해준다.
                                searchedPeopleItemArrayList.set(searchedPeopleItemArrayList.size() - 1, searchedPeopleItem);
                                //어댑터 notify
                                searchedPeopleAdapter.notifyItemChanged(searchedPeopleItemArrayList.size() - 1);
                                Log.d("사람검색 사이즈:", String.valueOf(searchedPeopleItemArrayList.size()));

                                isSubstituted = false;
                            }
                            //최초에 로딩바와 게시물이 교체가 끝났으면 그 뒤로는 게시물 추가만 해준다.
                            else {
                                searchedPeopleItemArrayList.add(searchedPeopleItem);
                                searchedPeopleAdapter.notifyItemInserted(searchedPeopleItemArrayList.size() - 1);
                            }

                            listSize += 1;
                            loadPossible = true;
                        }
                    } else {//더 이상 사용자 데이터가 없는 경우
                        loadPossible = true;
                        searchedPeopleItemArrayList.remove(searchedPeopleItemArrayList.size() - 1);
                        searchedPeopleAdapter.notifyItemRemoved(searchedPeopleItemArrayList.size());
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {//response가 없을 때
            Toast.makeText(getContext(), "문제가 생겼습니다", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("사람검색 프래그먼트 onResume", "호출");
        //데이터 변경을 계속 반영해주기 위해서 프래그먼트가 화면에 나타나면 반드시 타게 되어있는 onResume단계에 데이터를 계속 새롭게 가져오는
        //비동기 클래스를 넣어준다.
        //검색어가 없는 경우에는 전체 데이터를 가져오고
        if (searchText == null) {
            //여기에 팔로잉 전체 데이터를 가져와서 셋해주는 코딩
            getSearchedPeople(account, searchText, false, 0, listSize);
        }
        //검색어가 입력되어있던 상태였으면 검색어에 맞는 데이터를 가져온다.
        else {
            //여기에 팔로잉 전체 데이터를 가져와서 셋해주는 코딩
            getSearchedPeople(account, searchText, true, 0, listSize);
        }
    }


    @Override
    public void onListClicked(int position) {
        //게시물 번호를 번들에 담아서 프래그먼트에 전달해준다.
        Bundle bundle = new Bundle();
        bundle.putString("account", searchedPeopleItemArrayList.get(position).getAccount());
        bundle.putString("parentActivity", "SearchActivity");
        if (account.equals(searchedPeopleItemArrayList.get(position).getAccount())) {
            //나의 게시물을 true
            bundle.putBoolean("isMyPost", true);
        }
        //좋아요 리스트의 계정이 로그인한 사용자의 계정과 다르면
        else {
            //나의 게시물을 false
            bundle.putBoolean("isMyPost", false);
        }


        SearchActivity.accountPageFragment.setArguments(bundle);

        SearchActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, SearchActivity.accountPageFragment).show(SearchActivity.accountPageFragment).addToBackStack(null).commit();

        //키보드 종료
        SearchActivity.softKeyboard.closeSoftKeyboard();
    }
}
