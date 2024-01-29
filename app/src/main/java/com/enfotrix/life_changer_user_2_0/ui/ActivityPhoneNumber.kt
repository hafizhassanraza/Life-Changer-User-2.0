package com.enfotrix.life_changer_user_2_0.ui

import User
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Data.Repo
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityPhoneNumberBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class ActivityPhoneNumber : AppCompatActivity() {


    private val userViewModel: UserViewModel by viewModels()
    private lateinit var sharedPrefManager : SharedPrefManager

    private var storedVerificationId: String=""
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var binding : ActivityPhoneNumberBinding
    private lateinit var repo: Repo
    private lateinit var user: User
    private lateinit var dialog : Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_number)

        binding = ActivityPhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivityPhoneNumber   //I have to set phone number manually by my self
        repo= Repo(mContext)
        utils = Utils(mContext)
        constants= Constants()
        firebaseAuth = FirebaseAuth.getInstance()

        sharedPrefManager = SharedPrefManager(mContext)

        binding.backImage.setOnClickListener{
            startActivity(Intent(mContext,ActivityUserDetails::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
            finish()
        }
        binding.tvTermsCondition.setOnClickListener{
            Toast.makeText(mContext, "Available Soon!", Toast.LENGTH_SHORT).show()
        }
        binding.btnSendOtp.setOnClickListener{

            binding.ccp.registerCarrierNumberEditText(binding.etMobileNumber.editText);
            /*binding.ccp.setPhoneNumberValidityChangeListener(PhoneNumberValidityChangeListener {
                if(it) {
                    //Toast.makeText(mContext, binding.ccp.getFullNumberWithPlus()+"Val", Toast.LENGTH_SHORT).show()
                    sendOTPCode( binding.ccp.getFullNumberWithPlus())
                }
                else Toast.makeText(mContext, "Invalid Number", Toast.LENGTH_SHORT).show()
            })*/
            if(IsValid() ){
                lifecycleScope.launch {
                    var user:User=sharedPrefManager.getUser()!!
                    user.phone= binding.ccp.getFullNumberWithPlus()

                    userViewModel.updateUser(user).observe(this@ActivityPhoneNumber) {
                        utils.endLoadingAnimation()
                        if (it == true) {
                            sharedPrefManager.saveUser(user)
                            sharedPrefManager.putInvestorPhoneNumber(true)
                            Toast.makeText(mContext, "Phone number verified and saved!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(mContext,ActivityUserDetails::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                            finish()
                        }
                        else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                    }
                }
            }


        }
    }

    private fun sendOTPCode(phoneNumber: String) {
        utils.startLoadingAnimation()
        firebaseAuth.setLanguageCode("en") // Set the language code if necessary
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-retrieval or instant verification completed
                    // You can proceed with sign-in using the provided credential
                    // For example, call signInWithCredential(credential)
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, "Verification completed", Toast.LENGTH_SHORT).show()
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    // Verification failed
                    // Display a message to the user or handle the error
                    Toast.makeText(mContext, "Verification failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    utils.endLoadingAnimation()

                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    // Code sent successfully
                    // Save the verification ID and resending token to use them later
                    // For example, store them in a shared preference or global variable
                    Toast.makeText(mContext, "Code sent successfully", Toast.LENGTH_SHORT).show()
                    storedVerificationId = verificationId
                    utils.endLoadingAnimation()

                    // Proceed to enter the OTP
//                    showEnterOtpDialog()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun verifyPhoneNumberWithCredential(credential: PhoneAuthCredential) {
        utils.startLoadingAnimation()
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    utils.endLoadingAnimation()
                    // OTP verification successful
//                    Toast.makeText(mContext, "OTP verification successful", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()

                    lifecycleScope.launch {
                        var user:User=sharedPrefManager.getUser()!!
                        user.phone= binding.ccp.getFullNumberWithPlus()

                        userViewModel.updateUser(user).observe(this@ActivityPhoneNumber) {
                            utils.endLoadingAnimation()
                            if (it == true) {
                                sharedPrefManager.saveUser(user)
                                sharedPrefManager.putInvestorPhoneNumber(true)
                                dialog.dismiss()
                                Toast.makeText(mContext, "Phone number verified and saved!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(mContext,ActivityUserDetails::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                                finish()
                            }
                            else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                        }
                    }

                    // Here, you can pass the OTP value as a parameter to another function or perform any desired action

                    // For example, you can pass the OTP to a function for further processing
                    /*val otp = credential.smsCode
                    if (otp != null) {
                        //processOTP(otp)
                    } else {
                        Toast.makeText(mContext, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }*/
                } else {
                    // OTP verification failed
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, "OTP verification failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    fun showEnterOtpDialog() {

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
        val tvDisc = dialog.findViewById<TextView>(R.id.tvDisc)
        val btnSetPin = dialog.findViewById<Button>(R.id.btnSetPin)

        tvHeader.setText("Enter your OTP !")

        tvDisc.setText("Enter your OTP to verify your provided phone.")

        btnSetPin.setText("Verify")

        etPin1.requestFocus();
        utils.moveFocus( listOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6))

        tvClearAll.setOnClickListener{
            utils.clearAll( listOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6))
            etPin1.requestFocus();

        }
        btnSetPin.setOnClickListener {
            if(!utils.checkEmpty( listOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6))){
                var otp : String =  utils.getPIN( listOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6))

                verifyPhoneNumberWithCredential(PhoneAuthProvider.getCredential(storedVerificationId, otp))

            }
            else Toast.makeText(mContext, "Please enter 6 Digits OTP!", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }
    private fun IsValid(): Boolean {

        val result = MutableLiveData<Boolean>()
        result.value=false
        if (binding.etMobileNumber.editText?.text.toString().length<11) binding.etMobileNumber.editText?.error = "Invalid Number"
        else if (!binding.etMobileNumber.editText?.text.toString().startsWith("0")) binding.etMobileNumber.editText?.error = "Invalid Number"
        //else if (binding.etMobileNumber.editText?.text.toString().length<11) binding.etMobileNumber.editText?.error = "Invalid Phone Number"
        else result.value = true

        return result.value!!
    }

}