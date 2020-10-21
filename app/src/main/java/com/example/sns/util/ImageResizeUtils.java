package com.example.sns.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//이 클래스는 이미지의 사이즈를 커스터마이징하고 카메라로 촬영됐을 경우 회전 각도를 가져와서 이미지를 적절하게 회전시키는 기능을 갖는다.
public class ImageResizeUtils {

    //param-1: 내가 수정하고 싶은 이미지 파일
    //param-2: 수정된 이미지를 저장할 파일
    //param-3: 리사이징할 크기(이미지의 가로와 세로를 비교해서 더 긴 쪽으의 사이즈를 newWidth값으로 변경해준다.)
    //param-4: 카메라에서 온 이미지인지 아닌지를 가리기 위함.
    public static void resizeFile(File file, File newFile, int newWidth, Boolean isCamera){
        String TAG = "moonsoo";
        Bitmap imageBitmap = null;
        Bitmap resizedImageBitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            imageBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            //카메라로 촬영한 이미지의 경우
            if (isCamera) {
                //상황에 맞게 이미지를 회전시킴
                try {
                    //이 객체는 이미지의 각종 정보를 담고 있는 객체다. 파라미터로 들어간 이미지의 정보들을 가져와서 쓸 수 있다.
                    ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                    int exifOrientation= exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                    int exifDegree = exifOrientationToDegrees(exifOrientation);
                    Log.d(TAG, "exifDegree:"+exifDegree);

                    imageBitmap = rotate(imageBitmap, exifDegree);

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            if(imageBitmap == null) {
                Log.e(TAG,"파일 에러");
                return;
            }

            int width = imageBitmap.getWidth();
            int height = imageBitmap.getHeight();

            float aspect, scaleWidth, scaleHeight;

            if(width > height){


                if(width <= newWidth) return;

                aspect=(float)width / height;

                scaleWidth = newWidth;
                scaleHeight = scaleWidth / aspect;


            } else {


                if(height <= newWidth) return;

                aspect = (float) height / width;

                scaleHeight = newWidth;
                scaleWidth= scaleHeight / aspect;

            }

            //비트맵 이미지를 조작하기 위해서 matrix객체 선언
            Matrix matrix = new Matrix();

            //비트맵 이미지를 리사이즈한다.
            matrix.postScale(scaleWidth/width, scaleHeight/height);

            //새로운 비트맵이미지를 생성한다.
            resizedImageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, width, height, matrix, true);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //새로운 이미지를 압축 저장한다.
                resizedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(newFile));

            }else {
                resizedImageBitmap.compress(Bitmap.CompressFormat.PNG, 80, new FileOutputStream(newFile));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {

            if(imageBitmap != null) {
                imageBitmap.recycle();
            }

            if(resizedImageBitmap != null){
                resizedImageBitmap.recycle();
            }

        }
    }
    //이 함수는 exif정보를 가져와서 회전 정도에 따라 이미지를 회전시켜주는 메소드다.
    //param: exif에 들어있는 회전각
    //return: 실제 각도
    public static int exifOrientationToDegrees(int exifOrientation){
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90){
            return 90;
        }else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        }else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270){
            return 270;
        }

        return 0;
    }
    //실제로 이미지를 회전시킨 후 bitmap객체를 return하는 메소드
    //param-1: 비트맵 이미지
    //param-2: 이미지의 회전 정도
    public static Bitmap rotate(Bitmap bitmap, int degrees) {
        if(degrees != 0 && bitmap != null){

            Matrix matrix = new Matrix();
            matrix.setRotate(degrees, (float) bitmap.getWidth()/2, (float) bitmap.getHeight()/2);

            try{
                Bitmap converted = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                if(bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            } catch (OutOfMemoryError e){
                e.printStackTrace();
                //메모리가 부족하여 회전을 시키지 못하면 그냥 원본 반환
            }
        }
        return bitmap;
    }
}
