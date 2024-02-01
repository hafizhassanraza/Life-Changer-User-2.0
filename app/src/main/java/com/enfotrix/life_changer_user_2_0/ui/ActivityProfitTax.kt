package com.enfotrix.life_changer_user_2_0.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.InvestmentViewModel
import com.enfotrix.life_changer_user_2_0.Models.TransactionModel
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.Pdf.PdfProfitHistory
import com.enfotrix.life_changer_user_2_0.Pdf.PdfTaxHistory
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityProfitTaxBinding
import com.enfotrix.life_changer_user_2_0.databinding.DialogDatepickerBinding
import java.util.Calendar

class ActivityProfitTax : AppCompatActivity() {

    private val CREATE_PDF_REQUEST_CODE = 123

    private lateinit var binding: ActivityProfitTaxBinding
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private val sharedPrefManager: SharedPrefManager by lazy { SharedPrefManager(this) }

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private var filteredEarnings: List<TransactionModel>? = null
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfitTaxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this@ActivityProfitTax
        utils = Utils(mContext)
        constants = Constants()

        binding.rvProfit.layoutManager = LinearLayoutManager(mContext)
        binding.rvProfit.adapter = investmentViewModel.getProfitAdapter(constants.FROM_PROFIT)

        binding.pdf.setOnClickListener {
            dialogWithdrawDetails()
        }
        binding.imgBack.setOnClickListener { finish() }
    }

    private fun dialogWithdrawDetails() {
        val dialogBinding = DialogDatepickerBinding.inflate(LayoutInflater.from(mContext))
        val dialogDatepicker = Dialog(mContext)
        dialogDatepicker.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogDatepicker.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogDatepicker.setContentView(dialogBinding.root)
        val yearPicker: NumberPicker = dialogBinding.yearPicker
        val monthPicker: NumberPicker = dialogBinding.monthPicker
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = 2024
        yearPicker.maxValue = 2050
        yearPicker.value = 2024
        val monthNames = resources.getStringArray(R.array.months)
        val monthValues = (1..12).toList().toIntArray()
        monthPicker.minValue = 0
        monthPicker.maxValue = monthNames.size - 1
        monthPicker.displayedValues = monthNames
        monthPicker.setFormatter { index -> monthNames[index] }
        monthPicker.value = Calendar.JULY - 1
        dialogBinding.btnDownload.setOnClickListener {
            val selectedYear = yearPicker.value
            val selectedMonth = monthValues[monthPicker.value]
            filterWithdrawList(selectedYear, selectedMonth)
            dialogDatepicker.dismiss()
        }
        dialogDatepicker.show()
    }

    private fun filterWithdrawList(year: Int, month: Int) {
    filteredEarnings = sharedPrefManager.getProfitList().filter { earning ->
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

    private fun generatePDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "profit_details.pdf")
        }
        startActivityForResult(intent, CREATE_PDF_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val outputStream = mContext.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val success = PdfProfitHistory(
                        filteredEarnings,
                        sharedPrefManager.getUser()?.firstName ?: ""
                    ).generatePdf(outputStream)
                    outputStream.close()
                    if (success) {
                        Toast.makeText(mContext, "Saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(mContext, "Failed to save", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
