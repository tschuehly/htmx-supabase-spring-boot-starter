package de.tschuehly.supabasesecurityspringbootstarter.exception.info

class UserAlreadyRegisteredException(email: String) : Exception("User: $email has tried to sign up again, but he was already registered")
