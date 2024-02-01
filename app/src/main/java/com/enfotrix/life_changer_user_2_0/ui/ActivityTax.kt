package com.enfotrix.life_changer_user_2_0.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.InvestmentViewModel
import com.enfotrix.life_changer_user_2_0.Models.TransactionModel
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.Pdf.PdfTaxHistory
import com.enfotrix.life_changer_user_2_0.Pdf.PdfTransaction
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityTaxBinding
import com.enfotrix.life_changer_user_2_0.databinding.DialogDatepickerBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class ActivityTax : AppCompatActivity() {






    private val db = Firebase.firestore

    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var binding : ActivityTaxBinding

    val listTransaction = ArrayList<TransactionModel>()
    private val CREATE_PDF_REQUEST_CODE = 123

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog
    private var taxList: List<TransactionModel>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivityTax
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        binding.rvTax.layoutManager = LinearLayoutManager(mContext)
        binding.rvTax.adapter= investmentViewModel.getTaxAdapter(constants.FROM_TAX)



     binding.pdfTaxHistory.setOnClickListener {
         dialogWithdrawDetails()
     }
        binding.imgBack.setOnClickListener{finish()}


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
            filterTaxList(selectedYear, selectedMonth)
            dialogDatepicker.dismiss()
        }
        dialogDatepicker.show()
    }


    private fun filterTaxList(year: Int, month: Int) {
        taxList =sharedPrefManager.getTaxList().filter{ it.status.equals(constants.TRANSACTION_STATUS_APPROVED) }.sortedByDescending { it.createdAt }
            .filter { earning ->
            val taxDate = earning.createdAt
            if (taxDate != null) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = taxDate.seconds * 1000
                return@filter calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) + 1 == month
            }
            false
        }

        if (taxList!!.isEmpty()) {
            Toast.makeText(mContext, "No data found", Toast.LENGTH_SHORT).show()
        } else {
            generatePDF()
        }
    }
    private fun generatePDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "tax_details.pdf")
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
                        PdfTaxHistory(taxList,sharedPrefManager.getUser()!!.firstName).generatePdf(
                            outputStream
                        )
                    outputStream.close()
                    if (success) {
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


  /*  fun getData(){



        utils.startLoadingAnimation()

        db.collection(constants.TRANSACTION_REQ_COLLECTION)
            .whereEqualTo(constants.INVESTOR_ID,sharedPrefManager.getToken() )
            .whereEqualTo(constants.TRANSACTION_TYPE,constants.TAX_TYPE).get()
            .addOnCompleteListener{task->
                if(task.isSuccessful){
                    utils.endLoadingAnimation()
                    if(task.result.size()>0){
                        //Toast.makeText(mContext, task.result.size().toString(), Toast.LENGTH_SHORT).show()


                        for(document in task.result)listTransaction.add( document.toObject(TransactionModel::class.java))

                        binding.rvProfitTax.adapter=
                            TransactionsAdapter(constants.FROM_TAX,listTransaction.sortedByDescending { it.createdAt })



                    }
                }
            }
    }*/
}