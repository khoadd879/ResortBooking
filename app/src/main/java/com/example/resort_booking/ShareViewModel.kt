package com.example.resort_booking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _favoritesUpdated = MutableLiveData<Boolean>()
    val favoritesUpdated: LiveData<Boolean> = _favoritesUpdated

    fun triggerUpdate() {
        _favoritesUpdated.value = true
    }
    fun resetUpdate() {
        _favoritesUpdated.value = false
    }
}