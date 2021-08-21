package com.example.myapplication.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.myapplication.R

class SettingsFragment: PreferenceFragmentCompat() {

    private lateinit var mListPreference: ListPreference
    private lateinit var mTimeListPreference: ListPreference
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        mListPreference = findPreference("radius")!!

        mListPreference.title = "Distanza (${ mListPreference.entry})"

        mListPreference.setOnPreferenceChangeListener { preference, newValue ->
            if (preference is ListPreference) {
                val index = preference.findIndexOfValue(newValue.toString())
                val entry = preference.entries[index]
                preference.title = "Distanza ($entry)"
            }
            true
        }

        mTimeListPreference = findPreference("time")!!

        mTimeListPreference.title = "Durata database (${ mTimeListPreference.entry})"

        mTimeListPreference.setOnPreferenceChangeListener { preference, newValue ->
            if (preference is ListPreference) {
                val index = preference.findIndexOfValue(newValue.toString())
                val entry = preference.entries[index]
                preference.title = "Durata database ($entry)"
            }
            true
        }
    }
}