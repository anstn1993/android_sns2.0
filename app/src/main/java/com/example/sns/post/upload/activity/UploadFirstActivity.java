package com.example.sns.post.upload.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sns.util.ItemTouchHelperCallback;
import com.example.sns.R;
import com.example.sns.post.upload.adapter.UploadImageAdapter;
import com.example.sns.post.upload.model.UploadImageItem;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;


import java.util.ArrayList;
import java.util.List;

import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;


public class UploadFirstActivity extends AppCompatActivity implements UploadImageAdapter.UploadImageRecyclerViewClickListener {

    Button btn_cancel, btn_next;
    TextView tv_alert;
    ImageView  iv_detail;
    RecyclerView rv_image;
    LinearLayoutManager layoutManager;
    UploadImageAdapter uploadImageAdapter;
    ItemTouchHelper mItemTouchHelper;
    //아이템을 담을 arraylist정의
    public ArrayList<UploadImageItem> uploadImageItemArrayList = new ArrayList<>();

    private List<Uri> selectedUriList;

    //어떤 이미지가 선택되어있는 상태인지 확인하기 위한 인덱스 변수
    public String selectedImage=null;

    //이미지의 수와 이미지 uri를 뷰페이저 어댑터로 넘겨주기 위한 static arraylist
    public static ArrayList<String> imageArrayList = new ArrayList<>();

    public static Activity uploadFirstActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        Log.d("onCreate", "호출");

        uploadFirstActivity = UploadFirstActivity.this;


        iv_detail = findViewById(R.id.imageview_detail);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_next = findViewById(R.id.btn_next);

        tv_alert = findViewById(R.id.textview_alert);

        setRecyclerView();

        //사용자에게 갤러리와 카메라 접근 허용을 요청하는 메소드
        tedPermission();

        //다음 버튼 클릭 리스너
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //static이기 때문에 기존의 데이터가 누적되지 않게 먼저 비워준 후 다시 넣어준다.
                imageArrayList.clear();
                //사진이 한 장 이상일 때
                if(uploadImageItemArrayList.size()!=0){
                    for(int i = 0; i < uploadImageItemArrayList.size(); i++){
                        imageArrayList.add(uploadImageItemArrayList.get(i).imageSource);
                    }
                    Intent intent = new Intent(getApplicationContext(), UploadSecondActivity.class);
                    startActivity(intent);
                }
                //사진이 한 장도 없을 때
                else {
                    Toast.makeText(getApplicationContext(), "이미지를 선택해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });



        //취소버튼 클릭 리스너
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //선택한 이미지를 담는 리스트를 비워주고
                imageArrayList.clear();
                //액티비티 종료
                finish();
                return;
            }
        });
    }

    //tedPermission라이브러리는 권한요청을 편리하게 할 수 있는 api
    private void tedPermission(){

        //권한요청이 일어나는 순간을 캐치하는 리스터 객체
        PermissionListener permissionListener=new PermissionListener() {
            //권한이 허용됐을 때 실행될 함수
            @Override
            public void onPermissionGranted() {

            }

            //권한이 허용되지 않았을 때 실행될 함수
            //파라미터는 허용되지 않은 권한을 담는 리스트가 리턴되어 들어간다.
            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }

        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("[설정]-[권한]에서 권한을 허용할 수 있습니다.")
                .setDeniedMessage("사진 및 파일을 저장하기 위해서는 접근 권한이 필요합니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();


    }

    private void setRecyclerView(){

        //리사이클러뷰 연결
        rv_image=findViewById(R.id.recyclerview);
        rv_image.setHasFixedSize(true);

        //그리드 레이아웃 메니저로 초기화, 한 row에 최대 6개 아이템 삽입
        layoutManager = new LinearLayoutManager(this);
        //가로형 리사이클러뷰로 만들기 위해서 orientation을 수평으로
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        //리사이클러뷰에 레이아웃 매니저를 설정
        rv_image.setLayoutManager(layoutManager);



        //어탭터를 현재의 arraylist를 담은 상태로 초기화
        uploadImageAdapter = new UploadImageAdapter(uploadImageItemArrayList, this);
        //어댑터에 클릭 리스너를 설정해서 뷰홀더 클릭 이벤트가 가능하게 설정
        uploadImageAdapter.setOnClickListener(UploadFirstActivity.this);

        //드래그앤 드롭을 하기 위한 클래스 선언(리스너는 리사이클러뷰의 어댑터가 된다.)
        ItemTouchHelperCallback mCallBack = new ItemTouchHelperCallback(uploadImageAdapter);
        //ItemTouchHelper클래스 객체를 생성
        mItemTouchHelper = new ItemTouchHelper(mCallBack);
        mItemTouchHelper.attachToRecyclerView(rv_image);

        //리사이클러뷰에 어댑터 설정
        rv_image.setAdapter(uploadImageAdapter);


        uploadImageItemArrayList.clear();

        //선택한 사진의 uri를 인텐트에서 받아와서 아이템 객체에 넣고 그 객체를 arraylist에 추가
        Intent intent = getIntent();
        int imageCount = intent.getIntExtra("imageCount",0);
        for(int i=0; i<imageCount; i++){
            Log.d("넘어온 uri", intent.getStringExtra("image"+(i+1)));
            uploadImageItemArrayList.add(new UploadImageItem(
              intent.getStringExtra("image"+(i+1)), R.drawable.delete,"uri"
            ));

        }

        //어댑터에 arraylist가 들어왔음을 알려줘서 결과를 반영하게 함
        uploadImageAdapter.notifyDataSetChanged();

        for(int i = 0; i < uploadImageItemArrayList.size(); i++){
            Log.d("바뀌기 전 arraylist", uploadImageItemArrayList.get(i).imageSource);
        }

        Glide.get(this).clearMemory();
        Glide.with(this)
                .load(uploadImageItemArrayList.get(0).imageSource)
                .apply(new RequestOptions().centerCrop())
                .into(iv_detail);

        //선택된 이미지는 가장 첫번째 이미지(default)
        selectedImage = uploadImageItemArrayList.get(0).imageSource;



    }

    //이미지를 클릭할 시 호출되는 메소드
    @Override
    public void onImageClicked(int position) {

//        Glide.get(this).clearMemory();
        Glide.with(this)
                .load(Uri.parse(uploadImageItemArrayList.get(position).imageSource))
                .apply(new RequestOptions().centerCrop())
                .into(iv_detail);
        Log.d("클릭된 이미지 uri", uploadImageItemArrayList.get(position).imageSource);
        Log.d("클릭된 position", String.valueOf(position));
        selectedImage = uploadImageItemArrayList.get(position).imageSource;
    }

    //이미지 삭제 버튼을 클릭할 시 호출되는 메소드
    @Override
    public void onRemoveButtonClicked(int position) {
        Log.d("지우려는 사진 index", String.valueOf(position));

        //현재 선택되어있는 사진을 지우려는 경우
        if(uploadImageItemArrayList.get(position).imageSource== selectedImage){
            //근데 그 사진이 첫번째 사진인 경우
            if(position == 0){
                //사진이 2장 이상 남아있는 경우
                if(uploadImageItemArrayList.size()>1){
                    //첫번째 사진 다음 사진을 보여준다.
                    Glide.with(this)
                            .load(Uri.parse(uploadImageItemArrayList.get(position+1).imageSource))
                            .apply(new RequestOptions().centerCrop())
                            .into(iv_detail);
                    selectedImage = uploadImageItemArrayList.get(position+1).imageSource;


                }
                //사진이 한장만 남았다면
                else {
                    iv_detail.setImageResource(0);
                    tv_alert.setVisibility(View.VISIBLE);
                    selectedImage = null;
                }
            }
            //사진이 첫번째 이후의 사진인 경우
            else{
                //그 사진 이전의 사진을 보여준다.
                Glide.with(this)
                        .load(Uri.parse(uploadImageItemArrayList.get(position-1).imageSource))
                        .apply(new RequestOptions().centerCrop())
                        .into(iv_detail);
                //선택된 사진의 인덱스를 반영해준다.
                selectedImage = uploadImageItemArrayList.get(position-1).imageSource;
            }

        }
        //현재 선택되어있지 않은 사진을 지우려는 경우
        else {
            //예외적인 경우인데 1번 사진을 선택하고 0번 사진을 지우면 이후에 0번 사진을 지울 때 selectedImage=1이고
            //지우는 사진의 index는 0이기 때문에 위의 position=selectedImage가 true인 조건문을 타지 못한다.
            //그래서 이 경우 selectedImage=0으로 설정해서 위의 조건문을 탈 수 있게 해준다.
//            selectedImage = uploadImageItemArrayList.get(0).imageUri;
        }


        Log.d("삭제버튼 클릭", "yes");
        //arraylist에서 해당 인덱스의 아이템 객체를 지워주고
        uploadImageItemArrayList.remove(position);
        //어댑터에 알린다.
        uploadImageAdapter.notifyItemRemoved(position);
        //이 notify도 반드시 해줘야 한다. 해주지 않으면 index오류
        uploadImageAdapter.notifyDataSetChanged();
    }

    //이미지 추가버튼 클릭시 호출되는 메소드
    @Override
    public void onAddImageClicked(int position) {
        Log.d("이미지버튼 index", String.valueOf(position));

        //이미지가 추가되기 전에 이미지 버튼의 index
        int oldPosition = position;

        //사진이 6장 선택된 경우
        if(uploadImageItemArrayList.size()==6){
            Toast.makeText(getApplicationContext(), "더 이상 사진을 선택할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
        //사진이 6장이 안 되는 경우
        else {
            //tedbottompicker라이브러리
            TedBottomPicker.with(UploadFirstActivity.this)
                    //이미지 피커액티비티가 올라오는 높이 설정
                    .setPeekHeight(2000)
                    .showTitle(false)
                    .setCompleteButtonText("완료")
                    .setEmptySelectionText("이미지 없음")
                    .setSelectedUriList(selectedUriList)
                    .setPreviewMaxCount(1000)
                    .setSelectMaxCount(6-uploadImageItemArrayList.size())
                    .setSelectMinCount(1)
                    .setEmptySelectionText("이미지를 선택해주세요.")
                    .showMultiImage(new TedBottomSheetDialogFragment.OnMultiImageSelectedListener() {
                        @Override
                        public void onImagesSelected(List<Uri> uriList) {
                            // here is selected image uri list
                            //선택한 이미지 수만큼 arraylist에 넣어준다.
                            for(int i=0; i<uriList.size(); i++){
                                uploadImageItemArrayList.add(new UploadImageItem(
                                    uriList.get(i).toString(), R.drawable.delete, "uri"
                                ));
                            }

                            uploadImageAdapter.notifyDataSetChanged();

                            //사진이 0장에서 추가됐을 때의 동작을 캐치하기 위해서 다음의 조건문을 작성한다.
                            if(oldPosition==0 && uploadImageItemArrayList.size()!=0){
                                //이미지 선택 경고 메세지를 지워주고
                                tv_alert.setVisibility(View.INVISIBLE);
                                //첫번째 사진을 보여준다.
                                Glide.with(getApplicationContext())
                                        .load(Uri.parse(uploadImageItemArrayList.get(0).imageSource))
                                        .apply(new RequestOptions().centerCrop())
                                        .into(iv_detail);

                                selectedImage = uploadImageItemArrayList.get(0).imageSource;
                            }
                        }
                    });
        }





    }

    //드래그가 시작되면 호출되는 메소드
    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder, int toPosition) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    //뒤로가기 버튼을 눌렀을 때 콜백
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //선택한 이미지를 담는 리스트를 비워준다.
        imageArrayList.clear();
    }
}
