package com.example.sns;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import gun0912.tedimagepicker.builder.TedImagePicker;
import gun0912.tedimagepicker.builder.listener.OnSelectedListener;


public class UploadVideoFirstActivity extends AppCompatActivity {
    private String TAG = UploadVideoFirstActivity.class.getSimpleName();
    private Button btn_cancel, btn_next, btn_selectVideo;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private String videoUri;//동영상 로컬 videoUri
    private ImageButton ib_muteVolume;//음소거 on/off버튼



    public static Activity uploadVideoFirstActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video_first);
        Log.d("onCreate", "호출");
        if(getIntent() != null) {
            videoUri = getIntent().getStringExtra("uri");
        }
        uploadVideoFirstActivity = UploadVideoFirstActivity.this;

        btn_selectVideo = findViewById(R.id.button_selectvideo);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_next = findViewById(R.id.btn_next);
        playerView = findViewById(R.id.exoplayerview);
        ib_muteVolume = findViewById(R.id.imagebutton_muteonoff);

        //플레이어 초기화
        initializePlayer(videoUri);
        //다음 버튼 클릭 리스너
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UploadVideoSecondActivity.class);
                intent.putExtra("uri", videoUri);
                startActivity(intent);
            }
        });

        btn_selectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TedImagePicker.with(UploadVideoFirstActivity.this)
                        .zoomIndicator(false)
                        .title("동영상 선택")
                        .video()
                        .start(new OnSelectedListener() {
                            @Override
                            public void onSelected(Uri uri) {
                                Log.d(TAG, "video videoUri: "+uri.toString());
                                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                                retriever.setDataSource(uri.getPath());
                                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                                long timeInmillisec = Long.parseLong(time);
                                long duration = timeInmillisec / 1000;
                                long hours = duration / 3600;
                                long minutes = (duration - hours * 3600) / 60;

                                if(minutes > 2) {//동영상의 길이가 5분을 넘어가면
                                    Toast.makeText(getApplicationContext(),"2분 이하의 동영상만 업로드 가능합니다.", Toast.LENGTH_SHORT).show();
                                }else{
                                    videoUri = uri.toString();
                                    releasePlayer();
                                    initializePlayer(videoUri);
                                }
                            }
                        });
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



        //취소버튼 클릭 리스너
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //액티비티 종료
                finish();
                return;
            }
        });
    }

    private void initializePlayer(String uri) {
        if(player == null) {
            player = ExoPlayerFactory.newSimpleInstance(this);
            playerView.setPlayer(player);//뷰에 플레이어 set
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            MediaSource mediaSource = buildMediaSource(Uri.parse(uri));//재생할 미디어 소스
            player.prepare(mediaSource);//미디어 소스를 player에 prepare
            player.setRepeatMode(Player.REPEAT_MODE_ALL);//무한 반복
            ib_muteVolume.setSelected(true);
            player.setVolume(0);//무음 모드
            player.setPlayWhenReady(true);
        }
    }

    private void releasePlayer(){
        if(player != null) {
            player.release();
            player = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "sns"));
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    //tedPermission라이브러리는 권한요청을 편리하게 할 수 있는 api
    private void tedPermission(){

        //권한요청이 일어나는 순간을 캐치하는 리스터 객체
        PermissionListener permissionListener=new PermissionListener() {
            //권한이 허용됐을 때 실행될 함수
            @Override
            public void onPermissionGranted() {

            }

            //권한이 허용되지 않았을 때 실행될 함수
            //파라미터는 허용되지 않은 권한을 담는 리스트가 리턴되어 들어간다.
            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }

        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("[설정]-[권한]에서 권한을 허용할 수 있습니다.")
                .setDeniedMessage("사진 및 파일을 저장하기 위해서는 접근 권한이 필요합니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();
    }


    @Override
    protected void onResume() {
        super.onResume();
        initializePlayer(videoUri);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestoy 호출");
        releasePlayer();
        super.onDestroy();
    }
}
