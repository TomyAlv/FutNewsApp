package com.example.espnapp.ui.more

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class SportItem(val title: String, val key: String)

class MenuViewModel : ViewModel() {

    // Static list (if you want to load it from network/DB later, just change it here)
    private val baseSports = listOf(
        SportItem("Fútbol (general)", "soccer"),
        SportItem("Tenis", "tennis"),
        SportItem("Básquet", "basketball"),
        SportItem("Béisbol", "baseball"),
        SportItem("Fórmula 1", "f1"),
        SportItem("MMA", "mma"),
        SportItem("NFL (Fútbol americano)", "football"),
        SportItem("Hockey", "hockey")
    )

    private val _sports = MutableLiveData<List<SportItem>>(baseSports)
    val sports: LiveData<List<SportItem>> = _sports

    // Optional: small local filter if you need it later
    fun filter(query: String?) {
        val q = query?.trim()?.lowercase().orEmpty()
        if (q.isEmpty()) {
            _sports.value = baseSports
        } else {
            _sports.value = baseSports.filter { it.title.lowercase().contains(q) }
        }
    }
}
