package de.tschuehly.htmx.spring.supabase.auth.exception

import io.github.jan.supabase.auth.exception.AuthRestException


class SupabaseAuthException(exc: AuthRestException, email: String) :
    Exception("Supabase failed for email:$email - ${exc.error} with message: ${exc.message}, code: ${exc.errorCode}", exc) {
    val errorCode = exc.errorCode
    val error: String = exc.error
}
