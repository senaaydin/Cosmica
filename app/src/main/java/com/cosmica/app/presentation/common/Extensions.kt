package com.cosmica.app.presentation.common

import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toUserMessage(): String = when (this) {
    is UnknownHostException  -> "No internet connection. Please check your network."
    is SocketTimeoutException -> "Request timed out. Please try again."
    is HttpException -> when (code()) {
        429  -> "API rate limit exceeded. Try again later."
        404  -> "The requested resource was not found."
        else -> "Server error (${code()}). Please try again."
    }
    else -> message?.takeIf { it.isNotBlank() }
        ?: "Something went wrong. Please try again."
}
