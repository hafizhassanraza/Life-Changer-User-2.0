package com.enfotrix.life_changer_user_2_0.ui

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.InvestmentViewModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityWithdrawBinding
import java.util.Locale

class ActivityWithdraw : AppCompatActivity() {



    private lateinit var selectedCalendar: Calendar
    private var startFormattedDate = ""
    private var endFormattedDate= ""
    @RequiresApi(Build.VERSION_CODES.N)
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    private lateinit var binding: ActivityWithdrawBinding
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext=this@ActivityWithdraw
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)




        binding.imgBack.setOnClickListener{finish()}

        val spinner = binding.spWithdraws

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.withdraw_options, // Replace with your array of items
            R.layout.item_investment_selection_spiner // Use the custom layout
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        binding.rvWithdraws.layoutManager = LinearLayoutManager(mContext)

        binding.spWithdraws.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                // Show a Toast message when an item is selected
                //val selectedItem = parentView?.getItemAtPosition(position).toString()

                // Check the index and show a different message for 1st and 2nd index
                when (position) {
                    0 -> {
                        binding.rvWithdraws.adapter= investmentViewModel.getApprovedWithdrawReqAdapter(constants.FROM_APPROVED_WITHDRAW_REQ)
                    }
                    1 -> {
                        binding.rvWithdraws.adapter= investmentViewModel.getPendingWithdrawReqAdapter(constants.FROM_PENDING_WITHDRAW_REQ)
                    }
                }


            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing if nothing is selected
            }
        }



        //withdraw_options

    }

 /*   private fun setupTabLayout() {
        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab,
            position ->
            if(position==0) tab.text ="Approved"
            else if(position==1) tab.text="Pending" }.attach()
    }

    private fun setupViewPager() {
        val adapter = WithdrawViewPagerAdapter(this, 2)
        binding.viewPager.adapter = adapter
    }

    private fun getData(){
        utils.startLoadingAnimation()
        lifecycleScope.launch{
            investmentViewModel.getWithdrawsReq(sharedPrefManager.getToken())
                .addOnCompleteListener{task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {

                        val list = ArrayList<TransactionModel>()
                        if(task.result.size()>0){

                            for (document in task.result) list.add( document.toObject(TransactionModel::class.java))
                            sharedPrefManager.putWithdrawReqList(list)
                            setupViewPager()
                            setupTabLayout()



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



    override fun onBackPressed() {
        val viewPager = binding.viewPager
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }*/

}