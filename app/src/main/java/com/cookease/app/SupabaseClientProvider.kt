package com.cookease.app

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SettingsSessionManager
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json


object SupabaseClientProvider {

    private lateinit var _client: SupabaseClient
    val client: SupabaseClient get() = _client

    fun init() {
        _client = createSupabaseClient(
            supabaseUrl = "https://nrorypixaucxuoculxta.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5yb3J5cGl4YXVjeHVvY3VseHRhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njk0ODgxOTcsImV4cCI6MjA4NTA2NDE5N30.BW-Nh1AX2vqdg8OdsVEenl3f4eJ1s3iQC4C64pIC7z8"
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}