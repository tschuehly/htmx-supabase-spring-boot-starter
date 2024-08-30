package de.tschuehly.htmx.spring.supabase.auth.service

import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserMfaFactor
import io.github.jan.supabase.gotrue.user.UserSession
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

interface SupabaseUserService {
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
    fun signUpWithEmailAndMeta(
        request: HttpServletRequest,
        email: String,
        password: String,
        meta: Map<String, String>,
        response: HttpServletResponse
    ): UserInfo?
    fun signUpWithEmailAndMeta(
        serviceJwt: String,
        email: String,
        password: String,
        meta: Map<String, String>,
        response: HttpServletResponse
    ): UserInfo?

    fun inviteUserByEmail(
        serviceJwt: String,
        email: String,
        redirectUrl: String,
        meta: Map<String, String>,
        response: HttpServletResponse
    )

    fun retrieveMFAFactorsForCurrentUser(request: HttpServletRequest): List<UserMfaFactor>?
    fun enrollSecondFactor(request: HttpServletRequest, issuer: String, deviceName: String): Map<String, String>?
    fun unenrollMfaFactor(request: HttpServletRequest, factorId: String)
    fun createAndVerifyMFAChallenge(request: HttpServletRequest, factorId: String, code: String): UserSession?
    fun loggedInUsingMfa(request: HttpServletRequest): Boolean
}