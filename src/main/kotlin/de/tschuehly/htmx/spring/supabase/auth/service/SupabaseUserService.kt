package de.tschuehly.htmx.spring.supabase.auth.service

import de.tschuehly.htmx.spring.supabase.auth.types.SupabaseUser
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

interface SupabaseUserService {
    fun signUpWithEmail(email: String, password: String, response: HttpServletResponse)
    fun loginWithEmail(email: String, password: String, response: HttpServletResponse)

    fun handleClientAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    )

    fun logout(request: HttpServletRequest, response: HttpServletResponse)
    fun setRolesWithRequest(request: HttpServletRequest, userId: String, roles: List<String>?)
    fun sendPasswordRecoveryEmail(email: String)
    fun updatePassword(request: HttpServletRequest, password: String)
    fun sendOtp(email: String)
    fun signInAnonymously(request: HttpServletRequest, response: HttpServletResponse)
    fun linkAnonToIdentity(email: String, request: HttpServletRequest, response: HttpServletResponse)
    fun authenticate(jwt: String): SupabaseUser
    fun authenticateWithCurrentSession(): SupabaseUser
}