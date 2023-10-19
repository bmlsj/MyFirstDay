package com.shoppi.myfirstday.models

class User {

    var userId: String? = null
        get() {
            return field
        }
        set(value) {
            field = value
        }
    var accessToken: String? = null
        get() {
            return field
        }
        set(value) {
            field = value
        }

    var refreshToken: String? = null
        get() {
            return field
        }
        set(value) {
            field = value
        }
}