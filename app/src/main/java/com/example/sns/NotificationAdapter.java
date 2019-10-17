package com.example.sns;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final int VIEW_LIKE = 0;
    final int VIEW_COMMENT = 1;
    final int VIEW_CHILDCOMMENT = 2;
    final int VIEW_FOLLOW = 3;
    final int VIEW_PROGRESS = 4;

    private ArrayList<NotificationItem> notificationItemArrayList;
    private Context context;

    public NotificationAdapter(ArrayList<NotificationItem> notificationItemArrayList, Context context) {
        this.notificationItemArrayList = notificationItemArrayList;
        this.context = context;
    }

    interface NotificationRecyclerViewListener {
        void onProfileClicked(int position);

        void onNicknameClicked(int position);

        void onBodyClicked(int position, String category);

        void onImageClicked(int position);

        void onFollowClicked(int position);
    }

    NotificationRecyclerViewListener mListener;

    public void setOnClickListener(NotificationRecyclerViewListener listener) {
        this.mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        //페이지 로딩
        if (notificationItemArrayList.get(position) == null) {
            return VIEW_PROGRESS;
        } else {
            //좋아요 알림
            if (notificationItemArrayList.get(position).category.equals("like")) {
                return VIEW_LIKE;
            }
            //댓글 알림
            else if (notificationItemArrayList.get(position).category.equals("comment")) {
                return VIEW_COMMENT;
            }
            //답글(대댓글) 알림
            else if (notificationItemArrayList.get(position).category.equals("childcomment")) {
                return VIEW_CHILDCOMMENT;
            }
            //팔로우 알림
            else {
                return VIEW_FOLLOW;
            }
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        //좋아요 알림
        if (viewType == VIEW_LIKE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notificationitem_like, viewGroup, false);
            return new NotificationLikeViewHolder(view);
        }
        //댓글 알림
        else if (viewType == VIEW_COMMENT) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notificationitem_comment, viewGroup, false);
            return new NotificationCommentViewHolder(view);
        }
        //대댓글 알림
        else if (viewType == VIEW_CHILDCOMMENT) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notificationitem_childcomment, viewGroup, false);
            return new NotificationChildCommentViewHolder(view);
        }
        //팔로우 알림
        else if (viewType == VIEW_FOLLOW) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notificationitem_follow, viewGroup, false);
            return new NotificationFollowViewHolder(view);
        }
        //페이징 프로그래스
        else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.progressitem, viewGroup, false);
            return new ProgressViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //좋아요 알림인 경우
        if (holder instanceof NotificationLikeViewHolder) {
            //프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + notificationItemArrayList.get(position).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((NotificationLikeViewHolder) holder).cv_profile);

            ((NotificationLikeViewHolder) holder).cv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onProfileClicked(position);
                }
            });

            //알림 본문 설정
            //본문 전체
            String body = notificationItemArrayList.get(position).body;
            //본문에서 닉네임
            String nickname = notificationItemArrayList.get(position).nickname;
            //스패너 스트링을 통해서 닉네임에만 효과와 클릭이벤트를 준다.
            SpannableString spannableString = new SpannableString(body);
            //전체 본문에서 닉네임의 시작 인덱스
            int startIndex = body.indexOf(nickname);
            //전체 본문에서 닉네임의 마지막 인덱스
            int endIndex = startIndex + nickname.length();

            //닉네임 클릭 이벤트를 주기 위해서 ClickableSpan 인터페이스를 사용한다.
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    //닉네임 클릭 리스너
                    mListener.onNicknameClicked(position);
                }

                //닉네임텍스트를 꾸며준다.
                @Override
                public void updateDrawState(TextPaint textPaint) {
                    //닉네임 bold
                    textPaint.setFakeBoldText(true);

                }
            };

            int startIndex1 = endIndex;
            int endIndex1 = body.length();


            //닉네임이 아닌 나머지 부분에 클릭 이벤트를 주기 위해서 ClicableSpan인터페이스를 한번 더 만든다.
            ClickableSpan clickableSpan1 = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    mListener.onBodyClicked(position, notificationItemArrayList.get(position).category);
                }

                @Override
                public void updateDrawState(TextPaint textPaint) {
                    textPaint.setColor(Color.parseColor("#000000"));
                    textPaint.setUnderlineText(false);
                }
            };


            spannableString.setSpan(clickableSpan, startIndex, endIndex, 0);
            spannableString.setSpan(clickableSpan1, startIndex1, endIndex1, 0);
            //설정이 끝난 spannableString으로 셋
            ((NotificationLikeViewHolder) holder).tv_body.setText(spannableString);
            //이 부분이 꼭 있어야 클릭 이벤트가 동작함
            ((NotificationLikeViewHolder) holder).tv_body.setMovementMethod(LinkMovementMethod.getInstance());


            //대표 사진 설정
            Glide.with(context).load("http://13.124.105.47/uploadimage/" + notificationItemArrayList.get(position).image)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((NotificationLikeViewHolder) holder).iv_image);

            //사진 클릭 리스너
            ((NotificationLikeViewHolder) holder).iv_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onImageClicked(position);
                }
            });

            //배경 색 설정
            //확인한 알림인 경우
            if (notificationItemArrayList.get(position).isChecked) {
                ((NotificationLikeViewHolder) holder).container.setBackgroundColor(Color.parseColor("#ffffff"));
            }
            //확인하지 않은 알림인 경우
            else {
                ((NotificationLikeViewHolder) holder).container.setBackgroundColor(Color.parseColor("#E5EAF2"));
            }

            //시간 설정
            Date date = new Date();
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date = simpleDateFormat.parse(notificationItemArrayList.get(position).time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ((NotificationLikeViewHolder) holder).tv_time.setText(beforeTime(date));


        }
        //댓글 알림인 경우
        else if (holder instanceof NotificationCommentViewHolder) {
            //프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + notificationItemArrayList.get(position).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((NotificationCommentViewHolder) holder).cv_profile);

            ((NotificationCommentViewHolder) holder).cv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onProfileClicked(position);
                }
            });

            //알림 본문 설정
            //본문 전체
            String body = notificationItemArrayList.get(position).body;
            //본문에서 닉네임
            String nickname = notificationItemArrayList.get(position).nickname;
            //스패너 스트링을 통해서 닉네임에만 효과와 클릭이벤트를 준다.
            SpannableString spannableString = new SpannableString(body);
            //전체 본문에서 닉네임의 시작 인덱스
            int startIndex = body.indexOf(nickname);
            //전체 본문에서 닉네임의 마지막 인덱스
            int endIndex = startIndex + nickname.length();

            //닉네임 클릭 이벤트를 주기 위해서 ClickableSpan 인터페이스를 사용한다.
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    //닉네임 클릭 리스너
                    mListener.onNicknameClicked(position);
                }

                //닉네임텍스트를 꾸며준다.
                @Override
                public void updateDrawState(TextPaint textPaint) {
                    //닉네임 bold
                    textPaint.setFakeBoldText(true);

                }
            };


            //닉네임이 아닌 나머지 부분에 클릭 이벤트를 주기 위해서 ClicableSpan인터페이스를 한번 더 만든다.
            ClickableSpan clickableSpan1 = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    mListener.onBodyClicked(position, notificationItemArrayList.get(position).category);
                }

                @Override
                public void updateDrawState(TextPaint textPaint) {
                    textPaint.setColor(Color.parseColor("#000000"));
                    textPaint.setUnderlineText(false);
                }
            };

            int startIndex1 = endIndex;
            int endIndex1 = body.length();

            spannableString.setSpan(clickableSpan, startIndex, endIndex, 0);
            spannableString.setSpan(clickableSpan1, startIndex1, endIndex1, 0);
            //설정이 끝난 spannableString으로 셋
            ((NotificationCommentViewHolder) holder).tv_body.setText(spannableString);
            //이 부분이 꼭 있어야 클릭 이벤트가 동작함
            ((NotificationCommentViewHolder) holder).tv_body.setMovementMethod(LinkMovementMethod.getInstance());


            //대표 사진 설정
            Glide.with(context).load("http://13.124.105.47/uploadimage/" + notificationItemArrayList.get(position).image)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((NotificationCommentViewHolder) holder).iv_image);

            //사진 클릭 리스너
            ((NotificationCommentViewHolder) holder).iv_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onImageClicked(position);
                }
            });

            //배경 색 설정
            //확인한 알림인 경우
            if (notificationItemArrayList.get(position).isChecked) {
                ((NotificationCommentViewHolder) holder).container.setBackgroundColor(Color.parseColor("#ffffff"));
            }
            //확인하지 않은 알림인 경우
            else {
                ((NotificationCommentViewHolder) holder).container.setBackgroundColor(Color.parseColor("#E5EAF2"));
            }

            //시간 설정
            Date date = new Date();
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date = simpleDateFormat.parse(notificationItemArrayList.get(position).time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ((NotificationCommentViewHolder) holder).tv_time.setText(beforeTime(date));

        }
        //답글(대댓글) 알림인 경우
        else if (holder instanceof NotificationChildCommentViewHolder) {
            //프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + notificationItemArrayList.get(position).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((NotificationChildCommentViewHolder) holder).cv_profile);

            ((NotificationChildCommentViewHolder) holder).cv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onProfileClicked(position);
                }
            });

            //알림 본문 설정
            //본문 전체
            String body = notificationItemArrayList.get(position).body;
            //본문에서 닉네임
            String nickname = notificationItemArrayList.get(position).nickname;
            //스패너 스트링을 통해서 닉네임에만 효과와 클릭이벤트를 준다.
            SpannableString spannableString = new SpannableString(body);
            //전체 본문에서 닉네임의 시작 인덱스
            int startIndex = body.indexOf(nickname);
            //전체 본문에서 닉네임의 마지막 인덱스
            int endIndex = startIndex + nickname.length();

            //닉네임 클릭 이벤트를 주기 위해서 ClickableSpan 인터페이스를 사용한다.
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    //닉네임 클릭 리스너
                    mListener.onNicknameClicked(position);
                }

                //닉네임텍스트를 꾸며준다.
                @Override
                public void updateDrawState(TextPaint textPaint) {
                    //닉네임 bold
                    textPaint.setFakeBoldText(true);

                }
            };

            //닉네임이 아닌 나머지 부분에 클릭 이벤트를 주기 위해서 ClicableSpan인터페이스를 한번 더 만든다.
            ClickableSpan clickableSpan1 = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    mListener.onBodyClicked(position, notificationItemArrayList.get(position).category);
                }

                @Override
                public void updateDrawState(TextPaint textPaint) {
                    textPaint.setColor(Color.parseColor("#000000"));
                    textPaint.setUnderlineText(false);
                }
            };

            int startIndex1 = endIndex;
            int endIndex1 = body.length();

            spannableString.setSpan(clickableSpan, startIndex, endIndex, 0);
            spannableString.setSpan(clickableSpan1, startIndex1, endIndex1, 0);
            //설정이 끝난 spannableString으로 셋
            ((NotificationChildCommentViewHolder) holder).tv_body.setText(spannableString);
            ((NotificationChildCommentViewHolder) holder).tv_body.setMovementMethod(LinkMovementMethod.getInstance());

            //대표 사진 설정
            Glide.with(context).load("http://13.124.105.47/uploadimage/" + notificationItemArrayList.get(position).image)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((NotificationChildCommentViewHolder) holder).iv_image);

            //사진 클릭 리스너
            ((NotificationChildCommentViewHolder) holder).iv_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onImageClicked(position);
                }
            });


            //배경 색 설정
            //확인한 알림인 경우
            if (notificationItemArrayList.get(position).isChecked) {
                ((NotificationChildCommentViewHolder) holder).container.setBackgroundColor(Color.parseColor("#ffffff"));
            }
            //확인하지 않은 알림인 경우
            else {
                ((NotificationChildCommentViewHolder) holder).container.setBackgroundColor(Color.parseColor("#E5EAF2"));
            }

            //시간 설정
            Date date = new Date();
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date = simpleDateFormat.parse(notificationItemArrayList.get(position).time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ((NotificationChildCommentViewHolder) holder).tv_time.setText(beforeTime(date));


        }
        //팔로우 알림인 경우
        else if (holder instanceof NotificationFollowViewHolder) {
            //프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + notificationItemArrayList.get(position).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((NotificationFollowViewHolder) holder).cv_profile);

            ((NotificationFollowViewHolder) holder).cv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onProfileClicked(position);
                }
            });

            //알림 본문 설정
            //본문 전체
            String body = notificationItemArrayList.get(position).body;
            //본문에서 닉네임
            String nickname = notificationItemArrayList.get(position).nickname;
            //스패너 스트링을 통해서 닉네임에만 효과와 클릭이벤트를 준다.
            SpannableString spannableString = new SpannableString(body);
            //전체 본문에서 닉네임의 시작 인덱스
            int startIndex = body.indexOf(nickname);
            //전체 본문에서 닉네임의 마지막 인덱스
            int endIndex = startIndex + nickname.length();

            //닉네임 클릭 이벤트를 주기 위해서 ClickableSpan 인터페이스를 사용한다.
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    //닉네임 클릭 리스너
                    mListener.onNicknameClicked(position);
                }

                //닉네임텍스트를 꾸며준다.
                @Override
                public void updateDrawState(TextPaint textPaint) {
                    //닉네임 bold
                    textPaint.setFakeBoldText(true);

                }
            };

            //닉네임이 아닌 나머지 부분에 클릭 이벤트를 주기 위해서 ClicableSpan인터페이스를 한번 더 만든다.
            ClickableSpan clickableSpan1 = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    mListener.onBodyClicked(position, notificationItemArrayList.get(position).category);
                }

                @Override
                public void updateDrawState(TextPaint textPaint) {
                    textPaint.setColor(Color.parseColor("#000000"));
                    textPaint.setUnderlineText(false);
                }
            };

            int startIndex1 = endIndex;
            int endIndex1 = body.length();

            spannableString.setSpan(clickableSpan, startIndex, endIndex, 0);
            spannableString.setSpan(clickableSpan1, startIndex1, endIndex1, 0);
            //설정이 끝난 spannableString으로 셋
            ((NotificationFollowViewHolder) holder).tv_body.setText(spannableString);
            ((NotificationFollowViewHolder) holder).tv_body.setMovementMethod(LinkMovementMethod.getInstance());

            //팔로우를 하고 있는 상태인 경우
            if (notificationItemArrayList.get(position).isFollowing) {
                //팔로잉 버튼으로 설정
                ((NotificationFollowViewHolder) holder).btn_follow.setText("팔로잉");
                ((NotificationFollowViewHolder) holder).btn_follow.setBackgroundResource(R.drawable.et_border);
                ((NotificationFollowViewHolder) holder).btn_follow.setTextColor(Color.parseColor("#000000"));

            }
            //팔로우를 하지 않고 있는 상태인 경우
            else {
                //팔로우 버튼으로 설정
                ((NotificationFollowViewHolder) holder).btn_follow.setText("팔로우");
                ((NotificationFollowViewHolder) holder).btn_follow.setBackgroundResource(R.drawable.bluebutton);
                ((NotificationFollowViewHolder) holder).btn_follow.setTextColor(Color.parseColor("#ffffff"));

            }

            //팔로우 버튼 리스너 설정
            ((NotificationFollowViewHolder) holder).btn_follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onFollowClicked(position);
                }
            });


            //배경 색 설정
            //확인한 알림인 경우
            if (notificationItemArrayList.get(position).isChecked) {
                ((NotificationFollowViewHolder) holder).container.setBackgroundColor(Color.parseColor("#ffffff"));
            }
            //확인하지 않은 알림인 경우
            else {
                ((NotificationFollowViewHolder) holder).container.setBackgroundColor(Color.parseColor("#E5EAF2"));
            }

            //시간 설정
            Date date = new Date();
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date = simpleDateFormat.parse(notificationItemArrayList.get(position).time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ((NotificationFollowViewHolder) holder).tv_time.setText(beforeTime(date));

        }
        //페이징 로딩인 경우
        else {

        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        //페이로드가 notify시에 같이 넘어오지 않은 경우
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        }
        //페이로드가 notify시에 같이 넘어온 경우
        else {
            String payload = (String) payloads.get(0);
            //팔로우를 한 경우
            if (TextUtils.equals(payload, "true") && holder instanceof NotificationFollowViewHolder) {
                //팔로잉 버튼으로 설정
                ((NotificationFollowViewHolder) holder).btn_follow.setText("팔로잉");
                ((NotificationFollowViewHolder) holder).btn_follow.setBackgroundResource(R.drawable.et_border);
                ((NotificationFollowViewHolder) holder).btn_follow.setTextColor(Color.parseColor("#000000"));
            }
            //팔로우를 취소하는 경우
            else if (TextUtils.equals(payload, "false") && holder instanceof NotificationFollowViewHolder) {
                //팔로우 버튼으로 설정
                ((NotificationFollowViewHolder) holder).btn_follow.setText("팔로우");
                ((NotificationFollowViewHolder) holder).btn_follow.setBackgroundResource(R.drawable.bluebutton);
                ((NotificationFollowViewHolder) holder).btn_follow.setTextColor(Color.parseColor("#ffffff"));
            }
            //알림을 확인한 경우
            else if (TextUtils.equals(payload, "checked")) {
                if (holder instanceof NotificationLikeViewHolder) {
                    ((NotificationLikeViewHolder) holder).container.setBackgroundColor(Color.parseColor("#ffffff"));
                } else if (holder instanceof NotificationFollowViewHolder) {
                    ((NotificationFollowViewHolder) holder).container.setBackgroundColor(Color.parseColor("#ffffff"));
                } else if (holder instanceof NotificationCommentViewHolder) {
                    ((NotificationCommentViewHolder) holder).container.setBackgroundColor(Color.parseColor("#ffffff"));
                } else if(holder instanceof NotificationChildCommentViewHolder){
                    ((NotificationChildCommentViewHolder) holder).container.setBackgroundColor(Color.parseColor("#ffffff"));
                }

            }
        }
    }

    @Override
    public int getItemCount() {
        return (notificationItemArrayList == null) ? 0 : notificationItemArrayList.size();
    }

    private class NotificationLikeViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        CircleImageView cv_profile;
        TextView tv_body, tv_time;
        ImageView iv_image;


        public NotificationLikeViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_body = view.findViewById(R.id.textview_body);
            tv_time = view.findViewById(R.id.textview_time);
            iv_image = view.findViewById(R.id.imageview_image);
        }
    }

    private class NotificationCommentViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        CircleImageView cv_profile;
        TextView tv_body, tv_time;
        ImageView iv_image;

        public NotificationCommentViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_body = view.findViewById(R.id.textview_body);
            tv_time = view.findViewById(R.id.textview_time);
            iv_image = view.findViewById(R.id.imageview_image);
        }
    }

    private class NotificationChildCommentViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        CircleImageView cv_profile;
        TextView tv_body, tv_time;
        ImageView iv_image;

        public NotificationChildCommentViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_body = view.findViewById(R.id.textview_body);
            tv_time = view.findViewById(R.id.textview_time);
            iv_image = view.findViewById(R.id.imageview_image);
        }
    }

    private class NotificationFollowViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        CircleImageView cv_profile;
        TextView tv_body, tv_time;
        Button btn_follow;

        public NotificationFollowViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            cv_profile = view.findViewById(R.id.circleimageview_profile);
            tv_body = view.findViewById(R.id.textview_body);
            tv_time = view.findViewById(R.id.textview_time);
            btn_follow = view.findViewById(R.id.button_follow);
        }
    }

    private class ProgressViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public ProgressViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progressbar);
        }
    }

    //업로드된 게시물이 몇 분 전에 만들어진 게시물인지를 리턴해주는 메소드
    public String beforeTime(Date date) {

        //캘린더 클레스는 추상 클레스라서 객체를 생성할 수 없다.
        //대신 getInstance()메소드를 통해서 객체 생성이 가능하다.
        Calendar c = Calendar.getInstance();

        //캘린더 객체에서 getTimeInMillis()메소드를 사용해 현재 시간을 가져옴
        long now = c.getTimeInMillis();
        Log.d("현재 시간", String.valueOf(now));
        //date에서 시간만 가져온다. 여기서 중요한 점은 now변수는 계속해서 현재시간을 반환하기 때문에 변하는 수이고
        //date는 내가 선언한 순간의 시간을 가져오기 때문에 고정된 시간이다.
        long dateM = date.getTime();
        Log.d("입력된 날짜", String.valueOf(dateM));

        //이 변수는 위에서 봤듯이 현재의 시간에서 내가 이 메소드를 호출한 시간을 뺀 시간을 의미한다.
        long gap = now - dateM;

        String ret = "";

//        초       분   시
//        1000    60  60
        gap = (long) (gap / 1000);
        long hour = gap / 3600;
        gap = gap % 3600;
        long min = gap / 60;
        long sec = gap % 60;

        if (hour > 24) {
            ret = hour / 24 + "일 전";
        } else if (hour > 0) {
            ret = hour + "시간 전";
        } else if (min > 0) {
            ret = min + "분 전";
        } else if (sec > 0) {
            ret = sec + "초 전";
        } else {
            ret = new SimpleDateFormat("HH:mm").format(date);
        }
        return ret;

    }

}
