package com.example.sns;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class GridPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //뷰타입 설정
    final int VIEW_POST = 0;
    final int VIEW_PROGRESS = 1;

    //아이템을 담을 arraylist
    ArrayList<PostItem> postItemArrayList;

    Context context;
    //생성자
    public GridPostAdapter(ArrayList<PostItem> postItemArrayList, Context context) {
        this.postItemArrayList = postItemArrayList;
        this.context = context;
    }

    //인터페이스 객체로 액티비티에서 리사이클러뷰의 홀더의 아이템을 클릭해서 동작이 가능하게끔 하기 위해서 정의
    GridPostRecyclerViewListener mListener;

    interface GridPostRecyclerViewListener{
        void onGridPictureClicked(int position);
    }

    public void setOnClickListener(GridPostRecyclerViewListener mListener){
        this.mListener = mListener;
    }

    @Override
    public int getItemViewType(int position) {
        return (postItemArrayList.get(position) == null)?VIEW_PROGRESS:VIEW_POST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        if(viewType == VIEW_POST){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.gridpostitem, viewGroup, false);
            return new GridViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.progressitem, viewGroup, false);
            return new progressViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {

        if(holder instanceof GridViewHolder){
            if(postItemArrayList.get(i).type.equals("image")){//이미지 게시물인 경우
                //게시물 이미지 설정(첫번째 이미지로 설정한다)
                Glide.with(context).load("http://13.124.105.47/uploadimage/"+postItemArrayList.get(i).imageList.get(0))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions()
                                .centerCrop()
                                .error(R.drawable.profile))
                        .into(((GridViewHolder)holder).iv_image);

                ((GridViewHolder) holder).iv_video.setVisibility(View.GONE);//비디오 아이콘 gone
                //게시물의 이미지 수가 1개인 경우
                if(postItemArrayList.get(i).imageList.size() == 1){
                    //다중 이미지 아이콘 없앰
                    ((GridViewHolder)holder).iv_multiple.setVisibility(View.GONE);
                }
                //게시물의 이미지 수가 2개 이상인 경우
                else {
                    //다중 이미지 아이콘 표시
                    ((GridViewHolder) holder).iv_multiple.setVisibility(View.VISIBLE);
                }
            }
            else {//동영상 게시물인 경우
                //동영상의 썸네일 설정
                Glide.with(context).load("http://13.124.105.47/uploadvideo/"+postItemArrayList.get(i).video)
                        .thumbnail(0.1f)
                        .apply(new RequestOptions()
                                .centerCrop()
                                .error(R.drawable.video)
                                .frame(0))
                        .into(((GridViewHolder)holder).iv_image);

                ((GridViewHolder) holder).iv_multiple.setVisibility(View.GONE);//이미지 아이콘 gone
                ((GridViewHolder) holder).iv_video.setVisibility(View.VISIBLE);//비디오 아이콘 visible
            }


        } else{

        }


    }

    @Override
    public int getItemCount() {
        return (postItemArrayList != null)?postItemArrayList.size():0;
    }

    private class GridViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_image, iv_multiple, iv_video;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_image = itemView.findViewById(R.id.imageview_gridpicture);
            iv_multiple = itemView.findViewById(R.id.imageview_multiple);
            iv_video = itemView.findViewById(R.id.imageview_video);
            iv_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onGridPictureClicked(getAdapterPosition());
                }
            });


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
