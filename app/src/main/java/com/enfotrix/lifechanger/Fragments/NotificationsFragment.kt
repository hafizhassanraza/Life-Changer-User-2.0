package com.enfotrix.lifechanger.Fragments

import User
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorSpace.Model
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.ModelAnnouncement
import com.enfotrix.lifechanger.Models.ModelFA
import com.enfotrix.lifechanger.Models.NotificationsViewModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.FragmentNotificationsBinding
import com.enfotrix.lifechanger.ui.ActivityInvestorAccounts
import com.enfotrix.lifechanger.ui.ActivityLogin
import com.enfotrix.lifechanger.ui.ActivityUpdatePassword
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val db = Firebase.firestore
    private lateinit var utils: Utils
    private lateinit var investor:User

    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private val PICK_IMAGE_REQUEST = 1

    private lateinit var user: User
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog
        private lateinit var dialogPinUpdate: Dialog
        private lateinit var dialogPhoto: Dialog
    private lateinit var dialogUpdateTaken: Dialog
    private val userViewModel: UserViewModel by viewModels()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.layInvestorAccount.setOnClickListener {
            startActivity(Intent(context, ActivityInvestorAccounts::class.java))
        }
        binding.layLogut.setOnClickListener { task ->
            showDialog()
        }

        binding.imgUser.setOnClickListener {
            showPhotoDialog()
        }

        mContext = requireContext()
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        investor=sharedPrefManager.getUser()
        setimage()
        binding.updatepassword.setOnClickListener {
            showUpdatePinDialog()
        }

        checkData()
        setData()
        return root
    }

    private fun showPhotoDialog() {


        dialogPhoto = Dialog(requireContext())
        dialogPhoto.setContentView(R.layout.item_profile_view)
        dialogPhoto.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogPhoto.setCancelable(true)
        val upload = dialogPhoto.findViewById<ImageView>(R.id.uploadpic)
        val profile = dialogPhoto.findViewById<ImageView>(R.id.profile)

        setimage()
        Glide.with(mContext).load(investor)
            .placeholder(R.drawable.ic_launcher_background).into(profile)


        upload.setOnClickListener {
            openGallery()
         //   dialogPhoto.dismiss()
        }
dialogPhoto.show()
    }




    fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*" // Allow only images

        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // Check if the intent data is not null and the data is valid
            data?.data?.let { selectedImageUri ->
                // Now you can use the selectedImageUri to do further operations, such as uploading the image
                // For instance, you can display the selected image in an ImageView
                // imageView.setImageURI(selectedImageUri)
                // Or perform an upload operation using this URI
                uploadImage(selectedImageUri)
            }
        }
    }


    private fun uploadImage(imageUri: Uri) {
        // Assuming utils, userViewModel, sharedPrefManager, mContext, binding, dialogPhoto are properly initialized

        utils.startLoadingAnimation()
        val user = sharedPrefManager.getUser()

        user.photo = imageUri.toString()
var investor=sharedPrefManager.getUser()
        lifecycleScope.launch {



            try {
                userViewModel.updateUser(user).observe(this@NotificationsFragment) { success ->
                    if (success) {

                        Glide.with(mContext)
                            .load(investor.photo)
                            .centerCrop()
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(binding.imgUser)

                        utils.endLoadingAnimation()
                        dialogPhoto.dismiss()
                        Toast.makeText(mContext, "Profile photo updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        dialogPhoto.dismiss()
                        Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Handle exceptions here
                utils.endLoadingAnimation()
                dialogPhoto.dismiss()
                Toast.makeText(mContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }


    private  fun  setimage()
    {

        lifecycleScope.launch {
            userViewModel.getUser(sharedPrefManager.getToken())
                .addOnCompleteListener()
                {task->


                    if(task.isSuccessful)
                    {
                        var docu=task.result
                        investor= docu.toObject(investor::class.java)!!
                    }
                }
        }
    }

    private fun checkData() {
        if (!sharedPrefManager.getUser().fa_id.equals("")) {
            db.collection(constants.FA_COLLECTION).document(sharedPrefManager.getUser().fa_id)
                .addSnapshotListener { snapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText(mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    snapshot?.let { document ->
                        val modelFA = document.toObject<ModelFA>()
                        if (modelFA != null) {
                            sharedPrefManager.saveFA(modelFA)
                            setData()
                        }
                    }
                }
        }

        db.collection(constants.ANNOUNCEMENT_COLLECTION).document("Rx3xDtgwOH7hMdWxkf94")
            .addSnapshotListener { snapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapshot?.let { document ->
                    val announcement = document.toObject<ModelAnnouncement>()
                    if (announcement != null) {
                        sharedPrefManager.putAnnouncement(announcement)
                        setData()
                    }
                }
            }
    }

    private fun setData() {
        Glide.with(mContext).load(sharedPrefManager.getUser().photo).centerCrop()
            .placeholder(R.drawable.ic_launcher_background).into(binding.imgUser);
        binding.tvUserName.text = sharedPrefManager.getUser().firstName
        binding.tvCNIC.text = sharedPrefManager.getUser().cnic
        binding.tvAddress.text = sharedPrefManager.getUser().address
        binding.tvFatherName.text = sharedPrefManager.getUser().lastName
        binding.tvPhoneNumber.text = sharedPrefManager.getUser().phone

        binding.tvNomineeName.text = sharedPrefManager.getNominee().firstName
        binding.tvNomineeFatherName.text = sharedPrefManager.getNominee().lastName
        binding.tvNomineeCnic.text = sharedPrefManager.getNominee().cnic
        binding.tvNomineeAddress.text = sharedPrefManager.getNominee().address
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showDialog(): Boolean {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.logout_dialog, null)
        val buttonYes: Button = dialogView.findViewById(R.id.btn_yes)
        val buttonNo: Button = dialogView.findViewById(R.id.btn_no)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        builder.setCancelable(true)
        var flag = false

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        buttonYes.setOnClickListener {
            sharedPrefManager.clearWholeSharedPref()
            sharedPrefManager.logOut()
            startActivity(Intent(requireContext(), ActivityLogin::class.java))
            requireActivity().finish()
            alertDialog.dismiss()
        }

        buttonNo.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
        return false
    }

    private fun showUpdatePinDialog() {
        dialogPinUpdate = Dialog(requireContext())
        dialogPinUpdate.setContentView(R.layout.dialog_for_update_pin)
        dialogPinUpdate.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogPinUpdate.setCancelable(true)

        val pin1 = dialogPinUpdate.findViewById<EditText>(R.id.etPin1)
        val pin2 = dialogPinUpdate.findViewById<EditText>(R.id.etPin2)
        val pin3 = dialogPinUpdate.findViewById<EditText>(R.id.etPin3)
        val pin4 = dialogPinUpdate.findViewById<EditText>(R.id.etPin4)
        val pin5 = dialogPinUpdate.findViewById<EditText>(R.id.etPin5)
        val pin6 = dialogPinUpdate.findViewById<EditText>(R.id.etPin6)
        val btnSetPin = dialogPinUpdate.findViewById<Button>(R.id.btnSetpin)

        pin1.requestFocus()
        utils.moveFocus(listOf(pin1, pin2, pin3, pin4, pin5, pin6))

        val tvClearAll = dialogPinUpdate.findViewById<TextView>(R.id.tvClearAll)
        tvClearAll.setOnClickListener {
            utils.clearAll(listOf(pin1, pin2, pin3, pin4, pin5, pin6))
            pin1.requestFocus()
        }

        btnSetPin.setOnClickListener {
            val completePin = "${pin1.text}${pin2.text}${pin3.text}${pin4.text}${pin5.text}${pin6.text}"
            if (completePin == sharedPrefManager.getUser().pin) {
                if (completePin.contains("-")) {
                    Toast.makeText(requireContext(), "Enter valid pin", Toast.LENGTH_SHORT).show()
                } else {
                    startActivity(Intent(requireContext(),ActivityUpdatePassword::class.java ))
                    activity?.finish()

                }
            } else {
                Toast.makeText(requireContext(), "Invalid password, try another", Toast.LENGTH_SHORT).show()
            }
        }

        dialogPinUpdate.show()
    }












}
