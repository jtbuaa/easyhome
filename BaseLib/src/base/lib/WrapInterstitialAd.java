package base.lib;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.InterstitialAd;
import com.google.ads.AdRequest.ErrorCode;

public class WrapInterstitialAd {
	InterstitialAd mInstance;
	AdRequest adRequest;
	Handler mHandler;

	public WrapInterstitialAd(Activity activity, String publisherID, Handler handler) {
		mInstance = new InterstitialAd(activity, publisherID);
		mInstance.setAdListener(new Listener());
		adRequest = new AdRequest();
		mHandler = handler;
	}

	public boolean isReady() {
		if (mInstance != null)
			return mInstance.isReady();
		else return false;
	}
	
	public void show() {
		if (mInstance != null) mInstance.show();
	}
	
	public void loadAd() {
		if ((mInstance != null) && (adRequest != null))
			mInstance.loadAd(adRequest);
	}
	
	class Listener implements AdListener {
		@Override
		public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
			if (mHandler != null) {
				Message fail = mHandler.obtainMessage();
				fail.what = -2;
				mHandler.sendMessage(fail);
			}
		}

		@Override
		public void onReceiveAd(Ad arg0) {
			if (mHandler != null) {
				Message dismiss = mHandler.obtainMessage();
				dismiss.what = -4;
				mHandler.sendMessage(dismiss);
			}
		}

		@Override
		public void onDismissScreen(Ad arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onLeaveApplication(Ad arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPresentScreen(Ad arg0) {
			// TODO Auto-generated method stub
			
		}
	}
}