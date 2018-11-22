package exocr.idcard;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.kalu.ocr.CaptureActivity;

import java.io.IOException;

public final class CameraManager {

    private static final String TAG = CameraManager.class.getSimpleName();

    private static final int MIN_FRAME_WIDTH = 800;
    private static final int MIN_FRAME_HEIGHT = 480;
    private static final int MAX_FRAME_WIDTH = 1280;
    private static final int MAX_FRAME_HEIGHT = 800;

    private static CameraManager cameraManager;

    static final int SDK_INT; // Later we can use Build.VERSION.SDK_INT

    static {
        int sdkInt;
        try {
            sdkInt = Integer.parseInt(Build.VERSION.SDK);
        } catch (NumberFormatException nfe) {
            // Just to be safe
            sdkInt = 10000;
        }
        SDK_INT = sdkInt;
    }

    private final CameraConfigurationManager configManager;
    private Camera camera;
    private Rect framingRect;
    private Rect framingRectInPreview;
    private boolean initialized;
    private boolean previewing;
    private final boolean useOneShotPreviewCallback;


    private final PreviewCallback previewCallback;
    private final AutoFocusCallback autoFocusCallback;
    private final PictureCallback pictureCallback;

    public static void init(Context context) {
        if (cameraManager == null) {
            cameraManager = new CameraManager(context);
        }
    }

    public static CameraManager get() {
        return cameraManager;
    }

    private CameraManager(Context context) {

        this.configManager = new CameraConfigurationManager(context);
        useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > 3; // 3 = Cupcake

        previewCallback = new PreviewCallback(configManager, useOneShotPreviewCallback);
        autoFocusCallback = new AutoFocusCallback();
        pictureCallback = new PictureCallback();
    }

    public void openCamera(SurfaceHolder holder) throws IOException {
        if (camera == null) {
            camera = Camera.open();
            if (camera == null) {
                throw new IOException();
            }
            camera.setPreviewDisplay(holder);

            if (!initialized) {
                initialized = true;
                configManager.initFromCameraParameters(camera);
            }
            configManager.setDesiredCameraParameters(camera);
        }
    }

    /**
     * Closes the camera driver if still in use.
     */
    public void closeDriver() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public void enableFlashlight() {
        if (camera != null) {
            try {
                Parameters p = camera.getParameters();
                p.setFlashMode(Parameters.FLASH_MODE_TORCH);
                camera.setParameters(p);
            } catch (RuntimeException e) {
                Log.w(TAG, "Could not set flash mode: " + e);
            }
        }
    }

    public void disableFlashlight() {
        if (camera != null) {
            try {
                Parameters p = camera.getParameters();
                p.setFlashMode(Parameters.FLASH_MODE_OFF);
                camera.setParameters(p);
            } catch (RuntimeException e) {
                Log.w(TAG, "Could not set flash mode: " + e);
            }
        }
    }

    public void switchFlashlight() {
        if (camera != null) {
            try {
                Parameters p = camera.getParameters();
                String s = p.get("flash-mode");
                if ("off".equals(s)) {
                    p.setFlashMode(Parameters.FLASH_MODE_TORCH);
                } else {
                    p.setFlashMode(Parameters.FLASH_MODE_OFF);
                }
                camera.setParameters(p);
            } catch (RuntimeException e) {
                Log.w(TAG, "Could not set flash mode: " + e);
            }
        }
    }

    public final void startPreview() {
        if (camera != null && !previewing) {
            camera.startPreview();
            previewing = true;
        }
    }

    public final void stopPreview() {
        if (camera != null && previewing) {
            if (!useOneShotPreviewCallback) {
                camera.setPreviewCallback(null);
            }
            camera.stopPreview();
            previewCallback.setHandler(null, 0);
            autoFocusCallback.setHandler(null, 0);
            previewing = false;
        }
    }

    public final void requestPreviewFrame(DecodeHandler handler, int message) {
        if (camera != null && previewing) {
            previewCallback.setHandler(handler, message);
            if (useOneShotPreviewCallback) {
                camera.setOneShotPreviewCallback(previewCallback);
            } else {
                camera.setPreviewCallback(previewCallback);
            }
        }
    }

    public final void requestAutoFocus(Handler handler, int message) {
        if (camera != null && previewing) {
            //autoFocusCallback.SetAF(false);
            autoFocusCallback.setHandler(handler, message);
            //Log.d(TAG, "Requesting auto-focus callback");
            camera.autoFocus(autoFocusCallback);
        }
    }

    public Point getResolution() {
        return configManager.getScreenResolution();
    }

    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    public Rect getFramingRect() {
        Point screenResolution = configManager.getScreenResolution();
        if (framingRect == null) {
            if (camera == null) {
                return null;
            }
            float cardw = 0;
            float cardh = 0;
            float fper = 0.63084f;
            float heightPad = 50;
            if (screenResolution.x * fper - heightPad > screenResolution.y) {
                cardh = screenResolution.y - heightPad;
                cardw = cardh / fper;
            } else {
                cardw = screenResolution.x;
                cardh = cardw * fper;
            }

            //适配小屏
            if (cardh > screenResolution.y) {
                heightPad = 10;
                cardh = screenResolution.y - heightPad;
                cardw = cardh / fper;
            }

            float lft = (screenResolution.x - cardw) / 2;
            float top = heightPad / 2;

            framingRect = new Rect((int) lft, (int) top, (int) (lft + cardw), (int) (top + cardh));
        }
        return framingRect;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
     * not UI / screen.
     */
    public Rect getFramingRectInPreview() {
        if (framingRectInPreview == null) {
            Rect rect = new Rect(getFramingRect());
            Point cameraResolution = configManager.getCameraResolution();
            Point screenResolution = configManager.getScreenResolution();
            rect.left = rect.left * cameraResolution.x / screenResolution.x;
            rect.right = rect.right * cameraResolution.x / screenResolution.x;
            rect.top = rect.top * cameraResolution.y / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }

    /***
     * return camera
     */
    public Camera getCamera() {
        if (camera != null) {
            return camera;
        }
        return null;
    }

    /**
     * take picture
     */
    public void takePicture(CaptureActivity activity) {
        //stopPreview();// stop the preview
        previewing = false;
        pictureCallback.SetActivity(activity);
        camera.takePicture(activity.getShutterCallback(), null, pictureCallback); // picture
    }

    public CameraConfigurationManager getCCM() {
        return this.configManager;
    }
    /**
     * Converts the result points from still resolution coordinates to screen coordinates.
     *
     * @param points The points returned by the Reader subclass through Result.getResultPoints().
     * @return An array of Points scaled to the size of the framing rect and offset appropriately
     *         so they can be drawn in screen coordinates.
     */
  /*
  public Point[] convertResultPoints(ResultPoint[] points) {
    Rect frame = getFramingRectInPreview();
    int count = points.length;
    Point[] output = new Point[count];
    for (int x = 0; x < count; x++) {
      output[x] = new Point();
      output[x].x = frame.left + (int) (points[x].getX() + 0.5f);
      output[x].y = frame.top + (int) (points[x].getY() + 0.5f);
    }
    return output;
  }
   */

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data A preview frame.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
  /*
  public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
    Rect rect = getFramingRectInPreview();
    int previewFormat = configManager.getPreviewFormat();
    String previewFormatString = configManager.getPreviewFormatString();
    switch (previewFormat) {
      // This is the standard Android format which all devices are REQUIRED to support.
      // In theory, it's the only one we should ever care about.
      case PixelFormat.YCbCr_420_SP:
      // This format has never been seen in the wild, but is compatible as we only care
      // about the Y channel, so allow it.
      case PixelFormat.YCbCr_422_SP:
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
            rect.width(), rect.height());
      default:
        // The Samsung Moment incorrectly uses this variant instead of the 'sp' version.
        // Fortunately, it too has all the Y data up front, so we can read it.
        if ("yuv420p".equals(previewFormatString)) {
          return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
            rect.width(), rect.height());
        }
    }
  
    throw new IllegalArgumentException("Unsupported picture format: " +
        previewFormat + '/' + previewFormatString);
  }
  */

}
