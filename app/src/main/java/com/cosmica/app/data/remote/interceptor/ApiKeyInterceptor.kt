package com.cosmica.app.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.newBuilder()
            .addQueryParameter("api_key", apiKey)
            .build()
        return chain.proceed(chain.request().newBuilder().url(url).build())
    }
}
