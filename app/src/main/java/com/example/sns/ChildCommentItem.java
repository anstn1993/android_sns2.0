package com.example.sns;


import java.util.ArrayList;

public class ChildCommentItem {


    public int postNum, commentNum, id;
    public String account, nickname, profile, comment, time;
    public boolean isMyComment;


    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setPostNum(int postNum) {
        this.postNum = postNum;
    }

    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
    }

    public void setChildCommentNum(int childCommentNum) {
        this.id = childCommentNum;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setIsMyComment(boolean isMyComment) {
        this.isMyComment = isMyComment;
    }


    public int getPostNum() {
        return postNum;
    }

    public int getCommentNum() {
        return commentNum;
    }

    public int getChildCommentNum() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfile() {
        return profile;
    }

    public String getComment() {
        return comment;
    }

    public String getTime() {
        return time;
    }

    public boolean getIsMyComment() {
        return isMyComment;
    }

}
