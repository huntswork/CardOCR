package com.kalu.ocr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;

import java.io.File;
import java.io.FileFilter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import exocr.exocrengine.EXOCRModel;
import exocr.idcard.CameraManager;
import exocr.idcard.CaptureHandler;
import exocr.idcard.IDPhoto;
import exocr.idcard.ViewUtil;
import exocr.view.CaptureView;

public final class CaptureActivity extends Activity implements SurfaceHolder.Callback {

    public static final String INTNET_FRONT = "ShouldFront";

    public static final String EXTRA_SCAN_RESULT = "exocr.idcard.scanResult";
    private static final String TAG = CaptureActivity.class.getSimpleName();
    private CaptureHandler handler;
    private boolean hasSurface;
    private boolean bLight;

    private IDPhoto idPhoto;
    public static final int PHOTO_ID = 0x1025;
    private boolean bPhotoReco;


    private int compareCount = 0;

    private static int uniqueOMatic = 10;
    private static final int REQUEST_DATA_ENTRY = uniqueOMatic++;

    private static final String FRONT_TIP = "请将身份证放在屏幕中央，正面朝上";
    private static final String BACK_TIP = "请将身份证放在屏幕中央，背面朝上";
    private static final String ERR_FRONT_TIP = "检测到身份证背面，请将正面朝上";
    private static final String ERR_BACK_TIP = "检测到身份证正面，请将背面朝上";
    private boolean bshouleFront;
    private boolean bLastWrong;
    private boolean bCamera;
    private PopupWindow popupWindow;
    private static final int MSG_POPUP = 1001;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_POPUP) {
                // UI更新
                // 竖屏
                // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                // 一个自定义的布局，作为显示的内容
                int popupId = ViewUtil.getResourseIdByName(getApplicationContext().getPackageName(), "layout", "layout_alert");
                View contentView = CaptureActivity.this.getLayoutInflater().inflate(popupId, null);
                // 设置按钮的点击事件
                int btnId = ViewUtil.getResourseIdByName(getApplicationContext().getPackageName(), "id", "okButton");
                Button button = (Button) contentView.findViewById(btnId);
                button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                        Intent intent = new Intent();
                        CaptureActivity.this.setResult(REQUEST_DATA_ENTRY, intent);
                        finish();
                    }
                });

                popupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
                popupWindow.setTouchable(true);
                // 是否阻塞
                // popupWindow.setBackgroundDrawable(new BitmapDrawable());
                // 设置好参数之后再show
                int IDpreview_viewId = ViewUtil.getResourseIdByName(getApplicationContext().getPackageName(), "id", "IDpreview_view");
                popupWindow.showAtLocation(CaptureActivity.this.findViewById(IDpreview_viewId), Gravity.CENTER, 0, 0);
            }
        }
    };

    private final ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
        }
    };

    @Override
    public void finish() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View root = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_capture, null);
        final boolean isFront = getIntent().getBooleanExtra(INTNET_FRONT, true);
        final CaptureView capture = root.findViewById(R.id.captuer_scan);
        capture.setFront(isFront);
        setContentView(root);


        // 检测摄像头权限
        bCamera = hardwareSupportCheck();
        // CameraManager
        CameraManager.init(getApplication());
        // 横屏
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // FLAG_TRANSLUCENT_NAVIGATION
        if (VERSION.SDK_INT >= 19) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        //double-check
        //system version NOT less than 4.2.x, number of cpus NOT less than 4
        if (VERSION.SDK_INT >= 17 && getNumCores() >= 4) {
            EXOCRModel.DOUBLE_CHECK = true;
            Log.d(TAG, "open double-check");
            //disable double-check after 10s
            TimerTask task = new TimerTask() {
                public void run() {
                    // execute the task
                    EXOCRModel.DOUBLE_CHECK = false;
                    Log.d(TAG, "close double-check");
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, 10000);
        }

        if (bCamera) {
            hasSurface = false;
            bPhotoReco = false;
            bshouleFront = getIntent().getBooleanExtra(INTNET_FRONT, true);
            Log.d(TAG, "bshouleFront:" + bshouleFront);
            if (bshouleFront) {
                Log.d(TAG, "正面");
            } else {
                Log.d(TAG, "反面");
            }
            bLight = false;
        } else { // 摄像头权限受限
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    // 子线程发送信息
                    Message msg = mHandler.obtainMessage(MSG_POPUP);
                    msg.sendToTarget();
                }
            };
            mHandler.postDelayed(runnable, 100);
        }
    }

    public static boolean hardwareSupportCheck() {
        // Camera needs to open
        Camera c = null;
        boolean ret = true;
        try {
            c = Camera.open();
        } catch (RuntimeException e) {
            // throw new RuntimeException();
            ret = false;
        }
        if (c == null) { // 没有背摄像头
            return false;
        }
        if (ret) {
            c.release();
            c = null;
        }
        return ret;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bCamera && bPhotoReco == false) {
            //重置比对计数
            compareCount = 0;
            int IDpreview_viewId = ViewUtil.getResourseIdByName(getApplicationContext().getPackageName(), "id", "IDpreview_view");
            SurfaceView surfaceView = (SurfaceView) findViewById(IDpreview_viewId);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            if (hasSurface) {
                initCamera(surfaceHolder);
            } else {
                surfaceHolder.addCallback(this);
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openCamera(surfaceHolder);
        } catch (Exception ioe) {
            return;
        }
        if (handler == null) {
            handler = new CaptureHandler(this);
        }
    }

    public Handler getHandler() {
        return handler;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            this.setResult(REQUEST_DATA_ENTRY, intent);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (VERSION.SDK_INT < 14) {        //lower than android 4.0
                return false;
            }
            if (bCamera) {
                float x = event.getX();
                float y = event.getY();
                Point res = CameraManager.get().getResolution();

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (x > res.x * 8 / 10 && y < res.y / 4) {
                        return false;
                    }

                    handleDecode(null);

                    // 点击重新聚焦
                    if (handler != null) {
                        handler.restartAutoFocus();
                    }
                    return true;
                }
            }
        } catch (NullPointerException e) {

        }
        return false;
    }

    //check is equal()
//    public boolean CheckIsEqual(EXOCRModel cardcur) {
//        if (!(EXOCRModel.DOUBLE_CHECK)) {
//            Log.d(TAG, "disable double-check");
//            return true;
//        } else {
//            Log.d(TAG, "enable double-check");
//        }
//        if (compareCount++ > 50) {
//            return true;
//        }
//        EXOCRModel cardlast;
//        for (int i = 0; i < lastCardsLength; i++) {
//            if (lastCards[i] != null) {
//                cardlast = lastCards[i];
//                if (cardlast.type == 1 && cardcur.type == 1) {
//                    if (cardlast.name.equals(cardcur.name) &&
//                            cardlast.sex.equals(cardcur.sex) &&
//                            cardlast.nation.equals(cardcur.nation) &&
//                            cardlast.cardnum.equals(cardcur.cardnum) &&
//                            cardlast.address.equals(cardcur.address)) {
//                        //Log.e("比对成功",  String.valueOf(i));
//                        return true;
//                    }
//                } else if (cardlast.type == 2 && cardcur.type == 2) {
//                    if (cardlast.validdate.equals(cardcur.validdate) &&
//                            cardlast.office.equals(cardcur.office)) {
//                        //Log.e("比对成功",  String.valueOf(i));
//                        return true;
//                    }
//                }
//            }
//        }
//
//        lastCardsIndex++;
//        if (lastCardsIndex + 1 > lastCardsLength) {
//            lastCardsIndex = 0;
//        }
//        if (lastCards[lastCardsIndex] == null) {
//            lastCards[lastCardsIndex] = new EXOCRModel();
//        }
//        lastCards[lastCardsIndex].type = cardcur.type;
//        if (cardcur.type == 1) {
//            lastCards[lastCardsIndex].sex = cardcur.sex;
//            lastCards[lastCardsIndex].nation = cardcur.nation;
//            lastCards[lastCardsIndex].cardnum = cardcur.cardnum;
//            lastCards[lastCardsIndex].address = cardcur.address;
//            lastCards[lastCardsIndex].name = cardcur.name;
//        } else if (cardcur.type == 2) {
//            lastCards[lastCardsIndex].validdate = cardcur.validdate;
//            lastCards[lastCardsIndex].office = cardcur.office;
//        }
//        //Log.e("比对失败",  String.valueOf(lastCardsIndex));
//        return false;
//    }

    public ShutterCallback getShutterCallback() {
        return shutterCallback;
    }

    public void OnFlashBtnClick(View view) {
        if (bLight) {
            CameraManager.get().disableFlashlight();
            bLight = false;
        } else {
            CameraManager.get().enableFlashlight();
            bLight = true;
        }
    }

    //////////////////////////////////////////////////////////////////////////
    public void OnShotBtnClick(View view) {
        // Toast.makeText(this, "Button clicked!", Toast.LENGTH_SHORT).show();
        handleDecode(null);
        // playBeepSoundAndVibrate();
        handler.takePicture();
    }

    public void onPhotoBtnClickID(View view) {
        bPhotoReco = true;
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
        Log.d(TAG, "ID photo");
        idPhoto = new IDPhoto(this);
        idPhoto.openPhoto();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {                    //选中照片
            if (requestCode == PHOTO_ID) {
                Log.d(TAG, "ID received data");
                idPhoto.photoRec(getApplicationContext(), data);
            }
        } else if (resultCode == RESULT_CANCELED) {        //取消相册
            didFinishPhotoRec();
        }
    }

    public void didFinishPhotoRec() {
        bPhotoReco = false;
        //重置比对计数
        compareCount = 0;
        int IDpreview_viewId = ViewUtil.getResourseIdByName(getApplicationContext().getPackageName(), "id", "IDpreview_view");
        SurfaceView surfaceView = (SurfaceView) findViewById(IDpreview_viewId);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    private int getNumCores() {
        // Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                // Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            // Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            Log.d(TAG, "CPU Count: " + files.length);
            // Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            // Print exception
            Log.d(TAG, "CPU Count: Failed.");
            e.printStackTrace();
            // Default to return 1 core
            return 1;
        }
    }

    /**********************************************************************************************/

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface && bPhotoReco == false) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    /**********************************************************************************************/

    public void handleDecode(EXOCRModel result) {

        if (null == result)
            return;

        // 正面, 反面
        if (result.type == 1 || result.type == 2) {

            // final Bitmap cardBitmap = result.stdCardIm;
            // final Bitmap nameBitmap = result.GetNameBitmap();
            // final Bitmap faceBitmap = result.GetFaceBitmap();

            Intent intent = new Intent();
            intent.putExtra(EXTRA_SCAN_RESULT, result);
            intent.putExtras(getIntent()); // passing on any received params
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            setResult(REQUEST_DATA_ENTRY, intent);
            finish();
        }
        // 扫描
        else {

            if (!bLastWrong) {
                TimerTask task = new TimerTask() {
                    public void run() {
                        // execute the task
                        if (bshouleFront) {
                            Log.d(TAG, "正面");
                        } else {
                            Log.d(TAG, "反面");
                        }
                        bLastWrong = false;
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 2000);
                bLastWrong = true;
            }
            Message message = Message.obtain(this.getHandler(), ViewUtil.getResourseIdByName(getApplicationContext().getPackageName(), "id", "decode_failed"));
            message.sendToTarget();
        }
    }
}