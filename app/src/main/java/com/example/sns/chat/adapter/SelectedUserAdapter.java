package com.example.sns.chat.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import com.example.sns.R;
import com.example.sns.chat.model.SelectedUserItem;
import de.hdodenhof.circleimageview.CircleImageView;

public class SelectedUserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<SelectedUserItem> selectedUserItemArrayList;

    public SelectedUserAdapter(Context context, ArrayList<SelectedUserItem> selectedUserItemArrayList) {
        this.context = context;
        this.selectedUserItemArrayList = selectedUserItemArrayList;
    }

    public interface SelectedUserRecyclerViewListener {
        void onRemoveClicked(int position);
    }

    SelectedUserRecyclerViewListener mListener;

    public void setOnClickListener(SelectedUserRecyclerViewListener listener){
        this.mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.selecteduserlistitem, viewGroup, false);
        return new SelectedUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        //선택된 사용자 프로필 이미지 설정
        Glide.with(context).load("http://13.124.105.47/profileimage/"+selectedUserItemArrayList.get(position).getProfile())
                .thumbnail(0.1f)
                .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                .into(((SelectedUserViewHolder)holder).cv_profile);

        //선택된 사용자 닉네임 설정
        ((SelectedUserViewHolder) holder).tv_nickname.setText(selectedUserItemArrayList.get(position).getNickname());

        //삭제 버튼 설정
        ((SelectedUserViewHolder) holder).ib_remove.setImageResource(R.drawable.delete);
        ((SelectedUserViewHolder) holder).ib_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRemoveClicked(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (selectedUserItemArrayList == null)?0:selectedUserItemArrayList.size();
    }

    private class SelectedUserViewHolder extends RecyclerView.ViewHolder {

        CircleImageView cv_profile;
        TextView tv_nickname;
        ImageView ib_remove;

        public SelectedUserViewHolder(View view) {
            super(view);
            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_nickname = view.findViewById(R.id.textview_nickname);
            ib_remove = view.findViewById(R.id.imagebutton_remove);
        }
    }
}
