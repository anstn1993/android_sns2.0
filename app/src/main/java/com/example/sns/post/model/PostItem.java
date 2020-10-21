package com.example.sns.post.model;

import java.util.ArrayList;

//게시물 리사이클러뷰에 사용될 아이템 클래스
public class PostItem {
    private String getType, account, profile, string, article, time, address, latitude, longitude;
    private int commentCount,likeCount, moreID, postNum, totalCount;
    private boolean isMyPost, isLike;
    private boolean isPlaying = false;//동영상 게시물의 경우 동영상의 재생 여부
    private boolean isMuted = true;//동영상 게시물의 무음 처리 여부
    //이미지를 담을 리스트 객체
    private ArrayList<String> imageList = new ArrayList<>();
    //비디오 파일 명
    private String video;


    public String getType() {
        return getType;
    }

    public void setType(String type) {
        this.getType = type;
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
        return string;
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
        this.string = nickname;
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

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean getIsMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public ArrayList<String> getImageList() {
        return imageList;
    }

    public void setImageList(ArrayList<String> imageList) {
        this.imageList = imageList;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }
}



