package com.example.sns;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchedPeopleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final int VIEW_USER = 0;
    final int VIEW_PROGRESS = 1;

    //아이템의 데이터를 담을 arraylist
    private ArrayList<SearchedPeopleItem> searchedPeopleItemArrayList;
    //어댑터와 연동할 context
    Context context;

    public SearchedPeopleAdapter(ArrayList<SearchedPeopleItem> searchedPeopleItemArrayList, Context context) {
        this.searchedPeopleItemArrayList = searchedPeopleItemArrayList;
        this.context = context;
    }

    interface SearchedPeopleRecyclerViewListener{
        void onListClicked(int position);
    }

    SearchedPeopleRecyclerViewListener mListener;

    public void setOnClickListener(SearchedPeopleRecyclerViewListener listener){
        this.mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return (searchedPeopleItemArrayList.get(position) == null)?VIEW_PROGRESS:VIEW_USER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        if(viewType == VIEW_USER){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.searchedpeopleitem, viewGroup, false);
            return new SearchedPeopleViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.progressitem, viewGroup, false);
            return new progressViewHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof SearchedPeopleViewHolder){
            //프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/"+searchedPeopleItemArrayList.get(position).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((SearchedPeopleViewHolder)holder).cv_profile);

            //닉네임 설정
            ((SearchedPeopleViewHolder)holder).tv_nickname.setText(searchedPeopleItemArrayList.get(position).nickname);
            //이름 설정
            ((SearchedPeopleViewHolder)holder).tv_name.setText(searchedPeopleItemArrayList.get(position).name);
            ((SearchedPeopleViewHolder)holder).container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onListClicked(position);
                }
            });
        }else {

        }


    }

    @Override
    public int getItemCount() {
        return (searchedPeopleItemArrayList == null)?0:searchedPeopleItemArrayList.size();
    }


    private class SearchedPeopleViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        CircleImageView cv_profile;
        TextView tv_nickname, tv_name;

        public SearchedPeopleViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_nickname = view.findViewById(R.id.textview_nickname);
            tv_name = view.findViewById(R.id.textview_name);
        }
    }

    //프로그래스 뷰 홀더(페이징 처리를 할 때 로딩 뷰로 활용 )
    private class progressViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public progressViewHolder(View view) {
            super(view);

            progressBar = view.findViewById(R.id.progressbar);
        }
    }
}
