package com.example.sns.post.upload.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.example.sns.post.activity.AddLocationActivity;
import com.example.sns.util.ProcessImage;
import com.example.sns.R;
import com.example.sns.util.RetrofitService;
import com.example.sns.login.model.LoginUser;
import com.example.sns.main.activity.MainActivity;
import com.example.sns.post.upload.adapter.UploadImageViewPagerAdapter;
import com.example.sns.post.upload.model.UploadResponse;
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

import static com.example.sns.post.activity.PostActivity.isUploaded;
import static com.example.sns.post.upload.activity.UploadFirstActivity.imageArrayList;

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
        viewPager.setAdapter(new UploadImageViewPagerAdapter(getSupportFragmentManager(), imageArrayList));

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
            ArrayList imageArrayList,
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

        LoginUser loginUser = LoginUser.getInstance();
        //서버로 보내줄 데이터 param 설정
        RequestBody postNumPart = RequestBody.create(MultipartBody.FORM, String.valueOf(0));
        RequestBody accountPart = RequestBody.create(MultipartBody.FORM, loginUser.getAccount());
        //초기화를 바로 해주지 않는 이유는 이 데이터들은 쓰일 수도 있고 안 쓰일 수도 있기 때문이다.
        RequestBody articlePart = null;
        RequestBody addressPart = null;
        RequestBody latitudePart = null;
        RequestBody longitudePart = null;

        //게시글o
        if (article != null) {
            articlePart = RequestBody.create(MultipartBody.FORM, article);
        }

        //위치o
        if (address != null) {
            addressPart = RequestBody.create(MultipartBody.FORM, address);
            latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
            longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));
        }

        ArrayList<MultipartBody.Part> imageMultipartBodyList = createImageMultipartBody(imageArrayList);//이미지 multipartbody 생성

        //레트로핏 인터페이스 설정
        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        //인터페이스의 uploadResponse 메소드 실행
        Call<UploadResponse> call = retrofitService.uploadResponse(postNumPart, accountPart, articlePart, imageMultipartBodyList.get(0), imageMultipartBodyList.get(1), imageMultipartBodyList.get(2), imageMultipartBodyList.get(3), imageMultipartBodyList.get(4), imageMultipartBodyList.get(5), addressPart, latitudePart, longitudePart);


        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {

                UploadResponse uploadResponse = response.body();


                imageArrayList.clear();
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

    private ArrayList createImageMultipartBody(ArrayList<String> imageArrayList) {//로컬 단말기의 uri가 담긴 리스트
        ArrayList<MultipartBody.Part> imageMultipartBodyList = new ArrayList<>();//이미지 멀티파트 리스트

        ProcessImage processImage = new ProcessImage(UploadSecondActivity.this);//이미지 처리 객체
        LoginUser loginUser = LoginUser.getInstance();
        for (int i = 0; i < imageArrayList.size(); i++) {
            String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName = loginUser.getAccount() + timeStamp + (i+1) +".jpg" ;//서버의 이미지 파일 명
            File imageFile = processImage.createFileFromBitmap(processImage.getBitmapFromUri(String.valueOf(imageArrayList.get(i))), String.valueOf(imageArrayList.get(i)));
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);//리퀘스트 body
            MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("image" + (i + 1), imageFileName, requestBody);
            imageMultipartBodyList.add(multipartBody);//리스트에 추가
        }

        if (imageArrayList.size() != 6) {
            for (int i = imageArrayList.size(); i < 6; i++) {
                imageMultipartBodyList.add(null);
            }
        }

        return imageMultipartBodyList;
    }

}
