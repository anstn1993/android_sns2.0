package com.example.sns;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final int VIEW_USER = 0;
    final int VIEW_PROGRESS = 1;

    Context context;
    ArrayList<UserItem> userItemArrayList;

    public UserAdapter(Context context, ArrayList<UserItem> userItemArrayList) {
        this.context = context;
        this.userItemArrayList = userItemArrayList;
    }

    interface UserRecyclerViewListener {
        void onContainerClicked(int position);
        void onCheckBoxClicked(int position);
    }

    UserRecyclerViewListener mListener;

    public void setOnClickListener(UserRecyclerViewListener listener) {
        this.mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {

        return (userItemArrayList.get(position) == null)?VIEW_PROGRESS:VIEW_USER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if(viewType == VIEW_USER){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.userlistitem, viewGroup, false);
            return new UserViewHolder(view);
        }else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.progressitem, viewGroup, false);
            return new progressViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof UserViewHolder){
            //유저의 프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + userItemArrayList.get(position).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((UserViewHolder) holder).cv_profile);

            //유저의 닉네임 설정
            ((UserViewHolder) holder).tv_nickname.setText(userItemArrayList.get(position).nickname);
            //유저의 이름 설정
            ((UserViewHolder) holder).tv_name.setText(userItemArrayList.get(position).name);
            //유저가 채팅방에 참여중인 사용자가 아닌 경우
            if(!userItemArrayList.get(position).isCurrentParticipant) {
                //체크박스 설정
                ((UserViewHolder) holder).checkBox.setVisibility(View.VISIBLE);

                if(userItemArrayList.get(position).isSelected){
                    ((UserViewHolder) holder).checkBox.setChecked(true);
                }
                else {
                    ((UserViewHolder) holder).checkBox.setChecked(false);
                }

                ((UserViewHolder) holder).checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onCheckBoxClicked(position);
                    }
                });
                ((UserViewHolder) holder).container.setClickable(true);
                //아이템 클릭 리스너 설정
                ((UserViewHolder) holder).container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onContainerClicked(position);
                    }
                });

                //선택 가능한 유저이기 때문에 배경은 가려준다
                ((UserViewHolder) holder).background.setVisibility(View.INVISIBLE);
            }
            //이미 참여중인 사용자인 경우
            else {
                //체크박스는 안 보이게 처리
                ((UserViewHolder) holder).checkBox.setVisibility(View.GONE);
                //배경을 줘서 선택 불가능한 상태라는 것을 표시
                ((UserViewHolder) holder).background.setVisibility(View.VISIBLE);
                //아이템 클릭 금지
                ((UserViewHolder) holder).container.setClickable(false);
            }

        }
        else {

        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        //notifyitemchanged당시 payload가 함께 넘어오지 않은 경우
        if(payloads.isEmpty()){
            super.onBindViewHolder(holder, position, payloads);
        }
        //payload가 함께 넘어온 경우
        else {
            String payload = (String) payloads.get(0);
            if(TextUtils.equals(payload, "true") && holder instanceof UserViewHolder){
                ((UserViewHolder) holder).checkBox.setChecked(true);
            }
            else {
                ((UserViewHolder) holder).checkBox.setChecked(false);
            }
        }

    }

    @Override
    public int getItemCount() {
        return (userItemArrayList == null) ? 0 : userItemArrayList.size();
    }

    private class UserViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout container, background;
        CheckBox checkBox;
        CircleImageView cv_profile;
        TextView tv_nickname, tv_name;

        public UserViewHolder(View view) {
            super(view);

            container = view.findViewById(R.id.constraintlayout_container);
            checkBox = view.findViewById(R.id.checkbox);
            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_nickname = view.findViewById(R.id.textview_nickname);
            tv_name = view.findViewById(R.id.textview_name);
            background = view.findViewById(R.id.constraintlayout_background);
        }
    }

    private class progressViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        public progressViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progressbar);
        }
    }
}
