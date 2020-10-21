package com.example.sns.chat.model;

public class ChatUser {
    private String account, nickname, profile;
    public ChatUser(String account, String nickname, String profile){
        this.account = account;
        this.nickname = nickname;
        this.profile = profile;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
