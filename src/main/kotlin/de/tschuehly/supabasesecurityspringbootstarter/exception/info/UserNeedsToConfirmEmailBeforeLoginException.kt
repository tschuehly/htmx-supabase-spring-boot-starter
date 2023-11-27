package de.tschuehly.supabasesecurityspringbootstarter.exception.info

class UserNeedsToConfirmEmailBeforeLoginException(email: String) : Exception("User: $email needs to confirm email before he can login")
