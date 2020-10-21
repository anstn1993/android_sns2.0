package com.example.sns.chat.model;

import java.util.ArrayList;

public class ChatContentItem {

    private int id, roomNum;
    private String account, profile, nickname, message, time, unCheckedParticipant, type;
    private ChatType chatType;//채팅 메세지 타입 enum
    private boolean isMyContent;
    //자신이 보낸 채팅이 서버로 업로드됐는지 가리기 위한 boolean
    private boolean isSent;
    //이미지가 서버로부터 로드된 것인지 로컬에서 업로드된 것인지 가리기 위한 boolean
    private boolean isImageFromServer;
    //동영상이 서버로부터 로드된 것인지 로컬에서 업로드된 것인지 가리기 위한 boolean
    private boolean isVideoFromServer;
    private ArrayList<String> imageList = new ArrayList<>();
    private String video;

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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUnCheckedParticipant() {
        return unCheckedParticipant;
    }

    public void setUnCheckedParticipant(String unCheckedParticipant) {
        this.unCheckedParticipant = unCheckedParticipant;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public void setChatType(ChatType chatType) {
        this.chatType = chatType;
    }

    public boolean getIsMyContent() {
        return isMyContent;
    }

    public void setIsMyContent(boolean isMyContent) {
        this.isMyContent = isMyContent;
    }

    public ArrayList<String> getImageList() {
        return imageList;
    }

    public void setImageList(ArrayList<String> imageList) {
        this.imageList = imageList;
    }

    public boolean getIsSent() {
        return isSent;
    }

    public void setIsSent(boolean isSent) {
        this.isSent = isSent;
    }

    public boolean getIsImageFromServer() {
        return this.isImageFromServer;
    }

    public void setIsImageFromServer(boolean isImageFromServer) {
        this.isImageFromServer = isImageFromServer;
    }

    public boolean getIsVideoFromServer() {
        return isVideoFromServer;
    }

    public void setIsVideoFromServer(boolean isVideoFromServer) {
        this.isVideoFromServer = isVideoFromServer;
    }


    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }
}
