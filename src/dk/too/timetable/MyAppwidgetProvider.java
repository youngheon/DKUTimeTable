package dk.too.timetable;

import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.RemoteViews;
import dk.too.timetable.DKClass.PartialTime;

/**
 * TODO 바탕화면 위젯을 눌러 실행 할 경우, 액티비티가 하나 더 생기는 현상 수정할 것.
 */
public class MyAppwidgetProvider extends AppWidgetProvider {

    static final String SHOW_INFO = "SHOW_INFO";
    static final String UPDATE_WIDGET = "dk.too.timetable.MyAppwidgetProvider.UPDATE_WIDGET";
    private static MyDB db;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName("dk.too.timetable", ".MyAppwidgetProvider"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        if (db == null) {
            db = new MyDB(context);
            db.open();
        }

        String amService = Context.ALARM_SERVICE;
        AlarmManager am = (AlarmManager) context.getSystemService(amService);

        Intent intent = new Intent(UPDATE_WIDGET);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        am.setRepeating(AlarmManager.RTC, 10000, 600000, pi);

    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName("dk.too.timetable", ".MyAppwidgetProvider"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        if (db != null) {
            db.close();
            db = null;
        }

        String amService = Context.ALARM_SERVICE;
        AlarmManager am = (AlarmManager) context.getSystemService(amService);
        Intent intent = new Intent(UPDATE_WIDGET);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.cancel(pi);
    }

    /* 위젯을 2개 이상 추가할 경우와 위젯 갱신 후 호출됨 */
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d(Debug.D + "MyAppwidgetProvider", "onUpdate");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(Debug.D + "MyAppwidgetProvider", "onReceive " + intent.getAction());

        // 10분마다 불린다.
        if (UPDATE_WIDGET.equals(intent.getAction())) {

            update(context);
        }
    }

    private void update(Context context) {

        Log.d(Debug.D + "MyAppwidgetProvider", "update ");
        if (db == null) {
            db = new MyDB(context);
            db.open();
        }

        String str = makeStr(context);

        // 클릭시 시간표 화면 띄운다.
        Intent intent = new Intent(context, MyTimeTable.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 위젯에서의 텍스트 설정
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.widget_textview, str);
        views.setOnClickPendingIntent(R.id.widget_textview, pendingIntent);

        ComponentName componentName = new ComponentName(context, MyAppwidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(componentName, views);
    }

    private String makeStr(Context context) {

        StringBuffer buf = new StringBuffer();

        // 오늘 현재 시간 이후의 수업만
        List<DKClass> list = db.DBselect();

        for (DKClass dkClass : list) {

            PartialTime[] partials = dkClass.getPartialTime();
            for (int i = 0; i < partials.length; i++) {
                
                if(partials[i].getHour() == -1) continue;

                if (partials[i].isShowWidget()) {
                    buf.append(partials[i].getTimeStr(context) + " " + partials[i].getLecture() + " ("
                            + partials[i].getRoom() + ")\n");
                }
            }
        }

        if (buf.length() == 0)
            buf.append("수업 정보가 없습니다.");

        return buf.toString();
    }

    static String cut(String in) {
        if (in.getBytes().length > 18) {
            return in.substring(0, 5) + "..";
        } else {
            return in;
        }
    }

}
