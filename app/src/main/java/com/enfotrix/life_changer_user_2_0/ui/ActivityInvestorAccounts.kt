package com.enfotrix.life_changer_user_2_0.ui

import User
import android.app.Dialog
import android.content.Context
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
import com.enfotrix.life_changer_user_2_0.Adapters.InvestorAccountsAdapter
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.ModelBankAccount
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityInvestorAccountsBinding
import kotlinx.coroutines.launch

class ActivityInvestorAccounts : AppCompatActivity() , InvestorAccountsAdapter.OnItemClickListener {




    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding : ActivityInvestorAccountsBinding

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog


    private lateinit var adapter: InvestorAccountsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestorAccountsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mContext=this@ActivityInvestorAccounts
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvInvestorAccounts.layoutManager = LinearLayoutManager(mContext)
        setTitle("My Bank Accounts")

        getInvestorAccounts()

        binding.fbAddInvestorAccount.setOnClickListener {

            addAccountDialog()





        }

    }

    fun getInvestorAccounts(){
        binding.rvInvestorAccounts.adapter=userViewModel.getInvestorAccountsAdapter(constants.FROM_INVESTOR_ACCOUNTS,this@ActivityInvestorAccounts)
        /*if (sharedPrefManager.getInvestorBankList().size>0)
            binding.rvInvestorAccounts.adapter = InvestorAccountsAdapter(constants.FROM_INVESTOR_ACCOUNTS,sharedPrefManager.getInvestorBankList(), this@ActivityInvestorAccounts)
        else Toast.makeText(mContext, "No Account Available!", Toast.LENGTH_SHORT).show()*/
    }

    fun addAccountDialog(){

        dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_account)
        val tvHeaderBank = dialog.findViewById<TextView>(R.id.tvHeaderBank)
        val tvHeaderBankDisc = dialog.findViewById<TextView>(R.id.tvHeaderBankDisc)
        val spBank = dialog.findViewById<Spinner>(R.id.spBank)
        val etAccountTittle = dialog.findViewById<EditText>(R.id.etAccountTittle)
        val etAccountNumber = dialog.findViewById<EditText>(R.id.etAccountNumber)
        val btnAddAccount = dialog.findViewById<Button>(R.id.btnAddAccount)
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
        dialog.show()


    }
    fun updateInvestorBankList(modelBankAccount: ModelBankAccount) {

        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.addUserAccount(modelBankAccount)
                .observe(this@ActivityInvestorAccounts) {
                    dialog.dismiss()
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


    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }


    override fun onItemClick(modelBankAccount: ModelBankAccount) {

    }

    override fun onDeleteClick(modelBankAccount: ModelBankAccount) {

        Toast.makeText(mContext, "Available soon...", Toast.LENGTH_SHORT).show()
    }
}