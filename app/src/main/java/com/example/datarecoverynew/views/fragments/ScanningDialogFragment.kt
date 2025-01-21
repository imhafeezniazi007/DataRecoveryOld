package com.example.datarecoverynew.views.fragments

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.databinding.FragmentScanningDialogBinding
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.datarecoverynew.utils.SharedPrefsHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ScanningDialogFragment : BottomSheetDialogFragment() {

    private val binding by lazy { FragmentScanningDialogBinding.inflate(layoutInflater) }
    private val sharedPrefsHelper by lazy { SharedPrefsHelper(requireContext()) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { its ->
                val behaviour = BottomSheetBehavior.from(its)
                setupFullHeight(its)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }


    private fun setupFullHeight(bottomSheet: View) {
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams

        val windowHeight = getWindowHeight()
        if (layoutParams != null) {
            layoutParams.height = windowHeight
        }
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels

        bottomSheetBehavior.peekHeight = screenHeight // Hide the peek height
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        if (isNativeHomeEnabled()) {
            showNativeAd(binding.frameNativeProgress)
        } else {
            binding.frameNativeProgress.visibility = View.GONE
        }

    }

    private fun isNativeHomeEnabled(): Boolean {
        return sharedPrefsHelper.getIsNativeScanningDialogEnabled()
    }

    private fun showNativeAd(nativeFrame: FrameLayout) {
        val adId = AdDatabaseUtil.getAdmobNativeAdId(requireContext())
        if (adId.isNotEmpty()) {
            AppController.nativeAdRef.loadNativeAd(
                requireActivity(), nativeFrame, false, adId
            ) {}
        }
    }
//
//    override fun getTheme(): Int {
//        return R.style.FullScreenBottomSheetTheme
//    }
//
}