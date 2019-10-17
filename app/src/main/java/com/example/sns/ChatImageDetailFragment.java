package com.example.sns;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class ChatImageDetailFragment extends Fragment {
    public static ChatImageDetailFragment newInstance(String url) {
        
        Bundle args = new Bundle();
        args.putString("url", url);
        ChatImageDetailFragment fragment = new ChatImageDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chatimagedetail, container, false);
        ImageView imageView = rootView.findViewById(R.id.imageview_viewpager);
        Glide.with(getContext())
                .load(getArguments().getString("url"))
                .apply(new RequestOptions().centerCrop())
                .into(imageView);
        return rootView;
    }
}
