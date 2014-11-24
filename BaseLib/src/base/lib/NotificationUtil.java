
package base.lib;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public enum NotificationUtil {
    instance;

    Context mContext = null;
    public int id;
    NotificationManager nManager;

    public void init(Context context, int nid) {
        id = nid;
        if (mContext != null)
            return;

        mContext = context;

        nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public Notification getNotification(boolean isService, Intent intent, int iconId,
            String appName, String hint) {
        // request_code will help to diff different thread
        PendingIntent pendingIntent;
        if (isService)
            pendingIntent = PendingIntent.getService(mContext, id, intent, 0);
        else
            pendingIntent = PendingIntent.getActivity(mContext, id, intent, 0);

        Notification notification = new Notification(iconId, appName, System.currentTimeMillis());
        if (!isService) {
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            notification.flags |= Notification.FLAG_NO_CLEAR;
        }

        notification.setLatestEventInfo(mContext, appName, hint, pendingIntent);

        return notification;
    }

    public void createNotification(boolean isService, Intent intent, int iconId, String appName,
            String hint) {
        nManager.notify(id, getNotification(isService, intent, iconId, appName, hint));
    }

    public void cancelNotification() {
        nManager.cancel(id);
    }
}
