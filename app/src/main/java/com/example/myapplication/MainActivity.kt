package com.example.myimage

import AppResponse
import RetrofitInterface
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    lateinit var mBtImageSelect: Button
    lateinit var mBtImageShow: Button
//    private var mProgressBar: ProgressBar? = null
    private var mImageUrl = ""
    lateinit var iss:InputStream
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mBtImageSelect = findViewById(R.id.btn_select_image)
        mBtImageShow = findViewById(R.id.btn_show_image)


        mBtImageSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/jpeg"
            try {
                startActivityForResult(intent, INTENT_REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }
        mBtImageShow.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(mImageUrl)
            startActivity(intent)
        }


        val save = findViewById<Button>(R.id.save)
        save.setOnClickListener {
            uploadImage(getBytes(iss))
        }
    }



    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INTENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    iss = contentResolver.openInputStream(data!!.data!!)!!
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(IOException::class)
    fun getBytes(iss: InputStream?): ByteArray {
        val byteBuff = ByteArrayOutputStream()
        val buffSize = 1024
        val buff = ByteArray(buffSize)
        var len: Int
        while (iss!!.read(buff).also { len = it } != -1) {
            byteBuff.write(buff, 0, len)
        }
        return byteBuff.toByteArray()
    }

    private fun uploadImage(imageBytes: ByteArray) {
        val retrofit = Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val retrofitInterface: RetrofitInterface = retrofit.create(
            RetrofitInterface::class.java
        )
        val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes)
        val body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile)
        val call: Call<AppResponse?>? = retrofitInterface.uploadImage(body)
        call?.enqueue(object : Callback<AppResponse?> {
            override fun onResponse(call: Call<AppResponse?>, response: Response<AppResponse?>) {
                if (response.isSuccessful) {
                    Log.e("status code" , response.code().toString())
                    val responseBody = response.body()
                    Toast.makeText(applicationContext , "running ${response.code()}" , Toast.LENGTH_SHORT).show()
                    Toast.makeText(applicationContext , "FilePath ${responseBody!!.FilePath.toString()}" , Toast.LENGTH_SHORT).show()
                    Toast.makeText(applicationContext , "FileLength ${responseBody.FileLength.toString()}" , Toast.LENGTH_SHORT).show()
                    Toast.makeText(applicationContext , "FileLength ${responseBody.FileLength.toString()}" , Toast.LENGTH_SHORT).show()
                    Toast.makeText(applicationContext , "FileCreatedTime ${responseBody.FileCreatedTime.toString()}" , Toast.LENGTH_SHORT).show()

                    Snackbar.make(
                        findViewById(R.id.content),
                        responseBody.FileName.toString(),
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    val errorBody = response.errorBody()
                    val gson = Gson()
                    try {
                        val errorResponse: Response<*>? =
                            gson.fromJson(errorBody!!.string(), Response::class.java)
                        Log.e("error status code" , response.code().toString())
                        Log.e("error coming" , errorResponse.toString())

                        Toast.makeText(applicationContext , "Pressed ${errorResponse.toString()}" , Toast.LENGTH_SHORT).show()
                        Toast.makeText(applicationContext , "error coming" , Toast.LENGTH_SHORT).show()

                        Snackbar.make(
                            findViewById(R.id.content),
                            errorResponse.toString(),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            override fun onFailure(call: Call<AppResponse?>, t: Throwable) {
                Toast.makeText(applicationContext , "Pressed ${t.localizedMessage}" , Toast.LENGTH_SHORT).show()

                Log.d(TAG, "onFailure: " + t.localizedMessage)
            }
        })
    }

    companion object {
        val TAG = MainActivity::class.java.simpleName
        private const val INTENT_REQUEST_CODE = 100
        const val URL = "http://103.190.95.186"
    }
}