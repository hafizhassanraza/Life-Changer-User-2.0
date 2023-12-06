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
    private var startFormattedDate: String? = null
    private var endFormattedDate: String? = null

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

//        binding.llStartDate.setOnClickListener {
//            showDateTimePicker(true)
//        }
//
//        binding.llEndDate.setOnClickListener {
//            showDateTimePicker(false)
//        }
//
//        binding.getInvestment.setOnClickListener {
//
//            if (startFormattedDate != null && endFormattedDate != null) {
//
//                val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm:ss a 'UTC'Z", Locale.getDefault())
//                val startDate = dateFormat.parse(startFormattedDate)
//                val endDate = dateFormat.parse(endFormattedDate)
//
//
//                if (startDate != null && endDate != null && startDate.before(endDate)) {
//
//
//                    getDataByDate(startFormattedDate!!, endFormattedDate!!)
//                } else {
//                    Toast.makeText(this, "Invalid date range", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(this, "Select start and end dates", Toast.LENGTH_SHORT).show()
//            }
//        }


    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showDateTimePicker(isStartDate: Boolean) {
        val currentYear = selectedCalendar.get(Calendar.YEAR)
        val currentMonth = selectedCalendar.get(Calendar.MONTH)
        val currentDay = selectedCalendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedCalendar.set(year, month, day)
                showTimePicker(isStartDate)
            },
            currentYear,
            currentMonth,
            currentDay
        ).show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showTimePicker(isStartDate: Boolean) {
        val currentHour = selectedCalendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = selectedCalendar.get(Calendar.MINUTE)

        TimePickerDialog(
            this,
            { _, hour, minute ->
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hour)
                selectedCalendar.set(Calendar.MINUTE, minute)

                val dateFormat = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
                val selectedDate = dateFormat.format(selectedCalendar.time)

                // Format the selected date in "MMMM dd, yyyy 'at' hh:mm:ss a 'UTC'Z"
                val fullDateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm:ss a 'UTC'Z", Locale.getDefault())
                val formattedDate = fullDateFormat.format(selectedCalendar.time)

                if (isStartDate) {
                    // Handle logic for the selected start date
                    startFormattedDate = formattedDate
                    binding.startDate.text = selectedDate
                } else {
                    // Handle logic for the selected end date
                    endFormattedDate = formattedDate
                    binding.endDate.text = selectedDate
                }

                //Toast.makeText(this, "Selected Date-Time: $formattedDate", Toast.LENGTH_SHORT).show()
                //Toast.makeText(this, "Selected Date: $selectedDate", Toast.LENGTH_SHORT).show()
            },
            currentHour,
            currentMinute,
            false
        ).show()
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

                              //
    private fun getDataByDate(startTimestamp:String, endTimestamp:String){

                                  //Toast.makeText(mContext, ""+startTimestamp, Toast.LENGTH_SHORT).show()
                                  //Toast.makeText(mContext, ""+endTimestamp, Toast.LENGTH_SHORT).show()

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