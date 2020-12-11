package com.handysparksoft.data

sealed class Result<out T : Any?> {
    val <T> T.exhaustive: T
        get() = this

    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(
        val isNetworkError: Boolean,
        val code: Int? = null,
        val errorResponse: String? = null
    ) : Result<Nothing>()

    object Loading : Result<Nothing>()
}
