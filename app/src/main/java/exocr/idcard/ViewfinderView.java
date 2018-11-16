/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package exocr.idcard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import exocr.exocrengine.EXIDCardResult;
import exocr.idcard.Torch;


/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {
  private static final String TAG = ViewfinderView.class.getSimpleName();
  private static final long ANIMATION_DELAY = 10L;
  private static final int OPAQUE = 0xFF;

  private static final float GUIDE_FONT_SIZE = 22.0f;
  private static final int TORCH_WIDTH = 70;
  private static final int TORCH_HEIGHT = 50;
  private final Torch mTorch;
  private float mScale = 1;
  private Rect mTorchRect;
  
  private int lineStep = 0;
  private Rect mlineRect;
  private Drawable lineDrawable;
  private final int LINE_WIDTH = 60;
  private final int LINE_SPEED = 5;
 
  private final Paint paint;
  private final int maskColor;
  private final int frameColor;
  private final int boxColor;
  private Bitmap logo;
  private int tipColor;
  private String tipText;
  private final String supportText;
  private final int supportColor;
  private final float tipTextSize;
  private static final int BUTTON_TOUCH_TOLERANCE = 20;
  private boolean bLight;
  // This constructor is used when the class is built from an XML resource.
  public ViewfinderView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // Initialize these once for performance rather than calling them every time in onDraw().
    paint = new Paint();
    Resources resources = getResources();
    maskColor   = resources.getColor(ViewUtil.getResourseIdByName(context.getPackageName(), "color", "viewfinder_mask"));
    frameColor  = resources.getColor(ViewUtil.getResourseIdByName(context.getPackageName(), "color", "viewfinder_frame"));
    boxColor = resources.getColor(ViewUtil.getResourseIdByName(context.getPackageName(), "color", "viewfinder_box"));
//    maskColor   = resources.getColor(R.color.viewfinder_mask);
//    resources.getColor(R.color.result_view);
//    frameColor  = resources.getColor(R.color.viewfinder_frame);
//    resources.getColor(R.color.viewfinder_laser);
//    resources.getColor(R.color.possible_result_points);
//    boxColor = resources.getColor(R.color.viewfinder_box);
    tipColor = Color.GREEN;
    supportColor = Color.LTGRAY;
    supportText  = new String("本技术由易道博识提供");
    
    mScale = getResources().getDisplayMetrics().density / 1.5f;
    
    tipTextSize = GUIDE_FONT_SIZE * mScale;
    mTorch = new Torch(TORCH_WIDTH * mScale, TORCH_HEIGHT * mScale);
    
    Point topEdgeUIOffset;
    //topEdgeUIOffset = new Point((int) (60 * mScale), (int) (40 * mScale));
    topEdgeUIOffset = new Point((int) (TORCH_WIDTH * 1), (int) (TORCH_HEIGHT * 1));
    Point torchPoint = new Point( topEdgeUIOffset.x,  topEdgeUIOffset.y);
    // mTorchRect used only for touch lookup, not layout
    mTorchRect = ViewUtil.rectGivenCenter(torchPoint, (int)(TORCH_WIDTH * 1), (int)(TORCH_HEIGHT * 1));
    bLight = false;
    
    mlineRect = new Rect();
    int lineDrawableId = ViewUtil.getResourseIdByName(context.getPackageName(), "drawable", "scan_line_portrait");
//    lineDrawable = context.getResources().getDrawable(R.drawable.scan_line_portrait);
    lineDrawable = context.getResources().getDrawable(lineDrawableId);
  }

  /** 
  * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
  */  
  public static int dip2px(Context context, float dpValue) {  
    final float scale = context.getResources().getDisplayMetrics().density;  
    return (int) (dpValue * scale + 0.5f);  
  }  
    
  /** 
  * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
  */  
  public static int px2dip(Context context, float pxValue) {  
    final float scale = context.getResources().getDisplayMetrics().density;  
    return (int) (pxValue / scale + 0.5f);  
  }
  
  @Override
  public void onDraw(Canvas canvas) {
    Rect frame = CameraManager.get().getFramingRect();
    if (frame == null) {
      return;
    }
    
	if ((lineStep += LINE_SPEED) < frame.right - frame.left - LINE_WIDTH) {
		canvas.save();
		mlineRect.set(frame.left + lineStep, frame.top, frame.left + LINE_WIDTH + lineStep, frame.bottom);
		lineDrawable.setBounds(mlineRect);
		lineDrawable.draw(canvas);
		canvas.restore();
	} else {
		lineStep = 0;
	}
    
    int width = canvas.getWidth();
    int height = canvas.getHeight();
    int lw = 16;
    int lineWidth = 10;
    int roundWidth = (frame.right - frame.left) / 20;
    canvas.save();
    // Draw the exterior (i.e. outside the framing rect) darkened
    paint.setColor(maskColor);
    canvas.drawRect(0, 0, width, frame.top, paint);
    canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
    canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
    canvas.drawRect(0, frame.bottom + 1, width, height, paint);

      paint.setColor(boxColor);
      canvas.drawRect(frame.left, frame.top, frame.right, frame.top+1, paint);
      canvas.drawRect(frame.right-1, frame.top, frame.right, frame.bottom, paint);
      canvas.drawLine(frame.left, frame.top, frame.left+1, frame.bottom, paint);
      canvas.drawLine(frame.left, frame.bottom-1, frame.right, frame.bottom, paint);
      
      // Draw a two pixel solid black border inside the framing rect
      paint.setColor(frameColor);
      //canvas.drawRect(frame.left, frame.top, frame.right + 1, frame.top + 2, paint);
      //canvas.drawRect(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1, paint);
      //canvas.drawRect(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1, paint);
      //canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1, paint);
      
      canvas.drawRect(frame.left, frame.top, frame.left+roundWidth, frame.top+lineWidth, paint);
      canvas.drawRect(frame.left, frame.top, frame.left+lineWidth, frame.top+roundWidth, paint);
      
      canvas.drawRect(frame.right-roundWidth, frame.top, frame.right, frame.top+lineWidth, paint);
      canvas.drawRect(frame.right-lineWidth, frame.top, frame.right, frame.top+roundWidth, paint);
      
      canvas.drawRect(frame.left, frame.bottom-lineWidth, frame.left+roundWidth, frame.bottom, paint);
      canvas.drawRect(frame.left, frame.bottom-roundWidth, frame.left+lineWidth, frame.bottom, paint);
      
      canvas.drawRect(frame.right-roundWidth, frame.bottom-lineWidth, frame.right, frame.bottom, paint);
      canvas.drawRect(frame.right-lineWidth, frame.bottom-roundWidth, frame.right, frame.bottom, paint);

      /*
      //+
      int midx = (frame.left+frame.right)/2;
      int midy = (frame.top +frame.bottom)/2;
      // Draw a red "laser scanner" line through the middle to show decoding is active
      paint.setColor(laserColor);
      paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
      scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
      int middle = frame.height() / 2 + frame.top;
      //canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 1, paint);
      canvas.drawRect(midx-Round-3, midy-3, midx+Round+3, midy+3, paint);
      canvas.drawRect(midx-3, midy-Round-3, midx+3, midy+Round+3, paint);
      //int half = (Round+1)/2;
      //canvas.drawLine(midx-half, midy-half, midx+half, midy-half, paint);
      //canvas.drawLine(midx-half, midy+half, midx+half, midy+half, paint);
      //canvas.drawLine(midx-half, midy-half, midx-half, midy+half, paint);
      //canvas.drawLine(midx+half, midy-half, midx+half, midy+half, paint);
      */ 
		if(logo != null && EXIDCardResult.DISPLAY_LOGO){
		     paint.setAlpha(OPAQUE);
		     canvas.drawBitmap(logo, width - logo.getWidth() , 0, paint);
		}
		
		if(tipText != null){
			paint.setTextAlign(Align.CENTER);
			paint.setColor(tipColor);
			paint.setTextSize(tipTextSize);
			canvas.translate(frame.left + frame.width()/2, frame.top + frame.height()*1/3);
		    canvas.drawText(tipText, 0, 0, paint);
		}
		
		if(supportText != null && EXIDCardResult.DISPLAY_LOGO){
			canvas.save();
			paint.setTextAlign(Align.CENTER);
			paint.setColor(supportColor);
			paint.setTextSize(tipTextSize);
			canvas.translate(0, frame.height()*2/3 - tipTextSize);
		    canvas.drawText(supportText, 0, 0, paint);
		    canvas.restore();
		}
      
		if (mTorch != null) {
            canvas.translate(mTorchRect.exactCenterX() - (frame.left + frame.width()/2), mTorchRect.exactCenterY() - (frame.top + frame.height()*1/3));
            mTorch.draw(canvas);
        }
		
		canvas.restore();
		
      // Request another update at the animation interval, but only repaint the laser line,
      // not the entire viewfinder mask.
      postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
   }

  public void drawViewfinder() {
    invalidate();
  }

	public void OnFlashBtnClick(View view){
		if(bLight){
			CameraManager.get().disableFlashlight();
			bLight = false;
		}else{
			CameraManager.get().enableFlashlight();
			bLight = true;
		}
	}
	
	
  @Override
  public boolean onTouchEvent(MotionEvent event) {
      try {
          int action;
          action = event.getAction() & MotionEvent.ACTION_MASK;
          if (action == MotionEvent.ACTION_DOWN) {
              Point p = new Point((int) event.getX(), (int) event.getY());
              Rect r = ViewUtil.rectGivenCenter(p, BUTTON_TOUCH_TOLERANCE, BUTTON_TOUCH_TOLERANCE);
              if (mTorchRect != null && Rect.intersects(mTorchRect, r)) {
                  Log.d(TAG, "torch touched");
                  
                  if(bLight){
          			CameraManager.get().disableFlashlight();
          			bLight = false;
          		  }else{
          			CameraManager.get().enableFlashlight();
          			bLight = true;
          		  }
                  mTorch.setOn(bLight);
              } 
          }
      } catch (NullPointerException e) {
          // Un-reproducible NPE reported on device without flash where flash detected and flash
          // button pressed (see https://github.com/paypal/PayPal-Android-SDK/issues/27)
          //Log.d(TAG, "NullPointerException caught in onTouchEvent method");
      }
      return false;
  }
  
  
  /**
   * Draw a bitmap with the result points highlighted instead of the live scanning display.
   *
   * @param barcode An image of the decoded barcode.
   */
  public void drawResultBitmap(Bitmap barcode) {
    invalidate();
  }
  
  public void setLogo(Bitmap ilogo){
	  this.logo = ilogo;
  }
  public void setTipText(String str){
	  this.tipText = str;
  }
  public void setTipColor(int color) {
	  this.tipColor = color;
  }
}
