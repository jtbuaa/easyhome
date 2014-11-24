
package simple.home.jtbuaa;

import java.text.Collator;
import java.util.Comparator;

import android.content.pm.ResolveInfo;

public class myComparator implements Comparator<ResolveInfo> {
    public myComparator() {
    }

    public final int compare(ResolveInfo a, ResolveInfo b) {
        return sCollator.compare(a.activityInfo.applicationInfo.dataDir,
                b.activityInfo.applicationInfo.dataDir);
    }

    private final Collator sCollator = Collator.getInstance();
}
