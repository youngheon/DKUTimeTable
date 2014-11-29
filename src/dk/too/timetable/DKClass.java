package dk.too.timetable;

import java.util.Calendar;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DKClass {

    public static final String[] jukjeonTime = { "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00",
            "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30",
            // 야간
            "18:00", "18:50", "19:40", "20:30", "21:20", "22:10", };

    public static final String[] cheonanTime = { "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00",
            "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30",
            // 야간
            "17:40", "18:30", "19:20", "20:10", "21:00", "21:50", };

    private String code;
    private String lecture;
    private String div; // 분반
    private String timeRoom;
    private String professor;
    private String memo;

    public DKClass(String code, String div, String lecture, String timeRoom, String professor, String memo) {
        super();
        this.code = code;
        this.lecture = lecture;
        this.div = div;
        this.timeRoom = timeRoom;
        this.professor = professor;
        this.memo = memo;
    }

    public DKClass(String code, String lecture, String timeRoom, String professor, String memo) {
        super();
        this.code = code;
        this.lecture = lecture;
        this.timeRoom = timeRoom;
        this.professor = professor;
        this.memo = memo;
    }

    public DKClass(String lecture, String professor) {
        super();
        this.code = lecture;
        this.lecture = lecture;
        this.professor = professor;
        this.timeRoom = "";
    }

    public void addTime(String timeRoom) {
        this.timeRoom += timeRoom + "!";
    }

    public String getCode() {
        return code;
    }

    public String getLecture() {
        return lecture;
    }

    public String getProfessor() {
        return professor;
    }

    public String getTimeRoom() {

        return timeRoom;
    }

    public String getDiv() {
        return div;
    }

    static class DayOfWeekComp implements Comparator<String> {

        @Override
        public int compare(String object1, String object2) {

            if (object1.length() == 0) {
                return -1;
            } else if (object2.length() == 0) {
                return 1;
            } else if (object1.charAt(0) == object2.charAt(0)) {
                return object1.compareTo(object2);
            }

            return (getDayOfWeek(object1.charAt(0)) > getDayOfWeek(object2.charAt(0))) ? 1 : -1;
        }

    }

    public String getMemo() {
        return memo;
    }

    public static char toDayOfWeekStr(int col) {

        char dayOfWeek;
        switch (col) {
        case 1:
            dayOfWeek = '월';
            break;
        case 2:
            dayOfWeek = '화';
            break;
        case 3:
            dayOfWeek = '수';
            break;
        case 4:
            dayOfWeek = '목';
            break;
        case 5:
            dayOfWeek = '금';
            break;
        case 6:
            dayOfWeek = '토';
            break;
        default:
            dayOfWeek = ' ';
        }
        return dayOfWeek;
    }

    /**
     * 월2,화3,수4,목5,금6,토7
     * 
     * @param dayOfWeek
     * @return
     */
    public static int getDayOfWeek(char dayOfWeek) {
        int _dayOfWeek;
        switch (dayOfWeek) {
        case '일':
            _dayOfWeek = 1;
            break;
        case '월':
            _dayOfWeek = 2;
            break;
        case '화':
            _dayOfWeek = 3;
            break;
        case '수':
            _dayOfWeek = 4;
            break;
        case '목':
            _dayOfWeek = 5;
            break;
        case '금':
            _dayOfWeek = 6;
            break;
        case '토':
            _dayOfWeek = 7;
            break;
        default:
            _dayOfWeek = -1;
        }
        return _dayOfWeek;
    }

    @Override
    public String toString() {
        return code + " " + div + " " + lecture + " " + timeRoom + " " + professor + " " + memo;
    }

    /**
     * 월1,화2,수3,목4,금5,토6
     * 
     * @param dayOfWeek
     * @return
     */
    public static int getCol(char dayOfWeek) {
        int _dayOfWeek;
        switch (dayOfWeek) {
        case '월':
            _dayOfWeek = 1;
            break;
        case '화':
            _dayOfWeek = 2;
            break;
        case '수':
            _dayOfWeek = 3;
            break;
        case '목':
            _dayOfWeek = 4;
            break;
        case '금':
            _dayOfWeek = 5;
            break;
        case '토':
            _dayOfWeek = 6;
            break;
        default:
            _dayOfWeek = -1;
        }
        return _dayOfWeek;
    }

    public PartialTime[] getPartialTime() {

        String lecture = getLecture();
        String timeRoom = getTimeRoom();

        Matcher lastRoomMt = Pattern.compile(".*\\((.*)\\)").matcher(timeRoom);

        String tempRoom = lastRoomMt.find() ? lastRoomMt.group(1) : "(-)";

        String[] strs = timeRoom.split("/");
        PartialTime[] partials = new PartialTime[strs.length];
        for (int i = 0; i < strs.length; i++) {

            String s = strs[i];
            if(s.indexOf('(') <0) s+= '('+tempRoom+')';

            Matcher mt = Pattern.compile("(\\D)(.+)\\((.*)\\)").matcher(s);

            char dayOfWeek = '-';
            String[] chs = new String[0];
            int classHour = -1;
            String room = tempRoom;

            if (mt.find()) {

                dayOfWeek = mt.group(1).charAt(0);
                chs = mt.group(2).split(",");
                classHour = Integer.parseInt(chs[0]);
                room = mt.group(3);
            }

            partials[i] = new PartialTime(lecture, dayOfWeek, classHour, chs.length, room);

        }

        return partials;
    }

    static class PartialTime {

        private char dayOfWeek; // 요일 0:일요일 1:월요일..
        private int startHour; // 교시
        private String room; // 강의실
        private int timeLength;

        private String lecture;

        public PartialTime(String lecture, char dayOfWeek, int classHour, int timeLength, String room) {

            this.lecture = lecture;
            this.dayOfWeek = dayOfWeek;
            this.startHour = classHour;
            this.timeLength = timeLength;
            this.room = room;
        }

        public String getLecture() {
            return lecture;
        }

        public char getDayOfWeekChar() {
            return dayOfWeek;
        }

        public int getHour() {
            return startHour;
        }

        public String getTimeStr(Context context) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            String defCampus = context.getString(R.string.default_campus_value);
            String settingCampus = prefs.getString("setting_campus", defCampus);

            final String[] times = (settingCampus.equals(defCampus) ? DKClass.jukjeonTime : DKClass.cheonanTime);

            return times[startHour-1];
        }

        public int getTimeLength() {
            return timeLength;
        }

        public boolean isShowWidget() {
            // 오늘
            Calendar toDay = Calendar.getInstance();
            if (toDay.get(Calendar.DAY_OF_WEEK) == getDayOfWeek(dayOfWeek)) {
                // 월2
                return true;
            }
            return false;
        }

        public String getRoom() {
            return room;
        }

    }

}