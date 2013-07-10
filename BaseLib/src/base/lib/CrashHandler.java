package base.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class CrashHandler implements UncaughtExceptionHandler {

	// system default UncaughtException handler
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	private static CrashHandler INSTANCE = new CrashHandler();
	private Context mContext;

	// notification id
	int ID = 22994904;
	
	// to ensure only have one CrashHandler
	private CrashHandler() {
	}

	// get instance of CrashHandler, singleton mode
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	public void init(Context context) {
		mContext = context;

		// get system default UncaughtException handler and set it as handler of this app
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * use this to handle UncaughtException
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// handle by system default exception handler if no user defined
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {}
			// exit
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
	}

	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();

		
		Intent intent = new Intent();
		intent.setAction(mContext.getPackageName() + ".crashControl");
		
		intent.putExtra("errorMsg", writer.toString());
		intent.putExtra("id", ID);
		// request_code will help to diff different thread
		PendingIntent contentIntent = PendingIntent.getActivity(mContext,
				ID, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		
		Notification notification = new Notification(
				android.R.drawable.stat_notify_error,
				"error",
				System.currentTimeMillis());

		notification.setLatestEventInfo(mContext, 
				mContext.getPackageName() + " " + mContext.getString(R.string.crashed),
				mContext.getString(R.string.click_for_detail), contentIntent);
		
		NotificationManager nManager = (NotificationManager) mContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		nManager.notify(ID, notification);

		return true;
	}

}
