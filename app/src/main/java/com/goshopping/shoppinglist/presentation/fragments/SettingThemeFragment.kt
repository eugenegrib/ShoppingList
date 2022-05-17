package com.goshopping.shoppinglist.presentation.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.goshopping.shoppinglist.R

class SettingThemeFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_theme, rootKey)
        val button: Preference? = findPreference("theme")
        button?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            ColorFragment().show(this.requireFragmentManager(), "simple dialog")
            true
        }
    }
}