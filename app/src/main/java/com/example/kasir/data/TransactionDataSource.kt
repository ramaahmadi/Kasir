package com.example.kasir.data

import android.content.Context
import android.content.SharedPreferences
import com.example.kasir.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TransactionDataSource {
    private const val PREFS_NAME = "transactions_prefs"
    private const val KEY_TRANSACTIONS = "transactions"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val prefs = getPrefs(context)
        val gson = Gson()
        val json = gson.toJson(transactions)
        prefs.edit().putString(KEY_TRANSACTIONS, json).commit()
    }
    
    fun loadTransactions(context: Context): List<Transaction> {
        val prefs = getPrefs(context)
        val json = prefs.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        val gson = Gson()
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun addTransaction(context: Context, transaction: Transaction) {
        val transactions = loadTransactions(context).toMutableList()
        transactions.add(transaction)
        saveTransactions(context, transactions)
    }
}
