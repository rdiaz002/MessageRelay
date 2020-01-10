package com.example.messagerelay.View;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.messagerelay.R;

public class MainActivity extends AppCompatActivity {

    private SettingsFragment settingsFragment;
    private String[] permissions = {Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.SEND_SMS};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settingsFragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settingframe, settingsFragment)
                .commit();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(settingsFragment);


        for (String i : permissions) {
            if (checkSelfPermission(i) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(permissions, 1001);
                break;
            }
        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
