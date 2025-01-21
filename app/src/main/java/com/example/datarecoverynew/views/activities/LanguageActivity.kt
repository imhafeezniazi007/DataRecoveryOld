package com.example.datarecoverynew.views.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.datarecoverynew.base.BaseActivity
import com.example.datarecoverynew.databinding.ActivityLanguageBinding
import com.example.datarecoverynew.views.adapters.LanguagesAdapter
import com.example.recoverydata.interfaces.DataListener

class LanguageActivity : BaseActivity() {
    lateinit var binding: ActivityLanguageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    }
