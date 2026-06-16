package com.qtwl.icu.iiicu.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.qtwl.icu.iiicu.model.ShareCard
import java.io.File

/**
 * 本地卡片数据管理
 */
object CardStorage {
    private const val FILE_NAME = "share_cards.json"

    private fun getContextFile(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    fun saveCards(context: Context, cards: List<ShareCard>) {
        val file = getContextFile(context)
        val json = Gson().toJson(cards)
        file.writeText(json)
    }

    fun loadCards(context: Context): List<ShareCard> {
        val file = getContextFile(context)
        if (!file.exists()) return emptyList()

        val json = file.readText()
        val type = object : TypeToken<List<ShareCard>>() {}.type
        return try {
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addCard(context: Context, card: ShareCard) {
        val cards = loadCards(context).toMutableList()
        cards.add(0, card)
        saveCards(context, cards)
    }

    fun updateCard(context: Context, card: ShareCard) {
        val cards = loadCards(context).toMutableList()
        val index = cards.indexOfFirst { it.id == card.id }
        if (index != -1) {
            cards[index] = card
            saveCards(context, cards)
        }
    }

    fun deleteCard(context: Context, id: Long) {
        val cards = loadCards(context).filter { it.id != id }
        saveCards(context, cards)
    }
}
