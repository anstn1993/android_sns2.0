package com.example.sns;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChildCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //뷰 타입 숫자 설정
    private int CHILDCOMMENT_VIEW = 0;
    private int MORE_VIEW = 1;

    //리사이클러뷰의 데이터를 담아줄 arraylist
    private ArrayList<ChildCommentItem> childCommentItemArrayList;

    //부모 댓글의 position
    private int parentCommentPosition;

    //리사이클러뷰의 컨택스트
    Context context;

    //대댓글 어댑터 생성자
    public ChildCommentAdapter(Context context, ArrayList<ChildCommentItem> childCommentItemArrayList, int parentCommentPosition) {
        this.context = context;
        this.childCommentItemArrayList = childCommentItemArrayList;
        this.parentCommentPosition = parentCommentPosition;

    }

    ChildCommentRecyclerViewListener mListener;

    //액티비티 클래스에서 리사이클러뷰의 데이터 클릭 이벤트를 제어하기 위한 인터페이스
    interface ChildCommentRecyclerViewListener{
        //대댓글 수정 버튼 클릭시 콜백 메소드
        void onEditChildCommentClicked(int position, int parentPosition);
        //대댓글 삭제 버튼 클릭시 콜백 메소드
        void onDeleteChildCommentClicked(int position, int parentPosition);
        //대댓글 리스트 더 보기 버튼 클릭시 콜백 메소드
        void onMoreCommentClicked(int position, int parentPosition);

    }

    //액티비티를 리스너로 설정해주기 위한 메소드
    public void setOnClickListener(ChildCommentRecyclerViewListener mListener){
        this.mListener = mListener;
    }


    @Override
    public int getItemViewType(int position) {
        //대댓글 아이템 arraylist가 null이 아닌 경우에는 대댓글 뷰를, null인 경우에는 더 보기 뷰를 뷰타입으로 return해준다.
        return (childCommentItemArrayList.get(position) != null)?CHILDCOMMENT_VIEW:MORE_VIEW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //뷰 타입이 대댓글 뷰인 경우
        if(viewType == CHILDCOMMENT_VIEW){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.childcommentitem, viewGroup, false);
            return new ChildCommentHolder(view);
        }
        //뷰 타입이 더 보기 뷰인 경우
        else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.childcommentmoreitem, viewGroup, false);
            return new MoreCommentHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {

        //뷰 홀더가 대댓글 홀더인 경우
        if(holder instanceof ChildCommentHolder){
            //댓글 작성자의 프로필 사진 설정
            Glide.with(context).load("http://13.124.105.47/profileimage/"+childCommentItemArrayList.get(i).profile)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                    .into(((ChildCommentHolder)holder).cv_profile);


            //댓글 작성자의 닉네임과 댓글 설정
            ((ChildCommentHolder)holder).tv_comment.post(new Runnable() {
                @Override
                public void run() {
                    ((ChildCommentHolder)holder).tv_comment.setText(childCommentItemArrayList.get(i).nickname+"     "+childCommentItemArrayList.get(i).comment);
                    //댓글이 두 줄을 넘으면
                    if(((ChildCommentHolder)holder).tv_comment.getLineCount()>2){
                        //더 보기가 보이게 하고
                        ((ChildCommentHolder)holder).tv_more.setVisibility(View.VISIBLE);
                        //댓글의 줄 수를 2줄로 줄인다
                        ((ChildCommentHolder)holder).tv_comment.setMaxLines(2);
                    }
                    //댓글이 2줄 이하면
                    else{
                        //더 보기가 안 보이게 하고
                        ((ChildCommentHolder)holder).tv_more.setVisibility(View.GONE);
                        //댓글의 줄 수만큼 다 보이게 한다.
                        ((ChildCommentHolder)holder).tv_comment.setMaxLines(Integer.MAX_VALUE);
                    }
                }
            });

            //더 보기 버튼 클릭 리스너
            ((ChildCommentHolder)holder).tv_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //글자를 모두 보여준다.
                    ((ChildCommentHolder)holder).tv_comment.setMaxLines(Integer.MAX_VALUE);
                    //그리고 더 보기 버튼은 사라지게끔
                    ((ChildCommentHolder)holder).tv_more.setVisibility(View.GONE);
                }
            });


            //댓글 작성 시간 설정
            Date date = new Date();
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date = simpleDateFormat.parse(childCommentItemArrayList.get(i).time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ((ChildCommentHolder)holder).tv_time.setText(beforeTime(date));

            //수정 삭제 버튼 설정
            //댓글이 현재 로그인한 사용자의 댓글인 경우
            if(childCommentItemArrayList.get(i).isMyComment==true){
                //수정, 삭제 버튼이 나타나게 함
                ((ChildCommentHolder)holder).tv_editcomment.setVisibility(View.VISIBLE);
                ((ChildCommentHolder)holder).tv_deletecomment.setVisibility(View.VISIBLE);
            }
            //댓글이 현재 로그인한 사용자의 댓글이 아닌 경우
            else {
                //수정 삭제 버튼이 보이지 않게 함
                ((ChildCommentHolder)holder).tv_editcomment.setVisibility(View.GONE);
                ((ChildCommentHolder)holder).tv_deletecomment.setVisibility(View.GONE);
            }

            //수정 버튼 클릭 리스너
            ((ChildCommentHolder)holder).tv_editcomment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEditChildCommentClicked(i, parentCommentPosition);
                }
            });

            //삭제 버튼 클릭 리스너
            ((ChildCommentHolder)holder).tv_deletecomment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeleteChildCommentClicked(i, parentCommentPosition);
                }
            });

        }
        //뷰 홀더가 대댓글 더 보기인 경우
        else{
            //대댓글 더 보기 버튼 클릭 리스너
            ((MoreCommentHolder)holder).tv_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onMoreCommentClicked(i, parentCommentPosition);
                }
            });

            //대댓글 더 보기 버튼 클릭 리스너
            ((MoreCommentHolder)holder).divider.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onMoreCommentClicked(i, parentCommentPosition);
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return (childCommentItemArrayList == null)?0:childCommentItemArrayList.size();
    }

    //대댓글 뷰 홀더 클래스
    public class ChildCommentHolder extends RecyclerView.ViewHolder {

        CircleImageView cv_profile;
        TextView tv_comment, tv_more, tv_time, tv_editcomment, tv_deletecomment;

        public ChildCommentHolder(@NonNull View itemView) {
            super(itemView);

            cv_profile = itemView.findViewById(R.id.circleimageview_profile);
            tv_comment = itemView.findViewById(R.id.textview_comment);
            tv_more = itemView.findViewById(R.id.textview_readmore);
            tv_time = itemView.findViewById(R.id.textview_time);
            tv_editcomment = itemView.findViewById(R.id.textview_editcomment);
            tv_deletecomment = itemView.findViewById(R.id.textview_deletecomment);
        }
    }

    //대댓글 더 보기 홀더 클래스
    public class MoreCommentHolder extends RecyclerView.ViewHolder{
        View divider;
        TextView tv_more;

        public MoreCommentHolder(@NonNull View itemView) {
            super(itemView);
            divider = itemView.findViewById(R.id.divider);
            tv_more = itemView.findViewById(R.id.textview_more);
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
