package de.tschuehly.htmx.spring.supabase.auth.exception.info

class UserNeedsToConfirmEmailForEmailChangeException(email: String) :
    Exception("User: $email needs to confirm email")
