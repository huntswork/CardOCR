package exocr.idcard;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.kalu.ocr.CaptureActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import exocr.exocrengine.EXOCREngine;
import exocr.exocrengine.EXOCRModel;

/**
 * description: 解码
 * create by kalu on 2018/11/20 9:41
 */
final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();
    private final CaptureActivity activity;
    private int gcount;

    private int quit_id;

    DecodeHandler(CaptureActivity activity) {
        this.activity = activity;
        gcount = 0;
        quit_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "quit");
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == PreviewCallback.PARSE_DECODE) {
            decode((byte[]) message.obj, message.arg1, message.arg2);
        } else if (message.what == quit_id) {
            Looper.myLooper().quit();
        }
    }

    private void decode(byte[] data, int width, int height) {
        long start = System.currentTimeMillis();
        int[] rects = new int[32];
        // arg
        int ret = 0;
        //savetofile(data, width, height);
        //savetoJPEG(data, width, height);

        final byte[] obtain = EXOCREngine.obtain();

        ////////////////////////////////////////////////////////////////////////////////////////////////
        EXOCREngine.timestart = System.currentTimeMillis();
        ret = EXOCREngine.nativeRecoIDCardRawdat(data, width, height, width, 1, obtain, obtain.length);
        EXOCREngine.timeend = System.currentTimeMillis();

        if (ret > 0) {
            long end = System.currentTimeMillis();
            Log.d(TAG, "Found text (" + (end - start) + " ms):\n");
            EXOCRModel idcard = EXOCRModel.decode(obtain, ret);
            //if we have the text to show
            //检测，确保无误。
            if (idcard != null) {
                idcard.SetViewType("Preview");
                idcard.SetColorType(CardColorJudge(data, width, height));

                //NOTE: 下面这些代码是提取标准身份证图像的，如果客户有需求请打开
                //API For Image Return;
                Bitmap imcard = EXOCREngine.nativeGetIDCardStdImg(data, width, height, obtain, obtain.length, rects);
                //如果需要保存图像，请您打开保存图像的语句
                //try{saveBitmap(imcard); }catch (IOException e) {e.printStackTrace();}
                idcard.SetBitmap(activity.getApplicationContext(), imcard);
                //保存各个条目的矩形框
                idcard.setRects(rects);
                if (idcard.type == 1) {
                    // CaptureActivity.IDCardFaceImage = idcard.GetFaceBitmap();
                }
                //返回图像结束-------END

//				Message message = Message.obtain(activity.getHandler(),	R.id.decode_succeeded, idcard);
                Message message = Message.obtain(activity.getHandler(), PreviewCallback.PARSE_SUCC, idcard);
                message.sendToTarget();

                return;
            }
        }
        Message message = Message.obtain(activity.getHandler(), PreviewCallback.PARSE_FAIL);
        message.sendToTarget();
    }

    private int CardColorJudge(byte[] data, int width, int height) {
        int offset = width * height;
        int i;
        int iTht = 144;
        int iCnt = 255;
        int nNum = 0;
        int iSize = width * height / 2;

        for (i = 0; i < iSize; ++i) {
            int val = data[i + offset] & 0xFF;
            if (val > iTht) {
                ++nNum;
            }
        }

        if (nNum > iCnt) return 1;
        else return 0;
    }

    // save to jpeg
    private void savetoJPEG(byte[] data, int width, int height) {
        int w, h;
        gcount++;
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String date = sDateFormat.format(new java.util.Date());

        String tofile = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + date + "_" + gcount + ".jpg";
        //String tofile = Environment.()+File.separator+Environment.DIRECTORY_DCIM+File.separator+date+"_"+gcount+".jpg";
        //String tofile = "/sdcard/DCIM/"+"NV21_"+ date+"_"+gcount+".jpg";

        int imageFormat = ImageFormat.NV21;
        Rect frame = new Rect(0, 0, width - 1, height - 1);
        if (imageFormat == ImageFormat.NV21) {
            YuvImage img = new YuvImage(data, ImageFormat.NV21, width, height, null);
            OutputStream outStream = null;
            File file = new File(tofile);
            try {
                outStream = new FileOutputStream(file);
                img.compressToJpeg(frame, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveBitmap(Bitmap bitmap) throws IOException {
        String tofile = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "image_idcard.jpg";
        File file = new File(tofile);
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
