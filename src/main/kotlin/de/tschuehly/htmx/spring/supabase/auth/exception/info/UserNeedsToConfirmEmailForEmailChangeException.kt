package de.tschuehly.htmx.spring.supabase.auth.exception.info

class UserNeedsToConfirmEmailForEmailChangeException(val email: String, val newEmailSent: Boolean) :
    Exception("User: $email needs to confirm email")
