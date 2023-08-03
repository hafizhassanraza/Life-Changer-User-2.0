package com.enfotrix.lifechanger.Fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enfotrix.lifechanger.Models.HomeViewModel
import com.enfotrix.lifechanger.Models.NomineeViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.databinding.FragmentHomeBinding

class FragmentNominee : Fragment() {



    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var viewModel: NomineeViewModel


    /*companion object {
        fun newInstance() = FragmentNominee()
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val nomineeViewModel = ViewModelProvider(this).get(NomineeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val root: View = binding.root
        return root



        //return inflater.inflate(R.layout.fragment_nominee, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NomineeViewModel::class.java)
        // TODO: Use the ViewModel
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}