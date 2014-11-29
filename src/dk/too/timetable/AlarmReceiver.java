package dk.too.timetable;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    static final int ALARM_ID = 1234;
    static int count = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Debug.D + "AlarmReceiver", "alarm !! " + new Date());

        // 토스트
        String message = intent.getStringExtra("message");
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();

            // 알림 메시지
            NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent npi = PendingIntent.getActivity(context, 0, new Intent(context, MyTimeTable.class),
                    PendingIntent.FLAG_ONE_SHOT);
            Notification notify = new Notification(R.drawable.red_ball, "수업 시간 알림", System.currentTimeMillis());
            notify.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS | Notification.DEFAULT_VIBRATE;
            notify.ledOffMS = 1000; // LED
            notify.ledOnMS = 1000;
            notify.vibrate = new long[] { 500, 1000, 500, 1000, 500, 1000 }; // 진동
            notify.sound = Uri.parse("file:/system/media/audio/ringtones/sample.ogg"); // 소리
            notify.number = ++count;

            notify.setLatestEventInfo(context, "수업 시간 알림", message, npi);

            mgr.notify(ALARM_ID, notify);
        }

        // 액티비티 실행
        // Intent startIntent = new Intent(context, MyTimeTable.class);
        // PendingIntent pi = PendingIntent.getActivity(context, 0, startIntent,
        // PendingIntent.FLAG_ONE_SHOT);
        // try {
        // pi.send();
        // } catch (CanceledException e) {
        // e.printStackTrace();
        // }

    }

}
