import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface RetrofitInterface {
    @Multipart
    @POST("/multipartapi/api/fileupload/upload")
    fun uploadImage(@Part image: MultipartBody.Part?): Call<AppResponse?>?
}