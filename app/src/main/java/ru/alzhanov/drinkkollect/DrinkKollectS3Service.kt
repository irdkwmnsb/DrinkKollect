package ru.alzhanov.drinkkollect

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import java.util.Date

class DrinkKollectS3Service(host: String, accessKey: String, secretKey: String) {
    var s3: AmazonS3Client? = AmazonS3Client(BasicAWSCredentials(accessKey, secretKey),
        Region.getRegion("us-east-1"),
        ClientConfiguration()
    )
    init {
        s3?.endpoint = host
        s3?.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build())
    }

    fun getUrl(bucket: String, key: String): String {
        return s3?.generatePresignedUrl(bucket, key, Date(Date().time + 60000))?.toString() ?: ""
    }
}