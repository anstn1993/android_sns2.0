package com.example.sns.search.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import com.example.sns.R;
import com.example.sns.search.model.SearchedPlaceItem;
import de.hdodenhof.circleimageview.CircleImageView;

public class SearchedPlaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //뷰타입
    final int VIEW_PLACE = 0;
    final int VIEW_PROGRESS = 1;

    //아이템 어레이리스트
    private ArrayList<SearchedPlaceItem> searchedPlaceItemArrayList = new ArrayList<>();
    private Context context;

    public SearchedPlaceAdapter(ArrayList<SearchedPlaceItem> searchedPlaceItemArrayList, Context context) {
        this.searchedPlaceItemArrayList = searchedPlaceItemArrayList;
        this.context = context;
    }

    public interface SearchedPlaceRecyclerViewListener{
        void onListClicked(int position);
    }

    SearchedPlaceRecyclerViewListener mListener;

    public void setOnClickListener(SearchedPlaceRecyclerViewListener listener){
        this.mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {

        return (searchedPlaceItemArrayList.get(position)==null)?VIEW_PROGRESS:VIEW_PLACE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        if(viewType==VIEW_PLACE){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.searchedplaceitem, viewGroup, false);
            return new SearchedPlaceViewHolder(view);
        }else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.progressitem, viewGroup, false);
            return new ProgressViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof SearchedPlaceViewHolder){
            //장소 설정
            ((SearchedPlaceViewHolder) holder).tv_place.setText(searchedPlaceItemArrayList.get(position).getAddress());
            //개수 설정
            ((SearchedPlaceViewHolder) holder).tv_count.setText("게시물 "+searchedPlaceItemArrayList.get(position).getTotalCount()+"개");
            //아이템 클릭 설정
            ((SearchedPlaceViewHolder) holder).container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onListClicked(position);
                }
            });
        }else {

        }
    }

    @Override
    public int getItemCount() {
        return (searchedPlaceItemArrayList == null)?0:searchedPlaceItemArrayList.size();
    }

    //검색 결과 리스트 뷰 홀더
    private class SearchedPlaceViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        CircleImageView cv_image;
        TextView tv_place, tv_count;
        public SearchedPlaceViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            cv_image = view.findViewById(R.id.circleimageview_image);
            tv_place = view.findViewById(R.id.textview_place);
            tv_count = view.findViewById(R.id.textview_count);

        }
    }


    //프로그래스 뷰 홀더(페이징 처리를 할 때 로딩 뷰로 활용 )
    private class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public ProgressViewHolder(View view) {
            super(view);

            progressBar = view.findViewById(R.id.progressbar);
        }
    }
}
