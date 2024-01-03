package com.enfotrix.lifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.enfotrix.lifechanger.Data.Repo
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.ActivitySignupBinding
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


class ActivitySignup : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var binding : ActivitySignupBinding
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var repo: Repo
    private lateinit var user: User
    private lateinit var dialog : Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivitySignup
        repo= Repo(mContext)
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager=SharedPrefManager(mContext)
        binding.imgBack.setOnClickListener{
            startActivity(Intent(mContext,ActivityLogin::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
            finish()
        }
        binding.tvTermsCondition.setOnClickListener{
            Toast.makeText(mContext, "Available Soon!", Toast.LENGTH_SHORT).show()
        }
        binding.btnProfileRegister.setOnClickListener{
            if((!IsEmpty()) && IsValid() ) checkCNIC(utils.cnicFormate( binding.etCNIC.editText?.text.toString()))
        }
    }

    private fun checkCNIC(cnic:String) {

       user=User(
           cnic,
           binding.etFirstName.editText?.text.toString(),
           binding.etLastName.editText?.text.toString(),
           binding.etAddress.editText?.text.toString(),
           "",
           "",
           "",
           "",
           "",
           "",
           "",
           "",
           Timestamp.now()
       )
        utils.startLoadingAnimation()
        lifecycleScope.launch{

            userViewModel.isInvestorExist(cnic)
                .addOnCompleteListener{task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        if(task.result.size()>0){
                            Toast.makeText(mContext, constants.INVESTOR_CNIC_EXIST, Toast.LENGTH_SHORT).show()
                            binding.etCNIC.editText?.error =constants.INVESTOR_CNIC_EXIST
                        }
                        else showDialogPin()
                    }


                }
                .addOnFailureListener{
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()

                }

        }

    }


    fun showDialogPin() {



        var counter= 0
        dialog = Dialog (mContext,android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)




        //dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_pin_new)

        val tvOne = dialog.findViewById<TextView>(R.id.tvOne)
        val tvTwo = dialog.findViewById<TextView>(R.id.tvTwo)
        val tvThree = dialog.findViewById<TextView>(R.id.tvThree)
        val tvFour = dialog.findViewById<TextView>(R.id.tvFour)
        val tvFive = dialog.findViewById<TextView>(R.id.tvFive)
        val tvSix = dialog.findViewById<TextView>(R.id.tvSix)
        val tvSeven = dialog.findViewById<TextView>(R.id.tvSeven)
        val tvEight = dialog.findViewById<TextView>(R.id.tvEight)
        val tvNine = dialog.findViewById<TextView>(R.id.tvNine)
        val tvZero = dialog.findViewById<TextView>(R.id.tvZero)

        val tvForgotPassword = dialog.findViewById<TextView>(R.id.tvForgotPassword)

        tvForgotPassword.visibility = View.GONE

        val tvInput1 = dialog.findViewById<TextView>(R.id.tvInput1)
        val tvInput2 = dialog.findViewById<TextView>(R.id.tvInput2)
        val tvInput3 = dialog.findViewById<TextView>(R.id.tvInput3)
        val tvInput4 = dialog.findViewById<TextView>(R.id.tvInput4)
        val tvInput5 = dialog.findViewById<TextView>(R.id.tvInput5)
        val tvInput6 = dialog.findViewById<TextView>(R.id.tvInput6)
        val imgBack = dialog.findViewById<ImageView>(R.id.imgBack)
        val imgBackSpace = dialog.findViewById<ImageView>(R.id.imgBackSpace)


        val numberButtons = arrayOf(tvOne, tvTwo, tvThree, tvFour, tvFive, tvSix, tvSeven, tvEight, tvNine, tvZero)
        val inputTextViews = arrayOf(tvInput1, tvInput2, tvInput3, tvInput4, tvInput5, tvInput6)
        val backSpaceViews = arrayOf(tvOne, tvTwo, tvThree, tvFour, tvFive, tvSix)

        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (counter < 6) {
                    vibrate(mContext, 50)

                    counter++
                    if (counter <= inputTextViews.size) {
                        inputTextViews[counter - 1].text = (index + 1).toString()
                    }

                }
                if (counter == 6) {


                    if (user != null) {
                        user.pin=""+tvInput1.text+tvInput2.text+tvInput3.text+tvInput4.text+tvInput5.text+tvInput6.text
                        saveUser(user)

                    }




                }
            }
        }

        imgBack.setOnClickListener { dialog.dismiss() }
        imgBackSpace.setOnClickListener {
            if (counter > 0) {
                vibrate(mContext, 50)
                inputTextViews[counter - 1].text = "_"
                counter--
            }
        }






        dialog.show()
    }

    fun vibrate(context: Context, duration: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android 8.0 (API level 26) and above
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // For devices below Android 8.0
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

 /*   fun showDialogPin() {

        val dialog = Dialog (mContext)

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
        val btnSetPin = dialog.findViewById<Button>(R.id.btnSetPin)

        etPin1.requestFocus();
        utils.moveFocus( listOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6))

        tvClearAll.setOnClickListener{
            utils.clearAll( listOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6))
            etPin1.requestFocus();

        }
        btnSetPin.setOnClickListener {
            //
            if(!utils.checkEmpty( listOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6))){

                user.pin= etPin1.text.toString()+ etPin2.text.toString()+ etPin3.text.toString()+ etPin4.text.toString()+ etPin5.text.toString()+ etPin6.text.toString()
                saveUser(user)

            }
            else Toast.makeText(mContext, "Please enter 6 Digits Pin!", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }*/



    fun saveUser(user:User){


        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.addUser(user).observe(this@ActivitySignup) {
                utils.endLoadingAnimation()
                if (it == true) {

                    sharedPrefManager.putToken(user.id)
                    Toast.makeText(mContext, constants.INVESTOR_SIGNUP_MESSAGE, Toast.LENGTH_SHORT).show()


                    startActivity(Intent(mContext,ActivityLogin::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                    finish()
                }
                else Toast.makeText(mContext, constants.INVESTOR_SIGNUP_FAILURE_MESSAGE, Toast.LENGTH_SHORT).show()

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
        //else if (binding.etMobileNumber.editText?.text.toString().isEmpty()) binding.etMobileNumber.editText?.error = "Empty Phone"
        else result.value = false

        return result.value!!
    }
    private fun IsValid(): Boolean {

        val result = MutableLiveData<Boolean>()
        result.value=false
        if (binding.etCNIC.editText?.text.toString().length<13) binding.etCNIC.editText?.error = "Invalid CNIC"
        //else if (binding.etMobileNumber.editText?.text.toString().length<11) binding.etMobileNumber.editText?.error = "Invalid Phone Number"
        else result.value = true

        return result.value!!
    }

}




