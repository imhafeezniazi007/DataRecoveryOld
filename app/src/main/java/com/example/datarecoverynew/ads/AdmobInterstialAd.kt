package com.example.diabetes.helper.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.datarecoverynew.AppController
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * @author Umer Bilal
 * Created 01/06/2023 at 9:48 PM
 */
class AdmobInterstialAd {


    //    val myPreferences by inject<MyPrefrecnces> ()
    private val TAG = "AdmobAds"
    private var currentFireBaseConfigDecisionShowAd = 0
    private var mInterstitialAd: InterstitialAd? = null


    //    val isItemPurchased = fun(): Boolean {
//        return myPreferences.getDataBoolean(MyPrefrecnces.IS_ITEM_PURCHASED, false)
//            ?: false
//    }
    companion object {
        val FIREBASE_JSON_VALIDATOR_NAME = "DocSettingParams"
        fun initAds(context: Context) {
            MobileAds.initialize(context) {}
        }

        var IS_INTERSTIAL_AD_SHOWING = false

    }

    fun loadInterStialAd(
        context: Context,
        addId: String,
        refcallback: (loadedOrNot: Boolean) -> Unit = {}
    ) {

        Log.e(TAG, "loadInterStialAd: " + context.applicationInfo.className)
        val adRequest = AdRequest.Builder().build()
        if (context.isNetworkAvailable() && mInterstitialAd == null) {
            InterstitialAd.load(
                context,
                addId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e(TAG + "fil", "onAdFailedToLoad: InterStatil" + adError.message)
                        mInterstitialAd = null
                        FAIL_AD_INTERSTAIL_COUNTER++
                        refcallback.invoke(false)
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(
                            "MyLogs",
                            "Banner adapter class name:" + interstitialAd.responseInfo.mediationAdapterClassName
                        )
                        mInterstitialAd = interstitialAd
                        refcallback.invoke(true)
                    }
                })
        } else {
            refcallback.invoke(false)
        }
    }

    fun show_Interstial_Ad(activity: Activity, callback: (status: AdmobStatusType) -> Unit) {
//        if (isItemPurchased.invoke()) {
//            callback.invoke(AdmobStatusType.Fail_to_load_ad)
//            return
//        }
//        if (!Firebase.remoteConfig._getTheRequiredID(activity, currentFireBaseConfigDecisionShowAd)) {
//            callback.invoke(AdmobStatusType.Fail_to_load_ad)
//            return
//        }
        if (mInterstitialAd != null) {

            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    super.onAdClicked()


                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    // AppOpenManager.isAppopenAllowed=true
                    Log.e(TAG, "onAdFailedToShowFullScreenContent: " + getVariableName(activity))
                    mInterstitialAd = null

                    callback.invoke(AdmobStatusType.Fail_to_load_ad)
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    IS_INTERSTIAL_AD_SHOWING = true


                    // AppOpenManager.isAppopenAllowed=false
                    Log.e(TAG, "onAdShowedFullScreenContent: " + getVariableName(activity))
                    //callback.invoke(AdmobStatusType.Fail_to_load_ad)
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    callback.invoke(AdmobStatusType.Ad_ShownFullScreenDismiss)

                    IS_INTERSTIAL_AD_SHOWING = false
                    // AppOpenManager.isAppopenAllowed=true
                    mInterstitialAd = null

                }

                override fun onAdImpression() {
                    super.onAdImpression()


                    Log.e(TAG, "onAdImpression: " + getVariableName(activity))
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            callback.invoke(AdmobStatusType.Fail_to_load_ad)
        }

    }

    fun getVariableName(activity: Activity) =
        activity._getResNameOfVariable(currentFireBaseConfigDecisionShowAd)

}