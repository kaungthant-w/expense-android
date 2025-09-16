package com.example.myapplication

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ExpenseViewPagerAdapter(private val fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val languageManager: LanguageManager = LanguageManager.getInstance(fragmentActivity)
    
    private val fragmentFilters = arrayOf(
        ExpenseListFragment.FILTER_TODAY,
        ExpenseListFragment.FILTER_WEEK,
        ExpenseListFragment.FILTER_MONTH,
        ExpenseListFragment.FILTER_ALL
    )

    override fun getItemCount(): Int = fragmentFilters.size

    override fun createFragment(position: Int): Fragment {
        return ExpenseListFragment.newInstance(fragmentFilters[position])
    }
      fun getTabTitle(position: Int): String {
        return when (position) {
            0 -> languageManager.getString("tab_today")
            1 -> languageManager.getString("tab_this_week")
            2 -> languageManager.getString("tab_this_month")
            3 -> languageManager.getString("tab_all")
            else -> "Tab $position"
        }
    }
}
