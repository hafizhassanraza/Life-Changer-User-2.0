package com.enfotrix.lifechanger.ui
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.lifechanger.Adapters.InvestorAccountsAdapter
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.InvestmentViewModel
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.ActivityAddInvestmentBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class ActivityNewInvestmentReq : AppCompatActivity(), InvestorAccountsAdapter.OnItemClickListener {

    private val userViewModel: UserViewModel by viewModels()
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var binding : ActivityAddInvestmentBinding

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : BottomSheetDialog

    private var investorAccount:Boolean=true

    private lateinit var adapter: InvestorAccountsAdapter

    private var accountID:String=""
    private var adminAccountID:String=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddInvestmentBinding.inflate(layoutInflater)

        setContentView(binding.root)

        mContext=this@ActivityNewInvestmentReq

        utils = Utils(mContext)

        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        setTitle("Add Investment Request")

        // binding.rvInvestorAccounts.layoutManager = LinearLayoutManager(mContext)

        binding.layInvestorAccountSelect.setOnClickListener { getAccounts(constants.VALUE_DIALOG_FLOW_INVESTOR) }
        binding.layAdminAccountSelect.setOnClickListener { getAccounts(constants.VALUE_DIALOG_FLOW_ADMIN) }


        binding.layBalance.setOnClickListener { showAddBalanceDialog() }
        binding.btnInvestment.setOnClickListener {
            addInvestmentReq(
                TransactionModel(
                    sharedPrefManager.getToken(),
                    constants.TRANSACTION_TYPE_INVESTMENT,
                    constants.TRANSACTION_STATUS_PENDING,
                    binding.tvBalance.text.toString(),
                    adminAccountID,
                    sharedPrefManager.getBalance(),
                    accountID
                )
            )
        }

    }

    fun addInvestmentReq(transactionModel: TransactionModel){


        utils.startLoadingAnimation()
        lifecycleScope.launch {
            investmentViewModel.addTransactionReq(transactionModel).observe(this@ActivityNewInvestmentReq){
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

    fun getAccounts(from: String) {

        investorAccount=true
        var rvInvestorAccounts: RecyclerView
        dialog = BottomSheetDialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_bottom_sheet_accounts)
        rvInvestorAccounts = dialog.findViewById<RecyclerView>(R.id.rvInvestorAccounts)as RecyclerView
        rvInvestorAccounts.layoutManager = LinearLayoutManager(mContext)
        if(from.equals(constants.VALUE_DIALOG_FLOW_INVESTOR))
            rvInvestorAccounts.adapter=userViewModel.getInvestorAccountsAdapter(constants.FROM_NEW_INVESTMENT_REQ,this@ActivityNewInvestmentReq)
        else {
            investorAccount=false
            rvInvestorAccounts.adapter=userViewModel.getAdminAccountsAdapter(constants.FROM_NEW_INVESTMENT_REQ,this@ActivityNewInvestmentReq)
        }
        dialog.show()

    }
    override fun onItemClick(modelBankAccount: ModelBankAccount) {
        dialog.dismiss()

        if(investorAccount){
            binding.tvAccountNumber.text=modelBankAccount.account_number
            binding.tvBankName.text=modelBankAccount.bank_name
            binding.tvAccountTittle.text=modelBankAccount.account_tittle
            accountID=modelBankAccount.docID
            Toast.makeText(mContext, accountID+"", Toast.LENGTH_SHORT).show()
        }
        else {

            binding.tvAdminAccountNumber.text=modelBankAccount.account_number
            binding.tvAdminBankName.text=modelBankAccount.bank_name
            binding.tvAdminAccountTittle.text=modelBankAccount.account_tittle
            adminAccountID=modelBankAccount.docID

        }

    }

    override fun onDeleteClick(modelBankAccount: ModelBankAccount) {

    }


}
