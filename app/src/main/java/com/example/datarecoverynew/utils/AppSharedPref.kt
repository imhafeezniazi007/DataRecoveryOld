package com.example.datarecoverynew.utils

import com.example.datarecoverynew.storage.entities.BPAds
import com.google.gson.Gson
import webservice.models.AppConstants


object AppSharedPref {


    private const val AD_SETTINGS = "AD_SETTINGS"
    private const val IS_NEW_USER = "IS_NEW_USER"
    private const val IS_SIGNED_IN = "IS_SIGNED_IN"
    private const val IS_DARK_THEME = "IS_DARK_THEME"
    private const val FONT_SIZE = "FONT_SIZE"
    private const val CSV_SEP = "CSV_SEP"
    private const val IS_MORNING = "IS_MORNING"
    private const val IS_AFTERNOON = "IS_AFTERNOON"
    private const val IS_EVENING = "IS_EVENING"
    private const val IS_NIGHT = "IS_NIGHT"
    private const val SYSTOLIC_COLOR = "SYSTOLIC_COLOR"
    private const val DIASTOLIC_COLOR = "DIASTOLIC_COLOR"
    private const val PULSE_COLOR = "PULSE_COLOR"
    private const val WEIGHT_COLOR = "WEIGHT_COLOR"
    private const val IS_SIMULATOR_ENABLED = "IS_SIMULATOR_ENABLED"
    private const val IS_USER_PAID = "IS_USER_PAID"
    private const val LAST_SYNC_DATE = "LAST_SYNC_DATE"
    private const val CACHED_MORE_APPS = "CACHED_MORE_APPS"
    private const val IS_SOUND_ENABLED = "IS_SOUND_ENABLED"
    private const val IS_VIB_ENABLED = "IS_VIB_ENABLED"

    var isPaid: Boolean
        get() = SharedPref.get(IS_USER_PAID, false)
        set(isPaid) = SharedPref.set(IS_USER_PAID, isPaid)

    var systolicColor: String
        get() = SharedPref.get(SYSTOLIC_COLOR, "#F51A47").toString()
        set(value) = SharedPref.set(SYSTOLIC_COLOR, value)

    var diastolicColor: String
        get() = SharedPref.get(DIASTOLIC_COLOR, "#2777FF").toString()
        set(value) = SharedPref.set(DIASTOLIC_COLOR, value)

    var pulseColor: String
        get() = SharedPref.get(PULSE_COLOR, "#00EA72").toString()
        set(value) = SharedPref.set(PULSE_COLOR, value)

    var weightColor: String
        get() = SharedPref.get(WEIGHT_COLOR, "#F51A47").toString()
        set(value) = SharedPref.set(WEIGHT_COLOR, value)

    var isDarkTheme: Boolean
        get() = SharedPref.get(IS_DARK_THEME, false)
        set(obj) = SharedPref.set(IS_DARK_THEME, obj)

    var fontSize: String
        get() = SharedPref.get(FONT_SIZE, AppConstants.FONTS.MEDIUM).toString()
        set(obj) = SharedPref.set(FONT_SIZE, obj)

    var csvSeparator: String
        get() = SharedPref.get(CSV_SEP, "Comma").toString()
        set(obj) = SharedPref.set(CSV_SEP, obj)


    var isNewUser: Boolean
        get() = SharedPref.get(IS_NEW_USER, true)
        set(isNewUser) = SharedPref.set(IS_NEW_USER, isNewUser)

    var isSignedIn: Boolean
        get() = SharedPref.get(IS_SIGNED_IN, false)
        set(isNewUser) = SharedPref.set(IS_SIGNED_IN, isNewUser)

    var isMorningFilter: Boolean
        get() = SharedPref.get(IS_MORNING, true)
        set(isNewUser) = SharedPref.set(IS_MORNING, isNewUser)

    var isAfternoonFilter: Boolean
        get() = SharedPref.get(IS_AFTERNOON, true)
        set(isNewUser) = SharedPref.set(IS_AFTERNOON, isNewUser)

    var isEveningFilter: Boolean
        get() = SharedPref.get(IS_EVENING, true)
        set(isNewUser) = SharedPref.set(IS_EVENING, isNewUser)

    var isNightFilter: Boolean
        get() = SharedPref.get(IS_NIGHT, true)
        set(isNewUser) = SharedPref.set(IS_NIGHT, isNewUser)

    var adSettings: BPAds
        get() = Gson().fromJson(SharedPref.get(AD_SETTINGS, "{}"), BPAds::class.java)
        set(adSettings) = SharedPref.set(AD_SETTINGS, Gson().toJson(adSettings))


    var isSimulatorEnabled: Boolean
        get() = SharedPref.get(IS_SIMULATOR_ENABLED, true)
        set(obj) = SharedPref.set(IS_SIMULATOR_ENABLED, obj)

    var isSoundEnabled: Boolean
        get() = SharedPref.get(IS_SOUND_ENABLED, true)
        set(obj) = SharedPref.set(IS_SOUND_ENABLED, obj)

    var isVibEnabled: Boolean
        get() = SharedPref.get(IS_VIB_ENABLED, true)
        set(obj) = SharedPref.set(IS_VIB_ENABLED, obj)

    var lastSyncDate: Long
        get() = SharedPref.get(LAST_SYNC_DATE, 0L)
        set(obj) = SharedPref.set(LAST_SYNC_DATE, obj)



}
