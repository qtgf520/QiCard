package com.qtwl.icu.iiicu.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qtwl.icu.iiicu.model.ShareCard
import com.qtwl.icu.iiicu.util.CardStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 主页面 ViewModel
 */
class MainViewModel : ViewModel() {

    private val _cards = MutableStateFlow<List<ShareCard>>(emptyList())
    val cards: StateFlow<List<ShareCard>> = _cards.asStateFlow()

    fun loadCards(context: Context) {
        viewModelScope.launch {
            _cards.value = CardStorage.loadCards(context)
        }
    }

    fun addCard(context: Context, title: String, content: String, url: String, imageUrl: String) {
        val card = ShareCard(
            title = title,
            content = content,
            url = url,
            imageUrl = imageUrl
        )
        viewModelScope.launch {
            CardStorage.addCard(context, card)
            _cards.value = CardStorage.loadCards(context)
        }
    }

    fun updateCard(context: Context, card: ShareCard) {
        viewModelScope.launch {
            CardStorage.updateCard(context, card)
            _cards.value = CardStorage.loadCards(context)
        }
    }

    fun deleteCard(context: Context, id: Long) {
        viewModelScope.launch {
            CardStorage.deleteCard(context, id)
            _cards.value = CardStorage.loadCards(context)
        }
    }
}
