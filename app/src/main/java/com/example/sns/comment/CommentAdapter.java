package com.example.sns.comment;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sns.R;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //뷰 타입 설정
    final int COMMENT_HEADER = 0;
    final int COMMENT_ITEM = 1;
    final int VIEW_PROGRESS = 2;

    //리사이클러뷰의 아이템을 담을 arraylist
    private ArrayList<CommentItem> commentItemArrayList;


    //어댑터의 컨택스트
    private Context context;

    //comment 액티비티
    CommentActivity activity;

    //댓글 헤더 데이터
    String postProfile,postNickname, postArticle, postTime;

    //해시태그 라이브러리
    HashTagHelper hashTagHelper;

    //댓글 리사이클러뷰의 클릭 리스너
    CommentRecyclerViewListener mListener;

    //대댓글 리사이클러뷰의 레이아웃 매니저
    LinearLayoutManager linearLayoutManager;
    //대댓글 리사이클러뷰의 어댑터
    ChildCommentAdapter childCommentAdapter;

    interface CommentRecyclerViewListener{
        //댓글 수정 버튼 클릭시 콜백
        void onEditCommentClicked(int position);

        //댓글 삭제 버튼 클릭시 콜백
        void onDeleteCommentClicked(int position);

        //프로필 사진 버튼 클릭시 콜백
        void onProfileClicked(int position);

        //댓글 클릭시 콜백
        void onCommentClicked(int position);

        void onHashTagClicked(int position);

        void onChildCommentClicked(int position);
    }

    //댓글 액티비티를 리스너로 설정해주는 메소드
    public void setOnClickListener(CommentRecyclerViewListener mListener){
        this.mListener = mListener;
    }

    //어댑터의 생성자
    public CommentAdapter(Context context,
                          ArrayList<CommentItem> commentItemArrayList,
                          String postProfile,
                          String postNickname,
                          String postArticle,
                          String postTime,
                          CommentActivity activity) {
        this.commentItemArrayList = commentItemArrayList;
        this.context = context;
        this.postProfile = postProfile;
        this.postNickname = postNickname;
        this.postArticle = postArticle;
        this.postTime = postTime;
        this.activity = activity;
    }

    //뷰타입 설정


    //param: 리사이클러뷰의 index
    @Override
    public int getItemViewType(int position) {
        //댓글 리사이클러 뷰의 index가 0인 경우
        if(position == 0){
            return COMMENT_HEADER;
        }
        //arraylist에 null이 들어간 경우
        else if(commentItemArrayList.get(position-1) == null){
           return VIEW_PROGRESS;
        }
        //그 외의 모든 경우
        else {
            return COMMENT_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        //뷰 타입이 댓글의 헤더인 경우
        if(viewType == COMMENT_HEADER){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.commentheader, viewGroup, false);
            return new CommentHeaderHolder(view);
        }
        //뷰 타입이 댓글인 경우
        else if (viewType == COMMENT_ITEM){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.commentitem, viewGroup, false);
            return new CommentItemHolder(view);
        }
        //뷰 타입이 프로그래스 바인 경우
        else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.progressitem, viewGroup, false);
            return new ProgressHolder(view);
        }


    }
    //payload가 없는 경우
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {

        //뷰홀더가 댓글 헤더인 경우
        if(holder instanceof CommentHeaderHolder){
            //게시물 작성자의 프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/"+postProfile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((CommentHeaderHolder)holder).cv_profile);

            //게시물 작성자의 닉네임과 게시물 셋


            ((CommentHeaderHolder)holder).tv_article.setText(postNickname+"     "+postArticle);

            hashTagHelper =HashTagHelper.Creator.create(Color.parseColor("#02B2ED"), new HashTagHelper.OnHashTagClickListener() {
                //해시태그를 클릭했을 때 콜백(차후 클릭한 해시태그를 가진 게시물을 모아서 보여주는 액티비티로 이동시킬 예정)
                @Override
                public void onHashTagClicked(String hashTag) {
                    mListener.onHashTagClicked(i);
                }
            });
            //게시글을 라이브러리로 제어해서 해시태그가 보이게끔
            hashTagHelper.handle(((CommentHeaderHolder)holder).tv_article);



            //게시물 작성 시간 설정
            Date date = new Date();
            try {

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date = simpleDateFormat.parse(postTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            ((CommentHeaderHolder)holder).tv_time.setText(beforeTime(date));

        }
        //뷰홀더가 댓글 아이템인 경우
        else if(holder instanceof CommentItemHolder){

            //댓글 작성자의 프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/"+commentItemArrayList.get(i-1).getProfile())
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((CommentItemHolder)holder).cv_profile);

            //댓글 작성자의 닉네임과 댓글 설정

            ((CommentItemHolder)holder).tv_comment.post(new Runnable() {
                @Override
                public void run() {
                    ((CommentItemHolder)holder).tv_comment.setText(commentItemArrayList.get(i-1).getNickname()+"     "+commentItemArrayList.get(i-1).getComment());
                    //댓글이 두 줄을 넘으면
                    if(((CommentItemHolder)holder).tv_comment.getLineCount()>2){
                        //더 보기가 보이게 하고
                        ((CommentItemHolder)holder).tv_readmore.setVisibility(View.VISIBLE);
                        //댓글의 줄 수를 2줄로 줄인다
                        ((CommentItemHolder)holder).tv_comment.setMaxLines(2);
                    }
                    //댓글이 2줄 이하면
                    else{
                        //더 보기가 안 보이게 하고
                        ((CommentItemHolder)holder).tv_readmore.setVisibility(View.GONE);
                        //댓글의 줄 수만큼 다 보이게 한다.
                        ((CommentItemHolder)holder).tv_comment.setMaxLines(Integer.MAX_VALUE);
                    }
                }
            });

            //더 보기 버튼 클릭 리스너
            ((CommentItemHolder)holder).tv_readmore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //글자를 모두 보여준다.
                    ((CommentItemHolder)holder).tv_comment.setMaxLines(Integer.MAX_VALUE);
                    //그리고 더 보기 버튼은 사라지게끔
                    ((CommentItemHolder)holder).tv_readmore.setVisibility(View.GONE);
                }
            });



            //댓글 작성 시간 설정
            Date date = new Date();
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date = simpleDateFormat.parse(commentItemArrayList.get(i-1).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ((CommentItemHolder)holder).tv_time.setText(beforeTime(date));

            //수정 삭제 버튼 설정
            //댓글이 현재 로그인한 사용자의 댓글인 경우
            if(commentItemArrayList.get(i-1).getIsMyComment()==true){
                //수정, 삭제 버튼이 나타나게 함
                ((CommentItemHolder)holder).tv_editcomment.setVisibility(View.VISIBLE);
                ((CommentItemHolder)holder).tv_deletecomment.setVisibility(View.VISIBLE);
            }
            //댓글이 현재 로그인한 사용자의 댓글이 아닌 경우
            else {
                //수정 삭제 버튼이 보이지 않게 함
                ((CommentItemHolder)holder).tv_editcomment.setVisibility(View.GONE);
                ((CommentItemHolder)holder).tv_deletecomment.setVisibility(View.GONE);
            }

            //수정 버튼 클릭 리스너
            ((CommentItemHolder)holder).tv_editcomment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEditCommentClicked(i);
                }
            });

            //삭제 버튼 클릭 리스너
            ((CommentItemHolder)holder).tv_deletecomment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeleteCommentClicked(i);
                }
            });

            //답글 달기 버튼 클릭 리스너
            ((CommentItemHolder)holder).tv_addchildcomment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onChildCommentClicked(i);
                }
            });

            ((CommentItemHolder)holder).rv_childcomment.setHasFixedSize(true);

            //대댓글 레이아웃 메니저 설정
            linearLayoutManager = new LinearLayoutManager(context);
            ((CommentItemHolder)holder).rv_childcomment.setLayoutManager(linearLayoutManager);

            //대댓글 어댑터 설정
            childCommentAdapter = new ChildCommentAdapter(context, commentItemArrayList.get(i-1).childCommentList, i);
            childCommentAdapter.setOnClickListener(activity);
            ((CommentItemHolder)holder).rv_childcomment.setAdapter(childCommentAdapter);

        }
        //뷰 홀더가 프로그래스 바인 경우
        else {

        }
    }

    //payload가 있는 경우
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        //payload가 넘어오지 않은 경우
        if(payloads.isEmpty()){
            super.onBindViewHolder(holder, position, payloads);
        }
        //payload가 넘어온 경우
        else {
            String payload = (String)payloads.get(0);
            //payload의 키값이 editComment이면서 뷰홀더가 댓글 아이템의 뷰홀더인 경우
            if(TextUtils.equals(payload, "editComment") && holder instanceof CommentItemHolder){
                //댓글 작성자의 닉네임과 댓글 설정
                ((CommentItemHolder)holder).tv_comment.post(new Runnable() {
                    @Override
                    public void run() {
                        ((CommentItemHolder)holder).tv_comment.setText(commentItemArrayList.get(position-1).getNickname()+"     "+commentItemArrayList.get(position-1).getComment());
                        //댓글이 두 줄을 넘으면
                        if(((CommentItemHolder)holder).tv_comment.getLineCount()>2){
                            //더 보기가 보이게 하고
                            ((CommentItemHolder)holder).tv_readmore.setVisibility(View.VISIBLE);
                            //댓글의 줄 수를 2줄로 줄인다
                            ((CommentItemHolder)holder).tv_comment.setMaxLines(2);
                        }
                        //댓글이 2줄 이하면
                        else{
                            //더 보기가 안 보이게 하고
                            ((CommentItemHolder)holder).tv_readmore.setVisibility(View.GONE);
                            //댓글의 줄 수만큼 다 보이게 한다.
                            ((CommentItemHolder)holder).tv_comment.setMaxLines(Integer.MAX_VALUE);
                        }
                    }
                });

            }


        }

    }

    @Override
    public int getItemCount() {
        return (commentItemArrayList == null)?1:commentItemArrayList.size()+1;
    }


    //댓글 헤더 홀더 클래스
    private class CommentHeaderHolder extends RecyclerView.ViewHolder {

        CircleImageView cv_profile;
        TextView tv_article, tv_time;

        public CommentHeaderHolder(@NonNull View itemView) {
            super(itemView);

            cv_profile = itemView.findViewById(R.id.circleimageview_profile);
            tv_article = itemView.findViewById(R.id.textview_article);
            tv_time = itemView.findViewById(R.id.textview_time);
        }
    }

    //댓글 본문 홀더 클래스
    private class CommentItemHolder extends RecyclerView.ViewHolder {

        CircleImageView cv_profile;
        TextView tv_comment, tv_readmore, tv_time, tv_addchildcomment, tv_editcomment, tv_deletecomment;
        RecyclerView rv_childcomment;

        public CommentItemHolder(@NonNull View itemView) {
            super(itemView);

            cv_profile = itemView.findViewById(R.id.circleimageview_profile);
            tv_comment = itemView.findViewById(R.id.textview_comment);
            tv_readmore = itemView.findViewById(R.id.textview_readmore);
            tv_time = itemView.findViewById(R.id.textview_time);
            tv_addchildcomment = itemView.findViewById(R.id.textview_addchildcomment);
            tv_editcomment = itemView.findViewById(R.id.textview_editcomment);
            tv_deletecomment = itemView.findViewById(R.id.textview_deletecomment);
            rv_childcomment = itemView.findViewById(R.id.recyclerview_childcomment);
        }
    }

    //프로그래스 홀더 클래스
    private class ProgressHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public ProgressHolder(@NonNull View itemView) {
            super(itemView);

            progressBar = itemView.findViewById(R.id.progressbar);
        }
    }

    //업로드된 게시물이 몇 분 전에 만들어진 게시물인지를 리턴해주는 메소드
    public String beforeTime(Date date){

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
        gap = (long)(gap/1000);
        long hour = gap/3600;
        gap = gap%3600;
        long min = gap/60;
        long sec = gap%60;

        if(hour > 24){
            ret = hour/24+"일 전";
        }
        else if(hour > 0){
            ret = hour+"시간 전";
        }
        else if(min > 0){
            ret = min+"분 전";
        }
        else if(sec > 0){
            ret = sec+"초 전";
        }
        else{
            ret = new SimpleDateFormat("HH:mm").format(date);
        }
        return ret;

    }

}
