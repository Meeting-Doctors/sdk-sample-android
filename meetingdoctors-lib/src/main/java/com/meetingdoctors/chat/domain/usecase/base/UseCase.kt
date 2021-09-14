package com.meetingdoctors.chat.domain.usecase.base

import com.meetingdoctors.chat.domain.executor.SchedulersFacade
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.observers.CallbackCompletableObserver
import io.reactivex.internal.observers.ConsumerSingleObserver
import io.reactivex.internal.observers.LambdaObserver
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.subscribers.ResourceSubscriber
import java.util.concurrent.TimeUnit

abstract class UseCase<TYPE, PARAMS> internal constructor(
        val schedulersFacade: SchedulersFacade
) {

    val disposables = CompositeDisposable()

    fun dispose() {
        disposables.dispose()
    }

    protected abstract fun build(params: PARAMS): TYPE

    /**
     *
     * @param <R> Response type class
     * @param <P> Params class
    </P></R> */
    abstract class RxSingleUseCase<TYPE, PARAMS>(schedulersFacade: SchedulersFacade) :
            UseCase<Single<TYPE>, PARAMS>(schedulersFacade) {


        fun execute(observer: DisposableSingleObserver<TYPE>, params: PARAMS) {
            disposables.add(
                    build(params)
                            .subscribeOn(schedulersFacade.io())
                            .observeOn(schedulersFacade.ui())
                            .subscribeWith(observer)
            )
        }

        fun execute(success: Consumer<TYPE>, error: Consumer<Throwable>, params: PARAMS) {
            disposables.add(
                    build(params)
                            .subscribeOn(schedulersFacade.io())
                            .observeOn(schedulersFacade.ui())
                            .subscribeWith(ConsumerSingleObserver<TYPE>(success, error))
            )
        }

        fun execute(
                success: ((TYPE) -> Unit) = {},
                error: ((Throwable) -> Unit) = {},
                params: PARAMS
        ) {
            disposables.add(
                    build(params)
                            .subscribeOn(schedulersFacade.io())
                            .observeOn(schedulersFacade.ui())
                            .subscribe(success, error)
            )
        }

        fun execute(params: PARAMS) {
            execute({}, {}, params = params)
        }

        /**
         * Get result synchronously.
         * WARNING! Use this method just if you really need it and is not possible to use
         * asynchronous [execute] implementation
         */
        @JvmOverloads
        fun syncExecute(params: PARAMS, errorCallback: ((Throwable) -> Unit)? = null, timeout: Long = -1L): TYPE? {
            val single = build(params)
                    .subscribeOn(schedulersFacade.io())
                    .observeOn(schedulersFacade.io())
                    .doOnError {
                        errorCallback?.invoke(it)
                    }
            return if (timeout > 0) {
                single.timeout(timeout, TimeUnit.MILLISECONDS).blockingGet()
            } else {
                single.blockingGet()
            }
        }
    }

    /**
     *
     * @param <R> Response type class
     * @param <P> Params class
    </P></R> */
    abstract class RxObserverUseCase<R, P>(schedulersFacade: SchedulersFacade) :
            UseCase<Observable<R>, P>(schedulersFacade) {

        fun execute(observer: DisposableObserver<R>, params: P) {
            disposables.add(
                    build(params)
                            .subscribeOn(schedulersFacade.io())
                            .observeOn(schedulersFacade.ui())
                            .subscribeWith(observer)
            )
        }

        fun execute(success: Consumer<R>, error: Consumer<Throwable>, params: P) {
            disposables.add(
                    build(params)
                            .subscribeOn(schedulersFacade.io())
                            .observeOn(schedulersFacade.ui())
                            .subscribeWith(
                                    LambdaObserver<R>(
                                            success,
                                            error,
                                            Functions.EMPTY_ACTION,
                                            Functions.emptyConsumer()
                                    )
                            )
            )
        }
    }

    /**
     *
     * @param <R> Response type class
     * @param <P> Params class
    </P></R> */
    abstract class RxCompletableUseCase<P>(schedulersFacade: SchedulersFacade) :
            UseCase<Completable, P>(schedulersFacade) {

        fun execute(observer: DisposableCompletableObserver, params: P) {
            disposables.add(
                    build(params)
                            .subscribeOn(schedulersFacade.io())
                            .observeOn(schedulersFacade.ui())
                            .subscribeWith(observer)
            )
        }

        fun execute(onComplete: Action, error: Consumer<Throwable>, params: P) {
            disposables.add(
                    build(params)
                            .subscribeOn(schedulersFacade.io())
                            .observeOn(schedulersFacade.ui())
                            .subscribeWith(
                                    CallbackCompletableObserver(
                                            error,
                                            onComplete
                                    )
                            )
            )
        }

        fun execute(onComplete: (() -> Unit) = {}, error: ((Throwable) -> Unit) = {}, params: P) {
            disposables.add(
                    build(params)
                            .subscribeOn(schedulersFacade.io())
                            .observeOn(schedulersFacade.ui())
                            .subscribeWith(
                                    CallbackCompletableObserver(
                                            error,
                                            onComplete
                                    )
                            )
            )
        }

        @JvmOverloads
        fun execute(params: P, onComplete: (() -> Unit) = {}, error: ((Throwable) -> Unit) = {}) {
            disposables.add(
                    build(params)
                            .subscribeOn(schedulersFacade.io())
                            .observeOn(schedulersFacade.ui())
                            .subscribeWith(
                                    CallbackCompletableObserver(
                                            error,
                                            onComplete
                                    )
                            )
            )
        }
    }

    /**
     *
     * @param <R> Response type class
     * @param <P> Params class
    </P></R> */
    abstract class RxFlowableUseCase<R, P>(schedulersFacade: SchedulersFacade) :
            UseCase<Flowable<R>, P>(schedulersFacade) {

        fun execute(observer: ResourceSubscriber<R>, params: P) {
            disposables.add(
                    build(params)
                            .subscribeOn(schedulersFacade.io())
                            .observeOn(schedulersFacade.ui())
                            .subscribeWith(observer)
            )
        }

        fun execute(success: ((R) -> Unit), error: ((Throwable) -> Unit), params: P) {
            val subscriber = object : ResourceSubscriber<R>() {
                override fun onComplete() {
                    // Do nothing
                }

                override fun onNext(t: R) {
                    success.invoke(t)
                }

                override fun onError(t: Throwable?) {
                    error.invoke(t ?: Throwable("Error on use case: ${this.javaClass}"))
                }
            }

            disposables.add(
                    build(params)
                            .subscribeOn(schedulersFacade.io())
                            .observeOn(schedulersFacade.ui())
                            .subscribeWith(subscriber)
            )
        }

        fun execute(success: Consumer<R>, error: Consumer<Throwable>, params: P) {
            val subscriber = object : ResourceSubscriber<R>() {
                override fun onComplete() {
                    // Do nothing
                }

                override fun onNext(t: R) {
                    success.accept(t)
                }

                override fun onError(t: Throwable?) {
                    error.accept(t ?: Throwable("Error on use case: ${this.javaClass}"))
                }
            }

            disposables.add(
                    build(params)
                            .subscribeOn(schedulersFacade.io())
                            .observeOn(schedulersFacade.ui())
                            .subscribeWith(subscriber)
            )
        }
    }
}