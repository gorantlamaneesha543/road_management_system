package com.example.a31mod2case2

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>

    @Multipart
    @POST("api/inspect")
    suspend fun inspectRoad(
        @Part image: MultipartBody.Part,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody
    ): Response<InspectionResponse>

    @GET("api/summary")
    suspend fun getSummary(): Response<SummaryResponse>
}
