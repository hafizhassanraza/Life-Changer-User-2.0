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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.InvestmentViewModel
import com.enfotrix.life_changer_user_2_0.Models.TransactionModel
import com.enfotrix.life_changer_user_2_0.Pdf.PdfEstatmentHistory
import com.enfotrix.life_changer_user_2_0.Pdf.PdfTaxHistory
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityStatmentBinding
import com.enfotrix.life_changer_user_2_0.databinding.DialogDatepickerBinding
import java.util.Locale

class ActivityStatment : AppCompatActivity() {



    private lateinit var binding: ActivityStatmentBinding
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager
    private var eStatmentList: List<TransactionModel>? = null
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
        binding.pdfEstatment.setOnClickListener{
            dialogWithdrawDetails()
        }

        binding.rvInvestments.layoutManager = LinearLayoutManager(mContext)
        binding.rvInvestments.adapter= investmentViewModel.getStatmentAdapter()



    }


    private fun dialogWithdrawDetails() {
        val dialogBinding = DialogDatepickerBinding.inflate(LayoutInflater.from(mContext))
        val dialogDatepicker = Dialog(mContext)
        dialogDatepicker.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogDatepicker.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogDatepicker.setContentView(dialogBinding.root)
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
            filterEstatmentList(selectedYear, selectedMonth)
            dialogDatepicker.dismiss()
        }
        dialogDatepicker.show()
    }
    private fun filterEstatmentList(year: Int, month: Int) {
        eStatmentList = sharedPrefManager.getTransactionList().filter{ it.status.equals(constants.TRANSACTION_STATUS_APPROVED) }.sortedByDescending { it.createdAt }
            .filter { earning ->
            val taxDate = earning.createdAt
            if (taxDate != null) {
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = taxDate.seconds * 1000
                return@filter calendar.get(java.util.Calendar.YEAR) == year && calendar.get(java.util.Calendar.MONTH) + 1 == month
            }
            false
        }

        if (eStatmentList!!.isEmpty()) {
            Toast.makeText(mContext, "No data found", Toast.LENGTH_SHORT).show()
        } else {
            generatePDF()
        }
    }


    private fun generatePDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Statement.pdf")
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
                        eStatmentList?.let {
                            PdfEstatmentHistory(it, sharedPrefManager.getUser()!!.firstName).generatePdf(
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





}