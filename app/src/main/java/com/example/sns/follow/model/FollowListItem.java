package com.example.sns.follow.model;

public class FollowListItem {

    private String account, profile, nickname;
    private int id;
    private boolean isFollowing;

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

    public int getId() {
        return id;
    }

    public void setId(int listId) {
        this.id = listId;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public boolean getIsFollowing() {
        return isFollowing;
    }

    public void setIsFollowing(boolean isFollowing) {
        this.isFollowing = isFollowing;
    }
}
