package de.tschuehly.htmx.spring.supabase.auth.exception.info

class UserAlreadyRegisteredException(val email: String) :
    Exception("User: $email has tried to sign up again, but he was already registered")
