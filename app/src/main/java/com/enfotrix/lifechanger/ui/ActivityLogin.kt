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
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.ModelNominee
import com.enfotrix.lifechanger.Models.NomineeViewModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.ActivityLoginBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class ActivityLogin : AppCompatActivity() {


    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()

    private lateinit var binding : ActivityLoginBinding

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private var investorAccount: Boolean = true
    private lateinit var dialogPinUpdate: Dialog
    private lateinit var dialog1: Dialog

    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var investor: User
    private lateinit var modelNominee: ModelNominee
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivityLogin
        utils = Utils(mContext)
        constants= Constants()

        investor=User()
        sharedPrefManager = SharedPrefManager(mContext)

        modelNominee= ModelNominee()

        binding.btnSignIn.setOnClickListener(View.OnClickListener {


            if((!IsEmpty()) && IsValid()) checkCNIC(utils.cnicFormate(   binding.etCNIC.editText?.text.toString()))


        })


        binding.tvRegister.setOnClickListener(View.OnClickListener {

            startActivity(Intent(mContext, ActivitySignup::class.java))

        })



    }


    fun showDialogPin(user:User?,token:String) {



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
        val backSpaceViews = arrayOf(imgBackSpace) // Move tvZero to numberButtons array

        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (counter < 6) {
                    vibrate(mContext, 50)
                    counter++
                    if (counter <= inputTextViews.size) {
                        inputTextViews[counter - 1].text = if (index == 9) "0" else (index + 1).toString()
                    }
                }
                if (counter == 6) {
                    Toast.makeText(mContext, "Checking...", Toast.LENGTH_SHORT).show()
                    val pin = "" + tvInput1.text + tvInput2.text + tvInput3.text + tvInput4.text + tvInput5.text + tvInput6.text
                    loginUser(user, pin, token)
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

        tvForgotPassword.setOnClickListener {
            dialog.dismiss()
            showForgetPasswordDialog()
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


    private fun loginUser(user:User?,pin:String,token: String){

        //status
        //pending= profile complete and admin approval pending  -> Main
        //active = profile complete and admin approved (All Positive cases) -> Main
        //incomplete = profile not complete and admin approval pending -> User Registration



        if (user != null) {
            // Checking Pin
            if(user.pin.equals(pin)){

                // active || pending
                if(user.status.equals(constants.INVESTOR_STATUS_ACTIVE)||user.status.equals(constants.INVESTOR_STATUS_PENDING)){
                    utils.startLoadingAnimation()
                    FirebaseMessaging.getInstance().token
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                // Get the device token
                                val token = task.result

                                lifecycleScope.launch {
                                    userViewModel.updatedevicetoken(user.id,token)

                                }
                            }
                            else
                            {
                                Toast.makeText(mContext, "Something went Wrong!!!", Toast.LENGTH_SHORT).show()
                            }
                        }
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
                                    lifecycleScope.launch {

                                        // Use async to execute Firebase calls asynchronously
                                        val saveAdminDeferred = async { saveAdminAccounts() }
                                        val saveUserDeferred = async { saveUserAccounts() }

                                        // Wait for both async tasks to complete
                                        saveAdminDeferred.await()
                                        saveUserDeferred.await()


                                        // Now you can start MainActivity
                                        startActivity(
                                            Intent(mContext, MainActivity::class.java)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                        finish()
                                    }
                                }
                            }
                            .addOnFailureListener{
                                utils.endLoadingAnimation()
                                Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                            }


                    }

                }
                // Incomplete
                else if (user.status.equals(constants.INVESTOR_STATUS_INCOMPLETE)){
                    utils.startLoadingAnimation()
                    FirebaseMessaging.getInstance().token
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                // Get the device token
                                val token = task.result
                                lifecycleScope.launch {
                                    userViewModel.updatedevicetoken(user.id,token)



                                    utils.endLoadingAnimation()
                                    userViewModel.saveLoginAuth(user, user.id, true)//usre +token+login_boolean
                                    startActivity(Intent(mContext,ActivityUserDetails::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                                    finish()
                                }
                            }
                            else
                            {
                                Toast.makeText(mContext, "Something went Wrong!!!", Toast.LENGTH_SHORT).show()
                            }
                        }


                }


            }
            // Incorrect PIN
            else {
                vibrate(mContext, 200)
                Toast.makeText(mContext, "Incorrect Pin", Toast.LENGTH_SHORT).show()
            }
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
                            {
                                showDialogPin(user,token)
                            }
                            else if(user?.status.equals(constants.INVESTOR_STATUS_BLOCKED)){
                                binding.etCNIC.editText?.error =constants.INVESTOR_CNIC_BLOCKED
                                Toast.makeText(mContext, constants.INVESTOR_CNIC_BLOCKED, Toast.LENGTH_SHORT).show()

                            }
                            else if(documents.size()==0)
                            {
                                binding.etCNIC.editText?.error =constants.INVESTOR_CNIC_NOT_EXIST
                                Toast.makeText(mContext, constants.INVESTOR_CNIC_NOT_EXIST, Toast.LENGTH_SHORT).show()
                            }
                        }
                        else {
                            binding.etCNIC.editText?.error =constants.INVESTOR_CNIC_NOT_EXIST
                            Toast.makeText(mContext, constants.INVESTOR_CNIC_NOT_EXIST, Toast.LENGTH_SHORT).show()

                        }
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


    private fun showForgetPasswordDialog() {
        dialog1 = Dialog(this) // Use 'this' or 'requireContext()' depending on your context

        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog1.setCancelable(true)
        dialog1.setContentView(R.layout.dialog_forget_password)

        val cnicTextInputLayout = dialog1.findViewById<TextInputLayout>(R.id.etcnic)
        val cnicEditText = cnicTextInputLayout.editText // Find the EditText within the TextInputLayout

        val btn = dialog1.findViewById<MaterialButton>(R.id.btnEnter)
        btn.setOnClickListener {
            var cnic = cnicEditText?.text.toString() // Retrieve the text from the EditText

            checkCnicInFirebase(formatCNIC(cnic))
            dialog1.dismiss()
        }

        dialog1.show()
    }


    private fun checkCnicInFirebase(cnic: String) {
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.isInvestorExist(cnic)
                .addOnCompleteListener { task ->
                    utils.endLoadingAnimation()

                    if (task.isSuccessful) {
                        if (task.result.size() > 0) {
                            var token: String = ""
                            val documents = task.result
                            var user: User? = null
                            for (document in documents) {
                                user = document.toObject(User::class.java)
                                token = document.id
                            }
                            investor=user!!
                            if (user?.status.equals(constants.INVESTOR_STATUS_ACTIVE) ||
                                user?.status.equals(constants.INVESTOR_STATUS_PENDING) ||
                                user?.status.equals(constants.INVESTOR_STATUS_INCOMPLETE)
                            ) {
                                checkNomineeCnic()
                              /*  val intent = Intent(mContext, ActivityPhoneOtp::class.java)
                                intent.putExtra("cnic", cnic)
                                startActivity(intent)*/
                            } else if (user?.status.equals(constants.INVESTOR_STATUS_BLOCKED)) {
                                Toast.makeText(mContext, "Investor CNIC blocked", Toast.LENGTH_SHORT).show()
                            } else if (documents.size() == 0) {
                                    Toast.makeText(mContext, "Investor CNIC does Not exist 1", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(mContext, "Investor CNIC does Not exist 2", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message + "", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun checkNomineeCnic() {

            dialog1 = Dialog(this) // Use 'this' or 'requireContext()' depending on your context

            dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog1.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog1.setCancelable(true)
            dialog1.setContentView(R.layout.dialog_forget_password)

            val cnicTextInputLayout = dialog1.findViewById<TextInputLayout>(R.id.etcnic)
            val cnicEditText = cnicTextInputLayout.editText // Find the EditText within the TextInputLayout

            val btn = dialog1.findViewById<MaterialButton>(R.id.btnEnter)
            val title = dialog1.findViewById<TextView>(R.id.title)
        title.text="Enter your Nominee CNIC"
            btn.setOnClickListener {
                var cnic = cnicEditText?.text.toString() // Retrieve the text from the EditText
                checkNomineeCNIC(cnic)
                dialog1.dismiss()
            }

            dialog1.show()

    }



    fun checkNomineeCNIC(cnic:String)
    {
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.checkNomineeCNIC(cnic)
                .addOnCompleteListener { task ->

                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {


                        if (task.result.size() > 0) {
                            var token: String = ""
                            val documents = task.result
                            var nominee: ModelNominee? = null
                            for (document in documents) {
                                nominee = document.toObject(ModelNominee::class.java)

                            }
                            modelNominee=nominee!!
                            if (modelNominee.docID==investor.id)
                             {
                                showChangePasswordDialog()
                                /*  val intent = Intent(mContext, ActivityPhoneOtp::class.java)
                                  intent.putExtra("cnic", cnic)
                                  startActivity(intent)*/
                            }  else if (documents.size() == 0) {
                                Toast.makeText(mContext, "Nominee CNIC does Not exist  ", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(mContext, "Nominee CNIC does Not exist  ", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else
                    {
                        Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message + "", Toast.LENGTH_SHORT).show()
                }
        }
    }



    fun showChangePasswordDialog() {

        val dialog = Dialog (mContext)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_update_taken_pin)
        val etPin1 = dialog.findViewById<EditText>(R.id.etPin1)
        val etPin2 = dialog.findViewById<EditText>(R.id.etPin2)
        val etPin3 = dialog.findViewById<EditText>(R.id.etPin3)
        val etPin4 = dialog.findViewById<EditText>(R.id.etPin4)
        val etPin5 = dialog.findViewById<EditText>(R.id.etPin5)
        val etPin6 = dialog.findViewById<EditText>(R.id.etPin6)
        val tvClearAll = dialog.findViewById<TextView>(R.id.tvClearAll)
        val btnSetPin = dialog.findViewById<Button>(R.id.btnSetpin)

        etPin1.requestFocus();
        utils.moveFocus( listOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6))

        tvClearAll.setOnClickListener{
            utils.clearAll( listOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6))
            etPin1.requestFocus();

        }
        btnSetPin.setOnClickListener {
            //
            if(!utils.checkEmpty( listOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6))){

                investor.pin= etPin1.text.toString()+ etPin2.text.toString()+ etPin3.text.toString()+ etPin4.text.toString()+ etPin5.text.toString()+ etPin6.text.toString()
                saveUser(investor)

            }
            else Toast.makeText(mContext, "Please enter 6 Digits Pin!", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }



    fun saveUser(user:User){


        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.updateUser(user).observe(this@ActivityLogin) {
                utils.endLoadingAnimation()
                if (it == true) {

                    sharedPrefManager.putToken(user.id)
                    Toast.makeText(mContext, "Password Update Successfully", Toast.LENGTH_SHORT).show()


                    startActivity(Intent(mContext,ActivityLogin::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                    finish()
                }
                else Toast.makeText(mContext, "Failed to Update Password", Toast.LENGTH_SHORT).show()

            }
        }
    }

    fun formatCNIC(input: String): String {
        val formattedCNIC = StringBuilder()

        if (input.length == 13) {
            formattedCNIC.append(input.substring(0, 5))
            formattedCNIC.append("-")
            formattedCNIC.append(input.substring(5, 12))
            formattedCNIC.append("-")
            formattedCNIC.append(input[12])
        } else {
            // If input length is not 13, return the input as is
            return input
        }
        return formattedCNIC.toString()
    }


           /////this function will store

    private fun saveAdminAccounts()
    {
        lifecycleScope.launch {
            userViewModel.getUserAccounts(constants.ADMIN)
                .addOnCompleteListener{task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        val list = ArrayList<ModelBankAccount>()
                        if(task.result.size()>0){
                            for (document in task.result) list.add( document.toObject(ModelBankAccount::class.java))
                            sharedPrefManager.putAdminBankList(list)
                           // Toast.makeText(mContext, constants.ACCOPUNT_ADDED_MESSAGE, Toast.LENGTH_SHORT).show()
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

    //to store admin accounts///

    private fun saveUserAccounts() {
        lifecycleScope.launch {
            userViewModel.getUserAccounts(sharedPrefManager.getToken())
                .addOnCompleteListener { task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        val list = ArrayList<ModelBankAccount>()
                        if(task.result.size()>0){
                            for (document in task.result) list.add( document.toObject(ModelBankAccount::class.java))
                            sharedPrefManager.putInvestorBankList(list)
                           // Toast.makeText(mContext, constants.ACCOPUNT_ADDED_MESSAGE, Toast.LENGTH_SHORT).show()
                        }
                    }else Toast.makeText(
                        mContext,
                        constants.SOMETHING_WENT_WRONG_MESSAGE,
                        Toast.LENGTH_SHORT
                    ).show()

                }
                .addOnFailureListener {
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message + "", Toast.LENGTH_SHORT).show()

                }
        }


    }

    override fun onBackPressed() {
        finishAffinity()
    }
}


