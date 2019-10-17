package com.example.sns;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final int VIEW_MINE = 0;
    public final int VIEW_OTHERS = 1;
    public final int VIEW_MINE_IMAGE_ONE = 2;
    public final int VIEW_MINE_IMAGE_TWO = 3;
    public final int VIEW_MINE_IMAGE_THREE = 4;
    public final int VIEW_MINE_IMAGE_FOUR = 5;
    public final int VIEW_MINE_IMAGE_FIVE = 6;
    public final int VIEW_MINE_IMAGE_SIX = 7;
    public final int VIEW_OTHERS_IMAGE_ONE = 8;
    public final int VIEW_OTHERS_IMAGE_TWO = 9;
    public final int VIEW_OTHERS_IMAGE_THREE = 10;
    public final int VIEW_OTHERS_IMAGE_FOUR = 11;
    public final int VIEW_OTHERS_IMAGE_FIVE = 12;
    public final int VIEW_OTHERS_IMAGE_SIX = 13;
    public final int VIEW_TIMEDIVIDER = 14;
    public final int VIEW_EXIT = 15;
    public final int VIEW_ADD = 16;

    //시간 포맷
    private String fromFromat = "yyyy-MM-dd HH:mm:ss";
    private String toFormat = "a h:mm";


    public ArrayList<ChatContentItem> chatContentItemArrayList;
    Context context;

    public ChatContentAdapter(ArrayList<ChatContentItem> chatContentItemArrayList, Context context) {
        this.chatContentItemArrayList = chatContentItemArrayList;
        this.context = context;
    }

    public interface ChatContentRecyclerViewListener {
        void onImageclicked(int position, int imagePosition);
        void onProfileClicked(int position);
    }

    public ChatContentRecyclerViewListener mListner;

    public void setOnClickListener(ChatContentRecyclerViewListener listener) {
        this.mListner = listener;
    }

    @Override
    public int getItemViewType(int position) {
        //시간 경계 아이템인 경우
        if (chatContentItemArrayList.get(position).isTimeDivider) {
            return VIEW_TIMEDIVIDER;
        }
        //채팅방 퇴장 아이템인 경우
        else if (chatContentItemArrayList.get(position).isExit) {
            return VIEW_EXIT;
        }
        //사용자 초대 아이템인 경우
        else if (chatContentItemArrayList.get(position).isAddedParticipantMessage) {
            return VIEW_ADD;
        }
        //시간 경계 아이템이나 채팅방 퇴장 아이템, 사용자 초대 아이템이 아닌 경우
        else {
            if (chatContentItemArrayList.get(position).isMyContent) {
                if (chatContentItemArrayList.get(position).imageList.size() == 0) {
                    return VIEW_MINE;
                } else if (chatContentItemArrayList.get(position).imageList.size() == 1) {
                    return VIEW_MINE_IMAGE_ONE;
                } else if (chatContentItemArrayList.get(position).imageList.size() == 2) {
                    return VIEW_MINE_IMAGE_TWO;
                } else if (chatContentItemArrayList.get(position).imageList.size() == 3) {
                    return VIEW_MINE_IMAGE_THREE;
                } else if (chatContentItemArrayList.get(position).imageList.size() == 4) {
                    return VIEW_MINE_IMAGE_FOUR;
                } else if (chatContentItemArrayList.get(position).imageList.size() == 5) {
                    return VIEW_MINE_IMAGE_FIVE;
                } else {
                    return VIEW_MINE_IMAGE_SIX;
                }

            } else {
                if (chatContentItemArrayList.get(position).imageList.size() == 0) {
                    return VIEW_OTHERS;
                } else if (chatContentItemArrayList.get(position).imageList.size() == 1) {
                    return VIEW_OTHERS_IMAGE_ONE;
                } else if (chatContentItemArrayList.get(position).imageList.size() == 2) {
                    return VIEW_OTHERS_IMAGE_TWO;
                } else if (chatContentItemArrayList.get(position).imageList.size() == 3) {
                    return VIEW_OTHERS_IMAGE_THREE;
                } else if (chatContentItemArrayList.get(position).imageList.size() == 4) {
                    return VIEW_OTHERS_IMAGE_FOUR;
                } else if (chatContentItemArrayList.get(position).imageList.size() == 5) {
                    return VIEW_OTHERS_IMAGE_FIVE;
                } else {
                    return VIEW_OTHERS_IMAGE_SIX;
                }

            }
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_MINE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mychatcontentitem, viewGroup, false);
            return new MyChatContentViewHolder(view);
        } else if (viewType == VIEW_OTHERS) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.otherschatcontentitem, viewGroup, false);
            return new OthersChatContentViewHolder(view);
        } else if (viewType == VIEW_MINE_IMAGE_ONE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mychatimge_one, viewGroup, false);
            return new MyChatImageOneViewHolder(view);
        } else if (viewType == VIEW_MINE_IMAGE_TWO) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mychatimge_two, viewGroup, false);
            return new MyChatImageTwoViewHolder(view);
        } else if (viewType == VIEW_MINE_IMAGE_THREE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mychatimge_three, viewGroup, false);
            return new MyChatImageThreeViewHolder(view);
        } else if (viewType == VIEW_MINE_IMAGE_FOUR) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mychatimge_four, viewGroup, false);
            return new MyChatImageFourViewHolder(view);
        } else if (viewType == VIEW_MINE_IMAGE_FIVE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mychatimge_five, viewGroup, false);
            return new MyChatImageFiveViewHolder(view);
        } else if (viewType == VIEW_MINE_IMAGE_SIX) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mychatimge_six, viewGroup, false);
            return new MyChatImageSixViewHolder(view);
        } else if (viewType == VIEW_OTHERS_IMAGE_ONE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.otherschatimage_one, viewGroup, false);
            return new OthersChatImageOneViewHolder(view);
        } else if (viewType == VIEW_OTHERS_IMAGE_TWO) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.otherschatimage_two, viewGroup, false);
            return new OthersChatImageTwoViewHolder(view);
        } else if (viewType == VIEW_OTHERS_IMAGE_THREE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.otherschatimage_three, viewGroup, false);
            return new OthersChatImageThreeViewHolder(view);
        } else if (viewType == VIEW_OTHERS_IMAGE_FOUR) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.otherschatimage_four, viewGroup, false);
            return new OthersChatImageFourViewHolder(view);
        } else if (viewType == VIEW_OTHERS_IMAGE_FIVE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.otherschatimage_five, viewGroup, false);
            return new OthersChatImageFiveViewHolder(view);
        } else if (viewType == VIEW_OTHERS_IMAGE_SIX) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.otherschatimage_six, viewGroup, false);
            return new OthersChatImageSixViewHolder(view);
        } else if (viewType == VIEW_TIMEDIVIDER) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chatcontenttimeitem, viewGroup, false);
            return new TimeDividerViewHolder(view);
        } else if (viewType == VIEW_EXIT) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chatcontentexit, viewGroup, false);
            return new ExitViewHolder(view);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chatcontentadd, viewGroup, false);
            return new AddViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //내가 보낸 채팅
        if (holder instanceof MyChatContentViewHolder) {
            //채팅 설정
            ((MyChatContentViewHolder) holder).tv_content.setText(chatContentItemArrayList.get(position).message);
            //채팅이 완전히 보내진 경우
            if (chatContentItemArrayList.get(position).isSent) {
                ((MyChatContentViewHolder) holder).iv_sending.setVisibility(View.INVISIBLE);
                String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, toFormat);
                ((MyChatContentViewHolder) holder).tv_time.setText(time);
                ((MyChatContentViewHolder) holder).tv_uncheckCount.setVisibility(View.VISIBLE);
                //미확인자가 없는 경우
                if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                    ((MyChatContentViewHolder) holder).tv_uncheckCount.setText("");
                }
                //미확인자가 존재하는 경우
                else {
                    //미확인자를 배열에 넣고
                    String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                    //그 수만큼 미확인자의 수로 지정해준다.
                    int uncheckedCount = unCheckepParticipantList.length;
                    ((MyChatContentViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                }
            }
            //채팅을 보내는 중인 경우
            else {
                ((MyChatContentViewHolder) holder).iv_sending.setVisibility(View.VISIBLE);
                ((MyChatContentViewHolder) holder).tv_time.setVisibility(View.INVISIBLE);
                ((MyChatContentViewHolder) holder).tv_uncheckCount.setVisibility(View.INVISIBLE);
            }


        }
        //내가 보낸 이미지(1장)
        else if (holder instanceof MyChatImageOneViewHolder) {

            //채팅이 완전히 보내진 경우
            if (chatContentItemArrayList.get(position).isSent) {
                ((MyChatImageOneViewHolder) holder).iv_sending.setVisibility(View.INVISIBLE);
                String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, toFormat);
                ((MyChatImageOneViewHolder) holder).tv_time.setText(time);
                ((MyChatImageOneViewHolder) holder).tv_uncheckCount.setVisibility(View.VISIBLE);
                //미확인자가 없는 경우
                if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                    ((MyChatImageOneViewHolder) holder).tv_uncheckCount.setText("");
                }
                //미확인자가 존재하는 경우
                else {
                    //미확인자를 배열에 넣고
                    String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                    //그 수만큼 미확인자의 수로 지정해준다.
                    int uncheckedCount = unCheckepParticipantList.length;
                    ((MyChatImageOneViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                }
            }
            //채팅을 보내는 중인 경우
            else {
                ((MyChatImageOneViewHolder) holder).iv_sending.setVisibility(View.VISIBLE);
                ((MyChatImageOneViewHolder) holder).tv_time.setVisibility(View.INVISIBLE);
                ((MyChatImageOneViewHolder) holder).tv_uncheckCount.setVisibility(View.INVISIBLE);
            }

            if (chatContentItemArrayList.get(position).isImageFromServer) {
                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(0))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageOneViewHolder) holder).iv_image1);
            } else {
                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(0))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageOneViewHolder) holder).iv_image1);
            }
//
//            //이미지 클릭 리스너
//            ((MyChatImageOneViewHolder) holder).iv_image1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 0);
//                }
//            });
        }
        //내가 보낸 이미지(2장)
        else if (holder instanceof MyChatImageTwoViewHolder) {


            //채팅이 완전히 보내진 경우
            if (chatContentItemArrayList.get(position).isSent) {
                ((MyChatImageTwoViewHolder) holder).iv_sending.setVisibility(View.INVISIBLE);
                String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, toFormat);
                ((MyChatImageTwoViewHolder) holder).tv_time.setText(time);
                ((MyChatImageTwoViewHolder) holder).tv_uncheckCount.setVisibility(View.VISIBLE);
                //미확인자가 없는 경우
                if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                    ((MyChatImageTwoViewHolder) holder).tv_uncheckCount.setText("");
                }
                //미확인자가 존재하는 경우
                else {
                    //미확인자를 배열에 넣고
                    String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                    //그 수만큼 미확인자의 수로 지정해준다.
                    int uncheckedCount = unCheckepParticipantList.length;
                    ((MyChatImageTwoViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                }

            }
            //채팅을 보내는 중인 경우
            else {
                ((MyChatImageTwoViewHolder) holder).iv_sending.setVisibility(View.VISIBLE);
                ((MyChatImageTwoViewHolder) holder).tv_time.setVisibility(View.INVISIBLE);
                ((MyChatImageTwoViewHolder) holder).tv_uncheckCount.setVisibility(View.INVISIBLE);
            }

            if (chatContentItemArrayList.get(position).isImageFromServer) {
                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(0))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageTwoViewHolder) holder).iv_image1);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(1))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageTwoViewHolder) holder).iv_image2);
            } else {
                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(0))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageTwoViewHolder) holder).iv_image1);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(1))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageTwoViewHolder) holder).iv_image2);
            }
//
//            //이미지 클릭 리스너
//            ((MyChatImageTwoViewHolder) holder).iv_image1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 0);
//                }
//            });
//
//            ((MyChatImageTwoViewHolder) holder).iv_image2.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 1);
//                }
//            });
        }
        //내가 보낸 이미지(3장)
        else if (holder instanceof MyChatImageThreeViewHolder) {
            //채팅이 완전히 보내진 경우
            if (chatContentItemArrayList.get(position).isSent) {
                ((MyChatImageThreeViewHolder) holder).iv_sending.setVisibility(View.INVISIBLE);
                String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, toFormat);
                ((MyChatImageThreeViewHolder) holder).tv_time.setText(time);
                ((MyChatImageThreeViewHolder) holder).tv_uncheckCount.setVisibility(View.VISIBLE);
                //미확인자가 없는 경우
                if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                    ((MyChatImageThreeViewHolder) holder).tv_uncheckCount.setText("");
                }
                //미확인자가 존재하는 경우
                else {
                    //미확인자를 배열에 넣고
                    String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                    //그 수만큼 미확인자의 수로 지정해준다.
                    int uncheckedCount = unCheckepParticipantList.length;
                    ((MyChatImageThreeViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                }
            }
            //채팅을 보내는 중인 경우
            else {
                ((MyChatImageThreeViewHolder) holder).iv_sending.setVisibility(View.VISIBLE);
                ((MyChatImageThreeViewHolder) holder).tv_time.setVisibility(View.INVISIBLE);
                ((MyChatImageThreeViewHolder) holder).tv_uncheckCount.setVisibility(View.INVISIBLE);
            }

            if (chatContentItemArrayList.get(position).isImageFromServer) {
                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(0))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageThreeViewHolder) holder).iv_image1);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(1))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageThreeViewHolder) holder).iv_image2);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(2))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageThreeViewHolder) holder).iv_image3);
            } else {
                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(0))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageThreeViewHolder) holder).iv_image1);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(1))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageThreeViewHolder) holder).iv_image2);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(2))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageThreeViewHolder) holder).iv_image3);
            }
//            //이미지 클릭 리스너
//            ((MyChatImageThreeViewHolder) holder).iv_image1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 0);
//                }
//            });
//
//            ((MyChatImageThreeViewHolder) holder).iv_image2.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 1);
//                }
//            });
//
//            ((MyChatImageThreeViewHolder) holder).iv_image3.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 2);
//                }
//            });

        }
        //내가 보낸 이미지(4장)
        else if (holder instanceof MyChatImageFourViewHolder) {

            //채팅이 완전히 보내진 경우
            if (chatContentItemArrayList.get(position).isSent) {
                ((MyChatImageFourViewHolder) holder).iv_sending.setVisibility(View.INVISIBLE);
                String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, toFormat);
                ((MyChatImageFourViewHolder) holder).tv_time.setText(time);
                ((MyChatImageFourViewHolder) holder).tv_uncheckCount.setVisibility(View.VISIBLE);
                //미확인자가 없는 경우
                if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                    ((MyChatImageFourViewHolder) holder).tv_uncheckCount.setText("");
                }
                //미확인자가 존재하는 경우
                else {
                    //미확인자를 배열에 넣고
                    String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                    //그 수만큼 미확인자의 수로 지정해준다.
                    int uncheckedCount = unCheckepParticipantList.length;
                    ((MyChatImageFourViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                }
            }
            //채팅을 보내는 중인 경우
            else {
                ((MyChatImageFourViewHolder) holder).iv_sending.setVisibility(View.VISIBLE);
                ((MyChatImageFourViewHolder) holder).tv_time.setVisibility(View.INVISIBLE);
                ((MyChatImageFourViewHolder) holder).tv_uncheckCount.setVisibility(View.INVISIBLE);
            }


            if (chatContentItemArrayList.get(position).isImageFromServer) {
                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(0))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFourViewHolder) holder).iv_image1);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(1))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFourViewHolder) holder).iv_image2);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(2))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFourViewHolder) holder).iv_image3);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(3))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFourViewHolder) holder).iv_image4);
            } else {
                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(0))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFourViewHolder) holder).iv_image1);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(1))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFourViewHolder) holder).iv_image2);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(2))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFourViewHolder) holder).iv_image3);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(3))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFourViewHolder) holder).iv_image4);
            }

//            //이미지 클릭 리스너
//            ((MyChatImageFourViewHolder) holder).iv_image1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 0);
//                }
//            });
//
//            ((MyChatImageFourViewHolder) holder).iv_image2.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 1);
//                }
//            });
//
//            ((MyChatImageFourViewHolder) holder).iv_image3.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 2);
//                }
//            });
//
//            ((MyChatImageFourViewHolder) holder).iv_image4.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 3);
//                }
//            });
        }
        //내가 보낸 이미지(5장)
        else if (holder instanceof MyChatImageFiveViewHolder) {

            //채팅이 완전히 보내진 경우
            if (chatContentItemArrayList.get(position).isSent) {
                ((MyChatImageFiveViewHolder) holder).iv_sending.setVisibility(View.INVISIBLE);
                String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, toFormat);
                ((MyChatImageFiveViewHolder) holder).tv_time.setText(time);
                ((MyChatImageFiveViewHolder) holder).tv_uncheckCount.setVisibility(View.VISIBLE);
                //미확인자가 없는 경우
                if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                    ((MyChatImageFiveViewHolder) holder).tv_uncheckCount.setText("");
                }
                //미확인자가 존재하는 경우
                else {
                    //미확인자를 배열에 넣고
                    String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                    //그 수만큼 미확인자의 수로 지정해준다.
                    int uncheckedCount = unCheckepParticipantList.length;
                    ((MyChatImageFiveViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                }
            }
            //채팅을 보내는 중인 경우
            else {
                ((MyChatImageFiveViewHolder) holder).iv_sending.setVisibility(View.VISIBLE);
                ((MyChatImageFiveViewHolder) holder).tv_time.setVisibility(View.INVISIBLE);
                ((MyChatImageFiveViewHolder) holder).tv_uncheckCount.setVisibility(View.INVISIBLE);
            }


            if (chatContentItemArrayList.get(position).isImageFromServer) {
                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(0))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFiveViewHolder) holder).iv_image1);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(1))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFiveViewHolder) holder).iv_image2);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(2))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFiveViewHolder) holder).iv_image3);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(3))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFiveViewHolder) holder).iv_image4);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(4))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFiveViewHolder) holder).iv_image5);
            } else {
                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(0))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFiveViewHolder) holder).iv_image1);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(1))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFiveViewHolder) holder).iv_image2);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(2))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFiveViewHolder) holder).iv_image3);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(3))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFiveViewHolder) holder).iv_image4);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(4))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageFiveViewHolder) holder).iv_image5);
            }

            //이미지 클릭 리스너
            ((MyChatImageFiveViewHolder) holder).iv_image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(position, 0);
                }
            });

            ((MyChatImageFiveViewHolder) holder).iv_image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(position, 1);
                }
            });

            ((MyChatImageFiveViewHolder) holder).iv_image3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(position, 2);
                }
            });

            ((MyChatImageFiveViewHolder) holder).iv_image4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(position, 3);
                }
            });

            ((MyChatImageFiveViewHolder) holder).iv_image5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(position, 4);
                }
            });
        }
        //내가 보낸 이미지(6장)
        else if (holder instanceof MyChatImageSixViewHolder) {

            //채팅이 완전히 보내진 경우
            if (chatContentItemArrayList.get(position).isSent) {
                ((MyChatImageSixViewHolder) holder).iv_sending.setVisibility(View.INVISIBLE);
                String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, toFormat);
                ((MyChatImageSixViewHolder) holder).tv_time.setText(time);
                ((MyChatImageSixViewHolder) holder).tv_uncheckCount.setVisibility(View.VISIBLE);
                //미확인자가 없는 경우
                if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                    ((MyChatImageSixViewHolder) holder).tv_uncheckCount.setText("");
                }
                //미확인자가 존재하는 경우
                else {
                    //미확인자를 배열에 넣고
                    String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                    //그 수만큼 미확인자의 수로 지정해준다.
                    int uncheckedCount = unCheckepParticipantList.length;
                    ((MyChatImageSixViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                }
            }
            //채팅을 보내는 중인 경우
            else {
                ((MyChatImageSixViewHolder) holder).iv_sending.setVisibility(View.VISIBLE);
                ((MyChatImageSixViewHolder) holder).tv_time.setVisibility(View.INVISIBLE);
                ((MyChatImageSixViewHolder) holder).tv_uncheckCount.setVisibility(View.INVISIBLE);
            }

            if (chatContentItemArrayList.get(position).isImageFromServer) {
                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(0))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageSixViewHolder) holder).iv_image1);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(1))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageSixViewHolder) holder).iv_image2);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(2))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageSixViewHolder) holder).iv_image3);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(3))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageSixViewHolder) holder).iv_image4);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(4))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageSixViewHolder) holder).iv_image5);

                Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(5))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageSixViewHolder) holder).iv_image6);
            } else {
                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(0))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageSixViewHolder) holder).iv_image1);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(1))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageSixViewHolder) holder).iv_image2);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(2))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageSixViewHolder) holder).iv_image3);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(3))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageSixViewHolder) holder).iv_image4);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(4))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageSixViewHolder) holder).iv_image5);

                Glide.with(context).load(chatContentItemArrayList.get(position).imageList.get(5))
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(((MyChatImageSixViewHolder) holder).iv_image6);
            }

//            //이미지 클릭 리스너
//            ((MyChatImageSixViewHolder) holder).iv_image1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 0);
//                }
//            });
//
//            ((MyChatImageSixViewHolder) holder).iv_image2.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 1);
//                }
//            });
//
//            ((MyChatImageSixViewHolder) holder).iv_image3.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 2);
//                }
//            });
//
//            ((MyChatImageSixViewHolder) holder).iv_image4.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 3);
//                }
//            });
//
//            ((MyChatImageSixViewHolder) holder).iv_image5.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 4);
//                }
//            });
//
//            ((MyChatImageSixViewHolder) holder).iv_image6.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 5);
//                }
//            });
        }
        //다른 사람들이 보낸 채팅
        else if (holder instanceof OthersChatContentViewHolder) {
            //프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + chatContentItemArrayList.get(position).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatContentViewHolder) holder).cv_profile);

            //닉네임 설정
            ((OthersChatContentViewHolder) holder).tv_nickname.setText(chatContentItemArrayList.get(position).nickname);
            //채팅 설정
            ((OthersChatContentViewHolder) holder).tv_content.setText(chatContentItemArrayList.get(position).message);
            //메세지 전송 시간 설정
            String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, toFormat);
            ((OthersChatContentViewHolder) holder).tv_time.setText(time);

            //미확인자가 없는 경우
            if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                ((OthersChatContentViewHolder) holder).tv_uncheckCount.setText("");
            }
            //미확인자가 존재하는 경우
            else {
                //미확인자를 배열에 넣고
                String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                //그 수만큼 미확인자의 수로 지정해준다.
                int uncheckedCount = unCheckepParticipantList.length;
                ((OthersChatContentViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
            }
        }
        //남이 보낸 이미지(1장)
        else if (holder instanceof OthersChatImageOneViewHolder) {

            //프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + chatContentItemArrayList.get(position).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageOneViewHolder) holder).cv_profile);

            //닉네임 설정
            ((OthersChatImageOneViewHolder) holder).tv_nickname.setText(chatContentItemArrayList.get(position).nickname);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(0))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageOneViewHolder) holder).iv_image1);
//
//            //이미지 클릭 리스너
//            ((OthersChatImageOneViewHolder) holder).iv_image1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 0);
//                }
//            });

            //메세지 전송 시간 설정
            String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, toFormat);
            ((OthersChatImageOneViewHolder) holder).tv_time.setText(time);

            //미확인자가 없는 경우
            if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                ((OthersChatImageOneViewHolder) holder).tv_uncheckCount.setText("");
            }
            //미확인자가 존재하는 경우
            else {
                //미확인자를 배열에 넣고
                String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                //그 수만큼 미확인자의 수로 지정해준다.
                int uncheckedCount = unCheckepParticipantList.length;
                ((OthersChatImageOneViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
            }
        }
        //남이 보낸 이미지(2장)
        else if (holder instanceof OthersChatImageTwoViewHolder) {

            //프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + chatContentItemArrayList.get(position).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageTwoViewHolder) holder).cv_profile);

            //닉네임 설정
            ((OthersChatImageTwoViewHolder) holder).tv_nickname.setText(chatContentItemArrayList.get(position).nickname);


            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(0))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageTwoViewHolder) holder).iv_image1);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(1))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageTwoViewHolder) holder).iv_image2);
//
//            //이미지 클릭 리스너
//            ((OthersChatImageTwoViewHolder) holder).iv_image1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 0);
//                }
//            });
//
//            ((OthersChatImageTwoViewHolder) holder).iv_image2.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 1);
//                }
//            });
            //메세지 전송 시간 설정
            String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, toFormat);
            ((OthersChatImageTwoViewHolder) holder).tv_time.setText(time);

            //미확인자가 없는 경우
            if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                ((OthersChatImageTwoViewHolder) holder).tv_uncheckCount.setText("");
            }
            //미확인자가 존재하는 경우
            else {
                //미확인자를 배열에 넣고
                String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                //그 수만큼 미확인자의 수로 지정해준다.
                int uncheckedCount = unCheckepParticipantList.length;
                ((OthersChatImageTwoViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
            }
        }
        //남이 보낸 이미지(3장)
        else if (holder instanceof OthersChatImageThreeViewHolder) {

            //프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + chatContentItemArrayList.get(position).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageThreeViewHolder) holder).cv_profile);

            //닉네임 설정
            ((OthersChatImageThreeViewHolder) holder).tv_nickname.setText(chatContentItemArrayList.get(position).nickname);


            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(0))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageThreeViewHolder) holder).iv_image1);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(1))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageThreeViewHolder) holder).iv_image2);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(2))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageThreeViewHolder) holder).iv_image3);

//            //이미지 클릭 리스너
//            ((OthersChatImageThreeViewHolder) holder).iv_image1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 0);
//                }
//            });
//
//            ((OthersChatImageThreeViewHolder) holder).iv_image2.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 1);
//                }
//            });
//
//            ((OthersChatImageThreeViewHolder) holder).iv_image3.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 2);
//                }
//            });

            //메세지 전송 시간 설정
            String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, toFormat);
            ((OthersChatImageThreeViewHolder) holder).tv_time.setText(time);


            //미확인자가 없는 경우
            if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                ((OthersChatImageThreeViewHolder) holder).tv_uncheckCount.setText("");
            }
            //미확인자가 존재하는 경우
            else {
                //미확인자를 배열에 넣고
                String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                //그 수만큼 미확인자의 수로 지정해준다.
                int uncheckedCount = unCheckepParticipantList.length;
                ((OthersChatImageThreeViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
            }
        }
        //남이 보낸 이미지(4장)
        else if (holder instanceof OthersChatImageFourViewHolder) {

            //프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + chatContentItemArrayList.get(position).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageFourViewHolder) holder).cv_profile);

            //닉네임 설정
            ((OthersChatImageFourViewHolder) holder).tv_nickname.setText(chatContentItemArrayList.get(position).nickname);


            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(0))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageFourViewHolder) holder).iv_image1);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(1))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageFourViewHolder) holder).iv_image2);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(2))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageFourViewHolder) holder).iv_image3);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(3))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageFourViewHolder) holder).iv_image4);

//            //이미지 클릭 리스너
//            ((OthersChatImageFourViewHolder) holder).iv_image1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 0);
//                }
//            });
//
//            ((OthersChatImageFourViewHolder) holder).iv_image2.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 1);
//                }
//            });
//
//            ((OthersChatImageFourViewHolder) holder).iv_image3.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 2);
//                }
//            });
//
//            ((OthersChatImageFourViewHolder) holder).iv_image4.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 3);
//                }
//            });

            //메세지 전송 시간 설정
            String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, toFormat);
            ((OthersChatImageFourViewHolder) holder).tv_time.setText(time);
            //미확인자가 없는 경우
            if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                ((OthersChatImageFourViewHolder) holder).tv_uncheckCount.setText("");
            }
            //미확인자가 존재하는 경우
            else {
                //미확인자를 배열에 넣고
                String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                //그 수만큼 미확인자의 수로 지정해준다.
                int uncheckedCount = unCheckepParticipantList.length;
                ((OthersChatImageFourViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
            }
        }
        //남이 보낸 이미지(5장)
        else if (holder instanceof OthersChatImageFiveViewHolder) {

            //프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + chatContentItemArrayList.get(position).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageFiveViewHolder) holder).cv_profile);

            //닉네임 설정
            ((OthersChatImageFiveViewHolder) holder).tv_nickname.setText(chatContentItemArrayList.get(position).nickname);


            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(0))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageFiveViewHolder) holder).iv_image1);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(1))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageFiveViewHolder) holder).iv_image2);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(2))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageFiveViewHolder) holder).iv_image3);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(3))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageFiveViewHolder) holder).iv_image4);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(4))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageFiveViewHolder) holder).iv_image5);
//
//            //이미지 클릭 리스너
//            ((OthersChatImageFiveViewHolder) holder).iv_image1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 0);
//                }
//            });
//
//            ((OthersChatImageFiveViewHolder) holder).iv_image2.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 1);
//                }
//            });
//
//            ((OthersChatImageFiveViewHolder) holder).iv_image3.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 2);
//                }
//            });
//
//            ((OthersChatImageFiveViewHolder) holder).iv_image4.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 3);
//                }
//            });
//
//            ((OthersChatImageFiveViewHolder) holder).iv_image5.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 4);
//                }
//            });

            //메세지 전송 시간 설정
            String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, toFormat);
            ((OthersChatImageFiveViewHolder) holder).tv_time.setText(time);

            //미확인자가 없는 경우
            if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                ((OthersChatImageFiveViewHolder) holder).tv_uncheckCount.setText("");
            }
            //미확인자가 존재하는 경우
            else {
                //미확인자를 배열에 넣고
                String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                //그 수만큼 미확인자의 수로 지정해준다.
                int uncheckedCount = unCheckepParticipantList.length;
                ((OthersChatImageFiveViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
            }

        }
        //남이 보낸 이미지(6장)
        else if (holder instanceof OthersChatImageSixViewHolder) {

            //프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + chatContentItemArrayList.get(position).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageSixViewHolder) holder).cv_profile);

            //닉네임 설정
            ((OthersChatImageSixViewHolder) holder).tv_nickname.setText(chatContentItemArrayList.get(position).nickname);


            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(0))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageSixViewHolder) holder).iv_image1);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(1))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageSixViewHolder) holder).iv_image2);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(2))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageSixViewHolder) holder).iv_image3);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(3))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageSixViewHolder) holder).iv_image4);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(4))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageSixViewHolder) holder).iv_image5);

            Glide.with(context).load("http://13.124.105.47/chatimage/" + chatContentItemArrayList.get(position).imageList.get(5))
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((OthersChatImageSixViewHolder) holder).iv_image6);
//
//            //이미지 클릭 리스너
//            ((OthersChatImageSixViewHolder) holder).iv_image1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 0);
//                }
//            });
//
//            ((OthersChatImageSixViewHolder) holder).iv_image2.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 1);
//                }
//            });
//
//            ((OthersChatImageSixViewHolder) holder).iv_image3.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 2);
//                }
//            });
//
//            ((OthersChatImageSixViewHolder) holder).iv_image4.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 3);
//                }
//            });
//
//            ((OthersChatImageSixViewHolder) holder).iv_image5.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 4);
//                }
//            });
//
//            ((OthersChatImageSixViewHolder) holder).iv_image6.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListner.onImageclicked(position, 5);
//                }
//            });
            //메세지 전송 시간 설정
            String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, toFormat);
            ((OthersChatImageSixViewHolder) holder).tv_time.setText(time);

            //미확인자가 없는 경우
            if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                ((OthersChatImageSixViewHolder) holder).tv_uncheckCount.setText("");
            }
            //미확인자가 존재하는 경우
            else {
                //미확인자를 배열에 넣고
                String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                //그 수만큼 미확인자의 수로 지정해준다.
                int uncheckedCount = unCheckepParticipantList.length;
                ((OthersChatImageSixViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
            }
        }
        //시간 경계 아이템
        else if (holder instanceof TimeDividerViewHolder) {

            String time = ChatImageDetailActivity.formatDate(chatContentItemArrayList.get(position).time, fromFromat, "yyyy년 M월 d일");
            ((TimeDividerViewHolder) holder).tv_timeDivider.setText(time);
        }
        //채팅방 퇴장 아이템
        else if (holder instanceof ExitViewHolder) {
            //퇴장 메세지 셋
            String message = chatContentItemArrayList.get(position).message;
            ((ExitViewHolder) holder).tv_exit.setText(message);
        }
        //사용자 초대 아이템
        else if(holder instanceof AddViewHolder){
            //초대 메세지 셋
            String message = chatContentItemArrayList.get(position).message;
            ((AddViewHolder) holder).tv_add.setText(message);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            String payload = (String) payloads.get(0);
            if (TextUtils.equals(payload, "checked")) {
                //메세지를 확인해서 읽지 않은 참가자의 수를 줄여준다.
                if (holder instanceof MyChatContentViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((MyChatContentViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((MyChatContentViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof MyChatImageOneViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((MyChatImageOneViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((MyChatImageOneViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof MyChatImageTwoViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((MyChatImageTwoViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((MyChatImageTwoViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof MyChatImageThreeViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((MyChatImageThreeViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((MyChatImageThreeViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof MyChatImageFourViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((MyChatImageFourViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((MyChatImageFourViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof MyChatImageFiveViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((MyChatImageFiveViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((MyChatImageFiveViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof MyChatImageSixViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((MyChatImageSixViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((MyChatImageSixViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof OthersChatContentViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((OthersChatContentViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((OthersChatContentViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof OthersChatImageOneViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((OthersChatImageOneViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((OthersChatImageOneViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof OthersChatImageTwoViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((OthersChatImageTwoViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((OthersChatImageTwoViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof OthersChatImageThreeViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((OthersChatImageThreeViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((OthersChatImageThreeViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof OthersChatImageFourViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((OthersChatImageFourViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((OthersChatImageFourViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof OthersChatImageFiveViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((OthersChatImageFiveViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((OthersChatImageFiveViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof OthersChatImageSixViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((OthersChatImageSixViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((OthersChatImageSixViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                }
            } else if (TextUtils.equals(payload, "exit")) {
                //메세지를 확인해서 읽지 않은 참가자의 수를 줄여준다.
                if (holder instanceof MyChatContentViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((MyChatContentViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((MyChatContentViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof MyChatImageOneViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((MyChatImageOneViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((MyChatImageOneViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof MyChatImageTwoViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((MyChatImageTwoViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((MyChatImageTwoViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof MyChatImageThreeViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((MyChatImageThreeViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((MyChatImageThreeViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof MyChatImageFourViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((MyChatImageFourViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((MyChatImageFourViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof MyChatImageFiveViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((MyChatImageFiveViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((MyChatImageFiveViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof MyChatImageSixViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((MyChatImageSixViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((MyChatImageSixViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof OthersChatContentViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((OthersChatContentViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((OthersChatContentViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof OthersChatImageOneViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((OthersChatImageOneViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((OthersChatImageOneViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof OthersChatImageTwoViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((OthersChatImageTwoViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((OthersChatImageTwoViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof OthersChatImageThreeViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((OthersChatImageThreeViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((OthersChatImageThreeViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof OthersChatImageFourViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((OthersChatImageFourViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((OthersChatImageFourViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof OthersChatImageFiveViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((OthersChatImageFiveViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((OthersChatImageFiveViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }
                } else if (holder instanceof OthersChatImageSixViewHolder) {
                    //미확인자가 없는 경우
                    if (chatContentItemArrayList.get(position).unCheckedParticipant.equals("null")) {
                        ((OthersChatImageSixViewHolder) holder).tv_uncheckCount.setText("");
                    }
                    //미확인자가 존재하는 경우
                    else {
                        //미확인자를 배열에 넣고
                        String[] unCheckepParticipantList = chatContentItemArrayList.get(position).unCheckedParticipant.split("/");
                        //그 수만큼 미확인자의 수로 지정해준다.
                        int uncheckedCount = unCheckepParticipantList.length;
                        ((OthersChatImageSixViewHolder) holder).tv_uncheckCount.setText(String.valueOf(uncheckedCount));
                    }

                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return (chatContentItemArrayList == null) ? 0 : chatContentItemArrayList.size();
    }

    private class MyChatContentViewHolder extends RecyclerView.ViewHolder {

        TextView tv_content, tv_time, tv_uncheckCount;
        ImageView iv_sending;

        public MyChatContentViewHolder(View view) {
            super(view);
            tv_time = view.findViewById(R.id.textview_time);
            tv_content = view.findViewById(R.id.textview_chatcontent);
            iv_sending = view.findViewById(R.id.imageview_sending);
            tv_uncheckCount = view.findViewById(R.id.textview_uncheckcount);
        }
    }

    private class OthersChatContentViewHolder extends RecyclerView.ViewHolder {

        CircleImageView cv_profile;
        TextView tv_nickname, tv_content, tv_time, tv_uncheckCount;

        public OthersChatContentViewHolder(View view) {
            super(view);

            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_nickname = view.findViewById(R.id.textview_nickname);
            tv_content = view.findViewById(R.id.textview_chatcontent);
            tv_time = view.findViewById(R.id.textview_time);
            tv_uncheckCount = view.findViewById(R.id.textview_uncheckcount);

            cv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onProfileClicked(getAdapterPosition());
                }
            });
        }
    }

    private class MyChatImageOneViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_image1;
        ImageView iv_sending;
        TextView tv_time, tv_uncheckCount;

        public MyChatImageOneViewHolder(View view) {
            super(view);

            iv_image1 = view.findViewById(R.id.imageview_image);
            iv_sending = view.findViewById(R.id.imageview_sending);
            tv_time = view.findViewById(R.id.textview_time);
            tv_uncheckCount = view.findViewById(R.id.textview_uncheckcount);

            iv_image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 0);
                }
            });
        }
    }

    private class MyChatImageTwoViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_image1, iv_image2;
        ImageView iv_sending;
        TextView tv_time, tv_uncheckCount;

        public MyChatImageTwoViewHolder(View view) {
            super(view);
            iv_image1 = view.findViewById(R.id.imageview_image);
            iv_image2 = view.findViewById(R.id.imageview_image2);
            iv_sending = view.findViewById(R.id.imageview_sending);
            tv_time = view.findViewById(R.id.textview_time);
            tv_uncheckCount = view.findViewById(R.id.textview_uncheckcount);

            iv_image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 0);
                }
            });

            iv_image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 1);
                }
            });
        }
    }

    private class MyChatImageThreeViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_image1, iv_image2, iv_image3;
        ImageView iv_sending;
        TextView tv_time, tv_uncheckCount;

        public MyChatImageThreeViewHolder(View view) {
            super(view);
            iv_image1 = view.findViewById(R.id.imageview_image);
            iv_image2 = view.findViewById(R.id.imageview_image2);
            iv_image3 = view.findViewById(R.id.imageview_image3);
            iv_sending = view.findViewById(R.id.imageview_sending);
            tv_time = view.findViewById(R.id.textview_time);
            tv_uncheckCount = view.findViewById(R.id.textview_uncheckcount);

            iv_image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 0);
                }
            });

            iv_image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 1);
                }
            });

            iv_image3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 2);
                }
            });

        }
    }

    private class MyChatImageFourViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_image1, iv_image2, iv_image3, iv_image4;
        ImageView iv_sending;
        TextView tv_time, tv_uncheckCount;

        public MyChatImageFourViewHolder(View view) {
            super(view);
            iv_image1 = view.findViewById(R.id.imageview_image);
            iv_image2 = view.findViewById(R.id.imageview_image2);
            iv_image3 = view.findViewById(R.id.imageview_image3);
            iv_image4 = view.findViewById(R.id.imageview_image4);
            iv_sending = view.findViewById(R.id.imageview_sending);
            tv_time = view.findViewById(R.id.textview_time);
            tv_uncheckCount = view.findViewById(R.id.textview_uncheckcount);

            iv_image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 0);
                }
            });

            iv_image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 1);
                }
            });

            iv_image3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 2);
                }
            });

            iv_image4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 3);
                }
            });


        }
    }

    private class MyChatImageFiveViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_image1, iv_image2, iv_image3, iv_image4, iv_image5;
        ImageView iv_sending;
        TextView tv_time, tv_uncheckCount;

        public MyChatImageFiveViewHolder(View view) {
            super(view);
            iv_image1 = view.findViewById(R.id.imageview_image);
            iv_image2 = view.findViewById(R.id.imageview_image2);
            iv_image3 = view.findViewById(R.id.imageview_image3);
            iv_image4 = view.findViewById(R.id.imageview_image4);
            iv_image5 = view.findViewById(R.id.imageview_image5);
            iv_sending = view.findViewById(R.id.imageview_sending);
            tv_time = view.findViewById(R.id.textview_time);
            tv_uncheckCount = view.findViewById(R.id.textview_uncheckcount);

            iv_image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 0);
                }
            });

            iv_image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 1);
                }
            });

            iv_image3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 2);
                }
            });

            iv_image4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 3);
                }
            });

            iv_image5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 4);
                }
            });

        }
    }

    private class MyChatImageSixViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_image1, iv_image2, iv_image3, iv_image4, iv_image5, iv_image6;
        ImageView iv_sending;
        TextView tv_time, tv_uncheckCount;

        public MyChatImageSixViewHolder(View view) {
            super(view);
            iv_image1 = view.findViewById(R.id.imageview_image);
            iv_image2 = view.findViewById(R.id.imageview_image2);
            iv_image3 = view.findViewById(R.id.imageview_image3);
            iv_image4 = view.findViewById(R.id.imageview_image4);
            iv_image5 = view.findViewById(R.id.imageview_image5);
            iv_image6 = view.findViewById(R.id.imageview_image6);
            iv_sending = view.findViewById(R.id.imageview_sending);
            tv_time = view.findViewById(R.id.textview_time);
            tv_uncheckCount = view.findViewById(R.id.textview_uncheckcount);

            iv_image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 0);
                }
            });

            iv_image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 1);
                }
            });

            iv_image3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 2);
                }
            });

            iv_image4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 3);
                }
            });

            iv_image5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 4);
                }
            });

            iv_image6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 5);
                }
            });

        }
    }

    private class OthersChatImageOneViewHolder extends RecyclerView.ViewHolder {

        CircleImageView cv_profile;
        TextView tv_nickname, tv_time, tv_uncheckCount;
        ImageView iv_image1;

        public OthersChatImageOneViewHolder(View view) {
            super(view);

            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_nickname = view.findViewById(R.id.textview_nickname);
            iv_image1 = view.findViewById(R.id.imageview_image);
            tv_time = view.findViewById(R.id.textview_time);
            tv_uncheckCount = view.findViewById(R.id.textview_uncheckcount);

            cv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onProfileClicked(getAdapterPosition());
                }
            });

            iv_image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 0);
                }
            });

        }
    }

    private class OthersChatImageTwoViewHolder extends RecyclerView.ViewHolder {

        CircleImageView cv_profile;
        TextView tv_nickname, tv_time, tv_uncheckCount;
        ImageView iv_image1, iv_image2;

        public OthersChatImageTwoViewHolder(View view) {
            super(view);

            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_nickname = view.findViewById(R.id.textview_nickname);
            iv_image1 = view.findViewById(R.id.imageview_image);
            iv_image2 = view.findViewById(R.id.imageview_image2);
            tv_time = view.findViewById(R.id.textview_time);
            tv_uncheckCount = view.findViewById(R.id.textview_uncheckcount);

            cv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onProfileClicked(getAdapterPosition());
                }
            });


            iv_image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 0);
                }
            });

            iv_image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 1);
                }
            });

        }
    }

    private class OthersChatImageThreeViewHolder extends RecyclerView.ViewHolder {

        CircleImageView cv_profile;
        TextView tv_nickname, tv_time, tv_uncheckCount;
        ImageView iv_image1, iv_image2, iv_image3;

        public OthersChatImageThreeViewHolder(View view) {
            super(view);

            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_nickname = view.findViewById(R.id.textview_nickname);
            iv_image1 = view.findViewById(R.id.imageview_image);
            iv_image2 = view.findViewById(R.id.imageview_image2);
            iv_image3 = view.findViewById(R.id.imageview_image3);
            tv_time = view.findViewById(R.id.textview_time);
            tv_uncheckCount = view.findViewById(R.id.textview_uncheckcount);

            cv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onProfileClicked(getAdapterPosition());
                }
            });

            iv_image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 0);
                }
            });

            iv_image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 1);
                }
            });

            iv_image3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 2);
                }
            });


        }
    }

    private class OthersChatImageFourViewHolder extends RecyclerView.ViewHolder {

        CircleImageView cv_profile;
        TextView tv_nickname, tv_time, tv_uncheckCount;
        ImageView iv_image1, iv_image2, iv_image3, iv_image4;

        public OthersChatImageFourViewHolder(View view) {
            super(view);

            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_nickname = view.findViewById(R.id.textview_nickname);
            iv_image1 = view.findViewById(R.id.imageview_image);
            iv_image2 = view.findViewById(R.id.imageview_image2);
            iv_image3 = view.findViewById(R.id.imageview_image3);
            iv_image4 = view.findViewById(R.id.imageview_image4);
            tv_time = view.findViewById(R.id.textview_time);
            tv_uncheckCount = view.findViewById(R.id.textview_uncheckcount);

            cv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onProfileClicked(getAdapterPosition());
                }
            });

            iv_image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 0);
                }
            });

            iv_image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 1);
                }
            });

            iv_image3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 2);
                }
            });

            iv_image4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 3);
                }
            });


        }
    }

    private class OthersChatImageFiveViewHolder extends RecyclerView.ViewHolder {

        CircleImageView cv_profile;
        TextView tv_nickname, tv_time, tv_uncheckCount;
        ImageView iv_image1, iv_image2, iv_image3, iv_image4, iv_image5;

        public OthersChatImageFiveViewHolder(View view) {
            super(view);

            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_nickname = view.findViewById(R.id.textview_nickname);
            iv_image1 = view.findViewById(R.id.imageview_image);
            iv_image2 = view.findViewById(R.id.imageview_image2);
            iv_image3 = view.findViewById(R.id.imageview_image3);
            iv_image4 = view.findViewById(R.id.imageview_image4);
            iv_image5 = view.findViewById(R.id.imageview_image5);
            tv_time = view.findViewById(R.id.textview_time);
            tv_uncheckCount = view.findViewById(R.id.textview_uncheckcount);

            cv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onProfileClicked(getAdapterPosition());
                }
            });

            iv_image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 0);
                }
            });

            iv_image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 1);
                }
            });

            iv_image3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 2);
                }
            });

            iv_image4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 3);
                }
            });

            iv_image5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 4);
                }
            });

        }
    }

    private class OthersChatImageSixViewHolder extends RecyclerView.ViewHolder {

        CircleImageView cv_profile;
        TextView tv_nickname, tv_time, tv_uncheckCount;
        ImageView iv_image1, iv_image2, iv_image3, iv_image4, iv_image5, iv_image6;

        public OthersChatImageSixViewHolder(View view) {
            super(view);

            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_nickname = view.findViewById(R.id.textview_nickname);
            iv_image1 = view.findViewById(R.id.imageview_image);
            iv_image2 = view.findViewById(R.id.imageview_image2);
            iv_image3 = view.findViewById(R.id.imageview_image3);
            iv_image4 = view.findViewById(R.id.imageview_image4);
            iv_image5 = view.findViewById(R.id.imageview_image5);
            iv_image6 = view.findViewById(R.id.imageview_image6);
            tv_time = view.findViewById(R.id.textview_time);
            tv_uncheckCount = view.findViewById(R.id.textview_uncheckcount);

            cv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onProfileClicked(getAdapterPosition());
                }
            });

            iv_image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 0);
                }
            });

            iv_image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 1);
                }
            });

            iv_image3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 2);
                }
            });

            iv_image4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 3);
                }
            });

            iv_image5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 4);
                }
            });

            iv_image6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onImageclicked(getAdapterPosition(), 5);
                }
            });
        }
    }

    private class TimeDividerViewHolder extends RecyclerView.ViewHolder {

        TextView tv_timeDivider;

        public TimeDividerViewHolder(@NonNull View view) {
            super(view);

            tv_timeDivider = view.findViewById(R.id.textview_time);
        }
    }

    private class ExitViewHolder extends RecyclerView.ViewHolder {
        TextView tv_exit;

        public ExitViewHolder(@NonNull View view) {
            super(view);
            tv_exit = view.findViewById(R.id.textview_exit);
        }
    }

    private class AddViewHolder extends RecyclerView.ViewHolder {
        TextView tv_add;

        public AddViewHolder(View view) {
            super(view);
            tv_add = view.findViewById(R.id.textview_add);
        }
    }
}
