package com.enfotrix.lifechanger.Models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.enfotrix.lifechanger.Adapters.InvestorAccountsAdapter
import com.enfotrix.lifechanger.Adapters.ProfitTaxAdapter
import com.enfotrix.lifechanger.Adapters.TransactionsAdapter
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Data.Repo
import com.enfotrix.lifechanger.SharedPrefManager
import com.google.android.gms.tasks.Task
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
    suspend fun addTransactionReq(transactionModel: TransactionModel): LiveData<Boolean> {
        return userRepo.addTransactionReq(transactionModel)
    }
    fun getProfitAdapter( from:String): ProfitTaxAdapter {
        return ProfitTaxAdapter(from,sharedPrefManager.getProfitTaxList().filter{ it.type.equals(constants.PROFIT_TYPE) }.sortedByDescending { it.createdAt })
    }
    fun getTaxAdapter( from:String): ProfitTaxAdapter {
        return ProfitTaxAdapter(from,sharedPrefManager.getProfitTaxList().filter{ it.type.equals(constants.TAX_TYPE) }.sortedByDescending { it.createdAt })
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


}