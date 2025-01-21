package com.example.datarecoverynew.utils

import android.content.Context
import android.util.Log
import com.example.datarecoverynew.BuildConfig
import com.example.datarecoverynew.R
import com.example.datarecoverynew.interfaces.ICallBackListener
import com.example.datarecoverynew.models.ErrorDto
import com.example.datarecoverynew.storage.entities.AdSettings
import com.example.datarecoverynew.storage.entities.AdsIds
import com.example.datarecoverynew.storage.entities.BPAds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings


object AdDatabaseUtil {

    //    val check = false
    val check = BuildConfig.DEBUG
    fun getAdmobInterstitialAdId(context: Context): String {
        return if (check) context.getString(R.string.admob_inter_statial)
        else AppSharedPref.adSettings.adIds?.AdmobInt ?: ""
    }

    fun getAdmobBannerId(context: Context): String {
        return if (check) context.getString(R.string.admob_banner)
        else AppSharedPref.adSettings.adIds?.AdmobBanner ?: ""
    }

    fun getAdmobAppOpenId(context: Context): String {
        return if (check) context.getString(R.string.admob_app_open)
        else AppSharedPref.adSettings.adIds?.AdmobAppOpen ?: ""
    }

    fun getAdmobNativeAdId(context: Context): String {
        return if (check) context.getString(R.string.admob_native_ad)
        else AppSharedPref.adSettings.adIds?.AdNativeAd ?: ""
    }


    fun getAdSettings(listener: ICallBackListener<Any?>?) {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 1
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        val bpAds = BPAds()

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val bannerAdMob = remoteConfig.getString("bannerAdId")
                val bannerFacebook = remoteConfig.getString("facebookBannerId")
                val interstitialAdMob = remoteConfig.getString("interstitialAdId")
                val interstitialFacebook = remoteConfig.getString("facebookInterstitialId")
                val appOpenAdId = remoteConfig.getString("appOpenAdId")
                val nativeAdMob = remoteConfig.getString("nativeAdId")
                val nativeFacebook = remoteConfig.getString("facebookNativeId")
                val isNativeAdEnabled = remoteConfig.getBoolean("isNativeAdEnabled")
                val isBannerAdEnabled = remoteConfig.getBoolean("isBannerAdEnabled")
                val isInterstitialAdEnabled = remoteConfig.getBoolean("isInterstitialAdEnabled")
                val isAppOpenEnabled = remoteConfig.getBoolean("isAppOpenEnabled")
                val isFacebookBannerEnabled = remoteConfig.getBoolean("isFacebookBannerEnabled")
                val isFacebookInterstitialEnabled =
                    remoteConfig.getBoolean("isFacebookInterstitialEnabled")


                val adIds = AdsIds(
                    bannerAdMob,
                    interstitialAdMob,
                    appOpenAdId,
                    nativeAdMob,
                    bannerFacebook,
                    interstitialFacebook
                )
                val adsSetting = AdSettings(
                    isBannerAdEnabled,
                    isInterstitialAdEnabled,
                    isFacebookBannerEnabled,
                    isAppOpenEnabled,
                    isNativeAdEnabled,
                    isFacebookInterstitialEnabled
                )
                bpAds.adIds = adIds
                bpAds.addSettings = adsSetting

            } else {
                remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

                val bannerAdMob = remoteConfig.getString("bannerAdId")
                val bannerFacebook = remoteConfig.getString("facebookBannerId")
                val interstitialAdMob = remoteConfig.getString("interstitialAdId")
                val interstitialFacebook = remoteConfig.getString("facebookInterstitialId")
                val appOpenAdId = remoteConfig.getString("appOpenAdId")
                val nativeAdMob = remoteConfig.getString("nativeAdId")
                val nativeFacebook = remoteConfig.getString("facebookNativeId")
                val isNativeAdEnabled = remoteConfig.getBoolean("isNativeAdEnabled")
                val isBannerAdEnabled = remoteConfig.getBoolean("isBannerAdEnabled")
                val isInterstitialAdEnabled = remoteConfig.getBoolean("isInterstitialAdEnabled")
                val isAppOpenEnabled = remoteConfig.getBoolean("isAppOpenEnabled")
                val isFacebookBannerEnabled = remoteConfig.getBoolean("isFacebookBannerEnabled")
                val isFacebookInterstitialEnabled =
                    remoteConfig.getBoolean("isFacebookInterstitialEnabled")


                val adIds = AdsIds(
                    bannerAdMob,
                    interstitialAdMob,
                    appOpenAdId,
                    nativeAdMob,
                    bannerFacebook,
                    interstitialFacebook
                )
                val adsSetting = AdSettings(
                    isBannerAdEnabled,
                    isInterstitialAdEnabled,
                    isFacebookBannerEnabled,
                    isAppOpenEnabled,
                    isNativeAdEnabled,
                    isFacebookInterstitialEnabled
                )
                bpAds.adIds = adIds
                bpAds.addSettings = adsSetting
            }
            AppSharedPref.adSettings = bpAds
            listener?.onSuccess(bpAds, null)
        }
    }
}