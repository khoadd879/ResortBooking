package data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.File

import java.io.Serializable



data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val data: TokenData
)

data class TokenData(
    val idUser: String,
    val token: String,
    val refreshToken: String,
    val authenticated: Boolean
)

data class RefreshTokenRequest(
    val refreshToken: String
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

data class CreatedAt(
    @SerializedName("created_at")
    val created_at: String?
)

data class Favorite(
    val resortId: String,
    val resortName: String,
    val imageUrl: String,
    val created_at: String
)

data class FavoriteListResponse(
    val data: List<Favorite>?
)

data class Resort(
    val idRs: String,
    val name_rs: String,
    val location_rs: String,
    val describe_rs: String,
    val image: String?,
    val star: Double,
    val favorite: Boolean,
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
//dtk thêm cho dữ liệu chạy
data class Favourite(
    val id: String,
    val name: String,
    val location: String,
    val imageUrl: String,
    val isFavorite: Boolean,
    val rating: Float
)