package com.gulserakh.sweets

import android.app.Application
import com.gulserakh.sweets.data.AdminBootstrap
import com.gulserakh.sweets.data.DatabaseHelper

class SweetsApplication : Application() {
    val database: DatabaseHelper by lazy { DatabaseHelper(this) }

    override fun onCreate() {
        super.onCreate()
        database.seedDemoUsersIfEmpty()
        AdminBootstrap.ensureInitialAdmin(database)
    }
}
