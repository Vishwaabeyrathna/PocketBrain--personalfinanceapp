package com.example.pocketbrain.models

/**
 * Model class representing a transaction category
 */
data class Category(
    val id: String,
    val name: String,
    val color: Int,
    val isExpense: Boolean = true
)