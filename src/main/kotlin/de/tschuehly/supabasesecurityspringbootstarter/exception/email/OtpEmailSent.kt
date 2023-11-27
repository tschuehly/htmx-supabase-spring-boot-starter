package de.tschuehly.supabasesecurityspringbootstarter.exception.email

class OtpEmailSent(email: String) : Exception("OTP sent to $email")
