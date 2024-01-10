package com.enfotrix.lifechanger.ui
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
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
import com.enfotrix.lifechanger.databinding.ActivityNewWithdrawReqBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class ActivityNewWithdrawReq : AppCompatActivity(), InvestorAccountsAdapter.OnItemClickListener {

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding: ActivityNewWithdrawReqBinding
    private val investmentViewModel: InvestmentViewModel by viewModels()

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: BottomSheetDialog
    private lateinit var confirmationDialog: Dialog
    private lateinit var dialogAddA : Dialog


    private lateinit var adapter: InvestorAccountsAdapter

    private var accountID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewWithdrawReqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this@ActivityNewWithdrawReq
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        setTitle("Add Withdraw Request")

        // Initialize the confirmationDialog
        confirmationDialog = Dialog(mContext)
        confirmationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        confirmationDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        confirmationDialog.setContentView(R.layout.dialog_check_withdraw_info)

        binding.layInvestorAccountSelect.setOnClickListener { getInvestorAccounts() }
        binding.layBalance.setOnClickListener { showAddBalanceDialog() }
        binding.btnWithdraw.setOnClickListener {





            val balance = binding.tvBalance.text.toString()
            var bankTitle=binding.tvAccountTittle.text.toString()
            var accountNumber=binding.tvAccountNumber.text.toString()
            if (balance.isEmpty() || balance.toDoubleOrNull() == 0.0) {
                Toast.makeText(mContext, "Please enter a valid balance", Toast.LENGTH_SHORT).show()
            }
            else  if(bankTitle.isEmpty()||bankTitle.equals("Account Tittle")){

                Toast.makeText(mContext, "Please Enter Account Details", Toast.LENGTH_SHORT).show()
            }



            else {
                val investmentBalance = sharedPrefManager.getInvestment()?.investmentBalance?.takeIf { it.isNotBlank() }?.toInt() ?: 0
                val lastProfit = sharedPrefManager.getInvestment()?.lastProfit?.takeIf { it.isNotBlank() }?.toInt() ?: 0
                val Balance = binding.tvBalance.text.toString()
                var totalInvestorBalance=(investmentBalance + lastProfit)
//                Toast.makeText(mContext, ""+totalInvestorBalance, Toast.LENGTH_SHORT).show()
//                Toast.makeText(mContext, ""+Balance, Toast.LENGTH_SHORT).show()

                if (Balance.isNotBlank()) {
                    val balanceValue = Balance.toInt()

                    if (totalInvestorBalance >= balanceValue) {
                        showConfirmationDialog()
                    } else if (totalInvestorBalance < balanceValue) {
                        Toast.makeText(mContext, "Please Enter Valid Amount", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(mContext, "Please Enter a Valid Amount1", Toast.LENGTH_SHORT).show()
                }


            }







        }
    }

    fun addAccountDialog(view: View){

        dialogAddA = Dialog (mContext)
        dialogAddA.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogAddA.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogAddA.setContentView(R.layout.dialog_add_account)
        val tvHeaderBank = dialogAddA.findViewById<TextView>(R.id.tvHeaderBank)
        val tvHeaderBankDisc = dialogAddA.findViewById<TextView>(R.id.tvHeaderBankDisc)
        val spBank = dialogAddA.findViewById<Spinner>(R.id.spBank)
        val etAccountTittle = dialogAddA.findViewById<EditText>(R.id.etAccountTittle)
        val etAccountNumber = dialogAddA.findViewById<EditText>(R.id.etAccountNumber)
        val btnAddAccount = dialogAddA.findViewById<Button>(R.id.btnAddAccount)
        btnAddAccount.setOnClickListener {
            updateInvestorBankList(
                ModelBankAccount(
                    "",
                    spBank.selectedItem.toString(),
                    etAccountTittle.text.toString(),
                    etAccountNumber.text.toString(),
                    sharedPrefManager.getToken()
                )
            )
        }
        dialogAddA.show()


    }

    fun updateInvestorBankList(modelBankAccount: ModelBankAccount) {

        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.addUserAccount(modelBankAccount)
                .observe(this@ActivityNewWithdrawReq) {
                    dialogAddA.dismiss()
                    if (it == true) {

                        lifecycleScope.launch{
                            userViewModel.getUserAccounts(sharedPrefManager.getToken())
                                .addOnCompleteListener{task ->
                                    utils.endLoadingAnimation()
                                    if (task.isSuccessful) {
                                        val list = ArrayList<ModelBankAccount>()
                                        if(task.result.size()>0){
                                            for (document in task.result) list.add( document.toObject(ModelBankAccount::class.java))
                                            sharedPrefManager.putInvestorBankList(list)
                                            getInvestorAccounts()
                                            Toast.makeText(mContext, constants.ACCOPUNT_ADDED_MESSAGE, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                                }
                                .addOnFailureListener{
                                    utils.endLoadingAnimation()
                                    Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                                }


                        }

                    }
                    else {
                        utils.endLoadingAnimation()
                        Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                    }

                }

        }

    }


    fun addWithdrawReq(transactionModel: TransactionModel) {
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            investmentViewModel.addTransactionReq(transactionModel).observe(this@ActivityNewWithdrawReq) {
                utils.endLoadingAnimation()
                if (it == true) {
                    Toast.makeText(mContext, "Request Submitted Successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun showAddBalanceDialog() {
        val dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_balance)

        val etBalance = dialog.findViewById<EditText>(R.id.etBalance)
        val btnAddBalance = dialog.findViewById<Button>(R.id.btnAddBalance)

        btnAddBalance.setOnClickListener {
            dialog.dismiss()
            binding.tvBalance.text = etBalance.text
        }

        dialog.show()
    }

    fun getInvestorAccounts() {
        dialog = BottomSheetDialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_bottom_sheet_accounts)

        val rvInvestorAccounts: RecyclerView = dialog.findViewById<RecyclerView>(R.id.rvInvestorAccounts) as RecyclerView
        rvInvestorAccounts.layoutManager = LinearLayoutManager(mContext)
        rvInvestorAccounts.adapter = userViewModel.getInvestorAccountsAdapter(constants.FROM_NEW_WITHDRAW_REQ, this@ActivityNewWithdrawReq)

        dialog.show()
    }

    override fun onItemClick(modelBankAccount: ModelBankAccount) {
        dialog.dismiss()

        binding.tvAccountNumber.text = modelBankAccount.account_number
        binding.tvBankName.text = modelBankAccount.bank_name
        binding.tvAccountTittle.text = modelBankAccount.account_tittle
        accountID = modelBankAccount.docID
    }

    override fun onDeleteClick(modelBankAccount: ModelBankAccount) {
        // Handle the delete action here if needed
    }

    private fun showConfirmationDialog() {
        // Populate the confirmationDialog with required information
        val tvSenderAccountName = confirmationDialog.findViewById<TextView>(R.id.tvsender_account_name)
        val tvSenderAccountNumber = confirmationDialog.findViewById<TextView>(R.id.tvsender_account_number)
        val tvPhone = confirmationDialog.findViewById<TextView>(R.id.tvPhone)
        val tvAmount = confirmationDialog.findViewById<TextView>(R.id.tvamount)
        val btnCancel = confirmationDialog.findViewById<Button>(R.id.btnCancel)
        val btnSave = confirmationDialog.findViewById<Button>(R.id.btnsave)

        tvSenderAccountName?.text = binding.tvBankName.text.toString()
        tvSenderAccountNumber?.text = binding.tvAccountNumber.text.toString()
        tvPhone?.text = binding.tvPhone.text.toString()
        tvAmount?.text = binding.tvBalance.text.toString()

        btnCancel.setOnClickListener {
            confirmationDialog.dismiss()
        }
        btnSave.setOnClickListener {

            val investmentBalance = sharedPrefManager.getInvestment().investmentBalance
            val lastProfit = sharedPrefManager.getInvestment().lastProfit
            val lastInvestment = sharedPrefManager.getInvestment().lastInvestment
            val ExpextedSum = getTextFromInvestment(investmentBalance).toDouble() + getTextFromInvestment(lastProfit).toDouble() + getTextFromInvestment(lastInvestment).toDouble()

            addWithdrawReq(
                TransactionModel(
                    sharedPrefManager.getToken(),
                    constants.TRANSACTION_TYPE_WITHDRAW,
                    constants.TRANSACTION_STATUS_PENDING,
                    binding.tvBalance.text.toString(),
                    accountID,
                    ExpextedSum.toInt().toString(),
                )
            )
            confirmationDialog.dismiss()
        }
        confirmationDialog.show()
    }


    fun getTextFromInvestment(value: String?): String {
        return if (value.isNullOrEmpty()) "0" else value
    }
}
