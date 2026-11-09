package com.example.data.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "ankitverse_session_prefs"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
    }

    var currentUserId: String?
        get() = prefs.getString(KEY_CURRENT_USER_ID, null)
        set(value) {
            prefs.edit().putString(KEY_CURRENT_USER_ID, value).apply()
        }

    val isLoggedIn: Boolean
        get() = !currentUserId.isNullOrBlank()

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_IS_FIRST_LAUNCH, true)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_FIRST_LAUNCH, value).apply()
        }

    fun clearSession() {
        prefs.edit().remove(KEY_CURRENT_USER_ID).apply()
    }
}
