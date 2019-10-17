package com.example.sns;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.sns.EditPostFirstActivity.postNum;
import static com.example.sns.LoginActivity.account;
import static com.example.sns.LoginActivity.httpURLConnection;
import static com.example.sns.UploadFirstActivity.imageArrayList;

public class EditPostSecondActivity extends AppCompatActivity {

    Button btn_back, btn_edit;
    EditText et_article;
    //해시태그 라이브러리 클래스
    HashTagHelper hashTagHelper;


    TextView tv_location;

    ViewPager viewPager;
    //게시글 담는 변수
    String article = null;

    //위치 추가로 가져온 장소의 주소
    private String address = null;
    //위치 추가로 가져온 장소의 위도와 경도
    private double latitude;
    private double longitude;


    //넘어온 이미지가 url인지, uri인지를 담는 list
    private ArrayList<String> imageRootArrayList;

    //서버로 전송시킬 이미지 파일 객체 선언
    File file1, file2, file3, file4, file5, file6 = null;

    //서버로 전송시킬 이미지 파일 객체 선언
    private ArrayList<File> imageFileArrayList;

    //이미지 파일의 경로를 담는 파일
    String imagePath = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post_second);

        btn_edit = findViewById(R.id.btn_edit);
        btn_back = findViewById(R.id.btn_back);

        et_article = findViewById(R.id.et_article);

        //hashtag어댑터를 초기화해주고
        hashTagHelper = HashTagHelper.Creator.create(Color.parseColor("#02B2ED"), new HashTagHelper.OnHashTagClickListener() {
            @Override
            public void onHashTagClicked(String hashTag) {

            }
        });


        //해시태그를 적용하고자 하는 텍스트 영역을 handle해준다.
        hashTagHelper.handle(et_article);

        tv_location = findViewById(R.id.tv_location);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        //뷰 페이저를 어댑터와 연결해서 보여준다.
        viewPager.setAdapter(new UploadImageViewPagerAdapter(getSupportFragmentManager()));

        //하단의 탭 설정
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager, true);


        //수정 전 게시글과 위치가 존재했다면 화면에 세팅하고 변수에 담아주는 메소드
        setArticleAddress();

        //이미지의 소스 타입 설정 메소드
        setImageRoot();


        //위치 추가 버튼 클릭 리스너
        tv_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddLocationActivity.class);
                startActivityForResult(intent, 0);

            }
        });


        //이 코드는 잘못된 코드로 교훈삼아 남겨둔다. 파일 어레이에 파일을 추가하는 반복문인데 asynctask는 비동기 작업이기 때문에
        //내가 원하는 순서대로 어레이에 추가가 되지 않고 뒤죽박죽으로 추가될 가능성이 있기 때문에 어레이에 추가를 할 거면 반드시
        //asynctask안에서 함께 처리하는 방식으로 구현을 해야 한다.
//        for(int i =0; i<imageRootArrayList.size(); i++){
//            if(imageRootArrayList.get(i).equals("uri")){
//                imageFileArrayList.add(new File(Uri.parse(String.valueOf(imageArrayList.get(i))).getPath()));
//
//            }else{
//                UrlImageToFile urlImageToFile = new UrlImageToFile();
//                urlImageToFile.execute(String.valueOf(imageArrayList.get(i)));
//            }
//
//        }
        //이미지 파일 객체 생성
        imageFileArrayList = new ArrayList<>();
        for (int i = 0; i < imageRootArrayList.size(); i++) {
            UrlImageToFile urlImageToFile = new UrlImageToFile();
            urlImageToFile.execute(String.valueOf(imageArrayList.get(i)), imageRootArrayList.get(i), String.valueOf(i));
        }

        //수정 버튼 클릭 리스너
        //imageArrayList 클리어
        //glide 클리어
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                article = et_article.getText().toString();

                upload(article, imageArrayList, address, latitude, longitude);

            }
        });

        //뒤로 버튼 클릭 리스너 눌렀을 때 동작
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageArrayList.clear();

                for (int i = 0; i < imageFileArrayList.size(); i++) {
                    if (imageRootArrayList.get(i).equals("url")) {
                        Log.d("삭제된 이미지 경로", String.valueOf(imageFileArrayList.get(i)));
                        imageFileArrayList.get(i).delete();

                    }

                }

                finish();
                return;
            }
        });

    }

    //startactivityforresult를 통해서 인텐트한 액티비티에서 전달된 값을 처리해주는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //위치 추가를 위한 인텐트 리퀘스트 번호:0
        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            address = data.getStringExtra("address");
            Log.d("넘어온 주소", address);
            latitude = data.getDoubleExtra("latitude", 0);
            Log.d("넘어온 위도", String.valueOf(latitude));
            longitude = data.getDoubleExtra("longitude", 0);
            Log.d("넘어온 경도", String.valueOf(longitude));
            tv_location.setText(address);
        }
    }

    //게시물 수정 버튼을 누를 때 서버로 데이터를 넘겨주는 메소드
    private void upload(
            String article,
            ArrayList imageArraylist,
            String address,
            double latitude,
            double longitude) {

        //서버와 통신이 이루어질 동안 보일 로딩 창
        ProgressDialog progressDialog = new ProgressDialog(EditPostSecondActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show(EditPostSecondActivity.this, "게시물 수정", "잠시만 기다려주세요.", true, false);


        //레트로핏 세팅
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://13.124.105.47/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Log.d("아이디", account);


        //서버로 보내줄 데이터 param 설정
        RequestBody postNumPart = RequestBody.create(MultipartBody.FORM, String.valueOf(postNum));
        RequestBody accountPart = RequestBody.create(MultipartBody.FORM, account);
        //초기화를 바로 해주지 않는 이유는 이 데이터들은 쓰일 수도 있고 안 쓰일 수도 있기 때문이다.
        RequestBody articlePart;
        RequestBody addressPart;
        RequestBody latitudePart;
        RequestBody longitudePart;

        //이미지1
        RequestBody imageFile1;
        MultipartBody.Part body1;

        //이미지2
        RequestBody imageFile2;
        MultipartBody.Part body2;

        //이미지3
        RequestBody imageFile3;
        MultipartBody.Part body3;

        //이미지4
        RequestBody imageFile4;
        MultipartBody.Part body4;

        //이미지5
        RequestBody imageFile5;
        MultipartBody.Part body5;

        //이미지6
        RequestBody imageFile6;
        MultipartBody.Part body6;

        //레트로핏 인터페이스 설정
        RetrofitService retrofitService;
        Call<UploadResponse> call;

        //이미지 개수를 담는 변수
        int imageCount = imageArraylist.size();

        //사진 1장 선택했을 때
        if (imageCount == 1) {
            //이미지 파일의 이름 설정
            String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName = account + timeStamp + "1.jpg";

            file1 = imageFileArrayList.get(0);

            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file1);

            body1 = MultipartBody.Part.createFormData("image1", imageFileName, imageFile1);


            //게시글x 위치x
            if (article == null && address == null) {

                retrofitService = retrofit.create(RetrofitService.class);
                //인터페이스의 uploadResponse 메소드 실행
                call = retrofitService.editResponse(postNumPart, accountPart, null, body1, null, null, null, null, null, null, null, null);

            }
            //게시글o, 위치x
            else if (article != null && address == null) {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, articlePart, body1, null, null, null, null, null, null, null, null);

            }
            //게시글x, 위치o
            else if (article == null && address != null) {
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));


                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, null, body1, null, null, null, null, null, addressPart, latitudePart, longitudePart);

            }
            //게시글o, 위치o
            else {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, articlePart, body1, null, null, null, null, null, addressPart, latitudePart, longitudePart);

            }

        }
        //사진 2장 선택했을 때
        else if (imageCount == 2) {
            //이미지 파일1의 이름
            String timeStamp1 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName1 = account + timeStamp1 + "1.jpg";

            //이미지 파일2의 이름
            String timeStamp2 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName2 = account + timeStamp2 + "2.jpg";

            file1 = imageFileArrayList.get(0);
            file2 = imageFileArrayList.get(1);

            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
            imageFile2 = RequestBody.create(MediaType.parse("multipart/form-data"), file2);


            body1 = MultipartBody.Part.createFormData("image1", imageFileName1, imageFile1);
            body2 = MultipartBody.Part.createFormData("image2", imageFileName2, imageFile2);


            //게시글x 위치x
            if (article == null && address == null) {

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, null, body1, body2, null, null, null, null, null, null, null);

            }
            //게시글o, 위치x
            else if (article != null && address == null) {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, articlePart, body1, body2, null, null, null, null, null, null, null);

            }
            //게시글x, 위치o
            else if (article == null && address != null) {
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));


                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, null, body1, body2, null, null, null, null, addressPart, latitudePart, longitudePart);

            }
            //게시글o, 위치o
            else {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, articlePart, body1, body2, null, null, null, null, addressPart, latitudePart, longitudePart);

            }
        }
        //사진 3장 선택했을 때
        else if (imageCount == 3) {

            String timeStamp1 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName1 = account + timeStamp1 + "1.jpg";

            String timeStamp2 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName2 = account + timeStamp2 + "2.jpg";

            String timeStamp3 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName3 = account + timeStamp3 + "3.jpg";


            file1 = imageFileArrayList.get(0);
            file2 = imageFileArrayList.get(1);
            file3 = imageFileArrayList.get(2);

            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
            imageFile2 = RequestBody.create(MediaType.parse("multipart/form-data"), file2);
            imageFile3 = RequestBody.create(MediaType.parse("multipart/form-data"), file3);


            body1 = MultipartBody.Part.createFormData("image1", imageFileName1, imageFile1);
            body2 = MultipartBody.Part.createFormData("image2", imageFileName2, imageFile2);
            body3 = MultipartBody.Part.createFormData("image3", imageFileName3, imageFile3);


            //게시글x 위치x
            if (article == null && address == null) {

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, null, body1, body2, body3, null, null, null, null, null, null);

            }
            //게시글o, 위치x
            else if (article != null && address == null) {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, articlePart, body1, body2, body3, null, null, null, null, null, null);

            }
            //게시글x, 위치o
            else if (article == null && address != null) {
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));


                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, null, body1, body2, body3, null, null, null, addressPart, latitudePart, longitudePart);

            }
            //게시글o, 위치o
            else {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, articlePart, body1, body2, body3, null, null, null, addressPart, latitudePart, longitudePart);

            }
        }
        //사진4장 선택했을 때
        else if (imageCount == 4) {
            String timeStamp1 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName1 = account + timeStamp1 + "1.jpg";

            String timeStamp2 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName2 = account + timeStamp2 + "2.jpg";

            String timeStamp3 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName3 = account + timeStamp3 + "3.jpg";

            String timeStamp4 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName4 = account + timeStamp4 + "4.jpg";

            file1 = imageFileArrayList.get(0);
            file2 = imageFileArrayList.get(1);
            file3 = imageFileArrayList.get(2);
            file4 = imageFileArrayList.get(3);

            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
            imageFile2 = RequestBody.create(MediaType.parse("multipart/form-data"), file2);
            imageFile3 = RequestBody.create(MediaType.parse("multipart/form-data"), file3);
            imageFile4 = RequestBody.create(MediaType.parse("multipart/form-data"), file4);


            body1 = MultipartBody.Part.createFormData("image1", imageFileName1, imageFile1);
            body2 = MultipartBody.Part.createFormData("image2", imageFileName2, imageFile2);
            body3 = MultipartBody.Part.createFormData("image3", imageFileName3, imageFile3);
            body4 = MultipartBody.Part.createFormData("image4", imageFileName4, imageFile4);


            //게시글x 위치x
            if (article == null && address == null) {

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, null, body1, body2, body3, body4, null, null, null, null, null);

            }
            //게시글o, 위치x
            else if (article != null && address == null) {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, articlePart, body1, body2, body3, body4, null, null, null, null, null);

            }
            //게시글x, 위치o
            else if (article == null && address != null) {
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));


                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, null, body1, body2, body3, body4, null, null, addressPart, latitudePart, longitudePart);

            }
            //게시글o, 위치o
            else {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, articlePart, body1, body2, body3, body4, null, null, addressPart, latitudePart, longitudePart);

            }
        }
        //사진 5장 선택했을 때
        else if (imageCount == 5) {
            String timeStamp1 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName1 = account + timeStamp1 + "1.jpg";

            String timeStamp2 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName2 = account + timeStamp2 + "2.jpg";

            String timeStamp3 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName3 = account + timeStamp3 + "3.jpg";

            String timeStamp4 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName4 = account + timeStamp4 + "4.jpg";

            String timeStamp5 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName5 = account + timeStamp5 + "5.jpg";


            file1 = imageFileArrayList.get(0);
            file2 = imageFileArrayList.get(1);
            file3 = imageFileArrayList.get(2);
            file4 = imageFileArrayList.get(3);
            file5 = imageFileArrayList.get(4);

            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
            imageFile2 = RequestBody.create(MediaType.parse("multipart/form-data"), file2);
            imageFile3 = RequestBody.create(MediaType.parse("multipart/form-data"), file3);
            imageFile4 = RequestBody.create(MediaType.parse("multipart/form-data"), file4);
            imageFile5 = RequestBody.create(MediaType.parse("multipart/form-data"), file5);


            body1 = MultipartBody.Part.createFormData("image1", imageFileName1, imageFile1);
            body2 = MultipartBody.Part.createFormData("image2", imageFileName2, imageFile2);
            body3 = MultipartBody.Part.createFormData("image3", imageFileName3, imageFile3);
            body4 = MultipartBody.Part.createFormData("image4", imageFileName4, imageFile4);
            body5 = MultipartBody.Part.createFormData("image5", imageFileName5, imageFile5);

            //게시글x 위치x
            if (article == null && address == null) {

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, null, body1, body2, body3, body4, body5, null, null, null, null);

            }
            //게시글o, 위치x
            else if (article != null && address == null) {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, articlePart, body1, body2, body3, body4, body5, null, null, null, null);

            }
            //게시글x, 위치o
            else if (article == null && address != null) {
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));


                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, null, body1, body2, body3, body4, body5, null, addressPart, latitudePart, longitudePart);

            }
            //게시글o, 위치o
            else {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, articlePart, body1, body2, body3, body4, body5, null, addressPart, latitudePart, longitudePart);

            }
        }
        //사진 6장 선택했을 때
        else {


            String timeStamp1 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName1 = account + timeStamp1 + "1.jpg";

            String timeStamp2 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName2 = account + timeStamp2 + "2.jpg";

            String timeStamp3 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName3 = account + timeStamp3 + "3.jpg";

            String timeStamp4 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName4 = account + timeStamp4 + "4.jpg";

            String timeStamp5 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName5 = account + timeStamp5 + "5.jpg";

            String timeStamp6 = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName6 = account + timeStamp6 + "6.jpg";


            file1 = imageFileArrayList.get(0);
            file2 = imageFileArrayList.get(1);
            file3 = imageFileArrayList.get(2);
            file4 = imageFileArrayList.get(3);
            file5 = imageFileArrayList.get(4);
            file6 = imageFileArrayList.get(5);

            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
            imageFile2 = RequestBody.create(MediaType.parse("multipart/form-data"), file2);
            imageFile3 = RequestBody.create(MediaType.parse("multipart/form-data"), file3);
            imageFile4 = RequestBody.create(MediaType.parse("multipart/form-data"), file4);
            imageFile5 = RequestBody.create(MediaType.parse("multipart/form-data"), file5);
            imageFile6 = RequestBody.create(MediaType.parse("multipart/form-data"), file6);


            body1 = MultipartBody.Part.createFormData("image1", imageFileName1, imageFile1);
            body2 = MultipartBody.Part.createFormData("image2", imageFileName2, imageFile2);
            body3 = MultipartBody.Part.createFormData("image3", imageFileName3, imageFile3);
            body4 = MultipartBody.Part.createFormData("image4", imageFileName4, imageFile4);
            body5 = MultipartBody.Part.createFormData("image5", imageFileName5, imageFile5);
            body6 = MultipartBody.Part.createFormData("image6", imageFileName6, imageFile6);


            //게시글x 위치x
            if (article == null && address == null) {

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, null, body1, body2, body3, body4, body5, body6, null, null, null);

            }
            //게시글o, 위치x
            else if (article != null && address == null) {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, articlePart, body1, body2, body3, body4, body5, body6, null, null, null);

            }
            //게시글x, 위치o
            else if (article == null && address != null) {
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));


                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, null, body1, body2, body3, body4, body5, body6, addressPart, latitudePart, longitudePart);

            }
            //게시글o, 위치o
            else {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.editResponse(postNumPart, accountPart, articlePart, body1, body2, body3, body4, body5, body6, addressPart, latitudePart, longitudePart);

            }
        }


        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {

                UploadResponse uploadResponse = response.body();
                PostActivity.isEditted = true;

                imageArraylist.clear();
                Glide.get(getApplicationContext()).clearMemory();

                //url에서 가져와 저장한 이미지는 삭제를 해준다.
                if (file6 != null) {
                    if (imageRootArrayList.get(5).equals("url")) {
                        Log.d("이미지6 삭제 접근: ", "yes");
                        file6.delete();
                    }
                }

                if (file5 != null) {
                    if (imageRootArrayList.get(4).equals("url")) {
                        Log.d("이미지5 삭제 접근: ", "yes");
                        file5.delete();
                    }
                }

                if (file4 != null) {
                    if (imageRootArrayList.get(3).equals("url")) {
                        Log.d("이미지4 삭제 접근: ", "yes");
                        file4.delete();
                    }
                }

                if (file3 != null) {
                    if (imageRootArrayList.get(2).equals("url")) {
                        Log.d("이미지3 삭제 접근: ", "yes");
                        file3.delete();
                    }
                }

                if (file2 != null) {
                    if (imageRootArrayList.get(1).equals("url")) {
                        Log.d("이미지2 삭제 접근: ", "yes");
                        file2.delete();
                    }
                }

                if (file1 != null) {
                    if (imageRootArrayList.get(0).equals("url")) {
                        Log.d("이미지1 삭제 접근: ", "yes");
                        file1.delete();
                    }
                }

                //작업이 완료되면 로딩 다이얼로그를 없애주고 메인화면으로 인텐트
                progressDialog.dismiss();

                Toast.makeText(getApplicationContext(), "수정 완료", Toast.LENGTH_SHORT).show();
                //메인 액티비티로 데이터를 함께 넘겨준다.
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                EditPostFirstActivity editPostFirstActivity = (EditPostFirstActivity) EditPostFirstActivity.editPostFirstActivity;
                editPostFirstActivity.finish();
                startActivity(intent);
                finish();


            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "업로드 실패", Toast.LENGTH_SHORT).show();
                Log.d("업로드 실패 에러: ", t.getMessage());
            }
        });
    }

    public void setArticleAddress() {
        Intent intent = getIntent();
        //게시글이 존재하면 변수에 담고 변수에 세팅
        if (intent.getStringExtra("article") != null) {
            article = intent.getStringExtra("article");
            Log.d("세팅된 게시글: ", article);
            et_article.setText(article);
        }

        //주소가 존재하면 변수에 담고 변수에 세팅
        if (intent.getStringExtra("address") != null) {
            address = intent.getStringExtra("address");
            Log.d("세팅된 주소: ", address);
            latitude = Double.parseDouble(intent.getStringExtra("latitude"));
            Log.d("세팅된 위도: ", String.valueOf(latitude));
            longitude = Double.parseDouble(intent.getStringExtra("longitude"));
            Log.d("세팅된 경도: ", String.valueOf(longitude));

            tv_location.setText(address);
        }
    }

    public void setImageRoot() {
        imageRootArrayList = new ArrayList<>();
        Intent intent = getIntent();
        for (int i = 0; i < imageArrayList.size(); i++) {
            imageRootArrayList.add(intent.getStringExtra("imageRoot" + (i + 1)));
            Log.d("이미지 root:", imageRootArrayList.get(i));
        }
    }

    public class UrlImageToFile extends AsyncTask<String, Void, String> {

        String connectURL = null;
        String postParameters = null;
        String filename;
        String savefolder = "/savefolder";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected String doInBackground(String... params) {
            //이미지가 url에서 왔을 때
            if (params[1].equals("url")) {
                connectURL = params[0];

                String savePath = Environment.getExternalStorageDirectory().toString() + savefolder;
                File dir = new File(savePath);
                //만약 해당 경로로 된 디텍토리가 없다면
                if (!dir.exists()) {
                    //해당 경로로 된 디렉토리 생성
                    dir.mkdir();
                }

                //파일 이름 설정
                Date date = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
                //뒤에 숫자를 안 붙여주면 이름이 다 똑같이 생성돼서 뒤에 숫자를 하나씩 바꿔줘서 구분을 해줘야 함함
                filename = String.valueOf(simpleDateFormat.format(date) + params[2]);

                //이미지 파일의 경로 설정
                String filePath = savePath + "/" + filename + ".jpg";


                try {

                    URL url = new URL(connectURL);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setReadTimeout(0);
                    httpURLConnection.setConnectTimeout(0);
                    httpURLConnection.connect();

                    int length = httpURLConnection.getContentLength();


                    byte[] tmpByte = new byte[length];
                    //입력 스트림 생성
                    InputStream inputStream = httpURLConnection.getInputStream();
                    File file = new File(filePath);

                    //파일 저장을 위한 스트림 생성
                    FileOutputStream fileOutputStream = new FileOutputStream(file);

                    //write할 길이를 구하기 위한 구문
                    int read;
                    for (; ; ) {
                        //read함수의 param으로 byte어레이 객체를 넣어서 그 객체 안에 이미지 파일의 바이너리 코드를 넣겠다는 의미
                        read = inputStream.read(tmpByte);
                        if (read <= 0) {
                            break;
                        }
                        //file 생성
                        fileOutputStream.write(tmpByte, 0, read);
                    }
                    inputStream.close();
                    fileOutputStream.close();
                    httpURLConnection.disconnect();
                    imageFileArrayList.add(new File(file.getPath()));
                    Log.d("이미지 파일 경로" + params[2], String.valueOf(imageFileArrayList.get(Integer.parseInt(params[2]))));
                    return file.getPath();

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("커넥트 에러", e.getMessage());
                    return null;
                }


            }
            //이미지가 uri에서 왔을 때 마찬가지로 uri->bitmap->file의 형태로 이미지를 리사이징하여 파일화한다.
            else {

//                imageFileArrayList.add(new File(Uri.parse(params[0]).getPath()));
                //이미지 처리 객체 초기화
                ProcessImage processImage = new ProcessImage(EditPostSecondActivity.this);
                imageFileArrayList.add(processImage.createFileFromBitmap(processImage.getBitmapFromUri(params[0]), params[0]));

                return null;
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                Log.d("url이미지의 절대 경로:", result);

            } else {
                Log.d("url이미지의 절대 경로: ", "null");
            }


        }
    }

    //뒤로가기 버튼을 눌렀을 때
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        imageArrayList.clear();

        //url에서 가져와 저장한 이미지는 삭제를 해준다.
        for (int i = 0; i < imageFileArrayList.size(); i++) {
            if (imageRootArrayList.get(i).equals("url")) {
                Log.d("삭제된 이미지 경로", String.valueOf(imageFileArrayList.get(i)));
                imageFileArrayList.get(i).delete();

            }
        }
    }
}
