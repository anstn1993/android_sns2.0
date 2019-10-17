package com.example.sns;

public class UserItem {

    String account, profile, nickname, name;
    int id;
    boolean isSelected;
    //이 변수는 이미 만들어져있는 채팅방에서 사용자를 추가할 때 기존에 참여하고 있던 사용자인지 아닌지를 가리기 위한 변수다.
    boolean isCurrentParticipant;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isCurrentParticipant() {
        return isCurrentParticipant;
    }

    public void setCurrentParticipant(boolean currentParticipant) {
        isCurrentParticipant = currentParticipant;
    }
}
