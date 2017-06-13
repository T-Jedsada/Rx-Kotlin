package com.ponthaitay.introductionrx.service

import com.ponthaitay.introductionrx.service.model.MovieDao
import com.ponthaitay.introductionrx.service.model.UserInfoDao
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface APIs {

    @GET("movie?api_key=6c26bbd637c722ffab43dc6984053411")
    fun getMovie(@Query("sort_by") sortBy: String, @Query("page") page: Int): Observable<Response<MovieDao>>

    @GET("users/{username}")
    fun getUserInfoGitHub(@Path("username") username: String): Observable<Response<UserInfoDao>>

    @GET("users/{username}")
    fun getUserInfo(@Path("username") username: String): Call<UserInfoDao>
}