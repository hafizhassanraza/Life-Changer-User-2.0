package com.enfotrix.lifechanger.Fragments

import User
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
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.databinding.FragmentHomeBinding
import com.enfotrix.lifechanger.Models.HomeViewModel
import com.enfotrix.lifechanger.Models.InvestmentModel
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.ModelProfitTax
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.ui.ActivityInvestment
import com.enfotrix.lifechanger.ui.ActivityNewInvestmentReq
import com.enfotrix.lifechanger.ui.ActivityNewWithdrawReq
import com.enfotrix.lifechanger.ui.ActivityProfitTax
import com.enfotrix.lifechanger.ui.ActivityWithdraw
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val userViewModel: UserViewModel by viewModels()
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root



        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        //Toast.makeText(mContext, sharedPrefManager.getToken(), Toast.LENGTH_SHORT).show()


        binding.btnInvest.setOnClickListener{
            if(sharedPrefManager.getUser().status.equals(constants.INVESTOR_STATUS_PENDING)) showDialogRequest()
            else startActivity(Intent(mContext, ActivityNewInvestmentReq::class.java))

        }

        binding.btnWithdraw.setOnClickListener{
            if(sharedPrefManager.getUser().status.equals(constants.INVESTOR_STATUS_PENDING)) showDialogRequest()
            else startActivity(Intent(mContext, ActivityNewWithdrawReq::class.java))

        }

        binding.layInvestment.setOnClickListener{
            if(sharedPrefManager.getUser().status.equals(constants.INVESTOR_STATUS_PENDING)) showDialogRequest()
            else startActivity(Intent(mContext, ActivityInvestment::class.java))


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
            else startActivity(Intent(mContext, ActivityProfitTax::class.java))

        }



        checkData()
        setData()

        return root
    }

    private fun checkData() {


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
        binding.tvUserName.text= sharedPrefManager.getUser().firstName
         binding.tvBalance.text= sharedPrefManager.getInvestment().investmentBalance
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

    /*fun profitTax(){

        utils.startLoadingAnimation()
        lifecycleScope.launch {
            investmentViewModel.getProfitTax(sharedPrefManager.getToken())
                .addOnCompleteListener{task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        val list = ArrayList<ModelProfitTax>()
                        if(task.result.size()>0){
                            for (document in task.result) list.add( document.toObject(ModelProfitTax::class.java))
                            sharedPrefManager.putProfitTaxList(list)

                        }
                    }
                    else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener{
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                }
        }
    }*/

    /*lifecycleScope.launch {
               userViewModel.getUserAccounts(constants.ADMIN)
                   .addOnCompleteListener{task ->
                       utils.endLoadingAnimation()
                       if (task.isSuccessful) {
                           val list = ArrayList<ModelBankAccount>()
                           if(task.result.size()>0){
                               for (document in task.result) list.add( document.toObject(ModelBankAccount::class.java))
                               sharedPrefManager.putAdminBankList(list)
                               Toast.makeText(mContext, constants.ACCOPUNT_ADDED_MESSAGE, Toast.LENGTH_SHORT).show()
                           }
                       }
                       else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                   }
                   .addOnFailureListener{
                       utils.endLoadingAnimation()
                       Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                   }
           }*/

}