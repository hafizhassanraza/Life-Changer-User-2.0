package com.enfotrix.lifechanger.ui

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.enfotrix.lifechanger.Adapters.InvestmentViewPagerAdapter
import com.enfotrix.lifechanger.Adapters.WithdrawViewPagerAdapter
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.InvestmentViewModel
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.ActivityInvestmentBinding
import com.enfotrix.lifechanger.databinding.ActivityWithdrawBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ActivityInvestment : AppCompatActivity() {

    private lateinit var binding: ActivityInvestmentBinding
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext=this@ActivityInvestment
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        setTitle("My Investments")

        getData()




    }








    private fun setupTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if(position==0) tab.text ="Approved"
            else if(position==1) tab.text="Pending"
        }.attach()
    }

    private fun setupViewPager() {
                  val adapter = InvestmentViewPagerAdapter(this, 2)
        binding.viewPager.adapter = adapter
    }

                              //
    private fun getData(){
        utils.startLoadingAnimation()
        lifecycleScope.launch{
            investmentViewModel.getInvestmentReq(sharedPrefManager.getToken())
                .addOnCompleteListener{task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        val list = ArrayList<TransactionModel>()
                        if(task.result.size()>0){
                            for (document in task.result) list.add( document.toObject(TransactionModel::class.java))
                            sharedPrefManager.putInvestmentReqList(list)

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
    }



}