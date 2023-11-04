package com.enfotrix.lifechanger.Fragments

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.ContactUsModel
import com.enfotrix.lifechanger.databinding.FragmentDashboardBinding
import com.enfotrix.lifechanger.Models.DashboardViewModel
import com.enfotrix.lifechanger.Models.InvestmentModel
import com.enfotrix.lifechanger.Models.ModelAnnouncement
import com.enfotrix.lifechanger.Models.ModelFA
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null


    private val db = Firebase.firestore

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    //private lateinit var addBalanceDialog: Dialog

    //private lateinit var user: User
   private lateinit var designatorWhatsapp:String
    private lateinit var designatorMail:String
    private lateinit var designatorPhone:String
    var list=ArrayList<ContactUsModel>()
    var listDesignation=ArrayList<String>()
    var listwhatsapp=ArrayList<String>()
    var listmail=ArrayList<String>()
    var listPhoneNumber=ArrayList<String>()
    //private lateinit var contactUsModel: ContactUsModel
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog
    private lateinit var dialogFA : Dialog
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        //dialog intilization.
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_for_contact_us)
        dialogFA=Dialog(requireContext())
            dialogFA.setContentView(R.layout.dialog_for_financial_advisor)

        sharedPrefManager = SharedPrefManager(mContext)

        ///Manage Contact us button . our target is to make such a
        // design in which on top i have to make spinner and below code.

        binding.contactLinear.setOnClickListener {

            showCustomDialog()
        }

        var list=ArrayList<ContactUsModel>()
        db.collection("ContactUs").get()
            .addOnSuccessListener { querySnapshot ->
                // Clear the existing list to avoid duplicates
                list.clear()
                for (document in querySnapshot.documents) {
                    val contactUsModel = document.toObject<ContactUsModel>()
                    if (contactUsModel != null) {
                        // Add the UserData object to the list
                        list.add(contactUsModel)
                    }
                }

                for(doc in list){
                    listDesignation.add(doc.designation)
                }

                for(doc in list){
                    listwhatsapp.add(doc.wa)
                }
                for(doc in list){
                    listmail.add(doc.email)
                }
                for(doc in list){
                    listPhoneNumber.add(doc.mobile)
                }






            }.addOnFailureListener{ exception->
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }

        val whatsapp = dialog.findViewById<ImageView>(R.id.whatsapp)
        val mail = dialog.findViewById<ImageView>(R.id.mail)
        val phone = dialog.findViewById<ImageView>(R.id.dailor)

        whatsapp?.setOnClickListener {
             openWhatsApp(designatorWhatsapp)

        }

        mail?.setOnClickListener {

            openEmail(designatorMail)
        }
        phone?.setOnClickListener{
            openDialer(designatorPhone)

        }




        binding.myFinancialAdvisor.setOnClickListener{

            showFAdata()



        }


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
        binding.tvAnnouncement.text=sharedPrefManager.getAnnouncement().announcement

        if(sharedPrefManager.getFA().cnic.equals("default"))
        {
            binding.tvFAName.text="N/A"
            binding.tvFADesignation.text="N/A"
        }
        else {
            binding.tvFAName.text = sharedPrefManager.getFA().firstName
            binding.tvFADesignation.text = sharedPrefManager.getFA().designantion
            Glide.with(mContext).load(sharedPrefManager.getFA().photo).centerCrop().placeholder(R.drawable.
            ic_launcher_background).into(binding.imgFA);
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showCustomDialog() {
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        val spinnerWithdrawType = dialog.findViewById<Spinner>(R.id.spinnerWithdrawType)
        if (listDesignation.isNotEmpty()) {
            val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner,listDesignation)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerWithdrawType.adapter = adapter


            spinnerWithdrawType.setSelection(0) // Set the first index as the selected value
            spinnerWithdrawType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedDesignator = listDesignation[position]
                    val designator=dialog.findViewById<TextView>(R.id.designator)
                    designator.setText(selectedDesignator)



                            designatorWhatsapp = listwhatsapp.get(position)
                            designatorMail=listmail.get(position)
                            designatorPhone=listPhoneNumber.get(position)



                }
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        } else {
            // Set a default empty value in the spinner when the list is empty
            val emptyAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner, listOf("Empty"))
            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerWithdrawType.adapter = emptyAdapter

            // Set the spinner to display "Empty" as the selected value
            spinnerWithdrawType.setSelection(0)
            spinnerWithdrawType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Handle the selected value when the list is empty
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
        dialog.show()
    }

    private fun showFAdata()
    {
        dialogFA.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogFA.setCancelable(true)
        dialogFA.findViewById<TextView>(R.id.tvFA).text = sharedPrefManager.getFA().id.toString()
        dialogFA.findViewById<TextView>(R.id.tvFname).text = sharedPrefManager.getFA().firstName.toString()
        dialogFA.findViewById<TextView>(R.id.tvCnic).text = sharedPrefManager.getFA().cnic.toString()
        dialogFA.findViewById<TextView>(R.id.tvaddress).text = sharedPrefManager.getFA().address.toString()

        dialogFA.show()

    }










    private fun openWhatsApp(phone:String) {
//        val phoneNumber = "+923036307725" // Replace with the phone number you want to chat with
        val message = "Hello, this is a custom message" // Replace with the message you want to send

        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$message")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }



    private   fun openEmail(email:String) {
        val subject = "Hello, this is the email subject" // Replace with the email subject
        val message = "This is the email message body" // Replace with the email message body

        val uri = Uri.parse("mailto:$email?subject=$subject&body=$message")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        startActivity(intent)
    }


    private fun openDialer(phoneNumber: String) {
        val uri = Uri.parse("tel:$phoneNumber")
        val intent = Intent(Intent.ACTION_DIAL, uri)
        startActivity(intent)
    }























}