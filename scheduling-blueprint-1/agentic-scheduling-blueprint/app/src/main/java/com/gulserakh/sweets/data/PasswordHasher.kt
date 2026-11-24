package com.gulserakh.sweets.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Stores passwords with PBKDF2-HMAC-SHA256 (supported since Android API 26,
 * we target API 28+). Stored format: `pbkdf2:<saltB64>:<hashB64>`.
 * The plaintext password is never stored and never compared
 * (constant-time compare via `MessageDigest.isEqual`).
 */
object PasswordHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 100_000
    private const val KEY_BITS = 256
    private const val SALT_BYTES = 16
    private const val FORMAT_PREFIX = "pbkdf2"
    private val ALLOWED_CHARS = ('A'..'Z') + ('a'..'z') + ('0'..'9')

    /** Produces a salted hash in the form `pbkdf2:<saltB64>:<hashB64>`. */
    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS)
        val key = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec)
        spec.destroy()
        return "$FORMAT_PREFIX:${Base64.encodeToString(salt, Base64.DEFAULT).trim()}:${Base64.encodeToString(key.encoded, Base64.DEFAULT).trim()}"
    }

    /** Verifies a candidate password against a stored hash (constant time). */
    fun verify(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":").filter { it.isNotBlank() }.map { it.trim() }
            if (parts.size != 3 || parts[0] != FORMAT_PREFIX) return false
            val salt = Base64.decode(parts[1], Base64.DEFAULT)
            val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS)
            val key = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec)
            spec.destroy()
            java.security.MessageDigest.isEqual(key.encoded, Base64.decode(parts[2], Base64.DEFAULT))
        } catch (e: Exception) {
            false
        }
    }

    /** Generates a strong random alphanumeric password (fallback for first run). */
    fun generateRandomPassword(length: Int = 16): String {
        val rnd = SecureRandom()
        return List(length) { ALLOWED_CHARS[rnd.nextInt(ALLOWED_CHARS.size)] }.joinToString("")
    }
}
