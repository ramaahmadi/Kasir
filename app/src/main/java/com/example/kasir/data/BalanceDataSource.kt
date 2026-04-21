package com.example.kasir.data

import android.content.Context
import android.content.SharedPreferences
import com.example.kasir.model.DailyBalance
import java.text.SimpleDateFormat
import java.util.*

object BalanceDataSource {
    private const val PREFS_NAME = "balance_prefs"
    private const val KEY_TODAY_BALANCE = "today_balance"
    private const val KEY_BALANCE_DATE = "balance_date"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun getTodayBalance(context: Context): DailyBalance? {
        val prefs = getPrefs(context)
        val dateStr = prefs.getString(KEY_BALANCE_DATE, null) ?: return null
        val balance = prefs.getFloat(KEY_TODAY_BALANCE, 0f).toDouble()
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        
        // Check if the stored balance is from today
        if (dateStr == todayStr) {
            val date = dateFormat.parse(dateStr)
            return DailyBalance(
                date = date ?: Date(),
                openingBalance = balance
            )
        }
        
        return null
    }
    
    fun setTodayBalance(context: Context, balance: Double) {
        val prefs = getPrefs(context)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        
        prefs.edit()
            .putString(KEY_BALANCE_DATE, todayStr)
            .putFloat(KEY_TODAY_BALANCE, balance.toFloat())
            .apply()
    }
    
    fun isTodayBalanceSet(context: Context): Boolean {
        return getTodayBalance(context) != null
    }
}
