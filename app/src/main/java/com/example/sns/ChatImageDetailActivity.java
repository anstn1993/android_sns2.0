package com.example.sns;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.example.sns.LoginActivity.httpURLConnection;

public class ChatImageDetailActivity extends AppCompatActivity {

    TextView tv_nickname, tv_time, tv_download;
    ImageButton ib_back;
    ViewPager viewPager;


    //인텐트로 넘어온 이미지 리스트 사이즈
    int size;
    //인텐트로 넘어온 이미지 리스트

    //채팅화면에서 클릭한 이미지의 파일명
    String clickedImage;
    String clickedNickname;
    String clickedTime;

    //전체 이미지 어레이리스트
    ArrayList<ChatImageDetailItem> chatImageDetailItemArrayList;

    //클릭된 이미지가 전체 이미지 중에서 어떤 position에 위치하는지를 알려줄 int
    int clickedImagePosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image_detail);

        tv_nickname = findViewById(R.id.textview_nickname);
        tv_time = findViewById(R.id.textview_time);
        ib_back = findViewById(R.id.imagebutton_back);
        tv_download = findViewById(R.id.textview_download);
        chatImageDetailItemArrayList = new ArrayList<>();

        if (getIntent() != null) {
            //전체 이미지 사이즈
            size = getIntent().getIntExtra("imageListCount", 0);

            for (int i = 0; i < size; i++) {
                try {
                    String jsonString = getIntent().getStringExtra("imageData" + i);
                    JSONObject imageData = new JSONObject(jsonString);
                    String nickname = imageData.getString("nickname");
                    String time = imageData.getString("time");
                    String image = imageData.getString("image");
                    ChatImageDetailItem chatImageDetailItem = new ChatImageDetailItem();
                    chatImageDetailItem.setNickname(nickname);
                    chatImageDetailItem.setTime(time);
                    chatImageDetailItem.setImage(image);
                    chatImageDetailItemArrayList.add(chatImageDetailItem);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            clickedImagePosition = getIntent().getIntExtra("imageIndex", 0);
            clickedImage = chatImageDetailItemArrayList.get(clickedImagePosition).image;
            clickedNickname = chatImageDetailItemArrayList.get(clickedImagePosition).nickname;
            clickedTime = chatImageDetailItemArrayList.get(clickedImagePosition).time;
            Log.d("넘어온 시간", clickedTime);
        }
        //닉네임 설정
        tv_nickname.setText(clickedNickname);

        String fromFromat = "yyyy-MM-dd HH:mm:ss";
        String toFormat = "yyyy년 M월 d일";
        String time = formatDate(clickedTime, fromFromat, toFormat);
        tv_time.setText(time);


        //이미지 뷰페이저 설정
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new ChatImageViewPagerAdapter(getSupportFragmentManager(), chatImageDetailItemArrayList));

        //클릭한 이미지로 뷰 페이저 프래그먼트를 이동시킨다.
        viewPager.setCurrentItem(clickedImagePosition);
        //뷰페이저 페이지가 변하는 것을 감지하는 리스너
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            //페이지가 변해서 새로운 페이지가 나타나면 호출되는 메소드
            @Override
            public void onPageSelected(int position) {
                Log.d("현재 이미지 인덱스", String.valueOf(position));
                Log.d("현재 이미지 닉네임", chatImageDetailItemArrayList.get(position).nickname);
                Log.d("현재 이미지 이름", chatImageDetailItemArrayList.get(position).image);
                String nickname = chatImageDetailItemArrayList.get(position).nickname;
                String time = chatImageDetailItemArrayList.get(position).time;
                String date = formatDate(time, fromFromat, toFormat);
                tv_nickname.setText(nickname);
                tv_time.setText(date);
            }

            @Override
            public void onPageScrollStateChanged(int position) {


            }
        });

        //다운로드 버튼 클릭 리스너
        tv_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imageURL = "http://13.124.105.47/chatimage/" + chatImageDetailItemArrayList.get(viewPager.getCurrentItem()).image;
                //이미지가 저장된 서버로 접근해서 해당 이미지를 단말기로 저장하기 위한 asynctask
                UrlImageToFile urlImageToFile = new UrlImageToFile();
                urlImageToFile.execute(imageURL);
            }
        });

        //뒤로가기 버튼 클릭 리스너
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }

    public static String formatDate(String time, String fromFormat, String toFormat) {
        //실제 시간 String의 형태를 그대로 따르는 포맷
        SimpleDateFormat fromDateFormat = new SimpleDateFormat(fromFormat);
        //바꿔서 표시할 포맷
        SimpleDateFormat toDateFormat = new SimpleDateFormat(toFormat);
        //포맷을 바꾸기 전에 기존 포맷의 표기로 보여줄 date객체
        Date fromDate = null;
        try {
            fromDate = fromDateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            fromDate = new Date();
        }
        //시간을 바꿔서 표기할 포맷으로 포맷을 해준다.
        return toDateFormat.format(fromDate);
    }

    public class UrlImageToFile extends AsyncTask<String, Void, String> implements ChatImageDownloadDialog.ChatImageDownloadDialogClickListener {

        String connectURL = null;
        String postParameters = null;
        String filename;
        String savefolder = "/Download";
        //다운로드 다이얼로그 객체 초기화
        ChatImageDownloadDialog downloadDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadDialog = new ChatImageDownloadDialog(ChatImageDetailActivity.this, this);
            downloadDialog.setTotalCount(1);//다운로드할 이미지 수 셋
            downloadDialog.show();//다이얼로그 화면에 출력
        }

        @Override
        protected String doInBackground(String... params) {
            //이미지를 저장하고 있는 서버 주소
            connectURL = params[0];
            //저장 경로
            String savePath = Environment.getExternalStorageDirectory().toString() + savefolder;
            //저장 경로로 된 디렉토리 객체 생성
            File dir = new File(savePath);
            //만약 해당 경로로 된 디텍토리가 없다면
            if (!dir.exists()) {
                //해당 경로로 된 디렉토리 생성
                dir.mkdir();
            }

            //파일 이름 설정
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
            filename = String.valueOf(simpleDateFormat.format(date));

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
                    if (read <= 0) {//더 이상 읽어들일 값이 없는 경우 -1을 출력하기 때문에 읽어들이는 반복문을 탈출한다.
                        break;
                    }
                    fileOutputStream.write(tmpByte, 0, read);//파일에 값을 써준다.
                }
                inputStream.close();
                fileOutputStream.close();
                httpURLConnection.disconnect();
                //갤러리를 브로드캐스트 리시버를 통해서 업데이트를 해줘야 다운로드된 파일이 갤러리에 뜨게 된다.
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                downloadDialog.updateDownloadCount();//다운로드 완료 수 업데이트
                return file.getPath();

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("커넥트 에러", e.getMessage());
                downloadDialog.failDownload();//다이얼로그를 다운로드 실패 상태로 전환
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                Log.d("url이미지의 절대 경로:", result);
                Toast.makeText(getApplicationContext(), "다운로드 완료", Toast.LENGTH_SHORT).show();
                downloadDialog.completeDownload();//다이얼로그를 다운로드 완료 상태로 전환

            } else {
                Log.d("url이미지의 절대 경로: ", "null");
                Toast.makeText(getApplicationContext(), "다운로드 실패", Toast.LENGTH_SHORT).show();
                downloadDialog.failDownload();//다이얼로그를 다운로드 실패 상태로 전환
            }


        }
        //다운로드 아이얼로그에서 확인 버튼을 눌렀을 때 콜백되는 인터페이스 함수
        @Override
        public void onConfirmClicked() {
            downloadDialog.close();//다이얼로그 종료
        }
    }

}
