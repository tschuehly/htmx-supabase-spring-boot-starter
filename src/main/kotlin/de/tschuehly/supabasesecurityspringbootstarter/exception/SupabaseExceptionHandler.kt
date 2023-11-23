package de.tschuehly.supabasesecurityspringbootstarter.exception

import de.tschuehly.supabasesecurityspringbootstarter.exception.InvalidLoginCredentialsException
import de.tschuehly.supabasesecurityspringbootstarter.exception.MissingCredentialsException
import de.tschuehly.supabasesecurityspringbootstarter.exception.SuccessfulRegistrationConfirmationEmailSent
import de.tschuehly.supabasesecurityspringbootstarter.exception.UserNeedsToConfirmEmailBeforeLoginException
import org.springframework.web.bind.annotation.ExceptionHandler

interface SupabaseExceptionHandler {
    @ExceptionHandler(MissingCredentialsException::class)
    fun handleMissingCredentialsException(exception: MissingCredentialsException): Any

    @ExceptionHandler(InvalidLoginCredentialsException::class)
    fun handleInvalidLoginCredentialsException(exception: InvalidLoginCredentialsException): Any

    @ExceptionHandler(UserNeedsToConfirmEmailBeforeLoginException::class)
    fun handleUserNeedsToConfirmEmail(exception: UserNeedsToConfirmEmailBeforeLoginException): Any

    @ExceptionHandler(SuccessfulRegistrationConfirmationEmailSent::class)
    fun handleSuccessfulRegistration(exception: SuccessfulRegistrationConfirmationEmailSent): Any

    @ExceptionHandler(PasswordRecoveryEmailSent::class)
    fun handlePasswordRecoveryEmailSent(exception: PasswordRecoveryEmailSent): Any

    @ExceptionHandler(SuccessfulPasswordUpdate::class)
    fun handleSuccessfulPasswordUpdate(exception: SuccessfulPasswordUpdate): Any
}