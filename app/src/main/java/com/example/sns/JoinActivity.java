package com.example.sns;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class JoinActivity extends AppCompatActivity {
    private EditText editText_id, editText_password, editText_passwordCheck, editText_name, editText_nickname, editText_email;
    private TextView textView_id, textView_password, textView_passwordCheck, textView_name, textView_nickname, textView_email;
    private Button btn_join, btn_cancel;

    public static String IP_ADDRESS = "13.124.105.47";
    private String TAG = JoinActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        //회원가입 목록 입력 값을 담는 변수들
        editText_id = findViewById(R.id.edittext_id);
        editText_password = findViewById(R.id.edittext_password);
        editText_passwordCheck = findViewById(R.id.edittext_passwordcheck);
        editText_name = findViewById(R.id.edittext_name);
        editText_nickname = findViewById(R.id.edittext_nickname);
        editText_email = findViewById(R.id.edittext_email);

        //입력한 항목들의 양식이나 중복 체크를 위한 변수들
        textView_id = findViewById(R.id.textview_idresult);
        textView_password = findViewById(R.id.textview_passwordresult);
        textView_passwordCheck = findViewById(R.id.textview_passwordcheckresult);
        textView_name = findViewById(R.id.textview_nameresult);
        textView_nickname = findViewById(R.id.textview_nicknameresult);
        textView_email = findViewById(R.id.textview_emailresult);


        //회원가입 버튼
        btn_join = findViewById(R.id.button_join);
        //취소 버튼
        btn_cancel = findViewById(R.id.button_cancel);

        //택스트 입력창의 텍스트에 변화가 생길 때마다 이벤트를 캐치하는 리스너
        editText_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            //택스트가 변할때마다 호출되는 메소드다.
            @Override
            public void afterTextChanged(Editable s) {
                //아이디 입력창에 입력된 값
                String account = editText_id.getText().toString();
                //아이디 입력 칸이 공백인 경우
                if (TextUtils.isEmpty(account)) {
                    textView_id.setText("");
                }
                //아이디 입력 칸에 값이 있는 경우
                else {
                    //양식 검사와 중복검사를 실시하는 메소드
                    checkAccount(account);
                }

            }
        });

        editText_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //비밀번호 입력창에 입력된 값
                String password = editText_password.getText().toString();
                //비밀번호 확인 입력창에 입력된 값
                String passwordCheck = editText_passwordCheck.getText().toString();

                //비밀번호 입력 칸이 공백인 경우
                if (TextUtils.isEmpty(password)) {
                    textView_password.setText("");
                }
                //비밀번호 입력 칸에 값이 있는 경우
                else {
                    checkPassword(password);
                }

                //비밀번호 확인란이 입력된 상태에서 비밀번호 입력란의 데이터를 바꾸는 경우
                if (!passwordCheck.trim().equals("") && !password.equals(passwordCheck)) {
                    textView_passwordCheck.setText("비밀번호 불일치");
                    textView_passwordCheck.setTextColor(Color.parseColor("#FF6670"));
                }
                //비밀번호 확인란이 입력된 상태에서 비밀번호 입력란의 데이터가 비밀번호 확인란의 데이터와 일치하는 경우
                else if (!passwordCheck.trim().equals("") && password.equals(passwordCheck)) {
                    textView_passwordCheck.setText("비밀번호 일치");
                    textView_passwordCheck.setTextColor(Color.parseColor("#77A88D"));
                }
            }
        });


        editText_passwordCheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //비밀번호 입력창에 입력된 값
                String password = editText_password.getText().toString();
                //비밀번호 확인 입력창에 입력된 값
                String passwordCheck = editText_passwordCheck.getText().toString();
                //비밀번호 확인 입력 칸이 공백인 경우
                if (passwordCheck.trim().equals("")) {
                    textView_passwordCheck.setText("");
                }
                //비밀번호 확인 입력 칸에 값이 있는 경우
                else {
                    checkPasswordCheck(password, passwordCheck);
                }
            }
        });

        editText_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //이름 입력창에 입력된 값
                String name = editText_name.getText().toString();
                //이름 입력 칸이 공백인 경우
                if (name.trim().equals("")) {
                    textView_name.setText("");
                }
                //이름 입력 칸에 값이 있는 경우
                else {
                    checkName(name);
                }
            }
        });

        editText_nickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //닉네임 입력칸에 입력된 값
                String nickname = editText_nickname.getText().toString();
                //닉네임 입력 칸이 공백인 경우
                if (nickname.trim().equals("")) {
                    textView_nickname.setText("");
                }
                //닉네임 입력 칸에 값이 있는 경우
                else {
                    checkNickname(nickname);
                }

            }
        });


        editText_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //이메일 입력 칸에 입력한 이메일
                String email = editText_email.getText().toString();
                //이메일 입력 칸에 값이 없는 경우
                if (email.trim().equals("")) {
                    textView_email.setText("");
                }
                //이메일 입력 칸에 값이 있는 경우
                else {
                    checkEmail(email);
                }
            }
        });

        //회원 가입 버튼 클릭 이벤트 리스너
        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //아이디 검사 메세지가"사용 가능한 아이디 입니다."이면 됨
                //비밀번호 확인 검사 메세지가 "비밀번호 일치이면 됨"
                //이름은 빈 값이 아니고, 양식에 맞아야 함(이름 확인 메세지가 출력되지 않으면 됨)
                //닉네임 검사 메세지가 "사용 가능한 닉네임 입니다."이면 됨
                //이메일 확인 메세지가 출력되지 않고 이메일이 빈 값이 아니면 됨
                String account = editText_id.getText().toString();
                String accountMessage = textView_id.getText().toString();
                String password = editText_password.getText().toString();
                String passwordMessage = textView_password.getText().toString();
                String passwordCheck = editText_passwordCheck.getText().toString();
                String passwordCheckMessage = textView_passwordCheck.getText().toString();
                String name = editText_name.getText().toString();
                String nameMessage = textView_name.getText().toString();
                String nickname = editText_nickname.getText().toString();
                String nicknameMessage = textView_nickname.getText().toString();
                String email = editText_email.getText().toString();
                String emailMessage = textView_email.getText().toString();

                //아이디 입력란이 공백인 경우
                if (account.trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                //아이디는 입력됐지만 양식이 어긋나거나 중복되는 아이디인 경우
                else if (!accountMessage.equals("사용 가능한 아이디 입니다.")) {
                    Toast.makeText(getApplicationContext(), "아이디를 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
                //비밀번호 입력란이 공백인 경우
                else if (password.trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                //비밀번호 확인 입력란이 공백인 경우
                else if (passwordCheck.trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "비밀번호 확인을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                //비밀번호의 자릿수가 맞지 않거나 불일치하는 경우
                else if (!passwordCheckMessage.equals("비밀번호 일치")) {
                    Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
                //이름 입력란이 공백인 경우
                else if (name.trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                //이름의 양식이 맞지 않는 경우
                else if (nameMessage.equals("잘못된 형식의 이름입니다.")) {
                    Toast.makeText(getApplicationContext(), "이름을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
                //이름이 30자를 넘어가는 경우
                else if (nameMessage.equals("이름은 최대 30자까지 입니다.")) {
                    Toast.makeText(getApplicationContext(), "이름을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
                //닉네임 입력란이 공백인 경우
                else if (nickname.trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                //닉네임이 중복되는 경우
                else if (!nicknameMessage.equals("사용 가능한 닉네임 입니다.")) {
                    Toast.makeText(getApplicationContext(), "닉네임을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
                //이메일 입력란이 빈 값인 경우
                else if (email.trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                //이메일이 양식에 맞지 않은 경우
                else if (emailMessage.equals("잘못된 형식의 이메일 입니다.")) {
                    Toast.makeText(getApplicationContext(), "이메일을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
                //모든 항목들이 회원가입을 하기에 적합하게 입력된 경우
                else {
                    //이메일 인증 번호를 생성하는 메소드
                    //param: 사용자가 입력한 이메일
                    String verificationNum = randomNum();

                    //이메일 전송을 위한 asynctask클래스 객체
                    SendMailThread sendMailThread = new SendMailThread();
                    //param1: 사용자가 입력한 인증용 이메일, param2: 이메일로 전송될 인증번호
                    sendMailThread.execute(email, verificationNum);

                    //인증페이지에서 인증에 성공하면 회원정보를 db에 저장시켜야 하기 때문에 회원정보 데이터를 intent를 통해서 그대로 옮겨준다.
                    Intent intent = new Intent(JoinActivity.this, VerificationAcitvity.class);
                    intent.putExtra("account", account);
                    intent.putExtra("password", password);
                    intent.putExtra("name", name);
                    intent.putExtra("nickname", nickname);
                    intent.putExtra("email", email);
                    intent.putExtra("verificationNum", verificationNum);
                    startActivity(intent);
                    finish();
                }
            }
        });

        //취소 버튼 클릭 리스너
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "회원가입이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                finish();

            }
        });

    }


    //입력된 아이디 값이 회원가입에 적합한지 검사하는 메소드
    private void checkAccount(String account) {
        //영어와 숫자로 된 아이디가 아닌 경우
        if (!Pattern.matches("^[a-zA-Z0-9]*$", account)) {
            textView_id.setText("아이디는 영어와 숫자로 조합해주세요.");
            textView_id.setTextColor(Color.parseColor("#FF6670"));
        }
        //아이디가 양식에 부합하는 경우
        else {
            //아이디의 길이가 4자 이상 16자 이하가 될 경우
            if (account.length() >= 4 && account.length() <= 16) {
                //서버에 접근해서 중복확인을 하는 asynctask 객체를 선언
                DoubleCheck doubleCheck = new DoubleCheck();
                //asynctask실행
                //param1: 접근하고자 하는 서버 url
                //param2: 입력된 아이디
                //param3: 어떤 항목의 중복검사를 하고 있는지를 가려내기 위한 코드
                doubleCheck.execute("http://" + IP_ADDRESS + "/checkaccount.php", account, "account");
            }
            //아이디의 길이가 4자 미만, 16자 초과인 경우
            else {
                textView_id.setText("아이디의 길이를 4자에서 16자 사이로 설정해주세요.");
                textView_id.setTextColor(Color.parseColor("#FF6670"));
            }

        }

    }

    //입력한 비밀번호 값이 회원가입에 적합한지 검사하는 메소드
    private void checkPassword(String password) {

        //비밀번호의 길이가 8자에서 16자 사이인 경우
        if (password.length() >= 8 && password.length() <= 16) {
            textView_password.setText("");
        }
        //비밀번호의 길이가 8~16자 사이가 아닌 경우
        else {
            textView_password.setText("비밀번호의 길이를 8자에서 16자 사이로 설정해주세요.");
            textView_password.setTextColor(Color.parseColor("#FF6670"));
        }

    }

    //입력한 비밀번호 확인 값이 회원가입에 적합한지 검사하는 메소드
    private void checkPasswordCheck(String password, String passwordCheck) {
        //비밀번호가 8자에서 16자 사이에 해당하는 경우
        if (password.length() >= 8 && password.length() <= 16) {
            //비밀번호 확인 값과 비밀번호 값이 같은 경우
            if (password.equals(passwordCheck)) {
                textView_passwordCheck.setText("비밀번호 일치");
                textView_passwordCheck.setTextColor(Color.parseColor("#77A88D"));
            }
            //비밀번호 값과 비밀번호 확인 값이 다른 경우
            else {
                textView_passwordCheck.setText("비밀번호 불일치");
                textView_passwordCheck.setTextColor(Color.parseColor("#FF6670"));
            }
        }
        //비밀번호가 8자에서 16자에 해당하지 않는 경우
        else {
            textView_passwordCheck.setText("");
        }
    }


    private void checkName(String name) {
        //이름이 양식에 맞지 않은 경우
        if (!Pattern.matches("^[가-힝]{2,}$", name)) {
            textView_name.setText("잘못된 형식의 이름입니다.");
            textView_name.setTextColor(Color.parseColor("#FF6670"));
        }
        //이름이 30자가 넘어가는 경우
        else if (name.length() > 30) {
            textView_name.setText("이름은 최대 30자까지 입니다.");
            textView_name.setTextColor(Color.parseColor("#FF6670"));
        }
        //이름이 양식에 맞는 경우
        else {
            textView_name.setText("");
        }

    }

    private void checkNickname(String nickname) {
        //닉네임의 길이가 1자에서 20자 사이가 되는 경우
        if (nickname.length() > 0 && nickname.length() <= 20) {
            //서버에 접근해서 중복확인을 하는 asynctask 객체를 선언
            DoubleCheck doubleCheck = new DoubleCheck();
            //asynctask실행
            //param1: 접근하고자 하는 서버 url
            //param2: 입력된 아이디
            //param3: 어떤 항목의 중복검사를 하고 있는지를 가려내기 위한 코드
            doubleCheck.execute("http://" + IP_ADDRESS + "/checknickname.php", nickname, "nickname");
        }
        //닉네임의 길이가 20자를 넘어가는 경우
        else {
            textView_nickname.setText("닉네임의 길이는 20자를 넘을 수 없습니다.");
            textView_nickname.setTextColor(Color.parseColor("#FF6670"));
        }
    }

    private void checkEmail(String email) {
        //입력된 이메일이 이메일 양식에 맞지 않은 경우
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textView_email.setText("잘못된 형식의 이메일 입니다.");
            textView_email.setTextColor(Color.parseColor("#FF6670"));
        }
        //입력된 이메일이 양식에 맞는 경우
        else {
            textView_email.setText("");
        }
    }

    //인증번호를 위한 난수를 생성하는 메소드
    private String randomNum() {
        //인증번호의 자리수
        int length = 6;
        String number = "";

        Random random = new Random();

        for (int i = 0; i < length; i++) {

            //0-9사이의 난수를 담는 변수
            String ran = Integer.toString(random.nextInt(10));

            //인증번호에 중복되는 난수가 없는 경우
            if (!number.contains(ran)) {
                //인증번호에 난수를 추가
                number += ran;
            }
            //인증번호에 중복되는 난수가 있느면
            else {
                //반복문을 이전 단계로 돌려서 다시 실행
                i -= 1;

            }

        }
        //생성된 인증번호 리턴
        return number;
    }

    //네이버로 인증 메일을 보내기 위한 메소드
    //param: 회원가입시 인증번호를 받기 위해 입력한 이메일
    //이 메소드는 smtp를 통해서 네트워크 통신을 하기 때문에 메인 스레드에서 바로 호출하면 android.os.NetworkOnMainThreadException에러가 뜬다.
    //반드시 asynctask와 같은 클래스에 넣어서 실행되도록 하자.
    public void sendEmail(String email, String verificationNum) {
        //메일을 전송하는 호스트(네이버)
        String host = "smtp.naver.com";
        //메일을 전송하는 수신자 아이디
        final String sender = "rlarpdlcm@naver.com";
        //메일을 전송하는 수신자의 비밀번호
        final String password = "rla933466r";


        //smtp서버 설정
        Properties properties = new Properties();
        //메일을 전송하는 호스트 설정
        properties.put("mail.smtp.host", host);
        //메일을 전송할 포트(네이버의 경우 465)
        properties.put("mail.smtp.port", 465);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.trust", "smtp.naver.com");


        properties.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, password);
            }
        });


        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            //인증메일을 받을 주소 추가
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

            //이메일의 제목 설정
            message.setSubject("SNS회원가입 인증번호");

            //메일의 내용 입력
            message.setText("인증번호: " + verificationNum);

            //이메일 전송
            Transport.send(message);


        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    //서버의 데이터와 비교하여 중복검사를 실시하는 스레드다.
    public class DoubleCheck extends AsyncTask<String, Void, String> {

        //어떤 항목에서 넘어온 값인지 가려내는 코드(account, nickname)
        String messageCode = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            messageCode = params[2];

            //통신할 서버의 url
            String serverURL = params[0];

            String postParameter = null;

            //아이디의 중복을 체크하는 경우
            if (messageCode.equals("account")) {

                String account = params[1];

                //post방식으로 전달할 키와 값을 설정
                postParameter = "account=" + account;

            }
            //닉네밍의 중복을 체크하는 경우
            else {

                String nickname = params[1];

                postParameter = "nickname=" + nickname;

            }


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
                Log.d(TAG, "InsertData: Error ", e);

                return null;
            }


        }

        //result에 올 수 있는 값은 4가지: "사용 가능한 아이디 입니다.", "이미 사용 중인 아이디 입니다.", "사용 가능한 닉네임 입니다.", "이미 사용 중인 닉네임 입니다."
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //서버와 통신이 잘 이루어졌고 항목 코드가 account인 경우
            if (result != null && messageCode.equals("account")) {

                if (result.equals("사용 가능한 아이디 입니다.")) {
                    textView_id.setText(result);
                    textView_id.setTextColor(Color.parseColor("#77A88D"));
                } else {
                    textView_id.setText(result);
                    textView_id.setTextColor(Color.parseColor("#FF6670"));
                }


            }
            //서버와 통신이 잘 이루어졌고 항목 코드가 nickname인 경우
            else if (result != null && messageCode.equals("nickname")) {

                if (result.equals("사용 가능한 닉네임 입니다.")) {
                    textView_nickname.setText(result);
                    textView_nickname.setTextColor(Color.parseColor("#77A88D"));
                } else {
                    textView_nickname.setText(result);
                    textView_nickname.setTextColor(Color.parseColor("#FF6670"));
                }
            }
        }
    }

    //처음에 메일 전송 메소드를 메인 스레드에서 실행했을 때 android.os.NetworkOnMainThreadException라는 에러가 떴다.
    //해당 에러는 네트워크 통신을 메인 스레드에서 실행했을 때 발생하는 문제다.
    //네트워크 통신은 메인 스레드가 아닌 서브 스레드에서 실행시켜야 한다는 것을 명심하자.
    //해당 클래스는 메일 전송을 서브스레드에서 실행시키기 위한 asynctask 클래스다.
    private class SendMailThread extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //사용자가 입력한 email
            String email = params[0];
            //본인인증 번호
            String verificationNum = params[1];
            sendEmail(email, verificationNum);
            return null;
        }
    }
}
