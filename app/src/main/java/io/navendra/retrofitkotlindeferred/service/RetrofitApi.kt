package io.navendra.retrofitkotlindeferred.service

import io.navendra.retrofitkotlindeferred.data.CallRequest
import io.navendra.retrofitkotlindeferred.data.CallResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface RetrofitApi{

    @Headers("Content-Type: application/vnd.avaya.csa.tokens.v1+json; charset=utf-8")
    @POST("token-generation-service/token/getEncryptedToken")
    fun getAccesToken(@Body callRequest: CallRequest): Deferred<Response<CallResponse>>
}