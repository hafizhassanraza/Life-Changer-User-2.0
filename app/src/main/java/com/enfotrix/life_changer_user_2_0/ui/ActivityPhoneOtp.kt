package com.enfotrix.life_changer_user_2_0.ui
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityPhoneOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class ActivityPhoneOtp : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var verificationId: String? = null
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var binding : ActivityPhoneOtpBinding
    private lateinit var dialog : Dialog
    private var enteredOTP: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_number)

        binding = ActivityPhoneOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this@ActivityPhoneOtp
        constants = Constants()
        dialog = Dialog(this)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnSendOtp.setOnClickListener {
            val phoneNumber = binding.etMobileNumber.editText?.text.toString()

            if (isValidPhoneNumber(phoneNumber)) {
                // Valid Pakistani phone number for one of the networks
                Toast.makeText(this, "Valid phone number", Toast.LENGTH_SHORT).show()
                sendVerificationCode(phoneNumber)
            } else {
                // Invalid Pakistani phone number or not from a major network
                Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(s: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
            verificationId = s
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code: String? = phoneAuthCredential.smsCode
                if (code != null) {
                    enteredOTP = code
                    showEnterOtpDialog()
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@ActivityPhoneOtp, e.message, Toast.LENGTH_LONG).show()
            }
        }

    private fun sendVerificationCode(number: String) {
        // Ensure the phone number is in E.164 format
        val formattedPhoneNumber = "+92$number" // Assuming the country code for Pakistan is 92

        val options: PhoneAuthOptions = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(formattedPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBack)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
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

//                verifyPhoneNumberWithCredential(PhoneAuthProvider.getCredential(storedVerificationId, otp))

            }
            else Toast.makeText(mContext, "Please enter 6 Digits OTP!", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
         var flag=true;
        if(phoneNumber.isEmpty())
            flag=false

        return flag
    }
}
