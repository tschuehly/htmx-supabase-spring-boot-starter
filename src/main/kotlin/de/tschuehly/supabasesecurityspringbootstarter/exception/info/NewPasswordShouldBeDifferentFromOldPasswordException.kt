package de.tschuehly.supabasesecurityspringbootstarter.exception.info

class NewPasswordShouldBeDifferentFromOldPasswordException(email: String):
    Exception("User: $email tried to set a new password that was the same as the old one") {
}