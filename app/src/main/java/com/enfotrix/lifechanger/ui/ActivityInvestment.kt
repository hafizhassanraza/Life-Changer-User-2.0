package com.enfotrix.lifechanger.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.enfotrix.lifechanger.Adapters.InvestmentViewPagerAdapter
import com.enfotrix.lifechanger.Adapters.WithdrawViewPagerAdapter
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.InvestmentViewModel
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.ActivityInvestmentBinding
import com.enfotrix.lifechanger.databinding.ActivityWithdrawBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class ActivityInvestment : AppCompatActivity() {

    private lateinit var binding: ActivityInvestmentBinding
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



    @RequiresApi(Build.VERSION_CODES.N)
    private val startCalendar = Calendar.getInstance()
    @RequiresApi(Build.VERSION_CODES.N)
    private val endCalendar = Calendar.getInstance()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext=this@ActivityInvestment
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        setTitle("My Investments")

        getData()

        selectedCalendar = Calendar.getInstance()

        binding.llStartDate.setOnClickListener {
            showDatePickerDialog(true)
        }

        binding.llEndDate.setOnClickListener {
            showDatePickerDialog(false)
        }

        binding.getInvestment.setOnClickListener {

            if (startFormattedDate != null && endFormattedDate != null) {

                val date1 = dateFormat.parse(startFormattedDate)
                val startDate = Timestamp(date1)

                 val date2 = dateFormat.parse(endFormattedDate)
                val endDate = Timestamp(date2)



                getDataByDate(startDate!!, endDate!!)


            } else {
                Toast.makeText(this, "Select start and end dates", Toast.LENGTH_SHORT).show()
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = if (isStartDate) startCalendar else endCalendar
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                calendar.set(year, monthOfYear, dayOfMonth)
                updateButtonText(isStartDate)
            },
            currentYear,
            currentMonth,
            currentDay
        ).show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun updateButtonText(isStartDate: Boolean) {
        val calendar = if (isStartDate) startCalendar else endCalendar
        val selectedDate = dateFormat.format(calendar.time)

        if (isStartDate) {
            binding.startDate.text = selectedDate
            startFormattedDate = selectedDate
        } else {
            binding.endDate.text = selectedDate
            endFormattedDate = selectedDate
        }
        Toast.makeText(mContext, ""+startCalendar, Toast.LENGTH_SHORT).show()
    }

    private fun setupTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if(position==0) tab.text ="Approved"
            else if(position==1) tab.text="Pending"
        }.attach()
    }

    private fun setupViewPager() {
                  val adapter = InvestmentViewPagerAdapter(this, 2)
        binding.viewPager.adapter = adapter
    }

    private fun getDataByDate(startTimestamp:Timestamp, endTimestamp:Timestamp){

                                  Toast.makeText(mContext, ""+startTimestamp, Toast.LENGTH_SHORT).show()
                                  Toast.makeText(mContext, ""+endTimestamp, Toast.LENGTH_SHORT).show()

        utils.startLoadingAnimation()
        lifecycleScope.launch{
            investmentViewModel.getInvestmentReqByDates(sharedPrefManager.getToken(), startTimestamp, endTimestamp)
                .addOnCompleteListener{task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        val list = ArrayList<TransactionModel>()
                        if(task.result.size()>0){
                            for (document in task.result) list.add( document.toObject(TransactionModel::class.java))
                            Toast.makeText(mContext, "List size"+list.size, Toast.LENGTH_SHORT).show()
                            sharedPrefManager.putInvestmentReqList(list)

                            setupViewPager()
                            setupTabLayout()
                        }
                    }
                    else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()



                }
                .addOnFailureListener{
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                }

        }
    }

    private fun getData(){
        utils.startLoadingAnimation()
        lifecycleScope.launch{
            investmentViewModel.getInvestmentReq(sharedPrefManager.getToken())
                .addOnCompleteListener{task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        val list = ArrayList<TransactionModel>()
                        if(task.result.size()>0){
                            for (document in task.result) list.add( document.toObject(TransactionModel::class.java))
                            sharedPrefManager.putInvestmentReqList(list)

                            setupViewPager()
                            setupTabLayout()
                        }
                    }
                    else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()



                }
                .addOnFailureListener{
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                }

        }
    }






    override fun onBackPressed() {
        val viewPager = binding.viewPager
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }



}