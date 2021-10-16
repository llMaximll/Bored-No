package com.example.bored_no.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bored_no.MainRepository
import com.example.bored_no.data.Request
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel constructor(private val mainRepository: MainRepository) : ViewModel() {

    val errorMessage = MutableLiveData<String>()
    val request = MutableLiveData<Request>()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }
    val loading = MutableLiveData<Boolean>()

    var typeRequest: String? = null
    var accessibilityRequest: Float? = null
    var participantsRequest: Int? = null
    var priceRequest: Float? = null

    fun getRandom() {
        loading.value = true

        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {

            val response = mainRepository.getRandom(
                type = typeRequest,
                accessibility = accessibilityRequest,
                participants = participantsRequest,
                price = priceRequest
            )

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    request.postValue(response.body())
                    loading.value = false
                } else {
                    onError("Error : ${response.message()} ")
                }
            }
        }
    }

    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }
}