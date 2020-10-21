package com.example.sns.search.model;

public class SearchedPlaceItem {
    //게시물이 업로드된 장소의 위도와 경도
    private double latitude, longitude;

    //게시물 id
    private int id;

    //해당 장소로 업로드된 게시물의 수
    private int totalCount;

    //게시물이 업로드된 장소명
    private String address;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int listId) {
        this.id = listId;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
