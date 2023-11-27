package de.tschuehly.htmx.spring.supabase.auth.exception.email

import kotlinx.datetime.Instant


class RegistrationConfirmationEmailSent(email: String, confirmationSentAt: Instant?) :
    Exception(
        "User with the mail $email successfully signed up, " +
                "${confirmationSentAt?.let { "Confirmation Mail sent at $it" }}"
    )
