package com.example.sns.post.upload.model;

public class UploadImageItem {

    public String imageSource;
    public int deleteID;
    public String imageRoot;

    public UploadImageItem(String imageSource, int deleteID, String imageRoot){
        this.imageSource = imageSource;
        this.deleteID = deleteID;
        this.imageRoot = imageRoot;
    }
}
