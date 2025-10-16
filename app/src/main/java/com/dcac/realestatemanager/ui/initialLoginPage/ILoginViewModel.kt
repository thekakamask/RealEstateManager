package com.dcac.realestatemanager.ui.initialLoginPage

interface ILoginViewModel {

    fun signIn(email: String, password: String)
    fun signUp(email: String, password: String, agentName: String)
}