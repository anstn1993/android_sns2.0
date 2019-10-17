package com.example.sns;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class SearchActivity extends AppCompatActivity {

    EditText et_search;

    Button btn_searchPeople, btn_searchTag, btn_searchPlace;

    //SearchActivity위에 올려질 프래그먼트 전체를 관리할 프래그먼트 매니저
    public static FragmentManager fragmentManager;
    //SearchActivity위에 올려질 프래그먼트들
    public static LikeListFragment likeListFragment;
    public static AccountPageFragment accountPageFragment;
    public static PostDetailFragment postDetailFragment;
    public static FollowingListFragment followingListFragment;
    public static FollowerListFragment followerListFragment;
    public static HashTagPostListFragment hashTagPostListFragment;
    public static SearchedPlaceListFragment searchedPlaceListFragment;

    //SearchActivity내에서 교체될 검색 리스트를 관리할 프래그먼트 매니저
    FragmentManager childFragmentManager;

    //SearcheActivity내에서 교체될 검색 리스트 프래그먼트들
    SearchedPeopleFragment searchedPeopleFragment;
    SearchedTagFragment searchedTagFragment;
    SearchedPlaceFragment searchedPlaceFragment;

    public static SoftKeyboard softKeyboard;


    //SearchActivity에서 검색되는 순간 각각의 프래그먼트에서 검색 값을 받아서 처리해야하기 때문에 interface를 통해서 검색 값을 넘겨준다.
    interface SearchedPeopleListener {
        void isSearched(String searchText);
    }

    interface SearchedTagListener {
        void isSearched(String searchText);
    }

    interface SearchedPlaceListener {
        void isSearched(String searchText);
    }

    SearchedPeopleListener searchedPeopleListener;
    SearchedTagListener searchedTagListener;
    SearchedPlaceListener searchedPlaceListener;

    public void setOnSearchedPeopleListener(SearchedPeopleListener listener) {
        this.searchedPeopleListener = listener;
    }


    public void setOnSearchedTagListener(SearchedTagListener listener) {
        this.searchedTagListener = listener;
    }

    public void setOnSearchedPlaceListener(SearchedPlaceListener listener) {
        this.searchedPlaceListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        //검색창 edittext
        et_search = findViewById(R.id.edittext_search);

        //사람 검색 버튼
        btn_searchPeople = findViewById(R.id.button_searchpeople);
        btn_searchPeople.setTextColor(Color.parseColor("#0092EF"));
        //태그 검색 버튼
        btn_searchTag = findViewById(R.id.button_searchtag);
        //장소 검색 버튼
        btn_searchPlace = findViewById(R.id.button_searchplace);

        //초기에 검색 창에 포커스가 가 있는데 포커스를 지워준다.
        et_search.clearFocus();

        searchedPlaceListener = new SearchedPlaceListener() {
            @Override
            public void isSearched(String searchText) {

            }
        };

        ConstraintLayout constraintLayout = findViewById(R.id.constraintlayout_search);

        //키보드 제어 객체 선언
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
        softKeyboard = new SoftKeyboard(constraintLayout, inputMethodManager);

        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {

            }

            @Override
            public void onSoftKeyboardShow() {

            }
        });

        fragmentManager = getSupportFragmentManager();
        likeListFragment = new LikeListFragment();
        accountPageFragment = new AccountPageFragment();
        postDetailFragment = new PostDetailFragment();
        followingListFragment = new FollowingListFragment();
        followerListFragment = new FollowerListFragment();
        hashTagPostListFragment = new HashTagPostListFragment();
        searchedPlaceListFragment = new SearchedPlaceListFragment();

        childFragmentManager = getSupportFragmentManager();

        searchedPeopleFragment = new SearchedPeopleFragment();
        searchedTagFragment = new SearchedTagFragment();
        searchedPlaceFragment = new SearchedPlaceFragment();


        childFragmentManager.beginTransaction().add(R.id.frame_container, searchedPeopleFragment).commit();
        childFragmentManager.beginTransaction().add(R.id.frame_container, searchedTagFragment).commit();
        childFragmentManager.beginTransaction().add(R.id.frame_container, searchedPlaceFragment).commit();

        childFragmentManager.beginTransaction().hide(searchedTagFragment).commit();
        childFragmentManager.beginTransaction().hide(searchedPlaceFragment).commit();
        childFragmentManager.beginTransaction().show(searchedPeopleFragment).commit();

        //사람 검색 버튼 클릭 리스너
        btn_searchPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SearchActivity.this, EmptyActivity.class);
                startActivity(intent);
                //태그, 장소 검색 리스트 프래그먼트를 숨기고 사람 검색 프래그먼트를 show
                childFragmentManager.beginTransaction().hide(searchedTagFragment).commit();
                childFragmentManager.beginTransaction().hide(searchedPlaceFragment).commit();
                childFragmentManager.beginTransaction().show(searchedPeopleFragment).commit();
                et_search.setText("");
                et_search.setHint("사람 검색");
                btn_searchPeople.setTextColor(Color.parseColor("#0092EF"));
                btn_searchPlace.setTextColor(Color.parseColor("#000000"));
                btn_searchTag.setTextColor(Color.parseColor("#000000"));

            }
        });

        //태그 검색 버튼 클릭 리스너
        btn_searchTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, EmptyActivity.class);
                startActivity(intent);
                //태그, 장소 검색 리스트 프래그먼트를 숨기고 사람 검색 프래그먼트를 show
                childFragmentManager.beginTransaction().hide(searchedPlaceFragment).commit();
                childFragmentManager.beginTransaction().hide(searchedPeopleFragment).commit();
                childFragmentManager.beginTransaction().show(searchedTagFragment).commit();
                et_search.setText("");
                et_search.setHint("태그 검색");
                btn_searchPeople.setTextColor(Color.parseColor("#000000"));
                btn_searchPlace.setTextColor(Color.parseColor("#000000"));
                btn_searchTag.setTextColor(Color.parseColor("#0092EF"));
            }
        });

        //장소 검색 버튼 클릭 리스너
        btn_searchPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, EmptyActivity.class);
                startActivity(intent);
                //태그, 장소 검색 리스트 프래그먼트를 숨기고 사람 검색 프래그먼트를 show
                childFragmentManager.beginTransaction().hide(searchedPeopleFragment).commit();
                childFragmentManager.beginTransaction().hide(searchedTagFragment).commit();
                childFragmentManager.beginTransaction().show(searchedPlaceFragment).commit();
                et_search.setText("");
                et_search.setHint("장소 검색");
                btn_searchPeople.setTextColor(Color.parseColor("#000000"));
                btn_searchPlace.setTextColor(Color.parseColor("#0092EF"));
                btn_searchTag.setTextColor(Color.parseColor("#000000"));
            }
        });

        et_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                softKeyboard.openSoftKeyboard();
            }
        });

        //검색창 텍스트 추가 리스너
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchText = et_search.getText().toString();
                //현재 검색 결과 프래그먼트가 사람 검색 프래그먼트인 경우
                if (searchedPeopleFragment.isVisible()) {
                    Log.d("사람 검색", "호출");
                    //검색어를 사람 검색 프래그먼트로 넘겨준다.
                    searchedPeopleFragment.isSearched(searchText);
                }
                //현재 검색 결과 프래그먼트가 태그 검색 프래그먼트인 경우
                else if (searchedTagFragment.isVisible()) {
                    Log.d("태그 검색", "호출");
                    //검색어를 태그 검색 프래그먼트로 넘겨준다.
                    searchedTagFragment.isSearched(searchText);
                }
                //현재 검색 결과 프래그먼트가 장소 검색 프래그먼트인 경우
                else {
                    Log.d("장소 검색", "호출");
                    //검색어를 장소 검색 프래그먼트로 넘겨준다.
                    searchedPlaceFragment.isSearched(searchText);
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        //프래그먼트 스택에 프래그먼트가 존재하면
        if (SearchActivity.fragmentManager.getBackStackEntryCount() != 0) {
            Log.d("프래그먼트 스택 존재", "호출");
            //스택의 최상단에 있는 프래그먼트를 화면에 붙이고 그 전에 붙어있던 fragment는 destroy.
            SearchActivity.fragmentManager.popBackStackImmediate();
            //프래그먼트가 onResume을 타게 하기 위해서 호출되자마자 종료되는 액티비티로 인텐트롤 해준다.
            Intent intent = new Intent(getApplicationContext(), EmptyActivity.class);
            startActivity(intent);
        }
        //현재 화면에 액티비티라면 PostActivity로 이동.
        else {
//            super.onBackPressed();
            MainActivity.tabHost.setCurrentTab(0);
        }
    }
}
