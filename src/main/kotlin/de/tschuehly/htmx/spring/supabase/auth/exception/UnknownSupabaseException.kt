package de.tschuehly.htmx.spring.supabase.auth.exception

class UnknownSupabaseException(supabaseMessage: String = "Something went wrong when communicating with Supabase") :
    Exception(supabaseMessage)
