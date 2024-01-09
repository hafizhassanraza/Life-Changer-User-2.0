package com.enfotrix.lifechanger.Fragments

import User
import android.app.Dialog
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.databinding.FragmentHomeBinding
import com.enfotrix.lifechanger.Models.HomeViewModel
import com.enfotrix.lifechanger.Models.InvestmentModel
import com.enfotrix.lifechanger.Models.InvestmentViewModel
import com.enfotrix.lifechanger.Models.ModelAnnouncement
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.ModelProfitTax
import com.enfotrix.lifechanger.Models.Notificaion
import com.enfotrix.lifechanger.Models.NotificationData
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.api.ApiUtilities
import com.enfotrix.lifechanger.ui.ActivityInvestment
import com.enfotrix.lifechanger.ui.ActivityNavDrawer
import com.enfotrix.lifechanger.ui.ActivityNewInvestmentReq
import com.enfotrix.lifechanger.ui.ActivityNewWithdrawReq
import com.enfotrix.lifechanger.ui.ActivityProfitTax
import com.enfotrix.lifechanger.ui.ActivityStatment
import com.enfotrix.lifechanger.ui.ActivityTax
import com.enfotrix.lifechanger.ui.ActivityWithdraw
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val userViewModel: UserViewModel by viewModels()
    private val db = Firebase.firestore

    private val investmentViewModel: InvestmentViewModel by viewModels()



    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)




        binding.btnInvest.setOnClickListener{
            if(sharedPrefManager.getUser().status.equals(constants.INVESTOR_STATUS_PENDING)) showDialogRequest()
            else {/*
            //handle the code for notification
                            val notification = Notificaion(user.userdevicetoken, NotificationData("Withdraw Request", "Body work title"))
                            ApiUtilities.api.sendNotification(notification).enqueue(object :
                                Callback<Notification> {
                                override fun onResponse(call: Call<Notification>, response: Response<Notification>) {
                                    if (response.isSuccessful) {
                                        // Handle successful response here
                                        // For example, log success message or perform actions on success
                                        Toast.makeText(mContext, "Notification sent successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Handle unsuccessful response here
                                        // For example, log failure message or handle errors
                                        Toast.makeText(mContext, "Failed to send notification. Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<Notification>, t: Throwable) {
                                    // Handle failure (exception) here
                                    // For example, log the exception or perform actions on failure
                                    Toast.makeText(mContext, "Failed to send notification. Exception: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })

                    }*/

                startActivity(Intent(mContext, ActivityNewInvestmentReq::class.java))

            }
        }


        binding.imgDrawer.setOnClickListener{
            if(sharedPrefManager.getUser().status.equals(constants.INVESTOR_STATUS_PENDING)) showDialogRequest()
            else startActivity(Intent(mContext, ActivityNavDrawer::class.java))

        }

        binding.btnWithdraw.setOnClickListener{
            if(sharedPrefManager.getUser().status.equals(constants.INVESTOR_STATUS_PENDING)) showDialogRequest()
            else startActivity(Intent(mContext, ActivityNewWithdrawReq::class.java))

        }

        binding.layInvestment.setOnClickListener{
            if(sharedPrefManager.getUser().status.equals(constants.INVESTOR_STATUS_PENDING)) showDialogRequest()
            else startActivity(Intent(mContext, ActivityInvestment::class.java))


        }
        binding.layStatment.setOnClickListener{
            if(sharedPrefManager.getUser().status.equals(constants.INVESTOR_STATUS_PENDING)) showDialogRequest()
            else startActivity(Intent(mContext, ActivityStatment::class.java))


        }
        binding.layProfit.setOnClickListener{
            if(sharedPrefManager.getUser().status.equals(constants.INVESTOR_STATUS_PENDING)) showDialogRequest()
            else startActivity(Intent(mContext, ActivityProfitTax::class.java))
        }

        binding.layWithdraw.setOnClickListener{
            if(sharedPrefManager.getUser().status.equals(constants.INVESTOR_STATUS_PENDING)) showDialogRequest()
            else startActivity(Intent(mContext, ActivityWithdraw::class.java))
        }

        binding.layTax.setOnClickListener{
            if(sharedPrefManager.getUser().status.equals(constants.INVESTOR_STATUS_PENDING)) showDialogRequest()
            else startActivity(Intent(mContext, ActivityTax::class.java))

        }



        checkData()
        setData()
        getData()

        return root
    }

    private fun getData(){

        utils.startLoadingAnimation()
        db.collection(constants.TRANSACTION_REQ_COLLECTION)
            .whereEqualTo(constants.INVESTOR_ID, sharedPrefManager.getToken())
            .addSnapshotListener { snapshot, firebaseFirestoreException ->
                utils.endLoadingAnimation()
                firebaseFirestoreException?.let {
                    Toast.makeText(mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                snapshot?.let { task ->

                    val transactionList = task.documents.mapNotNull { document -> document.toObject(TransactionModel::class.java) }
                    sharedPrefManager.putTransactionList(transactionList)
                    sharedPrefManager.putInvestmentReqList(transactionList.filter { it.type == constants.TRANSACTION_TYPE_INVESTMENT })
                    sharedPrefManager.putWithdrawReqList(transactionList.filter { it.type == constants.TRANSACTION_TYPE_WITHDRAW })
                    sharedPrefManager.putProfitList(transactionList.filter { it.type == constants.TRANSACTION_TYPE_PROFIT })
                    sharedPrefManager.putTaxList(transactionList.filter { it.type == constants.TRANSACTION_TYPE_TAX })

                }

            }

    }


    private fun checkData() {



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


        db.collection(constants.INVESTOR_COLLECTION).document(sharedPrefManager.getToken())
            .addSnapshotListener { snapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText( mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapshot?.let { document ->
                    val user = document.toObject<User>()
                    if (user != null) {
                        sharedPrefManager.saveUser(user)
                        setData()
                    }
                }
            }

        db.collection(constants.INVESTMENT_COLLECTION).document(sharedPrefManager.getToken())
            .addSnapshotListener { snapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText( mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapshot?.let { document ->
                    val investment = document.toObject<InvestmentModel>()
                    if (investment != null) {
                        sharedPrefManager.saveInvestment(investment)
                        setData()
                    }
                }
            }
        val listAdminAccounts = ArrayList<ModelBankAccount>()
        val query = db.collection(constants.ACCOUNTS_COLLECTION)
            .whereEqualTo("account_holder", "Admin")

        query.addSnapshotListener { snapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                // Handle any errors that occurred while listening to the snapshot
                Toast.makeText(mContext, firebaseFirestoreException.message.toString(), Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            snapshot?.let { querySnapshot ->
                listAdminAccounts.clear() // Clear the existing list to avoid duplicates

                for (document in querySnapshot) {
                    val modelBankAccount = document.toObject(ModelBankAccount::class.java)
                    if (modelBankAccount.account_holder == constants.ADMIN) {
                        listAdminAccounts.add(modelBankAccount)
                    }
                }

                // Update your UI or perform any action with the updated listAdminAccounts here
            }
        }


    }

    private fun setData() {

        binding.tvAnnouncement.text=sharedPrefManager.getAnnouncement().announcement

        binding.tvUserName.text= sharedPrefManager.getUser().firstName
        //binding.uName.text= sharedPrefManager.getUser().firstName
        //binding.tvBalance.text= sharedPrefManager.getInvestment().investmentBalance




        val investmentBalance = sharedPrefManager.getInvestment().investmentBalance
        val lastProfit = sharedPrefManager.getInvestment().lastProfit
        val lastInvestment = sharedPrefManager.getInvestment().lastInvestment
        val ExpextedSum = getTextFromInvestment(investmentBalance).toDouble() + getTextFromInvestment(lastProfit).toDouble() + getTextFromInvestment(lastInvestment).toDouble()

        binding.tvBalance.text = getTextFromInvestment(investmentBalance)
        binding.availableProfit.text = getTextFromInvestment(lastProfit)
        binding.tvInActiveInvestment.text = getTextFromInvestment(lastInvestment)
        binding.tvExpectedSum.text = getTextFromInvestment(ExpextedSum.toInt().toString())


    }

    fun getTextFromInvestment(value: String?): String {
        return if (value.isNullOrEmpty()) "0" else value
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




    fun showDialogRequest() {

        dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_congratulation)
        dialog.setCancelable(true)
        dialog.show()

    }



}