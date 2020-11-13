package com.example.sns.post.upload.adapter;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sns.util.ItemTouchHelperCallback;
import com.example.sns.R;
import com.example.sns.post.upload.model.UploadImageItem;

import java.util.ArrayList;
import java.util.Collections;

public class UploadImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperCallback.OnItemMoveListener {

    private ArrayList<UploadImageItem> uploadImageItemArrayList;
    private Activity context;
    //뷰타입을 구분하기 위한 변수
    private int TYPE_ITEM = 0;
    private int TYPE_FOOTER = 1;
    //리사이클러뷰를 클릭했을 때 그것을 인식할 리스너 설정
    private UploadImageRecyclerViewClickListener mListener;


    //리사이클러뷰를 클릭했을 때 동작할 메소드를 붙여줄 인터페이스
    public interface UploadImageRecyclerViewClickListener {
        //이미지를 클릭했을 때 호출되는 메소드
        void onImageClicked(int position);

        //이미지 삭제 버튼을 클릭했을 때 호출되는 메소드
        void onRemoveButtonClicked(int position);

        //이미지 추가 버튼을 클릭했을 때 호출되는 메소드
        void onAddImageClicked(int position);

        //드래그 앤 드롭이 시작됐을 때 호출되는 메소드
        void onStartDrag(RecyclerView.ViewHolder holder, int toPosition);
    }

    public void setOnClickListener(UploadImageRecyclerViewClickListener listener) {
        mListener = listener;
    }

    public UploadImageAdapter(ArrayList<UploadImageItem> uploadImageItemArrayList, Activity context) {
        this.uploadImageItemArrayList = uploadImageItemArrayList;
        this.context = context;
    }

    //아이템의 position별로 뷰타입을 정의해서 각각 다른 뷰홀더가 적용되게 해준다.
    //여기서 리턴되는 값이 onCreateViewHolder메소드의 두번째 param으로 전달된다.
    @Override
    public int getItemViewType(int position) {
        if (position == uploadImageItemArrayList.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    //리사이클러뷰 뷰 홀더에 씌울 레이아웃을 inflate
    //두번째 param:뷰타입
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.uploadimageitem, viewGroup, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.addimageitem, viewGroup, false);
            return new FooterHolder(view);
        }
    }

    //뷰 홀더의 각 레이아웃에 들어갈 데이터 설정
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {
        final int index = holder.getAdapterPosition();
        //홀더가 viewHolder클래스의 객체인 경우
        if (holder instanceof ViewHolder) {
            //arraylist에 들어있는 스트링 타입의 이미지 uri를 uri로 파싱해서 화면에 뿌려준다.
            RequestOptions options = new RequestOptions().centerCrop();

            Log.d("어댑터에서 본 uri", uploadImageItemArrayList.get(i).imageSource);

            //리사이클러뷰에 이미지를 뿌려줄 때 기존에 뿌려줬던 이미지를 재사용할 시 이미지가 로드되지 않는 문제가 발생
            //글라이드의 메모리에 들어간 이미지가 있어서 그런 것으로 추정되어서 메모리를 모두 비운 후 글라이드로 이미지를 넣어주는 방식으로 구현
            Glide.with(context)
                    .load(Uri.parse(uploadImageItemArrayList.get(i).imageSource))
                    .thumbnail(0.1f)
                    .apply(options)
                    .into(((ViewHolder) holder).iv_image);

            ((ViewHolder) holder).iv_delete.setImageResource(uploadImageItemArrayList.get(i).deleteID);

            if (mListener != null) {
                //리사이클러뷰의 이미지를 길게 클릭할 때의 리스너
                ((ViewHolder) holder).iv_image.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mListener.onStartDrag((ViewHolder) holder, i);
                        return false;
                    }
                });

                //리사이클러뷰의 이미지 클릭 리스너
                ((ViewHolder) holder).iv_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onImageClicked(i);
                    }
                });

                ((ViewHolder) holder).iv_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onRemoveButtonClicked(i);
                    }
                });
            }
        }
        //홀더가 footerHolder클래스의 객체인 경우
        else {
            //이미지 추가 버튼 클릭 리스너
            ((FooterHolder) holder).iv_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onAddImageClicked(i);
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        //삼항연산자: 어레이리스트가 null이면 사이즈 0, 아니면 어레이리스트의 사이즈만큼 아이템의 개수 설정
        return (uploadImageItemArrayList == null) ? 1 : uploadImageItemArrayList.size() + 1;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView iv_image, iv_delete;

        //생성자
        public ViewHolder(@NonNull View view) {
            super(view);

            iv_image = view.findViewById(R.id.imageview_image);
            iv_delete = view.findViewById(R.id.imageview_delete);

        }
    }

    private static class FooterHolder extends RecyclerView.ViewHolder {

        private ImageView iv_add;

        public FooterHolder(@NonNull View view) {
            super(view);
            iv_add = view.findViewById(R.id.imageview_add);
        }
    }

    //ItemTouchHelperCallback.OnItemMoveListener 인터페이스 메소드
    //아이템이 이동될때마다 계속 호출되는 메소드
    @Override
    public Boolean onItemMove(int fromPosition, int toPosition) {
        //이동하고자 하는 뷰홀더가 이미지 추가 버튼이 아닌 경우에만 호출
        if (fromPosition != uploadImageItemArrayList.size()) {
            //뷰홀더가 놓이는 position이 arraylist의 사이즈 범위를 넘어서지 않는 경우에만 다음 코드가 동작함
            if (toPosition <= uploadImageItemArrayList.size() - 1) {
                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(uploadImageItemArrayList, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(uploadImageItemArrayList, i, i - 1);
                    }
                }

                //두 아이템의 위치가 바뀌었을 때 사용하는 notify로 주로 드래그앤 드롭에서 사용됨
                notifyItemMoved(fromPosition, toPosition);

                for (int i = 0; i < uploadImageItemArrayList.size(); i++) {
                    Log.d("바뀐 arraylist", uploadImageItemArrayList.get(i).imageSource);
                }
            }
        }
        return true;
    }

    //드래그 앤 드롭이 끝나면 호출되는 메소드
    @Override
    public void onItemMoved(int position) {
        notifyDataSetChanged();
    }
}
