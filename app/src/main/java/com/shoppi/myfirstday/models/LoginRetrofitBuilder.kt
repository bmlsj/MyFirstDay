package com.shoppi.myfirstday.models

import com.shoppi.myfirstday.models.interfaces.loginAPI

class LoginRetrofitBuilder {

    var loginApi: loginAPI

    init {
        val retrofit = GlobalApplication.getRetrofit()
        loginApi = retrofit.create(loginAPI::class.java)
    }
}