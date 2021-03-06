package com.example.sns.chat.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sns.R;
import com.example.sns.chat.adapter.ChatContentViewPagerAdapter;
import com.example.sns.chat.ChatImageDownloadDialog;
import com.example.sns.chat.model.ChatContentDetailItem;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import static com.example.sns.login.activity.LoginActivity.httpURLConnection;

public class ChatContentDetailActivity extends AppCompatActivity {
    private final String TAG = ChatContentDetailActivity.class.getSimpleName();
    private TextView tv_nickname, tv_time, tv_download;
    private ImageButton ib_back;
    private ViewPager viewPager;
    private ChatContentViewPagerAdapter viewPagerAdapter;


    //인텐트로 넘어온 컨텐츠 리스트 사이즈
    private int size;



    private String clickedNickname;//컨텐츠 발신자
    private String clickedTime;//컨텐츠 전송 시간

    private ArrayList<ChatContentDetailItem> chatContentDetailItemArrayList;//전체 컨텐츠 어레이리스트
    private int currentPosition;//현재 컨텐츠의 position


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_content_detail);

        tv_nickname = findViewById(R.id.textview_nickname);
        tv_time = findViewById(R.id.textview_time);
        ib_back = findViewById(R.id.imagebutton_back);
        tv_download = findViewById(R.id.textview_download);
        chatContentDetailItemArrayList = new ArrayList<>();

        if (getIntent() != null) {
            //전체 이미지 사이즈
            size = getIntent().getIntExtra("contentListCount", 0);

            for (int i = 0; i < size; i++) {
                try {
                    String jsonString = getIntent().getStringExtra("contentData" + i);
                    JSONObject contentData = new JSONObject(jsonString);
                    String type = contentData.getString("type");
                    String nickname = contentData.getString("nickname");
                    String time = contentData.getString("time");
                    String content = contentData.getString("content");
                    ChatContentDetailItem chatContentDetailItem = new ChatContentDetailItem();
                    chatContentDetailItem.setType(type);
                    chatContentDetailItem.setNickname(nickname);
                    chatContentDetailItem.setTime(time);
                    chatContentDetailItem.setContent(content);
                    chatContentDetailItemArrayList.add(chatContentDetailItem);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            currentPosition = getIntent().getIntExtra("contentIndex", 0);
            clickedNickname = chatContentDetailItemArrayList.get(currentPosition).getNickname();
            clickedTime = chatContentDetailItemArrayList.get(currentPosition).getTime();
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
        viewPagerAdapter = new ChatContentViewPagerAdapter(getSupportFragmentManager(), chatContentDetailItemArrayList, new HashMap<>());
        viewPager.setAdapter(viewPagerAdapter);
        //클릭한 이미지로 뷰 페이저 프래그먼트를 이동시킨다.
        viewPager.setCurrentItem(currentPosition);
        //뷰페이저 페이지가 변하는 것을 감지하는 리스너
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            //페이지를 전환하기 위해 스크롤을 하면 계속 호출
            //해상 position의 아이템이 화면에 보이는 비중이 반 이상이 되면 그 아이템의 position으로 전환
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            //페이지가 변해서 새로운 페이지가 나타나면 호출되는 메소드
            @Override
            public void onPageSelected(int position) {
                String nickname = chatContentDetailItemArrayList.get(position).getNickname();
                String time = chatContentDetailItemArrayList.get(position).getTime();
                String date = formatDate(time, fromFromat, toFormat);
                tv_nickname.setText(nickname);
                tv_time.setText(date);
                //바뀌기 이전 페이지의 content type이 video인 경우 player release를 해준다
                if (viewPagerAdapter.getContentFragment(currentPosition) != null && chatContentDetailItemArrayList.get(currentPosition).getType().equals("video")) {
                    (viewPagerAdapter.getContentFragment(currentPosition)).onReleasePlayer();
                }
                //바뀐 페이지의 contendt type이 video인 경우 player initialize를 해준다
                if (chatContentDetailItemArrayList.get(position).getType().equals("video")) {
                    (viewPagerAdapter.getContentFragment(position)).onInitializePlayer();
                }
                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int position) {


            }
        });

        //다운로드 버튼 클릭 리스너
        tv_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = chatContentDetailItemArrayList.get(viewPager.getCurrentItem()).getContent();
                String type = chatContentDetailItemArrayList.get(viewPager.getCurrentItem()).getType();
                //컨텐츠가 저장된 서버로 접근해서 해당 컨텐츠를 단말기로 저장하기 위한 asynctask
                UrlContentToFile urlContentToFile = new UrlContentToFile();
                urlContentToFile.execute(type, content);
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


    public class UrlContentToFile extends AsyncTask<String, Void, String> implements ChatImageDownloadDialog.ChatImageDownloadDialogClickListener {

        String connectURL = null;
        String filename = null;
        String savefolder = "/Download";
        String type = null;//컨텐츠 타입
        //다운로드 다이얼로그 객체 초기화
        ChatImageDownloadDialog downloadDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadDialog = new ChatImageDownloadDialog(ChatContentDetailActivity.this, this);
            downloadDialog.setTotalCount(1);//다운로드할 이미지 수 셋
            downloadDialog.show();//다이얼로그 화면에 출력
        }

        @Override
        protected String doInBackground(String... params) {
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
            String filePath = null;//컨텐츠 파일 경로

            //컨텐츠를 저장하고 있는 서버 주소
            type = params[0];
            if (type.equals("image")) {//이미지인 경우
                connectURL = "http://13.124.105.47/chatimage/" + params[1];
                //이미지 파일의 경로 설정
                filePath = savePath + "/" + filename + ".jpg";
            } else {//동영상인 경우
                connectURL = "http://13.124.105.47/chatvideo/" + params[1];
                //동영상 파일의 경로 설정
                filePath = savePath + "/" + filename + ".mp4";
            }



            try {

                URL url = new URL(connectURL);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setReadTimeout(10000);
                httpURLConnection.setConnectTimeout(10000);
                httpURLConnection.connect();

                int length = httpURLConnection.getContentLength();


                byte[] tmpByte = new byte[length];
                //입력 스트림 생성
//                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                File file = new File(filePath);

                //파일 저장을 위한 스트림 생성
//                FileOutputStream fileOutputStream = new FileOutputStream(file);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));

                //write할 길이를 구하기 위한 구문
                int read;
                for (; ; ) {
                    //tmpByte만큼의 데이터를 읽어서 tmpByte에 넣고 바이트 수를 return
                    read = bufferedInputStream.read(tmpByte);
                    if (read <= 0) {//더 이상 읽어들일 값이 없는 경우 -1을 return하기 때문에 읽어들이는 반복문을 탈출한다.
                        break;
                    }
                    //param1: 출력할 byte배열, param2:시작 index, param3: size
                    bufferedOutputStream.write(tmpByte, 0, read);//파일에 값을 써준다.
                }
                bufferedInputStream.close();
                bufferedOutputStream.close();
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


        }//end of UrlImageToFile

        //다운로드 아이얼로그에서 확인 버튼을 눌렀을 때 콜백되는 인터페이스 함수
        @Override
        public void onConfirmClicked() {
            downloadDialog.close();//다이얼로그 종료
        }
    }

}
