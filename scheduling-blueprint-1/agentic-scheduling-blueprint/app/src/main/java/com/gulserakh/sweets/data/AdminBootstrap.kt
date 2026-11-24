package com.gulserakh.sweets.data

import android.util.Log
import com.gulserakh.sweets.BuildConfig

/**
 * Creates the initial admin account for the admin panel when the users table
 * is empty.
 *
 * Credentials come from the environment settings injected at build time
 * (gradle reads the `CAKE_ADMIN_USERNAME` / `CAKE_ADMIN_PASSWORD` environment
 * variables into `BuildConfig`) — they are NOT hardcoded in the source code.
 * If no password is configured, a strong random one is generated as a fallback.
 */
object AdminBootstrap {
    private const val TAG = "AdminBootstrap"

    fun ensureInitialAdmin(db: DatabaseHelper) {
        if (db.getUserCount() > 0) return

        val username = BuildConfig.ADMIN_USERNAME.ifBlank { "admin" }
        val password = BuildConfig.ADMIN_INITIAL_PASSWORD

        if (password.isBlank()) {
            val generated = PasswordHasher.generateRandomPassword()
            Log.w(TAG, "Initial admin account '$username' created with a generated password (set CAKE_ADMIN_PASSWORD at build time for a known one).")
            db.registerUser(
                username = username,
                phone = null,
                fullName = "مدیر سیستم",
                passwordHash = PasswordHasher.hash(generated),
                role = Roles.ADMIN
            )
        } else {
            db.registerUser(
                username = username,
                phone = null,
                fullName = "مدیر سیستم",
                passwordHash = PasswordHasher.hash(password),
                role = Roles.ADMIN
            )
        }
    }
}
