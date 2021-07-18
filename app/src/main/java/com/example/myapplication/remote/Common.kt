package com.example.myapplication.remote

import com.example.myapplication.model.nearbyplaces.Results

object Common {

    private val URL = "https://maps.googleapis.com/"

    var currentResult: Results? = null

    val googleAPI: GoogleAPI
        get() = RetrofitClient.getClient(URL).create(GoogleAPI::class.java)

    val googleAPIScalars: GoogleAPI
        get() = RetrofitScalarClient.getClient(URL).create(GoogleAPI::class.java)
}