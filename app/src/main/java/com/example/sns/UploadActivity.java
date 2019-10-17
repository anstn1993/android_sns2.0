package com.example.sns;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;
import gun0912.tedimagepicker.TedImagePickerActivity;
import gun0912.tedimagepicker.builder.TedImagePicker;
import gun0912.tedimagepicker.builder.listener.OnSelectedListener;
import gun0912.tedimagepicker.builder.type.MediaType;

import static com.example.sns.MainActivity.uploadClicked;


//해당 액티비티는 tedBottomPicker라는 라이브러리를 실행시키기 위해서 만든 클래스
//main액티비티는 appcompatactivity를 상속받지 않고 activitygroup을 상속받아서 with()의 파라미터로 들어오는 프래그먼트 액티비티를 사용할 수 없다.
//그래서 해당 액티비티로 전환해서 실행시키는 것
public class UploadActivity extends AppCompatActivity {

    private final String TAG = "UploadActivity";

    //이미지 피커에서 이미지나 비디오를 선택했는지 하지 않고 그냥 취소했는지를 판별하는 boolean변수
    private boolean contentSelected = false;

    private List<Uri> selectedUriList;

    private String contentType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload2);
        Log.d("업로드 액티비티 onCreate", "호출");


        if(getIntent() != null) {
            contentType = getIntent().getStringExtra("content");//photo or video
        }

        //uploadClicked는 boolean타입 변수로 업로드 버튼을 누르는 순간 true, 업로드 행위를 취소하거나 업로드가 완료되면 false가 된다.
        if (uploadClicked) {

            if(contentType.equals("photo")) {//사진을 선택한 경우
                //tedbottompicker라이브러리
                TedBottomPicker.with(UploadActivity.this)
                        //이미지 피커액티비티가 올라오는 높이 설정
                        .setPeekHeight(2000)
                        .showTitle(false)
                        .setCompleteButtonText("완료")
                        .setEmptySelectionText("이미지 없음")
                        .setSelectedUriList(selectedUriList)
                        .setPreviewMaxCount(1000)
                        .setSelectMaxCount(6)
                        .setSelectMinCount(1)
                        .setEmptySelectionText("이미지를 선택해주세요.")
                        .showMultiImage(new TedBottomSheetDialogFragment.OnMultiImageSelectedListener() {

                            @Override
                            public void onImagesSelected(List<Uri> uriList) {
                                // here is selected image uri list
                                //이 메소드가 콜백됐다는 것은 이미지를 선택했음을 의미하기 때문에 불린 변수는 true가 된다.
                                contentSelected = true;
                                Intent intent = new Intent(UploadActivity.this, UploadFirstActivity.class);

                                for (int i = 0; i < uriList.size(); i++) {

                                    Log.d("uri" + (i + 1), uriList.get(i).toString());
                                    intent.putExtra("image" + (i + 1), uriList.get(i).toString());
                                }
                                intent.putExtra("imageCount", uriList.size());
                                startActivity(intent);
                                finish();
                            }
                        });
            }else {//동영상을 선택한 경우
                //tedbottompicker라이브러리
                TedBottomPicker.with(UploadActivity.this)
                        //이미지 피커액티비티가 올라오는 높이 설정
                        .setPeekHeight(2000)
                        .showVideoMedia()
                        .showTitle(false)
                        .setCompleteButtonText("완료")
                        .setEmptySelectionText("동영상 없음")
                        .setSelectedUriList(selectedUriList)
                        .setPreviewMaxCount(1000)
                        .setEmptySelectionText("동영상을 선택해주세요.")
                        .show(new TedBottomSheetDialogFragment.OnImageSelectedListener() {
                            @Override
                            public void onImageSelected(Uri uri) {
                                Log.d(TAG, "video uri: "+uri.toString());
                            }
                        });
//                        .showMultiImage(new TedBottomSheetDialogFragment.OnMultiImageSelectedListener() {
//
//                            @Override
//                            public void onImagesSelected(List<Uri> uriList) {
//                                // here is selected image uri list
//                                //이 메소드가 콜백됐다는 것은 이미지를 선택했음을 의미하기 때문에 불린 변수는 true가 된다.
//                                contentSelected = true;
//                                Intent intent = new Intent(UploadActivity.this, UploadFirstActivity.class);
//
//                                for (int i = 0; i < uriList.size(); i++) {
//
//                                    Log.d("uri" + (i + 1), uriList.get(i).toString());
//                                    intent.putExtra("image" + (i + 1), uriList.get(i).toString());
//                                }
//                                intent.putExtra("imageCount", uriList.size());
//                                startActivity(intent);
//                                finish();
//                            }
//
//
//                        });
//                TedImagePicker.with(this)
//                        .title("동영상 선택")
//                        .start(new OnSelectedListener() {
//                            @Override
//                            public void onSelected(Uri uri) {
//                                Log.d(TAG, "video uri: "+uri.toString());
//                            }
//                        });
            }
        }

    }

    //이미지 피커가 화면에서 사라지면 호출되는 콜백 메소드로 uploadactivity를 바로 종료시키기 위한 메소드
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d("업로드 액티비티 onWindowFC", "호출");
        //이미지 피커가 화면에서 사라졌다는 건 사용자가 업로드를 취소했거나 다음 과정으로 넘어갔음을 의미하기 때문에 uploadClicked는 false가 된다.
        uploadClicked = false;

//        이미지를 선택하지 않고 취소한 경우
        if (!contentSelected) {
            //다시 메인 액티비티로 화면 전환
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);


            //업로드 액티비티를 스택에서 지우고
            //메인 액티비티를 재활용해서 다시 스택 최상단으로 올린다.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //이미지 효과 없이 인텐트
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();

            contentSelected = false;
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("업로드 액티비티 onStart", "호출");


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("업로드 액티비티 onResume", "호출");


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("업로드 액티비티 onPause", "호출");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("업로드 액티비티 onStop", "호출");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("업로드 액티비티 onRestart", "호출");
    }


}
