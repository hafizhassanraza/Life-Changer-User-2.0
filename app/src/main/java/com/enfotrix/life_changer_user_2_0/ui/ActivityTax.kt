package com.enfotrix.life_changer_user_2_0.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.InvestmentViewModel
import com.enfotrix.life_changer_user_2_0.Models.TransactionModel
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.Pdf.PdfTransaction
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityTaxBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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



        /*binding.pdfProfit.setOnClickListener {
            generatePDF()
        }*/
        binding.imgBack.setOnClickListener{finish()}


    }


    private fun generatePDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "tax_details.pdf")
        }
        startActivityForResult(intent, CREATE_PDF_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val outputStream = mContext.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val success =
                        PdfTransaction(listTransaction.sortedByDescending { it.createdAt }).generatePdf(
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