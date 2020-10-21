package com.example.sns.post.edit.activity;

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

import com.example.sns.R;
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

import androidx.appcompat.app.AppCompatActivity;
import gun0912.tedimagepicker.builder.TedImagePicker;
import gun0912.tedimagepicker.builder.listener.OnSelectedListener;

public class EditVideoPostFirstActivity extends AppCompatActivity {
    private final String TAG = EditVideoPostFirstActivity.class.getSimpleName();

    private Button btn_cancel, btn_next, btn_selectVideo;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private ImageButton ib_muteVolume;//음소거 on/off버튼

    public static Activity editVideoPostEirstActivity;

    //수정 전 게시물의 게시글과 주소를 담을 변수다. 만약 게시글과 주소가 존재하지 않는다면 그대로 null처리
    String article = null;
    String address = null;
    String latitude = null;
    String longitude = null;

    //게시물 번호
    public int postNum;

    //서버에 저장된 동영상 uri
    private String videoUri;
    private boolean isFromServer = true;//업로드할 동영상이 기존 서버에 저장된 동영상인지 로컬에 저장된 동영상인지 가리는 용도

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video_post_first);
        Log.d("onCreate", "호출");

        editVideoPostEirstActivity = EditVideoPostFirstActivity.this;

        playerView = findViewById(R.id.exoplayerview);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_next = findViewById(R.id.btn_next);
        btn_selectVideo = findViewById(R.id.button_selectvideo);
        ib_muteVolume = findViewById(R.id.imagebutton_muteonoff);
        //게시글과 주소 처리
        setArticleAddress();

        if (getIntent() != null) {
            postNum = getIntent().getIntExtra("postNum", 0);
            Log.d("게시물 번호", String.valueOf(postNum));
            videoUri = getIntent().getStringExtra("uri");
        }


        //다음 버튼 클릭 리스너
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditVideoPostSecondActivity.class);
                intent.putExtra("postNum", postNum);//게시물 번호

                //게시글이 존재하는 경우 다시 게시글을 넘겨준다.
                if (article != null) {
                    intent.putExtra("article", article);
                }

                //주소가 존재하는 경우 다시 주소를 넘겨준다.
                if (address != null) {
                    intent.putExtra("address", address);
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                }

                intent.putExtra("uri", videoUri);
                intent.putExtra("isFromServer", isFromServer);
                startActivity(intent);
            }
        });

        btn_selectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TedImagePicker.with(EditVideoPostFirstActivity.this)
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
                                    isFromServer = false;
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


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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



    private void setArticleAddress() {
        Intent intent = getIntent();
        //게시글이 존재한다면 변수에 담아준다.
        if (intent.getStringExtra("article") != null) {
            article = intent.getStringExtra("article");
            Log.d("게시글 내용: ", article);
        }

        //주소가 존재한다면 변수에 담아준다.
        if (intent.getStringExtra("address") != null) {
            address = intent.getStringExtra("address");
            Log.d("주소 내용: ", address);
            latitude = intent.getStringExtra("latitude");
            Log.d("위도 내용: ", latitude);
            longitude = intent.getStringExtra("longitude");
            Log.d("경도 내용", longitude);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializePlayer(videoUri);//동영상 플레이어 초기화
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
