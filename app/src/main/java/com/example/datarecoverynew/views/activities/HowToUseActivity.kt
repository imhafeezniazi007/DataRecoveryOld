package com.example.datarecoverynew.views.activities

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.datarecoverynew.R
import com.example.datarecoverynew.base.BaseActivity
import com.example.datarecoverynew.databinding.ActivityHowToUseBinding
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.datarecoverynew.utils.AppPreferences
import com.example.datarecoverynew.utils.AppSharedPref
import com.example.datarecoverynew.utils.SharedPrefsHelper
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class HowToUseActivity : BaseActivity() {
    private val binding by lazy { ActivityHowToUseBinding.inflate(layoutInflater) }
    private val sharedPrefsHelper by lazy { SharedPrefsHelper(this) }
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment


    private val listener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.fragmentOne -> {
                    showBottomSheet()
                }

                R.id.fragmentTwo -> {
                    showBottomSheet()
                }

                R.id.adFragment -> {
                    hideBottomSheet()
                }

                R.id.fragmentFour -> {
                    showBottomSheet()
                }

                else -> {
                    showBottomSheet()
                }
            }
        }

    private fun showBottomSheet() {
        if (isAdsEnabled()) {
            binding.constraintLayout2.visibility = View.VISIBLE
        }
    }

    private fun hideBottomSheet() {
        binding.constraintLayout2.visibility = View.GONE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navControllerUse) as NavHostFragment
        navController = navHostFragment.navController

        if (isAdsEnabled()) {
            initBannerAd()
        } else {
            Log.d("TAG_KKKKK", "initBannerAdGOME: ")
            binding.constraintLayout2.visibility = View.GONE
        }

    }

    private fun initBannerAd() {
        if (AppPreferences.getInstance(this).isAppPurchased) {
            binding.constraintLayout2.visibility = View.GONE
        } else {
            loadAdmobBanner(this@HowToUseActivity, binding.adView)
        }
    }

    fun loadAdmobBanner(
        activity: Activity, bannerContainer: LinearLayout
    ) {
        bannerContainer.setGravity(Gravity.CENTER)
        val mAdmobBanner = AdView(activity)

        val adSize: AdSize = getAdSize(activity)
        mAdmobBanner.setAdSize(adSize)
        mAdmobBanner.adUnitId = AdDatabaseUtil.getAdmobBannerId(this@HowToUseActivity)

        val adRequest1: AdRequest =
            AdRequest.Builder().build()

        mAdmobBanner.loadAd(adRequest1)
        mAdmobBanner.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                binding.adLoadingTv.visibility = View.GONE
                bannerContainer.removeAllViews()
                bannerContainer.addView(mAdmobBanner)
            }

            override fun onAdClosed() {
                super.onAdClosed()
            }

            override fun onAdFailedToLoad(@NonNull loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.d("MyLogs", "onAdFailedToLoad: ${loadAdError.cause}")
                binding.adView.visibility = View.INVISIBLE
            }

            override fun onAdOpened() {
                super.onAdOpened()
            }

            override fun onAdClicked() {
                super.onAdClicked()
            }

            override fun onAdImpression() {
                super.onAdImpression()
            }
        }
    }

    private fun getAdSize(activity: Activity): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels: Float = outMetrics.widthPixels.toFloat()
        val density: Float = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    private fun isAdsEnabled(): Boolean {
        return sharedPrefsHelper.getIsBannerUseEnabled()
    }

    override fun onStart() {
        super.onStart()
        navController.addOnDestinationChangedListener(listener)
    }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(listener)
    }

    override fun onPause() {
        navController.removeOnDestinationChangedListener(listener)
        super.onPause()
    }
}