package data

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.File

import java.io.Serializable
import java.math.BigDecimal


data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val data: TokenData
)

data class TokenData(
    val idUser: String?,
    val token: String,
    val refreshToken: String?,
    val authenticated: Boolean
)

data class RefreshTokenRequest(
    val token: String
)


data class RegisterRequest(
    val account: String,
    val email: String,
    val passworduser: String,
    val confirmpassword: String
)

data class RegisterResponse(
    val email: String?,
    val account: String?,
    val success: Boolean?,
    val message: String?
)

data class IntrospectRequest(
    val token: String
)

data class IntrospectResponse(
    val data: Data
)

data class Data(
    val valid: Boolean
)

data class VerifyOTPResponse(
    val headers: Map<String, String>,
    val body: String,
    val statusCode: String,
    val statusCodeValue: Int
)

data class ChangePasswordRequest(
    val password: String,
    val repeatPassword: String
)

data class ChangePasswordResponse(
    val headers: Map<String, String>,
    val body: String,
    val statusCode: String,
    val statusCodeValue: Int
)

data class RoomRequest(
    val id_rs: String,
    val id_type: String,
    val name_room: String,
    val price: Double,
    val status: String,
    val describe_room: String,
    val image: String? = null
)

data class CreatedAt(
    @SerializedName("created_at")
    val created_at: String?
)

data class Resort(
    val idRs: String,
    val name_rs: String,
    val location_rs: String,
    val describe_rs: String,
    val image: String?,
    val star: Double,
    var favorite: Boolean,
    val rooms: List<Room>,
    val evaluates: List<Evaluate>?
)

@Parcelize
data class Room(
    val idRoom: String,
    val name_room: String,
    val type_room: String,
    val price: Double,
    val status: String,
    val describe_room: String,
    val image: String?
) : Parcelable

data class RoomResponse(
    val data: List<Room>?
)

data class ResortResponse(
    val data: List<Resort>?
)

data class Evaluate(
    val idEvaluate: String,
    val user_comment: String,
    val star_rating: Double,
    val created_date: String
)

data class ResortDetailResponse(
    val message: String,
    val data: ResortDetail
)

data class ResortDetail(
    val idRs: String,
    val name_rs: String,
    val location_rs: String,
    val describe_rs: String,
    val image: String?,
    val star: Double,
    val favorite: Boolean
)

data class CreateResortRequest(
    val idOwner: String,
    val name_rs: String,
    val location_rs: String,
    val describe_rs: String
)

data class CreateRoomRequest(
    val id_rs: String,
    val id_type: String,
    val name_room: String,
    val price: Double,
    val status: String,
    val describe_room: String,
)

data class CreateResortResponse(
    val message: String,
    val data: Resort?
)

data class CreateRoomResponse(
    val message: String,
    val data: Room?
)

data class RoomDraft(
    val name_room: String,
    val type_room: String,
    val price: Double,
    val status: String,
    val describe_room: String,
    val image: File? = null
) : Serializable

data class TypeRoom(
    val id_type: String,
    val nameType: String
)

data class TypeRoomResponse(
    val data: List<TypeRoom>?
)

data class UpdateResortRequest(
    val name_rs: String,
    val location_rs: String,
    val describe_rs: String
)

data class UpdateRoomRequest(
    val name_room: String,
    val id_type: String,
    val price: Double,
    val status: String,
    val describe_room: String,
)

data class User(
    val idUser: String,
    val nameuser: String?,
    val sex: String?,
    val email: String,
    val phone: String?,
    val identificationCard: String?,
    val dob: String?,
    val passport: String?,
    val account: String,
    val role_user: String?,
    val avatar: String?
)

data class ListUserResponse(
    val data: List<User>?
)

data class CreateUserRequest(
    val name_user: String,
    val sex: String,
    val phone: String,
    val email: String,
    val identification_card: String,
    val dob: String,
    val passport: String,
    val account: String,
    val passworduser: String,
)


data class CreateUserResponse(
    val message: String,
    val data: UserData
)

data class Role(
    val name: String,
    val desciption: String,
    val permission: List<String>?
)

data class UpdateUserRequest(
    val nameuser: String,
    val sex: String,
    val phone: String,
    val email: String,
    val identificationCard: String,
    val dob: String,
    val passport: String,
    val account: String,
    val passworduser: String,
    val role_user: List<String>
)



data class UserData(
    val idUser: String,
    val nameuser: String?,
    val sex: String,
    val phone: String,
    val email: String,
    val identificationCard: String?,
    val dob: String,
    val passport: String,
    val account: String,
    val role_user: List<Role>,
    val avatar: String
)

data class FavouriteListData(
    val resortId: String,
    val resortName: String,
    val imageUrl: String,
    val created_at: String?
)

data class  FavouriteResponse(
    val data: List<FavouriteListData>?
)

data class FavoriteRequest(
    @SerializedName("id_rs")
    val idRs: String,

    @SerializedName("id_user")
    val idUser: String
)

data class FavoriteResponse(
    val message: String,
    val data: CreatedAt?
)


data class Service(
    val idService: String,
    val name_sv: String,
    val price: BigDecimal,
    val describe_service: String
)

data class ServiceListResponse(
    val data: List<Service>?
)

data class CreateServiceRequest(
    val id_rs: String,
    val name_sv: String,
    val price: BigDecimal,
    val describe_service: String
)

data class CreateServiceResponse(
    val name_sv: String,
    val price: BigDecimal,
    val describe_service: String
)


//Lưu thông tin Service với số lượng
@Parcelize
data class ServiceWithQuantity(
    val id_sv: String,
    val name: String,
    var quantity: Int,
    val describe_service: String,
    val price: BigDecimal
) : Parcelable



data class ServiceBookingRequest(
    val id_sv: String,
    val quantity: Int
)

data class UpdateServiceRequest(
    val name_sv: String,
    val price: BigDecimal,
    val describe_service: String
)

data class UpdateServiceResponse(
    val name_sv: String,
    val price: BigDecimal,
    val describe_service: String
)

data class CreateBookingRoomRequest(
    val id_user: String,
    val id_room: String,
    val checkinday: String,
    val checkoutday: String,
    val services: List<ServiceBookingRequest>?
)

data class ServiceBookingRoom(
    val nameService: String,
    val quantity: Int,
    val total_amount: BigDecimal
)

data class CreateBookingRoomResponse(
    val message: String,
    val data: BookingRoomData?
)

data class BookingRoomData(
    val idBr: String,
    val checkinday: String,
    val checkoutday: String,
    val total_amount: BigDecimal,
    val services: List<ServiceBookingRoom>
)

data class CreateCheckOutRequest(
    val id_br: String,
    val payment_method: String
)

data class CreateCheckOutResponse(
    val money: BigDecimal,
    val create_date: String,
    val status: String,
    val payment_method: String
)

//Booking Room
data class GetListBookingRoomResponse(
    val data: List<DataBookingRoom>?
)

data class ResortBookingRoom(
    val name_rs: String,
    val location_rs: String,
    val image: String?,
)

data class RoomBookingRoom(
    val name_room: String,
    val type_room: String,
    val price: BigDecimal,
    val image: String?
)

data class DataBookingRoom(
    val idBr: String,
    val idResort: String,
    val checkinday: String,
    val checkoutday: String,
    val total_amount: BigDecimal,
    val status: String,
    val resortResponse: ResortBookingRoom,
    val roomResponse: RoomBookingRoom,
    val services: List<ServiceBookingRoom>
)

//Booking room detail
data class GetInfoBookingRoomResponse(
    val data: DataBookingRoom?
)

data class ServiceUpdate(
    val id_sv: String,
    val quantity: Int
)
//Update booking room
data class UpdateBookingRoom(
    val idBr: String,
    val checkinday: String,
    val checkoutday: String,
    val services: List<ServiceUpdate>?
)

data class ReportListRequest(
    val idResort: String,
    val reportYear: Int
)

//data class Detail(
//    val type: String,
//    val amount: BigDecimal,
//    val createDate: String
//)

data class DataReportListResponse(
    val idReport: String,
    val reportMonth: Int,
    val reportYear: Int,
    val totalRevenue: BigDecimal,
    val totalExpense: BigDecimal,
    val netProfit: BigDecimal
)

data class ReportListResponse(
    val data: List<DataReportListResponse>?
)

data class CreateExpenseRequest(
    val idResort: String,
    val category: String,
    val amount: BigDecimal
)

data class CreateExpenseResponse(
    val idExpense: String,
    val category: String,
    val amount: BigDecimal,
    val create_date: String
)

data class Earn(
    val totalAmount: BigDecimal,
    val roomResponse: RoomBookingRoom?
)

data class Expense(
    val idExpense: Int,
    val category: String,
    val amount: BigDecimal,
    val createDate: String
)

sealed class TransactionItem {
    data class EarnItem(val earn: Earn) : TransactionItem()
    data class ExpenseItem(val expense: Expense) : TransactionItem()
}

fun DataBookingRoom.toEarn(): Earn {
    return Earn(
        totalAmount = this.total_amount,
        roomResponse = this.roomResponse
    )
}
