package ru.alzhanov.drinkkollect

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import drinkollect.v1.DrinkollectOuterClass.S3Resource
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
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

    fun putUrl(image: S3Resource, imageBin: ByteArray?) {

        val formBody: RequestBody = FormBody.Builder().build()
        val request: Request = Request.Builder()
            .addHeader("content-type", "application/octet-stream")
            .url(getUrl(image.bucket, image.id))
            .post(formBody)
            .build()

        try {
            val response: Response = client.newCall(request).execute()
            // Do something with the response.
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}