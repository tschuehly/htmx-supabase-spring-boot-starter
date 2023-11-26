package de.tschuehly.supabasesecurityspringbootstarter.application

import de.tschuehly.supabasesecurityspringbootstarter.exception.*
import de.tschuehly.supabasesecurityspringbootstarter.exception.MissingCredentialsException.Companion.MissingCredentials
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ResponseBody

@ControllerAdvice
class CustomExceptionHandlerTest: SupabaseExceptionHandler {
    @ResponseBody
    override fun handleMissingCredentialsException(exception: MissingCredentialsException): Any {
        return when(exception.message){
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
    override fun handleUserNeedsToConfirmEmail(exception: UserNeedsToConfirmEmailBeforeLoginException): Any {
        return "UserNeedsToConfirmEmailBeforeLoginException"
    }

    @ResponseBody
    override fun handleSuccessfulRegistration(exception: SuccessfulSignUpConfirmationEmailSent): Any {
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
}