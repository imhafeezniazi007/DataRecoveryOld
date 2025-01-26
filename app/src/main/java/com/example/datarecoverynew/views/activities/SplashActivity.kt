package com.example.datarecoverynew.views.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.android.billingclient.api.BillingClient
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.BuildConfig
import com.example.datarecoverynew.R
import com.example.datarecoverynew.base.BaseActivity
import com.example.datarecoverynew.databinding.ActivitySplashBinding
import com.example.datarecoverynew.interfaces.ICallBackListener
import com.example.datarecoverynew.models.ErrorDto
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.datarecoverynew.utils.AppPreferences
import com.example.datarecoverynew.utils.AppSharedPref
import com.example.datarecoverynew.utils.SharedPref
import com.example.datarecoverynew.utils.SharedPrefsHelper
import com.example.recoverydata.interfaces.DataListener
import com.google.android.ads.mediationtestsuite.MediationTestSuite
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.subscription.ads.billing.SubscriptionViewModel
import com.subscription.ads.billing.SubscriptionsConstants
import okhttp3.Headers

class SplashActivity : BaseActivity() {
    lateinit var binding: ActivitySplashBinding
    var interstitialAdd: InterstitialAd? = null
    lateinit var billingClient: BillingClient
    private val subscriptionViewModel: SubscriptionViewModel by lazy {
        SubscriptionViewModel(
            application
        )
    }
    var lastHitTime: Long = 0
    private val sharedPref: SharedPrefsHelper by lazy { SharedPrefsHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 1
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val res =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/5224354917" else remoteConfig.getString(
                        "rewardedAdId"
                    )
                val keyAppOpen =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/9257395921" else remoteConfig.getString(
                        "appOpenAdId"
                    )
                val keyNativeLarge =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/2247696110" else remoteConfig.getString(
                        "keyNativeLarge"
                    )
                val interstitialHighRate =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712\n" else remoteConfig.getString(
                        "interstitialHighRate"
                    )
                val interstitialMediumRate =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712\n" else remoteConfig.getString(
                        "interstitialMediumRate"
                    )


                val resNtvEn = remoteConfig.getBoolean("isNativeSavedEnabled")
                val resNtvDup = remoteConfig.getBoolean("isNativeDuplicateEnabled")
                val resHme = remoteConfig.getBoolean("isHomeNativeEnabled")
                val resHmeBnr = remoteConfig.getBoolean("isHomeBannerEnabled")
                val useBnr = remoteConfig.getBoolean("isBannerUseEnabled")
                val showPremium = remoteConfig.getBoolean("showPremium")
                val isSavedAudioNativeEnabled = remoteConfig.getBoolean("isSavedAudioNativeEnabled")
                val isSavedFilesNativeEnabled = remoteConfig.getBoolean("isSavedFilesNativeEnabled")
                val isScanAudiosNativeEnabled = remoteConfig.getBoolean("isScanAudiosNativeEnabled")
                val isScanFilesNativeEnabled = remoteConfig.getBoolean("isScanFilesNativeEnabled")
                val isSavedImagesNativeEnabled =
                    remoteConfig.getBoolean("isSavedImagesNativeEnabled")
                val isSavedVideosNativeEnabled =
                    remoteConfig.getBoolean("isSavedVideosNativeEnabled")
                val isScanImagesNativeEnabled = remoteConfig.getBoolean("isScanImagesNativeEnabled")
                val isScanVideosNativeEnabled = remoteConfig.getBoolean("isScanVideosNativeEnabled")
                val isAppOpenEnabled = remoteConfig.getBoolean("isAppOpenEnabled")
                val isNativeDuplicateFragmentEnabled =
                    remoteConfig.getBoolean("isNativeDuplicateFragmentEnabled")
                val isNativeSavedFragmentEnabled =
                    remoteConfig.getBoolean("isNativeSavedFragmentEnabled")
                val isNativeDuplicateDialogEnabled =
                    remoteConfig.getBoolean("isNativeDuplicateDialogEnabled")
                val isNativeScanningDialogEnabled =
                    remoteConfig.getBoolean("isNativeScanningDialogEnabled")
                val isNativeExitEnabled =
                    remoteConfig.getBoolean("isNativeExitEnabled")
                val isNativeUseEnabled =
                    remoteConfig.getBoolean("isNativeUseEnabled")
                val isBannerSideNavEnabled =
                    remoteConfig.getBoolean("isBannerSideNavEnabled")
                val isPremiumHowToUseEnabled =
                    remoteConfig.getBoolean("isPremiumHowToUseEnabled")
                val isPremiumMonthlyEnabled =
                    remoteConfig.getBoolean("isPremiumMonthlyEnabled")
                val isScanFilesInterstitialEnabled =
                    remoteConfig.getBoolean("isScanFilesInterstitialEnabled")
                val isDuplicateActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isDuplicateActivityInterstitialEnabled")
                val isScanAudiosActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isScanAudiosActivityInterstitialEnabled")
                val isScanImagesActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isScanImagesActivityInterstitialEnabled")
                val isScanVideosActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isScanVideosActivityInterstitialEnabled")
                val isDuplicateFragmentInterstitialEnabled =
                    remoteConfig.getBoolean("isDuplicateFragmentInterstitialEnabled")
                val isHomeFragmentInterstitialEnabled =
                    remoteConfig.getBoolean("isHomeFragmentInterstitialEnabled")
                val isSavedFragmentInterstitialEnabled =
                    remoteConfig.getBoolean("isSavedFragmentInterstitialEnabled")

                sharedPref.setAdId(res)
                sharedPref.setIsNativeSavedEnabled(resNtvEn)
                sharedPref.setIsNativeDuplicateEnabled(resNtvDup)
                sharedPref.setIsNativeHomeEnabled(resHme)
                sharedPref.setIsBannerHomeEnabled(resHmeBnr)
                sharedPref.setIsBannerUseEnabled(useBnr)
                sharedPref.setIsPremiumEnabled(showPremium)
                sharedPref.setIsSavedAudioNativeEnabled(isSavedAudioNativeEnabled)
                sharedPref.setIsSavedFilesNativeEnabled(isSavedFilesNativeEnabled)
                sharedPref.setIsScanAudiosNativeEnabled(isScanAudiosNativeEnabled)
                sharedPref.setIsScanFilesNativeEnabled(isScanFilesNativeEnabled)
                sharedPref.setIsSavedImagesNativeEnabled(isSavedImagesNativeEnabled)
                sharedPref.setIsSavedVideosNativeEnabled(isSavedVideosNativeEnabled)
                sharedPref.setIsScanImagesNativeEnabled(isScanImagesNativeEnabled)
                sharedPref.setIsScanVideosNativeEnabled(isScanVideosNativeEnabled)
                sharedPref.setIsAppOpenEnabled(isAppOpenEnabled)
                sharedPref.setAppOpenAdId(keyAppOpen)
                sharedPref.setNativeLargeId(keyNativeLarge)
                sharedPref.setNativeDuplicateFragmentEnabled(isNativeDuplicateFragmentEnabled)
                sharedPref.setNativeSavedFragmentEnabled(isNativeSavedFragmentEnabled)
                sharedPref.setInterstitialHighId(interstitialHighRate)
                sharedPref.setInterstitialMediumId(interstitialMediumRate)
                sharedPref.setIsNativeDuplicateDialogEnabled(isNativeDuplicateDialogEnabled)
                sharedPref.setIsNativeScanningDialogEnabled(isNativeScanningDialogEnabled)
                sharedPref.setIsNativeExitEnabled(isNativeExitEnabled)
                sharedPref.setIsNativeUseEnabled(isNativeUseEnabled)
                sharedPref.setIsBannerSideNavEnabled(isBannerSideNavEnabled)
                sharedPref.setIsPremiumHowToUseEnabled(isPremiumHowToUseEnabled)
                sharedPref.setIsPremiumMonthlyEnabled(isPremiumMonthlyEnabled)
                sharedPref.setIsScanFilesInterstitialEnabled(isScanFilesInterstitialEnabled)
                sharedPref.setIsDuplicateActivityInterstitialEnabled(isDuplicateActivityInterstitialEnabled)
                sharedPref.setIsScanAudiosActivityInterstitialEnabled(isScanAudiosActivityInterstitialEnabled)
                sharedPref.setIsScanImagesActivityInterstitialEnabled(isScanImagesActivityInterstitialEnabled)
                sharedPref.setIsScanVideosActivityInterstitialEnabled(isScanVideosActivityInterstitialEnabled)
                sharedPref.setIsDuplicateFragmentInterstitialEnabled(isDuplicateFragmentInterstitialEnabled)
                sharedPref.setIsHomeFragmentInterstitialEnabled(isHomeFragmentInterstitialEnabled)
                sharedPref.setIsSavedFragmentInterstitialEnabled(isSavedFragmentInterstitialEnabled)
            } else {
                remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

                val res =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/5224354917" else remoteConfig.getString(
                        "rewardedAdId"
                    )
                val keyAppOpen =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/9257395921" else remoteConfig.getString(
                        "appOpenAdId"
                    )
                val keyNativeLarge =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/2247696110" else remoteConfig.getString(
                        "keyNativeLarge"
                    )
                val interstitialHighRate =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712\n" else remoteConfig.getString(
                        "interstitialHighRate"
                    )
                val interstitialMediumRate =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712\n" else remoteConfig.getString(
                        "interstitialMediumRate"
                    )


                val resNtvEn = remoteConfig.getBoolean("isNativeSavedEnabled")
                val resNtvDup = remoteConfig.getBoolean("isNativeDuplicateEnabled")
                val resHme = remoteConfig.getBoolean("isHomeNativeEnabled")
                val resHmeBnr = remoteConfig.getBoolean("isHomeBannerEnabled")
                val useBnr = remoteConfig.getBoolean("isBannerUseEnabled")
                val showPremium = remoteConfig.getBoolean("showPremium")
                val isSavedAudioNativeEnabled = remoteConfig.getBoolean("isSavedAudioNativeEnabled")
                val isSavedFilesNativeEnabled = remoteConfig.getBoolean("isSavedFilesNativeEnabled")
                val isScanAudiosNativeEnabled = remoteConfig.getBoolean("isScanAudiosNativeEnabled")
                val isScanFilesNativeEnabled = remoteConfig.getBoolean("isScanFilesNativeEnabled")
                val isSavedImagesNativeEnabled =
                    remoteConfig.getBoolean("isSavedImagesNativeEnabled")
                val isSavedVideosNativeEnabled =
                    remoteConfig.getBoolean("isSavedVideosNativeEnabled")
                val isScanImagesNativeEnabled = remoteConfig.getBoolean("isScanImagesNativeEnabled")
                val isScanVideosNativeEnabled = remoteConfig.getBoolean("isScanVideosNativeEnabled")
                val isAppOpenEnabled = remoteConfig.getBoolean("isAppOpenEnabled")
                val isNativeDuplicateFragmentEnabled =
                    remoteConfig.getBoolean("isNativeDuplicateFragmentEnabled")
                val isNativeSavedFragmentEnabled =
                    remoteConfig.getBoolean("isNativeSavedFragmentEnabled")
                val isNativeDuplicateDialogEnabled =
                    remoteConfig.getBoolean("isNativeDuplicateDialogEnabled")
                val isNativeScanningDialogEnabled =
                    remoteConfig.getBoolean("isNativeScanningDialogEnabled")
                val isNativeExitEnabled =
                    remoteConfig.getBoolean("isNativeExitEnabled")
                val isNativeUseEnabled =
                    remoteConfig.getBoolean("isNativeUseEnabled")
                val isBannerSideNavEnabled =
                    remoteConfig.getBoolean("isBannerSideNavEnabled")
                val isPremiumHowToUseEnabled =
                    remoteConfig.getBoolean("isPremiumHowToUseEnabled")
                val isPremiumMonthlyEnabled =
                    remoteConfig.getBoolean("isPremiumMonthlyEnabled")
                val isScanFilesInterstitialEnabled =
                    remoteConfig.getBoolean("isScanFilesInterstitialEnabled")
                val isDuplicateActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isDuplicateActivityInterstitialEnabled")
                val isScanAudiosActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isScanAudiosActivityInterstitialEnabled")
                val isScanImagesActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isScanImagesActivityInterstitialEnabled")
                val isScanVideosActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isScanVideosActivityInterstitialEnabled")
                val isDuplicateFragmentInterstitialEnabled =
                    remoteConfig.getBoolean("isDuplicateFragmentInterstitialEnabled")
                val isHomeFragmentInterstitialEnabled =
                    remoteConfig.getBoolean("isHomeFragmentInterstitialEnabled")
                val isSavedFragmentInterstitialEnabled =
                    remoteConfig.getBoolean("isSavedFragmentInterstitialEnabled")

                sharedPref.setAdId(res)
                sharedPref.setIsNativeSavedEnabled(resNtvEn)
                sharedPref.setIsNativeDuplicateEnabled(resNtvDup)
                sharedPref.setIsNativeHomeEnabled(resHme)
                sharedPref.setIsBannerHomeEnabled(resHmeBnr)
                sharedPref.setIsBannerUseEnabled(useBnr)
                sharedPref.setIsPremiumEnabled(showPremium)
                sharedPref.setIsSavedAudioNativeEnabled(isSavedAudioNativeEnabled)
                sharedPref.setIsSavedFilesNativeEnabled(isSavedFilesNativeEnabled)
                sharedPref.setIsScanAudiosNativeEnabled(isScanAudiosNativeEnabled)
                sharedPref.setIsScanFilesNativeEnabled(isScanFilesNativeEnabled)
                sharedPref.setIsSavedImagesNativeEnabled(isSavedImagesNativeEnabled)
                sharedPref.setIsSavedVideosNativeEnabled(isSavedVideosNativeEnabled)
                sharedPref.setIsScanImagesNativeEnabled(isScanImagesNativeEnabled)
                sharedPref.setIsScanVideosNativeEnabled(isScanVideosNativeEnabled)
                sharedPref.setIsAppOpenEnabled(isAppOpenEnabled)
                sharedPref.setAppOpenAdId(keyAppOpen)
                sharedPref.setNativeLargeId(keyNativeLarge)
                sharedPref.setNativeDuplicateFragmentEnabled(isNativeDuplicateFragmentEnabled)
                sharedPref.setNativeSavedFragmentEnabled(isNativeSavedFragmentEnabled)
                sharedPref.setInterstitialHighId(interstitialHighRate)
                sharedPref.setInterstitialMediumId(interstitialMediumRate)
                sharedPref.setIsNativeDuplicateDialogEnabled(isNativeDuplicateDialogEnabled)
                sharedPref.setIsNativeScanningDialogEnabled(isNativeScanningDialogEnabled)
                sharedPref.setIsNativeExitEnabled(isNativeExitEnabled)
                sharedPref.setIsNativeUseEnabled(isNativeUseEnabled)
                sharedPref.setIsBannerSideNavEnabled(isBannerSideNavEnabled)
                sharedPref.setIsPremiumHowToUseEnabled(isPremiumHowToUseEnabled)
                sharedPref.setIsPremiumMonthlyEnabled(isPremiumMonthlyEnabled)
                sharedPref.setIsScanFilesInterstitialEnabled(isScanFilesInterstitialEnabled)
                sharedPref.setIsDuplicateActivityInterstitialEnabled(isDuplicateActivityInterstitialEnabled)
                sharedPref.setIsScanAudiosActivityInterstitialEnabled(isScanAudiosActivityInterstitialEnabled)
                sharedPref.setIsScanImagesActivityInterstitialEnabled(isScanImagesActivityInterstitialEnabled)
                sharedPref.setIsScanVideosActivityInterstitialEnabled(isScanVideosActivityInterstitialEnabled)
                sharedPref.setIsDuplicateFragmentInterstitialEnabled(isDuplicateFragmentInterstitialEnabled)
                sharedPref.setIsHomeFragmentInterstitialEnabled(isHomeFragmentInterstitialEnabled)
                sharedPref.setIsSavedFragmentInterstitialEnabled(isSavedFragmentInterstitialEnabled)
            }
        }

        clickListener()

        try {
            Glide.with(this)
                .load(R.drawable.splash_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .error(R.drawable.ic_images)
                .into(binding.logoIV)
        } catch (e: Exception) {
            //do nothing
            Toast.makeText(this@SplashActivity, "Exception: " + e.message, Toast.LENGTH_SHORT)
                .show()
        }
        if (hasNetworkAvailable(this@SplashActivity)) {
            getAdSettings()
        } else {
            alertDialog("Internet issue\nPlease check internet")
        }
    }

    private fun clickListener() {
        binding.btn.setOnClickListener {
            if (manageAction()) {
                lastHitTime = System.currentTimeMillis()
                try {
                    subscriptionViewModel.isUserSubscribed.observe(this@SplashActivity) { sub ->
                        Log.d("TAG", "clickListener: " + sub)
                        AppPreferences.getInstance(this@SplashActivity).setAppPurchased(sub)
                        SubscriptionsConstants.isUserSubscribed = sub
                        if (!AppPreferences.getInstance(this@SplashActivity).isAppPurchased) {
                            if (hasNetworkAvailable(this@SplashActivity)) {
                                val adId =
                                    AdDatabaseUtil.getAdmobInterstitialAdId(this@SplashActivity)

                                Log.d("de_Ads", "Ad is on and ad id is $adId")
                                binding.adloadingscreen.isVisible = true
                                AppController.splshinterstialAd.loadInterStialAd(
                                    this@SplashActivity,
                                    adId
                                ) {
                                    AppController.splshinterstialAd.show_Interstial_Ad(this@SplashActivity) {
                                        binding.adloadingscreen.isVisible = false
                                        goNext()
                                    }

                                }
                            } else {
                                alertDialog("Internet issue\nPlease check internet")
                            }

                        } else {
                            goNext()
                        }
                    }
                } catch (ex: Exception) {
                    Log.d("TAG", "clickListener: Exception " + ex.printStackTrace())
                }

            }

        }
    }

    private fun getAdSettings() {
        AdDatabaseUtil.getAdSettings(object : ICallBackListener<Any?> {
            override fun onSuccess(t: Any?, headers: Headers?) {
                val adSettings = AppSharedPref.adSettings
                if (adSettings.addSettings?.AdmobInt == true) {
                }

            }

            override fun onFailure(t: ErrorDto) {
                AppSharedPref.isSimulatorEnabled = false
            }
        })
    }

    fun initAds() {
        val adId = AdDatabaseUtil.getAdmobInterstitialAdId(this@SplashActivity)
        val adRequest: AdRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this@SplashActivity,
            adId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    interstitialAdd = interstitialAd
                    Log.d("InterstitialAd", "onAdLoaded: ${interstitialAd.adUnitId}")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    alertDialog("onAdFailedToLoad : " + loadAdError.message)
                    Log.d("InterstitialAd", "onAdFailedToLoad: ${loadAdError.message}")
                }
            })
    }

    private fun goNext() {
        startActivity(Intent(this, SelectLanguagesActivity::class.java))
        finish()
    }

    fun showAdmobInterstitial(dataListener: DataListener) {
        if (interstitialAdd != null) {
            interstitialAdd!!.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError!!)
                        alertDialog(adError.message)
                        binding.adloadingscreen.isVisible = false
//                        dataListener.onRecieve(false)
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        //                    completeListener.onInterstitialDismissed(true);
                    }

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        interstitialAdd = null
                        dataListener.onRecieve(true)
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                    }
                }
            interstitialAdd!!.show(this@SplashActivity)
        } else {
            binding.adloadingscreen.isVisible = false
            alertDialog("interstitialAdd not initlized")
//            dataListener.onRecieve(false)
        }
    }

    fun alertDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Title")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, which ->
            // handle OK button click
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            // handle Cancel button click
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun hasNetworkAvailable(context: Context): Boolean {
        val service = Context.CONNECTIVITY_SERVICE
        val manager = context.getSystemService(service) as ConnectivityManager?
        val network = manager?.activeNetworkInfo
        return (network != null)
    }

    fun manageAction(): Boolean {
        return System.currentTimeMillis() - lastHitTime > 8000
    }
}