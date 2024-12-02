package com.dudek.dicodingstory.data.api

import com.dudek.dicodingstory.data.response.LogInResponse
import com.dudek.dicodingstory.data.response.RegisterResponse
import com.dudek.dicodingstory.data.response.StoriesResponse
import com.dudek.dicodingstory.data.response.StoryDetailResponse
import com.dudek.dicodingstory.data.response.StoryUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LogInResponse>

    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @Multipart
    @POST("stories")
    fun uploadStory(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lat") lat: RequestBody? = null,
        @Part("lon") lon: RequestBody? = null
    ): Call <StoryUploadResponse>

//    @Multipart
//    @POST("stories/guest")
//    fun addStory(
//        @Part("description") description: RequestBody,
//        @Part photo: MultipartBody.Part,
//        @Part("lat") lat: RequestBody? = null,
//        @Part("lon") lon: RequestBody? = null
//    ): Call <StoryUploadResponse>

    @GET("stories")
    fun getAllStories(
        @Header("Authorization") token: String,
//        @Field("page") page: Int,
//        @Field("size") size: Int,
//        @Query("location") location: Int
    ): Call<StoriesResponse>

    @GET("stories/{id}")
    fun getStoryDetail(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Call<StoryDetailResponse>
}