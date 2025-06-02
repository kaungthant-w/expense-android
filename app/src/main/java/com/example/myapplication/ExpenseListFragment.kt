package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ExpenseListFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var noDataTextView: TextView
    private lateinit var expenseAdapter: ExpenseAdapter
    private var expenseList = mutableListOf<ExpenseItem>()
    private lateinit var languageManager: LanguageManager
    
    companion object {
        private const val ARG_FILTER_TYPE = "filter_type"
        
        const val FILTER_ALL = "all"
        const val FILTER_TODAY = "today"
        const val FILTER_WEEK = "week"
        const val FILTER_MONTH = "month"
        
        fun newInstance(filterType: String): ExpenseListFragment {
            val fragment = ExpenseListFragment()
            val args = Bundle()
            args.putString(ARG_FILTER_TYPE, filterType)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_expense_list, container, false)
    }
      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize language manager
        languageManager = LanguageManager.getInstance(requireContext())
        
        recyclerView = view.findViewById(R.id.recyclerViewExpenses)
        noDataTextView = view.findViewById(R.id.noDataTextView)
        
        setupRecyclerView()
        updateUITexts()
        
        // Get filter type from arguments
        val filterType = arguments?.getString(ARG_FILTER_TYPE) ?: FILTER_ALL
        loadExpensesForFilter(filterType)
    }
    
    private fun updateUITexts() {
        noDataTextView.text = languageManager.getString("no_data_available")
    }
    
    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(expenseList,
            onDeleteClick = { position -> 
                // Delegate to MainActivity
                (activity as? MainActivity)?.deleteExpenseFromFragment(position, getFilteredExpenses())
            },
            onEditClick = { position -> 
                // Delegate to MainActivity
                (activity as? MainActivity)?.editExpenseFromFragment(position, getFilteredExpenses())
            },
            onItemClick = { position -> 
                // Delegate to MainActivity
                (activity as? MainActivity)?.openExpenseDetailFromFragment(position, getFilteredExpenses())
            }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = expenseAdapter
            setHasFixedSize(false)
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        }
    }
    
    private fun loadExpensesForFilter(filterType: String) {
        // Get expenses from MainActivity
        val mainActivity = activity as? MainActivity
        val allExpenses = mainActivity?.getAllExpenses() ?: emptyList()
        
        // Filter expenses based on filter type
        val filteredExpenses = when (filterType) {
            FILTER_TODAY -> filterTodayExpenses(allExpenses)
            FILTER_WEEK -> filterWeekExpenses(allExpenses)
            FILTER_MONTH -> filterMonthExpenses(allExpenses)
            else -> allExpenses
        }
          expenseList.clear()
        expenseList.addAll(filteredExpenses)
        expenseAdapter.notifyDataSetChanged()
        
        // Show/hide no data message
        if (filteredExpenses.isEmpty()) {
            recyclerView.visibility = View.GONE
            noDataTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            noDataTextView.visibility = View.GONE
        }
    }
    
    private fun filterTodayExpenses(expenses: List<ExpenseItem>): List<ExpenseItem> {
        val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
        return expenses.filter { it.date == today }
    }
    
    private fun filterWeekExpenses(expenses: List<ExpenseItem>): List<ExpenseItem> {
        val calendar = java.util.Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time
        
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        
        return expenses.filter { expense ->
            try {
                val expenseDate = dateFormat.parse(expense.date)
                expenseDate?.let { it >= startDate && it <= endDate } ?: false
            } catch (e: Exception) {
                false
            }
        }
    }
    
    private fun filterMonthExpenses(expenses: List<ExpenseItem>): List<ExpenseItem> {
        val calendar = java.util.Calendar.getInstance()
        val currentMonth = calendar.get(java.util.Calendar.MONTH)
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        
        return expenses.filter { expense ->
            try {
                val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val expenseDate = dateFormat.parse(expense.date)
                if (expenseDate != null) {
                    calendar.time = expenseDate
                    calendar.get(java.util.Calendar.MONTH) == currentMonth &&
                    calendar.get(java.util.Calendar.YEAR) == currentYear
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
    
    fun refreshExpenses() {
        val filterType = arguments?.getString(ARG_FILTER_TYPE) ?: FILTER_ALL
        loadExpensesForFilter(filterType)
    }
    
    private fun getFilteredExpenses(): List<ExpenseItem> {
        return expenseList.toList()
    }
}
