package com.ponthaitay.introductionrx

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ponthaitay.introductionrx.service.model.MovieDao
import com.ponthaitay.introductionrx.service.model.UserInfoDao
import com.ponthaitay.introductionrx.service.providesAPIs
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    val TAG = javaClass.simpleName!!
    val listTest = mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**Scenario not Rx*/
        // Loop array
        for ((index, element) in listTest.withIndex()) {
            Log.e(TAG, "the element at $index is $element\n")
        }

        for (element in listTest) {
            Log.e(TAG, "the element is $element\n")
        }

        listTest.forEach { Log.e(TAG, "the element is $it\n") }

        // NetworkOnMainThreadException
        val callUserInfo = providesAPIs("https://api.github.com/").getUserInfo("pondthaitay")

        doAsync {
            val response = callUserInfo.execute()
            uiThread { Log.e(TAG, "${response.body()}") }
        }

//        Observable.fromCallable { callUserInfo.execute() }
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe { Log.e(TAG, "execute : " + "$it") }

        // unused Rx call APIs
        providesAPIs("https://api.github.com/").getUserInfo("pondthaitay")
                .enqueue(object : Callback<UserInfoDao> {
                    override fun onResponse(call: Call<UserInfoDao>?, response: Response<UserInfoDao>?) {
                        if (response?.isSuccessful!!) {
                            Log.e(TAG, "unused Rx [case success] : " + response.body()?.name)
                        } else {
                            Log.e(TAG, "unused Rx [case error] : " + response.message())
                        }
                    }

                    override fun onFailure(call: Call<UserInfoDao>?, t: Throwable?) {
                        Log.e(TAG, "unused Rx [case error network] : " + t?.message)
                    }
                })

        /**Scenario Rx*/

        Observable.range(1, 10)
                .concatMap { it -> Observable.just(it).delay(1, TimeUnit.SECONDS) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { Log.e("delay", "the element is $it\n") }

        // Loop array
        Observable.fromIterable(listTest).subscribe { Log.e(TAG, "the element is $it\n") }

        Observable.fromArray(listTest)
                .map { retrieveListTest(it) }
                .subscribe { Log.e(TAG, "the element is $it\n") }

        // Rx operator -> flatMap [case 1]
        observableMovie().flatMap({ observableUserInfoGitHub() }, { r1, r2 -> r1 to r2 })
                .subscribe({
                    Log.e(TAG, "operator flatMap [case 1] : " + it?.first?.body()?.result)
                    Log.e(TAG, "operator flatMap [case 1] : " + it?.second?.body()?.name)
                }, { it.printStackTrace() })

        // Rx operator -> flatMap [case 2]
        observableMovieError().flatMap({ observableUserInfoGitHub() }, { r1, r2 -> r1 to r2 })
                .subscribe({
                    Log.e(TAG, "operator flatMap [case 2] : " + it?.first?.body()?.result)
                    Log.e(TAG, "operator flatMap [case 2] : " + it?.second?.body()?.name)
                }, { it.printStackTrace() })

        // Rx operator -> flatMap [case 3]
        observableMovie().flatMap({
            if (it.body() != null) ObservableSource { it.onNext(Response.success(null)) }
            else observableUserInfoGitHub()
        }, { r1: Response<MovieDao>?, r2: Response<UserInfoDao>? -> r1 to r2 })
                .subscribe({
                    Log.e(TAG, "operator flatMap [case 3] : " + it?.first?.body()?.result)
                    Log.e(TAG, "operator flatMap [case 3] : " + it?.second?.body()?.name)
                }, { it.printStackTrace() })

        // Rx operator -> zip
        Observable.zip(observableMovie(), observableUserInfoGitHub(), BiFunction<Response<MovieDao>, Response<UserInfoDao>,
                Pair<Response<MovieDao>, Response<UserInfoDao>>> { r1, r2 -> r1 to r2 })
                .subscribe({
                    Log.e(TAG, "operator zip : " + it?.first?.body()?.result)
                    Log.e(TAG, "operator zip : " + it?.second?.body()?.name)
                }, { it.printStackTrace() })

        // Rx operator -> map
        observableUserInfoGitHub().map {
            Log.e(TAG, "operator map before retrieveUserInfo : " + it?.body()?.company + "\t" + it?.body()?.email)
            retrieveUserInfo(it)
        }.subscribe({ Log.e(TAG, "operator map after retrieveUserInfo : " + it?.body()?.company + "\t" + it?.body()?.email) },
                { it.printStackTrace() })
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
        response?.body()?.bio = "Android Developer"
        return response
    }

    private fun retrieveListTest(it: MutableList<Int>?): MutableList<Int>? {
        it?.set(0, it[0].plus(99))
        return it
    }
}