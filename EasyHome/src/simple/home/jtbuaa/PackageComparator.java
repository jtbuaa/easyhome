package simple.home.jtbuaa;

import java.text.Collator;
import java.util.Comparator;

import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;

public class PackageComparator implements Comparator<PackageInfo> {
	public PackageComparator() {
	}

	public final int compare(PackageInfo a, PackageInfo b) {
	    return sCollator.compare(a.sharedUserId, b.sharedUserId);
	}

	private final Collator sCollator = Collator.getInstance();
}