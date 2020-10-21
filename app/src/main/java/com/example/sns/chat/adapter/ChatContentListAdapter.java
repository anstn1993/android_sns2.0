package com.example.sns.chat.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.example.sns.R;
import com.example.sns.chat.model.ChatContentListItem;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatContentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ChatContentListItem> chatImageArrayList;
    private Context context;


    public ChatContentListAdapter(ArrayList<ChatContentListItem> chatImageArrayList, Context context) {
        this.chatImageArrayList = chatImageArrayList;
        this.context = context;
    }

    public interface ChatImageRecyclerViewListener {
        void onContentClicked(int position);//컨텐츠 클릭시 호출될 메소드

        void onContentLongClicked(int position);//컨텐츠 롱 클릭시 호출될 메소드
    }

    ChatImageRecyclerViewListener mListener;

    //어댑터 인터페이스의 리스너를 설정해주는 메소드
    public void setOnClickListener(ChatImageRecyclerViewListener listener) {
        this.mListener = listener;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chatcontentitem, viewGroup, false);
        return new ChatImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        try {
            JSONObject contentData = new JSONObject(chatImageArrayList.get(position).getContentData());
            String type = contentData.getString("type");//컨텐츠 타입
            String content = contentData.getString("content");//컨텐츠 파일 명
            if(type.equals("image")) {//이미지 컨텐츠인 경우
                //이미지 설정
                Glide.with(context)
                        .load("http://13.124.105.47/chatimage/" + content)
                        .apply(new RequestOptions().centerCrop())
                        .thumbnail(0.1f)
                        .into(((ChatImageViewHolder) holder).iv_image);
                //동영상 아이콘 invisible
                ((ChatImageViewHolder) holder).iv_video.setVisibility(View.INVISIBLE);
            }
            else {
                //이미지 설정
                Glide.with(context)
                        .load("http://13.124.105.47/chatvideo/" + content)
                        .apply(new RequestOptions().centerCrop().frame(0))
                        .thumbnail(0.1f)
                        .into(((ChatImageViewHolder) holder).iv_image);
                //동영상 아이콘 visible
                ((ChatImageViewHolder) holder).iv_video.setVisibility(View.VISIBLE);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //이미지 선택 모드가 아닌 경우(default상태)
        if (!chatImageArrayList.get(position).getIsSelected()) {
            ((ChatImageViewHolder) holder).ib_checkbox.setVisibility(View.GONE);//이미지 체크 박스 GONE설정
            ((ChatImageViewHolder) holder).frame.setBackground(null);//이미지 선택 테두리 없앰
        }
        //이미지 선택 모드인 경우
        else {
            ((ChatImageViewHolder) holder).ib_checkbox.setVisibility(View.VISIBLE);//이미지 체크 박스 VISIBLE설정
            //이미지가 선택된 상태인 경우
            if (chatImageArrayList.get(position).getIsSelected() == true) {
                ((ChatImageViewHolder) holder).ib_checkbox.setSelected(true);//이미지 체크 선택
                ((ChatImageViewHolder) holder).frame.setBackgroundResource(R.drawable.chatimage_select_border);//이미지 선택 테두리 적용
            }
            //이미지가 미선택 상태인 경우
            else {
                ((ChatImageViewHolder) holder).ib_checkbox.setSelected(false);//이미지 체크 해제
                ((ChatImageViewHolder) holder).frame.setBackground(null);//이미지 선택 테두리 없앰
            }
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            String payload = (String) payloads.get(0);
            //체크박스 클릭
            if (TextUtils.equals(payload, "check")) {
                //이미지 선택
                if (chatImageArrayList.get(position).getIsSelected()) {
                    ((ChatImageViewHolder) holder).ib_checkbox.setSelected(true);//체크박스를 선택 상태로 전환
                    ((ChatImageViewHolder) holder).frame.setBackgroundResource(R.drawable.chatimage_select_border);//이미지 선택 테두리 적용
                }
                //이미지 선택 해제
                else {
                    ((ChatImageViewHolder) holder).ib_checkbox.setSelected(false);//체크박스의 선택 상태 해제
                    ((ChatImageViewHolder) holder).frame.setBackground(null);//이미지 선택 테두리 없앰
                }
            }
            //선택 버튼 클릭
            else if (TextUtils.equals(payload, "selectMode")) {
                ((ChatImageViewHolder) holder).ib_checkbox.setVisibility(View.VISIBLE);//이미지 체크 박스 VISIBLE설정
            }
            //취소 버튼 클릭
            else if (TextUtils.equals(payload, "cancel")) {
                ((ChatImageViewHolder) holder).ib_checkbox.setVisibility(View.GONE);//이미지 체크 박스 GONE설정
                ((ChatImageViewHolder) holder).ib_checkbox.setSelected(false);
                ((ChatImageViewHolder) holder).frame.setBackground(null);//이미지 선택 테두리 없앰
            }
        }

    }

    @Override
    public int getItemCount() {
        return (chatImageArrayList == null) ? 0 : chatImageArrayList.size();
    }

    private class ChatImageViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        FrameLayout frame;
        ImageView iv_image, iv_video;
        ImageButton ib_checkbox;


        public ChatImageViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            iv_image = view.findViewById(R.id.imageview_gridpicture);
            iv_video = view.findViewById(R.id.imageview_video);
            ib_checkbox = view.findViewById(R.id.imagebutton_checkbox);
            frame = view.findViewById(R.id.frame);

            //이미지 클릭 리스너
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onContentClicked(getAdapterPosition());
                }
            });

            //이미지 롱클릭 리스너
            container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mListener.onContentLongClicked(getAdapterPosition());
                    return true;//false-다음 동작인 click이벤트로 전환, true-long클릭에서 이벤트 종료
                }
            });
        }
    }
}
