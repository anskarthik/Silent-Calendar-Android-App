package com.anskarthik.calendar.silent.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by user on 09-01-2016.
 */
public class CalendarClass {

    public static String AccountName;
    public static EventClass[] allEventLists;
    public static final String SP_EventTag = "_eventOnSP";
    //public static ArrayList<InstanceClass> instList = new ArrayList<InstanceClass>();

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    private static final String DEBUG_TAG = "CALENDER CLASS";
    public static final String[] INSTANCE_PROJECTION = new String[] {
            CalendarContract.Instances.EVENT_ID,      // 0
            CalendarContract.Instances.BEGIN,         // 1
            CalendarContract.Instances.TITLE,         // 2
            CalendarContract.Instances._ID,           // 3
            CalendarContract.Instances.END            // 4
    };

    // The indices for the projection array above.
    //private static final int PROJECTION_ID_INDEX = 0;             // Already declared above
    private static final int PROJECTION_BEGIN_INDEX = 1;
    private static final int PROJECTION_TITLE_INDEX = 2;
    private static final int PROJECTION_INSTANCE_ID_INDEX = 3;
    private static final int PROJECTION_INSTANCE_END_INDEX = 4;

    public CalendarClass(String accountName) {
        AccountName = accountName;
    }

    public class EventClass {
        public long eventID;
        public String title;
        public String startTime;
        public String endTime;
        public boolean on;
        public ArrayList<InstanceClass> Instances;

        public EventClass() {}
        public EventClass(long id, String title, String starting, String ending, ArrayList<InstanceClass> instances) {
            this.eventID = id;
            this.title = title;
            this.startTime = starting;
            this.endTime = ending;
            this.on = false;
            this.Instances = instances;
        }
    }

    public class InstanceClass {
        public long InstID;
        public long eventID;
        public long startTime;
        public long endTime;
        public InstanceClass(long id,long evId, long starting, long ending){
            this.InstID = id;
            this.eventID = evId;
            this.startTime = starting;
            this.endTime = ending;
        }
    }
    // Used this link: https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
    public static class EventClassAdapter extends ArrayAdapter<EventClass> {
        private static final String TAG = EventClassAdapter.class.getSimpleName();

        public EventClassAdapter(Context context, ArrayList<EventClass> event) {
            super(context, 0, event);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            // Get the data item for this position
            final EventClass event = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.new_list_item, parent, false);
            }
            // Lookup view for data population
            TextView tvTitle = (TextView) convertView.findViewById(R.id.list_item_event_title);
            TextView tvSubTitle = (TextView) convertView.findViewById(R.id.list_item_event_subtitle);
            Switch tvSwitch = (Switch) convertView.findViewById(R.id.list_item_switch);
            // Populate the data into the template view using the data object
            tvTitle.setText(event.title);
            String sub = "Starting : "+event.startTime;
            tvSubTitle.setText(sub);

            tvSwitch.setOnCheckedChangeListener(null);

            //SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            //Set<String> eventOnSP = app_preferences.getStringSet(AccountName+SP_EventTag, null);
            //if(eventOnSP != null){
            //    if(eventOnSP.contains(allEventLists[position].eventID+":0"))    tvSwitch.setChecked(false);
            //    else {
            //        if (eventOnSP.contains(allEventLists[position].eventID + ":1"))
            //            tvSwitch.setChecked(true);
            //        else
            //            tvSwitch.setChecked(allEventLists[position].on);
            //    }
            //}
            tvSwitch.setChecked(allEventLists[position].on);

            tvTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Title");
                    Toast.makeText(parent.getContext(), event.title, Toast.LENGTH_SHORT).show();
                    long eventID = event.eventID;
                    Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
                    Intent intent = new Intent(Intent.ACTION_VIEW)
                            .setData(uri);
                    parent.getContext().startActivity(intent);
                }
            });

            tvSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    Log.v(TAG, "State : " + allEventLists[position].on + " " + allEventLists[position].eventID + " " + event.eventID);
                    Log.v(TAG, "State : " + allEventLists[0].on + " " + allEventLists[0].eventID + " " + event.eventID);
                    long graceTime = 30*1000;
                    if (isChecked) {
                        // The toggle is enabled
                        allEventLists[position].on = true;
                        addEventOnShPref(getContext(), allEventLists[position].eventID, true);
                        for (InstanceClass inst : allEventLists[position].Instances) {
                            setAlarm(getContext(), (int) inst.InstID, inst.startTime - graceTime, true, allEventLists[position].eventID);//inst.startTime - 30*1000
                            if(System.currentTimeMillis()>(inst.startTime - graceTime) && System.currentTimeMillis()<(inst.endTime + graceTime)){
                                setAlarm(getContext(), (int) inst.InstID, System.currentTimeMillis()+2*1000, true, allEventLists[position].eventID);
                            }
                            setAlarm(getContext(), (int) inst.InstID, inst.endTime + graceTime, false, allEventLists[position].eventID);
                        }
                        Toast.makeText(getContext(),"Phone will be set to Vibration Mode every time this Event starts",Toast.LENGTH_SHORT).show();
                        Toast.makeText(getContext(),"and will be Back to Normal Mode every time this Event ends.",Toast.LENGTH_SHORT).show();
                    } else {
                        // The toggle is disabled
                        allEventLists[position].on = false;
                        addEventOnShPref(getContext(), allEventLists[position].eventID, false);
                        for (InstanceClass inst : allEventLists[position].Instances) {
                            cancelAlarm(getContext(), (int) inst.InstID, inst.startTime - graceTime, true, allEventLists[position].eventID);
                            cancelAlarm(getContext(), (int) inst.InstID, inst.endTime + graceTime, false, allEventLists[position].eventID);
                        }
                        Toast.makeText(getContext(),"This Event will be Ignored",Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Return the completed view to render on screen
            return convertView;
        }
    }

    public static void addEventOnShPref(Context context,long eventId, boolean state){
        String stSetEle = "";
        if(state) { stSetEle = eventId + ":1";  }
        else{   stSetEle = eventId + ":0";  }

        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = app_preferences.edit();
        if(app_preferences.contains(AccountName+SP_EventTag)) {
            Set<String> stSet = app_preferences.getStringSet(AccountName+SP_EventTag,null);
            if(stSet!=null && stSet.contains(stSetEle)){
                Log.v(DEBUG_TAG,"Same Switch State Already Recorded");
                return;
            }
            if(stSet!=null && state && stSet.contains(eventId + ":0")){    stSet.remove(eventId + ":0"); }
            if(stSet!=null && !state && stSet.contains(eventId + ":1")){    stSet.remove(eventId + ":1"); }

            if(stSet!=null){    stSet.add(stSetEle);    }
            else{
                String[] elements = new String[1];
                elements[0] = stSetEle;
                stSet = new HashSet<String>(Arrays.asList(elements));
            }
            editor.remove(AccountName + SP_EventTag);
            editor.commit();
            editor.putStringSet(AccountName + SP_EventTag, stSet);
        }
        else{
            String[] elements = new String[1];
            elements[0] = stSetEle;
            Set<String> stSet = new HashSet<String>(Arrays.asList(elements));
            editor.remove(AccountName + SP_EventTag);
            editor.commit();
            editor.putStringSet(AccountName + SP_EventTag, stSet);
        }
        editor.commit();
    }

    public static void setAlarm(Context context, int requestCode, long time, boolean state, long eventId){
        if(System.currentTimeMillis()>time)
            return;
        Intent intent = new Intent(context, AlarmReceiver.class).putExtra("Vibrate", state).putExtra("eventId",eventId);
        if(state)   intent.setAction("com.anskarthik.calendar.silent.app.ACTION_RINGER_VIBRATE");
        else        intent.setAction("com.anskarthik.calendar.silent.app.ACTION_RINGER_NORMAL");

        boolean alarmUp = (PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE) != null);
        if(alarmUp) {
            Log.v("SET ALARM", "Already exists at " + time +" rc: "+ requestCode);
            //return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time,
                PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        Log.v("SET ALARM", "setting at " + time + " rc: " + requestCode);
    }

    public static void cancelAlarm(Context context, int requestCode, long time, boolean state, long eventId){
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = app_preferences.edit();
        if(app_preferences.contains("current_event")) {
            Set<String> stSet = app_preferences.getStringSet("current_event",null);
            if(stSet!=null && stSet.contains(eventId+"")){
                stSet.remove(eventId+"");
                Log.v("CANCEL ALARM", "deleted from current set");
            }

            if(stSet==null || stSet.isEmpty()){
                AudioManager myAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
            editor.remove("current_event");
            editor.commit();
            editor.putStringSet("current_event", stSet);
        }
        else{
            AudioManager myAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
        editor.commit();

        if(System.currentTimeMillis()>time)
            return;
        Intent intent = new Intent(context, AlarmReceiver.class).putExtra("Vibrate", state);
        if(state)   intent.setAction("com.anskarthik.calendar.silent.app.ACTION_RINGER_VIBRATE");
        else        intent.setAction("com.anskarthik.calendar.silent.app.ACTION_RINGER_NORMAL");

        boolean alarmUp = (PendingIntent.getBroadcast(context, requestCode,
                intent, PendingIntent.FLAG_NO_CREATE) != null);
        if(!alarmUp) {
            Log.v("CANCEL ALARM", "No such alarm exists at " + time +" rc: "+ requestCode);
            return;
        }
        PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT).cancel();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT));
        PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT).cancel();
        Log.v("CANCEL ALARM", "cancelling at " + time + " rc: " + requestCode);
    }

    public EventClass[] myFunction(Context context) {
        return myFunction(context,AccountName);
    }

    public EventClass[] myFunction(Context context,String accountNAME) {
        if(accountNAME.equals("")) {
            accountNAME = AccountName;
        }
        else{
            AccountName = accountNAME;
        }
        Log.v("MY_FUNCTION_CALENDAR", "I am here"+AccountName+" : "+accountNAME+accountNAME.equals(AccountName));
        // Run query
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[]{accountNAME, "com.google",
                accountNAME};

        Log.v("MY_FUNCTION_CALENDAR", "I am here-2");
        // Submit the query and get a Cursor object back.
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        Log.v("MY_FUNCTION_CALENDAR", "I am here-3");
        // Use the cursor to step through the returned records

        ArrayList<EventClass> mylist = new ArrayList<EventClass>();

        while (cur.moveToNext()) {
            long calID = 0;
            String displayName = null;
            String accountName = null;
            String ownerName = null;

            // Get the field values
            calID = cur.getLong(PROJECTION_ID_INDEX);
            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

            Log.v("MY_FUNCTION_CALENDAR", "Event by ID " + displayName);
            //Toast.makeText(context, accountName, Toast.LENGTH_SHORT).show();
            // Do something with the values...
            mylist.addAll(getEventByID(context, 1, accountName));
        }
        cur.close();
        allEventLists = mylist.toArray(new EventClass[mylist.size()]);

        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> eventOnSP = app_preferences.getStringSet(AccountName+SP_EventTag, null);
        if(eventOnSP != null){
            int pos = 0;
            while(pos < allEventLists.length) {
                long graceTime = 30 * 1000;
                if (eventOnSP.contains(allEventLists[pos].eventID + ":0")){
                    allEventLists[pos].on = false;
                    for (InstanceClass inst : allEventLists[pos].Instances) {
                        cancelAlarm(context, (int) inst.InstID, inst.startTime - graceTime, true, allEventLists[pos].eventID);
                        cancelAlarm(context, (int) inst.InstID, inst.endTime + graceTime, false, allEventLists[pos].eventID);
                    }
                }
                else {
                    if (eventOnSP.contains(allEventLists[pos].eventID + ":1")) {
                        allEventLists[pos].on = true;
                        for (InstanceClass inst : allEventLists[pos].Instances) {
                            setAlarm(context, (int) inst.InstID, inst.startTime - graceTime, true, allEventLists[pos].eventID);//inst.startTime - 30*1000
                            if(System.currentTimeMillis()>(inst.startTime - graceTime) && System.currentTimeMillis()<(inst.endTime + graceTime)){
                                setAlarm(context, (int) inst.InstID, System.currentTimeMillis() + 2 * 1000, true, allEventLists[pos].eventID);
                            }
                            setAlarm(context, (int) inst.InstID, inst.endTime + graceTime, false, allEventLists[pos].eventID);
                        }
                    }
                }
                pos++;
            }
        }

        return mylist.toArray(new EventClass[mylist.size()]);
    }

    /**The main/basic URI for the calendar events table*/
    private static final Uri EVENT_URI = CalendarContract.Events.CONTENT_URI;

    /**Builds the Uri for events (as a Sync Adapter)*/
    public static Uri buildEventUri(String stACCOUNT_NAME) {
        return EVENT_URI
                .buildUpon()
                        //.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, stACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, "com.google")
                .build();
    }

    /**Finds an event based on the ID
     * @param ctx The context (e.g. activity)
     * @param id The id of the event to be found
     */
    public ArrayList<EventClass> getEventByID(Context ctx, long id, String stACCOUNT_NAME) {
        ContentResolver cr = ctx.getContentResolver();
        //Projection array for query (the values you want)
        final String[] PROJECTION = new String[] {
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.LAST_DATE
        };
        final int ID_INDEX = 0, TITLE_INDEX = 1, DESC_INDEX = 2, LOCATION_INDEX = 3,
                START_INDEX = 4, END_INDEX = 5, LAST_DATE_INDEX = 6;
        long start_millis=0, end_millis=0, last_date=0;
        String title=null, description=null, location=null;
        final String selection = "("+ CalendarContract.Events.OWNER_ACCOUNT+" = ?)";// AND "+ CalendarContract.Events._ID+" = ?)";
        final String[] selectionArgs = new String[] {stACCOUNT_NAME};//, id+""};
        Cursor cursor = cr.query(buildEventUri(stACCOUNT_NAME), PROJECTION, selection, selectionArgs, null);
        //at most one event will be returned because event ids are unique in the table
        Log.v("MY_FUNCTION_CALENDAR", "Event by ID Inside");

        ArrayList<EventClass> mylist = new ArrayList<EventClass>();
        while(cursor.moveToNext()) {
            id = cursor.getLong(ID_INDEX);
            title = cursor.getString(TITLE_INDEX);
            description = cursor.getString(DESC_INDEX);
            location = cursor.getString(LOCATION_INDEX);
            start_millis = cursor.getLong(START_INDEX);
            end_millis = cursor.getLong(END_INDEX);
            last_date = cursor.getLong(LAST_DATE_INDEX);

            //do something with the values...
            String Start_dateTimeAsString = new Date(start_millis).toString();
            String End_dateTimeAsString = new Date(end_millis).toString();
            Log.v("MY_FUNCTION_CALENDAR", "Event by ID: " + id + "Title:" + title + " " + Start_dateTimeAsString + "; " + start_millis);
            //Toast.makeText(ctx, title+"\n Start: "+dateTimeAsString, Toast.LENGTH_SHORT).show();

            if(last_date==0 || (last_date+86400000)>System.currentTimeMillis())
                mylist.add(new EventClass(id, title, Start_dateTimeAsString,End_dateTimeAsString, getInstancesByID(ctx, id, stACCOUNT_NAME)));
            Log.v("MY_FUNCTION_CALENDAR","LAST DATE: "+last_date);
        }
        cursor.close();
        return mylist;
    }

    public ArrayList<InstanceClass> getInstancesByID(Context ctx, long id, String stACCOUNT_NAME) {
        // Specify the date range you want to search for recurring
        // event instances
        Calendar beginTime = Calendar.getInstance();
        //beginTime.set(2016, 0, 9, 8, 0);    // Month is 0-based
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(1970, 0, 3, 0, 0);   // Month is 0-based
        long endMillis = startMillis + endTime.getTimeInMillis();// + endTime.getTimeZone().getOffset(endTime.getTimeInMillis());

        endTime.set(1970, 0, 1, 0, 0);
        endMillis = endMillis - endTime.getTimeInMillis();

        Cursor cur = null;
        ContentResolver cr = ctx.getContentResolver();

        // The ID of the recurring event whose instances you are searching
        // for in the Instances table
        String selection = CalendarContract.Instances.EVENT_ID + " = ?";
        String[] selectionArgs = new String[] {""+id};

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // Submit the query
        cur =  cr.query(builder.build(),
                INSTANCE_PROJECTION,
                selection,
                selectionArgs,
                null);

        ArrayList<InstanceClass> mylist = new ArrayList<InstanceClass>();
        while (cur.moveToNext()) {
            String title = null;
            long eventID = 0;
            long inst_id = 0;
            long beginVal = 0, endVal = 0;

            // Get the field values
            inst_id = cur.getLong(PROJECTION_INSTANCE_ID_INDEX);
            eventID = cur.getLong(PROJECTION_ID_INDEX);
            beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
            title = cur.getString(PROJECTION_TITLE_INDEX);
            endVal = cur.getLong(PROJECTION_INSTANCE_END_INDEX);

            // Do something with the values.
            Log.i(DEBUG_TAG, "Event:  " + title);
            String Beg_dateTimeAsString = new Date(beginVal).toString();
            String End_dateTimeAsString = new Date(endVal).toString();
            String beg = new Date(startMillis).toString();
            String end = new Date(endMillis).toString();
            //Calendar calendar = Calendar.getInstance();
            //calendar.setTimeInMillis(beginVal);
            //DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            //Log.i(DEBUG_TAG, "Date: " + formatter.format(calendar.getTime()));
            Log.i(DEBUG_TAG, "Inst id: "+inst_id+" Start : " + Beg_dateTimeAsString+" End: "+End_dateTimeAsString);
            mylist.add(new InstanceClass(inst_id, eventID, beginVal, endVal));
        }
        cur.close();

        return mylist;
    }
}
