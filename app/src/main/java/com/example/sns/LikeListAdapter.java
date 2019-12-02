package com.example.sns;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class LikeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<LikeListItem> likeListItemArrayList;

    private Context context;

    private LikeListRecyclerViewListener mListener;

    public LikeListAdapter(Context context, ArrayList<LikeListItem> likeListItemArrayList) {
        this.likeListItemArrayList = likeListItemArrayList;
        this.context = context;
    }

    interface LikeListRecyclerViewListener{

        //컨테이너 클릭시 콜백
        void onContainerClicked(int position);
        //팔로우 버튼 클릭시 콜백
        void onFollowClicked(int position);
    }

    public void setOnClickListener(LikeListRecyclerViewListener mListener){
        this.mListener = mListener;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.likelistitem, viewGroup, false);

        return new LikeListItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        //리스트 컨테이너 클릭 리스너
        ((LikeListItemHolder)holder).cv_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onContainerClicked(position);
            }
        });

        //프로필 사진 설정
        Glide.with(context).load("http://13.124.105.47/profileimage/"+likeListItemArrayList.get(position).profile)
                .thumbnail(0.1f)
                .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                .into(((LikeListItemHolder)holder).cv_profile);


        //닉네임 설정
        ((LikeListItemHolder)holder).tv_nickname.setText(likeListItemArrayList.get(position).nickname);


        //현재 로그인 아이디와 좋아요한 계정이 일치하는 경우
        if(likeListItemArrayList.get(position).account.equals(LoginUser.getInstance().getAccount())){
            //팔로우 버튼을 없앤다.
            ((LikeListItemHolder)holder).btn_follow.setVisibility(View.GONE);
        }
        //팔로우를 하고 있는 상태인 경우
        else if(likeListItemArrayList.get(position).isFollowing){
            //팔로잉 버튼으로 설정
            ((LikeListItemHolder)holder).btn_follow.setText("팔로잉");
            ((LikeListItemHolder)holder).btn_follow.setBackgroundResource(R.drawable.et_border);
            ((LikeListItemHolder)holder).btn_follow.setTextColor(Color.parseColor("#000000"));

        }
        //팔로우를 하지 않고 있는 상태인 경우
        else {
            //팔로우 버튼으로 설정
            ((LikeListItemHolder)holder).btn_follow.setText("팔로우");
            ((LikeListItemHolder)holder).btn_follow.setBackgroundResource(R.drawable.bluebutton);
            ((LikeListItemHolder)holder).btn_follow.setTextColor(Color.parseColor("#ffffff"));

        }

        //팔로우 버튼 리스너 설정
        ((LikeListItemHolder)holder).btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFollowClicked(position);
            }
        });

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
                ((LikeListItemHolder)holder).btn_follow.setText("팔로잉");
                ((LikeListItemHolder)holder).btn_follow.setBackgroundResource(R.drawable.et_border);
                ((LikeListItemHolder)holder).btn_follow.setTextColor(Color.parseColor("#000000"));
            }
            //팔로우를 취소하는 경우
            else {
                //팔로우 버튼으로 설정
                ((LikeListItemHolder)holder).btn_follow.setText("팔로우");
                ((LikeListItemHolder)holder).btn_follow.setBackgroundResource(R.drawable.bluebutton);
                ((LikeListItemHolder)holder).btn_follow.setTextColor(Color.parseColor("#ffffff"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return (likeListItemArrayList == null)?0:likeListItemArrayList.size();
    }

    private class LikeListItemHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        CircleImageView cv_profile;
        TextView tv_nickname;
        Button btn_follow;

        public LikeListItemHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            cv_profile = itemView.findViewById(R.id.circleimageview_profile);
            tv_nickname = itemView.findViewById(R.id.textview_nickname);
            btn_follow = itemView.findViewById(R.id.button_follow);
        }
    }
}
