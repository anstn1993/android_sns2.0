package com.example.sns;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public final int VIEW_NONE = 0;
    public final int VIEW_ONE = 1;
    public final int VIEW_TWO = 2;
    public final int VIEW_THREE = 3;
    public final int VIEW_FOUR = 4;

    public ArrayList<ChatRoomItem> chatRoomItemArrayList;

    Context context;

    //시간 포맷
    private String fromFromat = "yyyy-MM-dd HH:mm:ss";
    private String toFormat = "M월 d일";

    //스와이프 메뉴 라이브러리에 있는 메소드로 이 객체는 스와이프 상태 저장을 하는 데 필요하다.
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public ChatRoomAdapter(ArrayList<ChatRoomItem> chatRoomItemArrayList, Context context) {
        this.chatRoomItemArrayList = chatRoomItemArrayList;
        this.context = context;
    }


    //리사이클러뷰 클릭 인터페이스
    interface ChatRoomRecyclerViewListener {
        void onContainerClicked(int position);

        void onExitRoomClicked(int position);
    }

    ChatRoomRecyclerViewListener mListener;

    public void setOnClickListener(ChatRoomRecyclerViewListener listener) {
        this.mListener = listener;
    }


    @Override
    public int getItemViewType(int position) {
        //채팅방에 자기밖에 안 남은 경우
        if (chatRoomItemArrayList.get(position).userList.size() == 0) {
            return VIEW_NONE;
        }
        //채팅방의 참여자 수가 2명인 경우(자기 포함)
        else if (chatRoomItemArrayList.get(position).userList.size() == 1) {
            return VIEW_ONE;
        }
        //채팅방의 참여자 수가 3명인 경우(자기 포함)
        else if (chatRoomItemArrayList.get(position).userList.size() == 2) {
            return VIEW_TWO;
        }
        //채팅방의 참여자 수가 4명인 경우(자기 포함)
        else if (chatRoomItemArrayList.get(position).userList.size() == 3) {
            return VIEW_THREE;
        }
        //채팅방의 참여자 수가 5명 이상인 경우(자기 포함)
        else {
            return VIEW_FOUR;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_NONE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chatlistitem_none, viewGroup, false);
            return new ChatListNoneViewHolder(view);
        } else if (viewType == VIEW_ONE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chatlistitem_one, viewGroup, false);
            return new ChatListOneViewHolder(view);
        } else if (viewType == VIEW_TWO) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chatlistitem_two, viewGroup, false);
            return new ChatListTwoViewHolder(view);
        } else if (viewType == VIEW_THREE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chatlistitem_three, viewGroup, false);
            return new ChatListThreeViewHolder(view);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chatlistitem_four, viewGroup, false);
            return new ChatListFourViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //한명과 대화하는 채팅방 리스트
        if (holder instanceof ChatListOneViewHolder) {


            String nickname = chatRoomItemArrayList.get(position).userList.get(0).nickname;
            String profile = chatRoomItemArrayList.get(position).userList.get(0).profile;
            //채팅방 상대의 프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((ChatListOneViewHolder) holder).cv_profile);

            //채팅방 제목 설정
            ((ChatListOneViewHolder) holder).tv_title.setText(nickname);

            if (chatRoomItemArrayList.get(position).message != null) {
                //채팅방 마지막 메세지 설정
                String message = chatRoomItemArrayList.get(position).message;
                ((ChatListOneViewHolder) holder).tv_content.setText(message);
            }

            if (chatRoomItemArrayList.get(position).time != null) {
                //채팅방 마지막 메세지 도착 시간 설정
                String time = ChatImageDetailActivity.formatDate(chatRoomItemArrayList.get(position).time, fromFromat, toFormat);
                ((ChatListOneViewHolder) holder).tv_time.setText(time);

            }

            //미확인 메세지 개수 설정
            //미확인 메세제가 없는 경우
            if (chatRoomItemArrayList.get(position).newMessageCount == 0) {
                //미확인 메세지 뷰를 안 보이게 처리
                ((ChatListOneViewHolder) holder).tv_messageCount.setVisibility(View.GONE);
            }
            //미확인 메세지가 존재하는 경우
            else {
                ((ChatListOneViewHolder) holder).tv_messageCount.setVisibility(View.VISIBLE);
                int newMessageCount = chatRoomItemArrayList.get(position).newMessageCount;
                ((ChatListOneViewHolder) holder).tv_messageCount.setText(String.valueOf(newMessageCount));
            }

            viewBinderHelper.bind(((ChatListOneViewHolder) holder).swipeLayout, String.valueOf(chatRoomItemArrayList.get(position).roomNum));
            //스와이프하면 나오는 나가기 버튼 클릭 리스너
            ((ChatListOneViewHolder) holder).tv_exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onExitRoomClicked(position);
                }
            });
        }
        //두명과 대화하는 채팅방 리스트
        else if (holder instanceof ChatListTwoViewHolder) {


            String nickname = chatRoomItemArrayList.get(position).userList.get(0).nickname;
            String profile = chatRoomItemArrayList.get(position).userList.get(0).profile;
            //채팅방 상대의 프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((ChatListTwoViewHolder) holder).cv_profile);


            String nickname2 = chatRoomItemArrayList.get(position).userList.get(1).nickname;
            String profile2 = chatRoomItemArrayList.get(position).userList.get(1).profile;
            //채팅방 상대의 프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + profile2)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((ChatListTwoViewHolder) holder).cv_profile2);

            //채팅방 제목 설정
            ((ChatListTwoViewHolder) holder).tv_title.setText(nickname + ", " + nickname2);

            //채팅방 마지막 메세지 설정
            if (chatRoomItemArrayList.get(position).message != null) {
                String message = chatRoomItemArrayList.get(position).message;
                ((ChatListTwoViewHolder) holder).tv_content.setText(message);
            }

            //채팅방 마지막 메세지 도착 시간 설정
            if (chatRoomItemArrayList.get(position).time != null) {
                String time = ChatImageDetailActivity.formatDate(chatRoomItemArrayList.get(position).time, fromFromat, toFormat);
                ((ChatListTwoViewHolder) holder).tv_time.setText(time);
            }

            //미확인 메세지 개수 설정
            //미확인 메세제가 없는 경우
            if (chatRoomItemArrayList.get(position).newMessageCount == 0) {
                //미확인 메세지 뷰를 안 보이게 처리
                ((ChatListTwoViewHolder) holder).tv_messageCount.setVisibility(View.GONE);
            }
            //미확인 메세지가 존재하는 경우
            else {
                ((ChatListTwoViewHolder) holder).tv_messageCount.setVisibility(View.VISIBLE);
                int newMessageCount = chatRoomItemArrayList.get(position).newMessageCount;
                ((ChatListTwoViewHolder) holder).tv_messageCount.setText(String.valueOf(newMessageCount));
            }

            viewBinderHelper.bind(((ChatListTwoViewHolder) holder).swipeLayout, String.valueOf(chatRoomItemArrayList.get(position).roomNum));
            //스와이프하면 나오는 나가기 버튼 클릭 리스너
            ((ChatListTwoViewHolder) holder).tv_exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onExitRoomClicked(position);
                }
            });
        }
        //세명과 대화하는 채팅방 리스트
        else if (holder instanceof ChatListThreeViewHolder) {


            String nickname = chatRoomItemArrayList.get(position).userList.get(0).nickname;
            String profile = chatRoomItemArrayList.get(position).userList.get(0).profile;
            //채팅방 상대의 프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((ChatListThreeViewHolder) holder).cv_profile);


            String nickname2 = chatRoomItemArrayList.get(position).userList.get(1).nickname;
            String profile2 = chatRoomItemArrayList.get(position).userList.get(1).profile;
            //채팅방 상대의 프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + profile2)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((ChatListThreeViewHolder) holder).cv_profile2);

            String nickname3 = chatRoomItemArrayList.get(position).userList.get(2).nickname;
            String profile3 = chatRoomItemArrayList.get(position).userList.get(2).profile;
            //채팅방 상대의 프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + profile3)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((ChatListThreeViewHolder) holder).cv_profile3);

            //채팅방 제목 설정
            ((ChatListThreeViewHolder) holder).tv_title.setText(nickname + ", " + nickname2 + ", " + nickname3);


            //채팅방 마지막 메세지 설정
            if (chatRoomItemArrayList.get(position).message != null) {
                String message = chatRoomItemArrayList.get(position).message;
                ((ChatListThreeViewHolder) holder).tv_content.setText(message);
            }
            //채팅방 마지막 메세지 도착 시간 설정
            if (chatRoomItemArrayList.get(position).time != null) {
                String time = ChatImageDetailActivity.formatDate(chatRoomItemArrayList.get(position).time, fromFromat, toFormat);
                ((ChatListThreeViewHolder) holder).tv_time.setText(time);
            }

            //미확인 메세지 개수 설정
            //미확인 메세제가 없는 경우
            if (chatRoomItemArrayList.get(position).newMessageCount == 0) {
                //미확인 메세지 뷰를 안 보이게 처리
                ((ChatListThreeViewHolder) holder).tv_messageCount.setVisibility(View.GONE);
            }
            //미확인 메세지가 존재하는 경우
            else {
                ((ChatListThreeViewHolder) holder).tv_messageCount.setVisibility(View.VISIBLE);
                int newMessageCount = chatRoomItemArrayList.get(position).newMessageCount;
                ((ChatListThreeViewHolder) holder).tv_messageCount.setText(String.valueOf(newMessageCount));
            }
            viewBinderHelper.bind(((ChatListThreeViewHolder) holder).swipeLayout, String.valueOf(chatRoomItemArrayList.get(position).roomNum));
            //스와이프하면 나오는 나가기 버튼 클릭 리스너
            ((ChatListThreeViewHolder) holder).tv_exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onExitRoomClicked(position);
                }
            });
        }
        //네명 이상과 대화하는 채팅방 리스트
        else if (holder instanceof ChatListFourViewHolder) {

            String nickname = chatRoomItemArrayList.get(position).userList.get(0).nickname;
            String profile = chatRoomItemArrayList.get(position).userList.get(0).profile;
            //채팅방 상대의 프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((ChatListFourViewHolder) holder).cv_profile);

            String nickname2 = chatRoomItemArrayList.get(position).userList.get(1).nickname;
            String profile2 = chatRoomItemArrayList.get(position).userList.get(1).profile;
            //채팅방 상대의 프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + profile2)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((ChatListFourViewHolder) holder).cv_profile2);

            String nickname3 = chatRoomItemArrayList.get(position).userList.get(2).nickname;
            String profile3 = chatRoomItemArrayList.get(position).userList.get(2).profile;
            //채팅방 상대의 프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + profile3)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((ChatListFourViewHolder) holder).cv_profile3);

            String nickname4 = chatRoomItemArrayList.get(position).userList.get(3).nickname;
            String profile4 = chatRoomItemArrayList.get(position).userList.get(3).profile;
            //채팅방 상대의 프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + profile4)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((ChatListFourViewHolder) holder).cv_profile4);

            //채팅방 제목 설정
            ((ChatListFourViewHolder) holder).tv_title.setText(nickname + ", " + nickname2 + ", " + nickname3 + ", " + nickname4);

            //채팅방 참여자 수 설정
            ((ChatListFourViewHolder) holder).tv_count.setText(String.valueOf(chatRoomItemArrayList.get(position).userList.size() + 1));


            //채팅방 마지막 메세지 설정
            if (chatRoomItemArrayList.get(position).message != null) {
                String message = chatRoomItemArrayList.get(position).message;
                ((ChatListFourViewHolder) holder).tv_content.setText(message);
            }

            //채팅방 마지막 메세지 도착 시간 설정
            if (chatRoomItemArrayList.get(position).time != null) {
                String time = ChatImageDetailActivity.formatDate(chatRoomItemArrayList.get(position).time, fromFromat, toFormat);
                ((ChatListFourViewHolder) holder).tv_time.setText(time);

            }

            //미확인 메세지 개수 설정
            //미확인 메세제가 없는 경우
            if (chatRoomItemArrayList.get(position).newMessageCount == 0) {
                //미확인 메세지 뷰를 안 보이게 처리
                ((ChatListFourViewHolder) holder).tv_messageCount.setVisibility(View.GONE);
            }
            //미확인 메세지가 존재하는 경우
            else {
                ((ChatListFourViewHolder) holder).tv_messageCount.setVisibility(View.VISIBLE);
                int newMessageCount = chatRoomItemArrayList.get(position).newMessageCount;
                ((ChatListFourViewHolder) holder).tv_messageCount.setText(String.valueOf(newMessageCount));
            }
            viewBinderHelper.bind(((ChatListFourViewHolder) holder).swipeLayout, String.valueOf(chatRoomItemArrayList.get(position).roomNum));
            //스와이프하면 나오는 나가기 버튼 클릭 리스너
            ((ChatListFourViewHolder) holder).tv_exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onExitRoomClicked(position);
                }
            });
        }
        //채팅방에 자기만 남은 경우
        else {

            //채팅방 제목 설정
            ((ChatListNoneViewHolder) holder).tv_title.setText("참여자 없음");


            if (chatRoomItemArrayList.get(position).message != null) {
                //채팅방 마지막 메세지 설정
                String message = chatRoomItemArrayList.get(position).message;
                ((ChatListNoneViewHolder) holder).tv_content.setText(message);

            }

            if (chatRoomItemArrayList.get(position).time != null) {
                //채팅방 마지막 메세지 도착 시간 설정
                String time = ChatImageDetailActivity.formatDate(chatRoomItemArrayList.get(position).time, fromFromat, toFormat);
                ((ChatListNoneViewHolder) holder).tv_time.setText(time);

            }

            //미확인 메세지 개수 설정
            //미확인 메세제가 없는 경우
            if (chatRoomItemArrayList.get(position).newMessageCount == 0) {
                //미확인 메세지 뷰를 안 보이게 처리
                ((ChatListNoneViewHolder) holder).tv_messageCount.setVisibility(View.GONE);
            }
            //미확인 메세지가 존재하는 경우
            else {
                ((ChatListNoneViewHolder) holder).tv_messageCount.setVisibility(View.VISIBLE);
                int newMessageCount = chatRoomItemArrayList.get(position).newMessageCount;
                ((ChatListNoneViewHolder) holder).tv_messageCount.setText(String.valueOf(newMessageCount));
            }

            viewBinderHelper.bind(((ChatListNoneViewHolder) holder).swipeLayout, String.valueOf(chatRoomItemArrayList.get(position).roomNum));
            //스와이프하면 나오는 나가기 버튼 클릭 리스너
            ((ChatListNoneViewHolder) holder).tv_exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onExitRoomClicked(position);
                }
            });
        }
    }

    //홀더의 아이템 데이터에 변화가 생겼을 때 아이템 홀더 전체를 새로고침하는 게 아니라 변화가 생긴 부분에만 새로고침을 해주는 메소드
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            String payload = (String) payloads.get(0);

            //자신을 제외한 채팅방 참여자 수가 1명(1:1채팅)
            if (holder instanceof ChatListOneViewHolder) {
                //채팅방 마지막 메세지 설정
                if (chatRoomItemArrayList.get(position).message != null) {
                    String message = chatRoomItemArrayList.get(position).message;
                    ((ChatListOneViewHolder) holder).tv_content.setText(message);
                }

                if (chatRoomItemArrayList.get(position).time != null) {
                    //채팅방 마지막 메세지 도착 시간 설정
                    String time = ChatImageDetailActivity.formatDate(chatRoomItemArrayList.get(position).time, fromFromat, toFormat);
                    ((ChatListOneViewHolder) holder).tv_time.setText(time);

                }
                //미확인 메세지 개수를 새롭게 반영해준다.
                ((ChatListOneViewHolder) holder).tv_messageCount.setVisibility(View.VISIBLE);
                int newMessageCount = chatRoomItemArrayList.get(position).newMessageCount;
                ((ChatListOneViewHolder) holder).tv_messageCount.setText(String.valueOf(newMessageCount));
            }
            //자신을 제외한 채팅방 참여자 수가 2명
            else if (holder instanceof ChatListTwoViewHolder) {
                //채팅방 마지막 메세지 설정
                if (chatRoomItemArrayList.get(position).message != null) {
                    String message = chatRoomItemArrayList.get(position).message;
                    ((ChatListTwoViewHolder) holder).tv_content.setText(message);
                }
                if (chatRoomItemArrayList.get(position).time != null) {
                    //채팅방 마지막 메세지 도착 시간 설정
                    String time = ChatImageDetailActivity.formatDate(chatRoomItemArrayList.get(position).time, fromFromat, toFormat);
                    ((ChatListTwoViewHolder) holder).tv_time.setText(time);

                }
                //미확인 메세지 개수를 새롭게 반영해준다.
                ((ChatListTwoViewHolder) holder).tv_messageCount.setVisibility(View.VISIBLE);
                int newMessageCount = chatRoomItemArrayList.get(position).newMessageCount;
                ((ChatListTwoViewHolder) holder).tv_messageCount.setText(String.valueOf(newMessageCount));
            }
            //자신을 제외한 채팅방 참여자 수가 3명
            else if (holder instanceof ChatListThreeViewHolder) {
                //채팅방 마지막 메세지 설정
                if (chatRoomItemArrayList.get(position).message != null) {
                    String message = chatRoomItemArrayList.get(position).message;
                    ((ChatListThreeViewHolder) holder).tv_content.setText(message);
                }
                if (chatRoomItemArrayList.get(position).time != null) {
                    //채팅방 마지막 메세지 도착 시간 설정
                    String time = ChatImageDetailActivity.formatDate(chatRoomItemArrayList.get(position).time, fromFromat, toFormat);
                    ((ChatListThreeViewHolder) holder).tv_time.setText(time);

                }
                //미확인 메세지 개수를 새롭게 반영해준다.
                ((ChatListThreeViewHolder) holder).tv_messageCount.setVisibility(View.VISIBLE);
                int newMessageCount = chatRoomItemArrayList.get(position).newMessageCount;
                ((ChatListThreeViewHolder) holder).tv_messageCount.setText(String.valueOf(newMessageCount));
            }
            //자신을 제외한 채팅방 참여자 수가 4명 이상
            else if (holder instanceof ChatListFourViewHolder) {
                //채팅방 마지막 메세지 설정
                if (chatRoomItemArrayList.get(position).message != null) {
                    String message = chatRoomItemArrayList.get(position).message;
                    ((ChatListFourViewHolder) holder).tv_content.setText(message);
                }
                if (chatRoomItemArrayList.get(position).time != null) {
                    //채팅방 마지막 메세지 도착 시간 설정
                    String time = ChatImageDetailActivity.formatDate(chatRoomItemArrayList.get(position).time, fromFromat, toFormat);
                    ((ChatListFourViewHolder) holder).tv_time.setText(time);

                }
                //미확인 메세지 개수를 새롭게 반영해준다.
                ((ChatListFourViewHolder) holder).tv_messageCount.setVisibility(View.VISIBLE);
                int newMessageCount = chatRoomItemArrayList.get(position).newMessageCount;
                ((ChatListFourViewHolder) holder).tv_messageCount.setText(String.valueOf(newMessageCount));
            }


            if (TextUtils.equals(payload, "userChanged")) {//사용자가 나가서 아이템에 변화가 생기는 경우
                //한명과 대화하는 채팅방 리스트
                if (holder instanceof ChatListOneViewHolder) {
                    String nickname = chatRoomItemArrayList.get(position).userList.get(0).nickname;
                    String profile = chatRoomItemArrayList.get(position).userList.get(0).profile;
                    //채팅방 상대의 프로필 사진 설정
                    Glide.with(context).load("http://13.124.105.47/profileimage/" + profile)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                            .into(((ChatListOneViewHolder) holder).cv_profile);

                    //채팅방 제목 설정
                    ((ChatListOneViewHolder) holder).tv_title.setText(nickname);

                }
                //두명과 대화하는 채팅방 리스트
                else if (holder instanceof ChatListTwoViewHolder) {


                    String nickname = chatRoomItemArrayList.get(position).userList.get(0).nickname;
                    String profile = chatRoomItemArrayList.get(position).userList.get(0).profile;
                    //채팅방 상대의 프로필 사진 설정
                    Glide.with(context).load("http://13.124.105.47/profileimage/" + profile)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                            .into(((ChatListTwoViewHolder) holder).cv_profile);


                    String nickname2 = chatRoomItemArrayList.get(position).userList.get(1).nickname;
                    String profile2 = chatRoomItemArrayList.get(position).userList.get(1).profile;
                    //채팅방 상대의 프로필 사진 설정
                    Glide.with(context).load("http://13.124.105.47/profileimage/" + profile2)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                            .into(((ChatListTwoViewHolder) holder).cv_profile2);

                    //채팅방 제목 설정
                    ((ChatListTwoViewHolder) holder).tv_title.setText(nickname + ", " + nickname2);
                }
                //세명과 대화하는 채팅방 리스트
                else if (holder instanceof ChatListThreeViewHolder) {


                    String nickname = chatRoomItemArrayList.get(position).userList.get(0).nickname;
                    String profile = chatRoomItemArrayList.get(position).userList.get(0).profile;
                    //채팅방 상대의 프로필 사진 설정
                    Glide.with(context).load("http://13.124.105.47/profileimage/" + profile)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                            .into(((ChatListThreeViewHolder) holder).cv_profile);


                    String nickname2 = chatRoomItemArrayList.get(position).userList.get(1).nickname;
                    String profile2 = chatRoomItemArrayList.get(position).userList.get(1).profile;
                    //채팅방 상대의 프로필 사진 설정
                    Glide.with(context).load("http://13.124.105.47/profileimage/" + profile2)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                            .into(((ChatListThreeViewHolder) holder).cv_profile2);

                    String nickname3 = chatRoomItemArrayList.get(position).userList.get(2).nickname;
                    String profile3 = chatRoomItemArrayList.get(position).userList.get(2).profile;
                    //채팅방 상대의 프로필 사진 설정
                    Glide.with(context).load("http://13.124.105.47/profileimage/" + profile3)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                            .into(((ChatListThreeViewHolder) holder).cv_profile3);

                    //채팅방 제목 설정
                    ((ChatListThreeViewHolder) holder).tv_title.setText(nickname + ", " + nickname2 + ", " + nickname3);

                }
                //네명 이상과 대화하는 채팅방 리스트
                else if (holder instanceof ChatListFourViewHolder) {

                    String nickname = chatRoomItemArrayList.get(position).userList.get(0).nickname;
                    String profile = chatRoomItemArrayList.get(position).userList.get(0).profile;
                    //채팅방 상대의 프로필 사진 설정
                    Glide.with(context).load("http://13.124.105.47/profileimage/" + profile)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                            .into(((ChatListFourViewHolder) holder).cv_profile);

                    String nickname2 = chatRoomItemArrayList.get(position).userList.get(1).nickname;
                    String profile2 = chatRoomItemArrayList.get(position).userList.get(1).profile;
                    //채팅방 상대의 프로필 사진 설정
                    Glide.with(context).load("http://13.124.105.47/profileimage/" + profile2)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                            .into(((ChatListFourViewHolder) holder).cv_profile2);

                    String nickname3 = chatRoomItemArrayList.get(position).userList.get(2).nickname;
                    String profile3 = chatRoomItemArrayList.get(position).userList.get(2).profile;
                    //채팅방 상대의 프로필 사진 설정
                    Glide.with(context).load("http://13.124.105.47/profileimage/" + profile3)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                            .into(((ChatListFourViewHolder) holder).cv_profile3);

                    String nickname4 = chatRoomItemArrayList.get(position).userList.get(3).nickname;
                    String profile4 = chatRoomItemArrayList.get(position).userList.get(3).profile;
                    //채팅방 상대의 프로필 사진 설정
                    Glide.with(context).load("http://13.124.105.47/profileimage/" + profile4)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                            .into(((ChatListFourViewHolder) holder).cv_profile4);

                    //채팅방 제목 설정
                    ((ChatListFourViewHolder) holder).tv_title.setText(nickname + ", " + nickname2 + ", " + nickname3 + ", " + nickname4);

                }
                //채팅방에 자기만 남은 경우
                else {

                    //채팅방 제목 설정
                    ((ChatListNoneViewHolder) holder).tv_title.setText("참여자 없음");

                }
            }


        }

    }

    @Override
    public int getItemCount() {
        return (chatRoomItemArrayList == null) ? 0 : chatRoomItemArrayList.size();
    }


    private class ChatListOneViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        CircleImageView cv_profile;
        TextView tv_title, tv_content, tv_time, tv_messageCount, tv_exit;
        SwipeRevealLayout swipeLayout;

        public ChatListOneViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.constraintlayout_container);
            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_title = view.findViewById(R.id.textview_title);
            tv_content = view.findViewById(R.id.textview_content);
            tv_time = view.findViewById(R.id.textview_time);
            tv_messageCount = view.findViewById(R.id.textview_messagecount);
            swipeLayout = view.findViewById(R.id.swipelayout);
            tv_exit = view.findViewById(R.id.textview_exit);

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onContainerClicked(getAdapterPosition());
                }
            });

        }
    }

    private class ChatListTwoViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        CircleImageView cv_profile, cv_profile2;
        TextView tv_title, tv_content, tv_time, tv_messageCount, tv_exit;
        SwipeRevealLayout swipeLayout;

        public ChatListTwoViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.constraintlayout_container);
            cv_profile = view.findViewById(R.id.circleimageview_profile);
            cv_profile2 = view.findViewById(R.id.circleimageview_profile2);
            tv_title = view.findViewById(R.id.textview_title);
            tv_content = view.findViewById(R.id.textview_content);
            tv_time = view.findViewById(R.id.textview_time);
            tv_messageCount = view.findViewById(R.id.textview_messagecount);
            swipeLayout = view.findViewById(R.id.swipelayout);
            tv_exit = view.findViewById(R.id.textview_exit);

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onContainerClicked(getAdapterPosition());
                }
            });
        }
    }

    private class ChatListThreeViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        CircleImageView cv_profile, cv_profile2, cv_profile3;
        TextView tv_title, tv_content, tv_time, tv_messageCount, tv_exit;
        SwipeRevealLayout swipeLayout;


        public ChatListThreeViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.constraintlayout_container);
            cv_profile = view.findViewById(R.id.circleimageview_profile);
            cv_profile2 = view.findViewById(R.id.circleimageview_profile2);
            cv_profile3 = view.findViewById(R.id.circleimageview_profile3);
            tv_title = view.findViewById(R.id.textview_title);
            tv_content = view.findViewById(R.id.textview_content);
            tv_time = view.findViewById(R.id.textview_time);
            tv_messageCount = view.findViewById(R.id.textview_messagecount);
            swipeLayout = view.findViewById(R.id.swipelayout);
            tv_exit = view.findViewById(R.id.textview_exit);

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onContainerClicked(getAdapterPosition());
                }
            });
        }
    }

    private class ChatListFourViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout container;
        CircleImageView cv_profile, cv_profile2, cv_profile3, cv_profile4;
        TextView tv_title, tv_content, tv_count, tv_time, tv_messageCount, tv_exit;
        SwipeRevealLayout swipeLayout;


        public ChatListFourViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.constraintlayout_container);
            cv_profile = view.findViewById(R.id.circleimageview_profile);
            cv_profile2 = view.findViewById(R.id.circleimageview_profile2);
            cv_profile3 = view.findViewById(R.id.circleimageview_profile3);
            cv_profile4 = view.findViewById(R.id.circleimageview_profile4);
            tv_title = view.findViewById(R.id.textview_title);
            tv_content = view.findViewById(R.id.textview_content);
            tv_count = view.findViewById(R.id.textview_count);
            tv_time = view.findViewById(R.id.textview_time);
            tv_messageCount = view.findViewById(R.id.textview_messagecount);
            swipeLayout = view.findViewById(R.id.swipelayout);
            tv_exit = view.findViewById(R.id.textview_exit);

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onContainerClicked(getAdapterPosition());
                }
            });
        }
    }

    private class ChatListNoneViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout container;
        TextView tv_title, tv_content, tv_time, tv_messageCount, tv_exit;
        SwipeRevealLayout swipeLayout;

        public ChatListNoneViewHolder(View view) {
            super(view);

            container = view.findViewById(R.id.constraintlayout_container);
            tv_title = view.findViewById(R.id.textview_title);
            tv_content = view.findViewById(R.id.textview_content);
            tv_time = view.findViewById(R.id.textview_time);
            tv_messageCount = view.findViewById(R.id.textview_messagecount);
            swipeLayout = view.findViewById(R.id.swipelayout);
            tv_exit = view.findViewById(R.id.textview_exit);

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onContainerClicked(getAdapterPosition());
                }
            });
        }
    }
}
