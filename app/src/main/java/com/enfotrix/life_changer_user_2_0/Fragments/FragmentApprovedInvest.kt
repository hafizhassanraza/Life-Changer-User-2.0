package com.enfotrix.life_changer_user_2_0.Fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.InvestmentViewModel
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.Pdf.PdfTransaction
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.FragmentApprovedInvestBinding


class FragmentApprovedInvest : Fragment() {


    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private var _binding: FragmentApprovedInvestBinding? = null
    private val binding get() = _binding!!

    private val CREATE_PDF_REQUEST_CODE = 123


    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentApprovedInvestBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        binding.rvWithdrawPending.layoutManager = LinearLayoutManager(mContext)
        binding.rvWithdrawPending.adapter= investmentViewModel.getApprovedInvestmentReqAdapter(constants.FROM_APPROVED_INVESTMENT_REQ)

        binding.pdfInvestment.setOnClickListener {
            generatePDF()
        }

        return root
    }

    private fun generatePDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "approved_investment.pdf")
        }
        startActivityForResult(intent, CREATE_PDF_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val outputStream = requireContext().contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val success =
                        PdfTransaction(sharedPrefManager.getInvestmentReqList().filter{
                            it.status.equals(constants.TRANSACTION_STATUS_APPROVED)
                        }.sortedByDescending {
                            it.createdAt
                        }
                        ).generatePdf(
                            outputStream
                        )
                    outputStream.close()
                    if (success) {
                        Toast.makeText(requireContext(), "Saved successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to save", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }


}