package com.example.datarecoverynew.views.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.android.billingclient.api.ProductDetails
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.databinding.FragmentDuplicateBinding
import com.example.datarecoverynew.databinding.FragmentSavedBinding
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.datarecoverynew.utils.AppPreferences
import com.example.datarecoverynew.utils.AppSharedPref
import com.example.datarecoverynew.utils.Constant
import com.example.datarecoverynew.utils.SharedPrefsHelper
import com.example.datarecoverynew.viewmodels.AppOpenAdViewModel
import com.example.datarecoverynew.views.activities.MainActivity
import com.example.datarecoverynew.views.activities.PermissionScreenActivity
import com.example.datarecoverynew.views.activities.PremiumActivity
import com.example.datarecoverynew.views.activities.SavedAudiosActivity
import com.example.datarecoverynew.views.activities.SavedFilesActivity
import com.example.datarecoverynew.views.activities.SavedImagesActivity
import com.example.datarecoverynew.views.activities.SavedVediosActivity
import com.example.datarecoverynew.views.activities.ScanAudiosActivity
import com.example.datarecoverynew.views.activities.ScanFilesActivity
import com.example.datarecoverynew.views.activities.ScanImagesActivity
import com.example.datarecoverynew.views.activities.ScanVideosActivity
import com.example.recoverydata.interfaces.DataListener
import com.example.recoverydata.utils.collectLatestLifeCycleFlow
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.subscription.ads.billing.SubscriptionViewModel


class SavedFragment : Fragment() {
    lateinit var binding: FragmentSavedBinding
    var interstitialAdd: InterstitialAd? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var productDetail: ProductDetails? = null
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private lateinit var appOpenAdViewModel: AppOpenAdViewModel
    private val sharedPrefsHelper: SharedPrefsHelper by lazy { SharedPrefsHelper(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSavedBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appOpenAdViewModel = (requireActivity().application as AppController).appOpenAdViewModel


        if (isAdded) firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        firebaseAnalytics.logEvent("saved_fragment", null)

        if (isPremiumEnabled()) {
            binding.prolayout.visibility = View.VISIBLE
        }


        if (isNativeHomeEnabled()) {
            if (isPremiumEnabled()) {
                showNativeAd()
            } else {
                showNativeLargeAd()
            }
        } else {
            binding.frameNative.visibility = View.GONE
        }

        clickListener()
        observerViewModel()
    }


    private fun showNativeLargeAd() {
        val adId = AdDatabaseUtil.getAdmobNativeAdId(requireContext())
        if (adId.isNotEmpty()) {
            AppController.nativeAdRef.loadNativeAd(
                requireActivity(), binding.frameNative, false, adId
            ) {}
        }
    }

    private fun isNativeHomeEnabled(): Boolean {
        return sharedPrefsHelper.getNativeSavedFragmentEnabled()
    }

    private fun showNativeAd() {
        val adId = AdDatabaseUtil.getAdmobNativeAdId(requireContext())
        if (adId.isNotEmpty()) {
            AppController.nativeAdRef.loadNativeAd(
                requireActivity(), binding.frameNative, true, adId
            ) {}
        }
    }


    private fun observerViewModel() {
        (activity as MainActivity).collectLatestLifeCycleFlow(subscriptionViewModel.productsForSaleFlows) {
            Log.d("TAG", "observerViewModel: productsForSaleFlows $it")
            setUpSubscriptionPrice(it)
        }
    }

    private fun setUpSubscriptionPrice(it: List<ProductDetails>) {
        var price = ""
        it.firstOrNull()?.let { item ->
            productDetail = item
            item.subscriptionOfferDetails?.get(0).let { sod ->
                price = sod?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice ?: ""
            }
        }
        binding.tvPrice.text = "$price/Month"

    }

    private fun showHighECPM(intent: Activity) {
        Log.d("inter_ecpm", "showHighECPM: ")
        val adSettings = AppSharedPref.adSettings
        val adId = sharedPrefsHelper.getInterstitialHighId()

        if (adSettings.addSettings?.AdmobInt != true) {

        } else {
            binding.adloadingscreen.isVisible = true
            AppController.splshinterstialAd.loadInterStialAd(
                requireContext(), adId
            ) { isLoaded ->
                if (isLoaded) {
                    appOpenAdViewModel.updateAdStatus(true, "Saved Fragment")
                    AppController.splshinterstialAd.show_Interstial_Ad(requireActivity()) {
                        binding.adloadingscreen.isVisible = false
                        appOpenAdViewModel.updateAdStatus(false, "Saved Fragment")
                        proceed(intent)
                    }
                } else {
                    showMediumECPM(intent)
                }
            }
        }
    }

    private fun showMediumECPM(intent: Activity) {
        Log.d("inter_ecpm", "showMediumECPM: ")
        val adSettings = AppSharedPref.adSettings

        val adId = sharedPrefsHelper.getInterstitialMediumId()

        if (adSettings.addSettings?.AdmobInt != true) {

        } else {
            AppController.splshinterstialAd.loadInterStialAd(
                requireContext(), adId
            ) { isLoaded ->
                if (isLoaded) {
                    appOpenAdViewModel.updateAdStatus(true, "Saved Fragment")
                    AppController.splshinterstialAd.show_Interstial_Ad(requireActivity()) {
                        binding.adloadingscreen.isVisible = false
                        appOpenAdViewModel.updateAdStatus(false, "Saved Fragment")
                        proceed(intent)
                    }
                } else {
                    showAutoECPM(intent)
                }
            }
        }
    }

    private fun showAutoECPM(intent: Activity) {
        val adSettings = AppSharedPref.adSettings
        val adId = AdDatabaseUtil.getAdmobInterstitialAdId(requireContext())

        if (adSettings.addSettings?.AdmobInt != true) {

        } else {
            AppController.splshinterstialAd.loadInterStialAd(
                requireContext(), adId
            ) {
                appOpenAdViewModel.updateAdStatus(true, "Saved Fragment")
                AppController.splshinterstialAd.show_Interstial_Ad(requireActivity()) {
                    binding.adloadingscreen.isVisible = false
                    appOpenAdViewModel.updateAdStatus(false, "Saved Fragment")
                    proceed(intent)
                }
            }
        }
    }

    private fun proceed(intent: Activity) {
        when (intent) {
            is SavedImagesActivity -> {
                val intentAct = Intent(requireActivity(), PermissionScreenActivity::class.java)
                intentAct.putExtra("for", "SavedImages")
                startActivity(intentAct)
            }

            is SavedVediosActivity -> {
                val intentAct = Intent(requireActivity(), PermissionScreenActivity::class.java)
                intentAct.putExtra("for", "SavedVideos")
                startActivity(intentAct)
            }

            is SavedAudiosActivity -> {
                val intentAct = Intent(requireActivity(), PermissionScreenActivity::class.java)
                intentAct.putExtra("for", "SavedAudios")
                startActivity(intentAct)
            }

            is SavedFilesActivity -> {
                val intentAct = Intent(requireActivity(), PermissionScreenActivity::class.java)
                intentAct.putExtra("for", "SavedFiles")
                startActivity(intentAct)
            }
        }
    }

    private fun clickListener() {
        binding.images.setOnClickListener {
            firebaseAnalytics.logEvent("saved_images_click", null)
            if (Constant.userClickSavedFirstTiem && !AppPreferences.getInstance(requireContext()).isAppPurchased) {
                Constant.userClickSavedFirstTiem = false
                showHighECPM(SavedImagesActivity())

            } else {
                proceed(SavedImagesActivity())/*if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                    val adSettings = AppSharedPref.adSettings

                    if (adSettings.addSettings?.AdmobInt != true) {
                        if (IronSource.isInterstitialReady()) {
                            IronSource.showInterstitial("19993437")
                            AppController.setOnAddClickListener {
                                startActivity(Intent(requireActivity(),SavedImagesActivity::class.java))

                            }
                        } else {
                            startActivity(Intent(requireActivity(),SavedImagesActivity::class.java))

                        }
                    } else {
                        showAdmobInterstitial(object : DataListener {
                            override fun onRecieve(any: Any) {
                                if (any as Boolean) {
                                    startActivity(Intent(requireActivity(),SavedImagesActivity::class.java))

                                } else {
                                    startActivity(Intent(requireActivity(),SavedImagesActivity::class.java))
                                }
                            }
                        })
                    }
                }else{
                    startActivity(Intent(requireActivity(),SavedImagesActivity::class.java))
                }*/
            }
        }
        binding.audios.setOnClickListener {
            firebaseAnalytics.logEvent("saved_audios_click", null)
            if (Constant.userClickSavedFirstTiem && !AppPreferences.getInstance(requireContext()).isAppPurchased) {
                Constant.userClickSavedFirstTiem = false
                showHighECPM(SavedAudiosActivity())
            } else {
                proceed(SavedAudiosActivity())/*if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                    val adSettings = AppSharedPref.adSettings

                    if (adSettings.addSettings?.AdmobInt != true) {
                        if (IronSource.isInterstitialReady()) {
                            IronSource.showInterstitial("19993437")
                            AppController.setOnAddClickListener {
                                startActivity(Intent(requireActivity(),SavedAudiosActivity::class.java))

                            }
                        } else {
                            startActivity(Intent(requireActivity(),SavedAudiosActivity::class.java))

                        }
                    } else {
                        showAdmobInterstitial(object : DataListener {
                            override fun onRecieve(any: Any) {
                                if (any as Boolean) {
                                    startActivity(Intent(requireActivity(),SavedAudiosActivity::class.java))

                                } else {
                                    startActivity(Intent(requireActivity(),SavedAudiosActivity::class.java))
                                }
                            }
                        })
                    }
                }else{
                    startActivity(Intent(requireActivity(),SavedAudiosActivity::class.java))
                }*/
            }
        }
        binding.videos.setOnClickListener {
            firebaseAnalytics.logEvent("saved_videos_click", null)
            if (Constant.userClickSavedFirstTiem && !AppPreferences.getInstance(requireContext()).isAppPurchased) {
                Constant.userClickSavedFirstTiem = false
                showHighECPM(SavedVediosActivity())
            } else {
                proceed(SavedVediosActivity())/* if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                     val adSettings = AppSharedPref.adSettings

                     if (adSettings.addSettings?.AdmobInt != true) {
                         if (IronSource.isInterstitialReady()) {
                             IronSource.showInterstitial("19993437")
                             AppController.setOnAddClickListener {
                                 startActivity(Intent(requireActivity(),SavedVediosActivity::class.java))

                             }
                         } else {
                             startActivity(Intent(requireActivity(),SavedVediosActivity::class.java))

                         }
                     } else {
                         showAdmobInterstitial(object : DataListener {
                             override fun onRecieve(any: Any) {
                                 if (any as Boolean) {
                                     startActivity(Intent(requireActivity(),SavedVediosActivity::class.java))

                                 } else {
                                     startActivity(Intent(requireActivity(),SavedVediosActivity::class.java))
                                 }
                             }
                         })
                     }
                 }else{
                     startActivity(Intent(requireActivity(),SavedVediosActivity::class.java))
                 }*/
            }
        }
        binding.files.setOnClickListener {
            firebaseAnalytics.logEvent("saved_files_click", null)
            if (Constant.userClickSavedFirstTiem && !AppPreferences.getInstance(requireContext()).isAppPurchased) {
                Constant.userClickSavedFirstTiem = false
                showHighECPM(SavedFilesActivity())
            } else {
                proceed(SavedFilesActivity())/*if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                    val adSettings = AppSharedPref.adSettings

                    if (adSettings.addSettings?.AdmobInt != true) {
                        if (IronSource.isInterstitialReady()) {
                            IronSource.showInterstitial("19993437")
                            AppController.setOnAddClickListener {
                                startActivity(Intent(requireActivity(),SavedFilesActivity::class.java))

                            }
                        } else {
                            startActivity(Intent(requireActivity(),SavedFilesActivity::class.java))

                        }
                    } else {
                        showAdmobInterstitial(object : DataListener {
                            override fun onRecieve(any: Any) {
                                if (any as Boolean) {
                                    startActivity(Intent(requireActivity(),SavedFilesActivity::class.java))

                                } else {
                                    startActivity(Intent(requireActivity(),SavedFilesActivity::class.java))
                                }
                            }
                        })
                    }
                }else{
                    startActivity(Intent(requireActivity(),SavedFilesActivity::class.java))
                }*/
            }
        }
        binding.prolayout.setOnClickListener {
            firebaseAnalytics.logEvent("saved_premium_click", null)
            startActivity(Intent(requireActivity(), PremiumActivity::class.java))
        }
    }

    private fun isPremiumEnabled(): Boolean {
        return sharedPrefsHelper.getIsPremiumEnabled()
    }

    fun initAds() {
        val adId = AdDatabaseUtil.getAdmobInterstitialAdId(requireContext())

        val adRequest: AdRequest = AdRequest.Builder().build()
        InterstitialAd.load(requireContext(),
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

                    alertDialog(loadAdError.message)
                    Log.d("InterstitialAd", "onAdFailedToLoad: ${loadAdError.message}")
                }
            })
    }

    fun showAdmobInterstitial(dataListener: DataListener) {
        if (interstitialAdd != null) {
            interstitialAdd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    super.onAdFailedToShowFullScreenContent(adError!!)
                    alertDialog(adError.message)
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
            interstitialAdd!!.show(requireActivity())
        } else {
            alertDialog("interstitialAdd not initlized")
//            dataListener.onRecieve(false)
        }
    }

    fun alertDialog(message: String) {
        val builder = AlertDialog.Builder(requireContext())
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
}