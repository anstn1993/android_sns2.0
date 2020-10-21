package com.example.sns.comment;

import java.util.ArrayList;

public class CommentItem {
    //댓글 본문 데이터
    private int id, postNum, childCommentCount;
    private String account, nickname, profile, comment, time;
    private boolean isMyComment;

    //대댓글을 담을 어레이리스트
    private ArrayList<ChildCommentItem> childCommentList = new ArrayList<>();

    //대댓글을 업로드했을 때 업로드한 대댓글의 id를 담을 arraylist
    private ArrayList<Integer> uploadedChildCommentChildNum = new ArrayList<>();


    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    //댓글 본문 데이터 관련 메소드
    public void setPostNum(int postNum){
        this.postNum = postNum;
    }

    public void setId(int commentNum){
        this.id = commentNum;
    }


    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public void setProfile(String profile){
        this.profile = profile;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public void setTime(String time){
        this.time = time;
    }

    public void setIsMyComment(boolean isMyComment){
        this.isMyComment = isMyComment;
    }

    public void setChildCommentCount(int childCommentCount){
        this.childCommentCount = childCommentCount;
    }
//
//    public void setCurrentChildCommentCount(int currentChildCommentCount){
//        this.currentChildCommentCount = currentChildCommentCount;
//    }

    public int getPostNum(){
        return postNum;
    }

    public int getId(){
        return id;
    }


    public String getNickname(){
        return nickname;
    }

    public String getProfile(){
        return profile;
    }

    public String getComment(){
        return comment;
    }

    public String getTime(){
        return time;
    }

    public boolean getIsMyComment(){
        return isMyComment;
    }

    public int getChildCommentCount(){
        return childCommentCount;
    }

//    public int getCurrentChildCommentCount(){
//        return currentChildCommentCount;
//    }



}
