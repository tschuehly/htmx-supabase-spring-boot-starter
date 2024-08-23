package de.tschuehly.htmx.spring.supabase.auth.exception

class WeakPasswordException(email: String) : Exception("Weak password for email: $email") {

}
