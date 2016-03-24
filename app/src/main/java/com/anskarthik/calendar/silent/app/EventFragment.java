package com.anskarthik.calendar.silent.app;

/**
 * Created by user on 08-01-2016.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * A forecast fragment containing a simple view.
 */
public class EventFragment extends Fragment {

    CalendarClass.EventClassAdapter mEventAdapter;
    CalendarClass myCalendar = new CalendarClass("");
    //public static final String SHARED_PREF_FILE_NAME = "MyEventFile";

    public EventFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Intent intent = new Intent(getActivity(), AlarmReceiver.class).putExtra("Vibrate", false);
        intent.setAction("com.anskarthik.calendar.silent.app.ACTION_DAILY_UPDATE");

        boolean alarmUp = (PendingIntent.getBroadcast(getActivity(), 0, intent,
                PendingIntent.FLAG_NO_CREATE) != null);
        if (!alarmUp){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 22);

            // With setInexactRepeating(), you have to use one of the AlarmManager interval
            // constants--in this case, AlarmManager.INTERVAL_DAY.
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(getActivity(), 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT));
        }
        Log.v("DAILY UPDATE"," "+alarmUp);
        //FetchEventsTask weatherTask = new FetchEventsTask();
        //weatherTask.execute("");
    }

    @Override
    public void onStart() {
        super.onStart();
        updateEvents();
    }

    @Override
    public void onStop() {
        super.onStop();  // Always call the superclass method first
        //myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater){
        inflater.inflate(R.menu.eventfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.action_refresh){
            updateEvents();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String[] forecastArray = {};

        //List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        //mEventAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_event, R.id.list_item_event_textview, weekForecast);

        mEventAdapter = new CalendarClass.EventClassAdapter(getActivity(), new ArrayList<CalendarClass.EventClass>());

        ListView listView = (ListView) rootView.findViewById(R.id.listView_event);
        listView.setAdapter(mEventAdapter);

        return rootView;
    }

    public void updateEvents() {
        //SharedPreferences pref = getActivity().getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String emailId = pref.getString(getString(R.string.pref_email_key), "");

        FetchEventsTask weatherTask = new FetchEventsTask();

        Toast.makeText(getActivity(), emailId, Toast.LENGTH_SHORT).show();
        weatherTask.execute(emailId);
    }

    public class FetchEventsTask extends AsyncTask<String, Void, CalendarClass.EventClass[]> {

        private final String LOG_TAG = FetchEventsTask.class.getSimpleName();

        @Override
        protected CalendarClass.EventClass[] doInBackground(String... params) {
            return myCalendar.myFunction(getActivity(), params[0]);
        }

        @Override
        protected void onPostExecute(CalendarClass.EventClass[] result) {
            if(result != null){
                mEventAdapter.clear();
                for(CalendarClass.EventClass event: CalendarClass.allEventLists){
                    mEventAdapter.add(event);
                }
            }
        }
    }
}