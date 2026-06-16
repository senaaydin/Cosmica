package com.cosmica.app.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/** Generic interceptor that appends a single query parameter to every request. */
class QueryParamInterceptor(
    private val name: String,
    private val value: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.newBuilder()
            .addQueryParameter(name, value)
            .build()
        return chain.proceed(chain.request().newBuilder().url(url).build())
    }
}
