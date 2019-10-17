package com.example.sns;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //뷰타입 설정
    private final int VIEW_IMAGEPOST = 0;
    private final int VIEW_VIDEOPOST = 1;
    private final int VIEW_PROGRESS = 2;

    private ArrayList<PostItem> postItemArrayList;


    Context context;

    //해시태그 라이브러리 객체
    HashTagHelper hashTagHelper;

    //뷰페이저의 상태를 저장하기 위한 해시맵
    HashMap<Integer, Integer> viewPagerState = new HashMap<>();

    //뷰페이저 프래그먼트 매니저
    FragmentManager fragmentManager;

    //뷰페이저 어댑터
    public ImageViewPager imageViewPager;


    //인터페이스 객체로 액티비티에서 리사이클러뷰의 홀더의 아이템을 클릭해서 동작이 가능하게끔 하기 위해서 정의
    PostRecyclerViewListener mListener;

    public boolean isMuted = true;//동영상 게시물의 오디오 활성화 여부

    SimpleExoPlayer player;//exoplayer

    //이 안에 함수를 정의하면 된다.
    interface PostRecyclerViewListener {
        //프로필 사진을 클릭했을 때 호출
        void onProfileClicked(int position);

        //닉네임을 클릭했을 때 호출
        void onNicknameClicked(int position);

        //더 보기 버튼을 클릭했을 때 호출
        void onMoreClicked(int position);

        //좋아요 버튼 클릭했을 때 호출
        void onLikeClicked(int position);

        //좋아요 개수 클릭했을 때 호출
        void onLikeCountClicked(int position);

        //댓글 버튼 클릭했을 때 호출
        void onCommentClick(int position);

        //댓글 개수 클릭했을 때 호출
        void onCommentCountClicked(int position);

        //해시태그를 클릭했을 때 호출
        void onHashTagClicked(int position, String hashTag);

        //장소 클릭했을 때 호출
        void onPlaceClicked(int position);

    }


    //액티비티에서 리사이클러뷰 클릭 리스너 설정해주기
    public void setOnClickListener(PostRecyclerViewListener listener) {
        mListener = listener;
    }

    public PostAdapter(Context context, ArrayList<PostItem> postItemArrayList, FragmentManager fragmentManager) {
        this.context = context;
        this.postItemArrayList = postItemArrayList;
        this.fragmentManager = fragmentManager;
        this.player = ExoPlayerFactory.newSimpleInstance(context);
    }


    @Override
    public int getItemViewType(int position) {
        return (postItemArrayList.get(position) == null) ? VIEW_PROGRESS : (postItemArrayList.get(position).type.equals("image")) ? VIEW_IMAGEPOST : VIEW_VIDEOPOST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //뷰타입이 게시물일 경우
        if (viewType == VIEW_IMAGEPOST) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.postitem, viewGroup, false);
            return new postViewHolder(view);
        } else if (viewType == VIEW_VIDEOPOST) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.videopostitem, viewGroup, false);
            return new videoPostViewHolder(view);
        }
        //뷰타입이 프로그레스바인 경우
        else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.progressitem, viewGroup, false);
            return new progressViewHolder(view);
        }

    }


    //뷰 홀더의 각 뷰에 데이터 매칭
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int i) {
        Log.d("onBindViewHolder", "호출");
        Log.d("onBindViewHolder 인덱스", String.valueOf(i));


        //뷰 홀더가 게시물 아이템의 뷰 홀더인 경우
        if (holder instanceof postViewHolder) {
            //프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + postItemArrayList.get(i).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().error(R.drawable.profile))
                    .into(((postViewHolder) holder).cv_profile);

            //프로필 사진 클릭 리스너
            ((postViewHolder) holder).cv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onProfileClicked(i);
                }
            });

            //닉네임 설정
            ((postViewHolder) holder).tv_nickname.setText(postItemArrayList.get(i).nickname);

            //닉네임 클릭 리스너
            ((postViewHolder) holder).tv_nickname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onNicknameClicked(i);
                }
            });

            //장소 설정
            //주소가 존재하면
            if (postItemArrayList.get(i).address != null) {
                ((postViewHolder) holder).tv_place.setText(postItemArrayList.get(i).address);
            }
            //주소가 존재하지 않으면
            else {
                ((postViewHolder) holder).tv_place.setVisibility(View.GONE);
            }

            //장소 클릭 리스너
            ((postViewHolder) holder).tv_place.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onPlaceClicked(i);
                }
            });

            //더 보기 버튼 설정
            ((postViewHolder) holder).ib_more.setImageResource(R.drawable.dot);
            //더 보기 버튼 클릭 리스너
            ((postViewHolder) holder).ib_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onMoreClicked(i);
                }
            });


            //뷰페이저 설정
            imageViewPager = new ImageViewPager(fragmentManager, i, holder);

            ((postViewHolder) holder).viewPager.setAdapter(imageViewPager);
            ((postViewHolder) holder).viewPager.setId(i + 1);


            //뷰페이저의 상태를 저장하는 해쉬 값 중 리사이클러뷰의 인덱스 번호로 된 키값이 존재하면 그 키의 값에 해당하는 퓨페이저의 인덱스로 바로 이동
            if (viewPagerState.containsKey(i)) {
                ((postViewHolder) holder).viewPager.setCurrentItem(viewPagerState.get(i));
            }


            //탭 레이아웃 설정
            ((postViewHolder) holder).tabLayout.setupWithViewPager(((postViewHolder) holder).viewPager, true);

            //게시글 설정
            ((postViewHolder) holder).tv_article.post(new Runnable() {
                @Override
                public void run() {
                    ((postViewHolder) holder).tv_article.setText(postItemArrayList.get(i).article);
                    Log.d("게시글 줄 수: ", String.valueOf(((postViewHolder) holder).tv_article.getLineCount()));
                    //게시글의 줄 수가 2줄을 초과하면
                    if (((postViewHolder) holder).tv_article.getLineCount() > 2) {
                        //더 보기 버튼이 보이게 한다.
                        ((postViewHolder) holder).tv_readmore.setVisibility(View.VISIBLE);
                        ((postViewHolder) holder).tv_article.setMaxLines(2);
                    }
                    //두줄을 초과하지 않으면
                    else {
                        ((postViewHolder) holder).tv_readmore.setVisibility(View.GONE);
                    }

                }
            });


//            Log.d("게시글 줄 수: ", String.valueOf(((postViewHolder)holder).tv_article.getLineCount()));
            hashTagHelper = HashTagHelper.Creator.create(Color.parseColor("#02B2ED"), new HashTagHelper.OnHashTagClickListener() {
                //해시태그를 클릭했을 때 콜백(차후 클릭한 해시태그를 가진 게시물을 모아서 보여주는 액티비티로 이동시킬 예정)
                @Override
                public void onHashTagClicked(String hashTag) {
                    mListener.onHashTagClicked(i, hashTag);
                }
            });
            //게시글을 라이브러리로 제어해서 해시태그가 보이게끔
            hashTagHelper.handle(((postViewHolder) holder).tv_article);

            //게시글의 줄 수를 구해서 2줄 이하면 더 보기 버튼을 없애버린다.
            //다음과 같이 게시글 view에 접근할 때 post를 통해서 UI스레드에 메세지를 전달해서 다른 스레드에서 이런 로직에 접근하는 이유는
            //getLineCount메소드 자체가 UI에 접근하는 메소드이기 때문이다. 그래서 저 메소드는 그냥 실행하면 항상 0을 리턴하게 된다.
            //그래서 다른 스레드에서 접근해야만 올바른 값을 리턴할 수 있게 된다.


            //더 보기 버튼 클릭 리스너 설정
            ((postViewHolder) holder).tv_readmore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //게시글의 줄 수를 최대로 늘리고
                    ((postViewHolder) holder).tv_article.setMaxLines(Integer.MAX_VALUE);
                    //더 보기 버튼을 없앤다.
                    ((postViewHolder) holder).tv_readmore.setVisibility(View.GONE);
                }
            });


            //댓글 버튼 설정
            ((postViewHolder) holder).ib_comment.setImageResource(R.drawable.comment);
            ((postViewHolder) holder).ib_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onCommentClick(i);
                }
            });

            //댓글 개수 클릭 리스너
            ((postViewHolder) holder).tv_commentCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onCommentCountClicked(i);
                }
            });


            //업로드 시간 설정
            Date date = new Date();
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date = dateFormat.parse(postItemArrayList.get(i).time);
                Log.d("시간", String.valueOf(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ((postViewHolder) holder).tv_uploadTime.setText(beforeTime(date));

            //좋아요 상태 설정
            //좋아요 상태가 true인 경우
            if (postItemArrayList.get(i).isLike == true) {
                //좋아요를 한 상태의 이미지로 셋
                ((postViewHolder) holder).ib_like.setImageResource(R.drawable.like_clicked);
            }
            //좋아요 상태가 false인 경우
            else {
                //좋아요를 하지 않은 상태의 이미지로 셋
                ((postViewHolder) holder).ib_like.setImageResource(R.drawable.like_unclicked);
            }

            //좋아요 클릭 이벤트 리스너
            ((postViewHolder) holder).ib_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onLikeClicked(i);
                }
            });


            //좋아요 개수 설정
            ((postViewHolder) holder).tv_likeCount.setText("좋아요 " + postItemArrayList.get(i).likeCount + "개");
            //좋아요 개수 클릭 리스너
            ((postViewHolder) holder).tv_likeCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onLikeCountClicked(i);
                }
            });

            //댓글 개수 설정
            ((postViewHolder) holder).tv_commentCount.setText("댓글 " + postItemArrayList.get(i).commentCount + "개");

        }
        //뷰 홀더가 동영상 게시물 뷰 홀더인 경우
        else if (holder instanceof videoPostViewHolder) {
            //프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/" + postItemArrayList.get(i).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().error(R.drawable.profile))
                    .into(((videoPostViewHolder) holder).cv_profile);

            //프로필 사진 클릭 리스너
            ((videoPostViewHolder) holder).cv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onProfileClicked(i);
                }
            });

            //닉네임 설정
            ((videoPostViewHolder) holder).tv_nickname.setText(postItemArrayList.get(i).nickname);

            //닉네임 클릭 리스너
            ((videoPostViewHolder) holder).tv_nickname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onNicknameClicked(i);
                }
            });

            //장소 설정
            //주소가 존재하면
            if (postItemArrayList.get(i).address != null) {
                ((videoPostViewHolder) holder).tv_place.setText(postItemArrayList.get(i).address);
            }
            //주소가 존재하지 않으면
            else {
                ((videoPostViewHolder) holder).tv_place.setVisibility(View.GONE);
            }

            //장소 클릭 리스너
            ((videoPostViewHolder) holder).tv_place.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onPlaceClicked(i);
                }
            });

            //더 보기 버튼 설정
            ((videoPostViewHolder) holder).ib_more.setImageResource(R.drawable.dot);
            //더 보기 버튼 클릭 리스너
            ((videoPostViewHolder) holder).ib_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onMoreClicked(i);
                }
            });

            //동영상 플레이어의 썸네일 설정
            Glide.with(context).load("http://13.124.105.47/uploadvideo/" + postItemArrayList.get(i).video)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions()
                            .centerInside()
                            .error(R.drawable.video)
                            .frame(0))//동영상의 첫 프레임을 썸네일 이미지로 설정
                    .into(((videoPostViewHolder) holder).iv_thumbnail);

            ((videoPostViewHolder) holder).ib_mute.setSelected(true);

            //게시글 설정
            ((videoPostViewHolder) holder).tv_article.post(new Runnable() {
                @Override
                public void run() {
                    ((videoPostViewHolder) holder).tv_article.setText(postItemArrayList.get(i).article);
                    Log.d("게시글 줄 수: ", String.valueOf(((videoPostViewHolder) holder).tv_article.getLineCount()));
                    //게시글의 줄 수가 2줄을 초과하면
                    if (((videoPostViewHolder) holder).tv_article.getLineCount() > 2) {
                        //더 보기 버튼이 보이게 한다.
                        ((videoPostViewHolder) holder).tv_readmore.setVisibility(View.VISIBLE);
                        ((videoPostViewHolder) holder).tv_article.setMaxLines(2);
                    }
                    //두줄을 초과하지 않으면
                    else {
                        ((videoPostViewHolder) holder).tv_readmore.setVisibility(View.GONE);
                    }

                }
            });


//            Log.d("게시글 줄 수: ", String.valueOf(((videoPostViewHolder)holder).tv_article.getLineCount()));
            hashTagHelper = HashTagHelper.Creator.create(Color.parseColor("#02B2ED"), new HashTagHelper.OnHashTagClickListener() {
                //해시태그를 클릭했을 때 콜백(차후 클릭한 해시태그를 가진 게시물을 모아서 보여주는 액티비티로 이동시킬 예정)
                @Override
                public void onHashTagClicked(String hashTag) {
                    mListener.onHashTagClicked(i, hashTag);
                }
            });
            //게시글을 라이브러리로 제어해서 해시태그가 보이게끔
            hashTagHelper.handle(((videoPostViewHolder) holder).tv_article);

            //게시글의 줄 수를 구해서 2줄 이하면 더 보기 버튼을 없애버린다.
            //다음과 같이 게시글 view에 접근할 때 post를 통해서 UI스레드에 메세지를 전달해서 다른 스레드에서 이런 로직에 접근하는 이유는
            //getLineCount메소드 자체가 UI에 접근하는 메소드이기 때문이다. 그래서 저 메소드는 그냥 실행하면 항상 0을 리턴하게 된다.
            //그래서 다른 스레드에서 접근해야만 올바른 값을 리턴할 수 있게 된다.


            //더 보기 버튼 클릭 리스너 설정
            ((videoPostViewHolder) holder).tv_readmore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //게시글의 줄 수를 최대로 늘리고
                    ((videoPostViewHolder) holder).tv_article.setMaxLines(Integer.MAX_VALUE);
                    //더 보기 버튼을 없앤다.
                    ((videoPostViewHolder) holder).tv_readmore.setVisibility(View.GONE);
                }
            });


            //댓글 버튼 설정
            ((videoPostViewHolder) holder).ib_comment.setImageResource(R.drawable.comment);
            ((videoPostViewHolder) holder).ib_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onCommentClick(i);
                }
            });

            //댓글 개수 클릭 리스너
            ((videoPostViewHolder) holder).tv_commentCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onCommentCountClicked(i);
                }
            });


            //업로드 시간 설정
            Date date = new Date();
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date = dateFormat.parse(postItemArrayList.get(i).time);
                Log.d("시간", String.valueOf(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ((videoPostViewHolder) holder).tv_uploadTime.setText(beforeTime(date));

            //좋아요 상태 설정
            //좋아요 상태가 true인 경우
            if (postItemArrayList.get(i).isLike == true) {
                //좋아요를 한 상태의 이미지로 셋
                ((videoPostViewHolder) holder).ib_like.setImageResource(R.drawable.like_clicked);
            }
            //좋아요 상태가 false인 경우
            else {
                //좋아요를 하지 않은 상태의 이미지로 셋
                ((videoPostViewHolder) holder).ib_like.setImageResource(R.drawable.like_unclicked);
            }

            //좋아요 클릭 이벤트 리스너
            ((videoPostViewHolder) holder).ib_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onLikeClicked(i);
                }
            });


            //좋아요 개수 설정
            ((videoPostViewHolder) holder).tv_likeCount.setText("좋아요 " + postItemArrayList.get(i).likeCount + "개");
            //좋아요 개수 클릭 리스너
            ((videoPostViewHolder) holder).tv_likeCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onLikeCountClicked(i);
                }
            });

            //댓글 개수 설정
            ((videoPostViewHolder) holder).tv_commentCount.setText("댓글 " + postItemArrayList.get(i).commentCount + "개");

        } else {

        }
    }

    //액티비티에서 데이터를 변경하고 notify를 해줄 때 두번째 인자인 payload에 값을 넣어서 주는 경우 이 onBindViewHolder가 호출되어서 원하는 뷰만 쏙 수정할 수 있게 해준다.
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        //페이로드가 notify시에 같이 넘어오지 않은 경우
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        }
        //페이로드가 notify시에 같이 넘어온 경우
        else {
            String payload = (String) payloads.get(0);
            if (TextUtils.equals(payload, "true")) {
                if (holder instanceof postViewHolder) {
                    //좋아요를 한 상태의 이미지로 셋
                    ((postViewHolder) holder).ib_like.setImageResource(R.drawable.like_clicked);
                    //좋아요 개수 설정
                    ((postViewHolder) holder).tv_likeCount.setText("좋아요 " + postItemArrayList.get(position).likeCount + "개");
                } else if (holder instanceof videoPostViewHolder) {

                    //좋아요를 한 상태의 이미지로 셋
                    ((videoPostViewHolder) holder).ib_like.setImageResource(R.drawable.like_clicked);
                    //좋아요 개수 설정
                    ((videoPostViewHolder) holder).tv_likeCount.setText("좋아요 " + postItemArrayList.get(position).likeCount + "개");
                }
            }

            if (TextUtils.equals(payload, "false")) {
                if (holder instanceof postViewHolder) {
                    //좋아요를 하지 않은 상태의 이미지로 셋
                    ((postViewHolder) holder).ib_like.setImageResource(R.drawable.like_unclicked);
                    //좋아요 개수 설정
                    ((postViewHolder) holder).tv_likeCount.setText("좋아요 " + postItemArrayList.get(position).likeCount + "개");
                } else if (holder instanceof videoPostViewHolder) {
                    //좋아요를 하지 않은 상태의 이미지로 셋
                    ((videoPostViewHolder) holder).ib_like.setImageResource(R.drawable.like_unclicked);
                    //좋아요 개수 설정
                    ((videoPostViewHolder) holder).tv_likeCount.setText("좋아요 " + postItemArrayList.get(position).likeCount + "개");

                }
            }

            if (TextUtils.equals(payload, "commentCount") && (holder instanceof postViewHolder || holder instanceof videoPostViewHolder)) {
                if (holder instanceof postViewHolder) {
                    //댓글 개수 설정
                    ((postViewHolder) holder).tv_commentCount.setText("댓글 " + postItemArrayList.get(position).commentCount + "개");

                    //좋아요 상태 설정
                    //좋아요 상태가 true인 경우
                    //댓글액티비티에서 돌아올 때 좋아요가 unclick상태로 돌아가는 문제를 해결하기 위해서 다시 좋아요 아이콘을 셋해주기 위해서 추가했다.
                    if (postItemArrayList.get(position).isLike == true) {
                        //좋아요를 한 상태의 이미지로 셋
                        ((postViewHolder) holder).ib_like.setImageResource(R.drawable.like_clicked);
                    }
                    //좋아요 상태가 false인 경우
                    else {
                        //좋아요를 하지 않은 상태의 이미지로 셋
                        ((postViewHolder) holder).ib_like.setImageResource(R.drawable.like_unclicked);
                    }
                } else if (holder instanceof videoPostViewHolder) {
                    //댓글 개수 설정
                    ((videoPostViewHolder) holder).tv_commentCount.setText("댓글 " + postItemArrayList.get(position).commentCount + "개");

                    //좋아요 상태 설정
                    //좋아요 상태가 true인 경우
                    //댓글액티비티에서 돌아올 때 좋아요가 unclick상태로 돌아가는 문제를 해결하기 위해서 다시 좋아요 아이콘을 셋해주기 위해서 추가했다.
                    if (postItemArrayList.get(position).isLike == true) {
                        //좋아요를 한 상태의 이미지로 셋
                        ((videoPostViewHolder) holder).ib_like.setImageResource(R.drawable.like_clicked);
                    }
                    //좋아요 상태가 false인 경우
                    else {
                        //좋아요를 하지 않은 상태의 이미지로 셋
                        ((videoPostViewHolder) holder).ib_like.setImageResource(R.drawable.like_unclicked);
                    }
                }
            }

            if (TextUtils.equals(payload, "profile") && (holder instanceof postViewHolder || holder instanceof videoPostViewHolder)) {
                if (holder instanceof postViewHolder) {

                    //프로필 사진 설정
                    Glide.with(context).load("http://13.124.105.47/profileimage/" + postItemArrayList.get(position).profile)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop().error(R.drawable.profile))
                            .into(((postViewHolder) holder).cv_profile);
                } else if (holder instanceof videoPostViewHolder) {

                    //프로필 사진 설정
                    Glide.with(context).load("http://13.124.105.47/profileimage/" + postItemArrayList.get(position).profile)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop().error(R.drawable.profile))
                            .into(((videoPostViewHolder) holder).cv_profile);
                }

            }

            //동영상 게시물의 비디오 재생
            if (TextUtils.equals(payload, "playVideo")) {
                //기존에 재생되고 있던 player resource release
                releasevideo();
                //exoplayer 설정
                if (player == null) {
                    player = ExoPlayerFactory.newSimpleInstance(context);
                }
                String uri = "http://13.124.105.47/uploadvideo/" + postItemArrayList.get(position).video;
                MediaSource mediaSource = buildMediaSource(Uri.parse(uri));//재생할 미디어 소스
                if (isMuted) {
                    player.setVolume(0);//초기 오디오 비활성화
                    ((videoPostViewHolder) holder).ib_mute.setSelected(true);
                } else {
                    player.setVolume(1);//오디오 활성화
                    ((videoPostViewHolder) holder).ib_mute.setSelected(false);
                }
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);//무한 반복
                player.prepare(mediaSource);
                ((videoPostViewHolder) holder).playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                ((videoPostViewHolder) holder).playerView.setPlayer(player);
                player.setPlayWhenReady(true);

                //플레이어의 상태 변화 감지 리스너
                player.addListener(new Player.EventListener() {
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        if (playbackState == Player.STATE_READY) {//영상 재생 준비가 끝나면
                            //썸네일 gone
                            ((videoPostViewHolder) holder).iv_thumbnail.setVisibility(View.GONE);
                            Log.d(PostActivity.class.getSimpleName(), "영상 재생 준비 완료");
                        }
                    }

                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        //영상이 정지되면 썸네일을 다시 visible로 만들어준다.
                        if (!isPlaying) {
                            ((videoPostViewHolder) holder).iv_thumbnail.setVisibility(View.VISIBLE);
                            Log.d(PostActivity.class.getSimpleName(), "영상 재생 중 false");
                        }
                    }
                });


                //동영상 플레이어 클릭 리스너
                ((videoPostViewHolder) holder).playerView.getVideoSurfaceView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(PostActivity.class.getSimpleName(), "플레이어 clicked");
                        if (!isMuted) {//무음 상태가 아닌 경우
                            player.setVolume(0);//소리를 꺼준다.

                            for (int j = 0; j < postItemArrayList.size(); j++) {
                                if (postItemArrayList.get(j).type.equals("video")) {
                                    postItemArrayList.get(j).setMuted(false);
                                    notifyItemChanged(j, "muteControl");
                                }
                            }
                            isMuted = true;
                        } else {//무음 상태인 경우
                            player.setVolume(1);//소리를 켜준다.

                            for (int j = 0; j < postItemArrayList.size(); j++) {
                                if (postItemArrayList.get(j).type.equals("video")) {
                                    postItemArrayList.get(j).setMuted(true);
                                    notifyItemChanged(j, "muteControl");
                                }
                            }
                            isMuted = false;
                        }
                    }
                });

            }


            //동영상 게시물의 비디오 리소스 release
            if (TextUtils.equals(payload, "releaseVideo")) {
                //썸네일 visible
//                ((videoPostViewHolder) holder).iv_thumbnail.setVisibility(View.VISIBLE);
                Log.d(PostActivity.class.getSimpleName(), position + "번 비디오 released");
                releasevideo();
            }


            //동영상 오디오 컨트롤
            if (TextUtils.equals(payload, "muteControl")) {
                if (!isMuted) {//오디오가 활성화되어있는 경우
                    ((videoPostViewHolder) holder).ib_mute.setSelected(false);
                    Log.d(PostAdapter.class.getSimpleName(), "뮤트 버튼 selected false");
                } else {//오디오가 비활성화되어있는 경우
                    ((videoPostViewHolder) holder).ib_mute.setSelected(true);
                    Log.d(PostAdapter.class.getSimpleName(), "뮤트 버튼 selected true");
                }
            }
        }

    }

    //player release
    public void releasevideo() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.release();
            player = null;
        }
    }

    //아이템의 수 리턴함수
    @Override
    public int getItemCount() {
        return (postItemArrayList == null) ? 0 : postItemArrayList.size();
    }

    //화면에서 뷰가 사라지면서 그 사라진 뷰의 내부 데이터가 지워지기 직전에 호출되는 함수로 param에 들어가는 holder는 사라지는 holder를 의미한다.
    //이 홀더 안의 viewpager의 아이템 번호를 기억해서 다시 화면에 나왔을 때 해당 아이템을 표시한 상태를 유지하기 위해서 정의한 메소드
    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        Log.d("onViewRecycled", "호출");
        Log.d("holder index", String.valueOf(holder.getAdapterPosition()));
        if (holder instanceof postViewHolder) {
            if (holder.getAdapterPosition() != -1) {
                viewPagerState.put(holder.getAdapterPosition(), ((postViewHolder) holder).viewPager.getCurrentItem());
            } else {
                imageViewPager.notifyDataSetChanged();
            }

        } else {

        }


    }

    //이미지 게시물 아이템 뷰 홀더 클래스
    private class postViewHolder extends RecyclerView.ViewHolder {

        CircleImageView cv_profile;
        TextView tv_nickname, tv_place, tv_article, tv_likeCount, tv_commentCount, tv_uploadTime, tv_readmore;
        ImageButton ib_more, ib_comment, ib_like;
        ViewPager viewPager;
        TabLayout tabLayout;

        public postViewHolder(View view) {
            super(view);

            cv_profile = view.findViewById(R.id.circleImageView);
            tv_nickname = view.findViewById(R.id.textview_nickname);
            tv_place = view.findViewById(R.id.textview_place);
            tv_article = view.findViewById(R.id.textview_article);
            tv_likeCount = view.findViewById(R.id.textview_like_count);
            tv_commentCount = view.findViewById(R.id.textview_comment_count);
            tv_uploadTime = view.findViewById(R.id.textview_upload_time);
            tv_readmore = view.findViewById(R.id.textview_readmore);
            ib_more = view.findViewById(R.id.button_more);
            ib_comment = view.findViewById(R.id.button_comment);
            ib_like = view.findViewById(R.id.button_like);
            viewPager = view.findViewById(R.id.viewpager);
            tabLayout = view.findViewById(R.id.tab_layout);


        }
    }

    //비디오 게시물 뷰 홀더
    private class videoPostViewHolder extends RecyclerView.ViewHolder {

        CircleImageView cv_profile;
        TextView tv_nickname, tv_place, tv_article, tv_likeCount, tv_commentCount, tv_uploadTime, tv_readmore;
        ImageButton ib_more, ib_comment, ib_like, ib_mute;
        PlayerView playerView;
        ImageView iv_thumbnail;

        public videoPostViewHolder(View view) {
            super(view);

            cv_profile = view.findViewById(R.id.circleImageView);
            tv_nickname = view.findViewById(R.id.textview_nickname);
            tv_place = view.findViewById(R.id.textview_place);
            tv_article = view.findViewById(R.id.textview_article);
            tv_likeCount = view.findViewById(R.id.textview_like_count);
            tv_commentCount = view.findViewById(R.id.textview_comment_count);
            tv_uploadTime = view.findViewById(R.id.textview_upload_time);
            tv_readmore = view.findViewById(R.id.textview_readmore);
            ib_more = view.findViewById(R.id.button_more);
            ib_comment = view.findViewById(R.id.button_comment);
            ib_like = view.findViewById(R.id.button_like);
            ib_mute = view.findViewById(R.id.imagebutton_mute);
            playerView = view.findViewById(R.id.exoplayerview);
            iv_thumbnail = view.findViewById(R.id.imageview_thumbnail);
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


    //뷰페이저 어댑터 클래스
    //로그를 다 찍어본 결과 FragmentPagerAdapter를 상속받으면 게시물을 새로고침해도 getItem메소드가 콜백되지 않아서 notifyDataSet을 해줘도
    //새로운 게시물 사진이 나타나지 않는 문제가 발생한다.
    //그래서 이 문제를 해결하기 위해서 FragmentStatePagerAdapter로 상속을 바꾸고 하단의 getItemPosition메소드를 오버라이드 한 후
    //return값으로 POSITION_NONE;을 해주면 된다.
    public class ImageViewPager extends FragmentStatePagerAdapter {
        //리사이클러뷰의 index
        int index;
        RecyclerView.ViewHolder holder;


        //생성자
        public ImageViewPager(FragmentManager fm, int index, RecyclerView.ViewHolder holder) {
            super(fm);
            this.index = index;
            this.holder = holder;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            Log.d("getItemPosition: ", "호출");
            return POSITION_NONE;
        }


        //현재 뷰페이저에 있는 아이템을 가져온다.
        @Override
        public Fragment getItem(int position) {


            Log.d("getItem: ", "호출");
            Log.d("뷰페이저 사진 개수", String.valueOf(postItemArrayList.get(holder.getAdapterPosition()).imageList.size()));
            Log.d("뷰페이저 사진" + String.valueOf(position) + "uri", postItemArrayList.get(holder.getAdapterPosition()).imageList.get(position));
            return PostImageFragment.newInstance("http://13.124.105.47/uploadimage/" + postItemArrayList.get(holder.getAdapterPosition()).imageList.get(position));
        }


        @Override
        public int getCount() {
            if (holder.getAdapterPosition() != -1) {
//                if(isRemoved){
//                    Log.d("게시물 index: ", String.valueOf(0));
//                    Log.d("getCount: ", "호출");
//                    Log.d(postItemArrayList.get(0).getPostNum()+" 뷰페이저 아이템 수: ", String.valueOf(postItemArrayList.get(0).imageArraylist.size()));
//                    Log.d("아이템의 경로: ", String.valueOf(postItemArrayList.get(0).imageArraylist));
//                    isRemoved = false;
//                    return postItemArrayList.get(0).imageArraylist.size();


//                }else {
                Log.d("게시물 index: ", String.valueOf(holder.getAdapterPosition()));
                Log.d("getCount: ", "호출");
                Log.d(postItemArrayList.get(holder.getAdapterPosition()).getPostNum() + " 뷰페이저 아이템 수: ", String.valueOf(postItemArrayList.get(holder.getAdapterPosition()).imageList.size()));
                Log.d("아이템의 경로: ", String.valueOf(postItemArrayList.get(holder.getAdapterPosition()).imageList));
                return postItemArrayList.get(holder.getAdapterPosition()).imageList.size();
//                }

            }
            return 0;

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

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "SNS"));

        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

}
