package com.example.sns;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

public class UploadVideoSecondActivity extends AppCompatActivity {
    //비디오를 업로드하는 액티비티로 미디어 코덱 라이브러리를 통해서 비디오를 디먹싱(extract) -> 디코딩 -> 용량 압축 -> 인코딩 -> 먹싱의 과정을 거친다.
    private final String TAG = UploadVideoSecondActivity.class.getSimpleName();

    private Button btn_back, btn_upload;
    private EditText et_article;
    //해시태그 라이브러리 클래스
    private HashTagHelper hashTagHelper;
    private TextView tv_location;


    private PlayerView playerView;//exoplayer뷰
    private SimpleExoPlayer player;//exoplayer객체
    private boolean playWhenReady = true;
    private MediaSource mediaSource;
    private ImageButton ib_muteVolume;//음소거 on/off버튼
    private long currentPosition;//영상의 현재 재생 위치
    private String uri;//영상의 uri
    private int videoWidth;//영상의 가로 사이즈
    private int videoHeight;//영상의 세로 사이즈

    //게시글 담는 변수
    String article = null;

    //위치 추가로 가져온 장소의 주소
    private String address = null;
    //위치 추가로 가져온 장소의 위도와 경도
    private double latitude = 1000;
    private double longitude = 1000;

    File compressedVideoFile;//압축된 동영상 파일 객체



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video_second);
        btn_upload = findViewById(R.id.btn_upload);
        btn_back = findViewById(R.id.btn_back);
        et_article = findViewById(R.id.et_article);
        playerView = findViewById(R.id.exoplayerview);
        ib_muteVolume = findViewById(R.id.imagebutton_muteonoff);
        if (getIntent() != null) {
            uri = getIntent().getStringExtra("uri");
        }
//        String sample = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";;
        initializePlayer(uri);

        //hashtag어댑터를 초기화해주고
        hashTagHelper = HashTagHelper.Creator.create(Color.parseColor("#02B2ED"), new HashTagHelper.OnHashTagClickListener() {
            @Override
            public void onHashTagClicked(String hashTag) {

            }
        });


        //해시태그를 적용하고자 하는 텍스트 영역을 handle해준다.
        hashTagHelper.handle(et_article);

        tv_location = findViewById(R.id.tv_location);


        //위치 추가 버튼 클릭 리스너
        tv_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddLocationActivity.class);
                startActivityForResult(intent, 0);

            }
        });


        //업로드 버튼 클릭 리스너
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //게시글
                article = et_article.getText().toString();
                ProgressDialog progressDialog = new ProgressDialog(UploadVideoSecondActivity.this);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        upload(article, address, latitude, longitude, videoWidth, videoHeight, progressDialog);
                    }
                }).start();
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

        ib_muteVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ib_muteVolume.isSelected()) {//mute되어있는 경우(소리 활성화하는 경우)
                    ib_muteVolume.setSelected(false);
                    player.setVolume(1);
                } else {//mute되어있지 않은 경우(소리 비활성화하는 경우)
                    ib_muteVolume.setSelected(true);
                    player.setVolume(0);
                }
            }
        });
    }

    //exoplayer 초기화
    private void initializePlayer(String sample) {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(this);
            //플레이어를 뷰와 연동
            playerView.setPlayer(player);
            //플레이어로 재생할 미디어 소스 생성
            mediaSource = buildMediaSource(Uri.parse(sample));
            player.setVolume(0);//default: 소리 비활성화
            ib_muteVolume.setSelected(true);
            player.prepare(mediaSource);//prepare까지 오면 player에서 콜백되는 메소드들로 playback을 제어할 수 있게 된다.
            playerView.setControllerAutoShow(false);//컨트롤러 자동 활성화 막음
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setRepeatMode(Player.REPEAT_MODE_ALL);

            player.setPlayWhenReady(playWhenReady);//playback을 재생하거나 일시정지할 수 있게 된다.(true: 자동재생, false: 자동재생x)
//            Format format = player.getVideoFormat();
//
            player.addVideoListener(new VideoListener() {
                @Override
                public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                    videoWidth = width;
                    videoHeight = height;
                    Log.d(TAG, "동영상 width:" + width);
                    Log.d(TAG, "동영상 height:" + height);
                    Log.d(TAG, "동영상 종횡비:" + pixelWidthHeightRatio);

                }
            });
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "SNS"));

        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
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

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
    }


    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
    }

    //게시물 업로드 버튼을 누를 때 서버로 데이터를 넘겨주는 메소드
    private void upload(
            String article,
            String address,
            double latitude,
            double longitude,
            int width,
            int height,
            ProgressDialog progressDialog) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show(UploadVideoSecondActivity.this, "게시물 업로드", "잠시만 기다려주세요", true, false);
            }
        });


        //동영상 파일의 이름
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String videoFileName = account + timeStamp + ".mp4";

        //동영상을 압축해주는 객체 선언(압축 사이즈 1280X720 or 720X1280 or 720X720)
        CompressMedia compressMedia = new CompressMedia(
                uri,
                videoFileName,
                UploadVideoSecondActivity.this,
                (width > height) ? 1280 : 720,
                (height > width) ? 1280 : 720);
        String filePath = null;//압축된 파일의 최종 경로
        try {
            //압축 시작
            filePath = compressMedia.startCompress();
            initializePlayer(filePath);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

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
        RequestBody articlePart = null;
        RequestBody addressPart = null;
        RequestBody latitudePart = null;
        RequestBody longitudePart = null;

        //동영상
        RequestBody videoFile;
        MultipartBody.Part body;


        //레트로핏 인터페이스 설정
        RetrofitService retrofitService;
        Call<UploadVideoResponse> call;


        compressedVideoFile = new File(filePath);//압축된 동영상 파일
        videoFile = RequestBody.create(MediaType.parse("multipart/form-data"), compressedVideoFile);


        body = MultipartBody.Part.createFormData("video", videoFileName, videoFile);


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



        retrofitService = retrofit.create(RetrofitService.class);
        call = retrofitService.uploadVideoResponse(postNumPart, accountPart, articlePart, body, addressPart, latitudePart, longitudePart);
        call.enqueue(new Callback<UploadVideoResponse>() {
            @Override
            public void onResponse(Call<UploadVideoResponse> call, Response<UploadVideoResponse> response) {

                compressedVideoFile.delete();//압축된 파일 삭제
                //작업이 완료되면 로딩 다이얼로그를 없애주고 메인화면으로 인텐트
                progressDialog.dismiss();

                Toast.makeText(getApplicationContext(), "업로드 완료", Toast.LENGTH_SHORT).show();
                isUploaded = true;


                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                UploadVideoFirstActivity.uploadVideoFirstActivity.finish();
                finish();


            }

            @Override
            public void onFailure(Call<UploadVideoResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "업로드 실패", Toast.LENGTH_SHORT).show();
                Log.d("게시물 업로드 실패 메세지", t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //비디오 플레이어 해제
        releasePlayer();
    }

    //자원을 점유하는 것을 막기 위해서 release해준다.
    private void releasePlayer() {
        if (player != null) {
            playerView.setPlayer(null);
            player.release();
            player = null;
        }
    }
}
