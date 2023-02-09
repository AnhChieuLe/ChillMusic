package com.example.chillmusic.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.chillmusic.R

class FragmentSettingManager: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_preferences, rootKey)
    }
}