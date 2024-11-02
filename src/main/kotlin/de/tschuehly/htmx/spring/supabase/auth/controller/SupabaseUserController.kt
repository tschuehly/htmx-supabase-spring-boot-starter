package de.tschuehly.htmx.spring.supabase.auth.controller

import de.tschuehly.htmx.spring.supabase.auth.exception.info.MissingCredentialsException.Companion.MissingCredentials
import de.tschuehly.htmx.spring.supabase.auth.service.SupabaseUserService
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Controller
@RequestMapping("api/user")
class SupabaseUserController(
    val supabaseUserService: SupabaseUserService,
) {
    val logger: Logger = LoggerFactory.getLogger(SupabaseUserController::class.java)


    @PostMapping("/login")
    fun login(
        @RequestParam email: String?,
        @RequestParam password: String?,
        response: HttpServletResponse,
    ) {
        checkCredentialsAndExecute(email, password) { checkedEmail, checkedPassword ->
            logger.debug("User with the email $checkedEmail is trying to login")
            supabaseUserService.loginWithEmail(checkedEmail, checkedPassword)
        }
    }

    @PostMapping("/signup")
    fun signUp(
        @RequestParam email: String?,
        @RequestParam password: String?
    ) {
        checkCredentialsAndExecute(email, password) { checkedEmail, checkedPassword ->
            logger.debug("User with the email $checkedEmail is trying to signup")
            supabaseUserService.signUpWithEmail(checkedEmail, checkedPassword)
        }
    }

    @PostMapping("/loginAnon")
    fun anonSignIn() {
        supabaseUserService.signInAnonymously()
    }

    @PostMapping("/loginAnonWithEmail")
    @ResponseBody
    fun anonSignInWithEmail(@RequestParam email: String?) {
        if (email != null) {
            supabaseUserService.signInAnonymouslyWithEmail(email)
        } else {
            MissingCredentials.EMAIL_MISSING.throwExc()
        }
    }


    @PostMapping("/linkIdentity")
    fun linkIdentity(
        @RequestParam email: String?
    ) {
        if (email != null) {
            logger.debug("User with the email $email is linking an Anonymous User")
            supabaseUserService.requestEmailChange(email)
        } else {
            MissingCredentials.EMAIL_MISSING.throwExc()
        }
    }

    @PostMapping("/signInWithMagicLink")
    fun sendEmailOtp(
        @RequestParam email: String?
    ) {
        if (email != null) {
            logger.debug("User with the email $email is trying to sign in with a Magic Link")
            supabaseUserService.signInWithMagicLink(email)
        } else {
            MissingCredentials.EMAIL_MISSING.throwExc()
        }
    }

    @PostMapping("/confirmEmailOtp")
    @ResponseBody
    fun confirmEmailOtp(
        @RequestParam email: String?,
        @RequestParam otp: String?
    ) {
        if (email.isNullOrBlank()) {
            MissingCredentials.EMAIL_MISSING.throwExc()
        }
        if (otp.isNullOrBlank()) {
            MissingCredentials.OTP_MISSING.throwExc()
        }
        logger.debug("User with the email $email is confirming an OTP")
        supabaseUserService.confirmEmailOtp(email!!, otp!!)
    }


    private fun checkCredentialsAndExecute(
        email: String?, password: String?,
        function: (email: String, password: String) -> Unit
    ) {
        when {
            email.isNullOrBlank() && password.isNullOrBlank() ->
                MissingCredentials.PASSWORD_AND_EMAIL_MISSING.throwExc()

            email.isNullOrBlank() ->
                MissingCredentials.EMAIL_MISSING.throwExc()

            password.isNullOrBlank() ->
                MissingCredentials.PASSWORD_MISSING.throwExc()

            else ->
                function(email.trim(), password.trim())
        }
    }

    @PostMapping("/jwt")
    @ResponseBody
    fun authorizeWithJwtOrResetPassword() {
        supabaseUserService.handleClientAuthentication()
    }

    @GetMapping("/logout")
    @ResponseBody
    fun logout() {
        supabaseUserService.logout()
    }

    @PutMapping("/setRoles")
    @ResponseBody
    fun setRoles(
        @RequestParam
        roles: List<String>?,
        @RequestParam
        userId: String,
    ) {
        if (userId == "") {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "UserId required")
        }
        supabaseUserService.setRolesWithRequest(userId, roles)
    }

    @PostMapping("/sendPasswordResetEmail")
    @ResponseBody
    fun sendPasswordResetEmail(
        @RequestParam
        email: String
    ) {
        logger.debug("User with the email $email requested a password reset")
        supabaseUserService.sendPasswordRecoveryEmail(email)
    }

    @PostMapping("/updatePassword")
    @ResponseBody
    fun updatePassword(@RequestParam password: String) {
        supabaseUserService.updatePassword(password)
    }
}
