package ru.alzhanov.drinkkollect

import android.util.Log
import com.amazonaws.ClientConfiguration
import com.amazonaws.HttpMethod
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.io.InputStream
import java.util.Date


class DrinkKollectS3Service(host: String, accessKey: String, secretKey: String) {
    var s3: AmazonS3Client? = AmazonS3Client(
        BasicAWSCredentials(accessKey, secretKey),
        Region.getRegion("us-east-1"),
        ClientConfiguration()
    )
    private val client = OkHttpClient()

    init {
        s3?.endpoint = host
        s3?.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build())
    }

    fun getUrl(bucket: String, key: String): String {
        return s3?.generatePresignedUrl(bucket, key, Date(Date().time + 60000))?.toString() ?: ""
    }

    fun getUploadUrl(bucket: String, key: String): String {
        return s3?.generatePresignedUrl(
            GeneratePresignedUrlRequest(bucket, key)
                .withMethod(HttpMethod.PUT)
                .withExpiration(Date(Date().time + 60000))
        )?.toString() ?: ""
    }

    fun createUploadRequest(
        observerUpload: Observer<Unit>,
        bucket: String,
        key: String,
        it: InputStream
    ) {
        val url = getUploadUrl(bucket, key)
        val request = Request.Builder()
            .url(url)
            .put(RequestBody.create(null, it.readBytes()))
            .build()
        Observable.create { emitter ->
            try {
                val response: Response = client.newCall(request).execute()
                val body = response.body()?.string()
                Log.d("DrinkKollectS3Service", "createUploadRequest: $body")
                if (!response.isSuccessful) {
                    emitter.onError(IOException("Unexpected code $response"))
                }
                emitter.onNext(Unit)
                emitter.onComplete()
            } catch (e: IOException) {
                emitter.onError(e)
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observerUpload)
    }
}