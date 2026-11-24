package com.gulserakh.sweets.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLite database `shop.db` (path via `context.getDatabasePath("shop.db")`,
 * typically `/data/data/com.gulserakh.sweets/databases/shop.db`).
 *
 * The `users` table (per spec):
 *   id            INTEGER PRIMARY KEY (unique)
 *   full_name     TEXT (نام و نام خانوادگی)
 *   username      TEXT UNIQUE (در صورت نیاز پروژه)
 *   phone         TEXT UNIQUE (شماره موبایل)
 *   password_hash TEXT (هش‌شده، بدون متن ساده)
 *   role          TEXT (customer | admin)
 *   created_at    INTEGER (timestamp millis)
 * Plus an extra `is_active` column used by the login lookup.
 *
 * All tables are created with `CREATE TABLE IF NOT EXISTS`, so an existing
 * `shop.db` is reused as-is — nothing is recreated or duplicated.
 */
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "shop.db"
        const val DB_VERSION = 1
        private const val TABLE_USERS = "users"
        private const val COL_ID = "id"
        private const val COL_FULL_NAME = "full_name"
        private const val COL_USERNAME = "username"
        private const val COL_PHONE = "phone"
        private const val COL_PASSWORD_HASH = "password_hash"
        private const val COL_ROLE = "role"
        private const val COL_IS_ACTIVE = "is_active"
        private const val COL_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FULL_NAME TEXT NOT NULL,
                $COL_USERNAME TEXT UNIQUE,
                $COL_PHONE TEXT UNIQUE,
                $COL_PASSWORD_HASH TEXT NOT NULL,
                $COL_ROLE TEXT NOT NULL DEFAULT 'customer',
                $COL_IS_ACTIVE INTEGER NOT NULL DEFAULT 1,
                $COL_CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // shop.db version 1 — no migration needed yet.
    }

    /** Looks up a user by username OR phone number; only active users match. */
    fun getUserByIdentifier(identifier: String): User? {
        val db = readableDatabase
        db.query(
            TABLE_USERS,
            arrayOf(COL_ID, COL_FULL_NAME, COL_USERNAME, COL_PHONE, COL_PASSWORD_HASH, COL_ROLE, COL_CREATED_AT),
            "(username = ? OR phone = ?) AND is_active = 1",
            arrayOf(identifier, identifier),
            null, null, null, 1
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                User(
                    id = cursor.getLong(0),
                    fullName = cursor.getString(1) ?: "",
                    username = cursor.getString(2),
                    phone = cursor.getString(3),
                    passwordHash = cursor.getString(4),
                    role = cursor.getString(5),
                    isActive = true,
                    createdAt = cursor.getLong(6)
                )
            } else {
                null
            }
        }
    }

    /**
     * Registers a new user (phone and/or username). Returns the new row id,
     * or -1 when username/phone is already taken (UNIQUE constraints).
     */
    fun registerUser(fullName: String, phone: String?, username: String?, passwordHash: String, role: String): Long {
        val values = ContentValues().apply {
            put(COL_FULL_NAME, fullName)
            put(COL_USERNAME, username)
            put(COL_PHONE, phone)
            put(COL_PASSWORD_HASH, passwordHash)
            put(COL_ROLE, role)
            put(COL_IS_ACTIVE, 1)
            put(COL_CREATED_AT, System.currentTimeMillis())
        }
        return writableDatabase.insert(TABLE_USERS, null, values)
    }

    fun getUserCount(): Int =
        readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_USERS", null).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }

    /**
     * Seeds the demo customer account (phone + hashed password), only when
     * the users table is empty — an existing database is never modified.
     */
    fun seedDemoUsersIfEmpty() {
        if (getUserCount() > 0) return
        registerUser(
            username = null,
            phone = "09121111222",
            fullName = "مشتری تستی",
            passwordHash = PasswordHasher.hash("123456"),
            role = Roles.CUSTOMER
        )
    }
}
