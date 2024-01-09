package com.enfotrix.lifechanger.ui
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
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
    private lateinit var dialogAddA : Dialog

    private var investorAccount:Boolean=true

    private lateinit var adapter: InvestorAccountsAdapter

    private var accountID:String=""
    private var adminAccountID:String=""

    private var imageURI: Uri? = null
    private val IMAGE_PICKER_REQUEST_CODE = 200
    private var userReceiptPhoto: Boolean = false






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

        binding.cvReceipt.setOnClickListener { showReceiptDialog() }


        binding.layBalance.setOnClickListener { showAddBalanceDialog() }
        binding.btnInvestment.setOnClickListener {
            if(binding.tvAccountNumber.text.isEmpty()|| binding.tvAccountNumber.text=="0000"){
                Toast.makeText(mContext, "Please Select bank Account ", Toast.LENGTH_SHORT).show()

            }

           else  if(binding.tvBalance.text.isEmpty()){
                Toast.makeText(mContext, "Please Enter balance for investment", Toast.LENGTH_SHORT).show()
            }

           else  if(binding.tvAdminAccountNumber.text.isEmpty()|| binding.tvAdminAccountNumber.text=="0000")
            {
                Toast.makeText(mContext, "Select Admin bank Account please", Toast.LENGTH_SHORT).show()
            }
            else if(binding.tvBalance.text.toString().toInt()<5){
                Toast.makeText(mContext, "Please Enter valid balance for investment", Toast.LENGTH_SHORT).show()

            }
            else if (userReceiptPhoto!=true || imageURI == null) Toast.makeText(mContext, "Please select the transaction image", Toast.LENGTH_SHORT).show()

            else {


                val investmentBalance = sharedPrefManager.getInvestment().investmentBalance
                val lastProfit = sharedPrefManager.getInvestment().lastProfit
                val lastInvestment = sharedPrefManager.getInvestment().lastInvestment
                val ExpextedSum = getTextFromInvestment(investmentBalance).toDouble() + getTextFromInvestment(lastProfit).toDouble() + getTextFromInvestment(lastInvestment).toDouble()



                addInvestmentReq(
                    TransactionModel(
                        sharedPrefManager.getToken(),
                        constants.TRANSACTION_TYPE_INVESTMENT,
                        constants.TRANSACTION_STATUS_PENDING,
                        binding.tvBalance.text.toString(),
                        adminAccountID,
                        ExpextedSum.toInt().toString(),
                        accountID
                    )
                )
            }

        }


    }
    fun getTextFromInvestment(value: String?): String {
        return if (value.isNullOrEmpty()) "0" else value
    }

    fun showReceiptDialog() {
        userReceiptPhoto = false

        dialogAddA = Dialog(mContext)
        dialogAddA.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogAddA.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogAddA.setContentView(R.layout.receiptdialog)

        val tvSelect = dialogAddA.findViewById<TextView>(R.id.tvSelect)
     val  image = dialogAddA.findViewById<ImageView>(R.id.imgProfilePhoto)

        tvSelect?.setOnClickListener {
            userReceiptPhoto = true
            val pickImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImage, IMAGE_PICKER_REQUEST_CODE)
            image.setImageURI(imageURI)


            dialogAddA.dismiss()
        }






        dialogAddA.show()
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
            
            
            
            if(etAccountTittle.text.isNotEmpty()||etAccountNumber.text.isNotEmpty()) {


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
            else
                Toast.makeText(mContext, "Please ,enter All fields!!", Toast.LENGTH_SHORT).show()
            
        }
        dialogAddA.show()


    }

    fun updateInvestorBankList(modelBankAccount: ModelBankAccount) {

        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.addUserAccount(modelBankAccount)
                .observe(this@ActivityNewInvestmentReq) {
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

    fun addInvestmentReq(transactionModel: TransactionModel) {
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            investmentViewModel.addTransactionReqWithImage(transactionModel,imageURI!!,"userTransactionReceipt").observe(this@ActivityNewInvestmentReq){
                utils.endLoadingAnimation()

                if (it == true){
                    //dialog.dismiss()
                    Toast.makeText(mContext, "Request Submitted Successfully!", Toast.LENGTH_SHORT).show()
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

            var balance: Int
            balance= Integer.parseInt(etBalance.text.toString())
            if(balance>0){

                if(etBalance.text.toString().isNullOrEmpty()) Toast.makeText(mContext, "enter amount", Toast.LENGTH_SHORT).show()
                else{
                    dialog.dismiss()
                    binding.tvBalance.text=etBalance.text
                }

            }

        }

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageURI = data.data!!

            binding.imgRecieptTransaction.setImageURI(imageURI)

            //imageView.setImageURI(imageURI)
        }
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
            //Toast.makeText(mContext, accountID+"", Toast.LENGTH_SHORT).show()
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
