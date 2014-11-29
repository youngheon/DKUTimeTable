package dk.too.timetable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.GridLayout.LayoutParams;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import dk.too.timetable.DKClass.PartialTime;
import dk.too.util.Util;

public class MyTimeTable extends Activity {

    public static final int LOGIN_DLG_FAIL = 8887;
    public static final int LOGIN_DLG = 8888;
    public static final int INFO_DLG = 8889;
    public static final int SETTINGS = 8890;

    static final String D = "DKUTimeTable ";

    static final int MAX = 6 * 16;

    private MyDB db;

    private ScrollView scroll;
    private GridLayout grid;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        scroll = (ScrollView) findViewById(R.id.scroll);
        grid = (GridLayout) findViewById(R.id.grid);

        db = new MyDB(this);
        db.open();
        if (loadData() != null) {
            showDialog(LOGIN_DLG);
        }

    }

    private int cellWidth;

    private int YELLOW1 = 0xFFFFEF7F;
    private int YELLOW2 = 0xFFFFEFAF;

    private void initGrid() {

        grid.removeAllViews();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        cellWidth = metrics.widthPixels / 6;

        makeAndAddView("/", cellWidth - 1, YELLOW1, 0, 0);
        makeAndAddView("월", cellWidth - 3, YELLOW1, 0, 1);
        makeAndAddView("화", cellWidth - 3, YELLOW1, 0, 2);
        makeAndAddView("수", cellWidth - 3, YELLOW1, 0, 3);
        makeAndAddView("목", cellWidth - 3, YELLOW1, 0, 4);
        makeAndAddView("금", cellWidth - 3, YELLOW1, 0, 5);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String defCampus = getString(R.string.default_campus_value);
        String settingCampus = prefs.getString("setting_campus", defCampus);

        final String[] times = (settingCampus.equals(defCampus) ? DKClass.jukjeonTime : DKClass.cheonanTime);

        for (int i = 0; i < 24; i++) {
            String s = (i < 9 ? " " : "") + (i + 1) + "  ";

            if (i < 18)
                makeAndAddView(s + times[i], cellWidth - 1, YELLOW1, i + 1, 0);
            else
                makeAndAddView(s + times[i], cellWidth - 1, YELLOW2, i + 1, 0);
        }
    }

    private void makeAndAddView(String txt, int cellWidth, int color, int row, int col) {
        TextView tv = new TextView(this);
        tv.setText(txt);
        tv.setTextColor(Color.BLACK);
        tv.setBackgroundColor(color);
        tv.setHeight(55);
        tv.setWidth(cellWidth);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        LayoutParams params = new LayoutParams(GridLayout.spec(row), GridLayout.spec(col));
        params.setGravity(Gravity.FILL);
        params.setMargins(1, 1, 1, 1);

        grid.addView(tv, params);
    }

    private void makeAndAddView(final String code, String txt, String room, int row, int col, int rowSpan) {

        // TextView tv = new TextView(this);
        // tv.setText(Html.fromHtml(txt + "<br><font color=blue>" + room +
        // "</font>"));
        // tv.setTextColor(Color.BLACK);
        // tv.setBackgroundColor(0xfff3f3f3);
        // tv.setHeight(100);
        // tv.setWidth(cellWidth - 1);
        // tv.setGravity(Gravity.CENTER);
        // tv.setEllipsize(TruncateAt.END);
        // tv.setOnClickListener(new View.OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        //
        // DKClass dkClass = db.getDkClass(code);
        //
        // createClassInfoDlg(dkClass).show();
        // }
        // });
        //
        // GridLayout.LayoutParams layparam = new LayoutParams();
        // layparam.columnSpec = GridLayout.spec(col);
        // layparam.rowSpec = GridLayout.spec(row, rowSpan);
        // layparam.setGravity(Gravity.FILL);
        // layparam.setMargins(1, 1, 1, 1);
        //
        // tv.setLayoutParams(layparam);
        // grid.addView(tv);

        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = (View) vi.inflate(R.layout.item, grid, false);
        v.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                DKClass dkClass = db.getDkClass(code);

                createClassInfoDlg(dkClass).show();
            }
        });

        GridLayout.LayoutParams layparam = (GridLayout.LayoutParams) v.getLayoutParams();
        layparam.columnSpec = GridLayout.spec(col);
        layparam.rowSpec = GridLayout.spec(row, rowSpan);
        layparam.setGravity(Gravity.FILL);
        layparam.setMargins(1, 1, 1, 1);
        layparam.height = 55;
        layparam.width = cellWidth - 3;

        v.setLayoutParams(layparam);

        TextView tv = (TextView) v.findViewById(R.id.label);
        tv.setText(txt);
        tv.setMaxLines(2 * rowSpan);

        TextView r = (TextView) v.findViewById(R.id.room);
        r.setText(room);

        grid.addView(v);
    }

    @Override
    protected void onDestroy() {

        db.close();

        super.onDestroy();
    }

    /**
     * @return 시간표 정보가 있는지 유무
     */
    private String loadData() {

        initGrid();

        List<DKClass> list = db.DBselect();

        for (DKClass dkClass : list) {

            String code = dkClass.getCode();
            String lecture = dkClass.getLecture();

            PartialTime[] partials = dkClass.getPartialTime();
            for (int i = 0; i < partials.length; i++) {

                int col = DKClass.getCol(partials[i].getDayOfWeekChar());
                int startHour = partials[i].getHour();
                int timeLen = partials[i].getTimeLength();
                String room = partials[i].getRoom();

                if(startHour == -1) continue;
                makeAndAddView(code, lecture, room, startHour, col, timeLen);
            }

            // 월9,10,11(자연516)/금5,6,7(자연517)
            // 월1,2/화4(자연517)
            // 월10,11,12(자연305)
        }

        return (!list.isEmpty()) ? null : "DB 내용 없음";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

        case R.id.login: {
            showDialog(LOGIN_DLG);
            return true;
        }
        case R.id.settings: {

            Intent intent = new Intent(this, SettingPreference.class);
            startActivityForResult(intent, SETTINGS);
            return true;
        }
        case R.id.send_image: {

            takeScreen();
            return true;
        }
        case R.id.program_info: {

            Intent intent = new Intent(this, ProgramInfo.class);
            startActivity(intent);
            return true;
        }
        case R.id.food_menu: {

            // 금주의 식단-학생식당 페이지
            Intent i = new Intent(Intent.ACTION_VIEW);
            Uri u = Uri.parse("http://203.237.226.95:8080/mobile/m11/m11_c1_2.jsp?instanceid=cvnc");
            i.setData(u);
            startActivity(i);

            return true;
        }
        case R.id.backup: {

            String filePath = db.backupDB();
            if (filePath == null) {
                Toast.makeText(this, "백업이 실패하였습니다.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, filePath + " 에 백업 되었습니다.", Toast.LENGTH_LONG).show();
            }

            return true;
        }
        case R.id.restore: {
            boolean isSuccess = db.restoreDB();
            if (isSuccess) {
                Toast.makeText(this, "복원이 성공하였습니다.", Toast.LENGTH_LONG).show();
                loadData();
            } else {
                Toast.makeText(this, "복원이 실패하였습니다.", Toast.LENGTH_LONG).show();
            }

            return true;
        }
        }

        return super.onOptionsItemSelected(item);
    }

    private void takeScreen() {

        View v1 = scroll;
        v1.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        v1.buildDrawingCache();
        Bitmap bm = v1.getDrawingCache();

        try {
            String folderName = Util.getExtPath() + "/tmp";
            File folder = new File(folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String path = folder + "/MyTimeTable_temp.png";

            FileOutputStream out = new FileOutputStream(path, false);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);

            if (bm != null) {
                File file = new File(path);

                Log.i(Debug.D + "MyTimeTable", "size :" + file.length());

                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Photo");
                sendIntent.putExtra(Intent.EXTRA_TEXT, "share timetable with your friends.");
                sendIntent.setType("image/jpeg");
                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

                startActivity(Intent.createChooser(sendIntent, "Share"));
            }
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(this, "이미지 보내기를 실패하였습니다.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS && resultCode == RESULT_OK) {

            boolean useAlarm = data.getBooleanExtra("useAlarm", false);
            int alarmTime = data.getIntExtra("alarmTime", 10);

            changeAlarm(useAlarm, alarmTime);

            loadData();
        }

    }

    private void changeAlarm(boolean useAlarm, int alarmTime) {

        // Log.d(Debug.D + "MyTimeTable", "onActivityResult useAlarm : " +
        // useAlarm + " alarmTime : " + alarmTime);
        // // 수업시간에서 Calendar 객체를 만들어낼 것.
        //
        // AlarmManager am = (AlarmManager)
        // getSystemService(Context.ALARM_SERVICE);
        //
        // List<DKClass> classes = db.DBselect();
        // int requestCode = 9999;
        // for (DKClass dkClass : classes) {
        //
        // for (PartialTime partialTime : dkClass.getFirstTimes()) {
        //
        // Intent intent = new Intent(this, AlarmReceiver.class);
        // intent.putExtra("message", "수업 시작 " + alarmTime + "분 전 입니다. \n" +
        // dkClass.getLecture() + ". "
        // + partialTime.getRoom());
        // // intent.putExtra("message", "수업 시작 " + alarmTime +
        // // "분 전 입니다. \n" + "소프트웨어공학" + " ("
        // // + "2공524" + ")");
        // PendingIntent sender = PendingIntent.getBroadcast(this,
        // requestCode++, intent,
        // PendingIntent.FLAG_UPDATE_CURRENT);
        //
        // long time = partialTime.nextCal().getTimeInMillis() - alarmTime *
        // 60000;
        // // Calendar cal = Calendar.getInstance();
        // // cal.set(Calendar.HOUR_OF_DAY, 10);
        // // cal.set(Calendar.MINUTE, 55);
        // // cal.set(Calendar.SECOND, 0);
        // // long time = cal.getTimeInMillis();
        //
        // Log.d(Debug.D + "MyTimeTable", intent.getStringExtra("message") +
        // " time : " + new Date(time));
        //
        // // 알람 사용 유무에 따라.
        // if (useAlarm) {
        // am.setRepeating(AlarmManager.RTC_WAKEUP, time, 604800000, sender); //
        // 일주일
        // // 단위로
        // // 반복
        // } else {
        // am.cancel(sender);
        // }
        //
        // }
        //
        // }

    }

    @Override
    protected Dialog onCreateDialog(int id) {

        switch (id) {

        case LOGIN_DLG: {
            Dialog dlg = createLoginDlg();
            return dlg;
        }
        case LOGIN_DLG_FAIL: {
            Dialog dlg = createLoginDlg();
            dlg.setTitle("로그인에 실패했습니다.\n다시 로그인 하세요.");
            return dlg;
        }

        }

        return super.onCreateDialog(id);
    }

    private Dialog createEmptyClassDlg() {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.lecture_new, (ViewGroup) findViewById(R.id.layout_root));

        final EditText lecture = (EditText) layout.findViewById(R.id.lecture);
        final EditText professor = (EditText) layout.findViewById(R.id.professor);
        final EditText timeRoom = (EditText) layout.findViewById(R.id.timeRoom);
        final EditText memo = (EditText) layout.findViewById(R.id.memo);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("강의 정보");

        builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setView(layout);

        return builder.create();

    }

    private Dialog createClassInfoDlg(final DKClass dkClass) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String year = prefs.getString("year", "2013");
        final String semester = prefs.getString("semester", "1");

        String defCampus = getString(R.string.default_campus_value);
        String settingCampus = prefs.getString("setting_campus", defCampus);

        final String campusCode = (settingCampus.equals(defCampus) ? "4000000001" : "3000000001");

        // 죽전
        // http://daninfo.dankook.ac.kr/hagsa/hlt/plan/printplan.aspx?year=2013&haggi=1&campus=4000000001&gwamogid=447940&class=2

        // 천안
        // http://daninfo.dankook.ac.kr/hagsa/hlt/plan/printplan.aspx?year=2013&haggi=1&campus=3000000001&gwamogid=346200&class=1

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.lecture_info, (ViewGroup) findViewById(R.id.layout_root));

        final TextView lecture = (TextView) layout.findViewById(R.id.lecture);
        lecture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String s = String
                        .format("http://daninfo.dankook.ac.kr/hagsa/hlt/plan/printplan.aspx?year=%s&haggi=%s&campus=%s&gwamogid=%s&class=%s",
                                year, semester, campusCode, dkClass.getCode(), dkClass.getDiv());

                Intent i = new Intent(getBaseContext(), WebViewActivity.class);
                Uri u = Uri.parse(s);
                i.setData(u);
                startActivity(i);
            }
        });

        final TextView professor = (TextView) layout.findViewById(R.id.professor);
        final TextView timeRoom = (TextView) layout.findViewById(R.id.timeRoom);
        final EditText memo = (EditText) layout.findViewById(R.id.memo);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("강의 정보");

        builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                db.updateMemo(memo.getText().toString(), dkClass.getCode());
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setView(layout);

        lecture.setText(dkClass.getLecture());
        professor.setText(dkClass.getProfessor());
        timeRoom.setText(dkClass.getTimeRoom());
        memo.setText(dkClass.getMemo());

        return builder.create();
    }

    private Dialog createLoginDlg() {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.login, (ViewGroup) findViewById(R.id.layout_root));

        final AlertDialog.Builder aDialog = new AlertDialog.Builder(this);
        aDialog.setTitle("로그인하시겠습니까?");
        aDialog.setView(layout);

        final EditText schoolNumber = (EditText) layout.findViewById(R.id.schoolNumber);
        final EditText password = (EditText) layout.findViewById(R.id.password);

        aDialog.setPositiveButton("시간표 다시 가져오기", new DialogInterface.OnClickListener() {

            public void onClick(final DialogInterface dialog, int which) {
                new LoginTask().execute(schoolNumber.getText().toString(), password.getText().toString());

            }
        });
        aDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return aDialog.create();
    }

    private void saveError(Exception e) {

        String folderName = Util.getExtPath() + "/tmp";
        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = folderName + "/dku_time_table.txt";
        File file = new File(fileName);

        try {
            PrintWriter w = new PrintWriter(file);
            Throwable t = e.getCause();
            if (t != null) {
                e.getCause().printStackTrace(w);
            } else if (e.getMessage() != null) {
                w.write(e.getMessage());
            }
            w.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        private final ProgressDialog dialog = new ProgressDialog(MyTimeTable.this);

        @Override
        protected void onPreExecute() {

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            boolean isConnect = false;
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                isConnect = true;
            }

            if (isConnect) {

                this.dialog.setCancelable(false);
                this.dialog.setTitle("잠시만 기다려주세요.");
                this.dialog.setMessage("시간표를 가져오는 중입니다.");
                this.dialog.show();

            } else {
                cancel(true);
            }

        }

        @Override
        protected String doInBackground(String... params) {

            String schoolNumber = params[0];
            String password = params[1];

            try {
                List<DKClass> classes = Parse.getClasses(schoolNumber, password, getBaseContext());

                db.DBclear();
                db.DBinsert(classes);

            } catch (Exception e) {
                saveError(e);

                return e.getMessage();
            }

            return null; // null이 정상
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            AlertDialog alertDialog = new AlertDialog.Builder(MyTimeTable.this).create();

            alertDialog.setTitle("인터넷 연결 실패");
            alertDialog.setMessage("Wifi 혹은 3G망이 연결되지 않았거나 원활하지 않습니다.네트워크 확인후 다시 접속해 주세요!");
            alertDialog.setButton("확인", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            alertDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            // 강의 목록을 가져오지 못하는 경우.
            // * 비밀번호 틀리거나
            // * 웹정보시스템 접속 안되거나
            // * html이 변경되었거나
            if (result == null) {
                Toast.makeText(MyTimeTable.this, "강의 시간 목록 가져오기 성공", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MyTimeTable.this, result, Toast.LENGTH_LONG).show();
                showDialog(LOGIN_DLG_FAIL);
            }

            loadData();

        }
    }

}