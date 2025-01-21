package com.example.diabetes.helper.ads

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build


/**
 * @author Umer Bilal
 * Created 01/06/2023 at 9:50 PM
 */


var FAIL_AD_INTERSTAIL_COUNTER = 0
var MAX_AD_FAIL_LOAD_LIMIT = 4
val isFailAdINTERSTAILLimitReached = FAIL_AD_INTERSTAIL_COUNTER >= MAX_AD_FAIL_LOAD_LIMIT

fun Context._getResNameOfVariable(idd: Int): String {
    return getResources().getResourceEntryName(idd)
}

enum class AdmobStatusType(var type: Int) {
    Fail_to_load_ad(1),
    Ad_ShownFullScreenDismiss(8),
}

fun Context.isNetworkAvailable(): Boolean {
    //////////////////////if Item is purchased
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val cap = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val networks = cm.allNetworks
        for (n in networks) {
            val nInfo = cm.getNetworkInfo(n)
            if (nInfo != null && nInfo.isConnected) return true
        }
    } else {
        val networks = cm.allNetworkInfo
        for (nInfo in networks) {
            if (nInfo != null && nInfo.isConnected) return true
        }
    }
    return false
}


//fun Activity.showAndReloadAd(){
//    MainApplication.interstialAd.show_Interstial_Ad(this) {
//
//        when(it){
//            AdmobStatusType.Fail_to_load_ad -> {
//                if (isFailAdINTERSTAILLimitReached.not())
//                    MainApplication.Companion.interstialAd.loadInterStialAd(
//                        this,
//                        R.string.admob_inter_statial
//                    )
//            }
//            AdmobStatusType.Ad_ShownFullScreenDismiss -> {
//                MainApplication.Companion.interstialAd.loadInterStialAd(
//                    this,
//                    R.string.admob_inter_statial
//                ) {
//
//                }
//            }
//        }
//
//    }
//}

