package ru.alzhanov.drinkkollect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UsernameViewModel : ViewModel() {
    private val _username = MutableLiveData("")
    val username: LiveData<String> = _username
    fun setUsername(username: String) {
        _username.value = username
    }

    fun hasNoUsernameSet(): Boolean {
        return _username.value.isNullOrEmpty()
    }
}