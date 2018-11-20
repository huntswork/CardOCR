package exocr.idcard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Message;

import com.kalu.ocr.CaptureActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import exocr.exocrengine.EXOCREngine;
import exocr.exocrengine.EXOCRModel;

/**
 * description: 拍照回调
 * create by kalu on 2018/11/20 9:53
 */
final class PictureCallback implements Camera.PictureCallback {

    private CaptureActivity activity;

    PictureCallback() {
        this.activity = null;
    }

    void SetActivity(CaptureActivity activity) {
        this.activity = activity;
    }

    public void onPictureTaken(byte[] data, Camera camera) {
        //save the jpeg image if needed
        if (CameraManager.get().getCCM().getPictureFormat() == PixelFormat.JPEG) {
            saveImage(data);
        }
        //call for recognition the card by take picture get
        if (this.activity != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int ret = 0;

            final byte[] obtain = EXOCREngine.obtain();

            ret = EXOCREngine.nativeRecoIDCardBitmap(bitmap, obtain, obtain.length);

            if (ret > 0) {
                EXOCRModel idcard = EXOCRModel.decode(obtain, ret);
                if (idcard != null) {
                    idcard.SetViewType("Preview");
                    Message message = Message.obtain(activity.getHandler(), PreviewCallback.PARSE_SUCC, idcard);
                    message.sendToTarget();
                }
            }
            this.activity = null;
        }
        CameraManager.get().startPreview();
    }

    static private void convert2Gray(Bitmap bmp, byte[] data, int width, int height) {
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
        int nPixelCount = 0;
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int gray = pixels[nPixelCount];

                int red = ((gray & 0x00FF0000) >> 16);
                int green = ((gray & 0x0000FF00) >> 8);
                int blue = (gray & 0x000000FF);

                gray = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                data[nPixelCount] = (byte) gray;
                nPixelCount++;
            }
        }
    }

    static private String saveImage(byte[] data) { // 保存jpg到SD卡中
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        String tofile = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM + File.separator
                + date + ".jpg";

        try {
            File file = new File(tofile);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tofile;
    }
}
