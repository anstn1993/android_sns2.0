package com.example.sns;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatContentViewPagerAdapter extends FragmentPagerAdapter {
    private final String TAG = ChatContentViewPagerAdapter.class.getSimpleName();
    ArrayList<ChatContentDetailItem> chatContentDetailItemArrayList;
    HashMap<Integer, ChatContentDetailFragment> contentFragments;


    public ChatContentViewPagerAdapter(FragmentManager fm, ArrayList<ChatContentDetailItem> chatContentDetailItemArrayList, HashMap<Integer, ChatContentDetailFragment> contentFragments) {
        super(fm);
        this.chatContentDetailItemArrayList = chatContentDetailItemArrayList;
        this.contentFragments = contentFragments;
    }

    @Override
    public Fragment getItem(int i) {
        Log.d(TAG, "getItem 호출");
        String type = chatContentDetailItemArrayList.get(i).type;//컨텐츠 타입(image or video)
        String contentUri = null;//컨텐츠 경로
        if (type.equals("image")) {//컨텐츠가 이미지인 경우
            contentUri = "http://13.124.105.47/chatimage/" + chatContentDetailItemArrayList.get(i).content;
        } else {//컨텐츠가 동영상인 경우
            contentUri = "http://13.124.105.47/chatvideo/" + chatContentDetailItemArrayList.get(i).content;
        }
        ChatContentDetailFragment fragment = ChatContentDetailFragment.newInstance(type, contentUri);
        contentFragments.put(i, fragment);//해쉬맵에 프래그먼트 put
        return fragment;

    }

    @Override
    public int getCount() {
        return (chatContentDetailItemArrayList == null) ? 0 : chatContentDetailItemArrayList.size();
    }

    //해쉬맵에 저장한 fragment getter
    public ChatContentDetailFragment getContentFragment(int position) {
        Log.d(TAG, "getContentFragment 호출");
        return contentFragments.get(position);
    }
}
