package com.enfotrix.life_changer_user_2_0.ui

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.InvestmentViewModel
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityStatmentBinding
import java.util.Locale

class ActivityStatment : AppCompatActivity() {



    private lateinit var binding: ActivityStatmentBinding
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager

    private lateinit var selectedCalendar: Calendar
    private var startFormattedDate = ""
    private var endFormattedDate= ""
    @RequiresApi(Build.VERSION_CODES.N)
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext=this@ActivityStatment
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)



        binding.imgBack.setOnClickListener{finish()}

        binding.rvInvestments.layoutManager = LinearLayoutManager(mContext)
        binding.rvInvestments.adapter= investmentViewModel.getStatmentAdapter()


    }
}