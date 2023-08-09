package com.enfotrix.lifechanger.ui

import User
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Data.Repo
import com.enfotrix.lifechanger.Models.ModelNominee
import com.enfotrix.lifechanger.Models.NomineeViewModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.ActivityNomineeBinding
import com.enfotrix.lifechanger.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch

class ActivityNominee : AppCompatActivity() {

    private val nomineeViewModel: NomineeViewModel by viewModels()

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var binding : ActivityNomineeBinding
    private lateinit var repo: Repo
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNomineeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivityNominee
        repo= Repo(mContext)
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)






        binding.imgBack.setOnClickListener{

            if(intent.getStringExtra(constants.KEY_ACTIVITY_FLOW).equals(constants.VALUE_ACTIVITY_FLOW_USER_DETAILS)){
                startActivity(Intent(mContext,ActivityUserDetails::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                finish()
            }

        }
        binding.tvTermsCondition.setOnClickListener{
            Toast.makeText(mContext, "Available Soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnNomineeRegister.setOnClickListener{
            if((!IsEmpty()) && IsValid() ) {
                saveNominee(ModelNominee(
                    sharedPrefManager.getToken(),
                    binding.etCNIC.editText?.text.toString(),
                    binding.etFirstName.editText?.text.toString(),
                    binding.etLastName.editText?.text.toString(),
                    binding.etAddress.editText?.text.toString(),
                    binding.etMobileNumber.editText?.text.toString(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    sharedPrefManager.getToken(),
                    binding.spNominee.selectedItem.toString()
                ))
            }

        }
    }






    fun saveNominee(nominee:ModelNominee){


        utils.startLoadingAnimation()
        lifecycleScope.launch {
            nomineeViewModel.addNominee(nominee).observe(this@ActivityNominee) {
                utils.endLoadingAnimation()
                if (it == true) {
                    sharedPrefManager.saveNominee(nominee)// id overwrite in repo
                    Toast.makeText(mContext, constants.NOMINEE_SIGNUP_MESSAGE, Toast.LENGTH_SHORT).show()
                    if(intent.getStringExtra(constants.KEY_ACTIVITY_FLOW).equals(constants.VALUE_ACTIVITY_FLOW_USER_DETAILS)){
                        startActivity(Intent(mContext,ActivityUserDetails::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                        finish()
                    }
                }
                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

            }
        }
    }


    private fun IsEmpty(): Boolean {

        val result = MutableLiveData<Boolean>()
        result.value=true
        if (binding.etCNIC.editText?.text.toString().isEmpty()) binding.etCNIC.editText?.error = "Empty CNIC"
        else if (binding.etAddress.editText?.text.toString().isEmpty()) binding.etAddress.editText?.error = "Empty Address"
        else if (binding.etFirstName.editText?.text.toString().isEmpty()) binding.etFirstName.editText?.error = "Empty First Name"
        else if (binding.etLastName.editText?.text.toString().isEmpty()) binding.etLastName.editText?.error = "Empty Last Name"
        else if (binding.etMobileNumber.editText?.text.toString().isEmpty()) binding.etMobileNumber.editText?.error = "Empty Phone"
        else result.value = false

        return result.value!!
    }
    private fun IsValid(): Boolean {

        val result = MutableLiveData<Boolean>()
        result.value=false
        if (binding.etCNIC.editText?.text.toString().length<13) binding.etCNIC.editText?.error = "Invalid CNIC"
        else if (binding.etMobileNumber.editText?.text.toString().length<11) binding.etMobileNumber.editText?.error = "Invalid Phone Number"
        else result.value = true

        return result.value!!
    }
}