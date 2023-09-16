package io.navendra.retrofitkotlindeferred.service

import io.navendra.retrofitkotlindeferred.utils.Constants

object ApiFactory{


    val retrofitApi : RetrofitApi = RetrofitFactory.retrofit(Constants.BASE_URL)
        .create(RetrofitApi::class.java)
}