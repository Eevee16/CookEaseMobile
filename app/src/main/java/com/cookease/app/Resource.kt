package com.cookease.app

/**
 * Resource - Wrapper class for API responses
 * Handles three states: Loading, Success, Error
 *
 * Usage:
 * - Resource.Loading: Show loading indicator
 * - Resource.Success: Display data
 * - Resource.Error: Show error message
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}