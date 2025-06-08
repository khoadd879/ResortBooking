package interfaceAPI

import data.ChangePasswordRequest
import data.ChangePasswordResponse
import data.CreateBookingRoomRequest
import data.CreateBookingRoomResponse
import data.CreateCheckOutRequest
import data.CreateCheckOutResponse
import data.CreateExpenseRequest
import data.CreateExpenseResponse
import data.CreateResortResponse
import data.CreateRoomResponse
import data.CreateServiceRequest
import data.CreateServiceResponse
import data.CreateUserResponse
import data.DataBookingRoom
import data.Expense
import data.FavoriteRequest
import data.FavoriteResponse
import data.FavouriteResponse
import data.GetInfoBookingRoomResponse
import data.GetListBookingRoomResponse
import data.IntrospectRequest
import data.IntrospectResponse
import data.ListUserResponse
import data.LoginRequest
import data.LoginResponse
import data.Payment
import data.RefreshTokenRequest
import data.RegisterRequest
import data.RegisterResponse
import data.ReportListRequest
import data.ReportListResponse
import data.ResortDetailResponse
import data.ResortResponse
import data.Room
import data.RoomResponse
import data.ServiceListResponse
import data.TypeRoomResponse
import data.UpdateBookingRoom
import data.UpdateServiceRequest
import data.UpdateServiceResponse
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

    @POST("https://booking-resort-final.onrender.com/api/auth/refresh")
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
    fun changePassword(
        @Path("email") email: String,
        @Body request: ChangePasswordRequest
    ): Call<ChangePasswordResponse>

    @GET("api/resort/list_resort/{idUser}")
    fun getResortList(@Path("idUser") idUser: String): Call<ResortResponse>

    @GET("api/resort/{idResort}/{idUser}")
    fun getResortById(
        @Path("idResort") idRs: String,
        @Path("idUser") idUser: String
    ): Call<ResortDetailResponse>

    @GET("api/resort/list_resort_created/{idOwner}")
    fun getResortListCreated(@Path("idOwner") idOwner: String): Call<ResortResponse>

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
        @Path("resortID") idRs: String,
        @Part("request") requestBody: RequestBody,
        @Part file: MultipartBody.Part,
        @Header("Authorization") token: String
    ): Call<CreateResortResponse>

    @DELETE("api/resort/delete/{resortID}")
    fun deleteResort(@Path("resortID") resortID: String): Call<Void>

    @Multipart
    @PUT("api/room/update/{idRoom}")
    fun updateRoom(
        @Path("idRoom") idRoom: String,
        @Part("request") requestBody: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<CreateRoomResponse>

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
    ): Call<CreateUserResponse>

    @DELETE("api/user/delete/{idUser}")
    fun deleteUser(@Path("idUser") idUser: String): Call<Void>

    @GET("api/favorite_resort/list_favorite/{idUser}")
    fun getListFavourite(@Path("idUser") idUser: String): Call<FavouriteResponse>

    @POST("api/favorite_resort/create_favorite")
    fun createFavorite(@Body favoriteRequest: FavoriteRequest): Call<FavoriteResponse>

    @DELETE("api/favorite_resort/delete_favorite/{idUser}/{idResort}")
    fun deleteFavorite(
        @Path("idUser") idUser: String,
        @Path("idResort") idResort: String
    ): Call<Void>

    @GET("/api/room/list_room/{idResort}")
    fun getListRoomById(@Path("idResort") idResort: String): Call<RoomResponse>

    @GET("/api/room/inf_room/{idRoom}")
    fun getRoomById(@Path("idRoom") idRoom: String): Call<Room>

    @GET("api/service/list_service/{idResort}")
    fun getListService(@Path("idResort") idResort: String): Call<ServiceListResponse>

    @POST("api/service/create_service")
    fun createService(@Body requestBody: CreateServiceRequest): Call<CreateServiceResponse>

    @PUT("api/service/update_service/{idService}")
    fun updateService(
        @Path("idService") idService: String,
        @Body requestBody: UpdateServiceRequest
    ): Call<UpdateServiceResponse>

    @DELETE("api/service/delete_service/{idService}")
    fun deleteService(@Path("idService") idService: String): Call<Void>

    @POST("api/booking_room/create_bookingroom")
    fun createBookingRoom(@Body requestBody: CreateBookingRoomRequest): Call<CreateBookingRoomResponse>

    @POST("api/payment/create_payment")
    fun createCheckOut(@Body requestBody: CreateCheckOutRequest): Call<CreateCheckOutResponse>

    @GET("api/booking_room/list_bookingroom/{idUser}")
    fun getListBookingRoom(@Path("idUser") idUser: String): Call<GetListBookingRoomResponse>

    @GET("api/booking_room/inf_bookingroom/{idBookingRoom}")
    fun getInfoBookingRoom(@Path("idBookingRoom") idBookingRoom: String): Call<GetInfoBookingRoomResponse>

    @PUT("api/booking_room/change_bookingroom/{idBookingRoom}")
    fun updateBookingRoom(@Path("idBookingRoom") idBookingRoom: String, @Body requestBody: UpdateBookingRoom): Call<CreateBookingRoomResponse>

    @POST("api/report/list_report")
    fun getListReport(@Body requestBody: ReportListRequest): Call<ReportListResponse>

    @POST("api/expense/create_expense")
    fun createExpense(@Body requestBody: CreateExpenseRequest): Call<CreateExpenseResponse>

    @GET("/api/expense/list_expense/{resortId}")
    suspend fun getExpenses(@Path("resortId") resortId: String): Call<List<Expense>>

    @GET("/api/booking_room/list_bookingroom_resort/{resortId}")
    suspend fun getBookings(@Path("resortId") resortId: String): Call<List<DataBookingRoom>>

        @GET("payment/list_payment/{idUser}")
        fun getPayments(@Path("idUser") idUser: Int): Call<List<Payment>>

}
