package exocr.bankcard;

/* ViewUtil.java
 * See the file "LICENSE.md" for the full license governing this code.
 */

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Debug;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Utility methods for altering views.
 *
 */
public class ViewUtil {

    /**
     * Wrapper to only use the deprecated {@link View#setBackgroundDrawable} on
     * older systems.
     *
     * @param view
     * @param drawable
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    // DIMENSION HELPERS

    // see also similar work: http://stackoverflow.com/a/11353603/306657
    private static final Map<String, Integer> DIMENSION_STRING_CONSTANT =
            initDimensionStringConstantMap();
    static Pattern DIMENSION_VALUE_PATTERN = Pattern
            .compile("^\\s*(\\d+(\\.\\d+)*)\\s*([a-zA-Z]+)\\s*$");

    static Map<String, Integer> initDimensionStringConstantMap() {
        Map<String, Integer> m = new HashMap<String, Integer>();
        m.put("px", TypedValue.COMPLEX_UNIT_PX);
        m.put("dip", TypedValue.COMPLEX_UNIT_DIP);
        m.put("dp", TypedValue.COMPLEX_UNIT_DIP);
        m.put("sp", TypedValue.COMPLEX_UNIT_SP);
        m.put("pt", TypedValue.COMPLEX_UNIT_PT);
        m.put("in", TypedValue.COMPLEX_UNIT_IN);
        m.put("mm", TypedValue.COMPLEX_UNIT_MM);
        return Collections.unmodifiableMap(m);
    }

    public static int typedDimensionValueToPixelsInt(String dimensionValueString, Context context) {
        if (dimensionValueString == null) {
            return 0;
        } else {
            return (int) typedDimensionValueToPixels(dimensionValueString, context);
        }
    }

    static HashMap<String, Float> pxDimensionLookupTable = new HashMap<String, Float>();

    @SuppressLint("DefaultLocale")
    public static float typedDimensionValueToPixels(String dimensionValueString, Context context) {
        if (dimensionValueString == null) {
            return 0;
        }
        dimensionValueString = dimensionValueString.toLowerCase();
        if (pxDimensionLookupTable.containsKey(dimensionValueString)) {
            return pxDimensionLookupTable.get(dimensionValueString);
        }
        Matcher m = DIMENSION_VALUE_PATTERN.matcher(dimensionValueString);
        if (!m.matches()) {
            throw new NumberFormatException();
        }
        float value = Float.parseFloat(m.group(1));
        String dimensionString = m.group(3).toLowerCase();
        Integer unit = DIMENSION_STRING_CONSTANT.get(dimensionString);
        if (unit == null) {
            unit = TypedValue.COMPLEX_UNIT_DIP;
        }
        float ret =
                TypedValue.applyDimension(unit, value, context.getResources().getDisplayMetrics());
        pxDimensionLookupTable.put(dimensionValueString, ret);
        return ret;
    }

    // ATTRIBUTE HELPERS

    public static void setPadding(View view, String left, String top, String right, String bottom) {
        Context context = view.getContext();
        view.setPadding(
                typedDimensionValueToPixelsInt(left, context),
                typedDimensionValueToPixelsInt(top, context),
                typedDimensionValueToPixelsInt(right, context),
                typedDimensionValueToPixelsInt(bottom, context));
    }

    // LAYOUT PARAM HELPERS

    /**
     * Set margins for given view if its LayoutParams are MarginLayoutParams.
     * Should be used after the view is already added to a layout.
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @category layout
     */
    public static void setMargins(View view, String left, String top, String right, String bottom) {
        Context context = view.getContext();
        LayoutParams params = view.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) params).setMargins(
                    typedDimensionValueToPixelsInt(left, context),
                    typedDimensionValueToPixelsInt(top, context),
                    typedDimensionValueToPixelsInt(right, context),
                    typedDimensionValueToPixelsInt(bottom, context));
        }
    }

    public static void setDimensions(View view, int width, int height) {
        LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
    }

//    public static void styleAsButton(View view, boolean primary, Context context) {
//    	
//        setDimensions(view, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//        setPadding(view, "10dip", "0dip", "10dip", "0dip");
//        setBackground(view, primary ? Appearance.buttonBackgroundPrimary(context) : 
//        	                          Appearance.buttonBackgroundSecondary(context));
//
//        view.setFocusable(true);
//        view.setMinimumHeight(ViewUtil.typedDimensionValueToPixelsInt(Appearance.BUTTON_HEIGHT, context));
//        
//        if (view instanceof TextView) {
//            styleAsButtonText((TextView) view);
//        }
//        if (!(view instanceof Button)) {
//            view.setClickable(true);
//        }
//    }
//
//    public static void styleAsButtonText(TextView textView) {
//        textView.setGravity(Gravity.CENTER);
//        textView.setTextColor(Appearance.TEXT_COLOR_BUTTON);
//        textView.setTextSize(Appearance.TEXT_SIZE_BUTTON);
//        textView.setTypeface(Appearance.TYPEFACE_BUTTON);
//    }

    public static Bitmap base64ToBitmap(String base64Data, Context context) {
        return base64ToBitmap(base64Data, context, DisplayMetrics.DENSITY_HIGH);
    }

    public static Bitmap base64ToBitmap(String base64Data, Context context,
            int displayMetricsDensity) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (context != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            options.inTargetDensity = metrics.densityDpi;
        } else {
            options.inTargetDensity = DisplayMetrics.DENSITY_MEDIUM;
        }
        options.inDensity = displayMetricsDensity;
        options.inScaled = false;

        byte[] imageBytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////
    private static final boolean TORCH_BLACK_LISTED = (Build.MODEL.equals("DROID2"));
    public static final String PUBLIC_LOG_TAG = "excardrec.jar";
    private static Boolean sHardwareSupported;

    public static boolean deviceSupportsTorch(Context context) {
        return !TORCH_BLACK_LISTED
                && context.getPackageManager()
                        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    @SuppressWarnings("rawtypes")
    public static String manifestHasConfigChange(ResolveInfo resolveInfo, Class activityClass) {
        String error = null;
        if (resolveInfo == null) {
            error = String.format("Didn't find %s in the AndroidManifest.xml",
                    activityClass.getName());
        } else if (!ViewUtil.hasConfigFlag(resolveInfo.activityInfo.configChanges,
                ActivityInfo.CONFIG_ORIENTATION)) {
            error = activityClass.getName()
                    + " requires attribute android:configChanges=\"orientation\"";
        }
        if (error != null)
            Log.e(ViewUtil.PUBLIC_LOG_TAG, error);
        return error;
    }

    public static boolean hasConfigFlag(int config, int configFlag) {
        return ((config & configFlag) == configFlag);
    }

    /* --- HARDWARE SUPPORT --- */

    public static boolean hardwareSupported() {
        if (sHardwareSupported == null) {
            sHardwareSupported = hardwareSupportCheck();
        }
        return sHardwareSupported;
    }

    public static boolean hardwareSupportCheck() {
        Log.i(PUBLIC_LOG_TAG, "Checking hardware support...");

        // we currently need froyo or better (aka Android 2.2, API level 8)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            Log.w(PUBLIC_LOG_TAG,
                    "- Android SDK too old. Minimum Android 2.2 / API level 8+ (Froyo) required");
            return false;
        }
        // Camera needs to open
        Camera c = null;
        try {
            c = Camera.open();
        } catch (RuntimeException e) {
            Log.w(PUBLIC_LOG_TAG, "- Error opening camera: " + e);
            throw new RuntimeException();
        }
        if (c == null) {
            Log.w(PUBLIC_LOG_TAG, "- No camera found");
            return false;
        } else {
            List<Camera.Size> list = c.getParameters().getSupportedPreviewSizes();
            c.release();

            boolean supportsVGA = false;

            for (Camera.Size s : list) {
                if (s.width == 640 && s.height == 480) {
                    supportsVGA = true;
                    break;
                }
            }

            if (!supportsVGA) {
                Log.w(PUBLIC_LOG_TAG, "- Camera resolution is insufficient");
                return false;
            }
        }
        return true;
    }

    public static String getNativeMemoryStats() {
        return "(free/alloc'd/total)" + Debug.getNativeHeapFreeSize() + "/"
                + Debug.getNativeHeapAllocatedSize() + "/" + Debug.getNativeHeapSize();
    }

    public static void logNativeMemoryStats() {
        Log.d("MEMORY", "Native memory stats: " + getNativeMemoryStats());
    }

    static public Rect rectGivenCenter(Point center, int width, int height) {
        return new Rect(center.x - width / 2, center.y - height / 2, center.x + width / 2, center.y
                + height / 2);
    }

    static public void setupTextPaintStyle(Paint paint) {
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        paint.setAntiAlias(true);
        float[] black = { 0f, 0f, 0f };
        paint.setShadowLayer(1.5f, 0.5f, 0f, Color.HSVToColor(200, black));
    }
    ///////////////////////////////////////////////////////////////////////////////////////
    public static boolean holoSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
    public static int getResourseIdByName(String packageName, String className,
			String name)
	{
		Class r = null;
		int id = 0;
		try
		{
			r = Class.forName(packageName + ".R");

			Class[] classes = r.getClasses();
			Class desireClass = null;

			for (int i = 0; i < classes.length; i++)
			{
				if (classes[i].getName().split("\\$")[1].equals(className))
				{
					desireClass = classes[i];
					break;
				}
			}

			if (desireClass != null)
				id = desireClass.getField(name).getInt(desireClass);
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		} catch (SecurityException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		} catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}

		return id;

	}
}
