package com.enfotrix.life_changer_user_2_0.Fragments

import User
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.InvestmentViewModel
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.FragmentPendingReqBinding


class FragmentPendingReq : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private val investmentViewModel:InvestmentViewModel by viewModels()
    private var _binding: FragmentPendingReqBinding? = null
    private val binding get() = _binding!!



    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        _binding = FragmentPendingReqBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        binding.rvWithdrawPending.layoutManager = LinearLayoutManager(mContext)
        binding.rvWithdrawPending.adapter= investmentViewModel.getPendingWithdrawReqAdapter(constants.FROM_PENDING_WITHDRAW_REQ)



        return root

    }



}