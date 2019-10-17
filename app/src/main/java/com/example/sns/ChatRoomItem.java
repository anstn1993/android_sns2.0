package com.example.sns;

import java.util.ArrayList;

public class ChatRoomItem {
    //채팅방 번호
    int roomNum;
    //마지막 메세지
    String message;
    //메세지 타입
    String type;
    //마지막 메세지의 전송 시간
    String time;

    //새로온 채팅 개수
    int newMessageCount;
    //채팅 참여자 리스트(사용자 닉네임과 프로필 사진 파일명을 /로 구분해서 넣어준 뒤에 split으로 쪼개서 사용한다.)
    ArrayList<User> userList = new ArrayList<>();

    public int getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(int roomNum) {
        this.roomNum = roomNum;
    }

    public ArrayList<User> getUserList() {
        return userList;
    }

    public void setUserList(String account, String nickname, String profile) {
        User user = new User(account, nickname, profile);
        userList.add(user);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public int getNewMessageCount() {
        return newMessageCount;
    }

    public void setNewMessageCount(int newMessageCount) {
        this.newMessageCount = newMessageCount;
    }


    public static class User {
        String account, nickname, profile;
        public User(String account, String nickname, String profile){
            this.account = account;
            this.nickname = nickname;
            this.profile = profile;
        };

    }
}
