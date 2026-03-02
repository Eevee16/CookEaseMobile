package com.cookease.app

import io.github.jan.supabase.SupabaseClient

object SupabaseProvider {
    val client: SupabaseClient
        get() = SupabaseClientProvider.client
}
