package com.example.sns.chat.fragment;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sns.chat.PlayerControl;
import com.example.sns.R;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class ChatContentDetailFragment extends Fragment implements PlayerControl {

    private final String TAG = ChatContentDetailFragment.class.getSimpleName();

    private PlayerView playerView;//동영상을 재생시킬 뷰와 exo플레이어
    private SimpleExoPlayer player;
    private String contentUri;
    private View rootView;
    private String type;//컨텐츠 타입

    public static ChatContentDetailFragment newInstance(String... param) {
        Bundle args = new Bundle();
        args.putString("type", param[0]);//컨텐츠 타입
        args.putString("contentUri", param[1]);//컨텐츠 경로
        ChatContentDetailFragment fragment = new ChatContentDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        type = getArguments().getString("type");//컨텐츠 타입
        contentUri = getArguments().getString("contentUri");//컨텐츠 경로
        if (type.equals("image")) {//컨텐츠 타입이 이미지인 경우
            rootView = inflater.inflate(R.layout.fragment_chatimagedetail, container, false);
            ImageView imageView = rootView.findViewById(R.id.imageview_viewpager);
            Glide.with(getContext())
                    .load(contentUri)
                    .apply(new RequestOptions().centerCrop().frame(0))
                    .into(imageView);

        } else {//컨텐츠 타입이 동영상인 경우
            rootView = inflater.inflate(R.layout.fragment_chatvideodetail, container, false);
            playerView = rootView.findViewById(R.id.exoplayerview);
            initializePlayer(contentUri);
        }


        return rootView;
    }

    private void initializePlayer(String contentUri) {
        if (player == null) {
            Log.d(TAG, "initialize player");
            player = ExoPlayerFactory.newSimpleInstance(getContext());
            playerView.setPlayer(player);
            MediaSource mediaSource = buildMediaSource(Uri.parse(contentUri));
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.prepare(mediaSource);
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.release();
            player = null;
            playerView.setPlayer(null);
            Log.d(TAG, "player released");
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "SNS"));
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart 호출");
        if(type.equals("video")) {
            initializePlayer(contentUri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume 호출");
        if(type.equals("video")) {
            initializePlayer(contentUri);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause 호출");
        if(type.equals("video")) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop 호출");
        if(type.equals("video")) {
            releasePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy 호출");
        if(type.equals("video")) {
            releasePlayer();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        if(type.equals("video")) {
            releasePlayer();
        }
    }

    @Override
    public void onInitializePlayer() {
        Log.d(TAG, "onInitializePlayer call back");
        initializePlayer(contentUri);
    }

    @Override
    public void onReleasePlayer() {
        Log.d(TAG, "onReleasePlayer call back");
        releasePlayer();
    }


}
