package com.dudek.dicodingstory.data.api

import com.dudek.dicodingstory.data.response.LogInResponse
import com.dudek.dicodingstory.data.response.RegisterResponse
import com.dudek.dicodingstory.data.response.StoriesResponse
import com.dudek.dicodingstory.data.response.StoryDetailResponse
import com.dudek.dicodingstory.data.response.StoryUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LogInResponse

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @Multipart
    @POST("stories")
    suspend fun uploadStory(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lat") lat: RequestBody? = null,
        @Part("lon") lon: RequestBody? = null
    ): StoryUploadResponse

    @GET("stories")
    suspend fun getAllStories(
        @Header("Authorization") token: String
        // @Query("page") page: Int = 1,
        // @Query("size") size: Int = 0
    ): StoriesResponse

    @GET("stories/{id}")
    suspend fun getStoryDetail(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): StoryDetailResponse
}
