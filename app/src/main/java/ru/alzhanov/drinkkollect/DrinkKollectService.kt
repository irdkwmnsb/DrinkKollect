package ru.alzhanov.drinkkollect

import android.util.Log
import drinkollect.v1.DrinkollectGrpc
import drinkollect.v1.DrinkollectOuterClass
import drinkollect.v1.DrinkollectOuterClass.S3Resource
import io.grpc.CallCredentials
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.Metadata.ASCII_STRING_MARSHALLER
import java.io.Closeable
import java.util.concurrent.Executor


class DrinkKollectService(host: String, port: Int) : Closeable {
    val AUTHORIZATION_METADATA_KEY: Metadata.Key<String> =
        Metadata.Key.of("authorization", ASCII_STRING_MARSHALLER)

    private var jwt: String? = null
    private var username: String? = null
    fun getUsername(): String? {
        return username
    }

    private val channel = ManagedChannelBuilder
        .forAddress(host, port)
        .usePlaintext()
        .enableRetry()
        .build()

    private var service = DrinkollectGrpc.newBlockingStub(channel)

    private fun onLostJwt() {
        jwt = null
        service = DrinkollectGrpc.newBlockingStub(channel)
    }

    private fun onGotJwt(token: String) {
        jwt = token
        service = DrinkollectGrpc.newBlockingStub(channel).withCallCredentials(object :
            CallCredentials() {
            override fun applyRequestMetadata(
                requestInfo: RequestInfo?,
                appExecutor: Executor?,
                applier: MetadataApplier?
            ) {
                val headers = Metadata()
                headers.put(AUTHORIZATION_METADATA_KEY, "Bearer " + jwt!!)
                applier?.apply(headers)
            }

            override fun thisUsesUnstableApi() {
            }
        })
    }

    fun registerRequest(username: String, password: String) {
        try {
            val request = DrinkollectOuterClass.RegisterRequest
                .newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build()
            val response = service.register(request)
            onGotJwt(response.token)
            this.username = username
        } catch (e: Exception) {
            e.message.orEmpty().let { Log.i("request error: ", it) }
            throw e
        }
    }

    fun loginRequest(username: String, password: String) {
        try {
            val request = DrinkollectOuterClass.LoginRequest
                .newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build()
            val response = service.login(request)
            onGotJwt(response.token)
            this.username = username
        } catch (e: Exception) {
            e.message.orEmpty().let { Log.i("request error: ", it) }
            throw e
        }
    }

    fun listUserPostsRequest(username: String): MutableList<DrinkollectOuterClass.Post>? {
        try {
            val request =
                DrinkollectOuterClass.ListUserPostsRequest
                    .newBuilder()
                    .setUsername(username)
                    .build()
            val response = service.listUserPosts(request)
            return response.postsList
        } catch (e: Exception) {
            e.message.orEmpty().let { Log.i("request error: ", it) }
            throw e
        }
    }

    fun listPostsRequest(): MutableList<DrinkollectOuterClass.Post>? {
        try {
            val request = DrinkollectOuterClass.ListPostsRequest.newBuilder().build()
            val response = service.listPosts(request)
            return response.postsList
        } catch (e: Exception) {
            e.message.orEmpty().let { Log.i("request error: ", it) }
            throw e
        }
    }

    fun createPostRequest(
        title: String,
        description: String?,
        location: String?,
        image: S3Resource?
    ) {
        try {
            val request = DrinkollectOuterClass.CreatePostRequest
                .newBuilder()
                .setTitle(title)
                .setDescription(description)
                .setLocation(location)
                .setImage(image)
                .build()
            service.createPost(request)
        } catch (e: Exception) {
            e.message.orEmpty().let { Log.i("request error: ", it) }
            throw e
        }
    }

    fun changePasswordRequest(oldPassword: String, newPassword: String) {
        try {
            val request = DrinkollectOuterClass.ChangePasswordRequest
                .newBuilder()
                .setPassword(oldPassword)
                .setNewPassword(newPassword)
                .build()
            service.changePassword(request)
        } catch (e: Exception) {
            e.message.orEmpty().let { Log.i("request error: ", it) }
            throw e
        }
    }

    fun togglePostLikeRequest(id: Long) {
        try {
            val request = DrinkollectOuterClass.TogglePostLikeRequest
                .newBuilder()
                .setId(id)
                .build()
            service.togglePostLike(request)
        } catch (e: Exception) {
            e.message.orEmpty().let { Log.i("request error: ", it) }
            throw e
        }
    }

    fun logout() {
        onLostJwt()
        username = null
    }

    override fun close() {
        channel.shutdownNow()
    }
}