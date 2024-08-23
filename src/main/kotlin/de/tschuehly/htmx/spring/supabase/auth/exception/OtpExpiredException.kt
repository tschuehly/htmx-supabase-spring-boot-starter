package de.tschuehly.htmx.spring.supabase.auth.exception

class OtpExpiredException(email: String) : Exception("The One Time Password is expired for email: $email") {

}
