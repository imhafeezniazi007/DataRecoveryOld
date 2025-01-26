package com.example.datarecoverynew.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.android.billingclient.api.ProductDetails
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.databinding.FragmentDuplicateBinding
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.datarecoverynew.utils.AppPreferences
import com.example.datarecoverynew.utils.AppSharedPref
import com.example.datarecoverynew.utils.Constant
import com.example.datarecoverynew.utils.SharedPrefsHelper
import com.example.datarecoverynew.viewmodels.AppOpenAdViewModel
import com.example.datarecoverynew.views.activities.MainActivity
import com.example.datarecoverynew.views.activities.PermissionScreenActivity
import com.example.datarecoverynew.views.activities.PremiumActivity
import com.example.recoverydata.interfaces.DataListener
import com.example.recoverydata.utils.collectLatestLifeCycleFlow
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.subscription.ads.billing.SubscriptionViewModel

class DuplicateFragment : Fragment() {
    lateinit var binding: FragmentDuplicateBinding
    var interstitialAdd: InterstitialAd? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var productDetail: ProductDetails? = null
    private lateinit var appOpenAdViewModel: AppOpenAdViewModel
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private val sharedPrefsHelper: SharedPrefsHelper by lazy { SharedPrefsHelper(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDuplicateBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appOpenAdViewModel = (requireActivity().application as AppController).appOpenAdViewModel


        if (isAdded) firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        firebaseAnalytics.logEvent("duplicate_fragment", null)

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
        if (isMonthlyPremiumEnabled()) {
            binding.tvPrice.text = "$price/Month"
            binding.tvMonth.text = "Monthly"
            binding.tv1.text = "1 Month"
        } else {
            binding.tvPrice.text = "$price/Year"
            binding.tvMonth.text = "Yearly"
            binding.tv1.text = "1 Year"
        }
    }

    private fun isMonthlyPremiumEnabled(): Boolean {
        return sharedPrefsHelper.getIsPremiumMonthlyEnabled()
    }

    private fun showNativeLargeAd() {
        val adId = AdDatabaseUtil.getAdmobNativeAdId(requireContext())
        if (adId.isNotEmpty()) {
            AppController.nativeAdRef.loadNativeAd(
                requireActivity(),
                binding.frameNative,
                false,
                adId
            ) {}
        }
    }

    private fun isNativeHomeEnabled(): Boolean {
        return sharedPrefsHelper.getNativeDuplicateFragmentEnabled()
    }

    private fun showNativeAd() {
        val adId = AdDatabaseUtil.getAdmobNativeAdId(requireContext())
        if (adId.isNotEmpty()) {
            AppController.nativeAdRef.loadNativeAd(
                requireActivity(),
                binding.frameNative,
                true,
                adId
            ) {}
        }
    }

    private fun showHighECPM(intent: String) {
        Log.d("inter_ecpm", "showHighECPM: ")
        val adId = sharedPrefsHelper.getInterstitialHighId()

        binding.adloadingscreen.isVisible = true
        AppController.splshinterstialAd.loadInterStialAd(
            requireContext(),
            adId
        ) { isLoaded ->
            if (isLoaded) {
                appOpenAdViewModel.updateAdStatus(true, "Duplicate Fragment")
                AppController.splshinterstialAd.show_Interstial_Ad(requireActivity()) {
                    binding.adloadingscreen.isVisible = false
                    appOpenAdViewModel.updateAdStatus(true, "Duplicate Fragment")
                    proceed(intent)
                }
            } else {
                showMediumECPM(intent)
            }
        }
    }

    private fun showMediumECPM(intent: String) {
        Log.d("inter_ecpm", "showMediumECPM: ")

        val adId = sharedPrefsHelper.getInterstitialMediumId()

        binding.adloadingscreen.isVisible = true
        AppController.splshinterstialAd.loadInterStialAd(
            requireContext(),
            adId
        ) { isLoaded ->
            if (isLoaded) {
                appOpenAdViewModel.updateAdStatus(true, "Duplicate Fragment")
                AppController.splshinterstialAd.show_Interstial_Ad(requireActivity()) {
                    binding.adloadingscreen.isVisible = false
                    appOpenAdViewModel.updateAdStatus(false, "Duplicate Fragment")
                    proceed(intent)
                }
            } else {
                showAutoECPM(intent)
            }
        }
    }

    private fun showAutoECPM(intent: String) {
        Log.d("inter_ecpm", "showAutoECPM: ")
        val adId = AdDatabaseUtil.getAdmobInterstitialAdId(requireContext())

        binding.adloadingscreen.isVisible = true
        AppController.splshinterstialAd.loadInterStialAd(
            requireContext(),
            adId
        ) {
            appOpenAdViewModel.updateAdStatus(true, "Duplicate Fragment")
            AppController.splshinterstialAd.show_Interstial_Ad(requireActivity()) {
                binding.adloadingscreen.isVisible = false
                appOpenAdViewModel.updateAdStatus(false, "Duplicate Fragment")
                proceed(intent)
            }
        }
    }


    private fun proceed(forActivity: String) {
        when (forActivity) {
            "DuplicateImages" -> {
                val intentAct = Intent(requireActivity(), PermissionScreenActivity::class.java)
                intentAct.putExtra("for", "DuplicateImages")
                startActivity(intentAct)
            }

            "DuplicateVideos" -> {
                val intentAct = Intent(requireActivity(), PermissionScreenActivity::class.java)
                intentAct.putExtra("for", "DuplicateVideos")
                startActivity(intentAct)
            }

            "DuplicateAudios" -> {
                val intentAct = Intent(requireActivity(), PermissionScreenActivity::class.java)
                intentAct.putExtra("for", "DuplicateAudios")
                startActivity(intentAct)
            }

            "DuplicateFiles" -> {
                val intentAct = Intent(requireActivity(), PermissionScreenActivity::class.java)
                intentAct.putExtra("for", "DuplicateFiles")
                startActivity(intentAct)
            }
        }
    }

    private fun isInterstitialEnabled(): Boolean {
        return sharedPrefsHelper.getIsDuplicateFragmentInterstitialEnabled()
    }

    private fun clickListener() {
        binding.files.setOnClickListener {
            firebaseAnalytics.logEvent("duplicate_files_click", null)

            if (Constant.userClickDuplicateItemFirstTiem && !AppPreferences.getInstance(
                    requireContext()
                ).isAppPurchased && isInterstitialEnabled()
            ) {
                Constant.userClickDuplicateItemFirstTiem = false
                showHighECPM("DuplicateFiles")
            } else {
                proceed("DuplicateFiles")
                /*if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                    val adSettings = AppSharedPref.adSettings

                    if (adSettings.addSettings?.AdmobInt != true) {
                        if (IronSource.isInterstitialReady()) {
                            IronSource.showInterstitial("19993437")
                            AppController.setOnAddClickListener {
                                startActivity(intent)

                            }
                        } else {
                            startActivity(intent)

                        }
                    } else {
                        showAdmobInterstitial(object : DataListener {
                            override fun onRecieve(any: Any) {
                                if (any as Boolean) {
                                    startActivity(intent)

                                } else {
                                    startActivity(intent)
                                }
                            }
                        })
                    }
                }else{
                    startActivity(intent)
                }*/
            }
        }
        binding.images.setOnClickListener {
            firebaseAnalytics.logEvent("duplicate_images_click", null)

            if (Constant.userClickDuplicateItemFirstTiem && !AppPreferences.getInstance(
                    requireContext()
                ).isAppPurchased
            ) {
                Constant.userClickDuplicateItemFirstTiem = false
                showHighECPM("DuplicateImages")
                /*showAdmobInterstitial(object : DataListener {
                    override fun onRecieve(any: Any) {
                        if (any as Boolean) {
                            startActivity(intent)

                        } else {
                            startActivity(intent)
                        }
                    }
                })*/

            } else {
                proceed("DuplicateImages")
                /*if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                    val adSettings = AppSharedPref.adSettings

                    if (adSettings.addSettings?.AdmobInt != true) {
                        if (IronSource.isInterstitialReady()) {
                            IronSource.showInterstitial("19993437")
                            AppController.setOnAddClickListener {
                                startActivity(intent)

                            }
                        } else {
                            startActivity(intent)

                        }
                    } else {
                        showAdmobInterstitial(object : DataListener {
                            override fun onRecieve(any: Any) {
                                if (any as Boolean) {
                                    startActivity(intent)

                                } else {
                                    startActivity(intent)
                                }
                            }
                        })
                    }
                }else{
                    startActivity(intent)
                }*/
            }
        }
        binding.audios.setOnClickListener {
            firebaseAnalytics.logEvent("duplicate_audios_click", null)

            if (Constant.userClickDuplicateItemFirstTiem && !AppPreferences.getInstance(
                    requireContext()
                ).isAppPurchased
            ) {
                Constant.userClickDuplicateItemFirstTiem = false
                showHighECPM("DuplicateAudios")

            } else {
                proceed("DuplicateAudios")
                /* if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                     val adSettings = AppSharedPref.adSettings

                     if (adSettings.addSettings?.AdmobInt != true) {
                         if (IronSource.isInterstitialReady()) {
                             IronSource.showInterstitial("19993437")
                             AppController.setOnAddClickListener {
                                 startActivity(intent)

                             }
                         } else {
                             startActivity(intent)

                         }
                     } else {
                         showAdmobInterstitial(object : DataListener {
                             override fun onRecieve(any: Any) {
                                 if (any as Boolean) {
                                     startActivity(intent)

                                 } else {
                                     startActivity(intent)
                                 }
                             }
                         })
                     }
                 }else{
                     startActivity(intent)
                 }*/
            }
        }
        binding.videos.setOnClickListener {
            firebaseAnalytics.logEvent("duplicate_videos_click", null)

            if (Constant.userClickDuplicateItemFirstTiem && !AppPreferences.getInstance(
                    requireContext()
                ).isAppPurchased
            ) {
                Constant.userClickDuplicateItemFirstTiem = false
                showHighECPM("DuplicateVideos")

            } else {
                proceed("DuplicateVideos")
                /*if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                    val adSettings = AppSharedPref.adSettings

                    if (adSettings.addSettings?.AdmobInt != true) {
                        if (IronSource.isInterstitialReady()) {
                            IronSource.showInterstitial("19993437")
                            AppController.setOnAddClickListener {
                                startActivity(intent)

                            }
                        } else {
                            startActivity(intent)

                        }
                    } else {
                        showAdmobInterstitial(object : DataListener {
                            override fun onRecieve(any: Any) {
                                if (any as Boolean) {
                                    startActivity(intent)

                                } else {
                                    startActivity(intent)
                                }
                            }
                        })
                    }
                }else{
                    startActivity(intent)
                }*/
            }
        }
        binding.prolayout.setOnClickListener {
            firebaseAnalytics.logEvent("duplicate_premium_click", null)
            startActivity(Intent(requireActivity(), PremiumActivity::class.java))
        }
    }


    private fun isPremiumEnabled(): Boolean {
        return sharedPrefsHelper.getIsPremiumEnabled()
    }

    fun initAds() {
        val adId = AdDatabaseUtil.getAdmobInterstitialAdId(requireContext())

        val adRequest: AdRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            requireContext(),
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
            interstitialAdd!!.fullScreenContentCallback =
                object : FullScreenContentCallback() {
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