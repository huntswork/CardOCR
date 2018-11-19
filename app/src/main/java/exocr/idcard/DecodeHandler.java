/*
 * Copyright (C) 2010 ZXing authors
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import exocr.exocrengine.*;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.content.Intent;
import android.graphics.Bitmap;

final class DecodeHandler extends Handler {

private static final String TAG = DecodeHandler.class.getSimpleName();
private final CaptureActivity activity;
private int gcount;

	private int decode_id;
	private int decode_succeeded_id;
	private int decode_failed_id;
	private int quit_id;
	
  DecodeHandler(CaptureActivity activity) {
    this.activity = activity;
    gcount = 0;
	decode_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "decode");
	decode_succeeded_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "decode_succeeded");
	decode_failed_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "decode_failed");
	quit_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "quit");
  }
  
  
  @Override
  public void handleMessage(Message message) {
//		if (message.what == R.id.decode) {
		if (message.what == decode_id) {
			// Log.d(TAG, "Got decode message");
			decode((byte[]) message.obj, message.arg1, message.arg2);
//		} else if (message.what == R.id.quit) {
		} else if (message.what == quit_id) {
			Looper.myLooper().quit();
		}
  }

  /**
   * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
   * reuse the same reader objects from one decode to the next.
   *
   * @param data   The YUV preview frame.
   * @param width  The width of the preview frame.
   * @param height The height of the preview frame.
   */
	private void decode(byte[] data, int width, int height) {
		long start = System.currentTimeMillis();
		int[] rects = new int[32];
		// arg
		int ret = 0;
		EXOCREngine ocrengine = new EXOCREngine();		
		//savetofile(data, width, height);
		//savetoJPEG(data, width, height);
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		ocrengine.timestart = System.currentTimeMillis();
		ret = EXOCREngine.nativeRecoIDCardRawdat(data, width, height, width, 1, ocrengine.bResultBuf, ocrengine.bResultBuf.length);
		ocrengine.timeend   = System.currentTimeMillis();
		
		if (ret > 0) {
			long end = System.currentTimeMillis();
			Log.d(TAG, "Found text (" + (end - start) + " ms):\n");
			EXIDCardResult idcard = EXIDCardResult.decode(ocrengine.bResultBuf, ret);
			//if we have the text to show
			//检测，确保无误。
			if ( idcard != null  && activity.CheckIsEqual(idcard) ) {
				idcard.SetViewType("Preview");
				idcard.SetColorType(CardColorJudge(data, width, height));
				
				//NOTE: 下面这些代码是提取标准身份证图像的，如果客户有需求请打开
				//API For Image Return;
				Bitmap imcard = EXOCREngine.nativeGetIDCardStdImg(data, width, height, ocrengine.bResultBuf, ocrengine.bResultBuf.length, rects);
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
				Message message = Message.obtain(activity.getHandler(),	decode_succeeded_id, idcard);
				message.sendToTarget();
			
				return;
			}
		}
		// retry to focus to the text
//		Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
		Message message = Message.obtain(activity.getHandler(), decode_failed_id);
		message.sendToTarget();
	}
	
	private void savetofile(byte[] data, int width, int height)
	{
		gcount++;
		String tofile = "/mnt/sdcard/test_"+gcount+".raw";
		String ssize = "size=width="+width+"height="+height;
		byte bsize[] = new byte[ssize.length()];
		
		for (int i = 0; i < ssize.length(); ++i){
			bsize[i] = (byte)ssize.charAt(i);
		}
		
		try {
		File file = new File(tofile);
		OutputStream fs = new FileOutputStream(file);// to为要写入sdcard中的文件名称
		fs.write(data, 0, width*height);
		fs.write(bsize);
		fs.close();
		} catch (Exception e) {
			return;
		}
	}
	
	private int CardColorJudge(byte []data, int width, int height)
	{
		int offset = width*height;
		int i;
		int iTht = 144;
		int iCnt = 255;
		int nNum = 0;
		int iSize = width*height/2;
		
		for(i = 0; i < iSize; ++i ){
			int val = data[i+offset]&0xFF;
			if( val > iTht){ ++nNum; }
		}
		
		if(nNum > iCnt) return 1;
		else return 0;
	}
	// save to jpeg
	private void savetoJPEG(byte[] data, int width, int height) {
		int w, h;
		gcount++;
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String date = sDateFormat.format(new java.util.Date());
		
		String tofile = Environment.getExternalStorageDirectory()+File.separator+Environment.DIRECTORY_DCIM+File.separator+date+"_"+gcount+".jpg";
		//String tofile = Environment.()+File.separator+Environment.DIRECTORY_DCIM+File.separator+date+"_"+gcount+".jpg";
		//String tofile = "/sdcard/DCIM/"+"NV21_"+ date+"_"+gcount+".jpg";

		int imageFormat = ImageFormat.NV21;
		Rect frame = new Rect(0, 0, width-1, height-1);
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
		String tofile = Environment.getExternalStorageDirectory()+File.separator+Environment.DIRECTORY_DCIM+File.separator+"image_idcard.jpg";
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
