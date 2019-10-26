package com.example.sns;

import java.util.ArrayList;

public class ChatContentItem {

    int id, roomNum;
    String account, profile, nickname, message, time, unCheckedParticipant, type;


    //자신이 보낸 채팅인지 다른 사람이 보낸 채팅인지 가리기 위한 boolean
    boolean isMyContent;
    //자신이 보낸 채팅이 서버로 업로드됐는지 가리기 위한 boolean
    boolean isSent;
    //이미지가 서버로부터 로드된 것인지 로컬에서 업로드된 것인지 가리기 위한 boolean
    boolean isImageFromServer;
    //동영상이 서버로부터 로드된 것인지 로컬에서 업로드된 것인지 가리기 위한 boolean
    boolean isVideoFromServer;
    //아이템이 시간 경계 아이템인지 가리기 위한 boolean
    boolean isTimeDivider;
    //특정 사용자가 나갔을 때 표시되는 아이템인지를 가리기 위한 boolean
    boolean isExit;
    //특정 사용자를 초대했을 때 표시되는 아이템인지를 가리기 위한 boolean
    boolean isAddedParticipantMessage;

    ArrayList<String> imageList = new ArrayList<>();
    String video;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(int roomNum) {
        this.roomNum = roomNum;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ArrayList<String> getImageList() {
        return imageList;
    }

    public void setImageList(ArrayList<String> imageList) {
        this.imageList = imageList;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public boolean isImageFromServer() {
        return this.isImageFromServer;
    }

    public void setImageFromServer(boolean isImageFromServer) {
        this.isImageFromServer = isImageFromServer;
    }

    public boolean isVideoFromServer() {
        return isVideoFromServer;
    }

    public void setVideoFromServer(boolean videoFromServer) {
        isVideoFromServer = videoFromServer;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isMyContent() {
        return isMyContent;
    }

    public void setMyContent(boolean myContent) {
        isMyContent = myContent;
    }

    public boolean isTimeDivider() {
        return isTimeDivider;
    }

    public void setTimeDivider(boolean timeDivider) {
        isTimeDivider = timeDivider;
    }

    public boolean isExit() {
        return isExit;
    }

    public void setExit(boolean exit) {
        isExit = exit;
    }

    public String getUnCheckedParticipant() {
        return unCheckedParticipant;
    }

    public void setUnCheckedParticipant(String unCheckedParticipant) {
        this.unCheckedParticipant = unCheckedParticipant;
    }

    public boolean isAddedParticipantMessage() {
        return isAddedParticipantMessage;
    }

    public void setAddedParticipantMessage(boolean addedParticipantMessage) {
        isAddedParticipantMessage = addedParticipantMessage;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }
}
