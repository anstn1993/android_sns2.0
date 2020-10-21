package com.example.sns.chat.model;

/*
채팅방의 컨텐츠 서랍에 들어가면 나오는 컨텐츠 리스트의 아이템 클래스
 */
public class ChatContentListItem {

    private String contentData;//채팅방 컨텐츠의 데이터를 담은 json스트링
    private boolean isSelected;//컨텐츠 선택모드에 진입한 경우 이미지가 선택된 상태인지 가리는 boolean
    private boolean isSelectMode;//컨텐츠 선택 모드인지 아닌지를 가리는 boolean

    public String getContentData() {
        return contentData;
    }

    public void setContentData(String contentData) {
        this.contentData = contentData;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean getIsSelectMode() {
        return isSelectMode;
    }

    public void setIsSelectMode(boolean isSelectMode) {
        this.isSelectMode = isSelectMode;
    }
}
