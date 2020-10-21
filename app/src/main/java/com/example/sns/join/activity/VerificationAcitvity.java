package com.example.sns.join.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.sns.R;
import com.example.sns.login.activity.LoginActivity;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.sns.join.activity.JoinActivity.IP_ADDRESS;


public class VerificationAcitvity extends AppCompatActivity {

    private EditText edittext_verificationNum;
    private TextView textview_timeCount;
    private Button btn_verification, btn_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_acitvity);

        edittext_verificationNum = findViewById(R.id.edittext_verification_num);
        textview_timeCount = findViewById(R.id.textview_timecount);
        btn_verification = findViewById(R.id.button_verification);
        btn_cancel = findViewById(R.id.button_cancel);

        //인증 제한 시간
        int totalTime = 1000*60*3;//3분
        //시간이 줄어드는 인터벌(1초)
        int interval = 1000;

        //인텐트에 담겨있는 회원정보를 꺼내준다.
        Intent intent = getIntent();
        String account = intent.getStringExtra("account");
        String password = intent.getStringExtra("password");
        String name = intent.getStringExtra("name");
        String nickname = intent.getStringExtra("nickname");
        String email = intent.getStringExtra("email");
        String verificationNum = intent.getStringExtra("verificationNum");

        //시간을 카운트다운하기 위해서 선언한 객체
        CountDownTimer countDownTimer = new CountDownTimer(totalTime,interval) {

            //생성자의 두번째 param에 지정한 숫자만큼 시간이 지날때마다 호출되는 메소드다. 즉 1초에 한번씩 호출
            //이 메소드의 param은 호출된 시점부터 종료까지 남은 시간을 의미한다.
            @Override
            public void onTick(long millisUntilFinished) {
                //종료까지 남은 시간
                long leftTime = millisUntilFinished/1000;

                //초가 10초보다 크면
                if((leftTime-((leftTime / 60)*60)) >= 10){
                    //초를 그대로 출력
                    textview_timeCount.setText((leftTime/60)+" : "+(leftTime-((leftTime / 60)*60)));
                }
                //초가 10초보다 적게 남을 때는
                else {
                    //10의 자리 수에 0을 붙여서 출력
                    textview_timeCount.setText((leftTime / 60)+": 0"+(leftTime-((leftTime / 60)*60)));
                }
            }

            //시간초가 다 갔을 때의 동작 정의
            @Override
            public void onFinish() {
                Toast.makeText(getApplicationContext(), "인증에 실패하셨습니다. 회원가입을 다시 진행해주세요.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(VerificationAcitvity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        };

        //카운트다운 시작!
        countDownTimer.start();

        btn_verification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //인증번호 칸에 입력된 인증번호
                String inputVerificationNum = edittext_verificationNum.getText().toString();
                //인증번호가 맞다면
                if(verificationNum.equals(inputVerificationNum)){
                    //사용자 정보를 서버의 db에 저장시키기 위한 asynctask실행
                    InsertUser insertUser = new InsertUser();
                    insertUser.execute("http://" + IP_ADDRESS + "/insertuser.php", account, password, name, nickname, email);

                    //타이머 종료
                    countDownTimer.cancel();

                    //로그인 화면으로 이동
                    Intent intent = new Intent(VerificationAcitvity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    //해당 액티비티 종료
                    finish();
                }
                //인증번호가 틀리다면
                else {
                    Toast.makeText(getApplicationContext(), "인증번호를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //최소버튼을 눌렀을 때
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //타이머를 꺼주고
                countDownTimer.cancel();

                //해당 액티비티 종료
                finish();
            }
        });

    }

    private class InsertUser extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {

            //회원 정보
            String account = params[1];
            String password = params[2];
            String name = params[3];
            String nickname = params[4];
            String email = params[5];

            //접근하고자 하는 서버 url
            String serverURL = params[0];

            String postParameters = "account="+account+"&password="+password+"&name="+name+"&nickname="+nickname+"&email="+email;


            try {
                //url객체 생성
                URL url = new URL(serverURL);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");

                //서버와 연결
                httpURLConnection.connect();

                //서버로 데이터를 내보내줄 스트림 생성
                OutputStream outputStream = httpURLConnection.getOutputStream();
                //속도 향상을 위해서 buffer스트림 사용
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                //스트림을 통해서 서버로 데이터를 바이트단위로 전송
                bufferedOutputStream.write(postParameters.getBytes("UTF-8"));
                //스트림의 데이터를 비워주고
                bufferedOutputStream.flush();
                //스트림을 종료시켜준다.(이 과정을 해줘야 실제로 데이터가 넘어감)
                bufferedOutputStream.close();

                InputStream inputStream;

                //http response코드
                int responseStatusCode = httpURLConnection.getResponseCode();
                //서버로부터 넘어오는 echo 값을 받기 위한 스트림 생성


                //통신에 성공한 경우
                if(responseStatusCode == httpURLConnection.HTTP_OK){
                    //php페이지의 echo 값을 가져온다
                    inputStream = httpURLConnection.getInputStream();
                }
                //통신에 실패한 경우
                else {

                    inputStream = httpURLConnection.getErrorStream();
                }

                //서버에서 넘어오는 한글이 깨지지 않게 하기 위해서 reader로 감싸주고
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                //속도를 올리기 위해서 bufferedreader로 감싼다.
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                //서버로부터 반환된 데이터를 실제로 담을 StringBuilder
                StringBuilder sb = new StringBuilder();
                //sb에 값을 넣어주기 위해서 잠깐 보관하는 매개체
                String line = null;


                if((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }


        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //통신이 잘 돼서 사용자 정보가 들어갔을 때
            if(result != null){
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            }
            //통신 실패했을 때
            else {
                Toast.makeText(getApplicationContext(), "문제가 생겼습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
