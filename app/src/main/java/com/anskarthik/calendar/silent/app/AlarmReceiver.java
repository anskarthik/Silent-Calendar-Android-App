package com.anskarthik.calendar.silent.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by user on 09-01-2016.
 */

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final String TAG = "My Broadcast Receiver";

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            Intent Newintent = new Intent(context, AlarmReceiver.class).putExtra("Vibrate", false);
            Newintent.setAction("com.anskarthik.calendar.silent.app.ACTION_DAILY_UPDATE");

            boolean alarmUp = (PendingIntent.getBroadcast(context, 0, Newintent,
                    PendingIntent.FLAG_NO_CREATE) != null);
            if (!alarmUp){
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 22);

                // With setInexactRepeating(), you have to use one of the AlarmManager interval
                // constants--in this case, AlarmManager.INTERVAL_DAY.
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(context, 0, Newintent,
                                PendingIntent.FLAG_UPDATE_CURRENT));
            }
            Log.v(TAG,"Boot Complete action complete");
            return;
        }

        Bundle extras = intent.getExtras();

        boolean state = true;
        //String spFileName = null;
        if (extras != null) {
            state = extras.getBoolean("Vibrate");
            //spFileName = extras.getString("SharedPrefFileName");
        }

        //SharedPreferences prefs = context.getSharedPreferences("com.example.calendar.silent.app_", Context.MODE_PRIVATE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String emailId = prefs.getString("emailId", "");
        Log.v(TAG,"here");
        Log.v(TAG, emailId );

        if(intent.getAction().equals("com.anskarthik.calendar.silent.app.ACTION_DAILY_UPDATE")){
            CalendarClass mycal = new CalendarClass(prefs.getString("emailId", ""));
            mycal.myFunction(context.getApplicationContext(),emailId);
            Log.v(TAG, "DAILY update action complete");
            return;
        }

        long eventId = extras.getLong("eventId");
        Set<String> stSet = prefs.getStringSet("current_event",null);
        SharedPreferences.Editor editor = prefs.edit();
        if(stSet!=null){
            if(state)   stSet.add(eventId+"");
            else{
                if(stSet.contains(eventId + ""))    stSet.remove(eventId+"");
            }
            editor.remove("current_event");
            editor.commit();
            editor.putStringSet("current_event", stSet);    editor.commit();
        }
        else{
            if(state) {
                String[] elements = new String[1];
                elements[0] = eventId + "";
                stSet = new HashSet<String>(Arrays.asList(elements));
                editor.putStringSet("current_event", stSet);    editor.commit();
            }
            else return;
        }

        if(!state) {
            if(!stSet.isEmpty()) return;
            Toast.makeText(context, "Ringer Mode Normal", Toast.LENGTH_SHORT).show();
            AudioManager myAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
        else{
            Toast.makeText(context, "Vibration Mode", Toast.LENGTH_SHORT).show();
            AudioManager myAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            myAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }

    }
}