package com.example.pocketbrain.models

import java.io.Serializable
import java.util.Date
import java.util.UUID

/**
 * Model class representing a financial transaction
 */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var amount: Double,
    var category: String,
    var date: Date,
    var isExpense: Boolean,
    var notes: String = ""
) : Serializable