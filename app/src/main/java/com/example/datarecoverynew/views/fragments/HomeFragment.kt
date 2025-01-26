package com.example.datarecoverynew.views.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.ProductDetails
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.databinding.FragmentDuplicateBinding
import com.example.datarecoverynew.databinding.FragmentHomeBinding
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.datarecoverynew.utils.AppPreferences
import com.example.datarecoverynew.utils.AppSharedPref
import com.example.datarecoverynew.utils.Constant
import com.example.datarecoverynew.utils.SharedPref
import com.example.datarecoverynew.utils.SharedPrefsHelper
import com.example.datarecoverynew.viewmodels.AppOpenAdViewModel
import com.example.datarecoverynew.views.activities.MainActivity
import com.example.datarecoverynew.views.activities.PermissionScreenActivity
import com.example.datarecoverynew.views.activities.PremiumActivity
import com.example.datarecoverynew.views.activities.SavedImagesActivity
import com.example.datarecoverynew.views.activities.ScanAudiosActivity
import com.example.datarecoverynew.views.activities.ScanFilesActivity
import com.example.datarecoverynew.views.activities.ScanImagesActivity
import com.example.datarecoverynew.views.activities.ScanVideosActivity
import com.example.recoverydata.interfaces.DataListener
import com.example.recoverydata.utils.collectLatestLifeCycleFlow
import com.google.android.ads.mediationtestsuite.MediationTestSuite
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.subscription.ads.billing.SubscriptionViewModel
import com.subscription.ads.billing.SubscriptionsConstants


class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
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
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appOpenAdViewModel = (requireActivity().application as AppController).appOpenAdViewModel

        if (isAdded) firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        firebaseAnalytics.logEvent("home_fragment", null)

        if (isPremiumEnabled()) {
            binding.prolayout.visibility = View.VISIBLE
        }
        clickListener()

        observerViewModel()

        if (isNativeHomeEnabled()) {
            if (isPremiumEnabled()) {
                showNativeAd()
            } else {
                Log.d("de_atag", "onViewCreated: correct")
                showNativeLargeAd()
            }
        } else {
            binding.frameNative.visibility = View.GONE
        }

//        MobileAds.openAdInspector(requireContext()) { error ->
//            Log.d("de_tag_main", "onCreate: ${error?.message}")
//        }


//        MediationTestSuite.launch(requireContext())
    }

    private fun showNativeLargeAd() {
        val adId = AdDatabaseUtil.getAdmobNativeAdId(requireContext())
        if (adId.isNotEmpty()) {
            AppController.nativeAdRef.loadNativeAd(
                requireActivity(),
                binding.frameNative,
                false,
                adId
            ) { it ->
                Log.d("de_atag", it.toString())
            }
        }
    }

    private fun isNativeHomeEnabled(): Boolean {
        return sharedPrefsHelper.getIsNativeHomeEnabled()
    }

    private fun showNativeAd() {
        val adId = AdDatabaseUtil.getAdmobNativeAdId(requireContext())
        if (adId.isNotEmpty()) {
            AppController.nativeAdRef.loadNativeAd(
                requireActivity(),
                binding.frameNative,
                true,
                adId
            ) {

            }
        }
    }

    private fun observerViewModel() {
        (activity as MainActivity).collectLatestLifeCycleFlow(subscriptionViewModel.productsForSaleFlows) {
            Log.d("TAG", "observerViewModel: productsForSaleFlows $it")
            setUpSubscriptionPrice(it)
        }
    }

    private fun showHighECPM(intent: Activity) {
        Log.d("inter_ecpm", "showHighECPM: ")
        val adId = sharedPrefsHelper.getInterstitialHighId()

        binding.adloadingscreen.isVisible = true
        AppController.splshinterstialAd.loadInterStialAd(
            requireContext(),
            adId
        ) { isLoaded ->
            if (isLoaded) {
                appOpenAdViewModel.updateAdStatus(true, "Home Fragment")
                AppController.splshinterstialAd.show_Interstial_Ad(requireActivity()) {
                    binding.adloadingscreen.isVisible = false
                    appOpenAdViewModel.updateAdStatus(false, "Home Fragment")
                    proceed(intent)
                }
            } else {
                showMediumECPM(intent)
            }
        }
    }

    private fun showMediumECPM(intent: Activity) {
        Log.d("inter_ecpm", "showMediumECPM: ")

        val adId = sharedPrefsHelper.getInterstitialMediumId()

        binding.adloadingscreen.isVisible = true
        AppController.splshinterstialAd.loadInterStialAd(
            requireContext(),
            adId
        ) { isLoaded ->
            if (isLoaded) {
                appOpenAdViewModel.updateAdStatus(true, "Home Fragment")
                AppController.splshinterstialAd.show_Interstial_Ad(requireActivity()) {
                    binding.adloadingscreen.isVisible = false
                    appOpenAdViewModel.updateAdStatus(false, "Home Fragment")
                    proceed(intent)
                }
            } else {
                showAutoECPM(intent)
            }
        }
    }

    private fun showAutoECPM(intent: Activity) {
        Log.d("inter_ecpm", "showAutoECPM: ")
        val adId = AdDatabaseUtil.getAdmobInterstitialAdId(requireContext())

        binding.adloadingscreen.isVisible = true
        AppController.splshinterstialAd.loadInterStialAd(
            requireContext(),
            adId
        ) {
            appOpenAdViewModel.updateAdStatus(true, "Home Fragment")
            AppController.splshinterstialAd.show_Interstial_Ad(requireActivity()) {
                binding.adloadingscreen.isVisible = false
                appOpenAdViewModel.updateAdStatus(false, "Home Fragment")
                proceed(intent)
            }
        }
    }


    private fun proceed(intent: Activity) {
        when (intent) {
            is ScanImagesActivity -> {
                val intentAct = Intent(requireActivity(), PermissionScreenActivity::class.java)
                intentAct.putExtra("for", "Images")
                startActivity(intentAct)
            }

            is ScanVideosActivity -> {
                val intentAct = Intent(requireActivity(), PermissionScreenActivity::class.java)
                intentAct.putExtra("for", "Videos")
                startActivity(intentAct)
            }

            is ScanAudiosActivity -> {
                val intentAct = Intent(requireActivity(), PermissionScreenActivity::class.java)
                intentAct.putExtra("for", "Audios")
                startActivity(intentAct)
            }

            is ScanFilesActivity -> {
                val intentAct = Intent(requireActivity(), PermissionScreenActivity::class.java)
                intentAct.putExtra("for", "Files")
                startActivity(intentAct)
            }
        }
    }

    private fun isInterstitialEnabled(): Boolean {
        return sharedPrefsHelper.getIsHomeFragmentInterstitialEnabled()
    }


    private fun clickListener() {
        binding.images.setOnClickListener {
            firebaseAnalytics.logEvent("home_image_click", null)
            if (Constant.userClickHomePageFirstTiem && !AppPreferences.getInstance(requireContext()).isAppPurchased && isInterstitialEnabled()) {
                Constant.userClickHomePageFirstTiem = false
                showHighECPM(ScanImagesActivity())
                /*showAdmobInterstitial(object : DataListener {
                    override fun onRecieve(any: Any) {
                        if (any as Boolean) {
                            startActivity(Intent(requireActivity(),ScanImagesActivity::class.java))

                        } else {
                            startActivity(Intent(requireActivity(),ScanImagesActivity::class.java))
                        }
                    }
                })*/

            } else {
                proceed(ScanImagesActivity())
                /*if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                    val adSettings = AppSharedPref.adSettings

                    if (adSettings.addSettings?.AdmobInt != true) {
                        if (IronSource.isInterstitialReady()) {
                            IronSource.showInterstitial("19993437")
                            AppController.setOnAddClickListener {
                                startActivity(Intent(requireActivity(),ScanImagesActivity::class.java))

                            }
                        } else {
                            startActivity(Intent(requireActivity(),ScanImagesActivity::class.java))

                        }
                    } else {
                        showAdmobInterstitial(object : DataListener {
                            override fun onRecieve(any: Any) {
                                if (any as Boolean) {
                                    startActivity(Intent(requireActivity(),ScanImagesActivity::class.java))

                                } else {
                                    startActivity(Intent(requireActivity(),ScanImagesActivity::class.java))
                                }
                            }
                        })
                    }
                }else{
                    startActivity(Intent(requireActivity(),ScanImagesActivity::class.java))
                }*/
            }
        }
        binding.audios.setOnClickListener {
            firebaseAnalytics.logEvent("home_audios_click", null)
            if (Constant.userClickHomePageFirstTiem && !AppPreferences.getInstance(requireContext()).isAppPurchased) {
                Constant.userClickHomePageFirstTiem = false

                showHighECPM(ScanAudiosActivity())
            } else {
                proceed(ScanAudiosActivity())
                /* if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                     val adSettings = AppSharedPref.adSettings

                     if (adSettings.addSettings?.AdmobInt != true) {
                         if (IronSource.isInterstitialReady()) {
                             IronSource.showInterstitial("19993437")
                             AppController.setOnAddClickListener {
                                 startActivity(Intent(requireActivity(),ScanAudiosActivity::class.java))

                             }
                         } else {
                             startActivity(Intent(requireActivity(),ScanAudiosActivity::class.java))

                         }
                     } else {
                         showAdmobInterstitial(object : DataListener {
                             override fun onRecieve(any: Any) {
                                 if (any as Boolean) {
                                     startActivity(Intent(requireActivity(),ScanAudiosActivity::class.java))

                                 } else {
                                     startActivity(Intent(requireActivity(),ScanAudiosActivity::class.java))
                                 }
                             }
                         })
                     }
                 }else{
                     startActivity(Intent(requireActivity(),ScanAudiosActivity::class.java))
                 }*/
            }
        }
        binding.videos.setOnClickListener {
            firebaseAnalytics.logEvent("home_videos_click", null)
            if (Constant.userClickHomePageFirstTiem && !AppPreferences.getInstance(requireContext()).isAppPurchased) {
                Constant.userClickHomePageFirstTiem = false

                showHighECPM(ScanVideosActivity())
            } else {
                proceed(ScanVideosActivity())
                /*if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                    val adSettings = AppSharedPref.adSettings

                    if (adSettings.addSettings?.AdmobInt != true) {
                        if (IronSource.isInterstitialReady()) {
                            IronSource.showInterstitial("19993437")
                            AppController.setOnAddClickListener {
                                startActivity(Intent(requireActivity(),ScanVideosActivity::class.java))

                            }
                        } else {
                            startActivity(Intent(requireActivity(),ScanVideosActivity::class.java))

                        }
                    } else {
                        showAdmobInterstitial(object : DataListener {
                            override fun onRecieve(any: Any) {
                                if (any as Boolean) {
                                    startActivity(Intent(requireActivity(),ScanVideosActivity::class.java))

                                } else {
                                    startActivity(Intent(requireActivity(),ScanVideosActivity::class.java))
                                }
                            }
                        })
                    }
                }else{
                    startActivity(Intent(requireActivity(),ScanVideosActivity::class.java))
                }*/
            }
        }
        binding.files.setOnClickListener {
            firebaseAnalytics.logEvent("home_files_click", null)
            if (Constant.userClickHomePageFirstTiem && !AppPreferences.getInstance(requireContext()).isAppPurchased) {
                Constant.userClickHomePageFirstTiem = false

                showHighECPM(ScanFilesActivity())

            } else {
                proceed(ScanFilesActivity())
                /*if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                    val adSettings = AppSharedPref.adSettings

                    if (adSettings.addSettings?.AdmobInt != true) {
                        if (IronSource.isInterstitialReady()) {
                            IronSource.showInterstitial("19993437")
                            AppController.setOnAddClickListener {
                                startActivity(Intent(requireActivity(),ScanFilesActivity::class.java))

                            }
                        } else {
                            startActivity(Intent(requireActivity(),ScanFilesActivity::class.java))

                        }
                    } else {
                        showAdmobInterstitial(object : DataListener {
                            override fun onRecieve(any: Any) {
                                if (any as Boolean) {
                                    startActivity(Intent(requireActivity(),ScanFilesActivity::class.java))

                                } else {
                                    startActivity(Intent(requireActivity(),ScanFilesActivity::class.java))
                                }
                            }
                        })
                    }
                }else{
                    startActivity(Intent(requireActivity(),ScanFilesActivity::class.java))
                }*/
            }
        }
        binding.prolayout.setOnClickListener {
            firebaseAnalytics.logEvent("home_premium_click", null)
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