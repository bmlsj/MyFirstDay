package com.shoppi.myfirstday.models.interfaces

import retrofit2.Call
import retrofit2.http.GET

interface dataAPI {


    @GET("/api/events/progressing")
    fun getProgressingEvents(): Call<String>

    // Call<List<InvitationEvent>>

    @GET("/api/events/done")
    fun getDoneEvents(): Call<String>

}