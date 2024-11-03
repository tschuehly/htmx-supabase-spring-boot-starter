package de.tschuehly.htmx.spring.supabase.auth.exception

class ValidationFailedException(val email: String) : Exception("The validation for the email failed: $email") {

}
