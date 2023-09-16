package io.navendra.retrofitkotlindeferred.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;


/**
 * Persistent foreground service (raising notifications to the status bar)
 * to get extra CPU wakelock time. This will make sure that the call audio keeps transmitting to other side when phone is put on sleep mode.
 * If this is not implemented then after phone is put on sleep mode, the call remains active however audio transmission to other side stops after a few seconds until you wake up the screen again.
 */
public class ForegroundService extends Service {

    private static final int NOTIFICATION_ID = 123;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private final String NOTIFICATION_CHANNEL_ID = LOG_TAG;
    Notification notification = null;

    @Override
    public void onCreate() {

        Log.d(LOG_TAG, "onCreate()");
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Call Status",
                    NotificationManager.IMPORTANCE_LOW));
        }
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d(LOG_TAG, "onStartCommand()");
//        if (notification == null) {
//            notification = getNotification();
//        }
//        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind()");
        return null;
    }

//    private Notification getNotification() {
//        Context context = this;
//        Intent notificationIntent = new Intent(context, CallNotificationActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
//
//        Notification notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
//                .setOngoing(true)
//                .setPriority(NotificationCompat.PRIORITY_LOW)
//                .setSmallIcon(R.mipmap.buzz_ic_launcher_round)
//                .setContentTitle("Halo BCA Mobile")
//                .setContentText("test")
//                .setContentIntent(pendingIntent).build();
//        return notification;
//    }

}
