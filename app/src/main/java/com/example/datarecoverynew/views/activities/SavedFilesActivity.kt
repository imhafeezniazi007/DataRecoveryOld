package com.example.datarecoverynew.views.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.DeviceUtils
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.base.BaseActivity
import com.example.datarecoverynew.databinding.ActivitySavedFilesBinding
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.datarecoverynew.utils.AppPreferences
import com.example.datarecoverynew.utils.AppSharedPref
import com.example.datarecoverynew.utils.SharedPrefsHelper
import com.example.datarecoverynew.viewmodels.MainViewModel
import com.example.datarecoverynew.views.adapters.DocumentAdapter
import com.example.diabetes.helper.ads.isNetworkAvailable
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

class SavedFilesActivity : BaseActivity() {
    lateinit var binding: ActivitySavedFilesBinding
    lateinit var mainViewModel: MainViewModel

    var documentList = ArrayList<FilesModel>()
    var recoverDocumets: ArrayList<FilesModel> = ArrayList<FilesModel>()
    lateinit var adapter: DocumentAdapter
    var updateCount = 0
    private val sharedPreferences: SharedPrefsHelper by lazy { SharedPrefsHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySavedFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = MainViewModel(this)

        initViews()
        showHideProgress(true)
        observer()
        clickListener()
        scrollListener()
    }


    private fun isAdEnabled(): Boolean {
        return sharedPreferences.getIsSavedFilesNativeEnabled()
    }

    private fun clickListener() {
        binding.recoverBtn.setOnClickListener {
            if (recoverDocumets.isEmpty()) {
                Toast.makeText(
                    this@SavedFilesActivity,
                    getText(R.string.no_item_selected),
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                var isDeleted = true
                val iterator = recoverDocumets.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    var isSucces = false
                    if (item.file.exists()) {
                        isSucces = item.file.delete()
                    }
                    if (!isSucces) {
                        isDeleted = false
                        Toast.makeText(
                            this@SavedFilesActivity, getString(R.string.try_again_later),
                            Toast.LENGTH_SHORT
                        ).show()
                        recoverDocumets.clear()
                        break
                    } else {
                        documentList.remove(item)
                        iterator.remove()
                    }
                }
                if (isDeleted) {
                    recoverDocumets.clear()
                    MediaScanner(this@SavedFilesActivity)
                    Toast.makeText(
                        this@SavedFilesActivity,
                        getString(R.string.all_files_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (documentList.isEmpty()) {
                        binding.noTV.visibility = View.VISIBLE
                        binding.recoverBtn.visibility = View.GONE
                    } else {
                        binding.noTV.visibility = View.GONE
                        binding.recoverBtn.visibility = View.VISIBLE
                        adapter.setCheckFalse()
                    }
                    /*val adapter = DocumentAdapter(this@SavedFilesActivity, documentList, object :
                        DataListener {
                        override fun onRecieve(any: Any) {

                            val file = any as FilesModel
                            openFile(file.file)
                        }

                        override fun onClick(any: Any) {
                            super.onClick(any)
                            performLogic(any as FilesModel)
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
            watchADDialog(this@SavedFilesActivity, object : DataListener {
                override fun onRecieve(any: Any) {
                    val watchAd = any as Boolean
                    if (watchAd) {

                        binding.adloadingscreen.isVisible = true
                        loadRewardedAd(this@SavedFilesActivity)


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
                            this@SavedFilesActivity,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onPremium(any: Any) {
                    super.onPremium(any)

                    if (any as Boolean) {
                        val intent = Intent(this@SavedFilesActivity, PremiumActivity::class.java)
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
                    showRewardedAd(this@SavedFilesActivity)
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
        val gridLayoutManager = GridLayoutManager(this, 1)
        binding.recylerView.layoutManager = gridLayoutManager

        adapter =
            DocumentAdapter(
                this@SavedFilesActivity,
                this@SavedFilesActivity,
                isAdsEnabled = isAdEnabled(),
                prepareListWithAds(documentList),
                object :
                    DataListener {
                    override fun onRecieve(any: Any) {

                        val file = any as FilesModel
                        openFile(file.file)
                    }

                    override fun onClick(any: Any) {
                        super.onClick(any)
                        performLogic(any as FilesModel)
                    }
                })
        binding.recylerView.adapter = adapter
    }

    private fun observer() {

        mainViewModel.liveDataSavedDocumentsList.observe(this) {
            Log.d("TAG", "observer: ${it.size}")
            binding.progressTV.text =
                mainViewModel.scannedFiles.toString() + getString(R.string.files_scanned) + " , " + it.size + " " + getString(
                    R.string.documents_found
                )
            documentList.clear()
            documentList.addAll(it)
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
                documentList.clear()
                documentList.addAll(mainViewModel.documentsSavedList)
                if (documentList.isEmpty()) {
                    binding.noTV.visibility = View.VISIBLE
                    binding.recoverBtn.visibility = View.GONE
                }
                completeScanningDialog(
                    this@SavedFilesActivity,
                    documentList.size,
                    mainViewModel.scannedFiles
                )
                val tempList = ArrayList<FilesModel>()
                tempList.addAll(documentList)
                documentList.clear()
                tempList.forEachIndexed { index, model ->
                    documentList.add(model)
                    if (DeviceUtils.isDeviceRooted().not()) {
                        val adSettings = AppSharedPref.adSettings.addSettings?.AdNativeAd
                        val adId = AdDatabaseUtil.getAdmobNativeAdId(this@SavedFilesActivity)
                        if (adSettings == true && adId.isNotEmpty()) {
                            if ((index == 2) and (isNetworkAvailable())) {
                                val newTemModel = FilesModel(
                                    model.file,
                                    model.isChecked,
                                    DocumentAdapter.VIEW_TYPE.AD_TYPE
                                )
                                documentList.add(newTemModel)
                            }
                        }
                    }
                }
                if (isAdEnabled()) {
                    adapter.updateList(prepareListWithAds(documentList))
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
                if ((index + 1) % 4 == 0) { // 4 items per row, so 16 items = 4 rows
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


    fun openFile(file: File) {
        // Get URI and MIME type of file
        val uri: Uri =
            FileProvider.getUriForFile(this, "com.example.datarecoverynew.provider", file)
        val mime = contentResolver.getType(uri)

        // Open file with user selected app
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(uri, mime)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

    private fun performLogic(file: FilesModel) {
        val list = recoverDocumets.filter { s -> s.file == file.file }
        if (list.isEmpty()) {
            recoverDocumets.add(file)
        } else {
            val item = list.first()
            if (!file.isChecked) {
                recoverDocumets.remove(item)
            } else {
                recoverDocumets.add(item)
            }
        }
        if (recoverDocumets.isNotEmpty()) {
            binding.recoverBtn.visibility = View.VISIBLE
        } else {
            binding.recoverBtn.visibility = View.GONE
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
                    if (documentList.size > 50 && !AppPreferences.getInstance(this@SavedFilesActivity).isAppPurchased && !adapter.isWatchAd) {
                        binding.viewMoreBtn.visibility = View.VISIBLE
                    }

                }
            }
        })
    }
}