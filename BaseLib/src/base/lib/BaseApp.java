package base.lib;

import java.io.File;
import java.util.HashMap;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class BaseApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
	}

	@Override
	public File getCacheDir() {// NOTE: this method is used in Android 2.2 and
								// higher
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean cacheToSd = sp.getBoolean("cache_tosd", false);

		if (cacheToSd)
			return new File(util.preparePath(getBaseContext()) + "cache/");
		else
			return super.getCacheDir();
	}
}
