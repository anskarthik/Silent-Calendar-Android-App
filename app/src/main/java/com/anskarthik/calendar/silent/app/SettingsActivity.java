package com.anskarthik.calendar.silent.app;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Map;

/**
 * Created by user on 10-01-2016.
 */
/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */

public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Add 'general' preferences, defined in the XML file
        // TODO: Add preferences from XML
        addPreferencesFromResource(R.xml.pref_general);
        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        // TODO: Add preferences
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_email_key)));

        Preference button = findPreference(getString(R.string.test_pref_key));
        Preference infoButton = findPreference(getString(R.string.info_button_key));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do
                Toast.makeText(getApplicationContext(), "Test Starts in 10 sec", Toast.LENGTH_SHORT).show();
                schedule(10, true, 0);
                schedule(15, false, 1);
                cancelAlarm(0, false);
                cancelAlarm(1, false);
                schedule(20, false, 2);
                return true;
            }
        });

        infoButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do
                SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                //Toast.makeText(getApplicationContext(), "Selected Events : \n"+
                  //      app_preferences.getStringSet(CalendarClass.AccountName+CalendarClass.SP_EventTag,null)
                    //    , Toast.LENGTH_LONG).show();

                //Toast.makeText(getApplicationContext(), "Running Events : \n"+
                  //      app_preferences.getStringSet("current_event",null)
                    //    , Toast.LENGTH_LONG).show();
                String disp = "";
                Map<String, ?> allEntries = app_preferences .getAll();
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
                    disp = disp+entry.getKey() + ": " + entry.getValue().toString()+"\n";
                }
                Toast.makeText(getApplicationContext(), disp, Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }

    public void schedule(int timeLater, boolean state, int id)
    {
        Intent intent = new Intent(this, AlarmReceiver.class).putExtra("Vibrate",state).putExtra("eventId",(long)-1);
        if(state)   intent.setAction("com.anskarthik.calendar.silent.app.ACTION_TEST_RINGER_VIBRATE");
        else        intent.setAction("com.anskarthik.calendar.silent.app.ACTION_TEST_RINGER_NORMAL");
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeLater * 1000,
                PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void cancelAlarm(int id,boolean state)
    {
        Intent intent = new Intent(this, AlarmReceiver.class).putExtra("Vibrate",state);
        if(state)   intent.setAction("com.anskarthik.calendar.silent.app.ACTION_TEST_RINGER_VIBRATE");
        else        intent.setAction("com.anskarthik.calendar.silent.app.ACTION_TEST_RINGER_NORMAL");
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_CANCEL_CURRENT));
        PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_CANCEL_CURRENT).cancel();
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

}