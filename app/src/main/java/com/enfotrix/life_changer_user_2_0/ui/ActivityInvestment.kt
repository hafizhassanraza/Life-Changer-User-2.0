package com.enfotrix.life_changer_user_2_0.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.InvestmentViewModel
import com.enfotrix.life_changer_user_2_0.Models.TransactionModel
import com.enfotrix.life_changer_user_2_0.Pdf.PdfAllTransactions
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityInvestmentBinding
import com.enfotrix.life_changer_user_2_0.databinding.DialogDatepickerBinding
import java.util.Locale


class ActivityInvestment : AppCompatActivity() {

    private lateinit var binding: ActivityInvestmentBinding
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private var filteredEarnings: List<TransactionModel>? = null


    private lateinit var selectedCalendar: Calendar
    private var startFormattedDate = ""
    private var Position = 1
    private var endFormattedDate = ""

    @RequiresApi(Build.VERSION_CODES.N)
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this@ActivityInvestment
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        /*setTitle("My Investments")
           */
        /*selectedCalendar = Calendar.getInstance()

        binding.llStartDate.setOnClickListener {
            showDatePickerDialog(true)
        }

        binding.llEndDate.setOnClickListener {
            showDatePickerDialog(false)
        }*/

        /*binding.getInvestment.setOnClickListener {

            if (startFormattedDate != null && endFormattedDate != null) {

                val date1 = dateFormat.parse(startFormattedDate)
                val startDate = Timestamp(date1)

                 val date2 = dateFormat.parse(endFormattedDate)
                val endDate = Timestamp(date2)



                getDataByDate(startDate!!, endDate!!)


            } else {
                Toast.makeText(this, "Select start and end dates", Toast.LENGTH_SHORT).show()
            }
        }*/


        binding.imgBack.setOnClickListener { finish() }

        val spinner = binding.spInvestments

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.investment_options, // Replace with your array of items
            R.layout.item_investment_selection_spiner // Use the custom layout
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        binding.rvInvestments.layoutManager = LinearLayoutManager(mContext)

        binding.spInvestments.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // Show a Toast message when an item is selected
                //val selectedItem = parentView?.getItemAtPosition(position).toString()

                // Check the index and show a different message for 1st and 2nd index
                when (position) {
                    0 -> {
                        Position = 1
                        binding.rvInvestments.adapter =
                            investmentViewModel.getApprovedInvestmentReqAdapter(constants.FROM_APPROVED_INVESTMENT_REQ)
                    }

                    1 -> {
                        Position = 2
                        binding.rvInvestments.adapter =
                            investmentViewModel.getPendingInvestmentReqAdapter(constants.FROM_PENDING_INVESTMENT_REQ)
                    }
                }


            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing if nothing is selected
            }
        }

        binding.btnPdfDialog.setOnClickListener {
            dialogWithdrawDetails()
        }


    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun dialogWithdrawDetails() {
        val dialogBinding = DialogDatepickerBinding.inflate(LayoutInflater.from(mContext))
        val DialogDatepicker = Dialog(mContext)
        DialogDatepicker.requestWindowFeature(Window.FEATURE_NO_TITLE)
        DialogDatepicker.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        DialogDatepicker.setContentView(dialogBinding.root)
        val yearPicker: NumberPicker = dialogBinding.yearPicker
        val monthPicker: NumberPicker = dialogBinding.monthPicker

        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        yearPicker.minValue = 2024
        yearPicker.maxValue = 2050
        yearPicker.value = 2024

        val monthNames = resources.getStringArray(R.array.months)
        val monthValues = (1..12).toList().toIntArray()
        monthPicker.minValue = 0
        monthPicker.maxValue = monthNames.size - 1
        monthPicker.displayedValues = monthNames
        monthPicker.setFormatter { index -> monthNames[index] }
        monthPicker.value = java.util.Calendar.JULY - 1

        dialogBinding.btnDownload.setOnClickListener {
            val selectedYear = yearPicker.value
            val selectedMonth = monthValues[monthPicker.value]
            filterwithdrawList(selectedYear, selectedMonth)
            DialogDatepicker.dismiss()
        }

        DialogDatepicker.show()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun filterwithdrawList(year: Int, month: Int) {
        when {
            Position == 1 -> {
                val listApproved = sharedPrefManager.getInvestmentReqList()
                    .filter { it.status == constants.TRANSACTION_STATUS_APPROVED }
                    .sortedByDescending { it.createdAt }
                 filteredEarnings = listApproved.filter { earning ->
                    val withdrawApprovedDate = earning.transactionAt
                    if (withdrawApprovedDate != null) {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = withdrawApprovedDate.seconds * 1000
                        return@filter calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) + 1 == month
                    }
                    false
                }

                if (filteredEarnings!!.isEmpty()) {
                    Toast.makeText(mContext, "No data found", Toast.LENGTH_SHORT).show()
                } else {
                    generatePDF()
                }
            }

            Position == 2 -> {
                val listApproved = sharedPrefManager.getInvestmentReqList()
                    .filter { it.status == constants.TRANSACTION_STATUS_PENDING }
                    .sortedByDescending { it.createdAt }
                filteredEarnings = listApproved.filter { earning ->
                    val withdrawApprovedDate = earning.createdAt
                    if (withdrawApprovedDate != null) {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = withdrawApprovedDate.seconds * 1000
                        return@filter calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) + 1 == month
                    }
                    false
                }

                if (filteredEarnings!!.isEmpty()) {
                    Toast.makeText(mContext, "No data found", Toast.LENGTH_SHORT).show()
                } else {
                    generatePDF()
                }
            }

        }
    }


    private fun generatePDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Investment History.pdf")
        }
        startActivityForResult(intent, 123)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val outputStream = mContext.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val success =
                        filteredEarnings?.let {
                            PdfAllTransactions(
                                it,
                                sharedPrefManager.getUser()!!.firstName).generatePdf(
                                outputStream
                            )
                        }
                    outputStream.close()
                    if (success == true) {
                        Toast.makeText(mContext, "Saved successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(mContext, "Failed to save", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }


    /*

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
    */


}