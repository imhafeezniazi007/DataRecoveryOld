package com.example.datarecoverynew.views.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.base.BaseActivity
import com.example.datarecoverynew.databinding.ActivitySavedVediosBinding
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.datarecoverynew.utils.AppPreferences
import com.example.datarecoverynew.utils.AppSharedPref
import com.example.datarecoverynew.utils.SharedPrefsHelper
import com.example.datarecoverynew.viewmodels.MainViewModel
import com.example.datarecoverynew.views.adapters.DocumentAdapter
import com.example.datarecoverynew.views.adapters.ImagesAdapter
import com.example.recoverydata.interfaces.DataListener
import com.example.recoverydata.models.FilesModel
import com.example.recoverydata.models.GroupFilesModel
import com.example.recoverydata.utils.MediaScanner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SavedVediosActivity : BaseActivity() {
    lateinit var binding: ActivitySavedVediosBinding
    lateinit var mainViewModel: MainViewModel
    var vedioList = ArrayList<FilesModel>()
    var recoverVedios: ArrayList<FilesModel> = ArrayList<FilesModel>()
    lateinit var adapter: ImagesAdapter
    var updateCount = 0
    private val sharedPreferences: SharedPrefsHelper by lazy { SharedPrefsHelper(this) }

    private fun isAdEnabled(): Boolean {
        return sharedPreferences.getIsSavedVideosNativeEnabled()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedVediosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = MainViewModel(this)
        showHideProgress(true)
        initViews()
        observer()
        clickListener()
        scrollListener()
    }


    private fun clickListener() {
        binding.recoverBtn.setOnClickListener {
            if (recoverVedios.isEmpty()) {
                Toast.makeText(
                    this@SavedVediosActivity,
                    getString(R.string.no_item_selected),
                    Toast.LENGTH_SHORT
                )
                    .show()

            } else {
                var isDeleted = true
                val iterator = recoverVedios.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    var isSucces = false
                    if (item.file.exists()) {
                        isSucces = item.file.delete()
                    }
                    if (!isSucces) {
                        isDeleted = false
                        Toast.makeText(
                            this@SavedVediosActivity,
                            getString(R.string.try_again_later),
                            Toast.LENGTH_SHORT
                        ).show()
                        recoverVedios.clear()
                        break
                    } else {
                        vedioList.remove(item)
                        iterator.remove()
                    }
                }
                if (isDeleted) {
                    recoverVedios.clear()
                    MediaScanner(this@SavedVediosActivity)
                    Toast.makeText(
                        this@SavedVediosActivity,
                        getString(R.string.all_files_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (vedioList.isEmpty()) {
                        binding.noTV.visibility = View.VISIBLE
                        binding.recoverBtn.visibility = View.GONE
                    } else {
                        binding.noTV.visibility = View.GONE
                        binding.recoverBtn.visibility = View.VISIBLE
                        adapter.setCheckFalse()
                    }
                    /* val adapter = ImagesAdapter(this@SavedVediosActivity, vedioList, object :
                         DataListener {
                         override fun onRecieve(any: Any) {
                             performLogic(any as FilesModel)
                         }

                         override fun onClick(any: Any) {
                             super.onClick(any)
                             val filesModel = any as FilesModel
 //                            val intent = Intent(this@SavedVediosActivity, VedioPlayerActivity::class.java)
 //                            intent.putExtra("path", filesModel.file.path)
 //                            startActivity(intent)
                         }

                     })
                     binding.recylerView.adapter = adapter*/
                }
            }
        }
        binding.backIV.setOnClickListener {
            finish()
        }
        binding.viewMoreBtn.setOnClickListener {
            watchADDialog(this@SavedVediosActivity, object : DataListener {
                override fun onRecieve(any: Any) {
                    val watchAd = any as Boolean
                    if (watchAd) {

                        binding.adloadingscreen.isVisible = true
                        loadRewardedAd(this@SavedVediosActivity)


//                        val adSettings = AppSharedPref.adSettings
//                        val adId = AdDatabaseUtil.getAdmobInterstitialAdId(this@ScanImagesActivity)
//                        if (adSettings.addSettings?.AdmobInt == true) {
//                            binding.adloadingscreen.isVisible = true
//                            AppController.splshinterstialAd.loadInterStialAd(
//                                this@ScanImagesActivity,
//                                adId
//                            ) {
//                                AppController.splshinterstialAd.show_Interstial_Ad(this@ScanImagesActivity) {
//                                    binding.adloadingscreen.isVisible = false
//                                    adapter?.setIsWatchAd()
//                                    binding.viewMoreLayoyt.visibility = View.GONE
//                                }
//                            }
//                        } else {
//                            adapter?.setIsWatchAd()
//                        }
                    } else {
                        Toast.makeText(
                            this@SavedVediosActivity,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onPremium(any: Any) {
                    super.onPremium(any)

                    if (any as Boolean) {
                        val intent = Intent(this@SavedVediosActivity, PremiumActivity::class.java)
//                                    intent.putExtra("from", "main")
                        startActivity(intent)
                    }
                }
            })
        }
    }

    private var rewardedAd: RewardedAd? = null

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
                    showRewardedAd(this@SavedVediosActivity)
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
                binding.viewMoreBtn.visibility = View.GONE
                adapter.isWatchAd = true
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


    private fun initViews() {
        CoroutineScope(Dispatchers.IO).launch {
            mainViewModel.scannedFiles = 0
            mainViewModel.getSavedData()
        }
        val gridLayoutManager = GridLayoutManager(this, 4) // 4 columns
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (isAdEnabled()) {
                    if (adapter.getItemViewType(position) == DocumentAdapter.VIEW_TYPE.AD_TYPE.id) 4 else 1
                } else {
                    1
                }
            }
        }
        binding.recylerView.layoutManager = gridLayoutManager

        adapter =
            ImagesAdapter(
                this@SavedVediosActivity,
                this@SavedVediosActivity,
                isAdEnabled(),
                prepareListWithAds(vedioList),
                object : DataListener {
                    override fun onRecieve(any: Any) {
                        performLogic(any as FilesModel)
                    }

                    override fun onClick(any: Any) {
                        super.onClick(any)
                        val filesModel = any as FilesModel
                        val intent =
                            Intent(this@SavedVediosActivity, VedioPlayerActivity::class.java)
                        intent.putExtra("path", filesModel.file.path)
                        startActivity(intent)
                    }

                })
        binding.recylerView.adapter = adapter
    }

    private fun observer() {

        mainViewModel.liveDataSavedVideosList.observe(this) {
            Log.d("TAG", "observer: ${it.size}")
            binding.progressTV.text =
                mainViewModel.scannedFiles.toString() + getString(R.string.files_scanned) + " , " + it.size + " " + getString(
                    R.string.vedios_found
                )
            vedioList.clear()
            vedioList.addAll(it)
            updateCount++
            if (updateCount > 10) {
                updateCount = 0
                adapter?.notifyDataSetChanged()
            }
        }
        mainViewModel.liveDataSavedIsDataLoaded.observe(this) {
            if (it) {
                showHideProgress(false)
                binding.progressLayoyt.visibility = View.GONE
                vedioList.clear()
                vedioList.addAll(mainViewModel.videosSavedList)
                if (vedioList.isEmpty()) {
                    binding.noTV.visibility = View.VISIBLE
                    binding.recoverBtn.visibility = View.GONE
                } else {
                    binding.noTV.visibility = View.GONE
                    binding.recoverBtn.visibility = View.VISIBLE
                }
                completeScanningDialog(
                    this@SavedVediosActivity,
                    vedioList.size,
                    mainViewModel.scannedFiles
                )
                if (isAdEnabled()) {
                    adapter.updateList(prepareListWithAds(vedioList))
                }
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun prepareListWithAds(originalList: List<FilesModel>): List<FilesModel> {
        if (isAdEnabled()) {
            val modifiedList = mutableListOf<FilesModel>()
            originalList.forEachIndexed { index, item ->
                modifiedList.add(item)
                // Insert a placeholder for ads after every 4th row
                if ((index + 1) % 16 == 0) { // 4 items per row, so 16 items = 4 rows
                    modifiedList.add(
                        FilesModel(
                            File("ad"),
                            false,
                            DocumentAdapter.VIEW_TYPE.AD_TYPE
                        )
                    ) // Add a unique object for ads
                }
            }
            Log.e("de_tagfil", "prepareListWithAds: ${modifiedList.size}")
            return modifiedList
        } else {
            return originalList
        }
    }

    private fun performLogic(file: FilesModel) {
        val list = recoverVedios.filter { s -> s.file == file.file }
        if (list.isEmpty()) {
            recoverVedios.add(file)
        } else {
            val item = list.first()
            if (!file.isChecked) {
                recoverVedios.remove(item)
            } else {
                recoverVedios.add(item)
            }
        }
        if (recoverVedios.isNotEmpty()) {
            binding.recoverBtn.visibility = View.VISIBLE
        } else {
            binding.recoverBtn.visibility = View.VISIBLE
        }
    }

    private fun findImageGroupIndex(arr: ArrayList<GroupFilesModel>, item: GroupFilesModel): Int {
        return arr.indexOf(item)
    }

    fun scrollListener() {
        binding.recylerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    if (vedioList.size > 50 && !AppPreferences.getInstance(this@SavedVediosActivity).isAppPurchased && !adapter.isWatchAd) {
                        binding.viewMoreBtn.visibility = View.VISIBLE
                    }

                }
            }
        })
    }
}