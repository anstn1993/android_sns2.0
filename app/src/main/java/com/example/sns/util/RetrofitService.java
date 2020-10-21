package com.example.sns.util;

import com.example.sns.mypage.model.ProfileResponse;
import com.example.sns.chat.model.AddChatResponse;
import com.example.sns.post.upload.model.UploadResponse;
import com.example.sns.post.upload.model.UploadVideoResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RetrofitService {

    //프로필 수정
    @Multipart
    @POST("editprofile.php")
    Call<ProfileResponse> getEditProfileResponse(
            @Part("account") RequestBody account,
            @Part("name") RequestBody name,
            @Part("nickname") RequestBody nickname,
            @Part("introduce") RequestBody introduce,
            @Part("isselected") RequestBody isselected,
            @Part MultipartBody.Part File
    );

    //게시물 업로드
    @Multipart
    @POST("upload.php")
    Call<UploadResponse> uploadResponse(
        @Part("postNum") RequestBody postNum,
        @Part("account") RequestBody account,
        @Part("article") RequestBody article,
        @Part MultipartBody.Part File1,
        @Part MultipartBody.Part File2,
        @Part MultipartBody.Part File3,
        @Part MultipartBody.Part File4,
        @Part MultipartBody.Part File5,
        @Part MultipartBody.Part File6,
        @Part("address") RequestBody address,
        @Part("latitude") RequestBody latitude,
        @Part("longitude") RequestBody lonitude
    );

    //게시물 수정
    @Multipart
    @POST("editpost.php")
    Call<UploadResponse> editResponse(
            @Part("postNum") RequestBody postNum,
            @Part("account") RequestBody account,
            @Part("article") RequestBody article,
            @Part MultipartBody.Part File1,
            @Part MultipartBody.Part File2,
            @Part MultipartBody.Part File3,
            @Part MultipartBody.Part File4,
            @Part MultipartBody.Part File5,
            @Part MultipartBody.Part File6,
            @Part("address") RequestBody address,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody lonitude
    );

    //동영상 게시물 업로드
    @Multipart
    @POST("uploadvideo.php")
    Call<UploadVideoResponse> uploadVideoResponse(
            @Part("postNum") RequestBody postNum,
            @Part("account") RequestBody account,
            @Part("article") RequestBody article,
            @Part MultipartBody.Part File,
            @Part("address") RequestBody address,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody lonitude
    );

    //동영상 게시물 수정
    @Multipart
    @POST("editvideopost.php")
    Call<UploadVideoResponse> editVideoResponse(
            @Part("postNum") RequestBody postNum,
            @Part("account") RequestBody account,
            @Part("article") RequestBody article,
            @Part MultipartBody.Part File,
            @Part("address") RequestBody address,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody lonitude
    );

    //채팅 전송
    @Multipart
    @POST("addchat.php")
    Call<AddChatResponse> addChatResponse(
            @Part("roomNum") RequestBody roomNum,
            @Part("sender") RequestBody sender,
            @Part("receiver") RequestBody receiver,
            @Part("message") RequestBody message,
            @Part("type") RequestBody type,
            //이미지 파일 최대 6개
            @Part MultipartBody.Part imageFile1,
            @Part MultipartBody.Part imageFile2,
            @Part MultipartBody.Part imageFile3,
            @Part MultipartBody.Part imageFile4,
            @Part MultipartBody.Part imageFile5,
            @Part MultipartBody.Part imageFile6,
            //동영상 파일 1개
            @Part MultipartBody.Part videoFile
    );


    //마이페이지 데이터 가져오기
    @GET("mypage.php")
    Call<ProfileResponse> getProfileResponse(
            @Query("userAccount") String userAccount,
            @Query("myAccount") String myAccount
    );

}
