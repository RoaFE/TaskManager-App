package com.example.steps.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.steps.R


class SettingsFragment : PreferenceFragmentCompat(){
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_fragment, rootKey)


    }
}