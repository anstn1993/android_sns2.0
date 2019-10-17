package com.example.sns;

class ChatImageItem {

    String imageData;//채팅방 이미지의 데이터를 담은 json스트링
    boolean isSelected;//이미지 선택모드에 진입한 경우 이미지가 선택된 상태인지 가리는 boolean
    boolean isSelectMode;//이미지 선택 모드인지 아닌지를 가리는 boolean

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelectMode() {
        return isSelectMode;
    }

    public void setSelectMode(boolean selectMode) {
        isSelectMode = selectMode;
    }
}
