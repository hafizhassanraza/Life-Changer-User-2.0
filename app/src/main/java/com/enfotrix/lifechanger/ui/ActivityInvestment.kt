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
    lateinit var fabButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext=this@ActivityInvestment
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        setTitle("My Withdraws")
        fabButton = findViewById(R.id.save_as_pdf)

        getData()


       fabButton.setOnClickListener {
           Toast.makeText(this, "hussain", Toast.LENGTH_SHORT).show()
              generatePDF()
       }

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


    private fun generatePDF() {
        val document = PdfDocument()
         val pageWidth = 595 // Adjust this value as needed
         val pageHeight = 842 // Adjust this value as needed


        // Create a page info for the PDF document.
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        // Start a new page.
        val page = document.startPage(pageInfo)

        // Create a canvas for the page.
        val canvas = page.canvas

        // Set up paint for drawing text.
        val paint = Paint()
        paint.textSize = 12f

        // Define the starting position for drawing text.
        var yPosition = 50f

        // Iterate through the list and draw each item on the PDF.
        for (item in sharedPrefManager.getInvestmentReqList()) {
            canvas.drawText(item.toString(), 50f, yPosition, paint)
            yPosition += 20f // Increase the yPosition for the next item.
        }

        // Finish the page and add it to the document.
        document.finishPage(page)

        // Define the file path for the PDF.
        val filePath = Environment.getExternalStorageDirectory().toString() + "/investment_req_list.pdf"

        try {
            // Create a file and write the document content to it.
            val file = File(filePath)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)

            // Close the document and file output stream.
            document.close()
            outputStream.close()

            // Display a toast indicating the PDF is saved.
            Toast.makeText(this, "PDF saved at $filePath", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Handle exceptions if any.
            e.printStackTrace()
            Toast.makeText(this, "Error saving PDF", Toast.LENGTH_SHORT).show()
        }
    }



}