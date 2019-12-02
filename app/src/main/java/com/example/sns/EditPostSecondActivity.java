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
import static com.example.sns.LoginActivity.httpURLConnection;
import static com.example.sns.EditPostFirstActivity.imageArrayList;

public class EditPostSecondActivity extends AppCompatActivity {
    private final String TAG = EditPostSecondActivity.class.getSimpleName();
    private Button btn_back, btn_edit;
    private EditText et_article;
    //해시태그 라이브러리 클래스
    private HashTagHelper hashTagHelper;


    private TextView tv_location;

    private ViewPager viewPager;
    //게시글 담는 변수
    private String article = null;

    //위치 추가로 가져온 장소의 주소
    private String address = null;
    //위치 추가로 가져온 장소의 위도와 경도
    private double latitude;
    private double longitude;

    //서버로 전송시킬 이미지 파일 객체 선언
    private File file1, file2, file3, file4, file5, file6 = null;

    //서버로 전송시킬 이미지 파일 객체 선언
    private ArrayList<File> imageFileArrayList;

    private LoginUser loginUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post_second);

        loginUser = LoginUser.getInstance();

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
        ArrayList<String> imageSourceList = new ArrayList<>();
        for (int i = 0; i < imageArrayList.size(); i++) {
            imageSourceList.add(imageArrayList.get(i).imageSource);
        }
        //뷰 페이저를 어댑터와 연결해서 보여준다.
        viewPager.setAdapter(new UploadImageViewPagerAdapter(getSupportFragmentManager(), imageSourceList));

        //하단의 탭 설정
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager, true);


        //수정 전 게시글과 위치가 존재했다면 화면에 세팅하고 변수에 담아주는 메소드
        setArticleAddress();


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
        addImageFileList();


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

                for (int i = 0; i < imageFileArrayList.size(); i++) {
                    if (imageArrayList.get(i).imageRoot.equals("url")) {
                        Log.d("삭제된 이미지 경로", String.valueOf(imageFileArrayList.get(i)));
                        imageFileArrayList.get(i).delete();

                    }

                }

                finish();
                return;
            }
        });

    }

    private void addImageFileList() {
        imageFileArrayList = new ArrayList<>();
        UrlImageToFile urlImageToFile = new UrlImageToFile();
        urlImageToFile.execute();

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
            ArrayList<UploadImageItem> imageArrayList,
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
        Log.d("아이디", loginUser.getAccount());


        //서버로 보내줄 데이터 param 설정
        RequestBody postNumPart = RequestBody.create(MultipartBody.FORM, String.valueOf(postNum));
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
        Call<UploadResponse> call = retrofitService.editResponse(postNumPart, accountPart, articlePart, imageMultipartBodyList.get(0), imageMultipartBodyList.get(1), imageMultipartBodyList.get(2), imageMultipartBodyList.get(3), imageMultipartBodyList.get(4), imageMultipartBodyList.get(5), addressPart, latitudePart, longitudePart);

        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {

                PostActivity.isEditted = true;

                Glide.get(getApplicationContext()).clearMemory();

                //url에서 가져와 저장한 이미지는 삭제를 해준다.
                for (int i = 0; i < imageFileArrayList.size(); i++) {
                    if (imageArrayList.get(i).imageRoot.equals("url")) {
                        Log.d("삭제된 이미지 경로", String.valueOf(imageFileArrayList.get(i)));
                        imageFileArrayList.get(i).delete();

                    }
                }

                imageArrayList.clear();
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


    private ArrayList createImageMultipartBody(ArrayList<UploadImageItem> imageArrayList) {//로컬 단말기의 uri가 담긴 리스트
        ArrayList<MultipartBody.Part> imageMultipartBodyList = new ArrayList<>();//이미지 멀티파트 리스트
        //이미지 처리 객체 초기화

        for (int i = 0; i < imageArrayList.size(); i++) {
            String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName = loginUser.getAccount() + timeStamp + (i + 1) + ".jpg";//서버의 이미지 파일 명
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFileArrayList.get(i));//리퀘스트 body
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

    private void setArticleAddress() {
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


    private class UrlImageToFile extends AsyncTask<String, Void, String> {

        String connectURL = null;
        String imageRoot = null;
        String filename;
        String savefolder = "/savefolder";
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(EditPostSecondActivity.this);

            progressDialog = ProgressDialog.show(EditPostSecondActivity.this, "이미지 로딩중", "잠시만 기다려주세요", false, false);


        }

        @Override
        protected String doInBackground(String... params) {
            try {
                for (int i = 0; i < imageArrayList.size(); i++) {
                    imageRoot = imageArrayList.get(i).imageRoot;

                    //이미지가 url에서 왔을 때
                    if (imageRoot.equals("url")) {
                        connectURL = imageArrayList.get(i).imageSource;

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
                        filename = String.valueOf(simpleDateFormat.format(date) + i);

                        //이미지 파일의 경로 설정
                        String filePath = savePath + "/" + filename + ".jpg";


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
                    }
                    //이미지가 uri에서 왔을 때 마찬가지로 uri->bitmap->file의 형태로 이미지를 리사이징하여 파일화한다.
                    else {
                        //이미지 처리 객체 초기화
                        ProcessImage processImage = new ProcessImage(EditPostSecondActivity.this);
                        imageFileArrayList.add(processImage.createFileFromBitmap(processImage.getBitmapFromUri(imageArrayList.get(i).imageSource), imageArrayList.get(i).imageSource));
                    }
                    Log.d(TAG, "imageFilePath: " + imageFileArrayList.get(i).getPath());
                }

                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("커넥트 에러", e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "사진 로드에 실패하였습니다", Toast.LENGTH_SHORT).show();
//                        progressDialog.dismiss();
                //메인 액티비티로 데이터를 함께 넘겨준다.
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                EditPostFirstActivity editPostFirstActivity = (EditPostFirstActivity) EditPostFirstActivity.editPostFirstActivity;
                editPostFirstActivity.finish();
                startActivity(intent);
                finish();
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                Log.d("url이미지의 절대 경로:", result);

            } else {
                Log.d("url이미지의 절대 경로: ", "null");
            }
            progressDialog.dismiss();
        }
    }

    //뒤로가기 버튼을 눌렀을 때
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //url에서 가져와 저장한 이미지는 삭제를 해준다.
        for (int i = 0; i < imageFileArrayList.size(); i++) {
            if (imageArrayList.get(i).imageRoot.equals("url")) {
                Log.d("삭제된 이미지 경로", String.valueOf(imageFileArrayList.get(i)));
                imageFileArrayList.get(i).delete();

            }
        }
    }
}
