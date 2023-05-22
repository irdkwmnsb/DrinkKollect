package ru.alzhanov.drinkkollect

import android.content.Context
import android.util.Log
import com.auth0.android.jwt.JWT
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


class DrinkKollectService(host: String, port: Int, val activity: MainActivity?) : Closeable {
    val AUTHORIZATION_METADATA_KEY: Metadata.Key<String> =
        Metadata.Key.of("authorization", ASCII_STRING_MARSHALLER)

    private var jwt: String? = loadJwt()
    private var preferencesKey = activity?.getString(R.string.preference_file_key)
    private val sharedPref = activity?.getSharedPreferences(preferencesKey, Context.MODE_PRIVATE)


    fun reloadJwt() {
        jwt = loadJwt()
    }
    private fun loadJwt(): String? {
        val jwt = sharedPref?.getString("storedJwt", null)
        Log.i("DrinkKollectService", "Reading jwt: $jwt")
        return jwt
    }

    private fun storeJwt(jwt: String?) {
        if(sharedPref != null) {
            Log.i("DrinkKollectService", "Storing jwt: $jwt")
            with(sharedPref.edit()) {
                putString("storedJwt", jwt)
                apply()
                commit()
            }
            Log.i("DrinkKollectService", "Sanity check: ${loadJwt()}")
        }
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

    fun getUsername(): String? {
        return jwt?.let { JWT(it).getClaim("username").asString() } ?: run { null }
    }

    fun isLoggedIn(): Boolean {
        return jwt != null
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
        storeJwt(token)
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

    fun listFriendsRequest(observer: Observer<MutableList<String>>, username: String) {
        val request = DrinkollectOuterClass.ListFriendsRequest
            .newBuilder()
            .setUsername(username)
            .build()
        listAchievingRequest(observer, request) { req ->
            service.listFriends(req).usernamesList
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

    fun removeFriendRequest(observer: Observer<Unit>, username: String) {
        val request = DrinkollectOuterClass.RemoveFriendRequest
            .newBuilder()
            .setUsername(username)
            .build()
        nothingAchievingRequest(observer, request) { req ->
            service.removeFriend(req)
        }
    }

    fun logout() {
        onLostJwt()
        storeJwt(null)
    }

    override fun close() {
        channel.shutdownNow()
    }
}