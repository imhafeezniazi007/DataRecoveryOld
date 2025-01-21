package com.example.datarecoverynew.views.fragments.howToUse

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.datarecoverynew.R
import com.example.datarecoverynew.databinding.FragmentOneBinding
import com.example.datarecoverynew.databinding.FragmentTwoBinding
import com.example.datarecoverynew.utils.SharedPrefsHelper

class FragmentTwo : Fragment() {
    private val binding by lazy { FragmentTwoBinding.inflate(layoutInflater) }
    private val sharedPrefsHelper by lazy { SharedPrefsHelper(requireContext()) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNext.setOnClickListener {
            if (isAdsEnabled()) {
                findNavController().navigate(R.id.navigation_two_to_ad)
            } else {
                findNavController().navigate(R.id.navigation_two_to_three)
            }
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun isAdsEnabled(): Boolean {
        return sharedPrefsHelper.getIsNativeUseEnabled()
    }

}