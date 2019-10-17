package com.example.sns;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.media.session.MediaControllerCompat;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProcessImage {
    /*이미지를 서버로 업로드시 이미지의 사이즈를 줄이고 이미지 회전값에 따라 적절하게 이미지를 회전시켜서 파일화시키는 기능을 담당하는 클래스*/

    Context context;

    public ProcessImage (Context context){
        this.context = context;
    }


    public Bitmap getBitmapFromUri (String uri) {

        Uri imageUri = Uri.parse(uri);//string to uri
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(imageUri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);


        //이미지 크기
        int width = options.outWidth;
        int height = options.outHeight;
        //이미지 resizing 비율
        float resizeRatio = getImageRatio(width, height);

        options.inJustDecodeBounds = false;
        options.inSampleSize = (int) resizeRatio;
        Bitmap resizedBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);//크기를 줄인 이미지 비트맵
        //이미지가 회전되어서 올라가는 것을 방지하기 위해서 exif정보를 활용하여 이미지 회전값에 따라 적절하게 회전시킨 bitmap을 추출한다.
        int rotateOrientation = getImageOrientation(uri);
        //회전값에 따른 이미지 회전이 반영된 비트맵
        Bitmap rotatedBitmap = getRotatedBitmap(resizedBitmap, rotateOrientation);
        return rotatedBitmap;

    }

    public File createFileFromBitmap(Bitmap bitmap, String uri) {
        Uri imageUri = Uri.parse(uri);
        File file = new File(imageUri.getPath());
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    //이미지 회전 값에 따라 적절하게 회전된 비트맵 추출
    private Bitmap getRotatedBitmap(Bitmap bitmap, int rotateDegree) {
        if (rotateDegree == 0) {//사진이 회전되어있지 않은 경우
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.setRotate(rotateDegree, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
        //param2: x축 픽셀의 시작 지점, param3: y축 픽셀의 시작 지점, param6: 변경된 설정을 담은 matrix객체
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

    }


    private float getImageRatio(int width, int height) {
        //리사이징 목표 사이즈
        final int targetWidth = 2160;
        final int targetHeight = 2160;
        float ratio;
        if (width > height) {
            if (width > targetWidth) {
                ratio = (float) width / (float) targetWidth;
            } else {
                ratio = 1f;
            }
        } else {
            if (height > targetHeight) {
                ratio = (float) height / (float) targetHeight;
            } else {
                ratio = 1f;
            }
        }
        return Math.round(ratio);
    }

    //이미지 회전 값을 출력하는 메소드
    private int getImageOrientation(String uri) {
        Uri imageUri = Uri.parse(uri);
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(imageUri.getPath());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        if (orientation != -1) {
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                return 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                return 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                return 270;
            } else {
                return 0;
            }
        }
        return 0;
    }

}
