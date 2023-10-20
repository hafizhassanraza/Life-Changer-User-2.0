package com.enfotrix.lifechanger.ui

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.lifechanger.Adapters.TransactionsAdapter
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.InvestmentViewModel
import com.enfotrix.lifechanger.Models.ModelProfitTax
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.ActivityLoginBinding
import com.enfotrix.lifechanger.databinding.ActivityProfitTaxBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ActivityProfitTax : AppCompatActivity() {

    private val db = Firebase.firestore

    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var binding : ActivityProfitTaxBinding


    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfitTaxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivityProfitTax
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        binding.rvProfitTax.layoutManager = LinearLayoutManager(mContext)

       /* binding.rvProfitTax.adapter=
            TransactionsAdapter(constants.FROM_PROFIT,sharedPrefManager.getProfitList().filter{ it.status.equals(constants.TRANSACTION_STATUS_APPROVED) }.sortedByDescending { it.createdAt })
*/
        setTitle("Profit Details")

        getData()




    }

    fun getData(){



        utils.startLoadingAnimation()

        db.collection(constants.TRANSACTION_REQ_COLLECTION)
            .whereEqualTo(constants.INVESTOR_ID,sharedPrefManager.getToken() )
            .whereEqualTo(constants.TRANSACTION_TYPE,constants.PROFIT_TYPE).get()
            .addOnCompleteListener{task->
                if(task.isSuccessful){
                    utils.endLoadingAnimation()
                    if(task.result.size()>0){
                        //Toast.makeText(mContext, task.result.size().toString(), Toast.LENGTH_SHORT).show()

                        val listTransaction = ArrayList<TransactionModel>()

                        for(document in task.result)listTransaction.add( document.toObject(TransactionModel::class.java))

                        binding.rvProfitTax.adapter=
                            TransactionsAdapter(constants.FROM_PROFIT,listTransaction.sortedByDescending { it.createdAt })



                    }
                }
            }
    }

}