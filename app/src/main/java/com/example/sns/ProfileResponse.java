package com.example.sns;

public class ProfileResponse {
    int postCount;
    int followerCount;
    int followingCount;

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    String account;
    String name;
    String nickname;
    String email;
    String introduce;
    String image;
    boolean isFollowing;

    public boolean getIsFollowing() {
        return isFollowing;
    }

    public void setIsFollowing(boolean following) {
        isFollowing = following;
    }

    public String getAccount(){
        return account;
    }

    public String getName(){
        return name;
    }

    public String getNickname(){
        return nickname;
    }

    public String getIntroduce(){
        return introduce;
    }


    public String getImage(){
        return image;
    }

    public String getEmail(){
        return email;
    }

}
