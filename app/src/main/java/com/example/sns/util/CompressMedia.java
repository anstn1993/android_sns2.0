package com.example.sns.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

public class CompressMedia {
    private static final String TAG = CompressMedia.class.getSimpleName();
    private static final boolean VERBOSE = true; // lots of logging

    /**
     * 다음 버퍼가 가능할 때까지 기다릴 시간
     */
    private static final int TIMEOUT_USEC = 1;

    /**
     * 압축된 파일을 저장할 경로
     */
    private static final File OUTPUT_FILENAME_DIR = Environment.getExternalStorageDirectory();//압축한 파일의 경로
    private String SourceUri;//압축 전의 원본 파일 경로
    private String outputFileName = null;//압축한 파일의 이름
    // 비디오 인코더 설정 값
    private static final String OUTPUT_VIDEO_MIME_TYPE = "video/hevc"; // H.265 Advanced Video Coding
    private static final int OUTPUT_VIDEO_BIT_RATE = 800000; // 800kbps
    private static final int OUTPUT_VIDEO_FRAME_RATE = 20; // 20fps
    private static final int OUTPUT_VIDEO_IFRAME_INTERVAL = 1; // 1 seconds between I-frames
    private static final int OUTPUT_VIDEO_COLOR_FORMAT =
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;

    // 오디오 인코더 설정 값(채널 수와 샘플 레이트는 반드시 원본 파일의 값과 일치해야 한다. )
    private static final String OUTPUT_AUDIO_MIME_TYPE = "audio/mp4a-latm"; // Advanced Audio Coding
    private static final int OUTPUT_AUDIO_CHANNEL_COUNT = 2; // Must match the input stream.
    private static final int OUTPUT_AUDIO_BIT_RATE = 64 * 1024;
    private static final int OUTPUT_AUDIO_AAC_PROFILE =
            MediaCodecInfo.CodecProfileLevel.AACObjectHE;
    private static final int OUTPUT_AUDIO_SAMPLE_RATE_HZ = 44100; // Must match the input stream.

    /**
     * Used for editing the frames.
     *
     * <p>Swaps green and blue channels by storing an RBGA color in an RGBA buffer.
     */
    //openGL ES2의 색상 연산을 담당하는 쉐이더 언더
    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord).rbga;\n" +
                    "}\n";

    /**
     * Whether to copy the video from the test video.
     */
    private boolean mCopyVideo;
    /**
     * Whether to copy the audio from the test video.
     */
    private boolean mCopyAudio;
    /**
     * Width of the output frames.
     */
    private int mWidth = -1;
    /**
     * Height of the output frames.
     */
    private int mHeight = -1;


    /**
     * 압축된 파일이 저장될 최종 경로
     */
    private String mOutputFile;

    private Context context;
    private ProgressDialog progressDialog;

    //생성자
    public CompressMedia(String uri, String fileName, Context context, int width, int height) {
        this.SourceUri = uri;
        this.outputFileName = fileName;
        this.context = context;
        this.mWidth = width;
        this.mHeight = height;
    }


    //압축을 시작하는 메소드
    public String startCompress() throws Throwable {

        setSource(SourceUri);
        setCopyAudio();
        setCopyVideo();
        CompressThread.runThread(this);
        return mOutputFile;
    }

    /**
     * 압축을 하는 서브 스레드
     */
    private static class CompressThread implements Runnable {
        private Throwable mThrowable;
        private CompressMedia compressMedia;

        private CompressThread(CompressMedia test) {
            this.compressMedia = test;
        }

        @Override
        public void run() {
            try {
                compressMedia.compress();
            } catch (Throwable th) {
                mThrowable = th;
            }

        }

        /**
         * Entry point.
         */
        public static void runThread(CompressMedia compressMedia) throws Throwable {
            compressMedia.setOutputFile();
            CompressThread wrapper = new CompressThread(compressMedia);
            Thread th = new Thread(wrapper, "codec test");
            th.start();
            th.join();
            if (wrapper.mThrowable != null) {
                throw wrapper.mThrowable;
            }
        }
    }

    /**
     * Sets the test to copy the video stream.
     */
    private void setCopyVideo() {
        mCopyVideo = true;
    }

    /**
     * Sets the test to copy the video stream.
     */
    private void setCopyAudio() {
        mCopyAudio = true;
    }

    /**
     * Sets the desired frame size.
     */
    private void setSize(int width, int height) {
        if ((width % 16) != 0 || (height % 16) != 0) {
            Log.w(TAG, "WARNING: width or height not multiple of 16");
        }
        mWidth = width;
        mHeight = height;
    }

    /**
     * Sets the raw resource used as the source video.
     */
    private void setSource(String uri) {
        SourceUri = uri;
    }

    /**
     * Sets the name of the output file based on the other parameters.
     *
     * <p>Must be called after {@link #setSize(int, int)} and {@link #setSource(String)}.
     */
    private void setOutputFile() {
        StringBuilder sb = new StringBuilder();
        sb.append(OUTPUT_FILENAME_DIR.getAbsolutePath() + "/");
        sb.append(outputFileName);

        mOutputFile = sb.toString();
    }

    //디먹싱
    private MediaExtractor mVideoExtractor = null;
    private MediaExtractor mAudioExtractor = null;

    private InputSurface mInputSurface = null;
    private OutputSurface mOutputSurface = null;

    //디코딩
    private MediaCodec mVideoDecoder = null;
    private MediaCodec mAudioDecoder = null;
    //인코딩
    private MediaCodec mVideoEncoder = null;
    private MediaCodec mAudioEncoder = null;
    //먹싱
    private MediaMuxer mMuxer = null;

    /**
     * Tests encoding and subsequently decoding video from frames generated into a buffer.
     * <p>
     * We encode several frames of a video test pattern using MediaCodec, then decode the output
     * with MediaCodec and do some simple checks.
     */
    private void compress() throws Exception {


        // Exception that may be thrown during release.
        Exception exception = null;

        mDecoderOutputVideoFormat = null;
        mDecoderOutputAudioFormat = null;
        mEncoderOutputVideoFormat = null;
        mEncoderOutputAudioFormat = null;

        mOutputVideoTrack = -1;
        mOutputAudioTrack = -1;
        mVideoExtractorDone = false;
        mVideoDecoderDone = false;
        mVideoEncoderDone = false;
        mAudioExtractorDone = false;
        mAudioDecoderDone = false;
        mAudioEncoderDone = false;
        mPendingAudioDecoderOutputBufferIndices = new LinkedList<Integer>();
        mPendingAudioDecoderOutputBufferInfos = new LinkedList<MediaCodec.BufferInfo>();
        mPendingAudioEncoderInputBufferIndices = new LinkedList<Integer>();
        mPendingVideoEncoderOutputBufferIndices = new LinkedList<Integer>();
        mPendingVideoEncoderOutputBufferInfos = new LinkedList<MediaCodec.BufferInfo>();
        mPendingAudioEncoderOutputBufferIndices = new LinkedList<Integer>();
        mPendingAudioEncoderOutputBufferInfos = new LinkedList<MediaCodec.BufferInfo>();
        mMuxing = false;
        mVideoExtractedFrameCount = 0;
        mVideoDecodedFrameCount = 0;
        mVideoEncodedFrameCount = 0;
        mAudioExtractedFrameCount = 0;
        mAudioDecodedFrameCount = 0;
        mAudioEncodedFrameCount = 0;

        MediaCodecInfo videoCodecInfo = selectCodec(OUTPUT_VIDEO_MIME_TYPE);//비디오 코덱 인포
        if (videoCodecInfo == null) {
            // Don't fail CTS if they don't have an AVC codec (not here, anyway).
            Log.e(TAG, "Unable to find an appropriate codec for " + OUTPUT_VIDEO_MIME_TYPE);
            return;
        }
        if (VERBOSE) Log.d(TAG, "video found codec: " + videoCodecInfo.getName());

        MediaCodecInfo audioCodecInfo = selectCodec(OUTPUT_AUDIO_MIME_TYPE);//오디오 코덱 인포
        if (audioCodecInfo == null) {
            // Don't fail CTS if they don't have an AAC codec (not here, anyway).
            Log.e(TAG, "Unable to find an appropriate codec for " + OUTPUT_AUDIO_MIME_TYPE);
            return;
        }
        if (VERBOSE) Log.d(TAG, "audio found codec: " + audioCodecInfo.getName());

        try {
            // Creates a muxer but do not start or add tracks just yet.
            mMuxer = createMuxer();

            if (mCopyVideo) {
                mVideoExtractor = createExtractor();//extractor초기화 및 어떤 미디어 파일을 extract할 것인지 set
                //extractor에서 inputformat을 오디오 트랙으로 set하고 track번호 return
                int videoInputTrack = getAndSelectVideoTrackIndex(mVideoExtractor);
                //extractor를 통해서 inputFormat(extract로부터 형성되는 format)을 셋
                MediaFormat inputFormat = mVideoExtractor.getTrackFormat(videoInputTrack);

                // We avoid the device-specific limitations on width and height by using values
                // that are multiples of 16, which all tested devices seem to be able to handle.
                MediaFormat outputVideoFormat =
                        MediaFormat.createVideoFormat(OUTPUT_VIDEO_MIME_TYPE, mWidth, mHeight);
                //포맷 세팅을 통해서 압축 설정을 한다.
                outputVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, OUTPUT_VIDEO_COLOR_FORMAT);
                outputVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, OUTPUT_VIDEO_BIT_RATE);
                outputVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, OUTPUT_VIDEO_FRAME_RATE);
                outputVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, OUTPUT_VIDEO_IFRAME_INTERVAL);
                if (VERBOSE) Log.d(TAG, "video format: " + outputVideoFormat);

                // Create a MediaCodec for the desired codec, then configure it as an encoder with
                // our desired properties. Request a Surface to use for input.
                AtomicReference<Surface> inputSurfaceReference = new AtomicReference<Surface>();
                //비디오 인코더 셋
                mVideoEncoder = createVideoEncoder(videoCodecInfo, outputVideoFormat, inputSurfaceReference);
                //inputSurface설정
                mInputSurface = new InputSurface(inputSurfaceReference.get());
                mInputSurface.makeCurrent();
                // Create a MediaCodec for the decoder, based on the extractor's format.
                mOutputSurface = new OutputSurface();
//                mOutputSurface.changeFragmentShader(FRAGMENT_SHADER);//이거때문에 동영상 color format이 바뀌는 거였음 ㅡㅡ
                //비디오 디코더 셋
                mVideoDecoder = createVideoDecoder(inputFormat, mOutputSurface.getSurface());
                mInputSurface.releaseEGLContext();
            }

            if (mCopyAudio) {
                mAudioExtractor = createExtractor();
                int audioInputTrack = getAndSelectAudioTrackIndex(mAudioExtractor);
                MediaFormat inputFormat = mAudioExtractor.getTrackFormat(audioInputTrack);

                MediaFormat outputAudioFormat =
                        MediaFormat.createAudioFormat(
                                OUTPUT_AUDIO_MIME_TYPE, inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                                inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                outputAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE, OUTPUT_AUDIO_BIT_RATE);
                outputAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, OUTPUT_AUDIO_AAC_PROFILE);

                // Create a MediaCodec for the desired codec, then configure it as an encoder with
                // our desired properties. Request a Surface to use for input.
                mAudioEncoder = createAudioEncoder(audioCodecInfo, outputAudioFormat);
                // Create a MediaCodec for the decoder, based on the extractor's format.
                mAudioDecoder = createAudioDecoder(inputFormat);
            }

            awaitEncode();
        } finally {
            if (VERBOSE) Log.d(TAG, "releasing extractor, decoder, encoder, and muxer");
            // Try to release everything we acquired, even if one of the releases fails, in which
            // case we save the first exception we got and re-throw at the end (unless something
            // other exception has already been thrown). This guarantees the first exception thrown
            // is reported as the cause of the error, everything is (attempted) to be released, and
            // all other exceptions appear in the logs.
            try {
                if (mVideoExtractor != null) {
                    mVideoExtractor.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing videoExtractor", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mAudioExtractor != null) {
                    mAudioExtractor.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing audioExtractor", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mVideoDecoder != null) {
                    mVideoDecoder.stop();
                    mVideoDecoder.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing videoDecoder", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mOutputSurface != null) {
                    mOutputSurface.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing outputSurface", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mVideoEncoder != null) {
                    mVideoEncoder.stop();
                    mVideoEncoder.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing videoEncoder", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mAudioDecoder != null) {
                    mAudioDecoder.stop();
                    mAudioDecoder.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing audioDecoder", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mAudioEncoder != null) {
                    mAudioEncoder.stop();
                    mAudioEncoder.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing audioEncoder", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mMuxer != null) {
                    mMuxer.stop();
                    mMuxer.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing muxer", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mInputSurface != null) {
                    mInputSurface.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing inputSurface", e);
                if (exception == null) {
                    exception = e;
                }
            }
            if (mVideoDecoderHandlerThread != null) {
                mVideoDecoderHandlerThread.quitSafely();
            }
            mVideoExtractor = null;
            mAudioExtractor = null;
            mOutputSurface = null;
            mInputSurface = null;
            mVideoDecoder = null;
            mAudioDecoder = null;
            mVideoEncoder = null;
            mAudioEncoder = null;
            mMuxer = null;
            mVideoDecoderHandlerThread = null;
        }
        if (exception != null) {
            throw exception;
        }
    }

    private MediaExtractor createExtractor() throws IOException {
        MediaExtractor extractor;
        extractor = new MediaExtractor();
        extractor.setDataSource(SourceUri);
        return extractor;
    }

    static class CallbackHandler extends Handler {
        CallbackHandler(Looper l) {
            super(l);
        }

        private MediaCodec mCodec;
        private boolean mEncoder;
        private MediaCodec.Callback mCallback;
        private String mMime;
        private boolean mSetDone;

        @Override
        public void handleMessage(Message msg) {
            try {
                mCodec = mEncoder ? MediaCodec.createEncoderByType(mMime) : MediaCodec.createDecoderByType(mMime);
            } catch (IOException ioe) {
            }
            mCodec.setCallback(mCallback);
            synchronized (this) {
                mSetDone = true;
                notifyAll();
            }
        }

        void create(boolean encoder, String mime, MediaCodec.Callback callback) {
            mEncoder = encoder;
            mMime = mime;
            mCallback = callback;
            mSetDone = false;
            sendEmptyMessage(0);
            synchronized (this) {
                while (!mSetDone) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }

        MediaCodec getCodec() {
            return mCodec;
        }
    }

    private HandlerThread mVideoDecoderHandlerThread;
    private CallbackHandler mVideoDecoderHandler;

    /**
     * Creates a decoder for the given format, which outputs to the given surface.
     *
     * @param inputFormat the format of the stream to decode
     * @param surface     into which to decode the frames
     */
    private MediaCodec createVideoDecoder(MediaFormat inputFormat, Surface surface) throws IOException {
        mVideoDecoderHandlerThread = new HandlerThread("DecoderThread");
        mVideoDecoderHandlerThread.start();
        mVideoDecoderHandler = new CallbackHandler(mVideoDecoderHandlerThread.getLooper());
        MediaCodec.Callback callback = new MediaCodec.Callback() {
            public void onError(MediaCodec codec, MediaCodec.CodecException exception) {
            }

            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                mDecoderOutputVideoFormat = codec.getOutputFormat();
                if (VERBOSE) {
                    Log.d(TAG, "video decoder: output format changed: "
                            + mDecoderOutputVideoFormat);
                }
            }

            public void onInputBufferAvailable(MediaCodec codec, int index) {
                // Extract video from file and feed to decoder.
                // We feed packets regardless of whether the muxer is set up or not.
                // If the muxer isn't set up yet, the encoder output will be queued up,
                // finally blocking the decoder as well.
                ByteBuffer decoderInputBuffer = codec.getInputBuffer(index);
                while (!mVideoExtractorDone) {
                    int size = mVideoExtractor.readSampleData(decoderInputBuffer, 0);
                    long presentationTime = mVideoExtractor.getSampleTime();
                    if (VERBOSE) {
                        Log.d(TAG, "video extractor: returned buffer of size " + size);
                        Log.d(TAG, "video extractor: returned buffer for time " + presentationTime);
                    }
                    if (size >= 0) {
                        codec.queueInputBuffer(
                                index,
                                0,
                                size,
                                presentationTime,
                                mVideoExtractor.getSampleFlags());
                    }
                    mVideoExtractorDone = !mVideoExtractor.advance();
                    if (mVideoExtractorDone) {
                        if (VERBOSE) Log.d(TAG, "video extractor: EOS");
                        codec.queueInputBuffer(
                                index,
                                0,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    }
                    mVideoExtractedFrameCount++;
                    logState();
                    if (size >= 0)
                        break;
                }
            }

            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                if (VERBOSE) {
                    Log.d(TAG, "video decoder: returned output buffer: " + index);
                    Log.d(TAG, "video decoder: returned buffer of size " + info.size);
                }
                if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    if (VERBOSE) Log.d(TAG, "video decoder: codec config buffer");
                    codec.releaseOutputBuffer(index, false);
                    return;
                }
                if (VERBOSE) {
                    Log.d(TAG, "video decoder: returned buffer for time "
                            + info.presentationTimeUs);
                }
                boolean render = info.size != 0;
                codec.releaseOutputBuffer(index, render);
                if (render) {
                    mInputSurface.makeCurrent();
                    if (VERBOSE) Log.d(TAG, "output surface: await new image");
                    mOutputSurface.awaitNewImage();
                    // Edit the frame and send it to the encoder.
                    if (VERBOSE) Log.d(TAG, "output surface: draw image");
                    mOutputSurface.drawImage();
                    mInputSurface.setPresentationTime(
                            info.presentationTimeUs * 1000);
                    if (VERBOSE) Log.d(TAG, "input surface: swap buffers");
                    mInputSurface.swapBuffers();
                    if (VERBOSE) Log.d(TAG, "video encoder: notified of new frame");
                    mInputSurface.releaseEGLContext();
                }
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (VERBOSE) Log.d(TAG, "video decoder: EOS");
                    mVideoDecoderDone = true;
                    mVideoEncoder.signalEndOfInputStream();
                }
                mVideoDecodedFrameCount++;
                logState();
            }
        };
        // Create the decoder on a different thread, in order to have the callbacks there.
        // This makes sure that the blocking waiting and rendering in onOutputBufferAvailable
        // won't block other callbacks (e.g. blocking encoder output callbacks), which
        // would otherwise lead to the transcoding pipeline to lock up.

        // Since API 23, we could just do setCallback(callback, mVideoDecoderHandler) instead
        // of using a custom Handler and passing a message to create the MediaCodec there.

        // When the callbacks are received on a different thread, the updating of the variables
        // that are used for state logging (mVideoExtractedFrameCount, mVideoDecodedFrameCount,
        // mVideoExtractorDone and mVideoDecoderDone) should ideally be synchronized properly
        // against accesses from other threads, but that is left out for brevity since it's
        // not essential to the actual transcoding.
        mVideoDecoderHandler.create(false, getMimeTypeFor(inputFormat), callback);
        MediaCodec decoder = mVideoDecoderHandler.getCodec();
        decoder.configure(inputFormat, surface, null, 0);
        decoder.start();
        return decoder;
    }

    /**
     * Creates an encoder for the given format using the specified codec, taking input from a
     * surface.
     *
     * <p>The surface to use as input is stored in the given reference.
     *
     * @param codecInfo        of the codec to use
     * @param format           of the stream to be produced
     * @param surfaceReference to store the surface to use as input
     */
    private MediaCodec createVideoEncoder(
            MediaCodecInfo codecInfo,
            MediaFormat format,
            AtomicReference<Surface> surfaceReference) throws IOException {
        MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
        encoder.setCallback(new MediaCodec.Callback() {
            public void onError(MediaCodec codec, MediaCodec.CodecException exception) {
            }

            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                if (VERBOSE) Log.d(TAG, "video encoder: output format changed");
                if (mOutputVideoTrack >= 0) {
                }
                mEncoderOutputVideoFormat = codec.getOutputFormat();
                setupMuxer();
            }

            public void onInputBufferAvailable(MediaCodec codec, int index) {
            }

            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                if (VERBOSE) {
                    Log.d(TAG, "video encoder: returned output buffer: " + index);
                    Log.d(TAG, "video encoder: returned buffer of size " + info.size);
                }
                muxVideo(index, info);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            encoder.configure(format, new Surface(new SurfaceTexture(true)), null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } else {
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        }
        // Must be called before start() is.
        surfaceReference.set(encoder.createInputSurface());
        encoder.start();
        return encoder;
    }

    /**
     * Creates a decoder for the given format.
     *
     * @param inputFormat the format of the stream to decode
     */
    private MediaCodec createAudioDecoder(MediaFormat inputFormat) throws IOException {
        MediaCodec decoder = MediaCodec.createDecoderByType(getMimeTypeFor(inputFormat));
        decoder.setCallback(new MediaCodec.Callback() {
            public void onError(MediaCodec codec, MediaCodec.CodecException exception) {
            }

            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                mDecoderOutputAudioFormat = codec.getOutputFormat();
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: output format changed: "
                            + mDecoderOutputAudioFormat);
                }
            }

            public void onInputBufferAvailable(MediaCodec codec, int index) {
                ByteBuffer decoderInputBuffer = codec.getInputBuffer(index);
                while (!mAudioExtractorDone) {
                    int size = mAudioExtractor.readSampleData(decoderInputBuffer, 0);
                    long presentationTime = mAudioExtractor.getSampleTime();
                    if (VERBOSE) {
                        Log.d(TAG, "audio extractor: returned buffer of size " + size);
                        Log.d(TAG, "audio extractor: returned buffer for time " + presentationTime);
                    }
                    if (size >= 0) {
                        codec.queueInputBuffer(
                                index,
                                0,
                                size,
                                presentationTime,
                                mAudioExtractor.getSampleFlags());
                    }
                    mAudioExtractorDone = !mAudioExtractor.advance();
                    if (mAudioExtractorDone) {
                        if (VERBOSE) Log.d(TAG, "audio extractor: EOS");
                        codec.queueInputBuffer(
                                index,
                                0,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    }
                    mAudioExtractedFrameCount++;
                    logState();
                    if (size >= 0)
                        break;
                }
            }

            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: returned output buffer: " + index);
                }
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: returned buffer of size " + info.size);
                }
                ByteBuffer decoderOutputBuffer = codec.getOutputBuffer(index);
                if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    if (VERBOSE) Log.d(TAG, "audio decoder: codec config buffer");
                    codec.releaseOutputBuffer(index, false);
                    return;
                }
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: returned buffer for time "
                            + info.presentationTimeUs);
                }
                mPendingAudioDecoderOutputBufferIndices.add(index);
                mPendingAudioDecoderOutputBufferInfos.add(info);
                mAudioDecodedFrameCount++;
                logState();
                tryEncodeAudio();
            }
        });
        decoder.configure(inputFormat, null, null, 0);
        decoder.start();
        return decoder;
    }

    /**
     * Creates an encoder for the given format using the specified codec.
     *
     * @param codecInfo of the codec to use
     * @param format    of the stream to be produced
     */
    private MediaCodec createAudioEncoder(MediaCodecInfo codecInfo, MediaFormat format) throws IOException {
        MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
        encoder.setCallback(new MediaCodec.Callback() {
            public void onError(MediaCodec codec, MediaCodec.CodecException exception) {
            }

            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                if (VERBOSE) Log.d(TAG, "audio encoder: output format changed");
                if (mOutputAudioTrack >= 0) {
                }

                mEncoderOutputAudioFormat = codec.getOutputFormat();
                setupMuxer();
            }

            public void onInputBufferAvailable(MediaCodec codec, int index) {
                if (VERBOSE) {
                    Log.d(TAG, "audio encoder: returned input buffer: " + index);
                }
                mPendingAudioEncoderInputBufferIndices.add(index);
                tryEncodeAudio();
            }

            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                if (VERBOSE) {
                    Log.d(TAG, "audio encoder: returned output buffer: " + index);
                    Log.d(TAG, "audio encoder: returned buffer of size " + info.size);
                }
                muxAudio(index, info);
            }
        });
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoder.start();
        return encoder;
    }

    // No need to have synchronization around this, since both audio encoder and
    // decoder callbacks are on the same thread.
    private void tryEncodeAudio() {
        if (mPendingAudioEncoderInputBufferIndices.size() == 0 ||
                mPendingAudioDecoderOutputBufferIndices.size() == 0)
            return;
        int decoderIndex = mPendingAudioDecoderOutputBufferIndices.poll();
        int encoderIndex = mPendingAudioEncoderInputBufferIndices.poll();
        MediaCodec.BufferInfo info = mPendingAudioDecoderOutputBufferInfos.poll();

        ByteBuffer encoderInputBuffer = mAudioEncoder.getInputBuffer(encoderIndex);
        int size = info.size;
        long presentationTime = info.presentationTimeUs;
        if (VERBOSE) {
            Log.d(TAG, "audio decoder: processing pending buffer: "
                    + decoderIndex);
        }
        if (VERBOSE) {
            Log.d(TAG, "audio decoder: pending buffer of size " + size);
            Log.d(TAG, "audio decoder: pending buffer for time " + presentationTime);
        }
        if (size >= 0) {
            ByteBuffer decoderOutputBuffer = mAudioDecoder.getOutputBuffer(decoderIndex).duplicate();
            decoderOutputBuffer.position(info.offset);
            decoderOutputBuffer.limit(info.offset + size);
            encoderInputBuffer.position(0);
            encoderInputBuffer.put(decoderOutputBuffer);

            mAudioEncoder.queueInputBuffer(
                    encoderIndex,
                    0,
                    size,
                    presentationTime,
                    info.flags);
        }
        mAudioDecoder.releaseOutputBuffer(decoderIndex, false);
        if ((info.flags
                & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            if (VERBOSE) Log.d(TAG, "audio decoder: EOS");
            mAudioDecoderDone = true;
        }
        logState();
    }

    private void setupMuxer() {
        if (!mMuxing
                && (!mCopyAudio || mEncoderOutputAudioFormat != null)
                && (!mCopyVideo || mEncoderOutputVideoFormat != null)) {
            if (mCopyVideo) {
                Log.d(TAG, "muxer: adding video track.");
                mOutputVideoTrack = mMuxer.addTrack(mEncoderOutputVideoFormat);
            }
            if (mCopyAudio) {
                Log.d(TAG, "muxer: adding audio track.");
                mOutputAudioTrack = mMuxer.addTrack(mEncoderOutputAudioFormat);
            }
            Log.d(TAG, "muxer: starting");
            mMuxer.start();
            mMuxing = true;

            MediaCodec.BufferInfo info;
            while ((info = mPendingVideoEncoderOutputBufferInfos.poll()) != null) {
                int index = mPendingVideoEncoderOutputBufferIndices.poll().intValue();
                muxVideo(index, info);
            }
            while ((info = mPendingAudioEncoderOutputBufferInfos.poll()) != null) {
                int index = mPendingAudioEncoderOutputBufferIndices.poll().intValue();
                muxAudio(index, info);
            }
        }
    }

    private void muxVideo(int index, MediaCodec.BufferInfo info) {
        if (!mMuxing) {
            mPendingVideoEncoderOutputBufferIndices.add(new Integer(index));
            mPendingVideoEncoderOutputBufferInfos.add(info);
            return;
        }
        ByteBuffer encoderOutputBuffer = mVideoEncoder.getOutputBuffer(index);
        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            if (VERBOSE) Log.d(TAG, "video encoder: codec config buffer");
            // Simply ignore codec config buffers.
            mVideoEncoder.releaseOutputBuffer(index, false);
            return;
        }
        if (VERBOSE) {
            Log.d(TAG, "video encoder: returned buffer for time "
                    + info.presentationTimeUs);
        }
        if (info.size != 0) {
            mMuxer.writeSampleData(
                    mOutputVideoTrack, encoderOutputBuffer, info);
        }
        mVideoEncoder.releaseOutputBuffer(index, false);
        mVideoEncodedFrameCount++;
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            if (VERBOSE) Log.d(TAG, "video encoder: EOS");
            synchronized (this) {
                mVideoEncoderDone = true;
                notifyAll();
            }
        }
        logState();
    }

    private void muxAudio(int index, MediaCodec.BufferInfo info) {
        if (!mMuxing) {
            mPendingAudioEncoderOutputBufferIndices.add(new Integer(index));
            mPendingAudioEncoderOutputBufferInfos.add(info);
            return;
        }
        ByteBuffer encoderOutputBuffer = mAudioEncoder.getOutputBuffer(index);
        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            if (VERBOSE) Log.d(TAG, "audio encoder: codec config buffer");
            // Simply ignore codec config buffers.
            mAudioEncoder.releaseOutputBuffer(index, false);
            return;
        }
        if (VERBOSE) {
            Log.d(TAG, "audio encoder: returned buffer for time " + info.presentationTimeUs);
        }
        if (info.size != 0) {
            mMuxer.writeSampleData(
                    mOutputAudioTrack, encoderOutputBuffer, info);
        }
        mAudioEncoder.releaseOutputBuffer(index, false);
        mAudioEncodedFrameCount++;
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            if (VERBOSE) Log.d(TAG, "audio encoder: EOS");
            synchronized (this) {
                mAudioEncoderDone = true;
                notifyAll();
            }
        }
        logState();
    }

    /**
     * returned buffer of size
     * Creates a muxer to write the encoded frames.
     *
     * <p>The muxer is not started as it needs to be started only after all streams have been added.
     */
    private MediaMuxer createMuxer() throws IOException {
        return new MediaMuxer(mOutputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
    }

    private int getAndSelectVideoTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            if (VERBOSE) {
                Log.d(TAG, "format for track " + index + " is "
                        + getMimeTypeFor(extractor.getTrackFormat(index)));
            }
            if (isVideoFormat(extractor.getTrackFormat(index))) {
                extractor.selectTrack(index);
                return index;
            }
        }
        return -1;
    }

    private int getAndSelectAudioTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            if (VERBOSE) {
                Log.d(TAG, "format for track " + index + " is "
                        + getMimeTypeFor(extractor.getTrackFormat(index)));
            }
            if (isAudioFormat(extractor.getTrackFormat(index))) {
                extractor.selectTrack(index);
                return index;
            }
        }
        return -1;
    }

    // We will get these from the decoders when notified of a format change.
    private MediaFormat mDecoderOutputVideoFormat = null;
    private MediaFormat mDecoderOutputAudioFormat = null;
    // We will get these from the encoders when notified of a format change.
    private MediaFormat mEncoderOutputVideoFormat = null;
    private MediaFormat mEncoderOutputAudioFormat = null;

    // We will determine these once we have the output format.
    private int mOutputVideoTrack = -1;
    private int mOutputAudioTrack = -1;
    // Whether things are done on the video side.
    private boolean mVideoExtractorDone = false;
    private boolean mVideoDecoderDone = false;
    private boolean mVideoEncoderDone = false;
    // Whether things are done on the audio side.
    private boolean mAudioExtractorDone = false;
    private boolean mAudioDecoderDone = false;
    private boolean mAudioEncoderDone = false;
    private LinkedList<Integer> mPendingAudioDecoderOutputBufferIndices;
    private LinkedList<MediaCodec.BufferInfo> mPendingAudioDecoderOutputBufferInfos;
    private LinkedList<Integer> mPendingAudioEncoderInputBufferIndices;

    private LinkedList<Integer> mPendingVideoEncoderOutputBufferIndices;
    private LinkedList<MediaCodec.BufferInfo> mPendingVideoEncoderOutputBufferInfos;
    private LinkedList<Integer> mPendingAudioEncoderOutputBufferIndices;
    private LinkedList<MediaCodec.BufferInfo> mPendingAudioEncoderOutputBufferInfos;

    private boolean mMuxing = false;

    private int mVideoExtractedFrameCount = 0;
    private int mVideoDecodedFrameCount = 0;
    private int mVideoEncodedFrameCount = 0;

    private int mAudioExtractedFrameCount = 0;
    private int mAudioDecodedFrameCount = 0;
    private int mAudioEncodedFrameCount = 0;

    private void logState() {
        if (VERBOSE) {
            Log.d(TAG, String.format(
                    "loop: "

                            + "V(%b){"
                            + "extracted:%d(done:%b) "
                            + "decoded:%d(done:%b) "
                            + "encoded:%d(done:%b)} "

                            + "A(%b){"
                            + "extracted:%d(done:%b) "
                            + "decoded:%d(done:%b) "
                            + "encoded:%d(done:%b) "

                            + "muxing:%b(V:%d,A:%d)",

                    mCopyVideo,
                    mVideoExtractedFrameCount, mVideoExtractorDone,
                    mVideoDecodedFrameCount, mVideoDecoderDone,
                    mVideoEncodedFrameCount, mVideoEncoderDone,

                    mCopyAudio,
                    mAudioExtractedFrameCount, mAudioExtractorDone,
                    mAudioDecodedFrameCount, mAudioDecoderDone,
                    mAudioEncodedFrameCount, mAudioEncoderDone,

                    mMuxing, mOutputVideoTrack, mOutputAudioTrack));
        }
    }

    private void awaitEncode() {
        synchronized (this) {
            while ((mCopyVideo && !mVideoEncoderDone) || (mCopyAudio && !mAudioEncoderDone)) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                }
            }
        }

        // Basic sanity checks.
        if (mCopyVideo) {
        }
        if (mCopyAudio) {
        }

        // TODO: Check the generated output file.
    }

    private static boolean isVideoFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("video/");
    }

    private static boolean isAudioFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("audio/");
    }

    private static String getMimeTypeFor(MediaFormat format) {
        return format.getString(MediaFormat.KEY_MIME);
    }

    /**
     * Returns the first codec capable of encoding the specified MIME type, or null if no match was
     * found.
     */
    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    /**
     * Retruns a color format that is supported by the codec and by this test
     * code. If no match is found, this throws a test failure -- the set of
     * formats known to the test should be expanded for new platforms.
     */
    protected int selectColorFormat(String mimeType) {
        MediaCodecInfo codecInfo = selectCodec(mimeType);
        if (codecInfo == null) {
            throw new RuntimeException("Unable to find an appropriate codec for " + mimeType);
        }

        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (isRecognizedFormat(colorFormat)) {
                Log.d("ColorFomar", "Find a good color format for " + codecInfo.getName() + " / " + mimeType);
                return colorFormat;
            }
        }
        return -1;
    }

    /**
     * Returns true if this is a color format that this test code understands
     * (i.e. we know how to read and generate frames in this format).
     */
    private boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            // these are the formats we know how to handle for this test
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }
}
