package com.transportation.kotline.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.transportation.kotline.onboarding.OnBoardingOneFragment
import com.transportation.kotline.onboarding.OnBoardingThreeFragment
import com.transportation.kotline.onboarding.OnBoardingTwoFragment

class SectionsPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        // slide according to page
        when (position) {
            0 -> fragment = OnBoardingOneFragment()
            1 -> fragment = OnBoardingTwoFragment()
            2 -> fragment = OnBoardingThreeFragment()
        }
        return fragment as Fragment
    }
}