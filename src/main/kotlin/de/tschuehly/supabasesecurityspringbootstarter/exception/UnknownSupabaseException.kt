package de.tschuehly.supabasesecurityspringbootstarter.exception

class UnknownSupabaseException(supabaseMessage: String = "Something went wrong when communicating with Supabase") : Exception(supabaseMessage)
