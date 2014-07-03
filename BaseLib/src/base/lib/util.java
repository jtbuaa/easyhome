package base.lib;

import java.io.File;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

public class util {

	static public void share(String packageName, Context context, String content) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
		if (content == null)
			intent.putExtra(Intent.EXTRA_TEXT,
					context.getString(R.string.share_text)
							+ " https://play.google.com/store/apps/details?id="
							+ packageName);
		else
			intent.putExtra(Intent.EXTRA_TEXT, content);
		startActivity(
				Intent.createChooser(intent,
						context.getString(R.string.share_with)), true, context);
	}

	static public void rate(String packageName, Context context) {
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("market://details?id=" + packageName));
		if (!startActivity(intent, false, context)) {
			intent.setData(Uri
					.parse("https://play.google.com/store/apps/details?id="
							+ packageName));
			startActivity(intent, true, context);
		}
	}

	static public void feedBack(Context context) {
		Intent intent = new Intent(Intent.ACTION_SENDTO);
		intent.setData(Uri.fromParts("mailto",
				context.getString(R.string.browser_author), null));
		startActivity(intent, true, context);
	}

	static public boolean startActivity(Intent intent, boolean showToast,
			Context context) {
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			if (showToast)
				try {
					AlertDialog dlg = new AlertDialog.Builder(context)
							.setMessage(e.toString())
							.setPositiveButton(R.string.ok,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
										}
									}).create();
					dlg.show();
				} catch (Exception ee) {
					Toast.makeText(context, e.toString(), Toast.LENGTH_LONG)
							.show();
				}
			return false;
		}
	}

	static public boolean startApp(ResolveInfo info, Context context) {
		if (info == null) {
			try {
				AlertDialog dlg = new AlertDialog.Builder(context)
						.setMessage("null pointer error of resolveInfo")
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).create();
				dlg.show();
			} catch (Exception ee) {
				Toast.makeText(context, "null pointer error of resolveInfo",
						Toast.LENGTH_LONG).show();
			}
			return false;
		}

		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setComponent(new ComponentName(
				info.activityInfo.applicationInfo.packageName,
				info.activityInfo.name));
		return startActivity(i, true, context);
	}

	static public String getVersion(Context context) {
		String version = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null)
				version = pi.versionName == null ? String
						.valueOf(pi.versionCode) : pi.versionName;
		} catch (Exception e) {
		}
		return version;
	}

	static public String getVersionCode(Context context) {
		String version = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null)
				version = String.valueOf(pi.versionCode);
		} catch (Exception e) {
		}
		return version;
	}

	static public String preparePath(Context context) {
		String defaultPath = "/data/data/" + context.getPackageName();
		try {
			defaultPath = context.getFilesDir().getPath();
		} catch (Exception e) {
		}

		String downloadPath = defaultPath + "/";

		boolean hasSDcard = false;
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			downloadPath = Environment.getExternalStorageDirectory()
					+ "/simpleHome/";
			hasSDcard = true;
		}

		java.io.File myFilePath = new java.io.File(downloadPath);
		try {
			if (myFilePath.isDirectory())
				;// folder exist
			else
				myFilePath.mkdir();// create folder

			File path = new File(downloadPath + "snap/");
			if (path.isDirectory())
				;// folder exist
			else
				path.mkdir();// create folder

			path = new File(downloadPath + "source/");
			if (path.isDirectory())
				;// folder exist
			else
				path.mkdir();// create folder

			path = new File(downloadPath + "cookie/");
			if (path.isDirectory())
				;// folder exist
			else
				path.mkdir();// create folder

			path = new File(downloadPath + "apk/");
			if (path.isDirectory())
				;// folder exist
			else
				path.mkdir();// create folder

			path = new File(downloadPath + "cache/");
			if (path.isDirectory())
				;// folder exist
			else
				path.mkdir();// create folder

			path = new File(downloadPath + "bookmark/");
			if (hasSDcard) {// if create this folder without sdcard, it will
							// lose bookmarks
				if (path.isDirectory())
					;// folder exist
				else
					path.mkdir();// create folder
			} else if (path.isDirectory())
				path.delete();
		} catch (Exception e) {
			downloadPath = defaultPath + "/";
		}

		return downloadPath;
	}

	/**
	 * get bitmap from resource id
	 * 
	 * @param res
	 * @param resId
	 * @return
	 */
	static public Bitmap getResIcon(Resources res, int resId) {
		Drawable icon = res.getDrawable(resId);
		if (icon instanceof BitmapDrawable) {
			BitmapDrawable bd = (BitmapDrawable) icon;
			return bd.getBitmap();
		} else
			return null;
	}

	/**
	 * put number on gaven bitmap with blue color
	 * 
	 * @param icon
	 *            gaven bitmap
	 * @return bitmap with count
	 */
	static public Bitmap generatorCountIcon(Bitmap icon, int count, int scheme,
			float density, Context context) {
		// init canvas
		int iconSize = (int) context.getResources().getDimension(
				android.R.dimen.app_icon_size);
		Bitmap contactIcon = Bitmap.createBitmap(iconSize, iconSize,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(contactIcon);

		// copy image
		Paint iconPaint = new Paint();
		iconPaint.setDither(true);
		iconPaint.setFilterBitmap(true);
		Rect src = new Rect(0, 0, icon.getWidth(), icon.getHeight());
		Rect dst = new Rect(0, 0, iconSize, iconSize);
		canvas.drawBitmap(icon, src, dst, iconPaint);

		Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG
				| Paint.DEV_KERN_TEXT_FLAG);
		if (scheme == 0) {// for newpage icon
			countPaint.setColor(Color.BLACK);
			countPaint.setTextSize(25f);
			canvas.drawText(String.valueOf(count), iconSize / 2 - 3,
					iconSize / 2 + 13, countPaint);
		} else if (scheme == 1) {// for miss call and unread sms
			countPaint.setColor(Color.DKGRAY);
			countPaint.setTextSize(25f);
			countPaint.setTypeface(Typeface.DEFAULT_BOLD);
			canvas.drawText(String.valueOf(count), iconSize - 30, 20,
					countPaint);
		} else {// for easy browser. i don't know why the font change if invoke
				// from easy browser. if from eash home, it is ok for 25f.
			countPaint.setColor(Color.DKGRAY);
			countPaint.setTextSize(20f * density);
			canvas.drawText(count > 9 ? "..." : String.valueOf(count), iconSize
					/ 2 - density, iconSize / 2 + 13 * density, countPaint);
		}
		return contactIcon;
	}

}
