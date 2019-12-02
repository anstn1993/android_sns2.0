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

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
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

public class EditVideoPostSecondActivity extends AppCompatActivity {

    private final String TAG = EditVideoPostSecondActivity.class.getSimpleName();
    private Button btn_back, btn_edit;
    private EditText et_article;
    //해시태그 라이브러리 클래스
    private HashTagHelper hashTagHelper;
    private TextView tv_location;
    private int postNum;//게시물 번호
    //게시글 담는 변수
    private String article = null;

    //위치 추가로 가져온 장소의 주소
    private String address = null;
    //위치 추가로 가져온 장소의 위도와 경도
    private double latitude;
    private double longitude;


    private PlayerView playerView;//exoplayer뷰
    private SimpleExoPlayer player;//exoplayer객체
    private ImageButton ib_muteVolume;//음소거 on/off버튼
    private boolean isFromServer = true;//넘어온 동영상이 로컬 uri인지 서버 uri인지 판별
    private String videoUri;//영상의 uri
    private int videoWidth;//영상의 가로 사이즈
    private int videoHeight;//영상의 세로 사이즈
    private File compressedVideoFile;//압축된 동영상 파일 객체

    private LoginUser loginUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video_post_second);
        loginUser = LoginUser.getInstance();
        btn_edit = findViewById(R.id.btn_edit);
        btn_back = findViewById(R.id.btn_back);

        et_article = findViewById(R.id.et_article);
        playerView = findViewById(R.id.exoplayerview);
        ib_muteVolume = findViewById(R.id.imagebutton_muteonoff);

        //hashtag어댑터를 초기화해주고
        hashTagHelper = HashTagHelper.Creator.create(Color.parseColor("#02B2ED"), new HashTagHelper.OnHashTagClickListener() {
            @Override
            public void onHashTagClicked(String hashTag) {

            }
        });

        if(getIntent() != null) {
            postNum = getIntent().getIntExtra("postNum", 0);
            isFromServer = getIntent().getBooleanExtra("isFromServer", true);
            videoUri = getIntent().getStringExtra("uri");
        }
        initializePlayer(videoUri);//플레이어 초기화
        //해시태그를 적용하고자 하는 텍스트 영역을 handle해준다.
        hashTagHelper.handle(et_article);

        tv_location = findViewById(R.id.tv_location);


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

        //수정 버튼 클릭 리스너
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                article = et_article.getText().toString();
                ProgressDialog progressDialog = new ProgressDialog(EditVideoPostSecondActivity.this);
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

    //exoplayer 초기화
    private void initializePlayer(String sample) {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(this);
            //플레이어를 뷰와 연동
            playerView.setPlayer(player);
            //플레이어로 재생할 미디어 소스 생성
            MediaSource mediaSource = buildMediaSource(Uri.parse(sample));
            player.setVolume(0);//default: 소리 비활성화
            ib_muteVolume.setSelected(true);
            player.prepare(mediaSource);//prepare까지 오면 player에서 콜백되는 메소드들로 playback을 제어할 수 있게 된다.
            playerView.setControllerAutoShow(false);//컨트롤러 자동 활성화 막음
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setRepeatMode(Player.REPEAT_MODE_ALL);

            player.setPlayWhenReady(true);//playback을 재생하거나 일시정지할 수 있게 된다.(true: 자동재생, false: 자동재생x)
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


    //게시물 수정 버튼을 누를 때 서버로 데이터를 넘겨주는 메소드
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
                progressDialog.show(EditVideoPostSecondActivity.this, "게시물 수정", "잠시만 기다려주세요", true, false);
            }
        });


        //레트로핏 세팅
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://13.124.105.47/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        //서버로 보내줄 데이터 param 설정
        RequestBody postNumPart = RequestBody.create(MultipartBody.FORM, String.valueOf(postNum));
        RequestBody accountPart = RequestBody.create(MultipartBody.FORM, loginUser.getAccount());
        //초기화를 바로 해주지 않는 이유는 이 데이터들은 쓰일 수도 있고 안 쓰일 수도 있기 때문이다.
        RequestBody articlePart = null;
        RequestBody addressPart = null;
        RequestBody latitudePart = null;
        RequestBody longitudePart = null;

        //동영상
        RequestBody videoFile = null;
        MultipartBody.Part body = null;


        //레트로핏 인터페이스 설정
        RetrofitService retrofitService;
        Call<UploadVideoResponse> call;

        if(!isFromServer) {//로컬에 있는 동영상을 서버로 올릴 때만 동영상 압축을 해서 압축된 파일을 만든다.
            //동영상 파일의 이름
            String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
            String videoFileName = loginUser.getAccount() + timeStamp + ".mp4";

            //동영상을 압축해주는 객체 선언(압축 사이즈 1280X720 or 720X1280 or 720X720)
            CompressMedia compressMedia = new CompressMedia(
                    videoUri,
                    videoFileName,
                    EditVideoPostSecondActivity.this,
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

            compressedVideoFile = new File(filePath);//압축된 동영상 파일
            videoFile = RequestBody.create(MediaType.parse("multipart/form-data"), compressedVideoFile);
            body = MultipartBody.Part.createFormData("video", videoFileName, videoFile);
        }

        retrofitService = retrofit.create(RetrofitService.class);

//        게시글x 위치x
        if (article == null && address == null) {
        }

        //게시글o, 위치x
        else if (article != null && address == null) {
            articlePart = RequestBody.create(MultipartBody.FORM, article);
        }
        //게시글x, 위치o
        else if (article == null && address != null) {
            addressPart = RequestBody.create(MultipartBody.FORM, address);
            latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
            longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));
        }
        //게시글o, 위치o
        else {

            articlePart = RequestBody.create(MultipartBody.FORM, article);
            addressPart = RequestBody.create(MultipartBody.FORM, address);
            latitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
            longitudePart = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));
        }

        call = retrofitService.editVideoResponse(postNumPart, accountPart, articlePart, body, addressPart, latitudePart, longitudePart);
        call.enqueue(new Callback<UploadVideoResponse>() {
            @Override
            public void onResponse(Call<UploadVideoResponse> call, Response<UploadVideoResponse> response) {
                if(!isFromServer) {
                    compressedVideoFile.delete();//압축된 파일 삭제
                }
                //작업이 완료되면 로딩 다이얼로그를 없애주고 메인화면으로 인텐트
                progressDialog.dismiss();

                Toast.makeText(getApplicationContext(), "수정 완료", Toast.LENGTH_SHORT).show();
                PostActivity.isEditted = true;


                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                EditVideoPostFirstActivity.editVideoPostEirstActivity.finish();
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

    public void setArticleAddress() {
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
}
