package com.example.datarecoverynew.views.fragments.howToUse

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.datarecoverynew.AppController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.databinding.FragmentAdBinding
import com.example.datarecoverynew.utils.AdDatabaseUtil
import com.example.datarecoverynew.utils.SharedPrefsHelper

class AdFragment : Fragment() {
    private val binding by lazy { FragmentAdBinding.inflate(layoutInflater) }
    private val sharedPrefsHelper by lazy { SharedPrefsHelper(requireContext()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.navigation_ad_to_four)
        }

        if (isAdsEnabled()) {
            showNativeAd()
        } else {
            binding.btnNext.visibility = View.VISIBLE
            binding.progress.visibility = View.GONE
        }

    }

    private fun showNativeAd() {
        val adId = AdDatabaseUtil.getAdmobNativeAdId(requireContext())
        if (adId.isNotEmpty()) {
            AppController.nativeAdRef.loadNativeAd(
                requireActivity(),
                binding.fragmeNative,
                false,
                adId
            ) {
                binding.btnNext.visibility = View.VISIBLE
                binding.progress.visibility = View.GONE
            }
        }
    }

    private fun isAdsEnabled(): Boolean {
        return sharedPrefsHelper.getIsNativeUseEnabled()
    }
}