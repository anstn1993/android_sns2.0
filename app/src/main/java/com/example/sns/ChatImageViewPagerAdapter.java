package com.example.sns;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class ChatImageViewPagerAdapter extends FragmentPagerAdapter {

    ArrayList<ChatImageDetailItem> chatImageDetailItemArrayList;

    public ChatImageViewPagerAdapter(FragmentManager fm, ArrayList<ChatImageDetailItem> chatImageDetailItemArrayList) {
        super(fm);
        this.chatImageDetailItemArrayList = chatImageDetailItemArrayList;
    }

    @Override
    public Fragment getItem(int i) {
        return ChatImageDetailFragment.newInstance("http://13.124.105.47/chatimage/"+chatImageDetailItemArrayList.get(i).image);
    }

    @Override
    public int getCount() {
        return (chatImageDetailItemArrayList == null)?0:chatImageDetailItemArrayList.size();
    }
}
