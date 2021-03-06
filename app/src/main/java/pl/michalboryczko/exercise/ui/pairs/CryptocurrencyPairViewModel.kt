package pl.michalboryczko.exercise.ui.pairs

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_cryptocurrency_details.*
import pl.michalboryczko.exercise.R
import pl.michalboryczko.exercise.app.BaseViewModel
import pl.michalboryczko.exercise.model.CryptocurrencyPair
import pl.michalboryczko.exercise.model.base.Resource
import pl.michalboryczko.exercise.model.exceptions.ApiException
import pl.michalboryczko.exercise.model.exceptions.NoInternetException
import pl.michalboryczko.exercise.source.repository.CryptocurrencyRepository
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Named


class CryptocurrencyPairViewModel
    @Inject constructor(
            private val repository : CryptocurrencyRepository,
            @Named("computationScheduler") val computationScheduler: Scheduler,
            @Named("mainScheduler") val mainScheduler: Scheduler

    ) :BaseViewModel() {

    val cryptocurrencyPairs: MutableLiveData<Resource<List<CryptocurrencyPair>>> = MutableLiveData()
    private var cryptocurrencyPairDisposable: Disposable? = null

    init {
        cryptocurrencyPairs.value = Resource.initial()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        cryptocurrencyPairDisposable?.dispose()
        cryptocurrencyPairDisposable = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        val isInitial = cryptocurrencyPairs.value?.status?.isInitial()
        if(isInitial == true){
            getCryptocurrencyPairs()
        }
    }

    fun getCryptocurrencyPairs(){
        cryptocurrencyPairDisposable?.dispose()
        cryptocurrencyPairDisposable = null
        cryptocurrencyPairDisposable =
                repository
                        .getCryptocurrencyPairs()
                        .subscribeOn(computationScheduler)
                        .observeOn(mainScheduler)
                        .doOnSubscribe{ cryptocurrencyPairs.value = Resource.loading() }
                        .subscribe(
                                { cryptocurrencyPairs.value = Resource.success(it) },
                                {
                                    when(it){
                                        is ApiException -> {
                                            cryptocurrencyPairs.value = Resource.error(it.errorMsg)
                                            Timber.d("ERROR ${it.errorMsg}")
                                        }

                                        is NoInternetException -> {
                                            cryptocurrencyPairs.value = Resource.error(R.string.noInternetConnectionError)
                                            Timber.d("internet connection not available")
                                        }

                                        is Exception -> {
                                            cryptocurrencyPairs.value = Resource.error(R.string.unknownError)
                                            Timber.d("${it.localizedMessage}")
                                        }
                                    }
                                }
                        )
    }

    override fun onCleared() {
        super.onCleared()
        cryptocurrencyPairDisposable?.dispose()
    }


}