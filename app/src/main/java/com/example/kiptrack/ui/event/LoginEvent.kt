package com.example.kiptrack.ui.event

import com.example.kiptrack.ui.data.UserRole

sealed interface LoginEvent {
    data class OnIdChange(val value: String) : LoginEvent
    data class OnPasswordChange(val value: String) : LoginEvent
    data class OnRoleSelected(val role: UserRole) : LoginEvent
    object OnToggleAdminMode : LoginEvent
    object OnLoginClicked : LoginEvent
    data class OnLoginSuccess(val role: UserRole, val uid: String) : LoginEvent
}
