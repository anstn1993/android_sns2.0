package com.example.sns;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.example.sns.LoginActivity.httpURLConnection;

public class ChatContentListActivity extends AppCompatActivity implements ChatContentListAdapter.ChatImageRecyclerViewListener {
    private int roomNum = 0;
    private ImageButton ib_check, ib_back, ib_download;
    private TextView tv_cancel;
    //채팅 컨텐츠 데이터의 json스트링을 담아서 어댑터와 연동할 리스트
    private ArrayList<ChatContentListItem> chatContentArrayList;
    private RecyclerView rv_chatContent;//리사이클러뷰
    private GridLayoutManager gridLayoutManager;//레이아웃 메니저
    private ChatContentListAdapter chatContentListAdapter;

    private boolean isSelectMode = false;//컨텐츠 선택모드 boolean

    private ArrayList<Integer> selectedContentIndexList;//선택모드에서 선택한 컨텐츠의 index를 담는 리스트


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_content_list);

        initView();//뷰 초기화
        setRecyclerView();//리사이클러뷰 셋
        selectedContentIndexList = new ArrayList<>();//선택모드에서 선택한 이미지 리스트 초기화

        //이미지 데이터 셋
        if (getIntent() != null) {
            int totalImageCount = getIntent().getIntExtra("totalContentCount", 0);
            for (int i = 0; i < totalImageCount; i++) {
                String contentData = getIntent().getStringExtra("contentData" + i);
                ChatContentListItem chatContentListItem = new ChatContentListItem();
                chatContentListItem.setContentData(contentData);
                chatContentListItem.setSelectMode(false);
                chatContentListItem.setSelected(false);
                chatContentArrayList.add(chatContentListItem);
            }
        }

        //이미지 선택 버튼 클릭 리스너
        ib_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelectMode = true;//선택모드 true
                ib_check.setVisibility(View.GONE);//이미지 선택 버튼 GONE
                tv_cancel.setVisibility(View.VISIBLE);//취소 버튼 visible
                ib_download.setVisibility(View.VISIBLE);//다운로드 버튼 visible
                //모든 이미지를 선택 모드로 전환해준다.
                for (int i = 0; i < chatContentArrayList.size(); i++) {
                    chatContentArrayList.get(i).setSelectMode(true);
                }
                chatContentListAdapter.notifyItemRangeChanged(0, chatContentListAdapter.getItemCount(), "selectMode");
            }
        });

        //이미지 취소 버튼 클릭 리스너
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelectMode = false;//선택모드 false
                tv_cancel.setVisibility(View.GONE);//이미지 취소 버튼 GONE
                ib_download.setVisibility(View.GONE);//다운로드 버튼 GONE
                ib_check.setVisibility(View.VISIBLE);//이미지 선택 버튼 VISIBLE
                selectedContentIndexList.clear();//선택된 이미지 리스트를 비워준다.
                //모든 이미지의 선택 모드를 해제한다.
                for (int i = 0; i < chatContentArrayList.size(); i++) {
                    chatContentArrayList.get(i).setSelectMode(false);//선택모드 해제
                    chatContentArrayList.get(i).setSelected(false);//모든 이미지를 미선택 상태로 전환
                }
                chatContentListAdapter.notifyItemRangeChanged(0, chatContentListAdapter.getItemCount(), "cancel");
            }
        });

        //뒤로가기 버튼 클릭 리스너
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();//액티비티 종료
            }
        });

        //다운로드 버튼 클릭 리스너
        ib_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //선택한 컨텐츠
                UrlContentToFile urlContentToFile = new UrlContentToFile();
                urlContentToFile.execute(selectedContentIndexList);//선택한 이미지 리스트를 넘겨줘서 이미지들을 다운받는 스레드 실행
            }
        });
    }

    //뷰 초기화 함수
    private void initView() {
        //뷰 초기화
        ib_check = findViewById(R.id.imagebutton_check);
        ib_back = findViewById(R.id.imagebutton_back);
        ib_download = findViewById(R.id.imagebutton_download);
        tv_cancel = findViewById(R.id.textview_cancel);
        //취소버튼과 다운로드 버튼은 Gone처리
        tv_cancel.setVisibility(View.GONE);
        ib_download.setVisibility(View.GONE);
    }

    private void setRecyclerView() {

        chatContentArrayList = new ArrayList<>();

        rv_chatContent = findViewById(R.id.recyclerview_chatimage);
        rv_chatContent.setHasFixedSize(true);

        //레이아웃 메니저 초기화
        gridLayoutManager = new GridLayoutManager(this, 3);
        rv_chatContent.setLayoutManager(gridLayoutManager);
        //어댑터 초기화
        chatContentListAdapter = new ChatContentListAdapter(chatContentArrayList, this);
        chatContentListAdapter.setOnClickListener(this);
        rv_chatContent.setAdapter(chatContentListAdapter);
    }

    //리사이클러뷰 컨텐츠를 클릭했을 때 호출되는 메소드
    @Override
    public void onContentClicked(int position) {
        //컨텐츠 선택모드가 아닌 경우
        if (isSelectMode == false) {
            Intent intent = new Intent(ChatContentListActivity.this, ChatContentDetailActivity.class);
            intent.putExtra("contentListCount", chatContentArrayList.size());//전체 이미지 개수
            intent.putExtra("contentIndex", position);//선택한 이미지의 인덱스
            for (int i = 0; i < chatContentArrayList.size(); i++) {
                //이미지 데이터를 담는 json스트링을 이미지 수만큼 담아준다.
                intent.putExtra("contentData" + i, chatContentArrayList.get(i).contentData);
            }
            startActivity(intent);
        }
        //컨텐츠 선택 모드인 경우
        else {
            //선택 상태인 경우
            if (chatContentArrayList.get(position).isSelected) {
                chatContentArrayList.get(position).setSelected(false);//선택 취소로 전환
                int index = selectedContentIndexList.indexOf(position);//컨텐츠의 인덱스
                selectedContentIndexList.remove(index);//선택 취소한 컨텐츠 index를 리스트에서 삭제
            }
            //선택을 하지 않은 상태면
            else {
                chatContentArrayList.get(position).setSelected(true);//선택 상태로 전환
                selectedContentIndexList.add(position);//선택한 컨텐츠 index를 리스트에 추가
            }
            chatContentListAdapter.notifyItemChanged(position, "check");
        }
    }

    //리사이클러뷰 컨텐츠를 길게 클릭했을 때 호출되는 메소드
    @Override
    public void onContentLongClicked(int position) {
        //이미지 선택 모드가 아닌 상태에서만 동작
        if (isSelectMode == false) {
            isSelectMode = true;//선택모드 true
            ib_check.setVisibility(View.GONE);//이미지 선택 버튼 GONE
            tv_cancel.setVisibility(View.VISIBLE);//취소 버튼 visible
            ib_download.setVisibility(View.VISIBLE);//다운로드 버튼 visible
            //모든 이미지를 선택 모드로 전환해준다.
            for (int i = 0; i < chatContentArrayList.size(); i++) {
                chatContentArrayList.get(i).setSelectMode(true);
            }
            chatContentListAdapter.notifyItemRangeChanged(0, chatContentListAdapter.getItemCount(), "selectMode");
        }
    }

    @Override
    public void onBackPressed() {
        //이미지 선택 모드인 경우에 뒤로가기를 누르면 선택 모드가 취소되게 한다.
        if (isSelectMode == true) {
            isSelectMode = false;//선택모드 false
            tv_cancel.setVisibility(View.GONE);//이미지 취소 버튼 GONE
            ib_download.setVisibility(View.GONE);//다운로드 버튼 GONE
            ib_check.setVisibility(View.VISIBLE);//이미지 선택 버튼 VISIBLE
            selectedContentIndexList.clear();//선택된 이미지 리스트를 비워준다.
            //모든 이미지의 선택 모드를 해제한다.
            for (int i = 0; i < chatContentArrayList.size(); i++) {
                chatContentArrayList.get(i).setSelectMode(false);//선택모드 해제
                chatContentArrayList.get(i).setSelected(false);//모든 이미지를 미선택 상태로 전환
            }
            chatContentListAdapter.notifyItemRangeChanged(0, chatContentListAdapter.getItemCount(), "cancel");
        }
        //이미지 선택 모드가 아닌 경우에는 뒤로가기
        else {
            super.onBackPressed();
        }
    }

    public class UrlContentToFile extends AsyncTask<ArrayList<Integer>, Void, String> implements ChatImageDownloadDialog.ChatImageDownloadDialogClickListener {

        String connectURL = null;
        String filename;
        String savefolder = "/Download";
        //다운로드 다이얼로그 객체 초기화
        ChatImageDownloadDialog downloadDialog;
        ArrayList<Integer> selectedContentIndexList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadDialog = new ChatImageDownloadDialog(ChatContentListActivity.this, this);
            downloadDialog.show();//다이얼로그 화면에 출력
        }

        @Override
        protected String doInBackground(ArrayList<Integer>... params) {
            selectedContentIndexList = params[0];
            downloadDialog.setTotalCount(selectedContentIndexList.size());//다운로드할 이미지 수 셋
            //저장 경로
            String savePath = Environment.getExternalStorageDirectory().toString() + savefolder;
            //저장 경로로 된 디렉토리 객체 생성
            File dir = new File(savePath);
            //만약 해당 경로로 된 디텍토리가 없다면
            if (!dir.exists()) {
                //해당 경로로 된 디렉토리 생성
                dir.mkdir();
            }

            try {
                //이미지의 수만큼 이미지를 다운받는 작업 수행
                for (int i = 0; i < selectedContentIndexList.size(); i++) {
                    //파일 이름 설정
                    Date date = new Date();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
                    filename = String.valueOf(simpleDateFormat.format(date));
                    JSONObject contentData = new JSONObject(chatContentArrayList.get(selectedContentIndexList.get(i)).contentData);
                    String type = contentData.getString("type");//컨텐츠 타입
                    String content = contentData.getString("content");//컨텐츠 파일 명
                    String filePath = null;//파일 경로
                    if (type.equals("image")) {//이미지 컨텐츠
                        filePath = savePath + "/" + filename + i + ".jpg";
                        //이미지를 저장하고 있는 서버 주소
                        connectURL = "http://13.124.105.47/chatimage/" + content;
                    }else {//동영상 컨텐츠
                        filePath = savePath + "/" + filename + i + ".mp4";
                        //이미지를 저장하고 있는 서버 주소
                        connectURL = "http://13.124.105.47/chatvideo/" + content;
                    }
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
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                    File file = new File(filePath);

                    //파일 저장을 위한 스트림 생성
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));

                    //write할 길이를 구하기 위한 구문
                    int read;
                    for (; ; ) {
                        //read함수의 param으로 byte어레이 객체를 넣어서 그 객체 안에 이미지 파일의 바이너리 코드를 넣겠다는 의미
                        read = bufferedInputStream.read(tmpByte);
                        if (read <= 0) {//더 이상 읽어들일 값이 없는 경우 -1을 출력하기 때문에 읽어들이는 반복문을 탈출한다.
                            break;
                        }
                        bufferedOutputStream.write(tmpByte, 0, read);//파일에 값을 써준다.(read를 tmpByte에 1byte씩 추가해주는 것)
                    }
                    bufferedInputStream.close();
                    bufferedOutputStream.close();
                    httpURLConnection.disconnect();
                    //갤러리를 브로드캐스트 리시버를 통해서 업데이트를 해줘야 다운로드된 파일이 갤러리에 뜨게 된다.
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                    downloadDialog.updateDownloadCount();//다운로드 완료 수 업데이트
                }

                return "success";

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
                Toast.makeText(getApplicationContext(), "다운로드 완료", Toast.LENGTH_SHORT).show();
                downloadDialog.completeDownload();//다이얼로그를 다운로드 완료 상태로 전환

            } else {
                Toast.makeText(getApplicationContext(), "다운로드 실패", Toast.LENGTH_SHORT).show();
                downloadDialog.failDownload();//다이얼로그를 다운로드 실패 상태로 전환
            }


        }

        //다운로드 아이얼로그에서 확인 버튼을 눌렀을 때 콜백되는 인터페이스 함수
        @Override
        public void onConfirmClicked() {
            downloadDialog.close();//다이얼로그 종료
            //선택모드 해제
            isSelectMode = false;//선택모드 false
            tv_cancel.setVisibility(View.GONE);//이미지 취소 버튼 GONE
            ib_download.setVisibility(View.GONE);//다운로드 버튼 GONE
            ib_check.setVisibility(View.VISIBLE);//이미지 선택 버튼 VISIBLE
            selectedContentIndexList.clear();//선택된 이미지 리스트를 비워준다.
            //모든 이미지의 선택 모드를 해제한다.
            for (int i = 0; i < chatContentArrayList.size(); i++) {
                chatContentArrayList.get(i).setSelectMode(false);//선택모드 해제
                chatContentArrayList.get(i).setSelected(false);//모든 이미지를 미선택 상태로 전환
            }
            chatContentListAdapter.notifyItemRangeChanged(0, chatContentListAdapter.getItemCount(), "cancel");
        }
    }

}
