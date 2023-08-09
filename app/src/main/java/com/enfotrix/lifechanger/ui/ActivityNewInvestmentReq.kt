package com.enfotrix.lifechanger.ui
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
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

    private lateinit var binding:ActivityAddInvestmentBinding

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: BottomSheetDialog
    private lateinit var addBalanceDialog: Dialog
    private lateinit var confirmationDialog: Dialog

    private var investorAccount: Boolean = true

    private lateinit var adapter: InvestorAccountsAdapter

    private var accountID: String = ""
    private var adminAccountID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddInvestmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this@ActivityNewInvestmentReq
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        setTitle("Add Investment Request")

        binding.layInvestorAccountSelect.setOnClickListener { getAccounts(constants.VALUE_DIALOG_FLOW_INVESTOR) }
        binding.layAdminAccountSelect.setOnClickListener { getAccounts(constants.VALUE_DIALOG_FLOW_ADMIN) }

        binding.layBalance.setOnClickListener { showAddBalanceDialog() }

        binding.btnInvestment.setOnClickListener {
            val balance = binding.tvBalance.text.toString()
            val sBankName = binding.tvAccountTittle.text.toString()
            val sAccountNumber = binding.tvAccountNumber.text.toString()
            val rBankName = binding.tvAdminAccountTittle.text.toString()
            val rAccountNumber = binding.tvAdminAccountNumber.text.toString()
            if (balance.isEmpty() || balance.toDoubleOrNull() == 0.0) {
                Toast.makeText(mContext, "Please enter a valid balance", Toast.LENGTH_SHORT).show()
            }
            else  if (sBankName.isEmpty()||sBankName.equals("Account Tittle")) {
                Toast.makeText(mContext, "Please enter Sender bankName", Toast.LENGTH_SHORT).show()
            }
            else  if (sAccountNumber.isEmpty()||sAccountNumber.equals("000000000000000")) {
                Toast.makeText(mContext, "Please enter Sender Account Number", Toast.LENGTH_SHORT).show()
            }
            else  if (rBankName.isEmpty()||rBankName.equals("Account Tittle")) {
                Toast.makeText(mContext, "Please enter Receiver Bank Name", Toast.LENGTH_SHORT).show()
            }
            else  if (rAccountNumber.isEmpty()||rAccountNumber.equals("000000000000000")) {
                Toast.makeText(mContext, "Please enter Receiver Account Number", Toast.LENGTH_SHORT).show()
            }
            else {
                showConfirmationDialog()
            }
        }

        // Initialize and set up the custom dialogs
        confirmationDialog = Dialog(this)
        confirmationDialog.setContentView(R.layout.dialog_check_request_info)
        confirmationDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        addBalanceDialog = Dialog(this)
        addBalanceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addBalanceDialog.setContentView(R.layout.dialog_add_balance)
        addBalanceDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnAddBalance = addBalanceDialog.findViewById<Button>(R.id.btnAddBalance)
        btnAddBalance.setOnClickListener {
            addBalanceDialog.dismiss()
            binding.tvBalance.text = addBalanceDialog.findViewById<EditText>(R.id.etBalance).text.toString()
        }
    }

    fun addInvestmentReq(transactionModel: TransactionModel) {
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            investmentViewModel.addTransactionReq(transactionModel).observe(this@ActivityNewInvestmentReq) {
                utils.endLoadingAnimation()
                if (it == true) {
                    Toast.makeText(mContext, "Request Submitted Successfully!", Toast.LENGTH_SHORT).show()
//                    startActivity(Intent(mContext, ::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
//                    finish()
                } else {
                    Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun showAddBalanceDialog() {
        addBalanceDialog.show()
    }

    fun getAccounts(from: String) {
        investorAccount = true
        dialog = BottomSheetDialog(mContext)
        dialog.setContentView(R.layout.dialog_bottom_sheet_accounts)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rvInvestorAccounts = dialog.findViewById<RecyclerView>(R.id.rvInvestorAccounts) as RecyclerView
        rvInvestorAccounts.layoutManager = LinearLayoutManager(mContext)
        if (from == constants.VALUE_DIALOG_FLOW_INVESTOR) {
            rvInvestorAccounts.adapter = userViewModel.getInvestorAccountsAdapter(constants.FROM_NEW_INVESTMENT_REQ, this@ActivityNewInvestmentReq)
        } else {
            investorAccount = false
            rvInvestorAccounts.adapter = userViewModel.getAdminAccountsAdapter(constants.FROM_NEW_INVESTMENT_REQ, this@ActivityNewInvestmentReq)
        }
        dialog.show()
    }

    override fun onItemClick(modelBankAccount: ModelBankAccount) {
        dialog.dismiss()

        if (investorAccount) {
            binding.tvAccountNumber.text = modelBankAccount.account_number
            binding.tvBankName.text = modelBankAccount.bank_name
            binding.tvAccountTittle.text = modelBankAccount.account_tittle
            accountID = modelBankAccount.docID
            Toast.makeText(mContext, accountID, Toast.LENGTH_SHORT).show()
        } else {
            binding.tvAdminAccountNumber.text = modelBankAccount.account_number
            binding.tvAdminBankName.text = modelBankAccount.bank_name
            binding.tvAdminAccountTittle.text = modelBankAccount.account_tittle
            adminAccountID = modelBankAccount.docID
        }
    }

    override fun onDeleteClick(modelBankAccount: ModelBankAccount) {
        // Handle the delete action here if needed
    }

    private fun showConfirmationDialog() {
        // Populate the confirmation dialog with required information
        val tvSenderAccountName = confirmationDialog.findViewById<TextView>(R.id.tvsender_account_name)
        val tvSenderAccountNumber = confirmationDialog.findViewById<TextView>(R.id.tvsender_account_number)
        val tvReceiverAccountName = confirmationDialog.findViewById<TextView>(R.id.tvreceiver_account_name)
        val tvReceiverAccountNumber = confirmationDialog.findViewById<TextView>(R.id.tvreceiver_account_number)
        val tvAmount = confirmationDialog.findViewById<TextView>(R.id.tvamount)

        tvSenderAccountName?.text = binding.tvBankName.text.toString()
        tvSenderAccountNumber?.text = binding.tvAccountNumber.text.toString()
        tvReceiverAccountName?.text = binding.tvAdminBankName.text.toString()
        tvReceiverAccountNumber?.text = binding.tvAdminAccountNumber.text.toString()
        tvAmount?.text = binding.tvBalance.text.toString()

        val btnCancel = confirmationDialog.findViewById<Button>(R.id.btnCancel)
        btnCancel.setOnClickListener {
            confirmationDialog.dismiss()
        }

        val btnSave = confirmationDialog.findViewById<Button>(R.id.btnsave)
        btnSave.setOnClickListener {
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
            confirmationDialog.dismiss()
        }

        confirmationDialog.show()
    }
}
