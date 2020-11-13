package com.example.sns.post.upload.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.util.Log;
import com.example.sns.post.upload.fragment.UploadImageFragment;

import java.util.ArrayList;


public class UploadImageViewPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<String> imageArrayList;


    //뷰 페이저 어댑터의 생성자
    public UploadImageViewPagerAdapter(FragmentManager fm, ArrayList<String> imageArrayList) {
        super(fm);
        this.imageArrayList = imageArrayList;
    }

    //param은 뷰페이저의 인덱스
    @Override
    public Fragment getItem(int i) {
        Log.d("뷰페이저 이미지 소스:", imageArrayList.get(i));
        return UploadImageFragment.newInstance(imageArrayList.get(i));
    }

    @Override
    public int getCount() {
        Log.d("뷰페이저 사이즈: ", String.valueOf(imageArrayList.size()));
        return imageArrayList.size();
    }
}
