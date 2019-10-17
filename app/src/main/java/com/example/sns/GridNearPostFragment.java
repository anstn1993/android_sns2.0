package com.example.sns;

import android.location.Location;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.example.sns.JoinActivity.IP_ADDRESS;
import static com.example.sns.LoginActivity.account;
import static com.example.sns.LoginActivity.httpURLConnection;
import static com.example.sns.SearchedPlaceListFragment.setDistance;


public class GridNearPostFragment extends Fragment implements GridPostAdapter.GridPostRecyclerViewListener, HttpRequest.OnHttpResponseListener {
    private String TAG = "GridNearPostFragment";
    //리사이클러뷰
   RecyclerView rv_gridpost;
    //리사이클러뷰 어댑터
   GridPostAdapter gridpostAdapter;
    //그리드뷰 레이아웃 매니저
   GridLayoutManager gridLayoutManager;

   //리사이클러뷰 아이템 arraylist
    ArrayList<PostItem> postItemArrayList;

    //프래그먼트 매니저
    FragmentManager fragmentManager;

    boolean isFirstLoad = false;


    //부모 액티비티
    String parentActivity;

    //직전에 로드된 게시물의 수
    int listSize;

    //현재 로드된 게시물의 수
    int currentCount;

    //검색된 장소의 위치
    Location searchedLocation;

    //검색된 장소 명
    String searchedAddress;
    //검색된 장소의 위도와 경도
    double latitude, longitude;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("onCreateView", "호출");

        ViewGroup rootView =(ViewGroup) inflater.inflate(R.layout.fragment_gridpost, container, false);

        listSize = 15;

        if(getArguments() != null){

            parentActivity = getArguments().getString("parentActivity");
            searchedAddress = getArguments().getString("address");
            latitude = getArguments().getDouble("latitude");
            longitude = getArguments().getDouble("longitude");
        }

        //검색된 장소의 위치 객체 선언
        searchedLocation = new Location("searchedLocation");
        //좌표 설정
        searchedLocation.setLatitude(latitude);
        searchedLocation.setLongitude(longitude);

        //리사이클러뷰 선언
        rv_gridpost = rootView.findViewById(R.id.recyclerview_grid);

        setRecyclerView();

        fragmentManager = getFragmentManager();


        //addOnScrollListener는 리사이클러뷰를 스크롤하는 순간을 캐치한다.->페이징 처리를 위해서 필요
        rv_gridpost.addOnScrollListener(new RecyclerView.OnScrollListener() {
            //리사이클러뷰가 스크롤된 후 콜백되는 메소드
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //현재 화면에서 보이는 첫번째 아이템의 index
                int firstVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                Log.d("현재 화면의 첫번째 index", String.valueOf(firstVisibleItemPosition));

                //현재 화면에서 완전하게 보이는 첫번째 아이템의 index
                int firstCompletelyVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                Log.d("완전하게 보이는 첫번째 index", String.valueOf(firstCompletelyVisibleItemPosition));

                //findLastVisibleItemPosition은 현재 화면에 보이는 뷰 중 가장 마지막 뷰의 position을 리턴해준다.
                //즉 이 변수는 현재 화면에 보이는 아이템 중 가장 마지막 아이템의 index를 담는다
                int lastVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                Log.d("현재 화면의 마지막 index", String.valueOf(lastVisibleItemPosition));

                //현재 화면에서 완전하게 보이는 마지막 아이템의 index
                int lastCompletelyVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                Log.d("완전하게 보이는 마지막 index", String.valueOf(lastCompletelyVisibleItemPosition));
                //이 변수는 전체 리사이클러뷰의 아이템 개수를 담는다.
                int totalCount = recyclerView.getAdapter().getItemCount();
                Log.d("전체 아이템 개수",String.valueOf(totalCount));
                //이 변수는 현재 화면에 보이는 아이템의 개수를 담는다.(내 경우에는 1~2를 왔다갔다 함)
                int visibleItemCount = recyclerView.getChildCount();
                Log.d("화면에 보여지는 아이템 개수", String.valueOf(visibleItemCount));

                //마지막으로 보이는 아이템의 index가 전체 아이템 수에서 1을 뺀 값과 같고 현재 화면에 보이는 아이템 수가 12개이며 마지막으로 완전히 보이는 아이템의 index가
                //전체 아이템 수에서 1을 뺀 값과 같을 때만 페이징을 실행한다.
                if((lastVisibleItemPosition == totalCount-1) && visibleItemCount == 12 && lastCompletelyVisibleItemPosition == totalCount-1){
                    Log.d("페이징 조건","부합");
                    //다음 페이지를 로드한다.
                    //param:현재 로드되어있는 데이터의 수(다음 페이지에 로드되어야 할 첫번째 게시물의 index)
//                    loadNextPage();
                }
            }
        });
        return rootView;
    }

    private void setRecyclerView(){

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

                if(viewType == gridpostAdapter.VIEW_PROGRESS){
                    return 3;
                }
                else {
                    return 1;
                }

            }
        });
        rv_gridpost.setLayoutManager(gridLayoutManager);
    }

    private void getPost(String account, int lastId, int listSize) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("requestType", "getPost");
            requestBody.put("account", account);
            requestBody.put("lastId", lastId);
            requestBody.put("listSize", listSize);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getnearpost.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    //게시물 리스트의 최하단으로 이동하면 페이징 처리를 위해서 실행될 메소드
    public void loadNextPage(){
        //null을 넣어서 뷰타입이 프로그래스 아이템이 추가되게 한다.
        postItemArrayList.add(null);
        gridpostAdapter.notifyItemInserted(postItemArrayList.size()-1);
        //핸들러를 통해서 2초 뒤에 해당 기능을 실행한다.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                LoadNextPage loadNextPage = new LoadNextPage();
                loadNextPage.execute(String.valueOf(currentCount), account, String.valueOf(15));


            }
        }, 1500);

    }
    //서버와 통신에 성공했을 때 호출되는 콜백 메소드(HttpRequest클래스(AsyncTask클래스 상속)의 onPostExecute()메소드에서 호출)
    //모든 통신에 대한 결과는 이 콜백 메소드에서 처리해준다.
    @Override
    public void onHttpResponse(String result) {
        Log.d(TAG, "통신 성공");
        Log.d(TAG, "서버에서 넘어온 json데이터- " + result);
        if(result != null) {
            try {
                JSONObject responseBody = new JSONObject(result);
                String requestType = responseBody.getString("requestType");
                if(requestType.equals("getPost")) {
                    postItemArrayList.clear();
                    currentCount = 0;
                    //jsonarray를 선언해서
                    JSONArray jsonArray = responseBody.getJSONArray("post");
                    Log.d("게시물 수", String.valueOf(jsonArray.length()));

                    //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                    for(int i=0; i<jsonArray.length(); i++){
                        JSONObject data = jsonArray.getJSONObject(i);
                        PostItem postItem = new Gson().fromJson(data.toString(), PostItem.class);
                        //서버로부터 넘어온 게시물의 위치 객체(검색된 게시물의 위치와 거리 계산을 위해서 선언했다.)
                        Location compareLocation = new Location("compareLocation");
                        compareLocation.setLatitude(Double.parseDouble(postItem.latitude));
                        compareLocation.setLongitude(Double.parseDouble(postItem.longitude));
                        //검색된 게시물과 서버에서 넘어온 게시물 사이의 거리(단위:미터)
                        //둘 사이의 거리가 0이 아니고 5km(5000)이하인 경우에만 주변 게시물로 간주한다.
                        double distance = searchedLocation.distanceTo(compareLocation);
                        Log.d("검색된 게시물과의 거리", String.valueOf(distance));

                        if(distance > 0 && distance <= setDistance && !searchedAddress.equals(postItem.address)){
                            //모든 데이터를 다 담았으면 이제 그 객체를 리사이클러뷰 어레이리스트에 추가해준다.
                            postItemArrayList.add(postItem);
                            //현재 로르되어있는 게시물 +1
                            currentCount+=1;
                        }
                    }

                    //arraylist가 모두 추가됐으면 어댑터 notify
                    gridpostAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(getContext(), "문제가 생겼습니다", Toast.LENGTH_SHORT).show();
        }
    }//end of onHttpResponse

    //다음 페이지 게시물 데이터를 서버에서 가져온 후 리사이클러뷰 화면에 뿌려주는 async클래스
    public class LoadNextPage extends AsyncTask<String, Void, String>{
        boolean isSubstituted = true;

        String connectURL = "http://"+IP_ADDRESS+"/getnearpost.php";
        String postParameters=null;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        @Override
        protected String doInBackground(String...params) {


            postParameters = "currentLastId="+params[0]+"&account="+params[2]+"&listSize="+params[3];


            try {
                URL url = new URL(connectURL);
                //http통신 세팅
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setReadTimeout(0);
                httpURLConnection.setConnectTimeout(0);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                //통신 시작
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                //속도를 올리기 위해서 buffer클래스로 감쌈
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                //스트림에 사용자에게 입력받은 데이터를 써준다.
                bufferedOutputStream.write(postParameters.getBytes("UTF-8"));
                //스트림을 비워준다.
                bufferedOutputStream.flush();
                //스트림 종료
                bufferedOutputStream.close();

                InputStream inputStream;

                //response코드를 변수(200, 500, 404...)
                int responseStatusCode = httpURLConnection.getResponseCode();

                //통신이 잘 된 경우
                if(responseStatusCode == httpURLConnection.HTTP_OK){
                    inputStream = httpURLConnection.getInputStream();

                }else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                //버퍼 리더로 감싸서 읽어들이는 속도를 올려준다
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                //스트링빌더를 통해서 서버로부터 넘어온 데이터를 하나하나 추가해준다. 통신에 성공할 시 json데이터가 들어간다.
                StringBuilder sb = new StringBuilder();
                String line = null;

                //데이터를 sb객체에 계속 넣어준다.
                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                //읽어들이기 종료
                bufferedReader.close();

                return sb.toString().trim();

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("통신 에러", e.getMessage());
                return null;
            }
        }



        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //서버로부터 값이 넘어온 경우
            if(result != null){
                Log.d("서버로부터 넘어온 데이터", result);
                try{
                    //json오브잭트를 선언하고
                    JSONObject jsonObject = new JSONObject(result);
                    //jsonarray를 선언해서
                    JSONArray jsonArray = jsonObject.getJSONArray("post");
                    Log.d("게시물 수", String.valueOf(jsonArray.length()));

                    //반복문을 돌려서 계속 arraylist에 넣어주고 어댑터에 notify
                    for(int i=0; i<jsonArray.length(); i++){
                        JSONObject data = jsonArray.getJSONObject(i);
                        //서버로부터 넘어온 데이터를 변수에 정의
                        PostItem postItem = new Gson().fromJson(data.toString(), PostItem.class);

                        //로딩바를 다음 페이지의 첫번째 게시물로 교체하기 위한 작업
                        if(isSubstituted){
                            //프로그래스바 아이템을 게시물 아이템으로 교체해준다.
                            postItemArrayList.set(postItemArrayList.size()-1, postItem);


                            //어댑터 notify
                            gridpostAdapter.notifyItemChanged(postItemArrayList.size()-1);
                            Log.d("게시물 사이즈:", String.valueOf(postItemArrayList.size()));

                            isSubstituted = false;
                        }
                        //최초에 로딩바와 게시물이 교체가 끝났으면 그 뒤로는 게시물 추가만 해준다.
                        else {
                            postItemArrayList.add(postItem);
                            gridpostAdapter.notifyItemInserted(postItemArrayList.size()-1);
                        }

                        //현재 로드되어있는 게시물 수 +1
                        currentCount += 1;
                        listSize += 1;

                    }


                }catch (Exception e){
                    Log.d("데이터 셋 오류", e.getMessage());
                    postItemArrayList.remove(postItemArrayList.size()-1);
                    gridpostAdapter.notifyItemRemoved(postItemArrayList.size());
                }
            }
            //서버로부터 값이 넘어오지 않은 경우
            else {
                Log.d("서버로부터 넘어온 데이터", "null");

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPost(account, 0, listSize);
    }

    //그리드 이미지 클릭 콜백 매소드
    @Override
    public void onGridPictureClicked(int position) {

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
        } else if (parentActivity.equals("SearchActivity")) {
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
        } else if (parentActivity.equals("MyPageActivity")) {
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
        } else {
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
    }//end of onGridPictureClicked
}
