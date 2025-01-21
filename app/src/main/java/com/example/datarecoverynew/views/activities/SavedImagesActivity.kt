package com.example.datarecoverynew.views.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.base.BaseActivity
import com.example.datarecoverynew.databinding.ActivitySavedImagesBinding
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
import java.text.SimpleDateFormat

class SavedImagesActivity : BaseActivity() {
    lateinit var binding: ActivitySavedImagesBinding
    lateinit var mainViewModel: MainViewModel
    var imagesList = ArrayList<FilesModel>()
    var recoverPhotos: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var groupImages: ArrayList<GroupFilesModel> = ArrayList<GroupFilesModel>()
    lateinit var adapter: ImagesAdapter
    var updateCount = 0
    private val sharedPreferences: SharedPrefsHelper by lazy { SharedPrefsHelper(this) }

    private fun isAdEnabled(): Boolean {
        return sharedPreferences.getIsSavedImagesNativeEnabled()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = MainViewModel(this)

        initViews()
        observer()
        clickListener()
        showHideProgress(true)
        scrollListener()
    }

    private fun clickListener() {
        binding.recoverBtn.setOnClickListener {
            if (recoverPhotos.isEmpty()) {
                Toast.makeText(
                    this@SavedImagesActivity,
                    getString(R.string.no_item_selected),
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                var isDeleted = true
                val iterator = recoverPhotos.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    var isSucces = false
                    if (item.file.exists()) {
                        isSucces = item.file.delete()
                    }

                    if (!isSucces) {
                        isDeleted = false
                        Toast.makeText(
                            this@SavedImagesActivity, getString(R.string.try_again_later),
                            Toast.LENGTH_SHORT
                        ).show()
                        recoverPhotos.clear()
                        adapter.setCheckFalse()
                        break
                    } else {
                        imagesList.remove(item)
                        iterator.remove()
                    }
                }
                if (isDeleted) {
                    recoverPhotos.clear()
                    MediaScanner(this@SavedImagesActivity)
                    binding.recoverBtn.visibility = View.GONE
                    Toast.makeText(
                        this@SavedImagesActivity,
                        getString(R.string.all_files_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (imagesList.isEmpty()) {
                        binding.noTV.visibility = View.VISIBLE
                        binding.recoverBtn.visibility = View.GONE
                    } else {
                        binding.noTV.visibility = View.GONE
                        binding.recoverBtn.visibility = View.VISIBLE
                        adapter.setCheckFalse()
                    }

                    /* adapter = ImagesAdapter(this@SavedImagesActivity,imagesList,object :
                        DataListener {
                        override fun onRecieve(any: Any) {
                            performLogic(any as FilesModel)
                        }

                        override fun onClick(any: Any) {
                            super.onClick(any)
                            val fileModel = any as FilesModel
                            deleteImage(fileModel)
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
            watchADDialog(this@SavedImagesActivity, object : DataListener {
                override fun onRecieve(any: Any) {
                    val watchAd = any as Boolean
                    if (watchAd) {

                        binding.adloadingscreen.isVisible = true
                        loadRewardedAd(this@SavedImagesActivity)


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
                            this@SavedImagesActivity,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onPremium(any: Any) {
                    super.onPremium(any)

                    if (any as Boolean) {
                        val intent = Intent(this@SavedImagesActivity, PremiumActivity::class.java)
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
                    showRewardedAd(this@SavedImagesActivity)
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
                this@SavedImagesActivity,
                this@SavedImagesActivity,
                isAdEnabled(),
                prepareListWithAds(imagesList),
                object : DataListener {
                    override fun onRecieve(any: Any) {
                        performLogic(any as FilesModel)
                    }

                    override fun onClick(any: Any) {
                        super.onClick(any)
                        val fileModel = any as FilesModel
                        deleteImage(fileModel)
                    }
                })
        binding.recylerView.adapter = adapter
    }

    private fun observer() {

        mainViewModel.liveDataSavedImagesList.observe(this) {
            Log.d("TAG", "observer: ${it.size}")
            binding.progressTV.text =
                mainViewModel.scannedFiles.toString() + getString(R.string.files_scanned) + " , " + it.size + " " + getString(
                    R.string.images_found
                )

            imagesList.clear()
            imagesList.addAll(it)
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
                imagesList.clear()
                imagesList.addAll(mainViewModel.imagesSavedList)

                if (imagesList.isEmpty()) {
                    binding.noTV.visibility = View.VISIBLE
                    binding.recoverBtn.visibility = View.GONE
                } else {
                    binding.noTV.visibility = View.GONE
                    binding.recoverBtn.visibility = View.VISIBLE
                }
                completeScanningDialog(
                    this@SavedImagesActivity,
                    imagesList.size,
                    mainViewModel.scannedFiles
                )
                if (isAdEnabled()) {
                    adapter.updateList(prepareListWithAds(imagesList))
                }
                adapter?.notifyDataSetChanged()
            }
        }
    }

    fun deleteImage(filesModel: FilesModel) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder
        builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.open_image_dialog, null)
        val recoverBtn = view.findViewById<AppCompatButton>(R.id.recoverBtn)
        val deleteBtn = view.findViewById<AppCompatButton>(R.id.daleteBtn)
        val img = view.findViewById<ImageView>(R.id.img)
        val cancelIV = view.findViewById<ImageView>(R.id.cancelIV)
        val nameTV = view.findViewById<TextView>(R.id.nameTV)
        val dateTV = view.findViewById<TextView>(R.id.dateTV)

        builder.setView(view)
        builder.setCancelable(false)
        imageDialog = builder.create()
        recoverBtn.visibility = View.GONE
        deleteBtn.visibility = View.VISIBLE
        val file = filesModel.file
        nameTV.text = file.name
        val dateFormated = SimpleDateFormat("MMM dd HH:mm a").format(file.lastModified())

        dateTV.text = dateFormated
        try {
            Glide.with(this@SavedImagesActivity)
                .load("file://" + file.path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .centerCrop()
                .error(R.drawable.ic_images)
                .into(img)
        } catch (e: Exception) {
            //do nothing
            Toast.makeText(this@SavedImagesActivity, "Exception: " + e.message, Toast.LENGTH_SHORT)
                .show()
        }
        recoverBtn.setOnClickListener {
            val isSucces = copy(file)
            if (!isSucces) {
                Toast.makeText(
                    this@SavedImagesActivity,
                    getString(R.string.try_again_later),
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this@SavedImagesActivity,
                    getString(R.string.recover_image_successfully),
                    Toast.LENGTH_SHORT
                ).show()

            }
            imageDialog.dismiss()
        }
        deleteBtn.setOnClickListener {
            val isSucces = file.delete()
            if (!isSucces) {
                Toast.makeText(
                    this@SavedImagesActivity, getString(R.string.try_again_later),
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                imagesList.remove(filesModel)
                Toast.makeText(
                    this@SavedImagesActivity, getString(R.string.image_deleted_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                adapter.notifyDataSetChanged()
            }
            imageDialog.dismiss()
        }
        cancelIV.setOnClickListener { imageDialog.dismiss() }
        if (imageDialog.getWindow() != null) imageDialog.getWindow()!!
            .setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
        imageDialog.setCancelable(false)
        imageDialog.show()
    }

    private fun performLogic(file: FilesModel) {
        val list = recoverPhotos.filter { s -> s.file == file.file }
        if (list.isEmpty()) {
            recoverPhotos.add(file)
        } else {
            recoverPhotos.remove(file)
            /*val item = list.first()
            val index = findIndex(recoverPhotos, item)
            if (!file.isChecked) {
                recoverPhotos.remove(item)
            } else {
                recoverPhotos.add(item)
            }*/
        }
        Log.d("TAG", "performLogic: ${recoverPhotos.size}")
        if (recoverPhotos.isNotEmpty()) {
            binding.recoverBtn.visibility = View.VISIBLE
        } else {
            binding.recoverBtn.visibility = View.GONE
        }
    }

    fun findIndex(arr: ArrayList<FilesModel>, item: FilesModel): Int {
        return arr.indexOf(item)
    }

    fun scrollListener() {
        binding.recylerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    if (imagesList.size > 50 && !AppPreferences.getInstance(this@SavedImagesActivity).isAppPurchased && !adapter.isWatchAd) {
                        binding.viewMoreBtn.visibility = View.VISIBLE
                    }

                }
            }
        })
    }
}