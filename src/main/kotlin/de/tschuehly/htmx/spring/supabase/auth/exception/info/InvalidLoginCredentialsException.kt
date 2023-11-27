package de.tschuehly.htmx.spring.supabase.auth.exception.info

class InvalidLoginCredentialsException(email: String) :
    Exception("User: $email has tried to login with invalid credentials") {

}
