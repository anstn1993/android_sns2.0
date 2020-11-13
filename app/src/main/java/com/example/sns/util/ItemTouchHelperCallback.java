package com.example.sns.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
    //업도르할 사진의 리사이클러뷰의 어댑터에 부착해서 사용할 인터페이스
    public interface OnItemMoveListener{
        Boolean onItemMove(int fromPosition, int toPosition);
        void onItemMoved(int position);
    }

    private final OnItemMoveListener mItemMoveListener;
    public ItemTouchHelperCallback(OnItemMoveListener mItemMoveListener) {
        this.mItemMoveListener = mItemMoveListener;
    }

    //선택한 아이템을 어느 방향으로 움직여줄지 결정해주는 메소드다.
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        //스와이프 동작은 수행하지 않을 것이기 때문에 0을 넣어준다.
        return makeMovementFlags(dragFlags, 0);
    }

    //드래그앤 드롭을 했을 때 어떤 동작이 수행될지 정의해주는 메소드다. 이때 리사이클러뷰의 어댑터가 이동에 의해 데이터의 순서가 변하는 것을
    //감지할 수 있게 해줘야하기 때문에 어뎁터에서 callback메소드를 통해서 이 메소드가 실행되게 해줘야 한다.
    //그래서 어뎁터에 부착시켜서 사용할 OnItemMoveListener 인터페이스를 만들어준다.
    //param1: 이동 전의 뷰홀더
    //param2: 이동 후의 뷰홀더
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        mItemMoveListener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    //드래그앤 드롭이 끝나면 호출되는 메소드
    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        mItemMoveListener.onItemMoved(viewHolder.getAdapterPosition());
    }
    //스와이프가 일어나면 호출되는 메소드
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }
}


