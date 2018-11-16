package exocr.idcard;

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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Debug;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

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
	
	
    //////////////////////////////////////////////////////////////////
    //static final String exocr_logo = "iVBORw0KGgoAAAANSUhEUgAAAJ4AAAAyCAYAAACtW2LuAAAACXBIWXMAAAsTAAALEwEAmpwYAAAKT2lDQ1BQaG90b3Nob3AgSUNDIHByb2ZpbGUAAHjanVNnVFPpFj333vRCS4iAlEtvUhUIIFJCi4AUkSYqIQkQSoghodkVUcERRUUEG8igiAOOjoCMFVEsDIoK2AfkIaKOg6OIisr74Xuja9a89+bN/rXXPues852zzwfACAyWSDNRNYAMqUIeEeCDx8TG4eQuQIEKJHAAEAizZCFz/SMBAPh+PDwrIsAHvgABeNMLCADATZvAMByH/w/qQplcAYCEAcB0kThLCIAUAEB6jkKmAEBGAYCdmCZTAKAEAGDLY2LjAFAtAGAnf+bTAICd+Jl7AQBblCEVAaCRACATZYhEAGg7AKzPVopFAFgwABRmS8Q5ANgtADBJV2ZIALC3AMDOEAuyAAgMADBRiIUpAAR7AGDIIyN4AISZABRG8lc88SuuEOcqAAB4mbI8uSQ5RYFbCC1xB1dXLh4ozkkXKxQ2YQJhmkAuwnmZGTKBNA/g88wAAKCRFRHgg/P9eM4Ors7ONo62Dl8t6r8G/yJiYuP+5c+rcEAAAOF0ftH+LC+zGoA7BoBt/qIl7gRoXgugdfeLZrIPQLUAoOnaV/Nw+H48PEWhkLnZ2eXk5NhKxEJbYcpXff5nwl/AV/1s+X48/Pf14L7iJIEyXYFHBPjgwsz0TKUcz5IJhGLc5o9H/LcL//wd0yLESWK5WCoU41EScY5EmozzMqUiiUKSKcUl0v9k4t8s+wM+3zUAsGo+AXuRLahdYwP2SycQWHTA4vcAAPK7b8HUKAgDgGiD4c93/+8//UegJQCAZkmScQAAXkQkLlTKsz/HCAAARKCBKrBBG/TBGCzABhzBBdzBC/xgNoRCJMTCQhBCCmSAHHJgKayCQiiGzbAdKmAv1EAdNMBRaIaTcA4uwlW4Dj1wD/phCJ7BKLyBCQRByAgTYSHaiAFiilgjjggXmYX4IcFIBBKLJCDJiBRRIkuRNUgxUopUIFVIHfI9cgI5h1xGupE7yAAygvyGvEcxlIGyUT3UDLVDuag3GoRGogvQZHQxmo8WoJvQcrQaPYw2oefQq2gP2o8+Q8cwwOgYBzPEbDAuxsNCsTgsCZNjy7EirAyrxhqwVqwDu4n1Y8+xdwQSgUXACTYEd0IgYR5BSFhMWE7YSKggHCQ0EdoJNwkDhFHCJyKTqEu0JroR+cQYYjIxh1hILCPWEo8TLxB7iEPENyQSiUMyJ7mQAkmxpFTSEtJG0m5SI+ksqZs0SBojk8naZGuyBzmULCAryIXkneTD5DPkG+Qh8lsKnWJAcaT4U+IoUspqShnlEOU05QZlmDJBVaOaUt2ooVQRNY9aQq2htlKvUYeoEzR1mjnNgxZJS6WtopXTGmgXaPdpr+h0uhHdlR5Ol9BX0svpR+iX6AP0dwwNhhWDx4hnKBmbGAcYZxl3GK+YTKYZ04sZx1QwNzHrmOeZD5lvVVgqtip8FZHKCpVKlSaVGyovVKmqpqreqgtV81XLVI+pXlN9rkZVM1PjqQnUlqtVqp1Q61MbU2epO6iHqmeob1Q/pH5Z/YkGWcNMw09DpFGgsV/jvMYgC2MZs3gsIWsNq4Z1gTXEJrHN2Xx2KruY/R27iz2qqaE5QzNKM1ezUvOUZj8H45hx+Jx0TgnnKKeX836K3hTvKeIpG6Y0TLkxZVxrqpaXllirSKtRq0frvTau7aedpr1Fu1n7gQ5Bx0onXCdHZ4/OBZ3nU9lT3acKpxZNPTr1ri6qa6UbobtEd79up+6Ynr5egJ5Mb6feeb3n+hx9L/1U/W36p/VHDFgGswwkBtsMzhg8xTVxbzwdL8fb8VFDXcNAQ6VhlWGX4YSRudE8o9VGjUYPjGnGXOMk423GbcajJgYmISZLTepN7ppSTbmmKaY7TDtMx83MzaLN1pk1mz0x1zLnm+eb15vft2BaeFostqi2uGVJsuRaplnutrxuhVo5WaVYVVpds0atna0l1rutu6cRp7lOk06rntZnw7Dxtsm2qbcZsOXYBtuutm22fWFnYhdnt8Wuw+6TvZN9un2N/T0HDYfZDqsdWh1+c7RyFDpWOt6azpzuP33F9JbpL2dYzxDP2DPjthPLKcRpnVOb00dnF2e5c4PziIuJS4LLLpc+Lpsbxt3IveRKdPVxXeF60vWdm7Obwu2o26/uNu5p7ofcn8w0nymeWTNz0MPIQ+BR5dE/C5+VMGvfrH5PQ0+BZ7XnIy9jL5FXrdewt6V3qvdh7xc+9j5yn+M+4zw33jLeWV/MN8C3yLfLT8Nvnl+F30N/I/9k/3r/0QCngCUBZwOJgUGBWwL7+Hp8Ib+OPzrbZfay2e1BjKC5QRVBj4KtguXBrSFoyOyQrSH355jOkc5pDoVQfujW0Adh5mGLw34MJ4WHhVeGP45wiFga0TGXNXfR3ENz30T6RJZE3ptnMU85ry1KNSo+qi5qPNo3ujS6P8YuZlnM1VidWElsSxw5LiquNm5svt/87fOH4p3iC+N7F5gvyF1weaHOwvSFpxapLhIsOpZATIhOOJTwQRAqqBaMJfITdyWOCnnCHcJnIi/RNtGI2ENcKh5O8kgqTXqS7JG8NXkkxTOlLOW5hCepkLxMDUzdmzqeFpp2IG0yPTq9MYOSkZBxQqohTZO2Z+pn5mZ2y6xlhbL+xW6Lty8elQfJa7OQrAVZLQq2QqboVFoo1yoHsmdlV2a/zYnKOZarnivN7cyzytuQN5zvn//tEsIS4ZK2pYZLVy0dWOa9rGo5sjxxedsK4xUFK4ZWBqw8uIq2Km3VT6vtV5eufr0mek1rgV7ByoLBtQFr6wtVCuWFfevc1+1dT1gvWd+1YfqGnRs+FYmKrhTbF5cVf9go3HjlG4dvyr+Z3JS0qavEuWTPZtJm6ebeLZ5bDpaql+aXDm4N2dq0Dd9WtO319kXbL5fNKNu7g7ZDuaO/PLi8ZafJzs07P1SkVPRU+lQ27tLdtWHX+G7R7ht7vPY07NXbW7z3/T7JvttVAVVN1WbVZftJ+7P3P66Jqun4lvttXa1ObXHtxwPSA/0HIw6217nU1R3SPVRSj9Yr60cOxx++/p3vdy0NNg1VjZzG4iNwRHnk6fcJ3/ceDTradox7rOEH0x92HWcdL2pCmvKaRptTmvtbYlu6T8w+0dbq3nr8R9sfD5w0PFl5SvNUyWna6YLTk2fyz4ydlZ19fi753GDborZ752PO32oPb++6EHTh0kX/i+c7vDvOXPK4dPKy2+UTV7hXmq86X23qdOo8/pPTT8e7nLuarrlca7nuer21e2b36RueN87d9L158Rb/1tWeOT3dvfN6b/fF9/XfFt1+cif9zsu72Xcn7q28T7xf9EDtQdlD3YfVP1v+3Njv3H9qwHeg89HcR/cGhYPP/pH1jw9DBY+Zj8uGDYbrnjg+OTniP3L96fynQ89kzyaeF/6i/suuFxYvfvjV69fO0ZjRoZfyl5O/bXyl/erA6xmv28bCxh6+yXgzMV70VvvtwXfcdx3vo98PT+R8IH8o/2j5sfVT0Kf7kxmTk/8EA5jz/GMzLdsAAAAgY0hSTQAAeiUAAICDAAD5/wAAgOkAAHUwAADqYAAAOpgAABdvkl/FRgAACWlJREFUeNrsXXtwVsUV/50kaACtgEZStBgBkQSEWlpqrXV8VKpWmdiKnU59DTAj2o4jRetQptapU2da6Qi2CKIday2ILcXB+uwIGukDxwFEIA9FMUjLSxIe4U3y6x/fxrlc7t2z++XeJPa7ZyYzyXf37J793d89e87uuV+EJDLJpLOlKIMgk4x4mWTEyySTVIUkouI8kkJyPsk25iefkFxJch7J60iekKENkBxLsilPTA+S/IDk8ySnkqzohvO7xWL/2k/5ZiHeKCYr20jeQVIKnHivJ4hpG8lnSX6hG81vocXe/e18sy21IxK26XQAswH8tcC9X5K4CoAbAKwh+dVuMr9hlmvvucR456Zk2HUAZhaot+sH4LQUuu4L4EWSA7p4fkUKb+pciFeVoo2TSY4sQO6liempAB7o4vmdBaDUcr2+Kz1e+xIxoQCJd27K/X+fZK8unF+lcv1Tj1cS4zKLHUDaBmBFgMBlAIYC6Odo5MWZx4uUpQBazO+9jRcZYh5WTXoC+DKAN7thfHeMx4vMakkOccioxkat8STHk9ztoL+zAGO8FxRMXorRqyD5imOme1MXzm+exa5WkqVaVuvyZK4+bv0UaRORvwCY5qBfWoAer9IXU4PrRwDGA2jq5rjaPF6jiBzUYjxtmW0WkR2W6y87GNlUSIwjeSKACqVZQ2xQLLIXwD+6Oa62B6s2+EdJnk9mrXL9kIORW5UbVWLilUoA5eYh2Q1gM4C3ReQ/n8HEoqgb4HomgDEABgLoA2CPIetaAO+KyJGUtooawgpRMd6/lDjiCcWISx1ikadjdKtIPkayWdFfR/IHRucKc0S3K+JnE8lBgf6fiWm3i+QbJPt6gF1Fcr2lv0cDbcc7YHKyMt4ahz5OjdDrSfKHJFcpuntJziFZZuL1f1rm9rPQGBcpfU88hm8xxNNu+o8VgH7pANCPQjonkZxtglAfeYTkLKXNZR4376eOpOtB8h2lr1cC7e9T2n6sjNff4dx8Q4TeNebh85GPSV6itHk+NM5Epf1FVuKZCWrybQtA5Y6H4EMDOoNJ1nfgzFIb74zAWL9Q2q5wJJ72cO0PzXGB0v41ZbxHHXAIelgh+esUMX0oZN8MpX0/jXiXOBg1KAacr5OsddBfGdq62cz0ZE/IxmEOB+/9FRKMIXlU6eeOkM5qzXPHjNXHwaO3yzcCpJvDdGVCyM6/WdruCId2JXlsAgLArYasnzN/lwP4EoBzHMOjWcaIkwG8BOCMFIP6+lB2WE9yHeIP6wXA1QCejCFCKYA/Aii2jPkqgDlB7+OwU1BJ8n6zadzDbAYPBXAhAJeiitUistz8/hMAk1NOluo9tuDqooAMe7yZKT8pa03GCpJPOOq0kFxKchHJZWYZc5WnIuY8XdF5zuLtHtY2xsOH9WYDOG35lhnrfJJHHHXWkVxs6vs+9Byvb3CrSInN5x3HtwjivZoiOIdJjjbjjHYsJp0ULqMyichDjmNOiyDPYAeinxihd7FDgH9DhN6VKZPu94Gx3nTZUSA5OMLOyx3Dnm0hvZFK+ykuxPsoJXBaSX4vMM4iLUPTChxJPugwbnWM7kpF78oIsm9UdP4UM9ZdKZJuWfuD6bClcVzsGWHrIPPg2eSNkM71SvurrMQj2SslcJpJXhsYvExZDg6RHOGQWfZ2AGlYjO49it7sUPu5DlsQfWLGeiwlXJ8m2TMwzh985tSBcGJuqL22VVShEe/8hIE5QvJJkp8PGXqLove4xybuMmX8HjF6AxUbGgNtxzrM9TKLjTUJ47qe5LiIAo2dFp0DJE9zxHScMv5dofbzlW2lIi2rrUwg29kBYJXJ7BaKyJaINl9T+njWY7wWy7UNcUdAIrKJ5FsA4krGB5pi1U1xGW5AZorIsg4UB2hyBMB6AMsBLAZQIyLhF2W0krSlIvJJAphGZam2+b0nIm3hD0vy2EqZbkjVGvp8L4AdIrLHoQ+t+mWVx00Z5JHyh2WBhXgAcI3BZIByE6YpZ5hlih0rTB+7Qp8fBtAMYLuItHYQ09UJYXoMrg5bRXVRH/p6vBoReTABr6i9G7DfcUk4C8Bw30kHZBFy73/EFVlOUQ6+jwK4MVjuk6e3u11E3kkZ030efV2t3JtNwZUBQC/fh78oooIiqafGJlqVxnDHfu713OQML7f/hb3USIuJfi4imnfWMD0KYF13wZRkFYBxtiqT0DKvza/OaqxjuXtdQsTTYo3JDgBVA7g9AXsX5DmHfwP4VQJhxfsicrQTMK0mWa5georBozih+M7J41U4HM3UJ0Q8rZ9JJO+MevnbVIVMBfDnBMaBCdbbPO3fB+Bmh7jLxSN0FqYnAVhCsiyGdF80ycsopZ8Gj7ygDYF3aeNiPJdy96Q83usAtHcDZiF3JrwEwEbkSrqHA/gOgDMdxthsqnahLLfbSS4FcIWH/VNFZINj26pOwnSNSURs9YRjAHxAciGAlQAOIHdOfrn5cZFaD4/XGBv/tu+rkLxbK5NJCCCQPMVh47ej8pqHPRM9+n3Bo1/tDJMkb04Q1992wpnwiNCYW32winrZp8qT6XmLiOwG8AjSFR97nzNBvkscNcmj3yTK3X1kBoCDKWLaGlw6zUlNf49lOTLG02KRhoQn8UAHs7mmpOwVkSbk9iY1uU1EtnoSD52Fq4g0ArinA100K9c3ishhj8Si1oV4lZ34ZEJEDgC4Ki74tK0oAH4D4JmkYieSvZHb+bfJUyKy2NNWDVOnONQT198ByGevdT2AGz0xzTtxChKvWMniliTtt0Vkswl45yJ3LOSSlFwoInfD/mb9h2a7w1Uehr2ItRHAnXlMsdhhSwYp4DodwHdNUqbJduQKR0cDeF9pO99jfg0A3o61MVASVYH49z7rRGRbmgGZKSSoRu4Iqxy5L6FpAbDFTOBFEQnGF70AfCWCgG3IVePudRy32sR4Ng97qYjU5DGnEwBcEBPnEbnXNPeniGkJgG+ajHUIckd3JQB2Gg9XA+DvwfNskucZ7MOyRUQaQv0XIXfu3iPiHqwSkZao5OKYLKMQheQAU2xqkxnIJEnM7d8IWgAAiEO19btRlciZZMTrCABTFNIdIjkqo0pGvCQnP5K5L7K2yb0ZTTLiJTnxUvN2lU2Wm6KJTDLiJTbx+x2+P+TsjCLpEq8Q/8HKQbOVESX7AEwQkY0ZRdIVYfZP9DLpAsn+pVQmGfEyyYiXSSYZ8TL5/5P/DQA8XTJjJh/7VgAAAABJRU5ErkJggg==";
	public static final String exocr_logo64 = "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";
	public static final String exocr_logo96 = "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";
	public static final String exocr_backImg = "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";

    //////////////////////////////////////////////////////////////////
	
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
