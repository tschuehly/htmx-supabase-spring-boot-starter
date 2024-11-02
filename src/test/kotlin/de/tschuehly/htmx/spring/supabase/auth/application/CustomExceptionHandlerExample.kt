package de.tschuehly.htmx.spring.supabase.auth.application

import de.tschuehly.htmx.spring.supabase.auth.exception.*
import de.tschuehly.htmx.spring.supabase.auth.exception.email.OtpEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.PasswordRecoveryEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.RegistrationConfirmationEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.SuccessfulPasswordUpdate
import de.tschuehly.htmx.spring.supabase.auth.exception.handler.SupabaseExceptionHandler
import de.tschuehly.htmx.spring.supabase.auth.exception.info.*
import de.tschuehly.htmx.spring.supabase.auth.exception.info.MissingCredentialsException.Companion.MissingCredentials
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ResponseBody

@ControllerAdvice
class CustomExceptionHandlerExample : SupabaseExceptionHandler {
    @ResponseBody
    override fun handleMissingCredentialsException(exception: MissingCredentialsException): Any {
        return when (exception.message) {
            MissingCredentials.EMAIL_MISSING.message -> "You need to supply an email"
            MissingCredentials.PASSWORD_MISSING.message -> "You need to supply an password"
            MissingCredentials.PASSWORD_AND_EMAIL_MISSING.message -> "You need to supply both password and email"
            else -> "MissingCredentialsException"
        }
    }


    @ResponseBody
    override fun handleInvalidLoginCredentialsException(exception: InvalidLoginCredentialsException): Any {
        return "InvalidLoginCredentialsException"
    }

    @ResponseBody
    override fun handleUserNeedsToConfirmEmailForEmailChange(exception: UserNeedsToConfirmEmailForEmailChangeException): Any {
        return "UserNeedsToConfirmEmailForEmailChangeException"
    }

    @ResponseBody
    override fun handleUserNeedsToConfirmEmailBeforeLogin(exception: UserNeedsToConfirmEmailBeforeLoginException): Any {
        return "UserNeedsToConfirmEmailBeforeLoginException"
    }

    @ResponseBody
    override fun handleSuccessfulRegistration(exception: RegistrationConfirmationEmailSent): Any {
        return "SuccessfulRegistrationConfirmationEmailSent"
    }

    @ResponseBody
    override fun handlePasswordRecoveryEmailSent(exception: PasswordRecoveryEmailSent): Any {
        return "PasswordRecoveryEmailSent"
    }

    @ResponseBody
    override fun handleSuccessfulPasswordUpdate(exception: SuccessfulPasswordUpdate): Any {
        return "SuccessfulPasswordUpdate"
    }

    @ResponseBody
    override fun handleOtpEmailSent(exception: OtpEmailSent): Any {
        return "OtpEmailSent"
    }

    @ResponseBody
    override fun handleUserAlreadyRegisteredException(exception: UserAlreadyRegisteredException): Any {
        return "UserAlreadyRegisteredException"
    }

    @ResponseBody
    override fun handleWeakPasswordException(exception: WeakPasswordException): Any {
        return "WeakPasswordException"
    }

    @ResponseBody
    override fun handlePasswordChangeError(exception: NewPasswordShouldBeDifferentFromOldPasswordException): Any {
        return "NewPasswordShouldBeDifferentFromOldPasswordException"
    }

    @ResponseBody
    override fun handleMissingServiceRoleForAdminAccessException(exception: MissingServiceRoleForAdminAccessException): Any {
        return "MissingServiceRoleForAdminAccessException"
    }

    @ResponseBody
    override fun handleSupabaseAuthException(exception: SupabaseAuthException): Any {
        return exception.error
    }

    @ResponseBody
    override fun handleUnknownSupabaseException(exception: UnknownSupabaseException): Any {
        return "UnknownSupabaseException"
    }

    override fun handleOtpExpiredException(exception: OtpExpiredException): Any {
        return "OtpExpiredException"
    }


}