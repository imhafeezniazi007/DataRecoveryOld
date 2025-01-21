package com.example.datarecoverynew.views.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.base.BaseActivity
import com.example.datarecoverynew.databinding.ActivityMainBinding
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

class MainActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var adContainerView: LinearLayout
    private val sharedPrefsHelper: SharedPrefsHelper by lazy { SharedPrefsHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setNavigationController()
        clickListener()

        if (isBannerHomeEnabled()) {
            initBannerAd()
        } else {
            binding.adView.visibility = View.GONE
            binding.adLoadingTv.visibility = View.GONE
            binding.border1.visibility = View.GONE
        }

    }

    private fun initBannerAd() {
        if (AppPreferences.getInstance(this).isAppPurchased) {
            binding.adView.visibility = View.GONE
            binding.adLoadingTv.visibility = View.GONE
            binding.border1.visibility = View.GONE
        } else {
            loadAdmobBanner(this@MainActivity, binding.adView)
        }
    }

    private fun clickListener() {
        binding.drawerIV.setOnClickListener {
            startActivity(Intent(this@MainActivity, SideMenuActivity::class.java))
        }
    }


    private fun setNavigationController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navController) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNavView, navController)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        exitDialog()
    }

    fun exitDialog() {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.exit_dialog, null)
        val yesBtn = view.findViewById<AppCompatButton>(R.id.recoverBtn)
        val noBtn = view.findViewById<AppCompatButton>(R.id.deleteBtn)
        val logoIV = view.findViewById<ImageView>(R.id.logoIV)
        val nativeFrame = view.findViewById<FrameLayout>(R.id.frameNativeExit)

        val theme = AppPreferences.getInstance(this@MainActivity).getTheme
        if (theme == 1) {
            logoIV.setImageResource(R.drawable.exit_icon_dark)
        } else if (theme == 2) {
            logoIV.setImageResource(R.drawable.exit_icon_theme_1)
        } else if (theme == 3) {
            logoIV.setImageResource(R.drawable.exit_icon_theme_2)
        } else if (theme == 4) {
            logoIV.setImageResource(R.drawable.exit_icon_theme_3)
        } else if (theme == 5) {
            logoIV.setImageResource(R.drawable.exit_icon_theme_4)
        } else if (theme == 6) {
            logoIV.setImageResource(R.drawable.exit_icon_theme_5)
        } else {
            logoIV.setImageResource(R.drawable.exit_icon_theme_5)
        }

        builder.setView(view)
        builder.setCancelable(false)
        imageDialog = builder.create()
        yesBtn.setOnClickListener {
            imageDialog.dismiss()
            finishAffinity()
        }
        noBtn.setOnClickListener {
            imageDialog.dismiss()
        }

        if (imageDialog.window != null) imageDialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        imageDialog.setCancelable(false)
        imageDialog.show()


        if (isNativeHomeEnabled()) {
            showNativeAd(nativeFrame)
        } else {
            nativeFrame.visibility = View.GONE
        }
    }

    private fun isNativeHomeEnabled(): Boolean {
        return sharedPrefsHelper.getIsNativeExitEnabled()
    }

    private fun showNativeAd(nativeFrame: FrameLayout) {
        val adId = AdDatabaseUtil.getAdmobNativeAdId(this)
        if (adId.isNotEmpty()) {
            AppController.nativeAdRef.loadNativeAd(
                this, nativeFrame, false, adId
            ) {}
        }
    }

    private fun isBannerHomeEnabled(): Boolean {
        return sharedPrefsHelper.getIsBannerHomeEnabled()
    }


    fun loadAdmobBanner(
        activity: Activity, bannerContainer: LinearLayout
    ) {
        bannerContainer.setGravity(Gravity.CENTER)
        val mAdmobBanner = AdView(activity)

        val adSize: AdSize = getAdSize(activity)
        mAdmobBanner.setAdSize(adSize)
        mAdmobBanner.adUnitId = AdDatabaseUtil.getAdmobBannerId(this@MainActivity)


        val extras = Bundle()
        extras.putString("collapsible", "top")
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