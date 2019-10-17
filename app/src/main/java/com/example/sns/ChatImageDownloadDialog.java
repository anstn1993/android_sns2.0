package com.example.sns;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ChatImageDownloadDialog {

    interface ChatImageDownloadDialogClickListener {
        void onConfirmClicked();
    }
    ChatImageDownloadDialogClickListener mListener;

    Context context;
    Dialog downloadDialog;//채팅 다운로드 다이얼로그
    TextView tv_title;//이미지 다운로드가 완료되면 다운로드 완료라는 표시가 뜨게 할 예정
    ProgressBar progressBar;//프로그래스 바
    TextView tv_completeCount;//다운로드가 완료된 이미지 수를 보여주는 텍스트뷰
    TextView tv_totalCount;//총 이미지 개수
    Button btn_complete;//다운로드가 완성되면 나타날 버튼
    int downloadCount = 0;//다운로드가 완료된 이미지수
    //생성자에서 다이얼로그의 뷰까지 모두 설정해준다.
    public ChatImageDownloadDialog(Context context, ChatImageDownloadDialogClickListener listener) {
        this.mListener = listener;
        this.context = context;
        downloadDialog = new Dialog(context);
        downloadDialog.setCanceledOnTouchOutside(false);//다이얼로그의 바깥 부분을 클릭하면 다이얼로그가 종료되는 현상 방지
        downloadDialog.setContentView(R.layout.chatimagedownload_box);
        tv_title = downloadDialog.findViewById(R.id.textview_title);
        progressBar = downloadDialog.findViewById(R.id.progressbar);
        btn_complete = downloadDialog.findViewById(R.id.button_complete);
        tv_completeCount = downloadDialog.findViewById(R.id.textview_completecount);
        tv_totalCount = downloadDialog.findViewById(R.id.textview_totalcount);
        tv_completeCount.setText(String.valueOf(downloadCount));//처음 생성자를 만들 때는 다운로드 된 이미지 수를 0으로 셋
    }


    //다운로드할 이미지 수 셋 메소드
    public void setTotalCount(int count) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                tv_totalCount.setText(String.valueOf(count));
            }
        });

    }
    //다운로드된 이미지 수 업데이트 메소드
    public void updateDownloadCount() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                downloadCount+=1;
                tv_completeCount.setText(String.valueOf(downloadCount));
            }
        });
    }

    //다이얼로그 show메소드
    public void show() {
        downloadDialog.show();
    }

    //다운로드 완료시 처리할 동작 메소드
    public void completeDownload(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                tv_title.setText("다운로드 완료");//타이틀을 다운로드 완료로 전환
                progressBar.setVisibility(View.INVISIBLE);//프로그래스 바 invisible
                btn_complete.setVisibility(View.VISIBLE);//확인 버튼 visible
                //확인 버튼 클릭 리스너
                btn_complete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onConfirmClicked();
                    }
                });
            }
        });
    }
    //다운로드 실패시 처리할 동작 메소드
    public void failDownload(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                tv_title.setText("다운로드 실패");//타이틀을 다운로드 실패로 전환
                progressBar.setVisibility(View.INVISIBLE);//프로그래스 바 invisible
                btn_complete.setVisibility(View.VISIBLE);//확인 버튼 visible
                //확인 버튼 클릭 리스너
                btn_complete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onConfirmClicked();
                    }
                });

            }
        });
    }

    //다이얼로그 dismiss메소드
    public void close() {
        downloadDialog.dismiss();
    }

}
