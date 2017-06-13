package com.ponthaitay.introductionrx

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ponthaitay.introductionrx.service.model.MovieDao
import com.ponthaitay.introductionrx.service.model.UserInfoDao
import com.ponthaitay.introductionrx.service.providesAPIs
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    val listTest = mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Observable.fromIterable(listTest).subscribe { Log.e("loop array", it.toString()) }

        Observable.fromArray(listTest)
                .map { retrieveListTest(it) }
                .subscribe { Log.e("POND", it.toString()) }

        // unused Rx call APIs
//        providesAPIs("https://api.github.com/").getUserInfo("pondthaitay")
//                .enqueue(object : Callback<UserInfoDao> {
//                    override fun onResponse(call: Call<UserInfoDao>?, response: Response<UserInfoDao>?) {
//                        if (response?.isSuccessful!!) {
//                            println("unused Rx [case success] : " + response.body()?.name)
//                        } else {
//                            println("unused Rx [case error] : " + response.message())
//                        }
//                    }
//
//                    override fun onFailure(call: Call<UserInfoDao>?, t: Throwable?) {
//                        println("unused Rx [case error network] : " + t?.message)
//                    }
//                })

        // used Rx Call APIs
        // Rx operator -> flatMap [case 1]
//        observableMovie().flatMap({ observableUserInfoGitHub() }, { r1, r2 -> r1.to(r2) })
//                .subscribe({
//                    println("operator flatMap [case 1] : " + it?.first?.body()?.result)
//                    println("operator flatMap [case 1] : " + it?.second?.body()?.name)
//                }, { it.printStackTrace() })
//
//        // Rx operator -> flatMap [case 2]
//        observableMovieError().flatMap({ observableUserInfoGitHub() }, { r1, r2 -> r1.to(r2) })
//                .subscribe({
//                    println("operator flatMap [case 2] : " + it?.first?.body()?.result)
//                    println("operator flatMap [case 2] : " + it?.second?.body()?.name)
//                }, { it.printStackTrace() })

        // Rx operator -> flatMap [case 3]
        observableMovie().flatMap({
           t: Response<MovieDao>? -> if(t?.body() != null) Observable. else observableUserInfoGitHub()
        }, { r1 : Response<MovieDao>?, r2 : Response<UserInfoDao>? -> r1.to(r2) })
                .subscribe({
//                    println("operator flatMap [case 3] : " + it?.first?.body()?.result)
//                    println("operator flatMap [case 3] : " + it?.second?.body()?.name)
                }, { it.printStackTrace() })


//        // Rx operator -> zip
//        Observable.zip(observableMovie(), observableUserInfoGitHub(), BiFunction<Response<MovieDao>, Response<UserInfoDao>,
//                Pair<Response<MovieDao>, Response<UserInfoDao>>> { r1, r2 -> r1.to(r2) })
//                .subscribe({
//                    println("operator zip : " + it?.first?.body()?.result)
//                    println("operator zip : " + it?.second?.body()?.name)
//                }, { it.printStackTrace() })
//
//        // Rx operator -> map
//        observableUserInfoGitHub().map {
//            println("operator map before retrieveUserInfo : " + it?.body()?.company + "\t" + it?.body()?.email)
//            retrieveUserInfo(it)
//        }.subscribe({ println("operator map after retrieveUserInfo : " + it?.body()?.company + "\t" + it?.body()?.email) },
//                { it.printStackTrace() })
    }

    private fun observableUserInfoGitHub() = providesAPIs("https://api.github.com/")
            .getUserInfoGitHub("pondthaitay")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturn { Response.success(null) }

    private fun observableMovie() = providesAPIs("https://api.themoviedb.org/3/discover/")
            .getMovie("popularity.desc", 1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturn { Response.success(null) }

    private fun observableMovieError() = providesAPIs("https://api.themoviedb.org/3/")
            .getMovie("popularity.desc", 1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturn { Response.success(null) }

    private fun retrieveUserInfo(response: Response<UserInfoDao>?): Response<UserInfoDao>? {
        response?.body()?.company = "20Scoops"
        response?.body()?.email = "jt@20Scoops.com"
        return response
    }

    private fun retrieveListTest(it: MutableList<Int>?): MutableList<Int>? {
        it?.set(0, it[0].plus(99))
        return it
    }
}