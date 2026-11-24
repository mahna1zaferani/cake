package com.gulserakh.sweets.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gulserakh.sweets.data.DatabaseHelper
import com.gulserakh.sweets.data.PasswordHasher
import com.gulserakh.sweets.data.Roles
import com.gulserakh.sweets.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data class Error(val message: String) : LoginState()
    data object Registered : LoginState()
    data class Success(val user: User, val role: String) : LoginState()
}

class LoginViewModel(private val db: DatabaseHelper) : ViewModel() {

    val state: MutableState<LoginState> = mutableStateOf(LoginState.Idle)

    /** Login: reads user data from shop.db and verifies the password. */
    fun login(identifier: String, password: String) {
        val id = identifier.trim()
        if (id.isEmpty() || password.isEmpty()) {
            state.value = LoginState.Error(EMPTY_FIELDS_MSG)
            return
        }
        state.value = LoginState.Loading
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) { db.getUserByIdentifier(id) }
            state.value = if (user != null && PasswordHasher.verify(password, user.passwordHash)) {
                LoginState.Success(user, user.role)
            } else {
                LoginState.Error(BAD_CREDENTIALS_MSG)
            }
        }
    }

    /** Registration: writes new user data into shop.db. */
    fun register(
        fullName: String,
        phone: String?,
        username: String?,
        password: String,
        role: String = Roles.CUSTOMER
    ) {
        val name = fullName.trim()
        if (name.isEmpty()) {
            state.value = LoginState.Error(REGISTER_NAME_REQUIRED_MSG)
            return
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            state.value = LoginState.Error(REGISTER_PASSWORD_TOO_SHORT_MSG)
            return
        }
        val id = username?.trim()?.takeIf { it.isNotEmpty() }
        val ph = phone?.trim()?.takeIf { it.isNotEmpty() }
        if (id == null && ph == null) {
            state.value = LoginState.Error(REGISTER_IDENTIFIER_REQUIRED_MSG)
            return
        }
        state.value = LoginState.Loading
        viewModelScope.launch {
            val rowId = withContext(Dispatchers.IO) {
                db.registerUser(name, ph, id, PasswordHasher.hash(password), role)
            }
            state.value = if (rowId > 0) LoginState.Registered else LoginState.Error(REGISTER_DUPLICATE_MSG)
        }
    }

    companion object {
        const val EMPTY_FIELDS_MSG = "لطفاً نام کاربری یا شماره موبایل و رمز عبور را وارد کنید."
        const val BAD_CREDENTIALS_MSG = "نام کاربری یا رمز عبور اشتباه است."
        const val MIN_PASSWORD_LENGTH = 6
        const val REGISTER_NAME_REQUIRED_MSG = "نام و نام خانوادگی الزامی است."
        const val REGISTER_PASSWORD_TOO_SHORT_MSG = "رمز عبور باید حداقل ۶ کاراکتر باشد."
        const val REGISTER_IDENTIFIER_REQUIRED_MSG = "حداقل یکی از نام کاربری یا شماره موبایل را وارد کنید."
        const val REGISTER_DUPLICATE_MSG = "نام کاربری یا شماره موبایل قبلاً ثبت شده است."
    }
}
