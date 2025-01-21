package com.example.datarecoverynew.views.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.datarecoverynew.BuildConfig
import com.example.datarecoverynew.R
import com.example.datarecoverynew.base.BaseActivity
import com.example.datarecoverynew.databinding.ActivityPermissionScreenBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File

class PermissionScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityPermissionScreenBinding
    private var isAllGranted = false
    private var act: Intent? = null

    companion object {
        private const val APP_STORAGE_ACCESS_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.getStringExtra("for")

        act = when (bundle) {
            "Images" -> {
                Intent(this, ScanImagesActivity::class.java)
            }

            "Videos" -> {
                Intent(this, ScanVideosActivity::class.java)
            }

            "Audios" -> {
                Intent(this, ScanAudiosActivity::class.java)
            }

            "Files" -> {
                Intent(this, ScanFilesActivity::class.java)
            }

            "SavedImages" -> {
                Intent(this, SavedImagesActivity::class.java)
            }

            "SavedVideos" -> {
                Intent(this, SavedVediosActivity::class.java)
            }

            "SavedAudios" -> {
                Intent(this, SavedAudiosActivity::class.java)
            }

            "SavedFiles" -> {
                Intent(this, SavedFilesActivity::class.java)
            }

            "DuplicateImages" -> {
                val intnt = Intent(this, DuplicateActivity::class.java)
                intnt.putExtra("from", "images")
                intnt
            }

            "DuplicateVideos" -> {
                val intnt = Intent(this, DuplicateActivity::class.java)
                intnt.putExtra("from", "videos")
                intnt
            }

            "DuplicateAudios" -> {
                val intnt = Intent(this, DuplicateActivity::class.java)
                intnt.putExtra("from", "audios")
                intnt
            }

            "DuplicateFiles" -> {
                val intnt = Intent(this, DuplicateActivity::class.java)
                intnt.putExtra("from", "contacts")
                intnt
            }

            else -> null
        }

        // Set the initial state of the switch for "All Files Access"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.btnFilesSwitch.isChecked = Environment.isExternalStorageManager()
        } else {
            binding.btnFilesSwitch.isChecked = true
        }

        binding.btnSwitch.isChecked = isPermissionGranted()

        binding.btnSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Check and request required permissions
                checkAndRequestPermissions()
            }
        }
        binding.btnFilesSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                callAllFiles()
            }
        }

        if (isPermissionGranted() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            createFolderAndProceed()
        }
    }

    private fun permissionDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
                    startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE)
                } catch (ex: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_STORAGE_ACCESS_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {

                } else {
                    binding.btnFilesSwitch.isChecked = false
                }
            }
        }
    }

    private fun createFolderAndProceed() {
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString() + "/Recovery"
        )
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("PermissionScreenActivity", "Failed to create directory")
                return
            }
        }
        // Proceed to SplashActivity
        startActivity(act)
        finish()
    }

    private fun isPermissionGranted(): Boolean {
        val permissionsNeeded = getPermissionsNeeded()

        val permissionsToRequest = permissionsNeeded.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        return permissionsToRequest.isEmpty()
    }


    private fun checkAndRequestPermissions() {
        val permissionsNeeded = getPermissionsNeeded()
        for (permission in permissionsNeeded) {
            Log.d("lll", permission)
        }
        if (permissionsNeeded.isNotEmpty()) {
            Dexter.withContext(this)
                .withPermissions(permissionsNeeded)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            binding.btnSwitch.isChecked = true
                            createFolderAndProceed()
                        } else {
                            binding.btnSwitch.isChecked = false
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        binding.btnSwitch.setOnCheckedChangeListener { compoundButton, b ->
                            if (b) token?.continuePermissionRequest()
                        }
                    }
                }).check()
        } else {
            binding.btnSwitch.isChecked = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) createFolderAndProceed()
        }
    }

    private fun getPermissionsNeeded(): List<String> {
        val permissions = mutableListOf<String>()

        // Contact permissions
        permissions.add(Manifest.permission.READ_CONTACTS)
        permissions.add(Manifest.permission.WRITE_CONTACTS)

        // Adjust storage permissions based on Android version
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ (API 33): Use granular media permissions
                permissions.remove(Manifest.permission.WRITE_CONTACTS)
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            else -> {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        return permissions
    }


    private fun callAllFiles() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            permissionDialog()
        } else {
            isAllGranted = true
            binding.btnFilesSwitch.isChecked = true
            createFolderAndProceed()
        }
    }
}
