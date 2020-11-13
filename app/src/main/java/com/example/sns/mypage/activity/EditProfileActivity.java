package com.example.sns.mypage.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sns.R;
import com.example.sns.login.model.LoginUser;
import com.example.sns.mypage.model.ProfileResponse;
import com.example.sns.util.ProcessImage;
import com.example.sns.util.RetrofitService;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import de.hdodenhof.circleimageview.CircleImageView;
import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static com.example.sns.join.activity.JoinActivity.IP_ADDRESS;
import static com.example.sns.util.ImageResizeUtils.exifOrientationToDegrees;

public class EditProfileActivity extends AppCompatActivity {
    private CircleImageView btn_editphoto;
    private Button btn_cancel, btn_apply;
    private ImageButton btn_delete_name, btn_delete_nickname, btn_delete_introduce;
    private EditText et_name, et_nickname, et_introduce;
    private TextView tv_account, tv_email, tv_nameresult, tv_nicknameresult, tv_length;

    //변경한 닉네임이 기존의 닉네임과 같을 때는 중복검사를 할 필요가 없기 때문에 그 경우를 캐치하기 위해서 원래 닉네임을 선언
    private String org_nickname;
    private String edittedProfile;//수정된 프로필 사진
    //프로필 이미지를 담을 비트맵
    private Bitmap profileBitmap;
    //사진의 회전값을 얻기 위한 이미지의 경로
    private int rotationDegree = 0;
    private String imageUri = null;//이미지 uri

    //실제로 서버로 전송될 이미지의 경로
    private String profileImagePath = null;

    //프사가 사전에 설정되어있는지 아닌지 가리기 위한 boolean
    private boolean isSelected;

    private String Image;

    private LoginUser loginUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        loginUser = LoginUser.getInstance();
        //프로필 사진 변경 버튼
        btn_editphoto = findViewById(R.id.img_profile);
        //아이디
        tv_account = findViewById(R.id.textview_account);
        tv_email = findViewById(R.id.textview_email);
        //사용자 정보
        et_name = findViewById(R.id.edittext_name);
        et_nickname = findViewById(R.id.edittext_nickname);
        et_introduce = findViewById(R.id.edittext_introduce);
        //소개 입력란에 쓸 수 있는 남은 글자수
        tv_length = findViewById(R.id.textview_introduce_length);
        //프로필 정보 삭제 버튼
        btn_delete_name = findViewById(R.id.imagebutton_delete_name);
        btn_delete_nickname = findViewById(R.id.imagebutton_delete_nickname);
        btn_delete_introduce = findViewById(R.id.imagebutton_delete_introduce);
        //입력받은 사용자 정보의 양식 검사 및 중복 체크 메세지
        tv_nameresult = findViewById(R.id.textview_nameresult);
        tv_nicknameresult = findViewById(R.id.textview_nicknameresult);
        //프로필 수정 완료 버튼
        btn_apply = findViewById(R.id.button_apply);
        //취소 버튼
        btn_cancel = findViewById(R.id.button_cancel);
        setProfile(loginUser.getAccount());

        //프로필 사진 클릭 리스너
        btn_editphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //프로필 사진 관련 설정을 할 수 있는 다이얼로그 출력
                final Dialog dialog = new Dialog(EditProfileActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.profilephoto_setting_box);
                dialog.show();

                TextView tv_delete, tv_change;
                //사진 삭제
                tv_delete = dialog.findViewById(R.id.textview_delete);
                //사진 변경
                tv_change = dialog.findViewById(R.id.textview_change);

                //사진 변경버튼 클릭 리스너
                tv_change.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //권한 허가를 요청하는 메소드
                        tedPermission();

                        TedBottomPicker.with(EditProfileActivity.this)
                                //이미지 피커가 올라오는 높이
                                .setPeekHeight(2000)
                                .setPreviewMaxCount(1000)
                                .show(new TedBottomSheetDialogFragment.OnImageSelectedListener() {
                                    // 사진을 선택하면 param으로 사진의 uri가 넘어옴
                                    @Override
                                    public void onImageSelected(Uri uri) {
                                        dialog.dismiss();

                                        RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.profile);
                                        Glide.with(getApplicationContext())
                                                .load(uri)
                                                .thumbnail(0.1f)
                                                .apply(options)
                                                .into(btn_editphoto);


                                        //비트맵을 서버로 전송하기 전에 용량을 1/4로 줄이고 비트맵 변수를 선언해준다.
                                        //나머지 작업은 실제로 프로필 수정을 완료하는 버튼을 누를 때 해준다.
                                        BitmapFactory.Options option = new BitmapFactory.Options();
                                        option.inSampleSize = 4;
                                        profileBitmap = BitmapFactory.decodeFile(uri.getPath(), option);
                                        imageUri = uri.toString();
                                        try {
                                            ExifInterface exifInterface = new ExifInterface(uri.getPath());
                                            int exifOrientation = exifInterface.getAttributeInt(
                                                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                                            rotationDegree = exifOrientationToDegrees(exifOrientation);
                                            Log.d("회전값", String.valueOf(rotationDegree));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        Log.d("uri경로", uri.getPath());

                                    }
                                });
                    }
                });

                //프로필 이미지 삭제 버튼을 눌렀을 경우
                tv_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Glide.with(getApplicationContext())
                                .load(R.drawable.profile)
                                .into(btn_editphoto);
                        profileBitmap = null;
                        Image = null;
                        isSelected = false;
                        dialog.dismiss();
                    }
                });
            }
        });


        //이름 입력란의 포커스 리스너
        et_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //포커스를 획득한 경우
                if (hasFocus) {
                    //삭제 버튼을 등장시키고
                    btn_delete_name.setVisibility(View.VISIBLE);
                    //클릭 이벤트 리스너를 달아서
                    btn_delete_name.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String name = et_name.getText().toString();
                            //이름 입력란이 공백이 아닐 경우에만
                            if (!name.trim().equals("")) {
                                //텍스트를 비워주고
                                et_name.setText("");
                                //검사 메세지 출력
                                tv_nameresult.setText("이름은 필수 항목 입니다.");
                                tv_nameresult.setTextColor(Color.parseColor("#FF6670"));
                            }
                        }
                    });


                    //이름 입력란에 텍스트 입력 리스너
                    et_name.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            String name = et_name.getText().toString();
                            //이름 입력 칸이 공백인 경우
                            if (name.trim().equals("")) {
                                tv_nameresult.setText("이름은 필수 항목 입니다.");
                                tv_nameresult.setTextColor(Color.parseColor("#FF6670"));
                            }
                            //이름 입력 칸에 값이 있는 경우
                            else {
                                //양식 검사 메소드 실행
                                checkName(name);
                            }
                        }
                    });
                }
                //포커스가 떠난 경우
                else {
                    //삭제 버튼을 다시 없애준다.
                    btn_delete_name.setVisibility(View.GONE);
                    String name = et_name.getText().toString();
                    //만약 이름이 공백 상태라면
                    if (name.trim().equals("")) {
                        tv_nameresult.setText("이름은 필수 항목 입니다.");
                        tv_nameresult.setTextColor(Color.parseColor("#FF6670"));
                    }
                    //이름 입력 칸에 값이 있는 경우
                    else {
                        //양식 검사 메소드 실행
                        checkName(name);
                    }
                }
            }
        });

        //닉네임 입력란의 포커스 리스너
        et_nickname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                //포커스를 획득한 경우
                if (hasFocus) {
                    //삭제 버튼을 등장시키고
                    btn_delete_nickname.setVisibility(View.VISIBLE);
                    //클릭 이벤트 리스너를 달아서
                    btn_delete_nickname.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String nickname = et_nickname.getText().toString();
                            //닉네임 입력란이 공백이 아닐 경우에만
                            if (!nickname.trim().equals("")) {
                                //텍스트를 비워주고
                                et_nickname.setText("");
                                //검사 메세지 출력
                                tv_nicknameresult.setText("닉네임은 필수 항목 입니다.");
                                tv_nicknameresult.setTextColor(Color.parseColor("#FF6670"));
                            }
                        }
                    });


                    //닉네임 입력란에 텍스트 입력 리스너
                    et_nickname.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            //닉네임 입력칸에 입력된 값
                            String nickname = et_nickname.getText().toString();
                            //닉네임 입력 칸이 공백인 경우
                            if (nickname.trim().equals("")) {
                                tv_nicknameresult.setText("닉네임은 필수 항목 입니다.");
                                tv_nicknameresult.setTextColor(Color.parseColor("#FF6670"));
                            }
                            //닉네임 입력 칸에 값이 있는 경우
                            else {
                                checkNickname(nickname);
                            }
                        }
                    });
                }
                //포커스가 떠난 경우
                else {
                    //삭제 버튼을 다시 없애준다.
                    btn_delete_nickname.setVisibility(View.GONE);
                    String nickname = et_nickname.getText().toString();
                    //만약 이름이 공백 상태라면
                    if (nickname.trim().equals("")) {
                        tv_nicknameresult.setText("닉네임은 필수 항목 입니다.");
                        tv_nicknameresult.setTextColor(Color.parseColor("#FF6670"));
                    }
                    //이름 입력 칸에 값이 있는 경우
                    else {
                        //양식 검사 메소드 실행
                        checkNickname(nickname);
                    }
                }
            }
        });


        //소개 입력란의 포커스 리스너
        et_introduce.setOnFocusChangeListener(new View.OnFocusChangeListener() {


            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                //소개 입력란에 포커스가 잡힌 경우
                if (hasFocus) {
                    //소개 입력란의 텍스트를 가져온다
                    String introduce = et_introduce.getText().toString();
                    //소개글의 길이를 구한다.
                    int length = introduce.length();
                    //남은 소개글의 길이를 대입해서
                    int leftLength = 150 - length;
                    //남은 글자수를 대입해준다.
                    tv_length.setText(String.valueOf(leftLength));
                    //삭제 버튼을 등장시키고
                    btn_delete_introduce.setVisibility(View.VISIBLE);
                    //클릭 이벤트 리스너를 달아서
                    btn_delete_introduce.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //소개 입력란을 다 비워준다.
                            et_introduce.setText("");
                            //글자 수는 0이 된다.
                            tv_length.setText("150");
                        }
                    });

                    et_introduce.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                            //소개 입력란의 텍스트를 가져온다
                            String introduce = et_introduce.getText().toString();
                            //소개글의 길이를 구한다.
                            int length = introduce.length();
                            //남은 소개글의 길이를 대입해서
                            int leftLength = 150 - length;
                            //화면에 뿌려준다.
                            tv_length.setText(String.valueOf(leftLength));
                            //남은 글자수가 0보다 작아지면
                            if (leftLength < 0) {
                                //글자수가 빨간 색으로 변한다.
                                tv_length.setTextColor(Color.parseColor("#FF6670"));
                            }
                            //그렇지 않으면
                            else {
                                //글자수가 원래 색으로 변한다.
                                tv_length.setTextColor(Color.parseColor("#808080"));
                            }
                        }
                    });
                }
                //소개 입력란에 포커스가 사라진 경우
                else {
                    btn_delete_introduce.setVisibility(View.GONE);
                }
            }
        });


        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = et_name.getText().toString();
                String nameResult = tv_nameresult.getText().toString();
                String nickname = et_nickname.getText().toString();
                String nicknameResult = tv_nicknameresult.getText().toString();
                String introduce = et_introduce.getText().toString();
                int introduceLength = 0;
                //글자수 textview가 null이 아닌 경우에만
                if (!tv_length.getText().toString().equals("")) {
                    //글자수를 숫자로 파싱한다.
                    introduceLength = Integer.parseInt(tv_length.getText().toString());
                }


                //이름 항목이 충족되지 않을 때
                if (nameResult.equals("이름은 필수 항목 입니다.") || nameResult.equals("잘못된 형식의 이름입니다.") || nameResult.equals("이름은 최대 30자까지 입니다.")) {
                    Toast.makeText(getApplicationContext(), "이름을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
                //닉네임 항목이 충족되지 않을 때
                else if (nicknameResult.equals("닉네임은 필수 항목 입니다.") || nicknameResult.equals("닉네임의 길이는 20자를 넘을 수 없습니다.") || nicknameResult.equals("이미 사용 중인 닉네임 입니다.")) {
                    Toast.makeText(getApplicationContext(), "닉네임을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
                //소개 항목이 충족되지 않을 때
                else if (introduceLength < 0) {
                    Toast.makeText(getApplicationContext(), "소개 항목의 글자수가 초과되었습니다.", Toast.LENGTH_SHORT).show();
                }
                //모든 항목이 충족될 때
                else {
                    updateProfile(name, nickname, introduce);
                }
            }
        });


        //취소 버튼 클릭 이벤트 리스너
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void checkName(String name) {
        //이름이 양식에 맞지 않은 경우
        if (!Pattern.matches("^[가-힝]{2,}$", name)) {
            tv_nameresult.setText("잘못된 형식의 이름입니다.");
            tv_nameresult.setTextColor(Color.parseColor("#FF6670"));
        } else if (name.length() > 30) {
            tv_nameresult.setText("이름은 최대 30자까지 입니다.");
            tv_nameresult.setTextColor(Color.parseColor("#FF6670"));
        }
        //이름이 양식에 맞는 경우
        else {
            tv_nameresult.setText("");
        }

    }

    private void checkNickname(String nickname) {
        //닉네임의 길이가 1자에서 20자 사이가 되고 기존의 닉네임과 다른 경우에만
        if (nickname.length() > 0 && nickname.length() <= 20 && !org_nickname.equals(nickname)) {
            //서버에 접근해서 중복확인을 하는 asynctask 객체를 선언
            DoubleCheck doubleCheck = new DoubleCheck();
            //asynctask실행
            //param1: 접근하고자 하는 서버 url
            //param2: 입력된 아이디
            doubleCheck.execute("http://" + IP_ADDRESS + "/checknickname.php", nickname);
        }
        //닉네임이 기존의 닉네임과 같은 경우
        else if (org_nickname.equals(nickname)) {
            tv_nicknameresult.setText("");
        }
        //닉네임의 길이가 20자를 넘어가는 경우
        else {
            tv_nicknameresult.setText("닉네임의 길이는 20자를 넘을 수 없습니다.");
            tv_nicknameresult.setTextColor(Color.parseColor("#FF6670"));
        }
    }


    public void setProfile(String account) {
        ProgressDialog progressDialog = new ProgressDialog(EditProfileActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        //레트로핏 객체 생성
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://13.124.105.47/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        Call<ProfileResponse> call = retrofitService.getProfileData(account, account);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                ProfileResponse profileResponse = response.body();
                Log.d("리턴 값", profileResponse.toString());

                String account = profileResponse.getAccount();
                String name = profileResponse.getName();
                String nickname = profileResponse.getNickname();
                String email = profileResponse.getEmail();
                String introduce = profileResponse.getIntroduce();
                String image = profileResponse.getImage();


                tv_account.setText(account);
                tv_email.setText(email);
                et_name.setText(name);
                et_nickname.setText(nickname);

                //수정 전 닉네임을 저장
                org_nickname = et_nickname.getText().toString();

                //소개란은 필수 항목이 아니라서 null값일 수도 있기 때문에 null체크를 해준다.
                if (introduce != null) {
                    et_introduce.setText(introduce);
                    //소개글의 길이를 구한다.
                    int length = introduce.length();
                    //남은 소개글의 길이를 대입해서
                    int leftLength = 150 - length;
                    Log.d("남은 글자수", String.valueOf(leftLength));
                    //남은 글자수를 대입해준다.
                    tv_length.setText(String.valueOf(leftLength));
                }

                //프로필 사진도 필수 항목이 아니라서 null값일 수도 있기 때문에 null체크를 해준다.
                if (image != null) {
                    Glide.with(getApplicationContext())
                            .load("http://" + IP_ADDRESS + "/profileimage/" + image)
                            .thumbnail(0.1f)
                            .apply(new RequestOptions().centerCrop())
                            .into(btn_editphoto);

                    Image = image;
                    isSelected = true;
                } else {
                    isSelected = false;
                }

                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "문제가 생겼습니다.", Toast.LENGTH_SHORT).show();
            }
        });

    }


    //서버의 데이터와 비교하여 중복검사를 실시하는 스레드다.
    private class DoubleCheck extends AsyncTask<String, Void, String> {

        //어떤 항목에서 넘어온 값인지 가려내는 코드(account, nickname)


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {


            //통신할 서버의 url
            String serverURL = params[0];

            //중복검사를 실시할 닉네임
            String nickname = params[1];

            //서버로 전달할 param
            String postParameter = "nickname=" + nickname;


            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();

                //서버로 데이터를 내보내기 위한 스트림 객체 선언
                OutputStream outputStream = httpURLConnection.getOutputStream();
                //전송할 데이터를 바이트 어레이로 풀어서 전달
                outputStream.write(postParameter.getBytes("UTF-8"));

                outputStream.flush();
                //종료를 해줘야 전송 완료
                outputStream.close();


                //통신의 결과에 대한 메세지를 담기 위해서 스트림 객체 선언
                InputStream inputStream;

                //서버의 responseCode
                int responseStatusCode = httpURLConnection.getResponseCode();

                //통신이 원활하게 됐으면
                if (responseStatusCode == httpURLConnection.HTTP_OK) {
                    //서버 php파일의 ehco값을 가져와서 저장
                    inputStream = httpURLConnection.getInputStream();

                }
                //통신이 원활하게 되지 않았으면
                else {
                    //에러 메세지 저장
                    inputStream = httpURLConnection.getErrorStream();
                }

                //한글 데이터가 깨지지 않게 하기 위해서 InputStreamReader로 감싸준다.
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                //읽어오는 속도를 향상시키기 위해 BufferedReader로 감싸준다.
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                //서버에서 응답받은 메세지를 추가시킬 stringbuilder
                StringBuilder sb = new StringBuilder();
                //서버의 응답메세지를 한줄씩 담을 변수
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                //읽어오기 종료
                bufferedReader.close();


                return sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }


        }

        //result에 올 수 있는 값은 2가지: "사용 가능한 닉네임 입니다.", "이미 사용 중인 닉네임 입니다."
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //서버와 통신이 잘 이루어진 경우
            if (result != null) {

                if (result.equals("사용 가능한 닉네임 입니다.")) {
                    tv_nicknameresult.setText(result);
                    tv_nicknameresult.setTextColor(Color.parseColor("#77A88D"));
                } else {
                    tv_nicknameresult.setText(result);
                    tv_nicknameresult.setTextColor(Color.parseColor("#FF6670"));
                }
            }
        }
    }


    public void updateProfile(String name, String nickname, String introduce) {

        //프로그래스 다이얼로그 시작
        ProgressDialog progressDialog = new ProgressDialog(EditProfileActivity.this);
        progressDialog.show(EditProfileActivity.this, "프로필 수정중", "잠시만 기다려주세요.", true, false);

        //레트로핏 객체 생성
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://13.124.105.47/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //서버로 보내줄 param설정
        RequestBody accountPart = RequestBody.create(MultipartBody.FORM, loginUser.getAccount());
        RequestBody namePart = RequestBody.create(MultipartBody.FORM, name);
        RequestBody nicknamePart = RequestBody.create(MultipartBody.FORM, nickname);
        RequestBody introducePart = RequestBody.create(MultipartBody.FORM, introduce);
        RequestBody isselectedPart;
        if(isSelected){
            isselectedPart = RequestBody.create(MultipartBody.FORM, "yes");
        }
        else {
            isselectedPart = RequestBody.create(MultipartBody.FORM, "no");
        }

        RequestBody imageFile;

        //프로필 이미지를 설정했을 때
        if (profileBitmap != null) {

            //이미지 파일의 이름(사용자계정+시간)
            String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
            String imageFileName = loginUser.getAccount() + timeStamp + ".jpg";

            //jpeg파일로 변환해서 임시 파일에 이미지를 넣어주고 경로를 불러오는 메소드를 실행해서 프로필 사진의 경로를 넣어준다.
            profileImagePath = saveBitmaptoJpeg(getApplicationContext(), profileBitmap, imageFileName);
            Log.d("이미지 경로", profileImagePath);


            ProcessImage processImage = new ProcessImage(EditProfileActivity.this);//이미지 처리 객체 초기화

            File file = processImage.createFileFromBitmap(processImage.getBitmapFromUri(imageUri), imageUri);
            imageFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

            MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFileName, imageFile);
            RetrofitService retrofitService = retrofit.create(RetrofitService.class);
            Call<ProfileResponse> call = retrofitService.editProfile(accountPart, namePart, nicknamePart, introducePart, isselectedPart, body);
            call.enqueue(new Callback<ProfileResponse>() {
                //통신에 성공한 경우
                @Override
                public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                    ProfileResponse profileResponse = response.body();
                    Log.d("통신", "성공");
                    Log.d("아이디", profileResponse.getAccount());
                    Log.d("이름", profileResponse.getName());
                    Log.d("닉네임", profileResponse.getNickname());
                    Log.d("소개", profileResponse.getIntroduce());
                    Log.d("이미지", profileResponse.getImage());

                    edittedProfile = profileResponse.getImage();

                    //변경된 닉네임과 프로필 사진의 데이터를 로컬에 업데이트 해준다.
                    SharedPreferences sharedPreferences = getSharedPreferences("loginUser", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("nickname", profileResponse.getNickname());
                    editor.putString("profile", profileResponse.getImage());
                    editor.apply();

                    //현재 로그인 사용자 정보 변경
                    LoginUser.getInstance().setNickname(profileResponse.getNickname());
                    LoginUser.getInstance().setProfile(profileResponse.getImage());

                    //프로그래스 다이얼로그 종료
                    progressDialog.dismiss();
                    //수정 완료 메세지 출력
                    Toast.makeText(getApplicationContext(), "프로필 수정 완료", Toast.LENGTH_SHORT).show();
                    //프로필 수정 액티비티 종료
                    finish();
                }

                //통신에 실패한 경우
                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    Log.d("통신", "실패");
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "문제가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        //설정하지 않았을 때
        else {
            RetrofitService retrofitService = retrofit.create(RetrofitService.class);
            Call<ProfileResponse> call = retrofitService.editProfile(accountPart, namePart, nicknamePart, introducePart, isselectedPart, null);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                    ProfileResponse profileResponse = response.body();
                    //변경된 닉네임과 프로필 사진의 데이터를 로컬에 업데이트 해준다.
                    SharedPreferences sharedPreferences = getSharedPreferences("sessionCookie", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userNickname", profileResponse.getNickname());
                    editor.putString("userProfile", "null");
                    editor.apply();

                    Log.d("통신", "성공");
                    //프로그래스 다이얼로그 종료
                    progressDialog.dismiss();

                    if (Image != null) {
                        edittedProfile = Image;
                    } else {
                        edittedProfile = profileResponse.getImage();
                    }

                    //수정 완료 메세지 출력
                    Toast.makeText(getApplicationContext(), "프로필 수정 완료", Toast.LENGTH_SHORT).show();
                    //프로필 수정 액티비티 종료
                    finish();
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    //프로그래스 다이얼로그 종료
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "문제가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    //tedPermission라이브러리는 권한요청을 편리하게 할 수 있는 api
    private void tedPermission() {

        //권한요청이 일어나는 순간을 캐치하는 리스터 객체
        PermissionListener permissionListener = new PermissionListener() {
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

    //비트맵을 jpeg파일로 변환해서 저장해주는 매소드
    //param1:적용하고자 하는 이미지 bitmap
    //param2: 저장시키려고 하는 로컬의 폴더
    //param3: 파일의 이름
    //이 메소드는 현재 사용하지 않는다. 왜냐하면 이런 리사이징 과정을 거치면 php에서 exif값이 가져와지지 않는 문제가 생겨서 이미지를 회전시킬 수 없기 때문이다.
    public static String saveBitmaptoJpeg(Context context, Bitmap bitmap, String name) {

        //파일을 저장시킬 임시의 폴더 생성
        File storage = context.getCacheDir();
        //파일의 이름
        String file_name = name + ".jpg";

        File tempFile = new File(storage, file_name);
        try {
            //파일을 생성해준다.
            tempFile.createNewFile();

            //param에 들어간 파일에 데이터를 넣어주기 위해서 FileOutPutStream객체 선언
            FileOutputStream out = new FileOutputStream(tempFile);
            //비트맵을 jpeg로 변환한 후 파일에 덮어써준다.(param3에 fileoutputstream을 넣어서 write와 같은 효과를 낸듯?)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            //스트림을 종료시켜서 저장시켜준다.
            out.close();
            //저장된 파일을 리턴해준다.
            return tempFile.getAbsolutePath();

        } catch (FileNotFoundException exception) {
            Log.e("FileNotFoundException", exception.getMessage());
            return null;
        } catch (IOException exception) {
            Log.e("IOException", exception.getMessage());
            return null;
        }
    }

}
