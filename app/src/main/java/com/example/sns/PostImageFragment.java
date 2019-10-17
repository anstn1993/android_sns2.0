package com.example.sns;

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

public class PostImageFragment extends Fragment {

    public static PostImageFragment newInstance(String url) {

        Bundle args = new Bundle();
        args.putString("url", url);
        PostImageFragment fragment = new PostImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("onCreateView: ", "호출");
        View rootView = inflater.inflate(R.layout.postimagefragment,container,false);
        ImageView imageView = rootView.findViewById(R.id.imageview_viewpager);

        Glide.with(getContext())
                .load(getArguments().getString("url"))
                .thumbnail(0.1f)
                .apply(new RequestOptions().centerCrop())
                .into(imageView);
        Log.d("뷰페이저 프래그먼트 이미지 경로",getArguments().getString("url"));
        return rootView;
    }
}
