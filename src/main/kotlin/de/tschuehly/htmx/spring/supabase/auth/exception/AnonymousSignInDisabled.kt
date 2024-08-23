package de.tschuehly.htmx.spring.supabase.auth.exception

class AnonymousSignInDisabled :
    Exception("You need to enable anonymous signIn to allow unauthenticated users to access the application") {

}
