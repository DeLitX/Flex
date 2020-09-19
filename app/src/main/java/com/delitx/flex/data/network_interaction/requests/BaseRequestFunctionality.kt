package com.delitx.flex.data.network_interaction.requests

import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import java.net.CookieManager
import java.net.CookiePolicy

open class BaseRequestFunctionality {
    internal val client: OkHttpClient
    internal val cookieManager = CookieManager()

    init {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        client = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()
    }

    internal fun longsListToJsonIdList(values: List<Long>): String {
        var result = ""
        var isFirst = true
        for (i in values) {
            if (!isFirst) {
                result += " "
            } else {
                isFirst = false
            }
            result += i
        }
        return result
    }
}