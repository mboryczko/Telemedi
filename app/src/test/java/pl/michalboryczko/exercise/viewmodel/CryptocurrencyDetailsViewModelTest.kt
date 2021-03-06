package pl.michalboryczko.exercise.viewmodel

import android.arch.lifecycle.Observer
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import pl.michalboryczko.exercise.BaseTest
import pl.michalboryczko.exercise.model.base.Resource
import pl.michalboryczko.exercise.model.exceptions.NoInternetException
import pl.michalboryczko.exercise.source.api.InternetConnectivityChecker
import pl.michalboryczko.exercise.source.repository.CryptocurrencyRepository
import junit.framework.Assert.assertEquals
import pl.michalboryczko.exercise.R
import pl.michalboryczko.exercise.model.CryptocurrencyPairDetails
import pl.michalboryczko.exercise.model.PriceStatus
import pl.michalboryczko.exercise.source.PriceStatusChecker
import pl.michalboryczko.exercise.ui.details.CryptocurrencyDetailsViewModel

class CryptocurrencyDetailsViewModelTest: BaseTest(){
    private val pairObserver = mock<Observer<Resource<CryptocurrencyPairDetails>>>()
    private val priceStatusObserver = mock<Observer<PriceStatus>>()
    private val checker = mock(InternetConnectivityChecker::class.java)
    private val repo = mock(CryptocurrencyRepository::class.java)
    private val viewmodel by lazy { CryptocurrencyDetailsViewModel(repo, PriceStatusChecker(), Schedulers.trampoline(), Schedulers.trampoline()) }


    @Before
    fun setUp(){
        whenever(checker.isInternetAvailable()).thenReturn(true)
        viewmodel.cryptocurrencyPairDetails.observeForever(pairObserver)
        viewmodel.priceStatus.observeForever(priceStatusObserver)
    }


    @Test
    fun noInternetTest(){
        whenever(repo.getCryptocurrencyDetails(simpleBtcEthPair)).thenReturn(Flowable.error(NoInternetException()))
        viewmodel.getCryptocurrencyDetails(simpleBtcEthPair)

        assertEquals(Resource.error<NoInternetException>(R.string.noInternetConnectionError), viewmodel.cryptocurrencyPairDetails.value)
    }

    @Test
    fun getCryptocurrencyDetailsTest(){
        whenever(repo.getCryptocurrencyDetails(simpleBtcEthPair)).thenReturn(Flowable.just(pairDetails))
        viewmodel.getCryptocurrencyDetails(simpleBtcEthPair)

        assertEquals(Resource.success(pairDetails), viewmodel.cryptocurrencyPairDetails.value)
    }


}