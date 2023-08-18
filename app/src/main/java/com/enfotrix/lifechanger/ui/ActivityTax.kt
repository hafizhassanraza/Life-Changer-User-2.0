package com.enfotrix.lifechanger.ui

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.lifechanger.Adapters.TransactionsAdapter
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.InvestmentViewModel
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.ActivityProfitTaxBinding
import com.enfotrix.lifechanger.databinding.ActivityTaxBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ActivityTax : AppCompatActivity() {


    private val db = Firebase.firestore

    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var binding : ActivityTaxBinding


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

        binding.rvProfitTax.layoutManager = LinearLayoutManager(mContext)

        /* binding.rvProfitTax.adapter=
             TransactionsAdapter(constants.FROM_PROFIT,sharedPrefManager.getProfitList().filter{ it.status.equals(constants.TRANSACTION_STATUS_APPROVED) }.sortedByDescending { it.createdAt })
 */
        setTitle("Tax")

        getData()


    }

    fun getData(){



        utils.startLoadingAnimation()

        db.collection(constants.TRANSACTION_REQ_COLLECTION)
            .whereEqualTo(constants.INVESTOR_ID,sharedPrefManager.getToken() )
            .whereEqualTo(constants.TRANSACTION_TYPE,constants.TAX_TYPE).get()
            .addOnCompleteListener{task->
                if(task.isSuccessful){
                    utils.endLoadingAnimation()
                    if(task.result.size()>0){
                        //Toast.makeText(mContext, task.result.size().toString(), Toast.LENGTH_SHORT).show()

                        val listTransaction = ArrayList<TransactionModel>()

                        for(document in task.result)listTransaction.add( document.toObject(TransactionModel::class.java))

                        binding.rvProfitTax.adapter=
                            TransactionsAdapter(constants.FROM_TAX,listTransaction.sortedByDescending { it.createdAt })



                    }
                }
            }
    }
}