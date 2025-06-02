package com.example.myapplication

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ExpenseViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentTitles = arrayOf(
        "All",
        "Today", 
        "This Week",
        "This Month"
    )
    
    private val fragmentFilters = arrayOf(
        ExpenseListFragment.FILTER_ALL,
        ExpenseListFragment.FILTER_TODAY,
        ExpenseListFragment.FILTER_WEEK,
        ExpenseListFragment.FILTER_MONTH
    )

    override fun getItemCount(): Int = fragmentTitles.size

    override fun createFragment(position: Int): Fragment {
        return ExpenseListFragment.newInstance(fragmentFilters[position])
    }
    
    fun getTabTitle(position: Int): String {
        return fragmentTitles[position]
    }
}
