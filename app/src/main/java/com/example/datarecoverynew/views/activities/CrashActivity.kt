package com.example.datarecoverynew.views.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.datarecoverynew.databinding.ActivityCrashBinding
import com.example.datarecoverynew.utils.ExceptionHandler

class CrashActivity : AppCompatActivity() {
    lateinit var binding: ActivityCrashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ExceptionHandler.getThrowableFromIntent(intent).let {
            binding.msgTV.text = it?.message
        }
        binding.okBtn.setOnClickListener {
            finishAffinity()
        }
    }
}