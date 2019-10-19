package com.example.messagerelay;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String IP_ADDRESS = "127.0.0.1";
    private String PORT = "8080";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settingframe, new SettingsFragment())
                .commit();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        //Retreive correct Address and port from preference storage.
        IP_ADDRESS = pref.getString("ip_address", null);
        PORT = pref.getString("port", null);

        //Check if Service is running and update preference in case of phone restart or destroyed activity
        SharedPreferences.Editor ed = pref.edit();
        if (RelayService.SERVICE_RUNNING) {
            ed.putBoolean("Service_On_Off", true);
        } else {
            ed.putBoolean("Service_On_Off", false);
        }

        ed.commit();

        pref.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        if (s.equals("port")) { //port preference changes
            PORT = s;
        } else if (s.equals("ip_address")) { //ip address preference changes
            IP_ADDRESS = s;
        } else if (s.equals("Service_On_Off")) { //Service Toggle
            Intent relayService = new Intent(this,RelayService.class);

            if (sharedPreferences.getBoolean(s, false)) {
                startService(relayService);
            } else {
                stopService(relayService);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("main_activity", "destroyed");
    }
}
