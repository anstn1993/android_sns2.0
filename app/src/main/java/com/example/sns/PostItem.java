package com.example.sns;

import java.util.ArrayList;

public class PostItem {

    String type, account, profile, nickname, article, time, address, latitude, longitude;
    int commentCount,likeCount, moreID, postNum, totalCount;
    boolean isMyPost, isLike;
    boolean isPlaying = false;//동영상 게시물의 경우 동영상의 재생 여부
    boolean isMuted = true;//동영상 게시물의 무음 처리 여부

    //이미지를 담을 리스트 객체
    ArrayList<String> imageList = new ArrayList<>();

    //비디오 파일 명
    String video;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getProfile(){
        return profile;
    }

    public String getNickname(){
        return nickname;
    }

    public String getArticle(){
        return article;
    }

    public String getTime(){
        return time;
    }

    public int getMoreID(){
        return moreID;
    }

    public int getCommentCount(){
        return commentCount;
    }

    public int getLikeCount(){
        return likeCount;
    }

    public String getAddress(){
        return address;
    }

    public String getLatitude(){
        return latitude;
    }

    public String getLongitude(){
        return longitude;
    }

    public boolean getIsMyPost(){return  isMyPost; }

    public boolean getIsLike(){return isLike; }

    public int getPostNum(){return postNum;}

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public void setProfile(String profile){
        this.profile = profile;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public void setArticle(String article){
        this.article = article;
    }


    public void setTime(String time){
        this.time = time;
    }

    public void setMoreID(int moreID){
        this.moreID = moreID;
    }

    public void setCommentCount(int commentCount){
        this.commentCount = commentCount;
    }

    public void setLikeCount(int likeCount){
        this.likeCount = likeCount;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public void setLatitude(String latitude){
        this.latitude = latitude;
    }

    public void setLongitude(String longitude){
        this.longitude = longitude;
    }

    public void setIsMyPost(boolean isMyPost){
        this.isMyPost = isMyPost;
    }

    public void setIsLike(boolean isLike){
        this.isLike = isLike;
    }

    public void setPostNum(int postNum){
        this.postNum = postNum;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }
}



