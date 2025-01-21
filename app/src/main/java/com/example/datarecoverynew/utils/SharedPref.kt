package com.example.datarecoverynew.utils

import android.preference.PreferenceManager
import com.example.datarecoverynew.AppController


class SharedPref {

    private fun e(str: String): String {

        return str
    }

    companion object {

        private val LoginType = "LoginType"

        val loginType: Int
            get() = get(LoginType, 0)

        fun setAppLoginType(value: Int) {
            set(LoginType, value)
        }

        // General Methods

        fun clearEntireCache() {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(AppController.context)
            val edit = prefs.edit()
            edit.clear()
            edit.commit()
        }

        operator fun get(valueKey: String, valueDefault: String): String? {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(AppController.context)
            return prefs.getString(valueKey, valueDefault)
        }

        operator fun set(valueKey: String, value: String) {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(AppController.context)
            val edit = prefs.edit()
            edit.putString(valueKey, value)
            edit.commit()
        }

        operator fun get(valueKey: String, valueDefault: Int): Int {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(AppController.context)
            return prefs.getInt(valueKey, valueDefault)
        }

        operator fun set(valueKey: String, value: Int) {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(AppController.context)
            val edit = prefs.edit()
            edit.putInt(valueKey, value)
            edit.commit()
        }

        operator fun get(valueKey: String, valueDefault: Boolean): Boolean {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(AppController.context)
            return prefs.getBoolean(valueKey, valueDefault)
        }

        operator fun set(valueKey: String, value: Boolean) {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(AppController.context)
            val edit = prefs.edit()
            edit.putBoolean(valueKey, value)
            edit.commit()
        }

        operator fun get(valueKey: String, valueDefault: Long): Long {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(AppController.context)
            return prefs.getLong(valueKey, valueDefault)
        }

        operator fun set(valueKey: String, value: Long) {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(AppController.context)
            val edit = prefs.edit()
            edit.putLong(valueKey, value)
            edit.commit()
        }

        operator fun get(valueKey: String, valueDefault: Float): Float {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(AppController.context)
            return prefs.getFloat(valueKey, valueDefault)
        }

        operator fun set(valueKey: String, value: Float) {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(AppController.context)
            val edit = prefs.edit()
            edit.putFloat(valueKey, value)
            edit.commit()
        }

        /* fun en(plain: String): String {
             i
         }*/
    }
}
