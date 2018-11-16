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

import exocr.exocrengine.*;
import exocr.idcard.ViewUtil;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Vector;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {

  private static final String TAG = CaptureActivityHandler.class.getSimpleName();

  private final CaptureActivity activity;
  private final DecodeThread decodeThread;
  private State state;
  
  private int auto_focus_id;
  private int restart_preview_id;
  private int decode_id;
  private int decode_succeeded_id;
  private int decode_failed_id;
  private int return_scan_result_id;
  private int launch_product_query_id;
  private int quit_id;
  
  private enum State {
    PREVIEW,
    SUCCESS,
    DONE
  }

  public CaptureActivityHandler(CaptureActivity activity) {
    this.activity = activity;
    decodeThread = new DecodeThread(activity);
    decodeThread.start();
    state = State.SUCCESS;
    
    auto_focus_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "auto_focus");
    restart_preview_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "restart_preview");
    decode_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "decode");
    decode_succeeded_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "decode_succeeded");
    decode_failed_id = ViewUtil.getResourseIdByName(activity.getApplicationContext().getPackageName(), "id", "decode_failed");
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
		// if (message.what == R.id.auto_focus) {
			// Log.d(TAG, "Got auto-focus message");
			// When one auto focus pass finishes, start another. This is the
			// closest thing to
			// continuous AF. It does seem to hunt a bit, but I'm not sure what
			// else to do.
			if (state == State.PREVIEW) {
				CameraManager.get().requestAutoFocus(this, auto_focus_id);
			}
		} else if (message.what == restart_preview_id) {
//		} else if (message.what == R.id.restart_preview) {
			Log.d(TAG, "Got restart preview message");
			restartPreviewAndDecode();
		} else if (message.what == decode_succeeded_id) {
//		} else if (message.what == R.id.decode_succeeded) {
			Log.d(TAG, "Got decode succeeded message");
			state = State.SUCCESS;
			// pass the result to the view to show
			activity.handleDecode((EXIDCardResult) message.obj);
		} else if (message.what == decode_failed_id) {
//		} else if (message.what == R.id.decode_failed) {
			// We're decoding as fast as possible, so when one decode fails,
			// start another.
			// activity.handleDecode(null);
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), decode_id);
		} else if (message.what == return_scan_result_id) {
//		} else if (message.what == R.id.return_scan_result) {
			Log.d(TAG, "Got return scan result message");
			activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
			activity.finish();
		} else if (message.what == launch_product_query_id) {
//		} else if (message.what == R.id.launch_product_query) {
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

    // Be absolutely sure we don't send any queued up messages
    removeMessages(decode_succeeded_id);
    removeMessages(decode_failed_id);
  }

  private void restartPreviewAndDecode() {
    if (state == State.SUCCESS) {
      state = State.PREVIEW;
      CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), decode_id);
      CameraManager.get().requestAutoFocus(this, auto_focus_id);
    }
  }
  
  public void restartAutoFocus(){
      state = State.PREVIEW;
      CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), decode_id);
      CameraManager.get().requestAutoFocus(this, auto_focus_id);
  }
  public void takePicture(){
	  CameraManager.get().takePicture(activity);
  }
}
