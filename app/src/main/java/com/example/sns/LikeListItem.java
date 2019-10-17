package com.example.sns;

public class LikeListItem {

    String account, profile, nickname;
    int id, postNum;
    boolean isFollowing;

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getId() {
        return id;
    }

    public void setId(int listId) {
        this.id = listId;
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

    public int getPostNum() {
        return postNum;
    }

    public void setPostNum(int postNum) {
        this.postNum = postNum;
    }
}
