package de.tschuehly.htmx.spring.supabase.auth.exception

class OverEmailSendRateException(val email: String): Exception("The E-Mail $email hit the send rate limit") {
}