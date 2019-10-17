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

public class UploadImageFragment extends Fragment {

    public static UploadImageFragment newInstance(String position) {

        Bundle args = new Bundle();
        args.putString("position", position);

        UploadImageFragment fragment = new UploadImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView =inflater.inflate(R.layout.uploadimagefragment, container, false);
        ImageView imageView = rootView.findViewById(R.id.imageview_viewpager);

        Glide.with(getActivity()).load(getArguments().getString("position")).into(imageView);

        return rootView;
    }
}
