package ru.alzhanov.drinkkollect

import android.util.Log
import drinkollect.v1.DrinkollectGrpc
import drinkollect.v1.DrinkollectOuterClass
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
        service = DrinkollectGrpc.newBlockingStub(channel).withCallCredentials(object:
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

    private fun <RequestType> tokenAchievingRequest(observer: Observer<Unit>,
                                                    request: RequestType,
                                                    requestAction: (RequestType) -> String) {
        Observable.create(ObservableOnSubscribe<Unit> { emitter ->
            try {
                val token = requestAction(request)
                onGotJwt(token)
            } catch (e: Exception) {
                e.message.orEmpty().let { Log.i("request error: ", it) }
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
    }

    override fun close() {
        channel.shutdownNow()
    }
}