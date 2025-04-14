package com.example.pocketbrain.models

/**
 * Model class representing a monthly budget
 */
data class Budget(
    val amount: Double,
    val month: Int,
    val year: Int
)