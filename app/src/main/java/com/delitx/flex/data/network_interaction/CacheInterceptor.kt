package com.delitx.flex.data.network_interaction

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class CacheInterceptor :Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original=chain.request()
        val cacheControl= CacheControl.Builder()
            .maxAge(3, TimeUnit.HOURS)
            .build()
        return chain.proceed(original).newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
            .header("Cache-Control",cacheControl.toString())
            .build()
    }
}