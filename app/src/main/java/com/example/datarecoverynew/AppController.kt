package com.example.datarecoverynew

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.akexorcist.localizationactivity.core.LanguageSetting
import com.akexorcist.localizationactivity.ui.LocalizationApplication
import com.example.datarecoverynew.ads.AdmobNativeAd
import com.example.datarecoverynew.utils.AppPreferences
import com.example.datarecoverynew.utils.ThemesUtils
import com.example.datarecoverynew.views.activities.CrashActivity
import com.example.diabetes.helper.ads.AdmobInterstialAd
import com.example.datarecoverynew.utils.ExceptionHandler
import com.example.datarecoverynew.utils.SharedPrefsHelper
import com.example.datarecoverynew.viewmodels.AppOpenAdViewModel
import com.example.datarecoverynew.viewmodels.MainViewModel
import com.example.datarecoverynew.views.activities.LoaderDialogActivity
import com.example.datarecoverynew.views.activities.SplashActivity
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Locale

class AppController : LocalizationApplication(),
    Application.ActivityLifecycleCallbacks,
    LifecycleObserver {
    override fun getDefaultLanguage(context: Context) = Locale.ENGLISH

    private var currentActivity: Activity? = null
    private var appOpenAd: AppOpenAd? = null
    private var isAdShowing = false
    private val sharedPrefsHelper by lazy { SharedPrefsHelper(this) }
    lateinit var appOpenAdViewModel: AppOpenAdViewModel

    companion object {
        private var onInterstitialCloseListener: (() -> Unit)? = null

        lateinit var context: Context

        val splshinterstialAd by lazy {
            AdmobInterstialAd()
        }

        val maininterstialAd by lazy {
            AdmobInterstialAd()
        }

        val nativeAdRef by lazy {
            AdmobNativeAd()
        }

        fun setOnAddClickListener(listener: (() -> Unit)) {
            onInterstitialCloseListener = listener
        }


    }


    override fun onCreate() {
        super.onCreate()

        appOpenAdViewModel =
            ViewModelProvider.AndroidViewModelFactory(this).create(AppOpenAdViewModel::class.java)

        val locale = Locale(Resources.getSystem().configuration.locale.language)
        LanguageSetting.setLanguage(this, locale)

        ThemesUtils.sTheme = AppPreferences.getInstance(this).getTheme
        ExceptionHandler.initialize(this, CrashActivity::class.java)

        context = applicationContext
        AdmobInterstialAd.initAds(this)
        AudienceNetworkAds.initialize(this)

        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        if (isAppOpenEnabled()) {
            if (currentActivity is SplashActivity) {

            } else {
                if (currentActivity != null) {
                    val isVisible = appOpenAdViewModel.isAdDisplayed.value!!
                    Log.d("check!!!", "onViewCreatedinFragmentApplication: $isVisible")
                    if (!isVisible) {
                        Log.d("check!!!", "onMoveToForeground: ")
                        appOpenAdViewModel.updateAdStatus(true, "Application Class")
                        showLoadingDialog()
                        currentActivity?.let { loadAppOpenAd(it) }
                    }
                }
            }
        }
    }

    private fun isAppOpenEnabled(): Boolean {
        return sharedPrefsHelper.getIsAppOpenEnabled()
    }

    private fun dismissLoadingDialog() {
        currentActivity?.let { activity ->
            if (activity is LoaderDialogActivity) {
                activity.finish()
            }
        }
    }

    private fun showLoadingDialog() {
        currentActivity?.let { activity ->
            val intent = Intent(activity, LoaderDialogActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity.startActivity(intent)
        }
    }

    private fun loadAppOpenAd(activity: Activity) {
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            this,
            sharedPrefsHelper.getAppOpenAdId(),
            adRequest,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    dismissLoadingDialog()
                    showAdIfAvailable(activity)
                    {

                    }
                    Log.d("AppOpenAd", "Ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.d("AppOpenAd", "Failed to load ad: ${error.message}")
                    dismissLoadingDialog()
                }
            }
        )
    }

    fun showAdIfAvailable(activity: Activity, onAdDismissed: () -> Unit) {
        if (isAdShowing || appOpenAd == null) {
            onAdDismissed()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isAdShowing = false
                appOpenAd = null
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                isAdShowing = false
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                isAdShowing = true
            }
        }

        appOpenAd?.show(activity)
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityStarted(p0: Activity) {
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStopped(p0: Activity) {}

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

}