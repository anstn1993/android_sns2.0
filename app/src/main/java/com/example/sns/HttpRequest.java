package com.example.sns;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.sns.JoinActivity.IP_ADDRESS;

/*서버로 데이터를 전송하거나 서버로부터 데이터를 가져오는 역할을 담당하는 클래드*/
public class HttpRequest extends AsyncTask<String, Void, String> {

    String requestMethod;//서버와 통신을 하는 방식 GET, POST...
    String requestBody;//서버로 전송할 json스트링
    String requestAPI;//통신하고자 하는 api(php파일)

    //서버통신이 끝난 후 onPostExecute()메소드가 호출될 때 콜백될 메소드를 가진 interface
    public interface OnHttpResponseListener {
        public void onHttpResponse(String result);
    }

    OnHttpResponseListener mListener;

    //클래스 생성자
    public HttpRequest(String requestMethod, String requestBody, String requestAPI, OnHttpResponseListener onHttpResponseListener) {
        this.requestMethod = requestMethod;
        this.requestBody = requestBody;
        this.requestAPI = requestAPI;
        mListener = onHttpResponseListener;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
            try {

                URL url = new URL("http://" + IP_ADDRESS + "/" + requestAPI);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod(requestMethod);//통신 방식 설정
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Content-Type", "application/json");//데이터 형식을 json형식으로 설정
                httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");//캐릭터셋 utf-8
                httpURLConnection.setReadTimeout(0);//데이터를 읽어오는 데 걸리는 시간 제한(무제한)
                httpURLConnection.setConnectTimeout(0);//서버와 통신에 시도해서 성공하는 데 걸리는 시간 제한(무제한)

                httpURLConnection.connect();//연결 시도

                OutputStream outputStream = httpURLConnection.getOutputStream();//서버로 json스트링을 보낼 스트림 새성
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);//버퍼를 통해서 줄단위로 보낸다.
                bufferedOutputStream.write(requestBody.getBytes("UTF-8"));//캐릭터셋을 utf-8로 설정해서 스트링을 바이트 배열에 모두 담아주고 쓴다.
                bufferedOutputStream.flush();//스트림을 비워준다. 이 메소드를 호출해야 실제로 데이터가 넘어간다.
                bufferedOutputStream.close();//스트림 종료

                InputStream inputStream;//서버의 Response 데이터를 받기 위한 스트림
                int responseCode = httpURLConnection.getResponseCode();//통신의 결과

                if (responseCode == httpURLConnection.HTTP_OK) {//통신에 성공한 경우
                    inputStream = httpURLConnection.getInputStream();
                } else { // 통신에 실패한 경우
                    inputStream = httpURLConnection.getErrorStream();
                }

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));//버퍼를 통해서 라인단위로 데이터를 받아온다
                StringBuilder sb = new StringBuilder();//서버에서 넘어오는 데이터를 쌓아주기 위한 빌더
                String line = null;//서버에서 라인별로 넘어오는 데이터를 담을 변수

                while ((line = bufferedReader.readLine()) != null) {//라인별로 읽어온 데이터가 존재할때까지 반복문을 돌려서 빌더에 데이터를 넣어준다.
                    sb.append(line);
                }

                bufferedReader.close();//스트림 종료
                httpURLConnection.disconnect();//http통신 종료
                return sb.toString().trim();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
    }

    @Override
    protected void onPostExecute(String result) {//result: json스트링
        super.onPostExecute(result);

        mListener.onHttpResponse(result);
    }

}
