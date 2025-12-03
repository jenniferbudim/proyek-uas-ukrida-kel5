package com.example.kiptrack.ui.event

import com.example.kiptrack.ui.model.UserRole

sealed interface LoginEvent {
    data class OnIdChange(val value: String) : com.example.kiptrack.ui.event.LoginEvent
    data class OnPasswordChange(val value: String) : com.example.kiptrack.ui.event.LoginEvent
    data class OnRoleSelected(val role: UserRole) : com.example.kiptrack.ui.event.LoginEvent
    object OnToggleAdminMode : com.example.kiptrack.ui.event.LoginEvent
    object OnLoginClicked : com.example.kiptrack.ui.event.LoginEvent
}