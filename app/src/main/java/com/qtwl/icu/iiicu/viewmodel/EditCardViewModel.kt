package com.qtwl.icu.iiicu.viewmodel

import androidx.lifecycle.ViewModel
import com.qtwl.icu.iiicu.model.ShareCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 编辑卡片 ViewModel
 */
class EditCardViewModel : ViewModel() {

    private val _currentCard = MutableStateFlow<ShareCard?>(null)
    val currentCard: StateFlow<ShareCard?> = _currentCard.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    private val _imageUrl = MutableStateFlow("")
    val imageUrl: StateFlow<String> = _imageUrl.asStateFlow()

    fun loadCard(card: ShareCard) {
        _currentCard.value = card
        _title.value = card.title
        _content.value = card.content
        _url.value = card.url
        _imageUrl.value = card.imageUrl
    }

    fun updateTitle(text: String) {
        _title.value = text
    }

    fun updateContent(text: String) {
        _content.value = text
    }

    fun updateUrl(text: String) {
        _url.value = text
    }

    fun updateImageUrl(text: String) {
        _imageUrl.value = text
    }

    fun getCardData(): ShareCard {
        return _currentCard.value?.copy(
            title = _title.value,
            content = _content.value,
            url = _url.value,
            imageUrl = _imageUrl.value
        ) ?: ShareCard(
            title = _title.value,
            content = _content.value,
            url = _url.value,
            imageUrl = _imageUrl.value
        )
    }
}
