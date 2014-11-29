package dk.too.timetable;
//hanguel an ssl gae yo! -hj
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingPreference extends PreferenceActivity implements OnPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        
        ListPreference settingCampusPreference = (ListPreference) findPreference("setting_campus");
        settingCampusPreference.setOnPreferenceChangeListener(this);
        
        settingCampusPreference.setSummary(settingCampusPreference.getEntry());
    }

    @Override
    public void finish() {
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean useAlarm = prefs.getBoolean("useAlarm", false);
        int alarmTime = Integer.parseInt(prefs.getString("alarmTime", "10"));

        Log.d(MyTimeTable.D + "AlarmPreference", "finish useAlarm : " + useAlarm + " alarmTime : " + alarmTime);
        
        Intent data = new Intent();
        data.putExtra("useAlarm", useAlarm);
        data.putExtra("alarmTime", alarmTime);
        setResult(RESULT_OK, data);
        
        super.finish();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        
        if("setting_campus".equals(preference.getKey())){
            
            ListPreference p = (ListPreference) preference;
            for (int i = p.getEntryValues().length - 1; i >= 0; i--) {

                if (p.getEntryValues()[i] == newValue) {

                    p.setSummary(p.getEntries()[i]);
                }
            }
        }
        
        
        return true;
    }

}