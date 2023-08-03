package com.enfotrix.lifechanger.ui
import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.ModelNominee
import com.enfotrix.lifechanger.Models.NomineeViewModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class ActivityLogin : AppCompatActivity() {


    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()

    private lateinit var binding : ActivityLoginBinding

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivityLogin
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)



        binding.btnSignIn.setOnClickListener(View.OnClickListener {


            if((!IsEmpty()) && IsValid() ) checkCNIC(utils.cnicFormate(   binding.etCNIC.editText?.text.toString()))



        })


        binding.tvRegister.setOnClickListener(View.OnClickListener {

            startActivity(Intent(mContext, ActivitySignup::class.java))

        })
        binding.tvForgotPassword.setOnClickListener{
            Toast.makeText(mContext, "Available soon...", Toast.LENGTH_SHORT).show()
        }




    }


    fun showDialogPin(user:User?,token:String) {

        dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_set_pin)
        val etPin1 = dialog.findViewById<EditText>(R.id.etPin1)
        val etPin2 = dialog.findViewById<EditText>(R.id.etPin2)
        val etPin3 = dialog.findViewById<EditText>(R.id.etPin3)
        val etPin4 = dialog.findViewById<EditText>(R.id.etPin4)
        val etPin5 = dialog.findViewById<EditText>(R.id.etPin5)
        val etPin6 = dialog.findViewById<EditText>(R.id.etPin6)
        val tvClearAll = dialog.findViewById<TextView>(R.id.tvClearAll)
        val tvHeader = dialog.findViewById<TextView>(R.id.tvHeader)
        val btnSetPin = dialog.findViewById<Button>(R.id.btnSetPin)

        tvHeader.setText("Enter your Pin to Login !")
        btnSetPin.setText("Login")
        etPin1.requestFocus();
        utils.moveFocus( listOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6))

        tvClearAll.setOnClickListener{
            utils.clearAll( listOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6))
            etPin1.requestFocus();

        }
        btnSetPin.setOnClickListener {
            if(!utils.checkEmpty( listOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6))){
                var pin : String =  utils.getPIN( listOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6))
                loginUser(user,pin,token)
            }
            else Toast.makeText(mContext, "Please enter 6 Digits Pin!", Toast.LENGTH_SHORT).show()
        }

         dialog.show()
    }


    private fun loginUser(user:User?,pin:String,token: String){

        //pending, active, incomplete

        if (user != null) {
            if(user.pin.equals(pin)){
                if(user.status.equals(constants.INVESTOR_STATUS_ACTIVE)||user.status.equals(constants.INVESTOR_STATUS_PENDING)){
                    utils.startLoadingAnimation()
                    lifecycleScope.launch {
                        nomineeViewModel.getNominee(token)
                            .addOnSuccessListener {
                                utils.endLoadingAnimation()
                                val nominee: ModelNominee? = it.toObject(ModelNominee::class.java)
                                if (nominee != null) {
                                    nominee.docID=it.id
                                    if(user!=null)userViewModel.saveLoginAuth(user, token, true)//usre +token+login_boolean
                                    if (nominee!=null) sharedPrefManager.saveNominee(nominee)

                                    //////// here -> nominee saved(if available), user saved , loginInfo saved //////

                                    startActivity(Intent(mContext,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                                    finish()
                                }
                            }
                            .addOnFailureListener{
                                utils.endLoadingAnimation()
                                Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                            }


                    }
                }
                else{
                    if(user!=null)userViewModel.saveLoginAuth(user, token, true)//usre +token+login_boolean
                    startActivity(Intent(mContext,ActivityUserDetails::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                    finish()
                }

            }




        }
        else {
            Toast.makeText(mContext, "Incorrect Pin", Toast.LENGTH_SHORT).show()
        }
    }

    ////////// check user active and exist ////////////////
    private fun checkCNIC(cnic:String) {

        utils.startLoadingAnimation()
        lifecycleScope.launch{
            userViewModel.isInvestorExist(cnic)
                .addOnCompleteListener{task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {

                        if(task.result.size()>0){
                            var token: String = ""
                            val documents = task.result
                            var user: User? = null
                            for (document in documents) {
                                user = document.toObject(User::class.java)
                                token= document.id
                            }


                            if(user?.status.equals(constants.INVESTOR_STATUS_ACTIVE) || user?.status.equals(constants.INVESTOR_STATUS_PENDING) || user?.status.equals(constants.INVESTOR_STATUS_INCOMPLETE) )
                                showDialogPin(user,token)
                            else if(user?.status.equals(constants.INVESTOR_STATUS_BLOCKED))
                                binding.etCNIC.editText?.error =constants.INVESTOR_CNIC_BLOCKED
                            else if(documents.size()==0)
                                binding.etCNIC.editText?.error =constants.INVESTOR_CNIC_NOT_EXIST
                        }
                        else binding.etCNIC.editText?.error =constants.INVESTOR_CNIC_NOT_EXIST

                    }

                }
                .addOnFailureListener{
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                }

        }
    }



    private fun IsEmpty(): Boolean {

        val result = MutableLiveData<Boolean>()
        result.value=true
        if (binding.etCNIC.editText?.text.toString().isEmpty()) binding.etCNIC.editText?.error = "Empty CNIC"
        else result.value = false

        return result.value!!
    }
    private fun IsValid(): Boolean {

        val result = MutableLiveData<Boolean>()
        result.value=false
        if (binding.etCNIC.editText?.text.toString().length<13) binding.etCNIC.editText?.error = "Invalid CNIC"
        else result.value = true

        return result.value!!
    }

}


