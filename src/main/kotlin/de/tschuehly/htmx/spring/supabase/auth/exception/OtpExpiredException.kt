package de.tschuehly.htmx.spring.supabase.auth.exception

class OtpExpiredException(val email: String) : Exception("The One Time Password is expired for email: $email") {

}
