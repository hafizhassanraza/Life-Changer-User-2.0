package com.enfotrix.lifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.lifechanger.Adapters.InvestorAccountsAdapter
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.InvestmentModel
import com.enfotrix.lifechanger.Models.InvestmentViewModel
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.ActivityNewWithdrawReqBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class ActivityNewWithdrawReq : AppCompatActivity() , InvestorAccountsAdapter.OnItemClickListener {

    private val userViewModel: UserViewModel by viewModels()
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var binding : ActivityNewWithdrawReqBinding

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : BottomSheetDialog


    private lateinit var adapter: InvestorAccountsAdapter

    private var accountID:String=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewWithdrawReqBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mContext=this@ActivityNewWithdrawReq
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        setTitle("Add Withdraw Request")

       // binding.rvInvestorAccounts.layoutManager = LinearLayoutManager(mContext)

        binding.layInvestorAccountSelect.setOnClickListener { getInvestorAccounts() }
        binding.layBalance.setOnClickListener { showAddBalanceDialog() }
        binding.btnWithdraw.setOnClickListener {
            addWithdrawReq(
                TransactionModel(
                    sharedPrefManager.getToken(),
                    constants.TRANSACTION_TYPE_WITHDRAW,
                    constants.TRANSACTION_STATUS_PENDING,
                    binding.tvBalance.text.toString(),
                    accountID,
                    sharedPrefManager.getBalance()
                )
            )
        }

    }

    fun addWithdrawReq(transactionModel: TransactionModel){


        utils.startLoadingAnimation()
        lifecycleScope.launch {
            investmentViewModel.addTransactionReq(transactionModel).observe(this@ActivityNewWithdrawReq){
                utils.endLoadingAnimation()

                if (it == true){
                    //dialog.dismiss()
                    Toast.makeText(mContext, "Request Submited Successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(mContext,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                    finish()
                }
                else {
                    Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                }


            }
        }
    }

    fun showAddBalanceDialog() {

        var dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_balance)

        val etBalance = dialog.findViewById<EditText>(R.id.etBalance)
        val btnAddBalance = dialog.findViewById<Button>(R.id.btnAddBalance)


        btnAddBalance.setOnClickListener {
            dialog.dismiss()
            binding.tvBalance.text=etBalance.text
        }

        dialog.show()
    }


    fun getInvestorAccounts() {

        var rvInvestorAccounts:RecyclerView

        dialog = BottomSheetDialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_bottom_sheet_accounts)
        rvInvestorAccounts = dialog.findViewById<RecyclerView>(R.id.rvInvestorAccounts)as RecyclerView
        rvInvestorAccounts.layoutManager = LinearLayoutManager(mContext)
        rvInvestorAccounts.adapter=userViewModel.getInvestorAccountsAdapter(constants.FROM_NEW_WITHDRAW_REQ,this@ActivityNewWithdrawReq)
        dialog.show()


    }

    override fun onItemClick(modelBankAccount: ModelBankAccount) {
        dialog.dismiss()

        binding.tvAccountNumber.text=modelBankAccount.account_number
        binding.tvBankName.text=modelBankAccount.bank_name
        binding.tvAccountTittle.text=modelBankAccount.account_tittle
        accountID=modelBankAccount.docID
    }

    override fun onDeleteClick(modelBankAccount: ModelBankAccount) {

    }


}