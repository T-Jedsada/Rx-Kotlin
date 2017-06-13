package com.ponthaitay.introductionrx.service

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

fun providesAPIs(url: String): APIs {
    return retrofit2.Retrofit.Builder()
            .baseUrl(url)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(providesOkHttpClient().build())
            .build()
            .create(APIs::class.java)
}

fun providesOkHttpClient(): OkHttpClient.Builder {
    val httpClient = OkHttpClient.Builder()
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    httpClient.addInterceptor(httpLoggingInterceptor)
    return httpClient
}
