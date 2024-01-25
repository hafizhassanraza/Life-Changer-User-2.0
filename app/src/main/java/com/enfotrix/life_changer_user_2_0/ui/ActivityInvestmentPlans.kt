package com.enfotrix.life_changer_user_2_0.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.enfotrix.life_changer_user_2_0.databinding.ActivityInvestmentPlansBinding

class ActivityInvestmentPlans : AppCompatActivity() {
    private lateinit var binding: ActivityInvestmentPlansBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityInvestmentPlansBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backImg.setOnClickListener {
            super.onBackPressed()
        }
    }
}