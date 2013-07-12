package base.lib;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CrashControl extends Activity {
	Button btnRetry, btnCancel;
	TextView tv;
	int id;
	boolean retry = false;
	
	NotificationManager nManager;

	// for information collection
	private Map<String, String> infos = new HashMap<String, String>();

	// format the date as part of log
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		init(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.crash);

		init(getIntent());
	}

	private void init(Intent intent) {
		tv = (TextView) findViewById(R.id.download_name);
		btnRetry = (Button) findViewById(R.id.pause);
		btnCancel = (Button) findViewById(R.id.stop);
		
		id = intent.getIntExtra("id", 0);
		nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		final String errorMsg = intent.getStringExtra("errorMsg");
		
		tv.setText(getPackageName() + " " + getString(R.string.crashed) + "\n\n" + errorMsg);

		btnCancel.setText(getString(R.string.cancel));
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				nManager.cancel(id);// remove notification
				finish();
			}
		});

		if (errorMsg == null) retry = true;
		else retry = errorMsg.contains("WebViewDatabase");// should clear the database and retry if SQLite* exception
		SharedPreferences perferences = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = perferences.edit();
		boolean retried = perferences.getBoolean("retried", false);
		if (retried) retry = false;// don't retry 2 times
		
		if (retry) {// clear database and retry
			btnRetry.setText(getString(R.string.retry));
			btnRetry.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {// start new task to download
					deleteDatabase("webview.db");
					editor.putBoolean("retried", true);
					editor.commit();
					
					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri data = Uri.parse("https://play.google.com/store/apps/details?id=easy.browser.classic");
					intent.setData(data);
					util.startActivity(intent, false, CrashControl.this);
					nManager.cancel(id);// remove notification
					finish();
				}
			});

		} else {// send error log to author
			btnRetry.setText(getString(R.string.sendto));
			btnRetry.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					editor.putBoolean("retried", false);
					editor.commit();
					
					// collect device info
					collectDeviceInfo(CrashControl.this);

					StringBuffer sb = new StringBuffer();
					for (Map.Entry<String, String> entry : infos.entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue();
						sb.append(key + "=" + value + "\n");
					}
					sb.append(errorMsg);


					Intent intent = new Intent(Intent.ACTION_SENDTO);
					intent.setData(Uri.fromParts("mailto",
							getString(R.string.browser_author), null));
					intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback) + "\n\n\n\n\n====================\n" + sb.toString());
					intent.putExtra(Intent.EXTRA_SUBJECT, getPackageName()
							+ getString(R.string.sorry));
					if (!util.startActivity(intent, false, CrashControl.this)) {// send mail by webpage if fail to send through mail client
						Uri data = Uri.parse("https://mail.google.com/mail/?ui=2&view=cm&fs=1&tf=1&su=" + getString(R.string.sorry) + "&to=" + getString(R.string.browser_author) + "&body=" + getString(R.string.feedback) + "\n\n\n\n\n====================\n" + sb.toString());
						intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(data);
						util.startActivity(intent, true, CrashControl.this);
					}
					
					nManager.cancel(id);// remove notification
					finish();
				}
			});
		}
	}
	/**
	 * collect device info
	 * 
	 * @param ctx
	 */
	public void collectDeviceInfo(Context ctx) {
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				Log.d(field.getName(), field.get(null).toString());
			} catch (Exception e) {}
		}

		infos.put("versionName", util.getVersion(ctx));
		infos.put("versionCode", util.getVersionCode(ctx));
	}

}
