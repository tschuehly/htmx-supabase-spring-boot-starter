package de.tschuehly.htmx.spring.supabase.auth.exception.handler

import de.tschuehly.htmx.spring.supabase.auth.exception.UnknownSupabaseException
import de.tschuehly.htmx.spring.supabase.auth.exception.email.OtpEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.PasswordRecoveryEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.RegistrationConfirmationEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.SuccessfulPasswordUpdate
import de.tschuehly.htmx.spring.supabase.auth.exception.info.*
import org.springframework.web.bind.annotation.ExceptionHandler

interface SupabaseExceptionHandler {
    @ExceptionHandler(MissingCredentialsException::class)
    fun handleMissingCredentialsException(exception: MissingCredentialsException): Any

    @ExceptionHandler(InvalidLoginCredentialsException::class)
    fun handleInvalidLoginCredentialsException(exception: InvalidLoginCredentialsException): Any

    @ExceptionHandler(UserNeedsToConfirmEmailBeforeLoginException::class)
    fun handleUserNeedsToConfirmEmail(exception: UserNeedsToConfirmEmailBeforeLoginException): Any

    @ExceptionHandler(RegistrationConfirmationEmailSent::class)
    fun handleSuccessfulRegistration(exception: RegistrationConfirmationEmailSent): Any

    @ExceptionHandler(PasswordRecoveryEmailSent::class)
    fun handlePasswordRecoveryEmailSent(exception: PasswordRecoveryEmailSent): Any

    @ExceptionHandler(SuccessfulPasswordUpdate::class)
    fun handleSuccessfulPasswordUpdate(exception: SuccessfulPasswordUpdate): Any

    @ExceptionHandler(OtpEmailSent::class)
    fun handleOtpEmailSent(exception: OtpEmailSent): Any

    @ExceptionHandler(UserAlreadyRegisteredException::class)
    fun handleUserAlreadyRegisteredException(exception: UserAlreadyRegisteredException): Any

    @ExceptionHandler(NewPasswordShouldBeDifferentFromOldPasswordException::class)
    fun handlePasswordChangeError(exception: NewPasswordShouldBeDifferentFromOldPasswordException): Any

    @ExceptionHandler(UnknownSupabaseException::class)
    fun handleUnknownSupabaseException(exception: UnknownSupabaseException): Any

}