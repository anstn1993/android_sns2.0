package com.example.sns;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.sns.LoginActivity.account;

public class CommentActivity extends AppCompatActivity implements CommentAdapter.CommentRecyclerViewListener, ChildCommentAdapter.ChildCommentRecyclerViewListener, HttpRequest.OnHttpResponseListener {

    String TAG = "CommentActivity";
    //뒤로가기 버튼
    ImageButton im_back;

    //댓글 다는 사람의 프로필 사진
    CircleImageView cv_profile;

    //댓글 입력 창
    EditText et_comment;

    //댓글 게시 버튼, 최상단의 댓글 버튼(누르면 리사이클러뷰 최상단으로)
    TextView tv_upload, tv_comment;

    //최상단으로 버튼
    Button bt_tothetop;

    //새로고침 레이아웃
    SwipeRefreshLayout swipeRefreshLayout;

    //댓글 리사이클러뷰
    RecyclerView rv_comment;
    //리사이클러뷰 레이아웃 매니저
    LinearLayoutManager linearLayoutManager;
    //리사이클러뷰 어댑터
    CommentAdapter commentAdapter;


    //리사이클러뷰 데이터 arraylist
    ArrayList<CommentItem> commentItemArrayList;

    //댓글 리사이클러뷰의 헤더 데이터
    int postNum;
    String postProfile = null;
    String postAccount = null;
    String postArticle = null;
    String postNickname = null;
    String postTime = null;

    //댓글 수정 상태 캐치를 위한 boolean
    private boolean isEditting = false;
    //댓글 수정 완료 상태를 캐치하기 위한 boolean
    private boolean isEditted = false;

    //답글 달기 상태를 캐치하기 위한 boolean
    private boolean isChildcommenting = false;
    //답글 달기 완료 상태를 캐치하기 위한 boolean
    private boolean isChildcommented = false;

    //답글 수정 상태 캐치를 위한 boolean
    private boolean isChildCommentEditting = false;
    //답글 수정 완료 상태를 캐치하기 위한 boolean
    private boolean isChildCommentEditted = false;

    //수정된 댓글의 index
    private int edittedPosition;

    //답글의 부모 댓글의 index
    private int addedChildCommentPosition;

    //수정된 답글의 index
    private int edittedChildCommentPosition;

    //키보드 제어를 위한 객체
    SoftKeyboard softKeyboard;

    ConstraintLayout constraintLayout;


    //현재 로드된 댓글의 개수
    public int currentCommentCount = 0;

    //전체 댓글의 수
    public int totalCommentCount;

    //댓글이 달린 후의 전체 댓글 수
    public int currentTotalCommentCount;

    //댓글을 지운 후에 페이징이 가능하게 하기 위해서 댓글의 삭제 여부를 판단하는 변수
    public boolean isRemoved = false;

    //완전히 댓글 로드가 끝났을 때를 체크하기 위한 boolean
    public boolean isCompletelyLoaded = false;

    public boolean loadPossible = true;

    //업로드한 댓글의 아이디를 저장할 arraylist
    public ArrayList<Integer> uploadedCommentNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        //액티비티의 가장 부모 레이아웃 객체
        constraintLayout = findViewById(R.id.constraintlayout_comment);

        //키보드 제어 객체 선언
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        softKeyboard = new SoftKeyboard(constraintLayout, inputMethodManager);

        //업로드한 댓글의 아이디를 저장할 arraylist 초기화
        uploadedCommentNum = new ArrayList<>();

        im_back = findViewById(R.id.imagebutton_back);
        cv_profile = findViewById(R.id.circleimageview_profile);
        et_comment = findViewById(R.id.edittext_comment);
        tv_upload = findViewById(R.id.textview_upload);
        tv_comment = findViewById(R.id.textview_comment);
        bt_tothetop = findViewById(R.id.button_tothetop);

        //새로고침
        swipeRefreshLayout = findViewById(R.id.refresh_layout);

        //새로고침 리스너
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //현재 로드된 댓글 수 초기화
                currentCommentCount = 0;
                //총 댓글 수 초기화
                totalCommentCount = 0;
                currentTotalCommentCount = 0;
                //마지막 댓글이 로드됐는지 가리는 boolean 초기화
                isCompletelyLoaded = false;
                isRemoved = false;
                //내가 업로드한 댓글의 id를 담는 arraylist를 초기화 해준다.
                uploadedCommentNum.clear();
                //내가 업로드한 대댓글의 id를 담는 arraylist를 초기화
                for (int i = 0; i < commentItemArrayList.size(); i++) {
                    commentItemArrayList.get(i).uploadedChildCommentChildNum.clear();
                }
                //댓글 데이터를 가져오는 스레드
                getComment();

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //뒤로 가기 버튼 클릭 리스너
        im_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });

        //댓글 입력버튼 클릭 리스너
        tv_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //댓글을 새롭게 다는 경우
                if (tv_upload.getText().toString().equals("게시") && et_comment.getHint().toString().equals("댓글 달기...")) {

                    //댓글 입력창이 비어있는 경우
                    if (et_comment.getText().toString().trim().equals("")) {
                        Toast toast = Toast.makeText(getApplicationContext(), "댓글을 입력해주세요", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                    //댓글 입력창에 글이 입력된 경우
                    else {
                        String comment = et_comment.getText().toString();

                        addComment(comment);//댓글 데이터를 서버로 보내는 스레드
                        et_comment.setText("");  //댓글이 서버에 성공적으로 넘어가면 댓글 입력창을 비워준다.
                        //키보드를 내려준다.
                        softKeyboard.closeSoftKeyboard();
                        //댓글 입력창에서 포커스를 없애준다.
                        et_comment.clearFocus();
                    }


                }
                //댓글을 수정하는 경우
                else if (tv_upload.getText().toString().equals("수정") && et_comment.getHint().toString().equals("댓글 수정...")) {

                    //댓글 입력창이 비어있는 경우
                    if (et_comment.getText().toString().trim().equals("")) {
                        Toast toast = Toast.makeText(getApplicationContext(), "댓글을 입력해주세요", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                    //댓글 입력창에 글이 입력된 경우
                    else {
                        //댓글 수정이 완료됐기 때문에 다시 false
                        isEditting = false;
                        String comment = et_comment.getText().toString();
                        int commentNum = commentItemArrayList.get(edittedPosition).id;
                        editComment(comment, commentNum);//수정된 댓글 데이터를 서버로 보내는 스레드
                    }
                }
                //답글을 다는 경우
                else if (tv_upload.getText().toString().equals("게시") && et_comment.getHint().toString().equals("답글 달기...")) {

                    //답글 입력창이 비어있는 경우
                    if (et_comment.getText().toString().trim().equals("")) {
                        Toast toast = Toast.makeText(getApplicationContext(), "답글을 입력해주세요", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                    //답글 입력창에 글이 입력된 경우
                    else {
                        //답글 등록이 완료됐기 때문에 다시 false처리
                        isChildcommenting = false;

                        //서버로 넘겨줄 데이터 셋
                        int postNum = commentItemArrayList.get(addedChildCommentPosition).getPostNum();
                        int commentNum = commentItemArrayList.get(addedChildCommentPosition).getId();
                        String childComment = et_comment.getText().toString();

                        addChildComment(account, postNum, commentNum, childComment);//대댓글 추가 메소드


                    }


                }
                //답글을 수정하는 경우
                else {

                    //댓글 입력창이 비어있는 경우
                    if (et_comment.getText().toString().trim().equals("")) {
                        Toast toast = Toast.makeText(getApplicationContext(), "답글을 입력해주세요", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                    //댓글 입력창에 글이 입력된 경우
                    else {
                        //답글 수정이 완료됐기 때문에 다시 false처리
                        isChildCommentEditting = false;

                        //서버로 넘겨줄 데이터 셋
                        int childCommentNum = commentItemArrayList.get(edittedPosition).childCommentList.get(edittedChildCommentPosition).getChildCommentNum();
                        String edittedChildComment = et_comment.getText().toString();
                        editChildComment(childCommentNum, edittedChildComment);//대댓글 수정 메소드
                    }


                }

            }
        });

        //키보드 콜백 메소드
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            //키보드가 사라지는 순간 콜백
            @Override
            public void onSoftKeyboardHide() {
                Log.d("키보드", "사라짐");

                //댓글 수정 과정에서 수정을 완료하지 않고 취소하여 키보드가 내려갈 때
                if (isEditting == true && isEditted == false) {
                    //ui를 변경하기 위한 스레드 실행
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //댓글 수정 상태는 false가 되고
                            isEditting = false;
                            //댓글 입력창의 hint는 다시 댓글 달기...으로 돌아간다.
                            et_comment.setHint("댓글 달기...");
                            //댓글 입력창을 비워준다.
                            et_comment.setText("");
                            //그리고 댓글 입력 버튼을 수정에서 게시로 전환
                            tv_upload.setText("게시");
                            //댓글 입력창의 포커스를 없앤다.
                            et_comment.clearFocus();
                        }
                    });

                } else if (isEditting == false && isEditted == true) {
                    //ui를 변경하기 위한 스레드 실행
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //댓글 수정이 완료됐기 때문에 다시 false로 돌린다.
                            isEditted = false;
                            //댓글 입력창의 hint는 다시 댓글 달기...으로 돌아간다.
                            et_comment.setHint("댓글 달기...");
                            //댓글 입력창을 비워준다.
                            et_comment.setText("");
                            //그리고 댓글 입력 버튼을 수정에서 게시로 전환
                            tv_upload.setText("게시");
                            //댓글 입력창의 포커스를 없앤다.
                            et_comment.clearFocus();
                        }
                    });
                }

                //답글 등록 과정에서 등록을 완료하지 않고 취소하여 키보드가 내려갈 때
                if (isChildcommenting == true && isChildcommented == false) {
                    //ui를 변경하기 위한 스레드 실행
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //답글 등록 상태는 false가 되고
                            isChildcommenting = false;
                            //댓글 입력창의 hint는 다시 댓글 달기...으로 돌아간다.
                            et_comment.setHint("댓글 달기...");
                            //댓글 입력창을 비워준다.
                            et_comment.setText("");
                            //댓글 입력창의 포커스를 없앤다.
                            et_comment.clearFocus();
                        }
                    });
                }
                //답글 등록 과정에서 등록을 완료하고 키보드가 내려갈 때
                else if (isChildcommenting == false && isChildcommented == true) {
                    //ui를 변경하기 위한 스레드 실행
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //답글 등록이 완료됐기 때문에 다시 false로 돌린다.
                            isChildcommented = false;
                            //댓글 입력창의 hint는 다시 댓글 달기...으로 돌아간다.
                            et_comment.setHint("댓글 달기...");
                            //댓글 입력창을 비워준다.
                            et_comment.setText("");
                            //댓글 입력창의 포커스를 없앤다.
                            et_comment.clearFocus();
                        }
                    });
                }

                //답글 수정 과정에서 수정을 완료하지 않고 키보드가 내려갈 때
                if (isChildCommentEditting == true && isChildCommentEditted == false) {

                    //ui를 변경하기 위한 스레드 실행
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //답글 입력중 상태를 false로
                            isChildCommentEditting = false;
                            //답글 입력창의 hint는 다시 댓글 달기...으로 돌아간다.
                            et_comment.setHint("댓글 달기...");
                            //답글 입력창을 비워준다.
                            et_comment.setText("");
                            //그리고 댓글 입력 버튼을 수정에서 게시로 전환
                            tv_upload.setText("게시");
                            //댓글 입력창의 포커스를 없앤다.
                            et_comment.clearFocus();
                        }
                    });

                }
                //답글 수정을 완료하고 키보드가 내려갈 때
                else if (isChildCommentEditting == false && isChildCommentEditted == true) {
                    //ui를 변경하기 위한 스레드 실행
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //답글 수정이 완료됐기 때문에 다시 false로 돌린다.
                            isChildCommentEditted = false;
                            //댓글 입력창의 hint는 다시 댓글 달기...으로 돌아간다.
                            et_comment.setHint("댓글 달기...");
                            //댓글 입력창을 비워준다.
                            et_comment.setText("");
                            //그리고 댓글 입력 버튼을 수정에서 게시로 전환
                            tv_upload.setText("게시");
                            //댓글 입력창의 포커스를 없앤다.
                            et_comment.clearFocus();
                        }
                    });
                }
            }

            //키보드가 나타나는 순간 콜백
            @Override
            public void onSoftKeyboardShow() {
                Log.d("키보드", "올라옴");
            }
        });


        //사용자 프로필 사진 설정
        Glide.with(getApplicationContext()).load("http://13.124.105.47/profileimage/" + LoginActivity.profile)
                .thumbnail(0.1f)
                .apply(new RequestOptions().centerCrop().placeholder(R.drawable.profile).error(R.drawable.profile))
                .into(cv_profile);

        //댓글 리사이클러뷰 세팅
        setRecyclerView();


        //addOnScrollListener는 리사이클러뷰를 스크롤하는 순간을 캐치한다.
        rv_comment.addOnScrollListener(new RecyclerView.OnScrollListener() {
            //리사이클러뷰가 스크롤된 후 콜백되는 메소드
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalCount = recyclerView.getAdapter().getItemCount();//리사이클러뷰의 전체 아이템 개수.
                int lastCompletelyVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();

                if (currentCommentCount != 0) {
                    if (lastCompletelyVisibleItemPosition == totalCount - 1 && loadPossible == true) {//완전히 보이는 마지막 아이템 인덱스가 전체 아이템의 마지막 인덱스인 경우
                        if (totalCommentCount >= 10) {//전체 댓글 개수가 10개 이상일 때만 페이징
                            Log.d("페이징 조건", "부합");
                            //param:현재 가장 마지막 댓글의 id
                            int lastCommentId = commentItemArrayList.get(commentItemArrayList.size() - 1).id;
                            loadNextPage(lastCommentId);
                        }
                    }
                }


            }
        });

        //화면 상단의 댓글 클릭 리스너
        tv_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rv_comment.smoothScrollToPosition(0);
            }
        });
        //화면 상단 클릭 리스너
        bt_tothetop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rv_comment.smoothScrollToPosition(0);
            }
        });


    }//end of onCreate


    //댓글 데이터를 서버로부터 가져오는 메소드
    private void getComment() {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("requestType", "getComment");
            requestBody.put("lastCommentId", 0);
            requestBody.put("postNum", postNum);
            requestBody.put("account", account);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getcomment.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //댓글 리스트의 최하단으로 이동하면 페이징 처리를 위해서 실행될 메소드
    public void loadNextPage(int lastCommentId) {
        loadPossible = false;
        //null을 넣어서 뷰타입이 프로그래스 아이템이 추가되게 한다.
        commentItemArrayList.add(null);
        commentAdapter.notifyItemInserted(commentItemArrayList.size());
        //핸들러를 통해서 1.5초 뒤에 해당 기능을 실행한다.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //1.5초가 지난 후에 프로그래스 아이템을 교체해준다.

                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("requestType", "loadNextPage");
                    requestBody.put("lastCommentId", lastCommentId);
                    requestBody.put("postNum", postNum);
                    requestBody.put("account", account);
                    HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getcomment.php", CommentActivity.this);
                    httpRequest.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, 800);

    }

    //댓글을 작성하면 서버로 데이터를 전송하여 db에 저장하는 메소드
    private void addComment(String comment) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("postNum", postNum);
            requestBody.put("account", account);
            requestBody.put("comment", comment);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "addcomment.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //댓글을 수정하면 서버로 데이터를 전송하여 db에 업데이트하는 메소드
    private void editComment(String comment, int commentNum) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("comment", comment);//수정할 댓글
            requestBody.put("commentNum", commentNum);//수정할 댓글 id
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "editcomment.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //댓글을 삭제하면 서버로 데이터를 전송하여 db에서 삭제하는 메소드
    private void deleteComment(int commentNum, int position) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("commentNum", commentNum);//삭제할 댓글 id
            requestBody.put("position", position);//수정할 댓글의 리사이클러뷰에서의 index
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "deletecomment.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //대댓글을 작성하면 서버로 데이터를 정송하여 db에 추가하는 메소드
    private void addChildComment(String account, int postNum, int commentNum, String childComment) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("account", account);
            requestBody.put("postNum", postNum);
            requestBody.put("commentNum", commentNum);
            requestBody.put("childComment", childComment);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "addchildcomment.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //대댓글을 수정하면 서버로 데이터를 전송하여 db에 업데이트하는 메소드
    private void editChildComment(int childCommentNum, String edittedChildComment) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("childCommentNum", childCommentNum);
            requestBody.put("edittedChildComment", edittedChildComment);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "editchildcomment.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //대댓글을 삭제하면 서버로 데이터를 전송하여 db에서 삭제하는 메소드
    private void deleteChildComment(int childCommentNum, int childCommentPosition, int commentPosition) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("childCommentNum", childCommentNum);
            requestBody.put("childCommentPosition", childCommentPosition);
            requestBody.put("commentPosition", commentPosition);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "deletechildcomment.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //대댓글 페이징을 하면 서버로 데이터를 전송하여 다음 대댓글 데이터를 가져오는 메소드
    private void loadNextChildComment(String account, int lastChildCommentId, int postNum, int commentNum, int parentPosition) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("account", account);
            requestBody.put("lastChildCommentId", lastChildCommentId);
            requestBody.put("postNum", postNum);
            requestBody.put("commentNum", commentNum);
            requestBody.put("parentPosition", parentPosition);
            HttpRequest httpRequest = new HttpRequest("GET", requestBody.toString(), "getchildcomment.php", this);
            httpRequest.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //댓글을 단 경우 fcm을 통해서 댓글이 달린 사용자에게 알림을 전달하기 위한 메소드
    private void pushNotification(String receiver, String title, String body, String click_action, String category, String sender, int postNum, int commentNum) {
        try {
            Log.d(TAG, "pushNotification 메소드 호출");
            JSONObject requestBody = new JSONObject();
            requestBody.put("account", receiver);//알림의 수신자
            requestBody.put("title", title);//알림의 제목
            requestBody.put("body", body);//알림의 내용
            requestBody.put("click_action", click_action);//푸시알림을 눌렀을 때 이동할 액티비티 혹은 프래그먼트
            requestBody.put("category", category);//알림의 카테고리
            requestBody.put("userAccount", sender);//알림 송신자
            requestBody.put("postNum", postNum);
            requestBody.put("commentNum", commentNum);
            HttpRequest httpRequest = new HttpRequest("POST", requestBody.toString(), "pushnotification.php", this);
            httpRequest.execute();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSON ERROR:" + e.getMessage());
        }

    }


    //서버와 통신에 성공했을 때 호출되는 콜백 메소드(HttpRequest클래스(AsyncTask클래스 상속)의 onPostExecute()메소드에서 호출)
    //모든 통신에 대한 결과는 이 콜백 메소드에서 처리해준다.
    @Override
    public void onHttpResponse(String result) {
        Log.d(TAG, "통신 성공");
        Log.d(TAG, "서버에서 넘어온 json데이터- " + result);
        try {
            JSONObject responseBody = new JSONObject(result);
            String requestType = responseBody.getString("requestType");//서버통신 요청 타입

            if (result != null) {
                if (requestType.equals("addComment")) {
                    //서버로부터 넘어온 json 배열의 값을 가져와서 댓글 데이터로 셋 해준다.
                    JSONObject commentData = responseBody.getJSONObject("addedComment");
                    CommentItem commentItem = new Gson().fromJson(commentData.toString(), CommentItem.class);

                    //댓글이 10개가 넘어서 페이징이 적용된 댓글 화면인 경우
                    if (totalCommentCount > 10) {
                        //댓글 어레이리스트에 데이터 셋 추가
                        //현재 로드된 댓글 수와 전체 댓글 수가 같은 경우(댓글이 모두 로드된 경우)
                        if (isCompletelyLoaded == true) {
                            commentItemArrayList.add(commentItem);
                            commentAdapter.notifyItemInserted(commentItemArrayList.size());
                            //댓글 리스트의 최하단으로 이동
                            rv_comment.smoothScrollToPosition(commentItemArrayList.size());

                        }
                        //현재 로드된 댓글 수와 전체 댓글 수가 다른 경우(댓글이 모두 로드되지 않은 경우)
                        else {
                            commentItemArrayList.add(0, commentItem);
                            //어댑터에 notify
                            commentAdapter.notifyItemInserted(1);
                            commentAdapter.notifyItemRangeChanged(1, commentItemArrayList.size());

                            //추가한 댓글의 id를 arraylist에 저장
                            uploadedCommentNum.add(0, commentItem.getId());
                            Log.d("추가된 댓글의 id", String.valueOf(uploadedCommentNum.get(0)));

                            //댓글 리스트의 최상단으로 이동
                            rv_comment.smoothScrollToPosition(0);
                        }

                    }
                    //댓글이 10개 이하라서 페이징이 적용되지 않은 댓글 화면인 경우
                    else {
                        commentItemArrayList.add(commentItem);
                        commentAdapter.notifyItemInserted(commentItemArrayList.size());
                        //댓글 리스트의 최하단으로 이동
                        rv_comment.smoothScrollToPosition(commentItemArrayList.size());
                    }
                    //현재 로드된 댓글 수 +1
                    currentCommentCount += 1;

                    //현재 댓글의 총 개수 +1
                    currentTotalCommentCount += 1;

                    //키보드 내려준다.
                    softKeyboard.closeSoftKeyboard();

                    //자신의 게시물에 댓글을 다는 경우는 알림이 가지 않는다.
                    if (!postAccount.equals(LoginActivity.account)) {
                        String receiver = postAccount;
                        String title = "SNS";
                        String body = LoginActivity.nickname + "님이 회원님의 게시물에 댓글을 남겼습니다:" + "\"" + commentItemArrayList.get(commentItemArrayList.size() - 1).comment + "\"";
                        String click_action = "CommentActivity";
                        String category = "comment";
                        String sender = LoginActivity.account;
                        pushNotification(receiver, title, body, click_action, category, sender, postNum, commentItemArrayList.get(commentItemArrayList.size() - 1).getId());
                    }

                } else if (requestType.equals("getComment")) {//댓글 데이터를 가져오는 통신을 한 경우
                    //댓글 리스트를 전부 비워주고
                    commentItemArrayList.clear();
                    //현재 전체 댓글 수를 담는다.
                    totalCommentCount = responseBody.getInt("totalCommentCount");
                    Log.d("초기의 총 댓글 수", String.valueOf(totalCommentCount));
                    currentTotalCommentCount = totalCommentCount;
                    Log.d("현재 총 댓글의 수", String.valueOf(currentTotalCommentCount));
                    //comment키값에 들어있는 배열을 jsonArray에 넣어준다.
                    JSONArray jsonArray = responseBody.getJSONArray("comment");
                    //댓글 수만큼 반복문을 돌리면서 각 댓글에 데이터를 넣어준다.
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);

                        //댓글 아이템 객체에 데이터를 셋하고
                        CommentItem commentItem = new Gson().fromJson(data.toString(), CommentItem.class);

                        //어레이 리스트에 넣어준다.
                        commentItemArrayList.add(commentItem);

                        if (commentItem.getChildCommentCount() > 3) {//전체 대댓글의 수가 3개를 초과하면 더 보기 버튼을 추가하여 페이징이 가능하게 해준다.
                            commentItemArrayList.get(i).childCommentList.add(null);
                        }
                        //현재 로드된 댓글 수 +1
                        currentCommentCount += 1;
                    }
                    Log.d("현재 로드된 댓글 수", String.valueOf(commentItemArrayList.size()));
                    //모든 데이터 셋이 끝나면 어뎁터에 notify
                    commentAdapter.notifyDataSetChanged();

                } else if (requestType.equals("loadNextPage")) {//댓글 페이징 통신
                    try {
                        //comment키값에 들어있는 배열을 jsonArray에 넣어준다.
                        JSONArray jsonArray = responseBody.getJSONArray("comment");
                        //댓글 수만큼 반복문을 돌리면서 각 댓글에 데이터를 넣어준다.

                        boolean isSubstituted = true;
                        if (jsonArray.length() != 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject data = jsonArray.getJSONObject(i);

                                //댓글 아이템 객체에 데이터를 셋하고
                                CommentItem commentItem = new Gson().fromJson(data.toString(), CommentItem.class);

                                //로딩바를 다음 페이지의 첫번째 댓글로 교체하기 위한 작업
                                if (isSubstituted) {

                                    commentItemArrayList.set(commentItemArrayList.size() - 1, commentItem);
                                    commentAdapter.notifyItemChanged(commentItemArrayList.size());
                                    //댓글이 모두 로드되기 전에 댓글을 달아서 최상단에 최신 댓글이 있을 경우 로드되다가 내 댓글이 다시 나오면 최상단의 댓글은 지워준다.
                                    for (int k = 0; k < uploadedCommentNum.size(); k++) {
                                        if (commentItem.getId() == uploadedCommentNum.get(k)) {
                                            Log.d("업로드한 댓글의 id", String.valueOf(uploadedCommentNum.get(k)));
                                            Log.d("업로드한 댓글의 id", String.valueOf(commentItemArrayList.get(uploadedCommentNum.indexOf(uploadedCommentNum.get(k))).getId()));
                                            commentItemArrayList.remove(uploadedCommentNum.indexOf(uploadedCommentNum.get(k)));
                                            Log.d("업로드한 댓글의 position", String.valueOf(uploadedCommentNum.indexOf(uploadedCommentNum.get(k))));
                                            commentAdapter.notifyItemRemoved(uploadedCommentNum.indexOf(uploadedCommentNum.get(k)) + 1);
                                            commentAdapter.notifyItemRangeChanged(uploadedCommentNum.indexOf(uploadedCommentNum.get(k)) + 1, commentItemArrayList.size() - 1);
                                            uploadedCommentNum.remove(uploadedCommentNum.indexOf(uploadedCommentNum.get(k)));
                                            currentCommentCount -= 1;
                                        }
                                    }
                                    isSubstituted = false;
                                }
                                //최초에 로딩바와 게시물이 교체가 끝났으면 그 뒤로는 게시물 추가만 해준다.
                                else {
                                    //어레이 리스트에 넣어준다.
                                    commentItemArrayList.add(commentItem);

                                    if (commentItem.getChildCommentCount() > 3) {//전체 대댓글의 수가 3개를 초과하면 더 보기 버튼을 추가하여 페이징이 가능하게 해준다.
                                        commentItemArrayList.get(i).childCommentList.add(null);
                                    }
                                    commentAdapter.notifyItemInserted(commentItemArrayList.size());

                                    //댓글이 모두 로드되기 전에 댓글을 달아서 최상단에 최신 댓글이 있을 경우 로드되다가 내 댓글이 다시 나오면 최상단의 댓글은 지워준다.
                                    for (int k = 0; k < uploadedCommentNum.size(); k++) {
                                        if (commentItem.getId() == uploadedCommentNum.get(k)) {
                                            Log.d("업로드한 댓글의 id", String.valueOf(uploadedCommentNum.get(k)));
                                            Log.d("업로드한 댓글의 id", String.valueOf(commentItemArrayList.get(uploadedCommentNum.indexOf(uploadedCommentNum.get(k))).getId()));
                                            commentItemArrayList.remove(uploadedCommentNum.indexOf(uploadedCommentNum.get(k)));
                                            Log.d("업로드한 댓글의 position", String.valueOf(uploadedCommentNum.indexOf(uploadedCommentNum.get(k))));
                                            commentAdapter.notifyItemRemoved(uploadedCommentNum.indexOf(uploadedCommentNum.get(k)) + 1);
                                            commentAdapter.notifyItemRangeChanged(uploadedCommentNum.indexOf(uploadedCommentNum.get(k)) + 1, commentItemArrayList.size() - 1);
                                            uploadedCommentNum.remove(uploadedCommentNum.indexOf(uploadedCommentNum.get(k)));
                                            currentCommentCount -= 1;
                                        }
                                    }
                                }
                                //현재 로드된 댓글 수 +1
                                currentCommentCount += 1;
                            }
                        } else {//댓글 데이터가 더 이상 없는 경우 프로그래스 바만 삭제
                            commentItemArrayList.remove(commentItemArrayList.size() - 1);
                            commentAdapter.notifyItemRemoved(commentItemArrayList.size() + 1);
                            isCompletelyLoaded = true;
                        }

                        loadPossible = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("json오류", e.getMessage());
                        commentItemArrayList.remove(commentItemArrayList.size() - 1);
                        commentAdapter.notifyItemRemoved(commentItemArrayList.size() + 1);
                        isCompletelyLoaded = true;
                        loadPossible = true;
                    }

                } else if (requestType.equals("editComment")) {//댓글 수정 통신
                    //수정이 됐기 때문에 true로 변환해준다.
                    isEditted = true;
                    String comment = responseBody.getString("comment");

                    commentItemArrayList.get(edittedPosition).setComment(comment);

                    //리사이클러뷰 어댑터에 notify
                    commentAdapter.notifyItemChanged(edittedPosition + 1, "editComment");

                    //키보드를 내려준다.
                    softKeyboard.closeSoftKeyboard();
                } else if (requestType.equals("deleteComment")) {//댓글 삭제 통신
                    int commentIndex = responseBody.getInt("position");
                    //이 페이지에서 내가 단 댓글을 다시 지우는 경우 업로드한 댓글의 id arraylist도 함께 반영을 해준다.
                    for (int i = 0; i < uploadedCommentNum.size(); i++) {
                        if (uploadedCommentNum.get(i) == commentItemArrayList.get(commentIndex).getId()) {
                            uploadedCommentNum.remove(i);
                        }
                    }
                    //리사이클러뷰 상에서 댓글을 지워준다.
                    commentItemArrayList.remove(commentIndex);
                    //어댑터에 notify
                    commentAdapter.notifyItemRemoved(commentIndex + 1);
                    commentAdapter.notifyItemRangeChanged(commentIndex + 1, commentItemArrayList.size());
                    //현재 댓글 수
                    currentTotalCommentCount -= 1;
                    //현재 로드된 댓글 수 -1
                    currentCommentCount -= 1;
                    isRemoved = true;
                } else if (requestType.equals("addChildComment")) {//대댓글(답글) 추가 통신
                    //답글이 등록됐기 때문에 상태 true로 변환
                    isChildcommented = true;
                    JSONObject commentData = responseBody.getJSONObject("addedComment");
                    ChildCommentItem childCommentItem = new Gson().fromJson(commentData.toString(), ChildCommentItem.class);

                    //대댓글이 전부 다 로드된 경우
                    if (commentItemArrayList.get(addedChildCommentPosition).getChildCommentCount() == commentItemArrayList.get(addedChildCommentPosition).childCommentList.size()) {
                        //대댓글 아이템을 제일 하단에 추가한 후
                        commentItemArrayList.get(addedChildCommentPosition).childCommentList.add(childCommentItem);
                        //대댓글 수를 +1해준다.
                        commentItemArrayList.get(addedChildCommentPosition).setChildCommentCount(commentItemArrayList.get(addedChildCommentPosition).getChildCommentCount() + 1);
                        //어댑터에 notify
                        commentAdapter.notifyItemChanged(addedChildCommentPosition + 1);

                        //댓글이 달린 사용자 단말에 push알림을 보내준다.
                        String receiver = LoginActivity.account;
                        String title = "SNS";
                        String body = LoginActivity.nickname + "님이 회원님의 댓글에 답글을 남겼습니다:" + "\"" + commentItemArrayList.get(addedChildCommentPosition).childCommentList.get(commentItemArrayList.get(addedChildCommentPosition).childCommentList.size() - 1).comment + "\"";
                        String click_action = "CommentActivity";
                        String category = "childcomment";
                        String sender = LoginActivity.account;

                        pushNotification(receiver, title, body, click_action, category, sender, postNum, commentItemArrayList.get(addedChildCommentPosition).getId());//푸시알림 전송
                    }
                    //대댓글이 아직 다 로드되지 않은 경우
                    else {
                        //대댓글 아이템을 최상단에 추가한 후
                        commentItemArrayList.get(addedChildCommentPosition).childCommentList.add(0, childCommentItem);
                        //대댓글 수를 +1해준다.
                        commentItemArrayList.get(addedChildCommentPosition).setChildCommentCount(commentItemArrayList.get(addedChildCommentPosition).getChildCommentCount() + 1);
                        //추가한 대댓글의 id를 저장한다.
                        commentItemArrayList.get(addedChildCommentPosition).uploadedChildCommentChildNum.add(0, childCommentItem.getChildCommentNum());
                        Log.d("업로드한 대댓글의 개수", String.valueOf(commentItemArrayList.get(addedChildCommentPosition).uploadedChildCommentChildNum.size()));

                        //어댑터에 notify
                        commentAdapter.notifyItemChanged(addedChildCommentPosition + 1);

                        //알림을 받아야 하는 사용자 계정을 하나의 문자열에 다 담아서 서버로 보내줘서 그 사용자들에게 동시에 푸시알림을 보내준다.
                        StringBuilder stringBuilder = new StringBuilder();

                        //다른 사람의 댓글에 대댓글을 다는 경우
                        if (!commentItemArrayList.get(addedChildCommentPosition).account.equals(LoginActivity.account)) {
                            stringBuilder.append(commentItemArrayList.get(addedChildCommentPosition).account + "/");
                        }

                        //댓글이 달린 사용자 단말에 push알림을 보내준다.
                        String receiver = LoginActivity.account;
                        String title = "SNS";
                        String body = LoginActivity.nickname + "님이 회원님의 댓글에 답글을 남겼습니다:" + "\"" + commentItemArrayList.get(addedChildCommentPosition).childCommentList.get(0).comment + "\"";
                        String click_action = "CommentActivity";
                        String category = "childcomment";
                        String sender = LoginActivity.account;

                        pushNotification(receiver, title, body, click_action, category, sender, postNum, commentItemArrayList.get(addedChildCommentPosition).getId());//푸시알림 전송

                    }

                    //키보드를 닫아준다.
                    softKeyboard.closeSoftKeyboard();
                } else if (requestType.equals("editChildComment")) {//대댓글 수정 통신
                    //댓글 수정이 완료됐기 때문에 수정완료 상태를 true로 전환
                    isChildCommentEditted = true;
                    String childComment = responseBody.getString("childComment");
                    commentItemArrayList.get(edittedPosition).childCommentList.get(edittedChildCommentPosition).setComment(childComment);
                    commentAdapter.notifyItemChanged(edittedPosition + 1);

                    softKeyboard.closeSoftKeyboard();
                } else if (requestType.equals("deleteChildComment")) {//대댓글 삭제 통신
                    int commentPosition = responseBody.getInt("commentPosition");//삭제된 대댓글의 부모 댓글의 인덱스
                    int childCommentPosition = responseBody.getInt("childCommentPosition");//삭제된 대댓글의 인덱스
                    //이 페이지에서 내가 단 답글(대댓글)을 다시 지우는 경우 업로드한 댓댓글의 id arraylist도 함께 반영을 해준다.
                    for (int i = 0; i < commentItemArrayList.get(commentPosition).uploadedChildCommentChildNum.size(); i++) {
                        if (commentItemArrayList.get(commentPosition).uploadedChildCommentChildNum.get(i) == commentItemArrayList.get(commentPosition).childCommentList.get(childCommentPosition).getChildCommentNum()) {
                            commentItemArrayList.get(commentPosition).uploadedChildCommentChildNum.remove(i);
                        }
                    }

                    //답글 arraylist에서 답글을 지우고
                    commentItemArrayList.get(commentPosition).childCommentList.remove(childCommentPosition);
                    //대댓글 수를 -1해준다.
                    commentItemArrayList.get(commentPosition).setChildCommentCount(commentItemArrayList.get(commentPosition).getChildCommentCount() - 1);


                    //어댑터에 notify
                    commentAdapter.notifyItemChanged(commentPosition + 1);

                } else if (requestType.equals("loadNextChildComment")) {//대댓글 페이징 통신
                    //추가된 대댓글 수
                    int addedCount = 0;
                    boolean isSubstituted = true;
                    int parentPosition = responseBody.getInt("parentPosition");
                    JSONArray jsonArray = responseBody.getJSONArray("childComment");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        ChildCommentItem childCommentItem = new Gson().fromJson(data.toString(), ChildCommentItem.class);

                        //대댓글 더 보기 버튼을 새로 불러온 댓글로 대체하기 위해서 분기한 것
                        if (isSubstituted) {
                            //대댓글 더 보기 버튼을 새로 불러온 첫번째 댓글로 교체
                            commentItemArrayList.get(parentPosition - 1).childCommentList.set(commentItemArrayList.get(parentPosition - 1).childCommentList.size() - 1, childCommentItem);
                            isSubstituted = false;

                            //현재 추가된 댓글들 중에 내가 단 댓글과 일치하는 id가 있는 경우 처음의 댓글은 지워준다.
                            for (int j = 0; j < commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.size(); j++) {
                                Log.d("업로드된 대댓글 개수", String.valueOf(commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.size()));

                                if (commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.get(j) == childCommentItem.getChildCommentNum()) {
                                    Log.d("삭제된 대댓글 id", String.valueOf(commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.get(j)));
                                    commentItemArrayList.get(parentPosition - 1).childCommentList.remove(commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.indexOf(commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.get(j)));
                                    commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.remove(commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.indexOf(commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.get(j)));
                                }
                            }
                        } else {
                            //그 이후의 댓글들은 자연스럽게 추가
                            commentItemArrayList.get(parentPosition - 1).childCommentList.add(childCommentItem);

                            //현재 추가된 댓글들 중에 내가 단 댓글과 일치하는 id가 있는 경우 처음의 댓글은 지워준다.
                            for (int j = 0; j < commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.size(); j++) {
                                Log.d("업로드된 대댓글 개수", String.valueOf(commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.size()));

                                if (commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.get(j) == childCommentItem.getChildCommentNum()) {
                                    Log.d("삭제된 대댓글 id", String.valueOf(commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.get(j)));
                                    commentItemArrayList.get(parentPosition - 1).childCommentList.remove(commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.indexOf(commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.get(j)));
                                    commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.remove(commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.indexOf(commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.get(j)));
                                }
                            }
                        }

                        addedCount += 1;
                    }

                    //전체 대댓글 개수가 현재 대댓글 개수와 다를 때만 더 보기 버튼 넣어주기
                    if (commentItemArrayList.get(parentPosition - 1).getChildCommentCount() != commentItemArrayList.get(parentPosition-1).childCommentList.size()) {

                        Log.d("더 보기 버튼", "추가");
                        commentItemArrayList.get(parentPosition - 1).childCommentList.add(null);
                    }
                    //전체 대댓글 개수가 현재 대댓글 개수와 같은 경우
                    else {
                        if (commentItemArrayList.get(parentPosition - 1).uploadedChildCommentChildNum.size() != 0) {
                            Log.d("더 보기 버튼", "추가");
                            commentItemArrayList.get(parentPosition - 1).childCommentList.add(null);
                        }
                    }
                    //댓글 어댑터에 데이터 변화 notify
                    commentAdapter.notifyItemChanged(parentPosition);

                }


            } else {
                Toast.makeText(getApplicationContext(), "문제가 생겼습니다", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSON ERROR: " + e.getMessage());
        }
    }


    public void setRecyclerView() {

        //댓글 데이터를 담는 어레이리스트 초기화
        commentItemArrayList = new ArrayList<>();

        //댓글 리사이클러뷰의 헤더 데이터를 생성
        Intent intent = getIntent();
        postNum = intent.getIntExtra("postNum", 0);
        postProfile = intent.getStringExtra("postProfile");
        postAccount = intent.getStringExtra("postAccount");
        postNickname = intent.getStringExtra("postNickname");
        postArticle = intent.getStringExtra("postArticle");
        postTime = intent.getStringExtra("postTime");

        rv_comment = findViewById(R.id.recyclerview_comment);
        rv_comment.setHasFixedSize(true);

        //레이아웃 메니저 설정
        linearLayoutManager = new LinearLayoutManager(this);
        rv_comment.setLayoutManager(linearLayoutManager);

        //어댑터 설정
        commentAdapter = new CommentAdapter(this, commentItemArrayList, postProfile, postNickname, postArticle, postTime, CommentActivity.this);
        commentAdapter.setOnClickListener(CommentActivity.this);
        rv_comment.setAdapter(commentAdapter);

        //댓글 데이터를 가져오는 스레드
        getComment();

    }

    //수정 버튼을 누르면 콜백되는 메소드
    @Override
    public void onEditCommentClicked(int position) {
        //댓글 수정 상태로 전환
        isEditting = true;

        //키보드가 뜨게 한다.
        softKeyboard.openSoftKeyboard();

        //게시 버튼의 글자를 수정으로 바꿔준다.
        tv_upload.setText("수정");

        //댓글 입력...이라는 edittext의 힌트를 댓글 수정...으로 바꿔준다.
        et_comment.setHint("댓글 수정...");

        //edittext입력 창에 기존 댓글을 넣어준다.
        et_comment.setText(commentItemArrayList.get(position - 1).comment);

        //댓글 입력창에 포커스를 준다
        et_comment.requestFocus();

        //포커스를 주면 글자 앞에 포커스가 가기 때문에 글자 뒤로 포커스를 이동시킨다.
        et_comment.setSelection(et_comment.getText().length());

        //현재 수정하고자 하는 댓글의 index를 다른 메소드에서 활용하기 위해서 전역변수에 담아준다.
        edittedPosition = position - 1;
    }

    //댓글 삭제버튼 클릭시 콜백되는 메소드
    @Override
    public void onDeleteCommentClicked(int position) {
        //삭제를 확인하는 다이얼로그
        Dialog deleteDialog = new Dialog(CommentActivity.this);
        deleteDialog.setContentView(R.layout.post_delete_check_box);
        deleteDialog.show();

        TextView tv_yes, tv_no;

        tv_yes = deleteDialog.findViewById(R.id.textview_yes);
        tv_no = deleteDialog.findViewById(R.id.textview_no);

        //예 버튼 클릭 리스너
        tv_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //삭제하려는 댓글의 번호
                int commentNum = commentItemArrayList.get(position - 1).id;

                deleteComment(commentNum, position - 1);
                deleteDialog.dismiss();
            }
        });

        //아니오 버튼 클릭 리스너
        tv_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //다이얼로그를 종료한다.
                deleteDialog.dismiss();
            }
        });

    }


    @Override
    public void onProfileClicked(int position) {

    }

    @Override
    public void onCommentClicked(int position) {

    }

    @Override
    public void onHashTagClicked(int position) {

    }

    @Override
    public void onChildCommentClicked(int position) {
        Log.d("답글 달기 버튼", "클릭");

        //답글 달기 상태로 전환
        isChildcommenting = true;

        //키보드가 뜨게 한다.
        softKeyboard.openSoftKeyboard();

        //댓글 입력...이라는 edittext의 힌트를 답글 달기...으로 바꿔준다.
        et_comment.setHint("답글 달기...");

        //댓글 입력창에 포커스를 준다
        et_comment.requestFocus();

        //답글을 달려고 하는 부모 댓글의 position
        addedChildCommentPosition = position - 1;

    }

    //대댓글 어댑터 인터페이스 메소드
    @Override
    public void onEditChildCommentClicked(int position, int parentPosition) {
        Log.d("대댓글 수정 버튼", "클릭");
        Log.d("부모 댓글의 index", String.valueOf(parentPosition));

        //대댓글 수정 상태로 전환
        isChildCommentEditting = true;

        //키보드가 뜨게 한다.
        softKeyboard.openSoftKeyboard();

        //게시 버튼의 글자를 수정으로 바꿔준다.
        tv_upload.setText("수정");

        //댓글 입력...이라는 edittext의 힌트를 답글 수정...으로 바꿔준다.
        et_comment.setHint("답글 수정...");

        //edittext입력 창에 기존 답글 댓글을 넣어준다.
        et_comment.setText(commentItemArrayList.get(parentPosition - 1).childCommentList.get(position).getComment());

        //댓글 입력창에 포커스를 준다
        et_comment.requestFocus();

        //포커스를 주면 글자 앞에 포커스가 가기 때문에 글자 뒤로 포커스를 이동시킨다.
        et_comment.setSelection(et_comment.getText().length());

        //현재 수정하고자 하는 답글의 부모 댓글 index를 다른 메소드에서 활용하기 위해서 전역변수에 담아준다.
        edittedPosition = parentPosition - 1;

        //현재 수정하고자 하는 답글의 index를 다른 메소드에서 활용하기 위해 전역변수에 담는다.
        edittedChildCommentPosition = position;

    }

    @Override
    public void onDeleteChildCommentClicked(int position, int parentPosition) {
        Log.d("대댓글 삭제 버튼", "클릭");
        Log.d("부모 댓글의 index", String.valueOf(parentPosition));

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.post_delete_check_box);

        dialog.show();

        TextView tv_yes, tv_no;

        tv_yes = dialog.findViewById(R.id.textview_yes);
        tv_no = dialog.findViewById(R.id.textview_no);

        tv_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

                //삭제하려는 대댓글의 테이블 번호
                int childCommentNum = commentItemArrayList.get(parentPosition - 1).childCommentList.get(position).getChildCommentNum();

                //삭제하려는 대댓글의 인덱스
                int childCommentPosition = position;

                //삭제하려는 대댓글의 부모 댓글 인덱스
                int commentPosition = parentPosition - 1;
                deleteChildComment(childCommentNum, childCommentPosition, commentPosition);
            }
        });

        tv_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


    }


    @Override
    public void onMoreCommentClicked(int position, int parentPosition) {
        Log.d("대댓글 더 보기 버튼", "클릭");
        Log.d("부모 댓글의 index", String.valueOf(parentPosition));
        //현재 로드되어있는 대댓글 수를 가져온다.
        int lastChildCommentId = commentItemArrayList.get(parentPosition - 1).childCommentList.get(commentItemArrayList.get(parentPosition - 1).childCommentList.size() - 2).id;
        int postNum = commentItemArrayList.get(parentPosition - 1).postNum;
        Log.d("게시물 번호", String.valueOf(postNum));
        int commentNum = commentItemArrayList.get(parentPosition - 1).id;
        Log.d("댓글 번호", String.valueOf(commentNum));

        loadNextChildComment(account, lastChildCommentId, postNum, commentNum, parentPosition);
    }

    //키보드 콜백 메소드 제거
    @Override
    protected void onDestroy() {
        super.onDestroy();
        softKeyboard.unRegisterSoftKeyboardCallback();
    }

    @Override
    public void onBackPressed() {
        if (et_comment.isFocused()) {
            et_comment.setText("");
            tv_upload.setText("게시");
        } else {
            super.onBackPressed();
            finish();
        }
    }
}
