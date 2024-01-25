package com.enfotrix.life_changer_user_2_0.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.enfotrix.life_changer_user_2_0.Fragments.FragmentApprovedReq
import com.enfotrix.life_changer_user_2_0.Fragments.FragmentPendingReq

class WithdrawViewPagerAdapter (fragmentActivity: FragmentActivity, private var totalCount: Int) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return totalCount
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentApprovedReq()
            1 -> FragmentPendingReq()
            else -> FragmentApprovedReq()
        }
    }
}
