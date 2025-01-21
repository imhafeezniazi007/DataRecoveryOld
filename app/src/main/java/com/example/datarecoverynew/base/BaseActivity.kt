package com.example.datarecoverynew.base

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.akexorcist.localizationactivity.core.LocalizationActivityDelegate
import com.akexorcist.localizationactivity.core.OnLocaleChangedListener
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.datarecoverynew.utils.AppPreferences
import com.example.datarecoverynew.utils.SharedPrefsHelper
import com.example.datarecoverynew.utils.ThemesUtils
import com.example.datarecoverynew.views.fragments.DuplicateScanningFragment
import com.example.datarecoverynew.views.fragments.ScanningDialogFragment
import com.example.recoverydata.interfaces.DataListener
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException


open class BaseActivity : LocalizationActivity(), OnLocaleChangedListener {
    private val localizationDelegate = LocalizationActivityDelegate(this)
    private val sharedPrefsHelper by lazy { SharedPrefsHelper(this) }


    val APP_STORAGE_ACCESS_REQUEST_CODE = 501
    var PERMISSIONS_REQUEST = 1002
    private var loadingDialog: ScanningDialogFragment? = null
    private var loadingDialogDuplicate: DuplicateScanningFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        localizationDelegate.addOnLocaleChangedListener(this)
        localizationDelegate.onCreate()
        super.onCreate(savedInstanceState)

        ThemesUtils.onActivityCreateSetTheme(this)
    }

    fun copy(src: File): Boolean {
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString() + "/Recovery"
        )
        val dst = File(
            mediaStorageDir.toString()
        )
        try {
            File(src.path).copyTo(File(dst, src.name), true)
            return true

        } catch (ex: NoSuchFileException) {
            Log.d("TAG", "copy: ${ex.message}")
            return false
        } catch (ex: IOException) {
            Log.d("TAG", "copy: ${ex.message}")
            return false
        }

    }

    fun showSnackbar() {
        val snack = Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.snackbar_text),
            Snackbar.LENGTH_INDEFINITE
        )

        val params = snack.view as Snackbar.SnackbarLayout
        params.minimumHeight = 150
        val view = snack.view
        val params1 = view.layoutParams as FrameLayout.LayoutParams
        params1.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        snack.setAction(getString(R.string.ok)) { v: View? ->
            snack.dismiss()
        }
        snack.show()
    }

    fun closeKeyboard() {
        val inputManager =
            applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager != null && this.currentFocus != null) inputManager.hideSoftInputFromWindow(
            this.currentFocus!!.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN
        )
    }

    fun completeScanningDialog(activity: Activity, recoverable: Int, total: Int) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.complete_dialog, null)
        builder.setView(view)
        builder.setCancelable(true)
        imageDialog = builder.create()
        imageDialog.setCancelable(true)
        val ok = view.findViewById<AppCompatButton>(R.id.btn)
        val recoverableTv = view.findViewById<TextView>(R.id.recoverableTv)
        val totalTv = view.findViewById<TextView>(R.id.totalTv)
        val logoIV = view.findViewById<ImageView>(R.id.logoIV)
        val theme = AppPreferences.getInstance(activity).getTheme
        if (theme == 2) {
            logoIV.setImageResource(R.drawable.ic_complete_theme_1)
        } else if (theme == 3) {
            logoIV.setImageResource(R.drawable.ic_complete_theme_2)
        } else if (theme == 4) {
            logoIV.setImageResource(R.drawable.ic_complete_theme_3)
        } else if (theme == 5) {
            logoIV.setImageResource(R.drawable.ic_complete_theme_4)
        } else if (theme == 6) {
            logoIV.setImageResource(R.drawable.ic_complete_theme_5)
        } else {
            logoIV.setImageResource(R.drawable.ic_scanned)
        }
        totalTv.text = total.toString() + " " + getString(R.string.total_files_scanned)

        recoverableTv.text =
            recoverable.toString() + " " + getString(R.string.recoverable_files_found)

        ok.setOnClickListener {
            imageDialog.dismiss()
        }
        imageDialog.window!!.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        if (!activity.isFinishing) {
            //show dialog
            imageDialog.show()
        }
    }

    fun watchADDialog(activity: Activity, dataListener: DataListener) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.watch_ad_dialog, null)
        builder.setView(view)
        builder.setCancelable(true)
        imageDialog = builder.create()
        imageDialog.setCancelable(true)
        val ok = view.findViewById<ConstraintLayout>(R.id.btn)
        val logoIV = view.findViewById<ImageView>(R.id.logoIV)
        val theme = AppPreferences.getInstance(activity).getTheme
        if (theme == 2) {
            logoIV.setImageResource(R.drawable.watch_ad_theme_1)
        } else if (theme == 3) {
            logoIV.setImageResource(R.drawable.watchad)
        } else if (theme == 4) {
            logoIV.setImageResource(R.drawable.watch_ad_theme_3)
        } else if (theme == 5) {
            logoIV.setImageResource(R.drawable.watchad)
        } else if (theme == 6) {
            logoIV.setImageResource(R.drawable.watchad)
        } else {
            logoIV.setImageResource(R.drawable.watchad)
        }

        logoIV.setOnClickListener {
            imageDialog.dismiss()
            dataListener.onRecieve(true)
        }
        ok.setOnClickListener {
            imageDialog.dismiss()
            dataListener.onPremium(true)
        }
        imageDialog.window!!.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        if (!activity.isFinishing) {
            //show dialog
            imageDialog.show()
        }
    }

    fun showHideProgress(show: Boolean) {
        closeKeyboard()
//        if (show) {
//            if (loadingDialog != null && loadingDialog!!.isShowing()) {
//                return
//            }
//            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
//            val inflater = layoutInflater
//            val view: View = inflater.inflate(R.layout.loading_dialog, null)
//            val nativeFrame = view.findViewById<FrameLayout>(R.id.frameNativeProgress)
//            builder.setView(view)
//            builder.setCancelable(false)
//            loadingDialog = builder.create()
//            loadingDialog!!.show()
//            if (isNativeHomeEnabled()) {
//                showNativeAd(nativeFrame)
//            } else {
//                nativeFrame.visibility = View.GONE
//            }
//        } else {
//            if (loadingDialog != null && loadingDialog!!.isShowing()) {
//                loadingDialog!!.dismiss()
//            }
//        }
        if (show) {
            Log.e("TAG_dedede", "showHideDuplicateProgress: ")
            loadingDialog = ScanningDialogFragment()
            loadingDialog?.show(supportFragmentManager, "FullScreenBottomSheet")
        } else {
            Log.e("TAG_dedede", "null")
            loadingDialog?.dismiss()
            loadingDialog = null
        }
    }

//    private fun isNativeHomeEnabled(): Boolean {
//        return sharedPrefsHelper.getIsNativeHomeEnabled()
//    }
//
//    private fun showNativeAd(nativeFrame: FrameLayout) {
//        val adId = AdDatabaseUtil.getAdmobNativeAdId(this)
//        if (adId.isNotEmpty()) {
//            AppController.nativeAdRef.loadNativeAd(
//                this, nativeFrame, false, adId
//            ) {}
//        }
//    }

    fun showHideDuplicateProgress(show: Boolean) {
        closeKeyboard()
//        if (show) {
//            if (loadingDialog != null && loadingDialog!!.isShowing()) {
//                return
//            }
//            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
//            val inflater = layoutInflater
//            val view: View = inflater.inflate(R.layout.duplicate_loading_dialog, null)
//            val nativeFrame = view.findViewById<FrameLayout>(R.id.frameNativeProgressDuplicate)
//            builder.setView(view)
//            builder.setCancelable(false)
//            loadingDialog = builder.create()
//
//            loadingDialog!!.show()
//
//            if (isNativeHomeEnabled()) {
//                showNativeAd(nativeFrame)
//            } else {
//                nativeFrame.visibility = View.GONE
//            }
//        } else {
//            if (loadingDialog != null && loadingDialog!!.isShowing()) {
//                loadingDialog!!.dismiss()
//            }
//        }
        if (show) {
            Log.e("TAG_dedede", "showHideDuplicateProgress: ")
            loadingDialogDuplicate = DuplicateScanningFragment()
            loadingDialogDuplicate?.show(supportFragmentManager, "FullScreenBottomSheet")
        } else {
            Log.e("TAG_dedede", "null")
            loadingDialogDuplicate?.dismiss()
            loadingDialogDuplicate = null
        }
    }

    fun filesDeleted(dataListener: DataListener) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.files_deleted_layout, null)
        val ok = view.findViewById<ConstraintLayout>(R.id.okBtn)

        builder.setView(view)
        builder.setCancelable(false)
        imageDialog = builder.create()
        ok.setOnClickListener {
            dataListener.onRecieve(true)
            imageDialog.dismiss()

        }
        imageDialog!!.getWindow()!!.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        imageDialog.setCancelable(false)
        imageDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (loadingDialog != null && loadingDialog!!.isVisible) {
            loadingDialog!!.dismiss()
            loadingDialog = null
        }
    }
}