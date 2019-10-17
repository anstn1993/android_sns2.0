package com.example.sns;

public class AddChatResponse {
    String id;
    String roomNum;
    String sender;
    String receiver;
    String message;
    String type;
    String image1, image2, image3, image4, image5, image6;
    String time;
    String unCheckedParticipant;

    public String getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(String roomNum) {
        this.roomNum = roomNum;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
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

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getImage3() {
        return image3;
    }

    public void setImage3(String image3) {
        this.image3 = image3;
    }

    public String getImage4() {
        return image4;
    }

    public void setImage4(String image4) {
        this.image4 = image4;
    }

    public String getImage5() {
        return image5;
    }

    public void setImage5(String image5) {
        this.image5 = image5;
    }

    public String getImage6() {
        return image6;
    }

    public void setImage6(String image6) {
        this.image6 = image6;
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
}
