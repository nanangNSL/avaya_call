package io.navendra.retrofitkotlindeferred.data


data class CallResponse(
    val encryptedToken: String?
)

data class CallRequest(
    val use:String?,
    val calledNumber:String?,
    val callingNumber:String?,
    val displayName:String?,
    val expiration:String?
)