package exocr.idcard;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.kalu.ocr.CaptureActivity;

import exocr.exocrengine.EXOCRModel;

/**
 * description: 解析
 * create by kalu on 2018/11/19 10:24
 */
public final class CaptureHandler extends Handler {

    private static final String TAG = CaptureHandler.class.getSimpleName();

    private final CaptureActivity activity;
    private final DecodeThread decodeThread;
    private State state;

    private int auto_focus_id;
    private int restart_preview_id;
    private int return_scan_result_id;
    private int launch_product_query_id;
    private int quit_id;

    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }

    public CaptureHandler(CaptureActivity activity) {
        this.activity = activity;
        decodeThread = new DecodeThread(activity);
        decodeThread.start();
        state = State.SUCCESS;

        auto_focus_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "auto_focus");
        restart_preview_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "restart_preview");
        return_scan_result_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "return_scan_result");
        launch_product_query_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "launch_product_query");
        quit_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "quit");

        // Start ourselves capturing previews and decoding.
        CameraManager.get().startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == auto_focus_id) {
            if (state == State.PREVIEW) {
                CameraManager.get().requestAutoFocus(this, auto_focus_id);
            }
        } else if (message.what == restart_preview_id) {
            Log.d(TAG, "Got restart preview message");
            restartPreviewAndDecode();
        } else if (message.what == PreviewCallback.PARSE_SUCC) {
            Log.d(TAG, "Got decode succeeded message");
            state = State.SUCCESS;
            activity.handleDecode((EXOCRModel) message.obj);
        } else if (message.what == PreviewCallback.PARSE_FAIL) {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), PreviewCallback.PARSE_DECODE);
        } else if (message.what == return_scan_result_id) {
            Log.d(TAG, "Got return scan result message");
            activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
            activity.finish();
        } else if (message.what == launch_product_query_id) {
            Log.d(TAG, "Got product query message");
            String url = (String) message.obj;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            activity.startActivity(intent);
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        CameraManager.get().stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), quit_id);
        quit.sendToTarget();
        try {
            decodeThread.join();
        } catch (InterruptedException e) {
            // continue
        }

        removeMessages(PreviewCallback.PARSE_SUCC);
        removeMessages(PreviewCallback.PARSE_FAIL);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), PreviewCallback.PARSE_DECODE);
            CameraManager.get().requestAutoFocus(this, auto_focus_id);
        }
    }

    public void restartAutoFocus() {
        state = State.PREVIEW;
        CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), PreviewCallback.PARSE_DECODE);
        CameraManager.get().requestAutoFocus(this, auto_focus_id);
    }

    public void takePicture() {
        CameraManager.get().takePicture(activity);
    }
}
