package com.example.sns.search.adapter;

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

import com.example.sns.R;
import com.example.sns.search.model.SearchedTagItem;
import de.hdodenhof.circleimageview.CircleImageView;

public class SearchedTagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final int VIEW_TAG = 0;
    final int VIEW_PROGRESS = 1;


    private ArrayList<SearchedTagItem> searchedTagItemArrayList;
    private Context context;

    public SearchedTagAdapter(ArrayList<SearchedTagItem> searchedTagItemArrayList, Context context) {
        this.searchedTagItemArrayList = searchedTagItemArrayList;
        this.context = context;
    }

    public interface SearchedTagRecyclerViewListener{
        void onListClicked(int position);
    }

    SearchedTagRecyclerViewListener mListener;

    public void setOnClickListener(SearchedTagRecyclerViewListener listener){
        this.mListener = listener;
    }


    @Override
    public int getItemViewType(int position) {
        return (searchedTagItemArrayList.get(position) == null)?VIEW_PROGRESS:VIEW_TAG;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if(viewType == VIEW_TAG){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.searchedtagitem, viewGroup, false);
            return new SearchedTagViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.progressitem, viewGroup, false);
            return new ProgressViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {

        if(holder instanceof SearchedTagViewHolder){

            //대표 사진 설정
            if(searchedTagItemArrayList.get(i).getType().equals("image")) {//이미지 게시물인 경우
                Glide.with(context).load("http://13.124.105.47/uploadimage/"+searchedTagItemArrayList.get(i).getImage())
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop())
                        .into(((SearchedTagViewHolder)holder).cv_image);
            }
            else {//비디오 게시물인 경우
                Glide.with(context).load("http://13.124.105.47/uploadvideo/"+searchedTagItemArrayList.get(i).getVideo())
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().error(R.drawable.video))
                        .frame(0)
                        .into(((SearchedTagViewHolder)holder).cv_image);

            }
            //태그 설정
            ((SearchedTagViewHolder)holder).tv_tag.setText("#"+searchedTagItemArrayList.get(i).getTag());
            //게시물 수 설정
            ((SearchedTagViewHolder)holder).tv_count.setText("게시물 "+searchedTagItemArrayList.get(i).getCount()+"개");
            ((SearchedTagViewHolder)holder).container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onListClicked(i);
                }
            });
        }
        else {

        }

    }

    @Override
    public int getItemCount() {
        return (searchedTagItemArrayList == null)?0:searchedTagItemArrayList.size();
    }

    private class SearchedTagViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        CircleImageView cv_image;
        TextView tv_tag, tv_count;

        public SearchedTagViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            cv_image = view.findViewById(R.id.circleimageview_image);
            tv_tag = view.findViewById(R.id.textview_tag);
            tv_count = view.findViewById(R.id.textview_count);
        }
    }

    private class ProgressViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public ProgressViewHolder(View view) {
            super(view);

            progressBar = view.findViewById(R.id.progressbar);
        }
    }
}
