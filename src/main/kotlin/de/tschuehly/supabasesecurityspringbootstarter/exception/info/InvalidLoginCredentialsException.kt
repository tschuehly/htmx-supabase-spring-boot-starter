package de.tschuehly.supabasesecurityspringbootstarter.exception.info

class InvalidLoginCredentialsException(email: String) : Exception("User: $email has tried to login with invalid credentials") {

}
