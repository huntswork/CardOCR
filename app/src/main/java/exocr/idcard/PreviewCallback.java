package exocr.idcard;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * description: 预览回调
 * create by kalu on 2018/11/20 9:51
 */
public final class PreviewCallback implements Camera.PreviewCallback {

    public static final int PARSE_DECODE = 1001;
    public static final int PARSE_SUCC = 1002;
    public static final int PARSE_FAIL = 1003;

    private static final String TAG = PreviewCallback.class.getSimpleName();

    private final CameraConfigurationManager configManager;
    private final boolean useOneShotPreviewCallback;
    private Handler handler;
    private int what;

    PreviewCallback(CameraConfigurationManager configManager, boolean useOneShotPreviewCallback) {
        this.configManager = configManager;
        this.useOneShotPreviewCallback = useOneShotPreviewCallback;
    }

    final void setHandler(DecodeHandler handler, int what) {
        this.handler = handler;
        this.what = what;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        Point cameraResolution = configManager.getCameraResolution();
        if (!useOneShotPreviewCallback) {
            camera.setPreviewCallback(null);
        }
        if (handler != null) {
            Message message = handler.obtainMessage(what, cameraResolution.x, cameraResolution.y, data);
            message.sendToTarget();
            handler = null;
        } else {
            Log.d(TAG, "Got preview callback, but no handler for it");
        }
    }
}
