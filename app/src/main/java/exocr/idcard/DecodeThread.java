package exocr.idcard;

import android.os.Looper;
import android.util.Log;

import com.kalu.ocr.CaptureActivity;

import java.util.concurrent.CountDownLatch;


/**
 * description: 解码
 * create by kalu on 2018/11/20 9:41
 */
final class DecodeThread extends Thread {

    private final CaptureActivity activity;
    private DecodeHandler handler;

    // 并发
    private final CountDownLatch countDown = new CountDownLatch(1);

    DecodeThread(CaptureActivity activity) {
        this.activity = activity;
    }

    DecodeHandler getHandler() {
        try {
            countDown.await();
        } catch (Exception e) {
            Log.e("kalu", "getHandler ==> " + e.getMessage(), e);
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(activity);
        countDown.countDown();
        Looper.loop();
    }
}
