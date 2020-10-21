package com.example.sns.facecall.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.example.sns.chat.service.ChatService;
import com.example.sns.R;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class CallActivity extends AppCompatActivity {

    private final String TAG = "CallActivity";
    private final String SIGNALLING_URL = "http://13.124.105.47:7000";//시그널링 서버 url
    private final String VIDEOTRACK_ID = "videoTrack";//비디오 트랙 id
    private final String AUDIOTRACK_ID = "audioTrack";//오디오 트랙 id
    private final String LOCAL_STREAM_ID = "localStream";//로컬 비디오 스트림 id
    private final String SDP_MID = "sdpMid";
    private final String SDP_M_LINE_INDEX = "sdpMLineIndex";
    private final String SDP = "sdp";
    //소켓 io의 리스너 식별자
    private final String CREATEOFFER = "createoffer";
    private final String OFFER = "offer";
    private final String ANSWER = "answer";
    private final String CANDIDATE = "candidate";
    private final String REQUIREROOMNAME = "requireroomname";
    private final String SENDROOMNAME = "sendroomname";
    private final String ENDCALL = "endcall";
    private final String SWITCHCAMERA = "switchcamera";
    private final String NOTIFYDEPART = "notifydepart";
    private final String NOTIFYGETBACK = "notifygetback";
    //webRTC를 위해서 필요한 객체
    private PeerConnection peerConnection;//직접적으로 peer간의 통신을 담당하는 객체
    private PeerConnectionFactory peerConnectionFactory;//peerConnection setting을 담당, 소스, 트랙, 스트림 모두 이걸로 추가
    private VideoCapturerAndroid videoCapturerAndroid;//단말기의 카메라 장치 제어 객체
    private VideoSource localVideoSource;//카메라에서 나오는 영상 소스
    private AudioSource localAudioSource;//오디오에서 나오는 음성 소스
    private VideoTrack localVideoTrack;//비디오 소스를 담을 트랙
    private AudioTrack localAudioTrack;//오디오 소스를 담을 트랙
    private MediaStream localMediaStream;//통신을 하면서 비디오와 오디오의 소스를 전달하기 위한 스트림
    private VideoRenderer.Callbacks localVideoRenderer;//자신의 비디오 스트림을 화면에 표시해줄 렌더러
    private VideoRenderer.Callbacks remoteVideoRenderer;//상대방의 비디오 스트림을 화면에 표시해줄 렌더러
    private VideoRendererGui videoRendererGui;//렌더러의 환경 설정 객체
    private Socket socket;//시그널링 서버와 통신하기 위한 클라이언트 소켓

    //각종 뷰 객체
    private GLSurfaceView videoView;//각 peer의 영상 스트림의 gui를 표시할 비디오 뷰
    private ImageButton ib_endCall, ib_switchCam, ib_muteControl;//통화 종료 버튼, 카메라 전환 버튼
    private ConstraintLayout screenContainer;
    private ConstraintLayout buttonContainer;


    private boolean screenOn;//해당 액티비티에 접근했을 때 화면이 꺼진 상태였는지 켜진 상태였는지 파악하는 boolean

    //화면이 꺼진 상태에서 접근했을 때 화면을 깨우기 위한 객체들
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    private String roomName;//시그널링 서버에서 생성될 room
    private String sender;//발신자
    private String receiver;//수신자

    private final long CLICK_INTERVAL = 1000;
    private long lastClickTime = 0;

    private boolean isCameraFront = true;//현재 카메라가 전면인지 후면인지 판단하는 boolean
    private boolean isRemoteCameraFront = true;//현재 상대방 카메라가 전면인지 후면인지 판단하는 boolean
    private boolean isMuted = false;//오디오 무음상태
    private boolean createOffer = false;//자신이 offer를 제공하는 쪽인지 받는 쪽인지 판별하는 boolean

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate호출");
        setContentView(R.layout.activity_call);
        //뷰 초기화
        ib_switchCam = findViewById(R.id.imagebutton_switchcamera);
        ib_endCall = findViewById(R.id.imagebutton_endcall);
        ib_muteControl = findViewById(R.id.imagebutton_controlmute);
        screenContainer = findViewById(R.id.screen_container);
        buttonContainer = findViewById(R.id.button_container);

        roomName = getIntent().getStringExtra("roomName");
        sender = getIntent().getStringExtra("sender");
        receiver = getIntent().getStringExtra("receiver");

        videoView = findViewById(R.id.surfaceview);
        videoRendererGui.setView(videoView, null);

        if (getIntent() != null) {
            screenOn = getIntent().getBooleanExtra("screenOn", false);
            if (!screenOn) {//화면 꺼짐 상태에서 접근한 경우
                powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);//파워 매니저
                wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "WAKE_LOCK");
                wakeLock.acquire();//화면 깨우기
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);//잠금화면위에 액티비티를 쌓아준다.
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);//잠금 해제
            }
        }


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//영상통화 화면에 진입하면 화면이 꺼지지 않게 설정

        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);//오디오 매니저를 통해서 영상통화시 오디오 설정 가능하게
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);//영상통화와 같은 양방향 통신상에서의 오디오 모드
        audioManager.setSpeakerphoneOn(true);//스피커폰 가능하게 설정

        PeerConnectionFactory.initializeAndroidGlobals(
                this,//context
                true,//audio enabled
                true,//video enabled
                true,//hardware acceleration enabled
                null//renderer egl context
        );

        peerConnectionFactory = new PeerConnectionFactory();
        //장치의 카메라를 사용할 수 있게 해주는 객체로 create의 첫번째 인자로 최초 접속시 카메라를 전면으로 할지, 후면으로 할지 설정할 수 있다.
        videoCapturerAndroid = VideoCapturerAndroid.create(VideoCapturerAndroid.getNameOfFrontFacingDevice(), null);
        //videocapturer를 이용해서 비디오 소스를 생성할 수 있다. MediaConstraints객체는 비디오나 오디오의 각종 설정을 담당한다.
        localVideoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid, new MediaConstraints());
        //비디오 소스를 통해서 비디오 트랙을 만든다.
        localVideoTrack = peerConnectionFactory.createVideoTrack(VIDEOTRACK_ID, localVideoSource);
        localVideoTrack.setEnabled(true);

        //오디오 소스 생성
        localAudioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        //오디오 소스를 통해서 트랙 생성
        localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIOTRACK_ID, localAudioSource);
        localAudioTrack.setEnabled(true);

        remoteVideoRenderer = VideoRendererGui.create(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
        localVideoRenderer = VideoRendererGui.create(70, 0, 30, 30, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);


        //미디어 스트림을 생성해서 비디오트랙과 오디오트랙을 추가해준다. 이후 peerConnection을 할 때 이 스트림을 추가해서 상대방과 데이터를 주고 받는다
        localMediaStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID);
        localMediaStream.addTrack(localVideoTrack);
        localMediaStream.addTrack(localAudioTrack);
        Log.d(TAG, "localMediaStream생성");
        localMediaStream.videoTracks.getFirst().addRenderer(new VideoRenderer(localVideoRenderer));


        //카메라 전환 버튼 클릭 리스너
        ib_switchCam.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                //카메라 전환 버튼을 클릭한지 1초가 지나야 전환 가능
                                                if (SystemClock.elapsedRealtime() - lastClickTime > CLICK_INTERVAL) {
                                                    videoCapturerAndroid.switchCamera(new Runnable() {
                                                                                          @Override
                                                                                          public void run() {

                                                                                              JSONObject jsonObject = null;
                                                                                              if (isCameraFront) {//전면 카메라였던 경우(후면 카메라로 전환하는 경우)
                                                                                                  try {
                                                                                                      VideoRendererGui.update(localVideoRenderer, 70, 0, 30, 30, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
                                                                                                      jsonObject = new JSONObject();
                                                                                                      jsonObject.put("roomName", roomName);
                                                                                                      jsonObject.put("orientation", "back");
                                                                                                  } catch (Exception e) {
                                                                                                      e.printStackTrace();
                                                                                                  }
                                                                                                  isCameraFront = false;
                                                                                              } else {//후면카메라였던 경우(전면 카메라로 전환하는 경우)
                                                                                                  try {
                                                                                                      VideoRendererGui.update(localVideoRenderer, 70, 0, 30, 30, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
                                                                                                      jsonObject = new JSONObject();
                                                                                                      jsonObject.put("roomName", roomName);
                                                                                                      jsonObject.put("orientation", "front");
                                                                                                  } catch (Exception e) {
                                                                                                      e.printStackTrace();
                                                                                                  }
                                                                                                  isCameraFront = true;
                                                                                              }
                                                                                              socket.emit(SWITCHCAMERA, jsonObject);
                                                                                          }
                                                                                      }
                                                    );

                                                    lastClickTime = SystemClock.elapsedRealtime();
                                                }
                                            }
                                        }
        );

        connectToPeer();//시그널링 서버를 통해서 remote peer와 connect

        //통화 종료 버튼 클릭 리스너
        ib_endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //통화중에 앱을 종료시키면 통화를 종료시킨다.
                if (socket.connected()) {
                    JSONObject objForSignaling = new JSONObject();
                    try {
                        objForSignaling.put("roomName", roomName);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit(ENDCALL, objForSignaling);
                    socket.disconnect();
                    JSONObject objForSocket = new JSONObject();
                    try {
                        objForSocket.put("type", "disconnectFaceChat");
                        objForSocket.put("sender", sender);
                        objForSocket.put("receiver", receiver);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //소켓 서버에도 통화 종료 사실을 전달해준다.
                    Message message = ChatService.handler.obtainMessage();
                    message.obj = objForSocket.toString();
                    ChatService.handler.sendMessage(message);
                }
                if (!screenOn && wakeLock.isHeld()) {
                    wakeLock.release();
                }

                peerConnection.removeStream(localMediaStream);//스트림도 삭제해준다.
                finish();//액티비티 종료
            }
        });

        //마이크 뮤트 버튼 클릭 리스너
        ib_muteControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMuted) {//마이크 mute상태인 경우(mute를 취소하는 경우)
                    localAudioTrack.setEnabled(true);//오디오 트랙 활성화
                    ib_muteControl.setSelected(false);
                    isMuted = false;
                } else {//마이크 mute상태가 아닌 경우(mute를 활성화하는 경우)
                    localAudioTrack.setEnabled(false);//오디오 트랙 비활성화
                    ib_muteControl.setSelected(true);
                    isMuted = true;
                }
            }
        });


        buttonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonContainer.animate().alpha(0.0f).setDuration(500).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        buttonContainer.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

        screenContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonContainer.animate().alpha(1.0f).setDuration(500).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        buttonContainer.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonContainer.animate().alpha(1.0f).setDuration(500).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        buttonContainer.setVisibility(View.VISIBLE);
                    }
                });
            }
        });


    }

    private void connectToPeer() {
        if (peerConnection != null) return;

        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        //ICE서버로 STUN서버를 등록하여 자신의 공인 IP를 확인하고 통신 가능한 상태인지 확인
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        peerConnection = peerConnectionFactory.createPeerConnection(
                iceServers,
                new MediaConstraints(),
                peerConnectionObserver
        );


        peerConnection.addStream(localMediaStream);//local 미디어 스트림 추가

        try {
            socket = IO.socket(SIGNALLING_URL, new IO.Options());
            //소켓 리스너 설정
            socket
                    //시그널링 서버로부터 방 이름 요청이 이 리스너로 들어온다.
                    //call method의 param은 시그널링 서버로부터 넘어온 json개체다.
                    .on(REQUIREROOMNAME, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("roomName", roomName);
                                socket.emit(SENDROOMNAME, jsonObject);//방 이름을 시그널링 서버로 전송
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    })

                    //수신자가 시그널링 서버에 접속하면 offer를 제공하라는 요청이 이 리스너로 들어온다.
                    .on(CREATEOFFER, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            createOffer = true;//수신자에게 offer제공을 요청 받았으니까 자신이 Offer를 하는 쪽
                            Log.i(TAG, "createOffer true");

                            //이 메소드를 실행하면 sdpObserver의 onCreateSuccess메소드가 콜백되어서 localdescription set
                            peerConnection.createOffer(sdpObserver, new MediaConstraints());

                        }
                    })
                    //발신자가 offer를 제공하면 이 리스너로 들어온다.(수신자가 이 리스너를 타게 된다)
                    .on(OFFER, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.i(TAG, "received offer");
                            try {
                                JSONObject jsonObject = (JSONObject) args[0];
                                String remoteOrientation = jsonObject.getString("orientation");
                                if (remoteOrientation.equals("front")) {
                                    isRemoteCameraFront = true;
                                } else {
                                    isRemoteCameraFront = false;
                                }

                                SessionDescription sdp = new SessionDescription(SessionDescription.Type.OFFER, jsonObject.getString(SDP));
                                Log.i(TAG, "remote description set");
                                peerConnection.setRemoteDescription(sdpObserver, sdp);//remote sdp from sender set
                                peerConnection.createAnswer(sdpObserver, new MediaConstraints());//answer to sender
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    })
                    //수신자가 answer를 발신자에게 보내면 이 리스너로 들어온다.(발신자가 이 리스너를 타게 된다)
                    .on(ANSWER, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.i(TAG, "received answer");
                            try {
                                JSONObject obj = (JSONObject) args[0];
                                String remoteOrientation = obj.getString("orientation");
                                if (remoteOrientation.equals("front")) {
                                    isRemoteCameraFront = true;
                                } else {
                                    isRemoteCameraFront = false;
                                }

                                //sdp객체 초기화
                                SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER,
                                        obj.getString(SDP));
                                Log.i(TAG, "remote description set");
                                peerConnection.setRemoteDescription(sdpObserver, sdp);//remote sdp from receiver set

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    //상대방이 ICE candidate를 보내오면 이 리스너로 들어온다.
                    .on(CANDIDATE, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.i(TAG, "onCANDIDATE호출");
                            try {
                                JSONObject obj = (JSONObject) args[0];
                                //ice candidate를 추가한다. 그럼 그 중 가장 최적의 경로를 매칭해서 상대방과 연결된다.
                                peerConnection.addIceCandidate(new IceCandidate(obj.getString(SDP_MID),
                                        obj.getInt(SDP_M_LINE_INDEX),
                                        obj.getString(SDP)));
                                peerConnection.updateIce(iceServers, new MediaConstraints());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    //상대방이 카메라를 전환하면 이 리스너로 들어온다.
                    .on(SWITCHCAMERA, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            try {
                                JSONObject jsonObject = (JSONObject) args[0];
                                String orientation = jsonObject.getString("orientation");
                                if ("front".equals(orientation)) {
                                    isRemoteCameraFront = true;
                                    VideoRendererGui.update(remoteVideoRenderer, 0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
                                } else {
                                    isRemoteCameraFront = false;
                                    VideoRendererGui.update(remoteVideoRenderer, 0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    //상대방이 화면에서 벗어났을 경우 이 리스너로 들어온다
                    .on(NOTIFYDEPART, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    screenContainer.animate().alpha(1.0f).setDuration(500).withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            screenContainer.setVisibility(View.VISIBLE);
                                        }
                                    }).start();
                                }
                            });

//
                        }
                    })
                    //상대방이 화면으로 돌아오는 경우 이 리스너로 들어온다
                    .on(NOTIFYGETBACK, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    screenContainer.animate().alpha(0.0f).setDuration(500).withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            screenContainer.setVisibility(View.INVISIBLE);
                                        }
                                    }).start();
                                }
                            });
                        }
                    })
                    //상대방이 통화를 종료하는 경우 이 리스너로 들어온다
                    .on(ENDCALL, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            if (socket.connected()) {
                                socket.disconnect();
                                if (!screenOn && wakeLock.isHeld()) {
                                    wakeLock.release();
                                }
                            }
                            peerConnection.removeStream(localMediaStream);//스트림도 삭제해준다.
                            finish();//액티비티 종료
                        }
                    });

            socket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.i(TAG, "서버 socket 종료");
            if (socket.connected()) {
                socket.disconnect();//소켓 연결 종료
            }
        }

    }//end of connectToPeer

    //sdp의 offer, set description과 같은 상태를 체크하기 위한 인터페이스
    SdpObserver sdpObserver = new SdpObserver() {
        //offer가 성공적으로 create되면 호출되는 메소드
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.i(TAG, "SdpObserver onCreateSuccess호출");
            //sdp local description set
            peerConnection.setLocalDescription(sdpObserver, sessionDescription);
            Log.i(TAG, "local description set");

            try {
                JSONObject obj = new JSONObject();
                obj.put("roomName", roomName);
                if (isCameraFront) {
                    obj.put("orientation", "front");
                } else {
                    obj.put("orientation", "back");
                }
                obj.put(SDP, sessionDescription.description);
                if (createOffer) {//자신이 통화를 걸면 createOffer가 true로 변환
                    socket.emit(OFFER, obj);//offer를 시그널링 서버로 전송
                    createOffer = false;
                } else {//자신이 통화를 받는 경우
                    socket.emit(ANSWER, obj);//answer를 시그널링 서버로 전송
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //icd candidate CHECKING이 끝나면 호출
        @Override
        public void onSetSuccess() {
            Log.i(TAG, "SdpObserver onSetSuccess호출");
        }

        @Override
        public void onCreateFailure(String s) {
            Log.i(TAG, "SdpObserver onCreateFailure호출");
        }

        @Override
        public void onSetFailure(String s) {
            Log.i(TAG, "SdpObserver onSetFailure호출");
        }


    };//end of SdpObserver

    PeerConnection.Observer peerConnectionObserver = new PeerConnection.Observer() {
        //list of signalingState
        //-HAVE_LOCAL_OFFER: offer가 create되는 순간 호출
        //-STABLE: 발신자의 경우 answer를 받고 remote destciption set까지 완료되면 호출 -> onAddStream 호출
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.i(TAG, "peerConnectionObserver onSignalingChange호출");
            Log.d(TAG, "onSignalingChange:" + signalingState.toString());
        }

        //ice connection에 변화가 생길때 호출, 예를 들면 연결이 완료되거나 상대방이 앱을 종료한다거나 통화를 종료한다거나 등등...
        //list of ice connection state
        //-CHECKING: 상대방의 스트림이 추가되면(onAddStream) candidate 체킹이 일어남
        //-CONNECTED: sdp 셋이 끝나면(sdp Observer의 onSetSuccess) 호출
        //-COMPLETED: 상대방과 연결되었을 때
        //-DISCONNECTED: 상대방과의 연결이 끊겼을 때
        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.d(TAG, "onIceConnectionChange:" + iceConnectionState.toString());
            if (iceConnectionState.toString().equals(iceConnectionState.DISCONNECTED.toString())) {
                //통화중에 앱을 종료시키면 통화를 종료시킨다.
                finishCall();
                finish();//액티비티 종료
            }
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Log.i(TAG, "peerConnectionObserver onIceConnectionReceivingChange호출");
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Log.i(TAG, "peerConnectionObserver onIceGatheringChange호출");
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Log.i(TAG, "peerConnectionObserver onIceCandidate호출");
            //각 peer가 sdp description set을 끝내면 ICE candidate를 주고 받는다.
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("roomName", roomName);
                jsonObject.put(SDP_MID, iceCandidate.sdpMid);
                jsonObject.put(SDP_M_LINE_INDEX, iceCandidate.sdpMLineIndex);
                jsonObject.put(SDP, iceCandidate.sdp);
                socket.emit(CANDIDATE, jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //signaling state가 STABLE이 되면 호출
        @Override
        public void onAddStream(MediaStream mediaStream) {
            Log.i(TAG, "peerConnectionObserver onAddStream호출");
            mediaStream.videoTracks.getFirst().addRenderer(new VideoRenderer(remoteVideoRenderer));
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.i(TAG, "peerConnectionObserver onRemoveStream호출");

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.i(TAG, "peerConnectionObserver onDataChannel호출");

        }

        @Override
        public void onRenegotiationNeeded() {
            Log.i(TAG, "peerConnectionObserver onRenegotiationNeeded호출");

        }


    };//end of peerConnectionObserver

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart호출");
        if (socket.connected()) {
            try {
                socket.emit(NOTIFYGETBACK, new JSONObject().put("roomName", roomName));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        localVideoSource.restart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume호출");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause호출");
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop호출");
        if (socket.connected()) {
            try {
                socket.emit(NOTIFYDEPART, new JSONObject().put("roomName", roomName));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        localVideoSource.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //통화중에 앱을 종료시키면 통화를 종료시킨다.
        finishCall();
    }

    //통화 종료 버튼을 누르거나, 앱을 종료할 때 영상통화를 종료하기 위한 로직을 수행하는 메서드
    private void finishCall() {
        if (socket.connected()) {
            JSONObject objForSignaling = new JSONObject();
            try {
                objForSignaling.put("roomName", roomName);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit(ENDCALL, objForSignaling);//상대방에게 연결 종료 사실을 전달
            socket.disconnect();
            JSONObject objForSocket = new JSONObject();
            try {
                objForSocket.put("type", "disconnect");
                objForSocket.put("sender", sender);
                objForSocket.put("receiver", receiver);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //소켓 서버에도 통화 종료 사실을 전달해준다.
            Message message = ChatService.handler.obtainMessage();
            message.obj = objForSocket.toString();
            ChatService.handler.sendMessage(message);
        }
        if (!screenOn && wakeLock.isHeld()) {
            wakeLock.release();
        }
        peerConnection.removeStream(localMediaStream);//스트림도 삭제해준다.
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        //영상통화중에는 통화 종료 버튼을 눌러서만 나갈 수 있게 설정
    }
}
