package de.tschuehly.htmx.spring.supabase.auth.exception.email

class SuccessfulPasswordUpdate(email: String?) :
    Exception("User with the mail: $email updated his password successfully") {

}
