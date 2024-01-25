package com.enfotrix.life_changer_user_2_0.Models

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.enfotrix.life_changer_user_2_0.Adapters.StatmentAdapter
import com.enfotrix.life_changer_user_2_0.Adapters.TransactionsAdapter
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Data.Repo
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot

class InvestmentViewModel(context: Application) : AndroidViewModel(context) {

    private val userRepo = Repo(context)
    private val sharedPrefManager = SharedPrefManager(context)

    private var constants= Constants()


    suspend fun addInvestment(investment: InvestmentModel): LiveData<Boolean> {
        return userRepo.addInvestment(investment)
    }
    suspend fun getProfitTax(token: String): Task<QuerySnapshot> {
        return userRepo.getProfitTax(token)
    }

    suspend fun getWithdrawsReq(token: String): Task<QuerySnapshot> {
        return userRepo.getTransactionReq(token,constants.TRANSACTION_TYPE_WITHDRAW)
    }
    suspend fun getInvestmentReq(token: String): Task<QuerySnapshot> {
        return userRepo.getTransactionReq(token,constants.TRANSACTION_TYPE_INVESTMENT)
    }

    suspend fun getInvestmentReqByDates(token: String, startDate:Timestamp, endDate: Timestamp): Task<QuerySnapshot> {
        return userRepo.getTransactionReqByDates(token,constants.TRANSACTION_TYPE_INVESTMENT, startDate, endDate)
    }



    suspend fun addTransactionReq(transactionModel: TransactionModel): LiveData<Boolean> {
        return userRepo.addTransactionReq(transactionModel)
    }

    suspend fun addTransactionReqWithImage(transactionModel: TransactionModel, imageUri: Uri, type:String): LiveData<Boolean> {
        return userRepo.addTransactionReqWithImage(transactionModel, imageUri, type)

    }

    fun getProfitAdapter( from:String): TransactionsAdapter {
        //return ProfitTaxAdapter(from,sharedPrefManager.getProfitTaxList().filter{ it.type.equals(constants.PROFIT_TYPE) }.sortedByDescending { it.createdAt })
        return TransactionsAdapter(from,sharedPrefManager.getProfitList().filter{ it.status.equals(constants.TRANSACTION_STATUS_APPROVED) }.sortedByDescending { it.createdAt })
    }
    fun getTaxAdapter( from:String): TransactionsAdapter {
        //return ProfitTaxAdapter(from,sharedPrefManager.getProfitTaxList().filter{ it.type.equals(constants.TAX_TYPE) }.sortedByDescending { it.createdAt })
        return TransactionsAdapter(from,sharedPrefManager.getTaxList().filter{ it.status.equals(constants.TRANSACTION_STATUS_APPROVED) }.sortedByDescending { it.createdAt })

    }
    fun getPendingWithdrawReqAdapter( from:String): TransactionsAdapter {
        return TransactionsAdapter(from,sharedPrefManager.getWithdrawReqList().filter{ it.status.equals(constants.TRANSACTION_STATUS_PENDING) }.sortedByDescending { it.createdAt })
    }
    fun getPendingInvestmentReqAdapter( from:String): TransactionsAdapter {
        return TransactionsAdapter(from,sharedPrefManager.getInvestmentReqList().filter{ it.status.equals(constants.TRANSACTION_STATUS_PENDING) }.sortedByDescending { it.createdAt })
    }
    fun getApprovedWithdrawReqAdapter( from:String): TransactionsAdapter {
        return TransactionsAdapter(from,sharedPrefManager.getWithdrawReqList().filter{ it.status.equals(constants.TRANSACTION_STATUS_APPROVED) }.sortedByDescending { it.createdAt })
    }

    fun getApprovedInvestmentReqAdapter( from:String): TransactionsAdapter {
        return TransactionsAdapter(from,sharedPrefManager.getInvestmentReqList().filter{ it.status.equals(constants.TRANSACTION_STATUS_APPROVED) }.sortedByDescending { it.createdAt })
    }
    fun getStatmentAdapter( ): StatmentAdapter {
        return StatmentAdapter(sharedPrefManager.getTransactionList().filter{ it.status.equals(constants.TRANSACTION_STATUS_APPROVED) }.sortedByDescending { it.createdAt })
    }


}