package com.shoppi.myfirstday.models.interfaces

import com.shoppi.myfirstday.models.Event
import com.shoppi.myfirstday.models.User
import retrofit2.Call
import retrofit2.http.*

interface loginAPI {

    // retrofit 통해 통실할 interface
    // post: 서버에 데이터 보낼때
    @POST("/api/members/kakao/android/")
    fun getLoginResponse(@Body user: User): Call<String>

    @POST("/api/events")
    fun getCreateEvent(@Body event: Event): Call<String>


}