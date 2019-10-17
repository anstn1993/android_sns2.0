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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.example.sns.LoginActivity.httpURLConnection;

public class ChatImageListActivity extends AppCompatActivity implements ChatImageAdapter.ChatImageRecyclerViewListener {
    private int roomNum = 0;
    private ImageButton ib_check, ib_back, ib_download;
    private TextView tv_cancel;
    //채팅 이미지 데이터의 json스트링을 담아서 어댑터와 연동할 리스트
    private ArrayList<ChatImageItem> chatImageArrayList;
    private RecyclerView rv_chatImage;//리사이클러뷰
    private GridLayoutManager gridLayoutManager;//레이아웃 메니저
    private ChatImageAdapter chatImageAdapter;

    private boolean isSelectMode = false;//이미지 선택모드인지를 가리는 boolean

    private ArrayList<String> selectedImageArrayList;//선택모드에서 선택한 이미지들을 담는 리스트


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image_list);

        initView();//뷰 초기화
        setRecyclerView();//리사이클러뷰 셋
        selectedImageArrayList = new ArrayList<>();//선택모드에서 선택한 이미지 리스트 초기화

        //이미지 데이터 셋
        if (getIntent() != null) {
            int totalImageCount = getIntent().getIntExtra("totalImageCount", 0);
            for (int i = 0; i < totalImageCount; i++) {
                String imageData = getIntent().getStringExtra("imageData" + i);
                ChatImageItem chatImageItem = new ChatImageItem();
                chatImageItem.setImageData(imageData);
                chatImageItem.setSelectMode(false);
                chatImageItem.setSelected(false);
                chatImageArrayList.add(chatImageItem);
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
                for (int i = 0; i < chatImageArrayList.size(); i++) {
                    chatImageArrayList.get(i).setSelectMode(true);
                }
                chatImageAdapter.notifyItemRangeChanged(0, chatImageAdapter.getItemCount(), "selectMode");
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
                selectedImageArrayList.clear();//선택된 이미지 리스트를 비워준다.
                //모든 이미지의 선택 모드를 해제한다.
                for (int i = 0; i < chatImageArrayList.size(); i++) {
                    chatImageArrayList.get(i).setSelectMode(false);//선택모드 해제
                    chatImageArrayList.get(i).setSelected(false);//모든 이미지를 미선택 상태로 전환
                }
                chatImageAdapter.notifyItemRangeChanged(0, chatImageAdapter.getItemCount(), "cancel");
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
                //선택한 이미지
                UrlImageToFile urlImageToFile = new UrlImageToFile();
                urlImageToFile.execute(selectedImageArrayList);//선택한 이미지 리스트를 넘겨줘서 이미지들을 다운받는 스레드 실행
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

        chatImageArrayList = new ArrayList<>();

        rv_chatImage = findViewById(R.id.recyclerview_chatimage);
        rv_chatImage.setHasFixedSize(true);

        //레이아웃 메니저 초기화
        gridLayoutManager = new GridLayoutManager(this, 3);
        rv_chatImage.setLayoutManager(gridLayoutManager);
        //어댑터 초기화
        chatImageAdapter = new ChatImageAdapter(chatImageArrayList, this);
        chatImageAdapter.setOnClickListener(this);
        rv_chatImage.setAdapter(chatImageAdapter);
    }

    //리사이클러뷰 이미지를 클릭했을 때 호출되는 메소드
    @Override
    public void onImageClicked(int position) {
        //이미지 선택모드가 아닌 경우
        if (isSelectMode == false) {
            Intent intent = new Intent(ChatImageListActivity.this, ChatImageDetailActivity.class);
            intent.putExtra("imageListCount", chatImageArrayList.size());//전체 이미지 개수
            intent.putExtra("imageIndex", position);//선택한 이미지의 인덱스
            for (int i = 0; i < chatImageArrayList.size(); i++) {
                //이미지 데이터를 담는 json스트링을 이미지 수만큼 담아준다.
                intent.putExtra("imageData" + i, chatImageArrayList.get(i).imageData);
            }
            startActivity(intent);
        }
        //이미지 선택 모드인 경우
        else {
            //선택 상태인 상태면
            if (chatImageArrayList.get(position).isSelected) {
                chatImageArrayList.get(position).setSelected(false);//선택 취소로 전환
                try {
                    JSONObject imageData = new JSONObject(chatImageArrayList.get(position).imageData);
                    String image = imageData.getString("image");//선택 취소한 이미지
                    int index = selectedImageArrayList.indexOf(image);//이미지의 인덱스
                    selectedImageArrayList.remove(index);//선택 취소한 이미지를 이미지 선택 리스트에서 삭제
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //선택을 하지 않은 상태면
            else {
                chatImageArrayList.get(position).setSelected(true);//선택 상태로 전환
                try {
                    JSONObject imageData = new JSONObject(chatImageArrayList.get(position).imageData);
                    String image = imageData.getString("image");//선택한 이미지
                    selectedImageArrayList.add(image);//선택한 이미지를 이미지 선택 리스트에 추가
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            chatImageAdapter.notifyItemChanged(position, "check");
        }
    }

    //리사이클러뷰 이미지를 길게 클릭했을 때 호출되는 메소드
    @Override
    public void onImageLongClicked(int position) {
        //이미지 선택 모드가 아닌 상태에서만 동작
        if (isSelectMode == false) {
            isSelectMode = true;//선택모드 true
            ib_check.setVisibility(View.GONE);//이미지 선택 버튼 GONE
            tv_cancel.setVisibility(View.VISIBLE);//취소 버튼 visible
            ib_download.setVisibility(View.VISIBLE);//다운로드 버튼 visible
            //모든 이미지를 선택 모드로 전환해준다.
            for (int i = 0; i < chatImageArrayList.size(); i++) {
                chatImageArrayList.get(i).setSelectMode(true);
            }
            chatImageAdapter.notifyItemRangeChanged(0, chatImageAdapter.getItemCount(), "selectMode");
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
            selectedImageArrayList.clear();//선택된 이미지 리스트를 비워준다.
            //모든 이미지의 선택 모드를 해제한다.
            for (int i = 0; i < chatImageArrayList.size(); i++) {
                chatImageArrayList.get(i).setSelectMode(false);//선택모드 해제
                chatImageArrayList.get(i).setSelected(false);//모든 이미지를 미선택 상태로 전환
            }
            chatImageAdapter.notifyItemRangeChanged(0, chatImageAdapter.getItemCount(), "cancel");
        }
        //이미지 선택 모드가 아닌 경우에는 뒤로가기
        else {
            super.onBackPressed();
        }
    }

    public class UrlImageToFile extends AsyncTask<ArrayList<String>, Void, String> implements ChatImageDownloadDialog.ChatImageDownloadDialogClickListener {

        String connectURL = null;
        String filename;
        String savefolder = "/Download";
        //다운로드 다이얼로그 객체 초기화
        ChatImageDownloadDialog downloadDialog;
        ArrayList<String> selectedImageArrayList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadDialog = new ChatImageDownloadDialog(ChatImageListActivity.this, this);
            downloadDialog.show();//다이얼로그 화면에 출력
        }

        @Override
        protected String doInBackground(ArrayList<String>... params) {
            selectedImageArrayList = params[0];
            downloadDialog.setTotalCount(selectedImageArrayList.size());//다운로드할 이미지 수 셋
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
                for (int i = 0; i < selectedImageArrayList.size(); i++) {
                    //파일 이름 설정
                    Date date = new Date();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
                    filename = String.valueOf(simpleDateFormat.format(date));

                    //이미지 파일의 경로 설정
                    String filePath = savePath + "/" + filename + i + ".jpg";


                    //이미지를 저장하고 있는 서버 주소
                    connectURL = selectedImageArrayList.get(i);
                    URL url = new URL("http://13.124.105.47/chatimage/" + connectURL);
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
                        fileOutputStream.write(tmpByte, 0, read);//파일에 값을 써준다.(read를 tmpByte에 1byte씩 추가해주는 것)
                    }
                    inputStream.close();
                    fileOutputStream.close();
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
            selectedImageArrayList.clear();//선택된 이미지 리스트를 비워준다.
            //모든 이미지의 선택 모드를 해제한다.
            for (int i = 0; i < chatImageArrayList.size(); i++) {
                chatImageArrayList.get(i).setSelectMode(false);//선택모드 해제
                chatImageArrayList.get(i).setSelected(false);//모든 이미지를 미선택 상태로 전환
            }
            chatImageAdapter.notifyItemRangeChanged(0, chatImageAdapter.getItemCount(), "cancel");
        }
    }

}
