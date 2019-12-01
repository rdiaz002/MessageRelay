package com.example.messagerelay.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.messagerelay.Model.RelayService;
import com.example.messagerelay.R;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (RelayService.isServiceRunning()) {
            sharedPreferences.edit().putBoolean("Service_On_Off", true).apply();
        } else {
            sharedPreferences.edit().putBoolean("Service_On_Off", false).apply();
        }

        setPreferencesFromResource(R.xml.preferences,rootKey);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        if (s.equals("Service_On_Off")) { //Service Toggle
            Intent relayService = new Intent(getContext(), RelayService.class);

            if (sharedPreferences.getBoolean(s, false)) {
                getContext().startService(relayService);
            } else {
                getContext().stopService(relayService);
            }
        }
    }

}
