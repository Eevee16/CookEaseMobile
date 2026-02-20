package com.cookease.app.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookease.app.Recipe
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

sealed class SearchResultsState {
    object Idle : SearchResultsState()
    object Loading : SearchResultsState()
    data class Success(val recipes: List<Recipe>, val query: String) : SearchResultsState()
    data class Empty(val query: String) : SearchResultsState()
    data class Error(val message: String) : SearchResultsState()
    object NoQuery : SearchResultsState()
}

class SearchResultsViewModel(private val supabase: SupabaseClient) : ViewModel() {

    private val _state = MutableLiveData<SearchResultsState>(SearchResultsState.Idle)
    val state: LiveData<SearchResultsState> = _state

    fun search(query: String) {
        if (query.isBlank()) {
            _state.value = SearchResultsState.NoQuery
            return
        }
        _state.value = SearchResultsState.Loading
        viewModelScope.launch {
            runCatching {
                supabase.postgrest["recipes"]
                    .select {
                        filter {
                            eq("status", "approved")
                            ilike("title", "%$query%")
                        }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Recipe>()
            }.onSuccess { data ->
                _state.value = if (data.isEmpty()) SearchResultsState.Empty(query)
                else SearchResultsState.Success(data, query)
            }.onFailure {
                _state.value = SearchResultsState.Error("Failed to search recipes. Please try again.")
            }
        }
    }
}