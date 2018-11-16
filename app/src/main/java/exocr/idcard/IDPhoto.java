package exocr.idcard;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.FileNotFoundException;

import exocr.exocrengine.EXIDCardResult;
import exocr.exocrengine.EXOCREngine;

public class IDPhoto {
    private static final String TAG = IDPhoto.class.getSimpleName();
    private CaptureActivity mActivity;
    private EXIDCardResult mCardInfo;
    static Bitmap markedCardImage = null;
    private boolean bSucceed;

    private ProgressDialog pd;
    private static final int PHOTO_DATA_ENTRY = 0x555;

    /**
     * Construction
     */
    public IDPhoto(CaptureActivity activity) {
        mActivity = activity;
    }

    public void openPhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        /* 开启Pictures画面Type设定为image */
        intent.setType("image/*");
        /* 取得相片后返回 */
        mActivity.startActivityForResult(intent, CaptureActivity.PHOTO_ID);
    }

    private void _photoRec(Context context, Bitmap bitmap) {
        byte[] result = new byte[4096];
        int[] rets = new int[16];
        int[] rects = new int[64];
        int ret = 0;

        //recgonise stillImage
        Bitmap cardim = EXOCREngine.nativeRecoIDCardStillImageV2(bitmap, 0, 1, result, 4096, rects, rets);
        Log.i("nativeRecoStillImage", "return=" + rets[0]);

        ret = rets[0];
        if (ret > 0) {
            mCardInfo = EXIDCardResult.decode(result, ret);
            mCardInfo.SetBitmap(context, cardim);
            //保存各个条目的矩形框
            mCardInfo.setRects(rects);
            bSucceed = true;
        } else {
            bSucceed = false;
            mCardInfo = new EXIDCardResult();
            if (markedCardImage != null && !markedCardImage.isRecycled()) {
                markedCardImage.recycle();
            }
            markedCardImage = bitmap;
            return;
        }

        if (bSucceed) {
//			if (mCardInfo.type == 1) {
//				if (CaptureActivity.IDCardFrontFullImage != null && !CaptureActivity.IDCardFrontFullImage.isRecycled()) {
//					CaptureActivity.IDCardFrontFullImage.recycle();
//				}
//				CaptureActivity.IDCardFrontFullImage = mCardInfo.stdCardIm;
//				if (CaptureActivity.IDCardFaceImage != null && !CaptureActivity.IDCardFaceImage.isRecycled()) {
//					CaptureActivity.IDCardFaceImage.recycle();
//				}
//				CaptureActivity.IDCardFaceImage = mCardInfo.GetFaceBitmap();
//			} else if (mCardInfo.type == 2) {
//				if (CaptureActivity.IDCardBackFullImage != null && !CaptureActivity.IDCardBackFullImage.isRecycled()) {
//					CaptureActivity.IDCardBackFullImage.recycle();
//				}
//				CaptureActivity.IDCardBackFullImage = mCardInfo.stdCardIm;
//			}
        }
    }

    public void photoRec(final Context context, Intent data) {
        Uri uri = data.getData();
        Log.d(TAG, uri.toString());
        ContentResolver cr = mActivity.getContentResolver();
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
            final Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri), null, opt);
            if (bitmap == null) {
                return;
            }
            pd = ProgressDialog.show(mActivity, null, "正在识别，请稍候");
            /* 解析bitmap, 生成cardInfo */
            new Thread() {
                public void run() {
                    // 识别图片
                    _photoRec(context, bitmap);
                }
            }.start();
        } catch (FileNotFoundException e) {
            Log.e("Exception", e.getMessage(), e);
        }
    }

    public EXIDCardResult getRecoResult() {
        return mCardInfo;
    }
}
