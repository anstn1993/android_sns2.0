package com.example.sns.follow.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import com.example.sns.R;
import com.example.sns.follow.model.FollowListItem;
import com.example.sns.login.model.LoginUser;
import de.hdodenhof.circleimageview.CircleImageView;


public class FollowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;

    ArrayList<FollowListItem> followListItemArrayList;

    public FollowAdapter(Context context, ArrayList<FollowListItem> followListItemArrayList) {
        this.context = context;
        this.followListItemArrayList = followListItemArrayList;
    }

    public interface FollowListRecyclerViewListener {
        void onFollowClicked(int position);
        void onListClicked(int position);
    }

    FollowListRecyclerViewListener mListener;

    public void setOnClickListener(FollowListRecyclerViewListener followListRecyclerViewListener){
        this.mListener = followListRecyclerViewListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.followlistitem, viewGroup, false);

        return new FollowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //리스트 클릭 리스너
        ((FollowViewHolder)holder).container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onListClicked(position);
            }
        });

        //프로필 사진 설정
        Glide.with(context).load("http://13.124.105.47/profileimage/"+followListItemArrayList.get(position).getProfile())
                .thumbnail(0.1f)
                .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                .into(((FollowViewHolder)holder).cv_profile);


        //닉네임 설정
        ((FollowViewHolder)holder).tv_nickname.setText(followListItemArrayList.get(position).getNickname());

        //현재 로그인 아이디와 팔로우리스트 계정이 일치하는 경우
        if(followListItemArrayList.get(position).getAccount().equals(LoginUser.getInstance().getAccount())){
            //팔로우 버튼을 없앤다.
            ((FollowViewHolder)holder).btn_follow.setVisibility(View.GONE);
        }
        //팔로우를 하고 있는 상태인 경우
        else if(followListItemArrayList.get(position).getIsFollowing()){
            //팔로잉 버튼으로 설정
            ((FollowViewHolder)holder).btn_follow.setText("팔로잉");
            ((FollowViewHolder)holder).btn_follow.setBackgroundResource(R.drawable.et_border);
            ((FollowViewHolder)holder).btn_follow.setTextColor(Color.parseColor("#000000"));

        }
        //팔로우를 하지 않고 있는 상태인 경우
        else {
            //팔로우 버튼으로 설정
            ((FollowViewHolder)holder).btn_follow.setText("팔로우");
            ((FollowViewHolder)holder).btn_follow.setBackgroundResource(R.drawable.bluebutton);
            ((FollowViewHolder)holder).btn_follow.setTextColor(Color.parseColor("#ffffff"));

        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        //페이로드가 notify시에 같이 넘어오지 않은 경우
        if(payloads.isEmpty()){
            super.onBindViewHolder(holder, position, payloads);
        }
        //페이로드가 notify시에 같이 넘어온 경우
        else {
            String payload = (String)payloads.get(0);
            //팔로우를 한 경우
            if(TextUtils.equals(payload, "true")){
                //팔로잉 버튼으로 설정
                ((FollowViewHolder)holder).btn_follow.setText("팔로잉");
                ((FollowViewHolder)holder).btn_follow.setBackgroundResource(R.drawable.et_border);
                ((FollowViewHolder)holder).btn_follow.setTextColor(Color.parseColor("#000000"));
            }
            //팔로우를 취소하는 경우
            else {
                //팔로우 버튼으로 설정
                ((FollowViewHolder)holder).btn_follow.setText("팔로우");
                ((FollowViewHolder)holder).btn_follow.setBackgroundResource(R.drawable.bluebutton);
                ((FollowViewHolder)holder).btn_follow.setTextColor(Color.parseColor("#ffffff"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return (followListItemArrayList == null)?0:followListItemArrayList.size();
    }

    public class FollowViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout container;
        CircleImageView cv_profile;
        TextView tv_nickname;
        Button btn_follow;

        public FollowViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            cv_profile = itemView.findViewById(R.id.circleimageview_profile);
            tv_nickname = itemView.findViewById(R.id.textview_nickname);
            btn_follow = itemView.findViewById(R.id.button_follow);

            //팔로우 버튼 리스너 설정
            btn_follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onFollowClicked(getAdapterPosition());
                }
            });

        }
    }
}
