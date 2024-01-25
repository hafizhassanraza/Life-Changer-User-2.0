package com.enfotrix.life_changer_user_2_0.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.enfotrix.life_changer_user_2_0.Fragments.FragmentApprovedInvest
import com.enfotrix.life_changer_user_2_0.Fragments.FragmentPendingInvest

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
