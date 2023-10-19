package com.shoppi.myfirstday.models

import okhttp3.Interceptor
import okhttp3.Response

class XAccessTokenInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val builder = chain.request().newBuilder()

        val jwtToken: String? =
            GlobalApplication.sSharedPreferences.getString(GlobalApplication.AUTHORIZATION, null)

        if (jwtToken != null) {
            builder.addHeader(GlobalApplication.AUTHORIZATION, jwtToken)
        }

        return chain.proceed(builder.build())
    }

}