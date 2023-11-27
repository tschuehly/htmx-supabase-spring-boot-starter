package de.tschuehly.htmx.spring.supabase.auth.exception.email

class OtpEmailSent(email: String) : Exception("OTP sent to $email")
