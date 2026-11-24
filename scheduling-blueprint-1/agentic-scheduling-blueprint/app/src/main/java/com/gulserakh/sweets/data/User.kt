package com.gulserakh.sweets.data

/** Domain model of a user stored in shop.db. */
data class User(
    val id: Long = 0,
    val fullName: String = "",
    val username: String? = null,
    val phone: String? = null,
    val passwordHash: String,
    val role: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

object Roles {
    const val CUSTOMER = "customer"
    const val ADMIN = "admin"
}
