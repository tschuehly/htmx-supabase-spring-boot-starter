package de.tschuehly.supabasesecurityspringbootstarter.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

interface ISupabaseUserService {
    fun signUpWithEmail(email: String, password: String, response: HttpServletResponse)
    fun loginWithEmail(email: String, password: String, response: HttpServletResponse)

    fun authorizeWithJwtOrResetPassword(
        request: HttpServletRequest,
        response: HttpServletResponse
    )

    fun logout(request: HttpServletRequest, response: HttpServletResponse)
    fun setRolesWithRequest(request: HttpServletRequest, userId: String, roles: List<String>?)
    fun sendPasswordRecoveryEmail(email: String)
    fun updatePassword(request: HttpServletRequest, password: String)
    fun sendOtp(email: String)
}