package com.example.datarecoverynew.views.fragments.howToUse

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.datarecoverynew.R
import com.example.datarecoverynew.databinding.FragmentFiveBinding
import com.example.datarecoverynew.utils.SharedPrefsHelper
import com.example.datarecoverynew.views.activities.MainActivity
import com.example.datarecoverynew.views.activities.PremiumActivity
import com.example.datarecoverynew.views.activities.SelectLanguagesActivity

class FragmentFive : Fragment() {
    private val binding by lazy { FragmentFiveBinding.inflate(layoutInflater) }
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

        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.btnNext.setOnClickListener {
            if (isPremiumHowToUseEnabled()) {
                startActivity(Intent(requireActivity(), PremiumActivity::class.java))
                requireActivity().finish()
            } else {
                startActivity(Intent(requireActivity(), MainActivity::class.java))
                requireActivity().finish()
            }
        }

    }

    private fun isPremiumHowToUseEnabled(): Boolean {
        return sharedPrefsHelper.getIsPremiumHowToUseEnabled()
    }
}