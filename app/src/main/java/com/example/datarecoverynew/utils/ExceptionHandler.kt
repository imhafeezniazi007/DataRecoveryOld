package com.example.datarecoverynew.utils

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import java.lang.Exception
import java.lang.Thread.UncaughtExceptionHandler
import kotlin.system.exitProcess

class ExceptionHandler private constructor(val applicationContext: Context,val defaulthandler:Thread.UncaughtExceptionHandler,
    val activityToBeLaunched:Class<*>):Thread.UncaughtExceptionHandler{
    override fun uncaughtException(p0: Thread, p1: Throwable) {
        try {
            launchActivity(applicationContext,activityToBeLaunched,p1)
            exitProcess(0)
        }catch (ex:Exception){
            defaulthandler.uncaughtException(p0,p1)
        }
    }

    private fun launchActivity(application: Context,
                               activity:Class<*>,
                               exception: Throwable){

        val crashedIntent = Intent(application,activity).also {
            it.putExtra(INTENT_DATA_NAME, Gson().toJson(exception))
        }
        crashedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or
        Intent.FLAG_ACTIVITY_NEW_TASK)
        crashedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        application.startActivity(crashedIntent)
    }

    companion object{
        private const val INTENT_DATA_NAME = "CrashData"
        private const val TAG = "CrashData"

        fun initialize(applicationContext: Context,activityToBeLaunched: Class<*>){
            val handler = ExceptionHandler(
                applicationContext,
                Thread.getDefaultUncaughtExceptionHandler() as UncaughtExceptionHandler,
                activityToBeLaunched
            )
            Thread.setDefaultUncaughtExceptionHandler(handler)
        }
        fun getThrowableFromIntent(intent: Intent):Throwable?{
            return try {

                Gson().fromJson(intent.getStringExtra(INTENT_DATA_NAME),Throwable::class.java)
            }catch (ex:Exception){
                null
            }
        }
    }
}