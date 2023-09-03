package com.example.drivenmap.feat_core.utils

sealed class ResponseState<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : ResponseState<T>(data, null)
    class Error<T>(message: String) : ResponseState<T>(null, message)
    class Loading<T> : ResponseState<T>()
}
