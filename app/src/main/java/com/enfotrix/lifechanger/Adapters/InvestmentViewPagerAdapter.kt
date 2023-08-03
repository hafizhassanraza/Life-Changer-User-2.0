package com.enfotrix.lifechanger.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.enfotrix.lifechanger.Fragments.FragmentApprovedInvest
import com.enfotrix.lifechanger.Fragments.FragmentApprovedReq
import com.enfotrix.lifechanger.Fragments.FragmentPendingInvest
import com.enfotrix.lifechanger.Fragments.FragmentPendingReq

class InvestmentViewPagerAdapter (fragmentActivity: FragmentActivity, private var totalCount: Int) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return totalCount
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentApprovedInvest()
            1 -> FragmentPendingInvest()
            else -> FragmentApprovedInvest()
        }
    }
}
