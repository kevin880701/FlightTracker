package com.lhr.flighttracker.features.exchangeRate.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lhr.flighttracker.features.exchangeRate.domain.usecase.GetExchangeRatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExchangeRateViewModel @Inject constructor(
    private val getExchangeRatesUseCase: GetExchangeRatesUseCase
) : ViewModel() {

    private val _exchangeRates = MutableStateFlow<Map<String, Double>>(emptyMap())
    val exchangeRates: StateFlow<Map<String, Double>> = _exchangeRates

    private val _inputAmount = MutableStateFlow("1.0")
    val inputAmount: StateFlow<String> = _inputAmount

    val amountAsDouble: StateFlow<Double> = _inputAmount.map {
        it.toDoubleOrNull() ?: 0.0
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    private val _baseCurrency = MutableStateFlow("USD")
    val baseCurrency: StateFlow<String> = _baseCurrency

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


    init {
        fetchData()
    }


    fun fetchData(currencies: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            getExchangeRatesUseCase(
                baseCurrency = _baseCurrency.value, // 使用 ViewModel 內的狀態
                currencies = currencies
            )
                .onSuccess { rates ->
                    _exchangeRates.value = rates
                }
                .onFailure { error ->
                    error.printStackTrace()
                }
            _isLoading.value = false
        }
    }

    // 更新使用者輸入金額
    fun updateInputAmount(amount: String) {
        _inputAmount.value = amount
    }

    // 更新基準貨幣，並重新載入資料
    fun updateBaseCurrency(newBaseCurrency: String) {
        _baseCurrency.value = newBaseCurrency
        // 當基準貨幣改變時，重新載入匯率資料
        fetchData(
            currencies = "AUD,BGN,BRL,CAD,CHF,CNY,CZK,DKK,EUR,GBP,HKD,HRK,HUF,IDR,ILS,INR,ISK,JPY,KRW,MXN,MYR,NOK,NZD,PHP,PLN,RON,RUB,SEK,SGD,THB,TRY,ZAR"
        )
    }
}