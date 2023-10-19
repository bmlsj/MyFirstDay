package com.shoppi.myfirstday.models

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kakao.sdk.common.KakaoSdk
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class GlobalApplication: Application() {

    companion object {

        // SharedPreferences 관련 문자열
        const val SP_TAG = "HEADER"    // 제목
        const val AUTHORIZATION = "Authorization"    // 저장할 키 값
        private const val BASE_URL = "http://192.168.0.10:8080"

        lateinit var sSharedPreferences: SharedPreferences
        private lateinit var retrofit: Retrofit

        private var gson: Gson = GsonBuilder().setLenient().create()

        @JvmName("getRetrofit1")
        fun getRetrofit(): Retrofit {

            if (!this::retrofit.isInitialized) {

                val client = OkHttpClient.Builder()
                    .readTimeout(50000, TimeUnit.MILLISECONDS)
                    .connectTimeout(50000, TimeUnit.MILLISECONDS) // 5000
                    .addNetworkInterceptor(XAccessTokenInterceptor()) // JWT 자동 헤더 전송
                    .build()

                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }

            return retrofit
        }
    }


    override fun onCreate() {

        super.onCreate()

        KakaoSdk.init(this, "d05298ca45943dde8fde0fb8ac745d2c")
        // sp 객체 초기화
        sSharedPreferences = applicationContext.getSharedPreferences(SP_TAG, Context.MODE_PRIVATE)

    }
}