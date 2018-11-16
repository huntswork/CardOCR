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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;

final class PictureCallback implements Camera.PictureCallback {

	private static final String TAG = PreviewCallback.class.getSimpleName();
	private CaptureActivity activity;

	PictureCallback() {
		this.activity = null;
	}
	
	void SetActivity(CaptureActivity activity) {
		this.activity = activity;
	}

	public void onPictureTaken(byte[] data, Camera camera) {
		//save the jpeg image if needed
		if (CameraManager.get().getCCM().getPictureFormat() == PixelFormat.JPEG) {
			saveImage(data);
		}
		//call for recognition the card by take picture get
		if (this.activity != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			int ret = 0;
			EXOCREngine ocrengine = new EXOCREngine();
			
			ret = EXOCREngine.nativeRecoIDCardBitmap(bitmap, ocrengine.bResultBuf, ocrengine.bResultBuf.length);
			
			if (ret > 0) {
				EXIDCardResult idcard = EXIDCardResult.decode(ocrengine.bResultBuf, ret);
				if(idcard != null){
					activity.SetRecoResult(idcard);
					idcard.SetViewType("Preview");
//					Message message = Message.obtain(activity.getHandler(),	R.id.decode_succeeded, idcard);
					Message message = Message.obtain(activity.getHandler(),	ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "decode_succeeded"), idcard);
					message.sendToTarget();
				}
			}
			this.activity = null;
		}
		CameraManager.get().startPreview();
	}
	
	//convert rgb bitmpa to gray bitmap data
	static private void convert2Gray(Bitmap bmp, byte []data, int width, int height)
	{
		int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
		int nPixelCount = 0;
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int gray = pixels[nPixelCount];

				int red = ((gray & 0x00FF0000) >> 16);
				int green = ((gray & 0x0000FF00) >> 8);
				int blue = (gray & 0x000000FF);

				gray = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
				data[nPixelCount] = (byte)gray;
				nPixelCount++;
			}
		}
	}
	
	static private String saveImage(byte[] data) { // 保存jpg到SD卡中
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sDateFormat.format(new java.util.Date());
		String tofile = Environment.getExternalStorageDirectory()
				+ File.separator + Environment.DIRECTORY_DCIM + File.separator
				+ date + ".jpg";
		
		try {
			File file = new File(tofile);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tofile;
	}
}
