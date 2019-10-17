package com.example.sns;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.sns.LoginActivity.account;
import static com.example.sns.PostActivity.isUploaded;
import static com.example.sns.UploadFirstActivity.imageArrayList;

public class UploadSecondActivity extends AppCompatActivity {

    /* 이 액티비티에서 핵심은 이미지 uri를 통해서 bitmap으로 만들어 이미지의 사이즈를 resize하고 그것을 파일로 만들어 서버로 전송하는 것
     * uri -> bitmap -> file -> 전송 */

    Button btn_back, btn_upload;
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
    private double latitude = 1000;
    private double longitude = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_second);

        btn_upload = findViewById(R.id.btn_upload);
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


        //위치 추가 버튼 클릭 리스너
        tv_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddLocationActivity.class);
                startActivityForResult(intent, 0);

            }
        });


        //업로드 버튼 클릭 리스너
        //imageArrayList 클리어
        //glide 클리어
        btn_upload.setOnClickListener(new View.OnClickListener() {
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
            latitude = data.getDoubleExtra("latitude", 0);
            Log.d("넘어온 위도", String.valueOf(latitude));
            longitude = data.getDoubleExtra("longitude", 0);
            Log.d("넘어온 경도", String.valueOf(longitude));
            tv_location.setText(address);
        }
    }

    //게시물 업로드 버튼을 누를 때 서버로 데이터를 넘겨주는 메소드
    private void upload(
            String article,
            ArrayList imageArraylist,
            String address,
            double latitude,
            double longitude) {

        //서버와 통신이 이루어질 동안 보일 로딩 창
        ProgressDialog progressDialog = new ProgressDialog(UploadSecondActivity.this);
        progressDialog.show(UploadSecondActivity.this, "게시물 업로드", "잠시만 기다려 주세요.", true, false);


        //레트로핏 세팅
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://13.124.105.47/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Log.d("아이디", account);


        //서버로 보내줄 데이터 param 설정
        RequestBody postNumPart = RequestBody.create(MultipartBody.FORM, String.valueOf(0));
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

        //이미지 처리 객체 초기화
        ProcessImage processImage = new ProcessImage(UploadSecondActivity.this);

        //사진 1장 선택했을 때
        if (imageCount == 1) {
            //이미지 파일의 이름 설정
            String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName = account + timeStamp + "1.jpg";


            File file = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(0))), String.valueOf(imageArraylist.get(0)));
            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file);


            body1 = MultipartBody.Part.createFormData("image1", imageFileName, imageFile1);


            //게시글x 위치x
            if (article == null && address == null) {

                retrofitService = retrofit.create(RetrofitService.class);
                //인터페이스의 uploadResponse 메소드 실행
                call = retrofitService.uploadResponse(postNumPart, accountPart, null, body1, null, null, null, null, null, null, null, null);

            }
            //게시글o, 위치x
            else if (article != null && address == null) {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, articlePart, body1, null, null, null, null, null, null, null, null);

            }
            //게시글x, 위치o
            else if (article == null && address != null) {
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));


                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, null, body1, null, null, null, null, null, addressPart, latitudePart, longitudePart);

            }
            //게시글o, 위치o
            else {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, articlePart, body1, null, null, null, null, null, addressPart, latitudePart, longitudePart);

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

            File file1 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(0))), String.valueOf(imageArraylist.get(0)));
            File file2 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(1))), String.valueOf(imageArraylist.get(1)));

            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
            imageFile2 = RequestBody.create(MediaType.parse("multipart/form-data"), file2);


            body1 = MultipartBody.Part.createFormData("image1", imageFileName1, imageFile1);
            body2 = MultipartBody.Part.createFormData("image2", imageFileName2, imageFile2);


            //게시글x 위치x
            if (article == null && address == null) {

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, null, body1, body2, null, null, null, null, null, null, null);

            }
            //게시글o, 위치x
            else if (article != null && address == null) {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, articlePart, body1, body2, null, null, null, null, null, null, null);

            }
            //게시글x, 위치o
            else if (article == null && address != null) {
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));


                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, null, body1, body2, null, null, null, null, addressPart, latitudePart, longitudePart);

            }
            //게시글o, 위치o
            else {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, articlePart, body1, body2, null, null, null, null, addressPart, latitudePart, longitudePart);

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


            File file1 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(0))), String.valueOf(imageArraylist.get(0)));
            File file2 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(1))), String.valueOf(imageArraylist.get(1)));
            File file3 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(2))), String.valueOf(imageArraylist.get(2)));



            imageFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
            imageFile2 = RequestBody.create(MediaType.parse("multipart/form-data"), file2);
            imageFile3 = RequestBody.create(MediaType.parse("multipart/form-data"), file3);


            body1 = MultipartBody.Part.createFormData("image1", imageFileName1, imageFile1);
            body2 = MultipartBody.Part.createFormData("image2", imageFileName2, imageFile2);
            body3 = MultipartBody.Part.createFormData("image3", imageFileName3, imageFile3);


            //게시글x 위치x
            if (article == null && address == null) {

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, null, body1, body2, body3, null, null, null, null, null, null);

            }
            //게시글o, 위치x
            else if (article != null && address == null) {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, articlePart, body1, body2, body3, null, null, null, null, null, null);

            }
            //게시글x, 위치o
            else if (article == null && address != null) {
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));


                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, null, body1, body2, body3, null, null, null, addressPart, latitudePart, longitudePart);

            }
            //게시글o, 위치o
            else {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, articlePart, body1, body2, body3, null, null, null, addressPart, latitudePart, longitudePart);

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

            File file1 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(0))), String.valueOf(imageArraylist.get(0)));
            File file2 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(1))), String.valueOf(imageArraylist.get(1)));
            File file3 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(2))), String.valueOf(imageArraylist.get(2)));
            File file4 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(3))), String.valueOf(imageArraylist.get(3)));


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
                call = retrofitService.uploadResponse(postNumPart, accountPart, null, body1, body2, body3, body4, null, null, null, null, null);

            }
            //게시글o, 위치x
            else if (article != null && address == null) {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, articlePart, body1, body2, body3, body4, null, null, null, null, null);

            }
            //게시글x, 위치o
            else if (article == null && address != null) {
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));


                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, null, body1, body2, body3, body4, null, null, addressPart, latitudePart, longitudePart);

            }
            //게시글o, 위치o
            else {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, articlePart, body1, body2, body3, body4, null, null, addressPart, latitudePart, longitudePart);

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


            File file1 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(0))), String.valueOf(imageArraylist.get(0)));
            File file2 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(1))), String.valueOf(imageArraylist.get(1)));
            File file3 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(2))), String.valueOf(imageArraylist.get(2)));
            File file4 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(3))), String.valueOf(imageArraylist.get(3)));
            File file5 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(4))), String.valueOf(imageArraylist.get(4)));

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
                call = retrofitService.uploadResponse(postNumPart, accountPart, null, body1, body2, body3, body4, body5, null, null, null, null);

            }
            //게시글o, 위치x
            else if (article != null && address == null) {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, articlePart, body1, body2, body3, body4, body5, null, null, null, null);

            }
            //게시글x, 위치o
            else if (article == null && address != null) {
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));


                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, null, body1, body2, body3, body4, body5, null, addressPart, latitudePart, longitudePart);

            }
            //게시글o, 위치o
            else {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, articlePart, body1, body2, body3, body4, body5, null, addressPart, latitudePart, longitudePart);

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


            File file1 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(0))), String.valueOf(imageArraylist.get(0)));
            File file2 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(1))), String.valueOf(imageArraylist.get(1)));
            File file3 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(2))), String.valueOf(imageArraylist.get(2)));
            File file4 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(3))), String.valueOf(imageArraylist.get(3)));
            File file5 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(4))), String.valueOf(imageArraylist.get(4)));
            File file6 = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArraylist.get(4))), String.valueOf(imageArraylist.get(5)));

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
                call = retrofitService.uploadResponse(postNumPart, accountPart, null, body1, body2, body3, body4, body5, body6, null, null, null);

            }
            //게시글o, 위치x
            else if (article != null && address == null) {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, articlePart, body1, body2, body3, body4, body5, body6, null, null, null);

            }
            //게시글x, 위치o
            else if (article == null && address != null) {
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));


                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, null, body1, body2, body3, body4, body5, body6, addressPart, latitudePart, longitudePart);

            }
            //게시글o, 위치o
            else {

                articlePart = RequestBody.create(MultipartBody.FORM, article);
                addressPart = RequestBody.create(MultipartBody.FORM, address);
                latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
                longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));

                retrofitService = retrofit.create(RetrofitService.class);
                call = retrofitService.uploadResponse(postNumPart, accountPart, articlePart, body1, body2, body3, body4, body5, body6, addressPart, latitudePart, longitudePart);

            }
        }


        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {

                UploadResponse uploadResponse = response.body();

                int postNum = Integer.parseInt(uploadResponse.postNum);

                imageArraylist.clear();
                Glide.get(getApplicationContext()).clearMemory();

                //작업이 완료되면 로딩 다이얼로그를 없애주고 메인화면으로 인텐트
                progressDialog.dismiss();

                Toast.makeText(getApplicationContext(), "업로드 완료", Toast.LENGTH_SHORT).show();
                isUploaded = true;


                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                UploadFirstActivity uploadFirstActivity = (UploadFirstActivity) UploadFirstActivity.uploadFirstActivity;
                uploadFirstActivity.finish();
                startActivity(intent);
                finish();


            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "업로드 실패", Toast.LENGTH_SHORT).show();
                Log.d("게시물 업로드 실패 메세지", t.getMessage());
            }
        });
    }
}
