package interfaceAPI

import data.ChangePasswordRequest
import data.ChangePasswordResponse
import data.CreateResortResponse
import data.CreateRoomResponse
import data.CreateUserResponse
import data.FavoriteRequest
import data.FavoriteResponse
import data.IntrospectRequest
import data.IntrospectResponse
import data.ListUserResponse
import data.LoginRequest
import data.LoginResponse
import data.RefreshTokenRequest
import data.RegisterRequest
import data.RegisterResponse
import data.ResortDetailResponse
import data.ResortResponse
import data.TypeRoomResponse
import data.VerifyOTPResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @POST("https://booking-resort-final.onrender.com/api/auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("https://booking-resort-final.onrender.com/api/auth/introspect")
    fun introspect(@Body request: IntrospectRequest): Call<IntrospectResponse>

    @POST("https://booking-resort-final.onrender.com/api/auth/refresh-token")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<LoginResponse>

    @POST("https://booking-resort-final.onrender.com/api/user/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("https://booking-resort-final.onrender.com/api/auth/logout")
    fun logout(@Body request: RefreshTokenRequest): Call<Void>

    @POST("https://booking-resort-final.onrender.com/forgotPassword/verifyEmail/{email}")
    fun verifyEmail(@Path("email") email: String): Call<Void>

    @POST("https://booking-resort-final.onrender.com/forgotPassword/verifyOtp/{otp}/{email}")
    fun verifyOTP(@Path("otp") otp: String, @Path("email") email: String): Call<VerifyOTPResponse>

    @POST("https://booking-resort-final.onrender.com/forgotPassword/changePassword/{email}")
    fun changePassword(@Path("email") email: String, @Body request: ChangePasswordRequest): Call<ChangePasswordResponse>

    @GET("api/resort/list_resort/{idUser}")
    fun getResortList(@Path("idUser") idUser: String): Call<ResortResponse>

    @POST("api/favorite_resort/create_favorite")
    fun createFavorite(@Body request: FavoriteRequest): Call<FavoriteResponse>

    @GET("api/favorite_resort/list_favorite/{idUser}")
    fun getListOfFavorite(@Path("idUser") idUser: String): Call<FavoriteResponse>

    @GET("api/resort/{idRs}")
    fun getResortById(@Path("idRs") idRs: String): Call<ResortDetailResponse>

    @Multipart
    @POST("api/resort")
    fun createResort(
        @Part("request") requestBody: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<CreateResortResponse>

    @Multipart
    @POST("api/room/create_room")
    fun createRoom(
        @Part("request") requestBody: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<CreateRoomResponse>

    @GET("api/typeroom/list_typeroom")
    fun getListTypeRoom(): Call<TypeRoomResponse>

    @Multipart
    @PUT("api/resort/update/{resortID}")
    fun updateResort(
        @Path ("resortID") idRs: String,
        @Part ("request") requestBody: RequestBody,
        @Part file: MultipartBody.Part,
        @Header("Authorization") token: String
    ): Call<CreateResortResponse>

    @DELETE("api/resort/delete/{resortID}")
    fun deleteResort(@Path("resortID") resortID: String): Call<Void>

    @Multipart
    @PUT("api/room/update/{idRoom}")
    fun updateRoom(@Path ("idRoom") idRoom: String,
                  @Part ("request") requestBody: RequestBody,
                  @Part file: MultipartBody.Part) : Call<CreateRoomResponse>

    @DELETE("api/room/delete/{idRoom}")
    fun deleteRoom(@Path("idRoom") idRoom: String): Call<Void>

    @GET("api/user/list_user")
    fun getListUser(): Call<ListUserResponse>

    @Multipart
    @POST("api/user/create_user")
    fun createUser(
        @Part("request") requestBody: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<CreateUserResponse>

    @Multipart
    @PUT("api/user/{idUser}")
    fun updateUser(
        @Path("idUser") idUser: String,
        @Part("request") requestBody: RequestBody,
        @Part file: MultipartBody.Part
    ) : Call<CreateUserResponse>

    @DELETE("api/user/delete/{idUser}")
    fun deleteUser(@Path("idUser") idUser: String): Call<Void>
}
