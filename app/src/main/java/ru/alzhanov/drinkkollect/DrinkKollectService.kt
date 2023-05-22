package ru.alzhanov.drinkkollect

import android.util.Log
import drinkollect.v1.DrinkollectGrpc
import drinkollect.v1.DrinkollectOuterClass
import drinkollect.v1.DrinkollectOuterClass.S3Resource
import io.grpc.CallCredentials
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.Metadata.ASCII_STRING_MARSHALLER
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.schedulers.Schedulers
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

    private fun <RequestType> tokenAchievingRequest(
        observer: Observer<Unit>,
        request: RequestType,
        requestAction: (RequestType) -> String
    ) {
        Observable.create(ObservableOnSubscribe<Unit> { emitter ->
            try {
                val token = requestAction(request)
                onGotJwt(token)
            } catch (e: io.grpc.StatusRuntimeException) {
                Log.e("DrinkKollectService", e.toString())
                emitter.onError(e)
            }
            emitter.onComplete()
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    fun registerRequest(observer: Observer<Unit>, username: String, password: String) {
        val request = DrinkollectOuterClass.RegisterRequest
            .newBuilder()
            .setUsername(username)
            .setPassword(password)
            .build()
        this.username = username
        tokenAchievingRequest(observer, request) { req ->
            service.register(req).token
        }
    }

    fun loginRequest(observer: Observer<Unit>, username: String, password: String) {
        val request = DrinkollectOuterClass.LoginRequest
            .newBuilder()
            .setUsername(username)
            .setPassword(password)
            .build()
        this.username = username
        tokenAchievingRequest(observer, request) { req ->
            service.login(req).token
        }
    }

    private fun <RequestType, T> listAchievingRequest(
        observer: Observer<MutableList<T>>,
        request: RequestType,
        requestAction: (RequestType) -> MutableList<T>
    ) {
        Observable.create { emitter ->
            try {
                emitter.onNext(requestAction(request))
            } catch (e: io.grpc.StatusRuntimeException) {
                Log.i("request error: ", e.status.toString())
                emitter.onError(e)
            }
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    fun listUserPostsRequest(
        observer: Observer<MutableList<DrinkollectOuterClass.Post>>,
        username: String
    ) {
        val request =
            DrinkollectOuterClass.ListUserPostsRequest
                .newBuilder()
                .setUsername(username)
                .build()
        listAchievingRequest(observer, request) { req ->
            service.listUserPosts(req).postsList
        }
    }

    fun listPostsRequest(
        observer: Observer<MutableList<DrinkollectOuterClass.Post>>
    ) {
        val request =
            DrinkollectOuterClass.ListPostsRequest
                .newBuilder()
                .build()
        listAchievingRequest(observer, request) { req ->
            service.listPosts(req).postsList
        }
    }


    fun listUsersRequest(observer: Observer<MutableList<String>>, username: String) {
        val request =
            DrinkollectOuterClass.ListUsersRequest
                .newBuilder()
                .setUsername(username)
                .build()
        listAchievingRequest(observer, request) { req ->
            service.listUsers(req).usernamesList
        }
    }

    fun listFriendRequestsRequest(observer: Observer<MutableList<String>>) {
        val request = DrinkollectOuterClass.ListFriendRequestsRequest
            .newBuilder()
            .build()
        listAchievingRequest(observer, request) { req ->
            service.listFriendRequests(req).usernamesList
        }
    }

    private fun <RequestType> nothingAchievingRequest(
        observer: Observer<Unit>,
        request: RequestType,
        requestAction: (RequestType) -> Unit
    ) {
        Observable.create(ObservableOnSubscribe<Unit> { emitter ->
            try {
                requestAction(request)
            } catch (e: Exception) {
                e.message.orEmpty().let { Log.i("request error: ", it) }
                emitter.onError(e)
            }
            emitter.onComplete()
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    fun sendFriendRequest(observer: Observer<Unit>, username: String) {
        val request = DrinkollectOuterClass.SendFriendRequestRequest
            .newBuilder()
            .setUsername(username)
            .build()
        nothingAchievingRequest(observer, request) { req ->
            service.sendFriendRequest(req)
        }
    }

    fun acceptFriendRequest(observer: Observer<Unit>, username: String) {
        val request = DrinkollectOuterClass.AcceptFriendRequestRequest
            .newBuilder()
            .setUsername(username)
            .build()
        nothingAchievingRequest(observer, request) { req ->
            service.acceptFriendRequest(req)
        }
    }

    fun rejectFriendRequest(observer: Observer<Unit>, username: String) {
        val request = DrinkollectOuterClass.RejectFriendRequestRequest
            .newBuilder()
            .setUsername(username)
            .build()
        nothingAchievingRequest(observer, request) { req ->
            service.rejectFriendRequest(req)
        }
    }

    fun createPostRequest(
        observer: Observer<Unit>,
        title: String,
        description: String?,
        location: String?,
        image: S3Resource?
    ) {
        val request = DrinkollectOuterClass.CreatePostRequest
            .newBuilder()
            .setTitle(title)
            .setDescription(description)
            .setLocation(location)
            .setImage(image)
            .build()
        nothingAchievingRequest(observer, request) { req ->
            service.createPost(req)
        }
    }

    fun togglePostLikeRequest(observer: Observer<Unit>, id: Long) {
        val request = DrinkollectOuterClass.TogglePostLikeRequest
            .newBuilder()
            .setId(id)
            .build()
        nothingAchievingRequest(observer, request) { req ->
            service.togglePostLike(req)
        }
    }

    fun changePasswordRequest(observer: Observer<Unit>, oldPassword: String, newPassword: String) {
        Observable.create(ObservableOnSubscribe<Unit> { emitter ->
            try {
                val request = DrinkollectOuterClass.ChangePasswordRequest
                    .newBuilder()
                    .setPassword(oldPassword)
                    .setNewPassword(newPassword)
                    .build()
                service.changePassword(request)
            } catch (e: Exception) {
                e.message.orEmpty().let { Log.i("request error: ", it) }
                emitter.onError(e)
            }
            emitter.onComplete()
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    fun logout() {
        onLostJwt()
        username = null
    }

    override fun close() {
        channel.shutdownNow()
    }
}