package exocr.activity;

import android.app.Activity;

public class OcrActivity extends Activity {

    @Override
    public void onBackPressed() {
        if(null != listener){
            listener.onFinish();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if(null != listener){
            listener.onFinish();
        }
        super.onDestroy();
    }

    @Override
    public void finish() {
        if(null != listener){
            listener.onFinish();
        }
        super.finish();
    }

    /**********************************************************************************************/

    private OnActivityLifecycleListener listener;

    public interface OnActivityLifecycleListener {

        void onFinish();
    }

    public void setOnActivityLifecycleListener(OnActivityLifecycleListener listener) {
        this.listener = listener;
    }
}
