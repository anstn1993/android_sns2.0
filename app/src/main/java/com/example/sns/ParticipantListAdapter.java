package com.example.sns;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

class ParticipantListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //대화상대 추가 아이템 뷰타입
    private final int VIEW_ADD = 0;
    //참여자 목록 아이템 뷰타입
    private final int VIEW_PARTICIPANT = 1;
    //리사이클러뷰 아이템을 담을 어레이
    private ArrayList<ChatParticipantItem> chatParticipantItemArrayList;
    //리사이클러뷰가 작동하는 context
    private Context context;
    //어댑터 생성자(아이템 리스트와 context 셋)
    public ParticipantListAdapter(ArrayList<ChatParticipantItem> chatParticipantItemArrayList, Context context) {
        this.chatParticipantItemArrayList = chatParticipantItemArrayList;
        this.context = context;
    }


    //리사이클러뷰 아이템 클릭시 호출될 함수를 정의한 인터페이스
    interface ParticipantListRecyclerViewListener {
        void onContainerClicked(int position);
    }
    //인터페이스 객체
    ParticipantListRecyclerViewListener mListener;
    //인터페이스의 리스너를 설정해줄 메소드
    public void setOnClickListener(ParticipantListRecyclerViewListener listener) {
        this.mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_ADD;
        } else {
            return VIEW_PARTICIPANT;
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //사용자 추가 아이템인 경우
        if (viewType == VIEW_ADD) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adduseritem, viewGroup, false);
            return new AddUserItemViewHolder(view);
        }
        //참여자 리스트 아이템인 경우
        else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chatparticipantitem, viewGroup, false);
            return new ChatParticipantItemViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AddUserItemViewHolder) {
            //대화상대 추가 클릭 리스너
            ((AddUserItemViewHolder) holder).container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onContainerClicked(position);
                }
            });
        } else {
            //참여자 목록 클릭 리스너
            ((ChatParticipantItemViewHolder)holder).container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onContainerClicked(position);
                }
            });
            //참여자 프로필 사진 셋
            Glide.with(context).load("http://13.124.105.47/profileimage/" + chatParticipantItemArrayList.get(position-1).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((ChatParticipantItemViewHolder) holder).cv_profile);
            //참여자 닉네임 셋
            ((ChatParticipantItemViewHolder)holder).tv_nickname.setText(chatParticipantItemArrayList.get(position-1).nickname);
        }
    }


    @Override
    public int getItemCount() {
        return (chatParticipantItemArrayList == null) ? 1 : chatParticipantItemArrayList.size() + 1;
    }

    private class AddUserItemViewHolder extends RecyclerView.ViewHolder {

        TextView tv_addUser;
        ConstraintLayout container;

        public AddUserItemViewHolder(View view) {
            super(view);
            tv_addUser = view.findViewById(R.id.textview_adduser);
            container = view.findViewById(R.id.container);
        }
    }

    private class ChatParticipantItemViewHolder extends RecyclerView.ViewHolder {

        CircleImageView cv_profile;
        TextView tv_nickname;
        ConstraintLayout container;

        public ChatParticipantItemViewHolder(View view) {
            super(view);
            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_nickname = view.findViewById(R.id.textview_nickname);
            container = view.findViewById(R.id.container);
        }
    }
}
