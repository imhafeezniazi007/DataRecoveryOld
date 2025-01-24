package com.example.datarecoverynew.views.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.base.BaseActivity
import com.example.datarecoverynew.databinding.ActivityScanAudiosBinding
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.datarecoverynew.utils.AppPreferences
import com.example.datarecoverynew.utils.AppSharedPref
import com.example.datarecoverynew.utils.SharedPrefsHelper
import com.example.datarecoverynew.viewmodels.MainViewModel
import com.example.datarecoverynew.views.adapters.DocumentAdapter
import com.example.recoverydata.interfaces.DataListener
import com.example.recoverydata.models.FilesModel
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

class ScanAudiosActivity : BaseActivity() {
    lateinit var binding: ActivityScanAudiosBinding
    lateinit var mainViewModel: MainViewModel
    var audioList = ArrayList<FilesModel>()
    var recoverAudios: ArrayList<FilesModel> = ArrayList<FilesModel>()
    val mediaPlayer = MediaPlayer()
    private var startTime = 0.0
    val seekForwardTime = 5000; // 5000 milliseconds
    val seekBackwardTime = 5000; // 5000 milliseconds
    private var currentIndex = -1
    lateinit var adapter: DocumentAdapter
    var updateCount = 0
    private val sharedPreferences: SharedPrefsHelper by lazy { SharedPrefsHelper(this) }

    private fun isAdEnabled(): Boolean {
        return sharedPreferences.getIsSavedAudioNativeEnabled()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanAudiosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = MainViewModel(this)

        initViews()
        observer()
        clickListener()
        scrollListener()
    }

    private fun clickListener() {
        binding.backIV.setOnClickListener {
            finish()
        }
        binding.recoverBtn.setOnClickListener {
            showHighECPM()
        }

        binding.viewMoreBtn.setOnClickListener {
            watchADDialog(this@ScanAudiosActivity, object : DataListener {
                override fun onRecieve(any: Any) {
                    val watchAd = any as Boolean
                    if (watchAd) {
                        binding.adloadingscreen.isVisible = true
                        loadRewardedAd(this@ScanAudiosActivity)

                    } else {
                        Toast.makeText(
                            this@ScanAudiosActivity,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onPremium(any: Any) {
                    super.onPremium(any)

                    if (any as Boolean) {
                        val intent = Intent(this@ScanAudiosActivity, PremiumActivity::class.java)
//                                    intent.putExtra("from", "main")
                        startActivity(intent)
                    }
                }
            })
        }
    }

    private fun showHighECPM() {
        Log.d("inter_ecpm", "showHighECPM: ")
        val adId = sharedPreferences.getInterstitialHighId()

        binding.adloadingscreen.isVisible = true
        AppController.splshinterstialAd.loadInterStialAd(
            this,
            adId
        ) { isLoaded ->
            if (isLoaded) {
                AppController.splshinterstialAd.show_Interstial_Ad(this) {
                    binding.adloadingscreen.isVisible = false
                    proceed()
                }
            } else {
                showMediumECPM()
            }
        }
    }

    private fun showMediumECPM() {
        Log.d("inter_ecpm", "showMediumECPM: ")

        val adId = sharedPreferences.getInterstitialMediumId()

        binding.adloadingscreen.isVisible = true
        AppController.splshinterstialAd.loadInterStialAd(
            this,
            adId
        ) { isLoaded ->
            if (isLoaded) {
                AppController.splshinterstialAd.show_Interstial_Ad(this) {
                    binding.adloadingscreen.isVisible = false
                    proceed()
                }
            } else {
                showAutoECPM()
            }
        }
    }

    private fun showAutoECPM() {
        Log.d("inter_ecpm", "showAutoECPM: ")
        val adId = AdDatabaseUtil.getAdmobInterstitialAdId(this)

        binding.adloadingscreen.isVisible = true
        AppController.splshinterstialAd.loadInterStialAd(
            this,
            adId
        ) {
            AppController.splshinterstialAd.show_Interstial_Ad(this) {
                binding.adloadingscreen.isVisible = false
                proceed()
            }
        }
    }

    private fun proceed() {
        var isDeleted = true
        val iterator = recoverAudios.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            val isSucces = copy(item.file)
            if (!isSucces) {
                isDeleted = false
                Toast.makeText(
                    this@ScanAudiosActivity,
                    getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
                recoverAudios.clear()
                break
            } else {
                iterator.remove()
            }

        }
        if (isDeleted) {
            recoverAudios.clear()
            MediaScanner(this@ScanAudiosActivity)
            showSnackbar()
//                Toast.makeText(this@AudiosActivity,"All files recover successfully",Toast.LENGTH_SHORT).show()
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
                    showRewardedAd(this@ScanAudiosActivity)
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


    private fun initViews() {
        showHideProgress(true)
        CoroutineScope(Dispatchers.IO).launch {
            mainViewModel.scannedFiles = 0
            mainViewModel.getDocuments()
        }
        val gridLayoutManager = GridLayoutManager(this, 1)
        binding.recylerView.layoutManager = gridLayoutManager

        adapter =
            DocumentAdapter(
                this@ScanAudiosActivity,
                this@ScanAudiosActivity,
                isAdsEnabled = isAdEnabled(),
                prepareListWithAds(audioList),
                object : DataListener {
                    override fun onRecieve(any: Any) {
                        val file = any as FilesModel
                        currentIndex = findFileIndex(audioList, file)
                        audioPlayerDialog(file.file)
                    }

                    override fun onClick(any: Any) {
                        super.onClick(any)
                        performLogic(any as FilesModel)
                    }
                })
        binding.recylerView.adapter = adapter
    }

    private fun observer() {
        mainViewModel.liveDataAudiosList.observe(this) {
            Log.d("TAG", "observer: ${it.size}")
            binding.progressTV.text =
                mainViewModel.scannedFiles.toString() + getString(R.string.files_scanned) + " , " + it.size + " " + getString(
                    R.string.audios_found
                )
            audioList.clear()
            audioList.addAll(it)
            updateCount++
            if (updateCount > 10) {
                updateCount = 0
                adapter?.notifyDataSetChanged()
            }
        }
        mainViewModel.liveDataIsDataLoaded.observe(this) {
            if (it) {
                showHideProgress(false)
                binding.progressLayoyt.visibility = View.GONE
                audioList.clear()
                audioList.addAll(mainViewModel.audiosList)
                if (audioList.isEmpty()) {
                    binding.noTV.visibility = View.VISIBLE
                    binding.recoverBtn.visibility = View.GONE
                } else {
                    binding.noTV.visibility = View.GONE
                    binding.recoverBtn.visibility = View.VISIBLE
                }
                adapter.setIsDataLoaded()
                completeScanningDialog(
                    this@ScanAudiosActivity,
                    audioList.size,
                    mainViewModel.scannedFiles
                )
                val tempList = ArrayList<FilesModel>()
                tempList.addAll(audioList)
                audioList.clear()
                tempList.forEachIndexed { index, model ->
                    audioList.add(model)
//                    if (DeviceUtils.isDeviceRooted().not()) {
//                        val adSettings = AppSharedPref.adSettings.addSettings?.AdNativeAd
//                        val adId = AdDatabaseUtil.getAdmobNativeAdId(this@ScanAudiosActivity)
//                        if (adSettings == true && adId.isNotEmpty()) {
//                            if ((index == 2) and (isNetworkAvailable())) {
//                                val newTemModel = FilesModel(
//                                    model.file,
//                                    model.isChecked,
//                                    DocumentAdapter.VIEW_TYPE.AD_TYPE
//                                )
//                                audioList.add(newTemModel)
//                            }
//                        }
//                    }
                }
                if (isAdEnabled()) {
                    adapter.updateList(prepareListWithAds(audioList))
                }
                adapter?.notifyDataSetChanged()
            }
        }
    }
    /*private fun playAudio(file: File) {
        binding.playerLayout.visibility = View.VISIBLE
        binding.playIV.setImageResource(R.drawable.ic_pause)
        binding.titleTV.text = file.name
        binding.seekBar!!.progress = 0
        mediaPlayer.reset()
        try {
            mediaPlayer.setDataSource(file.path)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        val finalTime = mediaPlayer.duration;
        startTime = mediaPlayer.currentPosition.toDouble();
        binding.seekBar!!.progress = startTime.toInt()
        binding.seekBar!!.max = finalTime / 1000
        val mHandler = Handler()
        runOnUiThread(object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    val mCurrentPosition: Int = mediaPlayer.currentPosition / 1000
                    binding.seekBar!!.progress = mCurrentPosition

                }
                mHandler.postDelayed(this, 100)
            }
        })
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000)
                }
            }
        })
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.seekTo(0)
            binding.playIV.setImageResource(R.drawable.ic_play)
        }
        binding.nextIV.setOnClickListener {
            // get current song position
            // check if next song is there or not
            if (currentIndex < (audioList.size - 1)) {
                currentIndex += 1;
                val nextFile = audioList[currentIndex].file
                try {
                    binding.titleTV.text = nextFile.name
                    binding.playIV.setImageResource(R.drawable.ic_play)
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(nextFile.path)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

        }
        binding.previousIV.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex -= 1;
                val previousFile = audioList[currentIndex].file
                try {
                    binding.titleTV.text = previousFile.name
                    binding.playIV.setImageResource(R.drawable.ic_play)
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(previousFile.path)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        binding.minusIV.setOnClickListener {
            val currentPosition: Int = mediaPlayer.currentPosition
            // check if seekBackward time is greater than 0 sec
            // check if seekBackward time is greater than 0 sec
            if (currentPosition - seekBackwardTime >= 0) {
                // forward song
                mediaPlayer.seekTo(currentPosition - seekBackwardTime)
            } else {
                // backward to starting position
                mediaPlayer.seekTo(0)
            }

        }
        binding.plusIV.setOnClickListener {
            val currentPosition: Int = mediaPlayer.currentPosition
            if (currentPosition + seekForwardTime <= mediaPlayer.duration) {
                // forward song
                mediaPlayer.seekTo(currentPosition + seekForwardTime)
            } else {
                // forward to end position
                mediaPlayer.seekTo(mediaPlayer.duration)
            }

        }
        binding.playIV.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause();
                binding.playIV.setImageResource(R.drawable.ic_play)
            } else {
                mediaPlayer.start();
                binding.playIV.setImageResource(R.drawable.ic_pause)
            }
        }

    }*/

    override fun onPause() {
        super.onPause()
        mediaPlayer.stop()
    }

    fun findFileIndex(arr: ArrayList<FilesModel>, item: FilesModel): Int {
        return arr.indexOf(item)
    }

    /*private fun performLogic(file: FilesModel) {
        val list = recoverAudios.filter { s -> s.file == file.file }
        if (list.isEmpty()) {
            recoverAudios.add(file)
        } else {
            val item = list.first()
            if (!file.isChecked) {
                recoverAudios.remove(item)
            } else {
                recoverAudios.add(item)
            }
        }
        if (recoverAudios.isNotEmpty()) {
            binding.noselectedLayout.visibility = View.GONE
            binding.recoverLayout.visibility = View.VISIBLE
        } else {
            binding.noselectedLayout.visibility = View.VISIBLE
            binding.recoverLayout.visibility = View.GONE
        }
    }*/

    private fun performLogic(file: FilesModel) {
        val list = recoverAudios.filter { s -> s.file == file.file }
        if (list.isEmpty()) {
            recoverAudios.add(file)
        } else {
            val item = list.first()
            if (!file.isChecked) {
                recoverAudios.remove(item)
            } else {
                recoverAudios.add(item)
            }
        }
        if (recoverAudios.isNotEmpty()) {
            binding.recoverBtn.visibility = View.VISIBLE
        } else {
            binding.recoverBtn.visibility = View.INVISIBLE
        }
    }

    fun audioPlayerDialog(file: File) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.audio_player_layout, null)
        val titleTV = view.findViewById<TextView>(R.id.titleTV)
        val playIV = view.findViewById<ImageView>(R.id.playIV)
        val nextIV = view.findViewById<ImageView>(R.id.nextIV)
        val previousIV = view.findViewById<ImageView>(R.id.previousIV)
        val cancelIV = view.findViewById<ImageView>(R.id.cancelIV)
        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)

        playIV.setImageResource(R.drawable.ic_audio_pause)
        titleTV.text = file.name
        seekBar!!.progress = 0
        mediaPlayer.reset()
        try {
            mediaPlayer.setDataSource(file.path)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        val finalTime = mediaPlayer.duration;
        startTime = mediaPlayer.currentPosition.toDouble();
        seekBar.progress = startTime.toInt()
        seekBar.max = finalTime / 1000
        val mHandler = Handler()
        runOnUiThread(object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    val mCurrentPosition: Int = mediaPlayer.currentPosition / 1000
                    seekBar.progress = mCurrentPosition

                }
                mHandler.postDelayed(this, 100)
            }
        })
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000)
                }
            }
        })
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.seekTo(0)
            playIV.setImageResource(R.drawable.ic_play)
        }
        nextIV.setOnClickListener {
            // get current song position
            // check if next song is there or not
            if (currentIndex < (audioList.size - 1)) {
                currentIndex += 1;
                val nextFile = audioList[currentIndex].file
                try {
                    titleTV.text = nextFile.name
                    playIV.setImageResource(R.drawable.ic_play)
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(nextFile.path)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

        }
        previousIV.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex -= 1;
                val previousFile = audioList[currentIndex].file
                try {
                    titleTV.text = previousFile.name
                    playIV.setImageResource(R.drawable.ic_play)
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(previousFile.path)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        playIV.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause();
                playIV.setImageResource(R.drawable.ic_play)
            } else {
                mediaPlayer.start();
                playIV.setImageResource(R.drawable.ic_audio_pause)
            }
        }

        cancelIV.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause();
                playIV.setImageResource(R.drawable.ic_play)
            }
            imageDialog!!.dismiss()
        }

        builder.setView(view)
        builder.setCancelable(true)
        imageDialog = builder.create()
        imageDialog!!.getWindow()!!
            .setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
        imageDialog.setCancelable(false)
        imageDialog.show()
    }

    fun scrollListener() {
        binding.recylerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    if (audioList.size > 50 && !AppPreferences.getInstance(this@ScanAudiosActivity).isAppPurchased && !adapter.isWatchAd) {
                        binding.viewMoreBtn.visibility = View.VISIBLE
                    }

                }
            }
        })
    }
}