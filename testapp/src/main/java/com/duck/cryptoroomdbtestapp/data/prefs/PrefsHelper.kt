package com.duck.cryptoroomdbtestapp.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.duck.prefshelper.BasePrefsHelper

class PrefsHelper(context: Context) {
    private val normalPrefs by lazy { NormalPrefs(context) }

    var key: String by normalPrefs::key
    var iv: String by normalPrefs::iv
}

class NormalPrefs(context: Context) : BasePrefsHelper() {
    override val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("normal_prefs", Context.MODE_PRIVATE)

    var key: String
        get() = getString(KEY, "")
        set(value) {
            setString(KEY, value)
        }
    var iv: String
        get() = getString(INITIALISATION_VECTOR, "")
        set(value) {
            setString(INITIALISATION_VECTOR, value)
        }

    companion object {
        private const val KEY = "uk.co.mowtivated.enc_prefs.key"
        private const val INITIALISATION_VECTOR =
            "uk.co.mowtivated.enc_prefs.initialisation_vector"
    }
}