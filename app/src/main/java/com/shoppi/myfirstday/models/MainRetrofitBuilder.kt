package com.shoppi.myfirstday.models

import com.shoppi.myfirstday.models.interfaces.dataAPI

class MainRetrofitBuilder {

    var dataApi: dataAPI

    init {
        val retrofit = GlobalApplication.getRetrofit()
        dataApi = retrofit.create(dataAPI::class.java)
    }
}