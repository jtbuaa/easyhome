package base.lib;

import java.text.Collator;
import java.util.Comparator;

import android.content.pm.ResolveInfo;

public class StringComparator implements Comparator<String> {
	public StringComparator() {
	}

	public final int compare(String a, String b) {
		if (a == null) return -1;
		else if (b == null) return 1;
		else return sCollator.compare(a, b);
	}

	private final Collator sCollator = Collator.getInstance();
}