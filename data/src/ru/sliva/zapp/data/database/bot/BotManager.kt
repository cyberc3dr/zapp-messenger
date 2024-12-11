package ru.sliva.zapp.data.database.bot

import ru.sliva.zapp.data.Bot

interface BotManager {

    suspend fun findBotByID(id: Int, fromDb: Boolean = false) : Bot?
    suspend fun findBotByUsername(username: String, fromDb: Boolean = false) : Bot?
    fun getAllBots() : List<Bot>
}