package com.example.myapplication.remote

import com.example.myapplication.model.nearbyplaces.PlacesModel
import com.example.myapplication.model.distance.Root
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import io.reactivex.Observable

interface GoogleAPI {
    @GET
    fun getNearbyPlaces(@Url url:String) : Call<PlacesModel>

    @GET
    fun getDirections(@Url url: String): Call<com.example.myapplication.model.direction.Root>

    @GET
    fun getDuration(@Url url: String) : Call<Root>
}