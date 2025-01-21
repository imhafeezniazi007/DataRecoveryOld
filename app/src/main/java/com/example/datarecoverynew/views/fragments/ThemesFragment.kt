package com.example.datarecoverynew.views.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.datarecoverynew.R
import com.example.datarecoverynew.databinding.FragmentThemesBinding
import com.example.datarecoverynew.utils.AppPreferences
import com.example.datarecoverynew.utils.SharedPrefsHelper
import com.example.datarecoverynew.utils.ThemesUtils
import com.example.datarecoverynew.views.activities.MainActivity
import com.example.datarecoverynew.views.activities.PremiumActivity
import com.example.datarecoverynew.views.adapters.ThemesAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings


class ThemesFragment : Fragment() {
    lateinit var binding: FragmentThemesBinding
    lateinit var viewPager: ViewPager2
    var mPos = 0
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val sharedPreferences: SharedPrefsHelper by lazy { SharedPrefsHelper(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentThemesBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        if (!isPremiumEnabled()) {
            if (mPos == 0 || mPos == 1) {
                binding.btn.text = "Unlock"
            } else {
                binding.btn.text = "Watch Ad to Unlock"
            }
        }

        firebaseAnalytics.logEvent("themes_fragment", null)

        viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val adapter = ThemesAdapter(requireContext())

        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1

        val pageMargin = resources.getDimensionPixelOffset(R.dimen.pageMargin).toFloat()
        val pageOffset = resources.getDimensionPixelOffset(R.dimen.offset).toFloat()

        viewPager.setPageTransformer { page, position ->
            val myOffset: Float = position * -(2 * pageOffset + pageMargin)
            if (position < -1) {
                page.setTranslationX(-myOffset)
            } else if (position <= 1) {
                val scaleFactor =
                    Math.max(0.7f, 1 - Math.abs(position - 0.14285715f))
                page.setTranslationX(myOffset)
                page.setScaleY(scaleFactor)
                page.setAlpha(scaleFactor)
            } else {
                page.setAlpha(0F)
                page.setTranslationX(myOffset)
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("TAG", "onPageSelected: $position")
                mPos = position
                if (!isPremiumEnabled()) {
                    if (mPos == 0 || mPos == 1) {
                        binding.btn.text = "Unlock"
                    } else {
                        binding.btn.text = "Watch Ad to Unlock"
                    }
                }
                /* Log.d("TAG", "onPageSelected: $position")
                    Log.d("TAG", "onPageSelected: ${Constant.unlockAllTheme}")
                    mPos = position
                    if(!AppPreferences.getInstance(requireContext()).isAppPurchased){
                        if(!Constant.unlockAllTheme) {
                            if (position == 0 || position == 1) {
                                binding.btn.visibility = View.VISIBLE
                                binding.btnUnlock.visibility = View.GONE
                            } else {
                                binding.btn.visibility = View.INVISIBLE
                                binding.btnUnlock.visibility = View.VISIBLE
                            }
                            if (Constant.watchedAdTheme == position) {
                                binding.btn.visibility = View.VISIBLE
                                binding.btnUnlock.visibility = View.GONE
                            }
                        }else{
                            binding.btn.visibility = View.VISIBLE
                            binding.btnUnlock.visibility = View.GONE
                        }
                    }else{
                        binding.btn.visibility = View.VISIBLE
                        binding.btnUnlock.visibility = View.GONE

                    }*/

            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

            }
        })

        binding.btn.setOnClickListener {
            firebaseAnalytics.logEvent("themes_unlock_click", null)

            if (!isPremiumEnabled()) {
                if (mPos == 0 || mPos == 1) {
                    AppPreferences.getInstance(requireContext()).setTheme(mPos)
                    ThemesUtils.changeToTheme(requireActivity() as MainActivity, mPos);
                } else {
                    binding.adloadingscreen.isVisible = true
                    loadRewardedAd(requireContext())
                }
            } else {
                if (!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                    if (mPos == 0 || mPos == 1) {
                        AppPreferences.getInstance(requireContext()).setTheme(mPos)
                        ThemesUtils.changeToTheme(requireActivity() as MainActivity, mPos);
                    } else {
                        startActivity(Intent(requireActivity(), PremiumActivity::class.java))
                    }
                } else {
                    AppPreferences.getInstance(requireContext()).setTheme(mPos)
                    ThemesUtils.changeToTheme(requireActivity() as MainActivity, mPos);
                }
            }
        }
    }

    private var rewardedAd: RewardedAd? = null
    private var rewardedAdId: String? = null


    fun loadRewardedAd(context: Context) {
        val adId = sharedPreferences.getAdId()
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            adId, // Replace with your Ad Unit ID
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    binding.adloadingscreen.isVisible = false
                    Log.d("AdMob", "Ad failed to load: ${adError.message}")
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d("AdMob", "Ad loaded successfully")

                    binding.adloadingscreen.isVisible = false
                    showRewardedAd(requireActivity())
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity) {
        if (rewardedAd != null) {
            rewardedAd?.show(activity) { rewardItem ->
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d("AdMob", "User earned the reward: $rewardAmount $rewardType")
                // Grant the reward to the user here
                AppPreferences.getInstance(requireContext()).setTheme(mPos)
                ThemesUtils.changeToTheme(requireActivity() as MainActivity, mPos)
            }
        } else {
            Log.d("AdMob", "The rewarded ad wasn't ready yet.")
        }

        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d("AdMob", "Ad was dismissed.")
                rewardedAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d("AdMob", "Ad failed to show: ${adError.message}")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d("AdMob", "Ad showed fullscreen content.")
            }
        }

    }

    private fun isPremiumEnabled(): Boolean {
        return sharedPreferences.getIsPremiumEnabled()
    }

}