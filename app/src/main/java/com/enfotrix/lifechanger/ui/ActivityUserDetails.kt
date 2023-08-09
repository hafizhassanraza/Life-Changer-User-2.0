package com.enfotrix.lifechanger.ui

import InvestorNomineeModel
import User
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.InvestmentModel
import com.enfotrix.lifechanger.Models.InvestmentViewModel
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.ModelNominee
import com.enfotrix.lifechanger.Models.NomineeViewModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.ActivityUserDetailsBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ActivityUserDetails : AppCompatActivity() {


    private val IMAGE_PICKER_REQUEST_CODE = 200

    private lateinit var uploadedImageURI: Uri

    private  var imageURI: Uri?=null
    private  var NomineeCnicFrontURI: Uri?=null
    private  var NomineeCnicBackURI: Uri?=null
    private  var UserCnicFrontURI: Uri? =null
    private  var UserCnicBackURI: Uri?=null

    private lateinit var imgSelectCnicBack: ImageView
    private lateinit var imgSelectCnicFront: ImageView

    private var NomineeCnicFront:Boolean=false
    private var NomineeCnicBack:Boolean=false
    private var UserCnicFront:Boolean=false
    private var UserCnicBack:Boolean=false
    private var UserProfilePhoto:Boolean=false

    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()
    private val investmentViewModel: InvestmentViewModel by viewModels()

    private lateinit var binding : ActivityUserDetailsBinding

    private lateinit var imgProfilePhoto: ImageView



    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivityUserDetails
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        binding.btnStart.visibility = View.GONE
        //imageURI = Uri.parse("https://www.google.com/search?q=place+holder+image&tbm=isch&ved=2ahUKEwjMh5DChNr_AhVMricCHXqrDJsQ2-cCegQIABAA&oq=place+holder+&gs_lcp=CgNpbWcQARgAMgUIABCABDIFCAAQgAQyBQgAEIAEMgUIABCABDIFCAAQgAQyBQgAEIAEMgUIABCABDIFCAAQgAQyBQgAEIAEMgUIABCABDoECCMQJzoKCAAQigUQsQMQQzoHCAAQigUQQzoICAAQgAQQsQM6CAgAELEDEIMBUIoOWJ4wYOY9aAFwAHgAgAH0AYgByRmSAQQyLTE1mAEAoAEBqgELZ3dzLXdpei1pbWfAAQE&sclient=img&ei=OuaVZMyCJszcnsEP-tay2Ak&bih=1081&biw=1920#imgrc=wqFTpxdyrrUpwM");

        checkData()



        binding.layInvestorPhone.setOnClickListener{

            //Toast.makeText(mContext, sharedPrefManager.getNominee().acc_number+"", Toast.LENGTH_SHORT).show()

            if(sharedPrefManager.isPhoneNumberAdded()) Toast.makeText(mContext, "Phone already added", Toast.LENGTH_SHORT).show()
            else startActivity(Intent(mContext,ActivityPhoneNumber::class.java).putExtra(constants.KEY_ACTIVITY_FLOW,constants.VALUE_ACTIVITY_FLOW_USER_DETAILS))
        }






        binding.layAddNominee.setOnClickListener{

            //Toast.makeText(mContext, sharedPrefManager.getNominee().acc_number+"", Toast.LENGTH_SHORT).show()

            if(sharedPrefManager.isNomineeAdded()) Toast.makeText(mContext, "Nominee already added", Toast.LENGTH_SHORT).show()
            else startActivity(Intent(mContext,ActivityNominee::class.java).putExtra(constants.KEY_ACTIVITY_FLOW,constants.VALUE_ACTIVITY_FLOW_USER_DETAILS))
        }
        binding.layInvestorBank.setOnClickListener{

            if(sharedPrefManager.isUserBankAdded()) Toast.makeText(mContext, "User Bank already added!", Toast.LENGTH_SHORT).show()
            else showAddAccountDialog(constants.VALUE_DIALOG_FLOW_INVESTOR_BANK)


        }
        binding.layInvestorNomineeBank.setOnClickListener{

            if(sharedPrefManager.isNomineeAdded()){
                if(sharedPrefManager.isNomineeBankAdded()) Toast.makeText(mContext, "Nominee Bank details already added!", Toast.LENGTH_SHORT).show()
                else showAddAccountDialog(constants.VALUE_DIALOG_FLOW_NOMINEE_BANK)
            }
            else Toast.makeText(mContext, "Please Add Nominee First!", Toast.LENGTH_SHORT).show()

        }
        binding.layInvestorProfilePhoto.setOnClickListener{
            //showPhotoDialog()
            if(sharedPrefManager.isUserPhotoAdded()) Toast.makeText(mContext, "User photo already added!", Toast.LENGTH_SHORT).show()
            else showPhotoDialog()
        }
        binding.layInvestorCNIC.setOnClickListener{
            if(sharedPrefManager.isUserCnicAdded()) Toast.makeText(mContext, "User CNIC already added!", Toast.LENGTH_SHORT).show()
            else showAddCnicDialog(constants.VALUE_DIALOG_FLOW_INVESTOR_CNIC)
        }
        binding.layInvestorNomineeCNIC.setOnClickListener{

            if(sharedPrefManager.isNomineeCnicAdded()) Toast.makeText(mContext, "Nominee CNIC already added!", Toast.LENGTH_SHORT).show()
            else showAddCnicDialog(constants.VALUE_DIALOG_FLOW_NOMINEE_CNIC)

        }
        binding.btnStart.setOnClickListener{

            startApp()
        }

    }
    fun startApp(){
        var user:User=sharedPrefManager.getUser()

        if(user.status.equals(constants.INVESTOR_STATUS_INCOMPLETE)){
            utils.startLoadingAnimation()
            lifecycleScope.launch {
                user.status= constants.INVESTOR_STATUS_PENDING
                userViewModel.updateUser(user).observe(this@ActivityUserDetails) {
                    if (it == true) {
                        sharedPrefManager.saveUser(user)
                        lifecycleScope.launch {
                            utils.endLoadingAnimation()
                            investmentViewModel.addInvestment(InvestmentModel(sharedPrefManager.getToken(),
                                "0",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "")).observe(this@ActivityUserDetails){

                                if (it == true){
                                    //dialog.dismiss()
                                    Toast.makeText(mContext, "Profile Completed Successfully!", Toast.LENGTH_SHORT).show()

                                    sharedPrefManager.setLogin(it)
                                    startActivity(Intent(mContext,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                                    finish()
                                }
                                else {
                                    Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                                }


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
    }




    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            when {
                UserCnicFront -> {
                    UserCnicFrontURI = data?.data
                    imgSelectCnicFront.setImageResource(R.drawable.check_small)
                }
                UserCnicBack -> {
                    UserCnicBackURI = data?.data
                    imgSelectCnicBack.setImageResource(R.drawable.check_small)
                }
                NomineeCnicFront -> {
                    NomineeCnicFrontURI = data?.data
                    imgSelectCnicFront.setImageResource(R.drawable.check_small)
                }
                NomineeCnicBack -> {
                    NomineeCnicBackURI = data?.data
                    imgSelectCnicBack.setImageResource(R.drawable.check_small)
                }
                UserProfilePhoto -> {
                    Glide.with(mContext).load(data?.data).into(imgProfilePhoto)
                    imageURI = data?.data
                }
            }




        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_right_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.top_logout -> {
                Toast.makeText(applicationContext, "click on setting", Toast.LENGTH_LONG).show()
                true
            }
            R.id.top_contactUs ->{
                Toast.makeText(applicationContext, "click on share", Toast.LENGTH_LONG).show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    @SuppressLint("ResourceAsColor")
    private fun checkData() {

        var checkCounter:Int=0
        if(sharedPrefManager.isNomineeAdded()){
            checkCounter++
            binding.tvHeaderNominee.text ="Completed"
            binding.tvHeaderNominee.setTextColor(Color.parseColor("#2F9B47"))
            binding.imgCheckNominee.setImageResource(R.drawable.check_small)
        }
        if(sharedPrefManager.isNomineeBankAdded()){
            checkCounter++
            binding.tvHeaderNomineeBank.text ="Completed"
            binding.tvHeaderNomineeBank.setTextColor(Color.parseColor("#2F9B47"))
            binding.imgCheckNomineeBank.setImageResource(R.drawable.check_small)
        }
        if(sharedPrefManager.isUserBankAdded()){
            checkCounter++
            binding.tvHeaderUserBank.text ="Completed"
            binding.tvHeaderUserBank.setTextColor(Color.parseColor("#2F9B47"))
            binding.imgCheckUserBank.setImageResource(R.drawable.check_small)
        }

        if(sharedPrefManager.isUserPhotoAdded()){
            checkCounter++
            binding.tvHeaderUserPhoto.text ="Completed"
            binding.tvHeaderUserPhoto.setTextColor(Color.parseColor("#2F9B47"))
            binding.imgCheckUserPhoto.setImageResource(R.drawable.check_small)
        }
        if(sharedPrefManager.isUserCnicAdded()){
            checkCounter++
            binding.tvHeaderUserCnic.text ="Completed"
            binding.tvHeaderUserCnic.setTextColor(Color.parseColor("#2F9B47"))
            binding.imgCheckUserCnic.setImageResource(R.drawable.check_small)
        }
        if(sharedPrefManager.isNomineeCnicAdded()){
            checkCounter++
            binding.tvHeaderNomineeCnic.text ="Completed"
            binding.tvHeaderNomineeCnic.setTextColor(Color.parseColor("#2F9B47"))
            binding.imgCheckNomineeCnic.setImageResource(R.drawable.check_small)
        }
        if(sharedPrefManager.isPhoneNumberAdded()){
            checkCounter++
            binding.tvHeaderUserPhoneNumber.text ="Completed"
            binding.tvHeaderUserPhoneNumber.setTextColor(Color.parseColor("#2F9B47"))
            binding.imgCheckUserPhoneNumber.setImageResource(R.drawable.check_small)
        }


        if(checkCounter==7) {
            binding.btnStart.visibility = View.VISIBLE

        }
    }

    fun showAddCnicDialog(type:String) {

        NomineeCnicFront=false
        NomineeCnicBack=false
        UserCnicFront=false
        UserCnicBack=false
        UserProfilePhoto=false

        NomineeCnicFrontURI=null
        NomineeCnicBackURI=null
        UserCnicFrontURI=null
        UserCnicBackURI=null
        imageURI=null

        dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_upload_cnic)

         imgSelectCnicFront = dialog.findViewById<ImageView>(R.id.imgSelectCnicFront)
         imgSelectCnicBack = dialog.findViewById<ImageView>(R.id.imgSelectCnicBack)
        val tvSelectCnicFront = dialog.findViewById<TextView>(R.id.tvSelectCnicFront)
        val tvSelectCnicBack = dialog.findViewById<TextView>(R.id.tvSelectCnicBack)
        val tvHeaderDesc = dialog.findViewById<TextView>(R.id.tvHeaderDesc)
        val tvHeader = dialog.findViewById<TextView>(R.id.tvHeader)
        val btnUploadCNIC = dialog.findViewById<Button>(R.id.btnUploadCNIC)
        if(type.equals(constants.VALUE_DIALOG_FLOW_NOMINEE_CNIC)){
            tvHeader.setText("Nominee CNIC Photo !")
            tvHeaderDesc.setText("Upload both (Front and Back) side photo of your Nominee CNIC")
        }


        tvSelectCnicFront.setOnClickListener {

            NomineeCnicFront = type == constants.VALUE_DIALOG_FLOW_NOMINEE_CNIC
            UserCnicFront = type == constants.VALUE_DIALOG_FLOW_INVESTOR_CNIC
            UserCnicBack = false
            NomineeCnicBack = false



            val pickImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImage, IMAGE_PICKER_REQUEST_CODE)

        }
        tvSelectCnicBack.setOnClickListener {




            NomineeCnicFront = false
            UserCnicFront = false
            UserCnicBack = type == constants.VALUE_DIALOG_FLOW_INVESTOR_CNIC
            NomineeCnicBack = type == constants.VALUE_DIALOG_FLOW_NOMINEE_CNIC



            val pickImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImage, IMAGE_PICKER_REQUEST_CODE)


        }
        btnUploadCNIC.setOnClickListener {
            if(type.equals(constants.VALUE_DIALOG_FLOW_NOMINEE_CNIC)){

                if(NomineeCnicFrontURI!=null && NomineeCnicBackURI!=null ){
                    Toast.makeText(mContext, NomineeCnicFrontURI.toString()+"", Toast.LENGTH_SHORT).show()
                    Toast.makeText(mContext, NomineeCnicBackURI.toString()+"", Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch{
                        addUserCNIC(NomineeCnicFrontURI!!,NomineeCnicBackURI!!,type)
                    }
                }
                else Toast.makeText(mContext, "Please Select both photos", Toast.LENGTH_SHORT).show()




            }
            else if(type.equals(constants.VALUE_DIALOG_FLOW_INVESTOR_CNIC)){
                if(UserCnicFrontURI!=null && UserCnicBackURI!=null )
                    lifecycleScope.launch{
                        addUserCNIC(UserCnicFrontURI!!,UserCnicBackURI!!,type)
                    }
                else Toast.makeText(mContext, "Please Select Image", Toast.LENGTH_SHORT).show()
            }

        }

        dialog.show()
    }
    fun showAddAccountDialog(type:String) {

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

        if(type.equals(constants.VALUE_DIALOG_FLOW_NOMINEE_BANK)){
            tvHeaderBank.setText("Add Nominee Account");
            tvHeaderBankDisc.setText("Add nominee bank account details for funds transfer and other servicest");
        }

        btnAddAccount.setOnClickListener {

            addUserBankAccount(type,spBank.selectedItem.toString(), etAccountTittle.text.toString(), etAccountNumber.text.toString())
        }

        dialog.show()
    }
    fun showPhotoDialog() {


        NomineeCnicFront=false
        NomineeCnicBack=false
        UserCnicFront=false
        UserCnicBack=false
        UserProfilePhoto=false

        dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_profile_photo_upload)

         imgProfilePhoto = dialog.findViewById<ImageView>(R.id.imgProfilePhoto)
        val tvSelect = dialog.findViewById<TextView>(R.id.tvSelect)
        val btnUplodProfile = dialog.findViewById<Button>(R.id.btnUplodProfile)

        tvSelect.setOnClickListener{
            UserProfilePhoto=true
            val pickImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImage, IMAGE_PICKER_REQUEST_CODE)
        }

        btnUplodProfile.setOnClickListener {

            lifecycleScope.launch {
                if (imageURI!=null) addUserPhoto(imageURI!!,"InvestorProfilePhoto")
                else Toast.makeText(mContext, "Please Select Image", Toast.LENGTH_SHORT).show()
            }


        }

        dialog.show()
    }
    fun addUserBankAccount(type: String ,bankName: String, accountTittle: String, accountNumber: String) {

        if(type.equals(constants.VALUE_DIALOG_FLOW_NOMINEE_BANK)){
            utils.startLoadingAnimation()

            var nominee: ModelNominee = sharedPrefManager.getNominee()
            nominee.acc_number=accountNumber
            nominee.acc_tittle=accountTittle
            nominee.bank_name=bankName

            lifecycleScope.launch {
                nomineeViewModel.updateNominee(nominee)
                    .observe(this@ActivityUserDetails) {
                        utils.endLoadingAnimation()
                        if (it == true) {

                            sharedPrefManager.saveNominee(nominee)
                            sharedPrefManager.putNomineeBank(true)
                            Toast.makeText(mContext, constants.ACCOPUNT_ADDED_MESSAGE, Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            checkData()
                        }
                        else {
                            Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }

                    }
            }
        }
        else {

            utils.startLoadingAnimation()
            lifecycleScope.launch {
                userViewModel.addUserAccount(ModelBankAccount("",bankName,accountTittle,accountNumber,""))
                    .observe(this@ActivityUserDetails) {
                    utils.endLoadingAnimation()
                    if (it == true) {
                        sharedPrefManager.putUserBank(true)
                        Toast.makeText(mContext, constants.ACCOPUNT_ADDED_MESSAGE, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        checkData()
                    }
                    else {
                        Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }

                }
            }

        }



    }


    suspend fun addUserPhoto(imageUri: Uri, type: String ) {

       /* utils.startLoadingAnimation()
        userViewModel.uploadPhotoRefrence(imageUri,type)
            .downloadUrl.addOnSuccessListener {uri ->
                var user:User=sharedPrefManager.getUser()
                user.photo= uri.toString()
                lifecycleScope.launch {

                    userViewModel.updateUser(user).observe(this@ActivityUserDetails) {
                        utils.endLoadingAnimation()
                        if (it == true) {
                            sharedPrefManager.saveUser(user)
                            sharedPrefManager.putUserPhoto(true)
                            Toast.makeText(mContext, "Profile Photo Updated", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            checkData()
                        }
                        else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                    }
                }
        }*/

        utils.startLoadingAnimation()
        userViewModel.uploadPhoto(imageUri, type)
            .addOnSuccessListener {taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    var user:User=sharedPrefManager.getUser()
                    user.photo = uri.toString()

                    lifecycleScope.launch {

                        userViewModel.updateUser(user).observe(this@ActivityUserDetails) {
                            utils.endLoadingAnimation()
                            if (it == true) {
                                sharedPrefManager.saveUser(user)
                                sharedPrefManager.putUserPhoto(true)
                                Toast.makeText(mContext, "Profile Photo Updated", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                checkData()
                            }
                            else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                        }
                    }

                }
                    .addOnFailureListener { exception ->
                        Toast.makeText(mContext, exception.message+"", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(mContext, "Failed to upload profile pic", Toast.LENGTH_SHORT).show()
            }
    }

    suspend fun addUserCNIC(imageUriFront: Uri,imageUriBack: Uri, type: String ) {

        //Toast.makeText(mContext, "debug1", Toast.LENGTH_SHORT).show()




        utils.startLoadingAnimation()

        val frontUploadTask = userViewModel.uploadPhoto(imageUriFront, type + "Front") // from_investorFront
        val backUploadTask = userViewModel.uploadPhoto(imageUriBack, type + "Back")  // from_investorBack

        val downloadUrlTasks = mutableListOf<Task<Uri>>()

        downloadUrlTasks.add(frontUploadTask.continueWithTask { task ->
            if (task.isSuccessful) {
                return@continueWithTask frontUploadTask.result?.storage?.downloadUrl
            } else {
                task.exception?.let {
                    throw it
                }
            }
        })
        downloadUrlTasks.add(backUploadTask.continueWithTask { task ->
            if (task.isSuccessful) {
                return@continueWithTask backUploadTask.result?.storage?.downloadUrl
            } else {
                task.exception?.let {
                    throw it
                }
            }
        })


        Tasks.whenAllComplete(downloadUrlTasks).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrls = mutableListOf<String>()
                for (downloadUrlTask in downloadUrlTasks) {
                    if (downloadUrlTask.isSuccessful) {
                        val downloadUrl = downloadUrlTask.result?.toString()
                        downloadUrl?.let { downloadUrls.add(it) }
                    }
                }


                if(type.equals(constants.VALUE_DIALOG_FLOW_INVESTOR_CNIC)){
                    // Update user photo in the user object
                    val user: User = sharedPrefManager.getUser()
                    user.cnic_front = downloadUrls[0] // Assuming the front image URL is at index 0
                    user.cnic_back = downloadUrls[1] // Assuming the front image URL is at index 0



                    // Update user in the database
                    lifecycleScope.launch {
                        userViewModel.updateUser(user).observe(this@ActivityUserDetails) { updateResult ->
                            utils.endLoadingAnimation()
                            if (updateResult == true) {

                                sharedPrefManager.saveUser(user)
                                sharedPrefManager.putUserCnic(true)
                                Toast.makeText(mContext, "CNIC Updated!", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                checkData()
                            } else {
                                Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                else if(type.equals(constants.VALUE_DIALOG_FLOW_NOMINEE_CNIC)){
                    // Update user photo in the user object


                    // Update user in the database
                    lifecycleScope.launch {

                        var nominee:ModelNominee=sharedPrefManager.getNominee()
                        nominee.cnic_front = downloadUrls[0] // Assuming the front image URL is at index 0
                        nominee.cnic_back = downloadUrls[1] // Assuming the front image URL is at index 0

                        nomineeViewModel.updateNominee(nominee).observe(this@ActivityUserDetails) {
                            utils.endLoadingAnimation()


                            if (it == true) {
                                sharedPrefManager.saveNominee(nominee)
                                sharedPrefManager.putNomineeCnic(true)
                                Toast.makeText(mContext, "CNIC Updated!", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                checkData()
                            }
                            else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()


                        }
                    }
                }

            } else {
                // Handle the failure
                task.exception?.let {
                    // Handle the exception
                }
            }
        }





    }


}