package com.enfotrix.lifechanger.Fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.ModelAnnouncement
import com.enfotrix.lifechanger.Models.ModelFA
import com.enfotrix.lifechanger.Models.NotificationsViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.FragmentNotificationsBinding
import com.enfotrix.lifechanger.ui.ActivityInvestorAccounts
import com.enfotrix.lifechanger.ui.ActivityLogin
import com.enfotrix.lifechanger.ui.ActivityUpdatePassword
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val db = Firebase.firestore
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog
        private lateinit var dialogPinUpdate: Dialog
    private lateinit var dialogUpdateTaken: Dialog

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

        mContext = requireContext()
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        binding.updatepassword.setOnClickListener {
            showUpdatePinDialog()
        }

        checkData()
        setData()
        return root
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
