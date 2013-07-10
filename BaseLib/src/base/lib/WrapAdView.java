package base.lib;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class WrapAdView {
	AdView mInstance;
	AdRequest adRequest;
	Handler mHandler;

	class DestroyTask extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {
			if (mInstance != null) {
				// seems easy to cause android.util.AndroidRuntimeException: 
				// Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag
				// which can't catch. add sleep seems ok. and put in AsyncTask will not block UI thread
				try {
					Thread.sleep(10000);
					mInstance.stopLoading();
					mInstance.destroy();
				} catch (Exception e) {}
			}
			return null;
		}
	}

	public WrapAdView(Activity activity, int size, String publisherID,
			Handler handler) {
		mHandler = handler;

		try {
			switch (size) {
			case 0:
				mInstance = new AdView(activity, AdSize.BANNER, publisherID);
				break;
			case 1:
				mInstance = new AdView(activity, AdSize.IAB_BANNER, publisherID);
				break;
			case 2:
				mInstance = new AdView(activity, AdSize.IAB_LEADERBOARD, publisherID);
				break;
			case 3:
				mInstance = new AdView(activity, AdSize.IAB_MRECT, publisherID);
				break;
			}
			mInstance.setAdListener(new Listener());

			adRequest = new AdRequest();
		} catch (Exception e) {
		}
	}

	boolean isReady() {
		if (mInstance != null)
			return mInstance.isReady();
		else return false;
	}
	
	public void loadAd() {
		if ((mInstance != null) && (adRequest != null))
			mInstance.loadAd(adRequest);
	}

	public void destroy() {//do nothing on destroy which may cause FLAG_ACTIVITY_NEW_TASK error?
		//DestroyTask dtask = new DestroyTask();
		//dtask.execute();
	}

	public View getInstance() {
		return mInstance;
	}
	
	class Listener implements AdListener {
		@Override
		public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
			if (mHandler != null) {
				Message fail = mHandler.obtainMessage();
				fail.what = -1;
				mHandler.sendMessage(fail);
			}
		}

		@Override
		public void onReceiveAd(Ad arg0) {
			if (mHandler != null) {
				Message dismiss = mHandler.obtainMessage();
				dismiss.what = 0;
				mHandler.sendMessage(dismiss);
			}
		}

		@Override
		public void onDismissScreen(Ad arg0) {// Called when an ad is clicked
												// and about to return to the
												// application.
			//Log.d("=============Ads ondismiss", "");
		}

		@Override
		public void onLeaveApplication(Ad arg0) {
			//Log.d("=============Ads onleave", "");
			if (mHandler != null) {
				Message dismiss = mHandler.obtainMessage();
				dismiss.what = 1;
				mHandler.sendMessage(dismiss);
			}
		}

		@Override
		public void onPresentScreen(Ad arg0) {// Called when an Activity is
												// created in front of the app
			//Log.d("=============Ads onpresent", "");
		}
	}
}