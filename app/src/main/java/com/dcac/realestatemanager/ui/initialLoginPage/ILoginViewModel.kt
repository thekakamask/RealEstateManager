package com.dcac.realestatemanager.ui.initialLoginPage

import kotlinx.coroutines.flow.StateFlow

interface ILoginViewModel {

    val uiState: StateFlow<LoginUiState>
    fun signIn(email: String, password: String)
    fun signUp(email: String, password: String, agentName: String)
}