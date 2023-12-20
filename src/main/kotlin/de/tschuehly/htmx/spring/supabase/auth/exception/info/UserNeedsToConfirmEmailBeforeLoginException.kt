package de.tschuehly.htmx.spring.supabase.auth.exception.info

class UserNeedsToConfirmEmailBeforeLoginException(email: String) :
    Exception("User: $email needs to confirm email before he can login")
