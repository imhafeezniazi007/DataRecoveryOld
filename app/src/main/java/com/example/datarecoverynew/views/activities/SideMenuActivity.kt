package com.example.datarecoverynew.views.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.NonNull
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.base.BaseActivity
import com.example.datarecoverynew.databinding.ActivitySideMenuBinding
import com.example.datarecoverynew.storage.entities.BPAds
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
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class SideMenuActivity : BaseActivity() {
    lateinit var binding: ActivitySideMenuBinding
    private val sharedPrefsHelper: SharedPrefsHelper by lazy { SharedPrefsHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySideMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (isPremiumEnabled()) binding.premiumLayout.visibility = View.VISIBLE

        clickListener()
        initViews()
        if (sharedPrefsHelper.getIsBannerSideNavEnabled()) {
            initBannerAd()
        } else {
            binding.adView.visibility = View.GONE
            binding.adLoadingTv.visibility = View.GONE
        }
    }

    private fun initViews() {
        binding.logo.setImageResource(R.drawable.ic_side_menu_delete_recovery)

        /*val theme = AppPreferences.getInstance(this@SideMenuActivity).getTheme
        if(theme == 2) {
            binding.logo.setImageResource(R.drawable.ic_side_menu_delete_theme_1)
        }else if(theme == 3) {
            binding.logo.setImageResource(R.drawable.ic_side_menu_delete_theme_2)
        }else if(theme == 4) {
            binding.logo.setImageResource(R.drawable.ic_side_menu_delete_theme_3)
        }else if(theme == 5) {
            binding.logo.setImageResource(R.drawable.ic_side_menu_delete_theme_4)
        }else if(theme == 6) {
            binding.logo.setImageResource(R.drawable.ic_side_menu_delete_theme_5)
        }else {
            binding.logo.setImageResource(R.drawable.ic_side_menu_delete_icon)
        }*/
    }

    private fun isPremiumEnabled(): Boolean {
        return sharedPrefsHelper.getIsPremiumEnabled()
    }

    private fun clickListener() {
        binding.backIV.setOnClickListener {
            finish()
        }
        binding.premiumLayout.setOnClickListener {
            startActivity(Intent(this@SideMenuActivity, PremiumActivity::class.java))
        }
        binding.language.setOnClickListener {
            startActivity(Intent(this@SideMenuActivity, SelectLanguagesActivity::class.java))
        }
        binding.privacy.setOnClickListener {

            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://sites.google.com/view/toolsmedicsproductivityappss/home")
            )
            startActivity(browserIntent)

        }
        binding.moreApps.setOnClickListener {
            val uri =
                Uri.parse("https://play.google.com/store/apps/developer?id=Smart+Tech+Tools+Inc")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
            startActivity(intent)

        }
        binding.rateUs.setOnClickListener {
            launchMarket()

        }

    }


    private fun initBannerAd() {
        if (AppPreferences.getInstance(this).isAppPurchased) {
            binding.adView.visibility = View.GONE
            binding.adLoadingTv.visibility = View.GONE
            binding.border1.visibility = View.GONE
        } else {
            loadAdmobBanner(this@SideMenuActivity, binding.adView)
        }
    }

    private fun launchMarket() {
        val uri = Uri.parse("market://details?id=$packageName")
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show()
        }
    }

    fun loadAdmobBanner(
        activity: Activity, bannerContainer: LinearLayout
    ) {
        bannerContainer.setGravity(Gravity.CENTER)
        val mAdmobBanner = AdView(activity)

        val adSize: AdSize = getAdSize(activity)
        mAdmobBanner.setAdSize(adSize)
        mAdmobBanner.adUnitId = AdDatabaseUtil.getAdmobBannerId(this@SideMenuActivity)


        val extras = Bundle()
        extras.putString("collapsible", "bottom")
        val adRequest1: AdRequest =
            AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()

        mAdmobBanner.loadAd(adRequest1)
        mAdmobBanner.adListener = object : AdListener() {
            override fun onAdLoaded() {

                super.onAdLoaded()
                bannerContainer.removeAllViews()
                bannerContainer.addView(mAdmobBanner)
                binding.adLoadingTv.visibility = View.GONE
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

}