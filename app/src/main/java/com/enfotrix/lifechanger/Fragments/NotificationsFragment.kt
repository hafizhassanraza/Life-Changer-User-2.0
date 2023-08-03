package com.enfotrix.lifechanger.Fragments

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.ModelAnnouncement
import com.enfotrix.lifechanger.Models.ModelFA
import com.enfotrix.lifechanger.databinding.FragmentNotificationsBinding
import com.enfotrix.lifechanger.Models.NotificationsViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.ui.ActivityInvestorAccounts
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
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog

    // This property is only valid between onCreateView and
    // onDestroyView.
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

        binding.layInvestorAccount.setOnClickListener{

            startActivity(Intent(context, ActivityInvestorAccounts::class.java))
        }


        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        checkData()
        setData()

        return root
    }







    private fun checkData() {

        if(!sharedPrefManager.getUser().fa_id.equals("")){

            db.collection(constants.FA_COLLECTION).document(sharedPrefManager.getUser().fa_id)
                .addSnapshotListener { snapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText( mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText( mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
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


        Glide.with(mContext).load(sharedPrefManager.getUser().photo).centerCrop().placeholder(R.drawable.ic_launcher_background).into(binding.imgUser);
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
}