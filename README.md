# sns2.0
My second android sns application. This application is progressed version compared with [android sns 1.0 application](https://github.com/anstn1993/android_sns1.0). 

해당 프로잭트는 [android sns 1.0 application](https://github.com/anstn1993/android_sns1.0) 프로잭트보다 더 진화된 SNS 애플리케이션 입니다. 서버를 도입하여 다른 사용자들과 소통할 수 있도록 구현했습니다.

[어플 시연 영상](https://www.youtube.com/watch?v=ZNHzLkDCLZI&t=5s)

## 목차

- [기술 스택](#기술-스택)
- [주요 기능](#주요-기능)
  - [회원가입 및 자동 로그인](#회원가입-및-자동-로그인)
  - [메인 페이지](#메인-페이지)
  - [마이 페이지](#마이-페이지)
  - [게시물](#게시물)
  - [댓글/대댓글](#댓글/대댓글)
  - [좋아요](#좋아요)
  - [팔로우](#팔로우)
  - [검색](#검색)
  - [채팅](#채팅)
  - [영상통화](#영상통화)
  - [알림](#알림)


## 기술 스택

**언어:** java, php, javascript

**프로토콜:** HTTP, TCP/IP, WebRTC

**서버 호스팅:** AWS EC2

**서버 운영체제:** Ubuntu

**서버 프로그램:** Apache2, [socket server](https://github.com/anstn1993/socket_server), [signaling server(node.js socket.io)](https://github.com/anstn1993/signaling_server)

**데이터베이스:** MySQL

**라이브러리:** Glide, Retrofit2, Gson, ExoPlayer2, FCM, TedImagePicker, TedBottomPicker, TedPermission...


## 주요 기능

### 회원가입 및 자동 로그인

<div>
<img src="https://user-images.githubusercontent.com/56672937/96290908-4eafab00-1022-11eb-9e42-1c1d3f52b1ed.gif" width="250" height="400"/>
  
<img src="https://user-images.githubusercontent.com/56672937/96291396-f0cf9300-1022-11eb-9d5f-85f9019faccb.gif" width="250" height="400"/>
</div>

- 회원가입시 항목의 유효성을 실시간으로 검사하여 피드백 제공
- 이메일 인증 요구
- 한 번 로그인을 하면 자동 로그인

### 메인 페이지

<div>
<img src="https://user-images.githubusercontent.com/56672937/96294007-aa7c3300-1026-11eb-8190-826f83a15629.gif" width="250" height="400"/>

<img src="https://user-images.githubusercontent.com/56672937/96294890-04c9c380-1028-11eb-8400-383236249088.gif" width="250" height="400"/>
</div>

- 사용자들이 업로드한 게시물들을 볼 수 있는 페이지
- 게시물은 이미지나 동영상으로 구성
- 동영상 게시물의 경우 화면 터치를 통한 소리 on/off 가능
- 페이징 처리를 하여 최하단으로 내려가면 다음 게시물 로딩.

### 마이 페이지

- 개별 사용자의 정보를 모아둔 페이지

<img src="https://user-images.githubusercontent.com/56672937/96296348-2035ce00-102a-11eb-994f-a74628c19685.gif" width="250" height="400"/>

- 사용자가 업로드한 게시물에 대해 격자형, 수직 나열형 보기 방식 제공

<img src="https://user-images.githubusercontent.com/56672937/96297283-98e95a00-102b-11eb-8fbc-5911a1406bca.gif" width="250" height="400"/>

- 팔로잉/팔로워를 확인할 수 있으며 팔로잉 유저나 팔로워의 페이지 방문 가능

<img src="https://user-images.githubusercontent.com/56672937/96298601-b15a7400-102d-11eb-8c49-df91c66c476a.gif" width="250" height="400"/>

- 프로필 수정을 통해 프로필 사진, 닉네임, 이름, 소개와 같은 항목 수정 가능

### 게시물

<div>
<img src="https://user-images.githubusercontent.com/56672937/96299812-92f57800-102f-11eb-8c07-53b6571bce72.gif" width="250" height="400"/>
<img src="https://user-images.githubusercontent.com/56672937/96301039-8a9e3c80-1031-11eb-9472-ecf8c3ece39b.gif" width="250" height="400"/>
</div>

- 이미지 업로드의 경우 이미지 선택 후에도 이미지 순서나 구성 편집 가능
- 장소 등록 가능
- 동영상 업로드의 경우 동영상 선택 후에도 다른 동영상 선택 가능
- 게시물 수정/삭제 가능

### 댓글/대댓글

<div>
<img src="https://user-images.githubusercontent.com/56672937/96302904-81fb3580-1034-11eb-85e5-a289aef9a7f7.gif" width="250" height="400"/>
<img src="https://user-images.githubusercontent.com/56672937/96303395-4614a000-1035-11eb-9156-9f17174a9346.gif" width="250" height="400"/>
</div>

- 게시물에 댓글 추가/수정/삭제
- 게시물에 대댓글 추가/수정/삭제

### 좋아요

<img src="https://user-images.githubusercontent.com/56672937/96307243-46fd0000-103c-11eb-956e-075737ce1c93.gif"/>

- 좋아요를 통해 게시물에 대한 호감 표시 기능
- 좋아요 리스트에서 좋아요를 누른 사용자들을 확인하고 페이지에 방문 가능

### 팔로우

<img src="https://user-images.githubusercontent.com/56672937/96309829-081d7900-1041-11eb-9a18-2c8e4b3fa687.gif"/>

<img src="https://user-images.githubusercontent.com/56672937/96309845-1075b400-1041-11eb-9364-d2e771512606.gif"/>

- 다른 사용자 팔로우/언팔로우 기능
- 마이페이지에서 팔로잉, 팔로워 목록 확인 

### 해시태그

<img src="https://user-images.githubusercontent.com/56672937/96310459-5717de00-1042-11eb-8207-8fef92839af1.gif" width="250" height="400"/>

- 특정 키워드의 해시태그가 달린 게시물들만 모아서 조회 가능

### 검색

- 사용자, 태그, 장소를 필터로 검색 가능
- 검색어가 일부 문자열만 포함해도 모두 조회

<img src="https://user-images.githubusercontent.com/56672937/96311126-c17d4e00-1043-11eb-83c2-a1341f2f995d.gif" width="250" height="400"/>

- 사용자 검색의 경우 이름이나 닉네임으로 검색

<img src="https://user-images.githubusercontent.com/56672937/96311462-7d3e7d80-1044-11eb-8911-66c34fb5486d.gif" width="250" height="400"/>

- 태그 검색을 통해서 해당 태그가 달린 게시물 조회

<img src="https://user-images.githubusercontent.com/56672937/96312182-0efaba80-1046-11eb-9b62-830a1c03cefa.gif" width="250" height="400"/>

- 장소 검색을 통해 검색된 장소에서 업로드된 게시물 조회
- '주변 게시물'의 경우 해당 장소의 주변에서 업로드된 게시물을 모아서 출력(주변 거리 설정 가능)

### 채팅

![1-n_chat](https://user-images.githubusercontent.com/56672937/96332803-26b35c80-10a1-11eb-958e-2e7e59a26031.gif)
- 1:1채팅, 단체 채팅 가능
- 전송 가능한 메세지 컨텐츠: 텍스트, 이미지, 동영상
- 채팅방 생성 후 사용자 초대 가능 
- 메세지를 확인하지 않은 인원수 표시 

![content_drawer](https://user-images.githubusercontent.com/56672937/96333013-69296900-10a2-11eb-8dae-0440a2ea839f.gif)
- 채팅방에서 공유된 이미지, 동영상들을 모아서 볼 수 있는 서랍 기능
- 채팅방 서랍에서 복수의 이미지와 동영상 다운로드 가능

### 영상통화

![face_call](https://user-images.githubusercontent.com/56672937/96333311-0f29a300-10a4-11eb-9ebb-9d0aa9a509f0.gif)
- 전/후면 카메라의 전환 가능
- 앱에서 벗어나는 경우 상대방에게 피드백 전달과 함께 화면 스트리밍 중단(상대방이 돌아오면 다시 스트리밍 시작)
- 음소거 기능

### 알림

![follow_notification](https://user-images.githubusercontent.com/56672937/96333454-f40b6300-10a4-11eb-8eb5-9560e094e9fb.gif)

![chat_notification](https://user-images.githubusercontent.com/56672937/96333598-db4f7d00-10a5-11eb-88c2-331169e83d62.gif)
- 알림 종류: 좋아요, 팔로우, 댓글(대댓글), 채팅
- 어플을 실행중일 때 알림이 오는 경우 화면에 표시
- 어플을 실행중이지 않을 때 알림이 오는 경우 푸시 알림이 오게 되고 해당 알림 클릭시 알림을 유발한 컨텐츠 확인 가능
