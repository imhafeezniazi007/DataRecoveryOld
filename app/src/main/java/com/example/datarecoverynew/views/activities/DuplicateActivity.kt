package com.example.datarecoverynew.views.activities

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.base.BaseActivity
import com.example.datarecoverynew.databinding.ActivityDuplicateBinding
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.datarecoverynew.utils.AppPreferences
import com.example.datarecoverynew.utils.AppSharedPref
import com.example.datarecoverynew.utils.Constant
import com.example.datarecoverynew.utils.SharedPrefsHelper
import com.example.datarecoverynew.viewmodels.MainViewModel
import com.example.datarecoverynew.views.adapters.DuplicateParentAdapter
import com.example.datarecoverynew.views.adapters.ContactsAdapter
import com.example.recoverydata.interfaces.DataListener
import com.example.recoverydata.models.ContactModel
import com.example.recoverydata.models.Duplicate
import com.example.recoverydata.models.DuplicateFile
import com.example.recoverydata.utils.MediaScanner
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DuplicateActivity : BaseActivity() {
    lateinit var binding: ActivityDuplicateBinding
    lateinit var mainViewModel: MainViewModel
    var deletedItems = ArrayList<DuplicateFile>()
    var imagesList = ArrayList<Duplicate>()
    var selectedContacts = ArrayList<ContactModel>()
    val uriList: ArrayList<Uri> = ArrayList()
    var adapter: DuplicateParentAdapter? = null
    private val sharedPreferences: SharedPrefsHelper by lazy { SharedPrefsHelper(this) }

    private fun isAdEnabled(): Boolean {
        return sharedPreferences.getIsNativeDuplicateEnabled()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDuplicateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showHideDuplicateProgress(true)
        mainViewModel = MainViewModel(this)
        val from = intent.getStringExtra("from")

        if (from.equals("images")) {
            binding.titleTV.text = getString(R.string.duplicate_images)
            imagesList.clear()
            CoroutineScope(Dispatchers.IO).launch {
                imagesList = mainViewModel.getDuplicateImages(this@DuplicateActivity)
                if (imagesList.isNotEmpty()) {
                    imagesList.withIndex()?.forEach { fileParent ->
                        fileParent.value.getDuplicateFiles().withIndex().forEach { fileChild ->
                            Log.d(
                                "TAG",
                                "testttt: ${imagesList[fileParent.index].getDuplicateFiles()[fileChild.index].getFile()}"
                            )
                            Log.d(
                                "TAG",
                                "testttt: ${imagesList[fileParent.index].getDuplicateFiles()[fileChild.index].isSelect}"
                            )
                            if (fileChild.index != 0) {
                                imagesList[fileParent.index].getDuplicateFiles()[fileChild.index].isSelect =
                                    true
                                deletedItems.add(imagesList[fileParent.index].getDuplicateFiles()[fileChild.index])
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    Log.d("TAG", "onCreate: ${imagesList.size}")
                    showHideDuplicateProgress(false)
                    adapter = DuplicateParentAdapter(
                        this@DuplicateActivity,
                        this@DuplicateActivity,
                        isAdEnabled(),
                        imagesList,
                        object :
                            DataListener {
                            override fun onRecieve(any: Any) {
                                val item = any as DuplicateFile
                                if (deletedItems.contains(item)) {
                                    deletedItems.remove(item)
                                } else {
                                    deletedItems.add(item)
                                }
                                Log.d("TAG", "onRecieve: ${item.isSelect}")
                            }
                        })
                    binding.recylerView.adapter = adapter
//                    binding.loadingIV.visibility = View.GONE
                    if (imagesList.isEmpty()) {
                        binding.noTV.visibility = View.VISIBLE
                    }
                }
            }

        } else if (from.equals("videos")) {
            binding.titleTV.text = getString(R.string.duplicate_videos)
//            binding.loadingIV.visibility = View.VISIBLE
            imagesList.clear()
            CoroutineScope(Dispatchers.IO).launch {
                imagesList = mainViewModel.getAllDuplicateVideos(this@DuplicateActivity)
                if (imagesList.isNotEmpty()) {
                    imagesList.withIndex().forEach { fileParent ->
                        fileParent.value.getDuplicateFiles().withIndex().forEach { fileChild ->
                            if (fileChild.index != 0) {
                                imagesList[fileParent.index].getDuplicateFiles()[fileChild.index].isSelect =
                                    true
                                deletedItems.add(imagesList[fileParent.index].getDuplicateFiles()[fileChild.index])
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    showHideDuplicateProgress(false)
                    adapter = DuplicateParentAdapter(
                        this@DuplicateActivity,
                        this@DuplicateActivity,
                        isAdEnabled(),
                        imagesList,
                        object :
                            DataListener {
                            override fun onRecieve(any: Any) {
                                val item = any as DuplicateFile
                                Log.d("TAG", "onRecieve: ${item.isSelect}")
                                if (deletedItems.contains(item)) {
                                    deletedItems.remove(item)
                                } else {
                                    deletedItems.add(item)
                                }
                            }

                        })
                    binding.recylerView.adapter = adapter
//                    binding.loadingIV.visibility = View.GONE
                    if (imagesList.isEmpty()) {
                        binding.noTV.visibility = View.VISIBLE
                    }
                }
            }
        } else if (from.equals("audios")) {
            binding.titleTV.text = getString(R.string.duplicate_audios)
//            binding.loadingIV.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                imagesList = mainViewModel.getDuplicateAudios(this@DuplicateActivity)
                if (imagesList.isNotEmpty()) {
                    imagesList.withIndex().forEach { fileParent ->
                        fileParent.value.getDuplicateFiles().withIndex().forEach { fileChild ->
                            if (fileChild.index != 0) {
                                imagesList[fileParent.index].getDuplicateFiles()[fileChild.index].isSelect =
                                    true
                                deletedItems.add(imagesList[fileParent.index].getDuplicateFiles()[fileChild.index])
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    showHideDuplicateProgress(false)
                    adapter = DuplicateParentAdapter(
                        this@DuplicateActivity,
                        this@DuplicateActivity,
                        isAdEnabled(),
                        imagesList,
                        object :
                            DataListener {
                            override fun onRecieve(any: Any) {
                                val item = any as DuplicateFile
                                Log.d("TAG", "onRecieve: ${item.isSelect}")
                                if (deletedItems.contains(item)) {
                                    deletedItems.remove(item)
                                } else {
                                    deletedItems.add(item)
                                }
                            }

                        })
                    binding.recylerView.adapter = adapter
//                    binding.loadingIV.visibility = View.GONE
                    if (imagesList.isEmpty()) {
                        binding.noTV.visibility = View.VISIBLE
                    }
                }
            }

        } else if (from.equals("contacts")) {
            binding.titleTV.text = getString(R.string.duplicate_contacts)
//            binding.loadingIV.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                val contactList = mainViewModel.getDuplicateContacts(this@DuplicateActivity)
                selectedContacts.addAll(mainViewModel.selectedContacts)
                withContext(Dispatchers.Main) {
                    showHideDuplicateProgress(false)
                    val madapter = ContactsAdapter(
                        this@DuplicateActivity,
                        contactList,
                        selectedContacts,
                        object :
                            DataListener {
                            override fun onRecieve(any: Any) {
                                val item = any as ContactModel
                                Log.d("TAG", "onRecieve: ${item.name}")
                                if (selectedContacts.contains(item)) {
                                    selectedContacts.remove(item)
                                } else {
                                    selectedContacts.add(item)
                                }
                                Log.d("TAG", "onRecieve: ${selectedContacts.size}")
                            }
                        })
                    binding.recylerView.adapter = madapter
//                    binding.loadingIV.visibility = View.GONE
                    if (contactList.isEmpty()) {
                        binding.noTV.visibility = View.VISIBLE
                    }
                }
            }
        }

        clickListener(from.toString())
    }

    private fun isInterstitialEnabled(): Boolean {
        return sharedPreferences.getIsDuplicateActivityInterstitialEnabled()
    }

    private fun showHighECPM(from: String) {
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
                    deleteDuplicate(from)
                }
            } else {
                showMediumECPM(from)
            }
        }
    }

    private fun showMediumECPM(from: String) {
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
                    deleteDuplicate(from)
                }
            } else {
                showAutoECPM(from)
            }
        }
    }

    private fun showAutoECPM(from: String) {
        Log.d("inter_ecpm", "showAutoECPM: ")
        val adId = AdDatabaseUtil.getAdmobInterstitialAdId(this)

        binding.adloadingscreen.isVisible = true
        AppController.splshinterstialAd.loadInterStialAd(
            this,
            adId
        ) {
            AppController.splshinterstialAd.show_Interstial_Ad(this) {
                binding.adloadingscreen.isVisible = false
                deleteDuplicate(from)
            }
        }
    }

    private fun clickListener(from: String) {
        binding.recoverBtn.setOnClickListener {

            if (Constant.userClickHomePageFirstTiem && !AppPreferences.getInstance(this).isAppPurchased && isInterstitialEnabled()) {
                Constant.userClickHomePageFirstTiem = false
                showHighECPM(from)
            } else {
                deleteDuplicate(from)
            }

        }
        binding.backIV.setOnClickListener {
            onBackPressed()
        }

    }

    private fun deleteDuplicate(from: String) {
        if (from.equals("contacts")) {
            if (selectedContacts.isEmpty()) {
                Toast.makeText(
                    this@DuplicateActivity, getString(R.string.no_item_selected),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                selectedContacts.withIndex().forEach {
                    it.value.id?.let { it1 ->
                        deleteContactById(it1)
                    }
                }
                filesDeleted(object : DataListener {
                    override fun onRecieve(any: Any) {
                        if (any as Boolean) {
                            finish()
                        }
                    }

                })
            }
        } else {
            if (deletedItems.isEmpty()) {
                Toast.makeText(
                    this@DuplicateActivity, getString(R.string.no_item_selected),
                    Toast.LENGTH_LONG
                ).show()
            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val selectedDuplicateFiles = mutableListOf<File?>()
                    for (i in deletedItems) {
                        selectedDuplicateFiles.add(i.getFile())
                    }
                    if (from.equals("images")) {
                        getUri(
                            selectedDuplicateFiles,
                            MediaStore.Images.Media.getContentUri("external")
                        )
                    } else if (from.equals("videos")) {
                        getUri(
                            selectedDuplicateFiles,
                            MediaStore.Video.Media.getContentUri("external")
                        )
                    } else if (from.equals("audios")) {
                        getUri(
                            selectedDuplicateFiles,
                            MediaStore.Audio.Media.getContentUri("external")
                        )
                    }
//                        withContext(Dispatchers.Main){
//                            waitDialog?.dismiss()
//                        }
                    requestDeletePermission(uriList)
                } else {
                    var isDeleted = true
                    for (i in deletedItems) {
                        if (i.getFile().exists()) {
                            if (!i.getFile().delete()) {
                                isDeleted = false
                                break
                            }
                        }
                    }
                    if (isDeleted) {
                        deletedItems.clear()
                        MediaScanner(this@DuplicateActivity)
                        filesDeleted(object : DataListener {
                            override fun onRecieve(any: Any) {
                                if (any as Boolean) {
                                    finish()
                                }
                            }

                        })
                    }
                }

            }
        }
    }

    private fun getUri(list: MutableList<File?>, uri: Uri) {
        list.forEach {
            it?.let {
                val mediaId = getFilePathToMediaID(it.path)
                val uri = ContentUris.withAppendedId(uri, mediaId)
                uriList.add(uri)
            }
        }
    }

    fun getFilePathToMediaID(songPath: String): Long {
        var id: Long = 0
        val cr: ContentResolver? = contentResolver
        val uri = MediaStore.Files.getContentUri("external")
        val selection = MediaStore.Audio.Media.DATA
        val selectionArgs = arrayOf(songPath)
        val projection = arrayOf(MediaStore.Audio.Media._ID)
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor: Cursor? = cr?.query(uri, projection, "$selection=?", selectionArgs, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val idIndex: Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                id = cursor.getString(idIndex).toLong()
            }
        }
        cursor?.close()
        return id
    }

    private fun requestDeletePermission(uriList: ArrayList<Uri>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pi = contentResolver?.let { MediaStore.createDeleteRequest(it, uriList) }

            pi?.let {
                val intent: IntentSenderRequest =
                    IntentSenderRequest.Builder(pi.intentSender).setFillInIntent(null)
                        .setFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        ).build()
                deleteRequest.launch(intent)
            }
        }
    }

    private val deleteRequest =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { it ->
            Log.e("deleteResult", it.toString())
            if (it.resultCode == Activity.RESULT_OK) {

                filesDeleted(object : DataListener {
                    override fun onRecieve(any: Any) {
                        if (any as Boolean) {
                            finish()
                        }
                    }

                })
            } else {
                uriList.clear()
            }
        }

    fun deleteContactById(id: String) {
        val cr = contentResolver
        val cur = cr?.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        cur?.let {
            try {
                if (it.moveToFirst()) {
                    do {
                        if (cur.getString(
                                cur.getColumnIndex(ContactsContract.PhoneLookup._ID) ?: 0
                            ) == id
                        ) {
                            val lookupKey =
                                cur.getString(
                                    cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY) ?: 0
                                )
                            val uri =
                                Uri.withAppendedPath(
                                    ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                                    lookupKey
                                )
                            cr.delete(uri, null, null)
                            break
                        }
                    } while (it.moveToNext())
                }

            } catch (e: Exception) {
                println(e.stackTrace)
            } finally {
                it.close()
            }
        }
    }
}