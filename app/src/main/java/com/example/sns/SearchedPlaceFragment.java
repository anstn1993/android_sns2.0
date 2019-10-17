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

public class SearchedPlaceFragment extends Fragment implements SearchActivity.SearchedPlaceListener, SearchedPlaceAdapter.SearchedPlaceRecyclerViewListener, HttpRequest.OnHttpResponseListener {

    RecyclerView rv_searchedPlace;
    SearchedPlaceAdapter searchedPlaceAdapter;
    LinearLayoutManager linearLayoutManager;
    ArrayList<SearchedPlaceItem> searchedPlaceItemArrayList;


    SearchActivity searchActivity;

    //검색어
    String searchText;
    //직전에 로드된 리스트 수
    int listSize;
    //현재 로드된 장소 수
    int currentCount;

    boolean loadPossible = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_searchedplace, container, false);

        listSize = 20;

        //장소 리스트 리사이클러뷰
        rv_searchedPlace = rootView.findViewById(R.id.recyclerview_searchedplace);

        //SearchActivity의 인터페이스 리스너를 이 프래그먼트로 설정하기 위해서 SearchActivity객체를 선언한다.
        searchActivity = new SearchActivity();
        searchActivity.setOnSearchedPlaceListener(this);

        //리사이클러뷰 셋
        setRecyclerView();

        //addOnScrollListener는 리사이클러뷰를 스크롤하는 순간을 캐치한다.
        rv_searchedPlace.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        searchedPlaceItemArrayList = new ArrayList<>();

        rv_searchedPlace.setHasFixedSize(true);

        //레이아웃 메니저 설정
        linearLayoutManager = new LinearLayoutManager(getContext());
        rv_searchedPlace.setLayoutManager(linearLayoutManager);

        //어댑터 설정
        searchedPlaceAdapter = new SearchedPlaceAdapter(searchedPlaceItemArrayList, getContext());
        searchedPlaceAdapter.setOnClickListener(this);
        rv_searchedPlace.setAdapter(searchedPlaceAdapter);
    }


    @Override
    public void isSearched(String searchText) {
        Log.d("장소 검색 isSearched", "호출");
        Log.d("검색어", searchText);
        if (searchText.length() == 0) {
            Log.d("검색어", "없음");
            this.searchText = searchText;
            getSearchedPlace(searchText, false, 0, listSize);
        } else {
            Log.d("검색어", searchText);
            this.searchText = searchText;
            getSearchedPlace(searchText, true, 0, 20);
        }
    }

    //장소 데이터를 가져오는 메소드
    private void getSearchedPlace(String searchText, boolean isSearched, int lastId, int listSize) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("requestType", "getPlace");
            requestBody.put("searchText", searchText);
            requestBody.put("isSearched", isSearched);
            requestBody.put("lastId", lastId);
            requestBody.put("listSize", listSize);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getplace.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //페이징 메소드
    public void loadNextPage() {
        loadPossible = false;
        //null을 넣어서 뷰타입이 프로그래스 아이템이 추가되게 한다.
        searchedPlaceItemArrayList.add(null);
        searchedPlaceAdapter.notifyItemInserted(searchedPlaceItemArrayList.size() - 1);
        //핸들러를 통해서 2초 뒤에 해당 기능을 실행한다.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("requestType", "loadNextPlace");
                    requestBody.put("searchText", searchText);
                    requestBody.put("lastId", currentCount);
                    requestBody.put("listSize", 20);
                    if (searchText == null) {
                        requestBody.put("isSearched", false);
                    } else {
                        requestBody.put("isSearched", true);
                    }
                    HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getplace.php", SearchedPlaceFragment.this::onHttpResponse);
                    httpRequest.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 1500);

    }

    //서버와 통신에 성공했을 때 호출되는 콜백 메소드(HttpRequest클래스(AsyncTask클래스 상속)의 onPostExecute()메소드에서 호출)
    //모든 통신에 대한 결과는 이 콜백 메소드에서 처리해준다.
    @Override
    public void onHttpResponse(String result) {
        if (result != null) {
            try {
                JSONObject responseBody = new JSONObject(result);
                String requestType = responseBody.getString("requestType");
                if (requestType.equals("getPlace")) {//장소 데이터를 가져오는 통신
                    searchedPlaceItemArrayList.clear();
                    currentCount = 0;
                    //json오브잭트를 선언하고
                    JSONObject jsonObject = new JSONObject(result);
                    //jsonarray를 선언해서
                    JSONArray jsonArray = jsonObject.getJSONArray("placeList");
                    Log.d("게시물 수", String.valueOf(jsonArray.length()));

                    //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        SearchedPlaceItem searchedPlaceItem = new Gson().fromJson(data.toString(), SearchedPlaceItem.class);
                        searchedPlaceItemArrayList.add(searchedPlaceItem);
                        currentCount += 1;
                    }

                    searchedPlaceAdapter.notifyDataSetChanged();


                } else if (requestType.equals("loadNextPlace")) {//페이징 처리를 하는 통신
                    boolean isSubstituted = true;
                    JSONArray jsonArray = responseBody.getJSONArray("placeList");
                    if(jsonArray.length() != 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject data = jsonArray.getJSONObject(i);
                            SearchedPlaceItem searchedPlaceItem = new Gson().fromJson(data.toString(), SearchedPlaceItem.class);

                            //로딩바를 다음 페이지의 첫번째 게시물로 교체하기 위한 작업
                            if (isSubstituted) {
                                Log.d("프로그레스바 삭제", "호출");
                                //프로그래스바 아이템을 게시물 아이템으로 교체해준다.
                                searchedPlaceItemArrayList.set(searchedPlaceItemArrayList.size() - 1, searchedPlaceItem);


                                //어댑터 notify
                                searchedPlaceAdapter.notifyItemChanged(searchedPlaceItemArrayList.size() - 1);
                                Log.d("사람검색 사이즈:", String.valueOf(searchedPlaceItemArrayList.size()));

                                isSubstituted = false;
                            }
                            //최초에 로딩바와 게시물이 교체가 끝났으면 그 뒤로는 게시물 추가만 해준다.
                            else {
                                searchedPlaceItemArrayList.add(searchedPlaceItem);
                                searchedPlaceAdapter.notifyItemInserted(searchedPlaceItemArrayList.size() - 1);
                            }

                            listSize += 1;
                            currentCount += 1;
                            loadPossible = true;
                        }
                    }
                    else {
                        loadPossible = true;
                        searchedPlaceItemArrayList.remove(searchedPlaceItemArrayList.size() - 1);
                        searchedPlaceAdapter.notifyItemRemoved(searchedPlaceItemArrayList.size());
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
            getSearchedPlace(searchText, false, 0, listSize);
        }
        //검색어가 입력되어있던 상태였으면 검색어에 맞는 데이터를 가져온다.
        else {
            //여기에 팔로잉 전체 데이터를 가져와서 셋해주는 코딩
            getSearchedPlace(searchText, false, 0, 20);
        }
    }

    @Override
    public void onListClicked(int position) {
        SearchedPlaceListFragment searchedPlaceListFragment = new SearchedPlaceListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("address", searchedPlaceItemArrayList.get(position).address);
        bundle.putDouble("latitude", searchedPlaceItemArrayList.get(position).latitude);
        bundle.putDouble("longitude", searchedPlaceItemArrayList.get(position).longitude);
        bundle.putString("parentActivity", "SearchActivity");
        searchedPlaceListFragment.setArguments(bundle);
        //해시태그 리스트 프래그먼트로 바꿔준다.
        SearchActivity.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right).add(R.id.frame_parent_container, searchedPlaceListFragment).addToBackStack(null).commit();
        SearchActivity.fragmentManager.beginTransaction().show(searchedPlaceListFragment).commit();

        //키보드 종료
        SearchActivity.softKeyboard.closeSoftKeyboard();
    }
}
