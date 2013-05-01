package simple.home.jtbuaa;

import java.util.Collections;
import java.util.HashMap;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class PkgAlphaList extends AlphaList<PackageInfo> {

	PkgAlphaList(Context context, PackageManager pmgr,
			HashMap<String, Object> packageSize, boolean isGrid, boolean largeScreen) {
		super(context, pmgr, packageSize, isGrid, largeScreen);
		tag = 3;
	}

	String getAlpha(PackageInfo info) {
		return info.sharedUserId;
	}
	
	String getPackageName(PackageInfo info) {
		return info.packageName;
	}
	
	String getProcessName(PackageInfo info) {
		return info.applicationInfo.processName;
	}
	
	String getLabel(PackageInfo info) {
		return info.applicationInfo.loadLabel(pm).toString();
	}
	
	String getSourceDir(PackageInfo info) {
		return info.applicationInfo.sourceDir;
	}
	
	Drawable getIcon(PackageInfo info) {
		return info.applicationInfo.loadIcon(pm);
	}
	
	int getFlag(PackageInfo info) {
		return info.applicationInfo.flags;
	}
	
	boolean start(PackageInfo info) {
		return false;
	}
	
	void sort() {
    	Collections.sort(mApps, new PackageComparator());//sort by name
	}
}
