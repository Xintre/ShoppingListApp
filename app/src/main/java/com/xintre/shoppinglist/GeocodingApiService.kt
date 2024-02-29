package com.xintre.shoppinglist

import retrofit2.http.GET
import retrofit2.http.Query

// creating a fragment of url in order to get the location in exchange for queries
interface GeocodingApiService {

    // address where we put the queries
    @GET("maps/api/geocode/json")
    suspend fun getAddressFromCoordinates(
        // we give the info: lattitude and longitude and key in order to get location in exchange
        @Query("latlng") latlng: String,
        @Query("key") apiKey: String
    ): GeocodingResponse
}
