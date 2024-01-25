package com.enfotrix.life_changer_user_2_0.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.enfotrix.life_changer_user_2_0.databinding.ActivityAboutUsBinding

class ActivityAboutUs : AppCompatActivity() {
    private lateinit var binding: ActivityAboutUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.backImg.setOnClickListener {
            super.onBackPressed()
        }
    }
}