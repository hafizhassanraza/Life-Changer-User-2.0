package com.enfotrix.life_changer_user_2_0.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.ModelBankAccount
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule

class ActivitySplash : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()


    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        mContext= this@ActivitySplash;
        actionBar?.hide()
        supportActionBar?.hide()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)




         Timer().schedule(1500){


             if(sharedPrefManager.isLoggedIn()==true){
                 if(sharedPrefManager.getUser().status.equals(constants.INVESTOR_STATUS_ACTIVE)||sharedPrefManager.getUser().status.equals(constants.INVESTOR_STATUS_PENDING))
                 {



                 }
                 else {
                     startActivity(Intent(mContext,ActivityUserDetails::class.java))
                     finish()
                 }

             }
             else if(sharedPrefManager.isLoggedIn()==false) {
                 startActivity(Intent(mContext,ActivityLogin::class.java))
                 finish()
             }

         }






    }

    fun getData(){

        lifecycleScope.launch{
            userViewModel.getAccounts()
                .addOnCompleteListener{task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        val listInvestorAccounts = ArrayList<ModelBankAccount>()
                        val listAdminAccounts = ArrayList<ModelBankAccount>()
                        if(task.result.size()>0){
                            for (document in task.result) {
                                if(document.toObject(ModelBankAccount::class.java).account_holder.equals(sharedPrefManager.getToken()))
                                    listInvestorAccounts.add( document.toObject(ModelBankAccount::class.java))
                                else if(document.toObject(ModelBankAccount::class.java).account_holder.equals(constants.ADMIN))
                                    listAdminAccounts.add( document.toObject(ModelBankAccount::class.java))
                            }
                            sharedPrefManager.putInvestorBankList(listInvestorAccounts)
                            sharedPrefManager.putAdminBankList(listAdminAccounts)
                            startActivity(Intent(mContext,MainActivity::class.java))
                            finish()
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


}